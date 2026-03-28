package neopetsmoneymaker;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class NeoBuyer {

    private NeoCaptchaBreaker       neoCaptchaBreaker;
    private NeoDatabaseManager      neoDatabaseManager;
    private int                     neopoints;

    private static int              NOTHING_TO_BUY          = -1,
                                    TRYING_TO_BUY           = 0,
                                    SHOP_VISITED            = 1,
                                    TOO_FAST                = 2,
                                    SOLD_OUT                = 3,
                                    COUNTER_OFFER           = 4,
                                    ITEM_BOUGHT             = 5;
    
    private boolean                 loggedOut;
    
    private int                     status;

    private NeoItem                 nextItemToBuy;
    
    private NeoLogger               log;
    
    private NeoAccount              neoAccount;
    
    public NeoBuyer( NeoAccount neoAccount ) {        
        this.neoAccount = neoAccount;
        neoDatabaseManager = new NeoDatabaseManager();
        neoCaptchaBreaker = new NeoCaptchaBreaker();
        log = new NeoLogger();
    }

    public DefaultHttpClient buySomething( DefaultHttpClient httpClient, int neopoints, List<NeoItem> shopItems, List<NeoItem> inventoryItems ) {

        this.neopoints = neopoints;
        
        nextItemToBuy = neoDatabaseManager.getNextItemToBuy( neopoints, shopItems, inventoryItems, neoAccount );

        if ( nextItemToBuy != null ) {
            httpClient = buy( nextItemToBuy, httpClient );
        } else {
            status = NOTHING_TO_BUY;
        }
            
        return httpClient;
    }

    private DefaultHttpClient buy( NeoItem itemToBuy, DefaultHttpClient httpClient ) {
    
        status = TRYING_TO_BUY;
        loggedOut = false;
            
        log.i( "Trying to buy a '" + itemToBuy.getName() + "' (profit: " + (itemToBuy.getSellPrice()-itemToBuy.getHagglingPrice()) + ")" );

        try {
            
            // Follow haggle-link
            HttpGet httpGet = new HttpGet( itemToBuy.getHaggleLink() );
            List<BasicHeader> defaultHeaders = new ArrayList<BasicHeader>();
            defaultHeaders.add(new BasicHeader( "Referer", itemToBuy.getShopLink() ) );
            httpGet.getParams().setParameter( ClientPNames.DEFAULT_HEADERS, defaultHeaders );
            HttpResponse httpResponse = httpClient.execute( httpGet );
            HttpEntity httpEntity = httpResponse.getEntity();
            String result = EntityUtils.toString( httpEntity );
            EntityUtils.consume( httpEntity );

            status = SHOP_VISITED;

            // Only proceed if buying is still an option and item isn't already sold out
            if ( result.indexOf( "Due to massive demand on the Neopian Shops" ) != -1 ) {
                log.w( "Too fast: massive demand notice." );
                status = TOO_FAST; 
            } else if (  result.indexOf( "Neopets is temporarily offline.") != -1 ) { 
                log.w ( "Neopets is temporarily offline." );
            }else if ( result.toLowerCase().indexOf( "sold out" ) != -1 ) {
                log.i( "Too bad, " + itemToBuy.getName() + " is sold out!" );
                status = SOLD_OUT;
            } else if ( result.indexOf( "Neopets - Hi!" ) != -1 ) {
                log.w( "Error: logged out while buying. Better do something about it!" );
                loggedOut = true;
            } else if ( result.indexOf( "Sorry, you can only carry a maximum of <b>50</b> items at one time!" ) != -1 ) {
                log.i( "Too bad. Inventory is full. Better do something about it!" );
            } else if ( result.indexOf( "Enter the amount you wish to pay" ) != -1 ) {

                // Get through security
                httpClient = neoCaptchaBreaker.breakIt( result, httpClient ); 
                int xPos = neoCaptchaBreaker.getX();
                int yPos = neoCaptchaBreaker.getY();

                // Post bid
                log.i( "Posting bid for " + itemToBuy.getName() );
                
                // Send request to shopkeeper
                HttpPost httpPost = new HttpPost( "http://www.neopets.com/haggle.phtml" );
                defaultHeaders = new ArrayList<BasicHeader>();
                defaultHeaders.add(new BasicHeader( "Referer", itemToBuy.getHaggleLink() ) );
                httpPost.getParams().setParameter( ClientPNames.DEFAULT_HEADERS, defaultHeaders );
                List <NameValuePair> nameValuePairs = new ArrayList <NameValuePair>();
                nameValuePairs.add( new BasicNameValuePair( "current_offer", Integer.toString( nextItemToBuy.getHagglingPrice() ) ) );
                nameValuePairs.add( new BasicNameValuePair( "x", Integer.toString( xPos ) ) );
                nameValuePairs.add( new BasicNameValuePair( "y", Integer.toString( yPos ) ) );
                httpPost.setEntity( new UrlEncodedFormEntity( nameValuePairs, Consts.UTF_8) );
                httpResponse = httpClient.execute( httpPost );
                httpEntity = httpResponse.getEntity();
                result = EntityUtils.toString( httpEntity );
                EntityUtils.consume( httpEntity );

                if ( result.indexOf( "You must select the correct pet in order to continue." ) != -1 ) {
                    log.w( "Captchabreaking didn't work." );
                } else if ( result.indexOf( "I accept your offer" ) != -1 | result.indexOf( "has been added to your inventory") != -1 ) {
                    log.i( "Offer accepted by shopkeeper. " + itemToBuy.getName() + " added to inventory." );
                    neopoints = neopoints - itemToBuy.getShopPrice();
                    status = ITEM_BOUGHT;
                } else if ( result.indexOf( "The Shopkeeper says 'I want at least" ) != -1 ) {
                    status = COUNTER_OFFER;
                    log.i ( "Offer declined by shopkeeper." );
                } else if ( result.indexOf( "The Shopkeeper says 'I wont take less than" ) != -1 ) {
                    log.i ( "Offer declined by shopkeeper." );
                    status = COUNTER_OFFER;
                } else if ( result.toLowerCase().indexOf( "sold out" ) != -1 ) {
                    log.i( "Too bad, " + itemToBuy.getName() + " is sold out!" );
                    status = SOLD_OUT;
                } else if ( result.indexOf( "The Shopkeeper says 'You don't have that kind of money." ) != -1 )  {
                    log.w( "The Shopkeeper says 'You don't have that kind of money' for buying " + itemToBuy.getName() );
                } else if ( result.indexOf( "Neopets is temporarily offline" ) != -1 ) {
                    log.w( "Buying not succesfull: Neopets is temporarily offline." );
                } else if ( result.indexOf( "Neopets - Hi!" ) != -1 ) {
                    log.w( "Error: logged out while buying." );
                    loggedOut = true;
                } else {
                    log.e( "Something unexpected did happen in buy method. Dumping result: " + result );
                }

            } else {
                log.w( "Something unexpected did happen while buying an item, result: " + result );
            }

        } catch( Exception e ) {
            log.e ( "Exception in buy method: " + e.getMessage() );
        }

        return httpClient;
    }

    public NeoItem getItemToBuy() {
        return nextItemToBuy;
    }

    public int getNeopoints() {
        return neopoints;
    }

    public boolean tooFast() {
        if ( status == TOO_FAST ) {
            return true;
        } else {
            return false;
        }
    }

    public boolean shopVisited() {
        if ( status >= SHOP_VISITED ) {
            return true;
        } else {
            return false;
        }
    }

    public boolean soldOut() {
        if ( status == SOLD_OUT ) {
            return true;
        } else {
            return false;
        }
    }

    public boolean itemBought() {
        if ( status == ITEM_BOUGHT ) {
            return true;
        } else {
            return false;
        }
    }    
    
    public boolean couterOffer() {
        if ( status == COUNTER_OFFER ) {
            return true;
        } else {
            return false;
        }        
    }

    public boolean isLoggedOut() {
        return loggedOut;
    }
    
}
