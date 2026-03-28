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

public class NeoInventory {

    private static int          MAX_ITEMS_IN_INVENTORY = 50;
    private int                 maxItems;

    private List<NeoItem>       neoItems = new ArrayList<NeoItem>();
    
    private NeoDatabaseManager  neoDatabaseManager;
    private NeoLogger           log;
    
    private boolean             isLoggedOut;

    public NeoInventory() {
        neoDatabaseManager      = new NeoDatabaseManager();
        maxItems = MAX_ITEMS_IN_INVENTORY;
        log = new NeoLogger();
    }

    public DefaultHttpClient update( DefaultHttpClient httpClient ) {

        isLoggedOut = false;
        log.i( "Updating inventory." );

        try {
            // Get inventory page
            HttpGet httpGet = new HttpGet( "http://www.neopets.com/inventory.phtml" );
            HttpResponse httpResponse = httpClient.execute( httpGet );
            HttpEntity httpEntity = httpResponse.getEntity();
            String result = EntityUtils.toString( httpResponse.getEntity() );
            EntityUtils.consume( httpEntity );

            neoItems.clear();
            
            // Process page
            if ( result.indexOf( "Neopets - Hi!" ) != -1 ) {
                log.w( "Logged out while updating inventory." );
                isLoggedOut = true;
            } else if ( result.indexOf( "You aren't carrying anything!" ) != -1 ) {
                //
            } else if (  result.indexOf( "Total Items: <b>" ) != -1 ) {

                while ( result.indexOf( "class=\"neopointItem\"></a><br>" ) != -1 ) {
                    result = result.substring( result.indexOf( "class=\"neopointItem\"></a><br>" ) + 29 );
                    String itemName = result.substring( 0, result.indexOf( "<") );
                
                    addItem( new NeoItem( itemName ) );
                }                
                
            } else if ( result.indexOf( "<div id=\"footer\">" ) == -1 ) {
                log.w( "Didn't get the complete resultpage in update() method. Inventory NOT updated." );
            } else {
                log.e( "Something unexpected happend in the update method. Inventory has not been updated. Result: " + result );
            }
        } catch( Exception e ) {
            log.e( "Exception in update method of NeopetsInventory: " + e.getMessage() );
        }
        
        return httpClient;
    }
    
    public DefaultHttpClient stockShop( DefaultHttpClient httpClient, int spaceLeftInShop ) {
        log.i( "Stocking shop." );

        try {
            // Get quickstock page
            HttpGet httpGet = new HttpGet( "http://www.neopets.com/quickstock.phtml" );
            HttpResponse httpResponse = httpClient.execute( httpGet );
            HttpEntity httpEntity = httpResponse.getEntity();
            String result = EntityUtils.toString( httpEntity );
            EntityUtils.consume( httpEntity );

            // Prepare stock post request
            HttpPost httpPost = new HttpPost( "http://www.neopets.com/process_quickstock.phtml" );
            List<BasicHeader> defaultHeaders = new ArrayList<BasicHeader>();
            defaultHeaders.add(new BasicHeader( "Referer", "http://www.neopets.com/quickstock.phtml" ) );
            httpPost.getParams().setParameter( ClientPNames.DEFAULT_HEADERS, defaultHeaders );

            List <NameValuePair> nameValuePairs = new ArrayList <NameValuePair>();
            nameValuePairs.add( new BasicNameValuePair( "buyitem", "0" ) );

            int itemsToBeStocked;
            if ( getNumberOfItems() >= spaceLeftInShop ) {
                itemsToBeStocked = spaceLeftInShop;
            } else {
                itemsToBeStocked = getNumberOfItems();
            }            
            
            String temp = result;
            if ( temp.indexOf( "<div id=\"footer\">" ) == -1 ) {
                log.w( "Didn't get the complete resultpage in stockShop method. Shop won't ben stocked." );
            } else if ( temp.indexOf( "asdjkasldajsdklasdasdasd" ) != -1 ) {
                // Todo: donate non-stockable items
            } else {
                for( int item = 1; item <= itemsToBeStocked; item++ ) {
                    if ( temp.indexOf( "id_arr" ) == -1 | temp.indexOf( "value") == -1 ) {
                        log.e( "Something unexpected occured in stockShop method. Item is skipped." );
                    } else {
                        temp = temp.substring( temp.indexOf( "id_arr") + 6 );
                        temp = temp.substring( temp.indexOf( "value") + 7 );
                        String id = temp.substring( 0, temp.indexOf( ">") - 1 );
                        temp = temp.substring( temp.indexOf( "<TD align='center'>" ) + 19 );
                        String naMaybe = temp.substring( 0, 3 );
                        if ( naMaybe.equals( "N/A" ) ) {
                            log.i( "Non-stockable item in inventory, it will be discarded." );
                            nameValuePairs.add( new BasicNameValuePair( "radio_arr[" + item + "]", "discard" ) );
                        } else {
                            nameValuePairs.add( new BasicNameValuePair( "radio_arr[" + item + "]", "stock" ) );
                        }
                        nameValuePairs.add( new BasicNameValuePair( "id_arr[" + item + "]", id ) );
                    }
                }
                /*
                 *  Tijdelijk uit, i.v.m non-stockable items. Kijken of het effect heeft.
                if ( getNumberOfItems() >= spaceLeftInShop ) {
                    nameValuePairs.add( new BasicNameValuePair( "checkall", "on" ) );
                }
                 */
                
                httpPost.setEntity( new UrlEncodedFormEntity( nameValuePairs, Consts.UTF_8) );
                httpResponse = httpClient.execute( httpPost );

                // Process response
                HttpEntity entity = httpResponse.getEntity();
                EntityUtils.consume( entity );

                log.i( itemsToBeStocked + " items stocked." );

                neoItems.clear();                
            }
        } catch( Exception e ) {
            log.e( "Exception in stockShop method: " + e.getMessage() );
        }
        return httpClient;
    }

    public List<NeoItem> getItems() {
        return neoItems;
    }   
   
    public int getMaxItems() {
        return maxItems;
    }

    public int getNumberOfItems() {
        int total = 0;
        
        for( NeoItem item : neoItems ) {
            total = total + item.getQuantity();
        }
        
        return total;
    }

    public int getSpaceLeft() {
        return maxItems - getNumberOfItems();
    }
   
    public boolean isEmpty() {
        if ( getNumberOfItems() > 0 ) {
            return false;
        } else {
            return true;
        }
    }

    public boolean hasItems() {
        return !isEmpty();
    }
    
    public boolean hasSpaceLeft() {
        return !isFull();
    }
    
    public boolean isFull() {
        if ( getNumberOfItems() == maxItems ) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isLoggedOut() {
        return isLoggedOut;
    }

    public void addItem( NeoItem item ) {
    
        boolean updated = false;
        
        for( int x=0; x<neoItems.size(); x++ ) {
            
           if ( neoItems.get( x ).getName().equals( item.getName() ) ) {
                
                neoItems.get( x ).setQuantity( neoItems.get( x ).getQuantity() + 1 );
                updated = true;
                break;
            }            
        }
        
        if ( !updated ) {
            item.setQuantity( 1 );
            neoItems.add( item );
        }
    }

}
