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

public class NeoMyShop {

    private int                     neopoints       = 0,
                                    shopSize        = 0;                                    
    private static int              ITEMS_PER_SIZE  = 5,
                                    OPENING_COSTS   = 150,
                                    UPGRADE_COSTS   = 200;

    private NeoTill                 neoTill;
    
    private NeoDatabaseManager      neoDatabaseManager;
    private List<NeoItem>           neoItems = new ArrayList<NeoItem>();
    private List<String>            shopPages = new ArrayList<String>();
    
    private NeoLogger               log;
    
    private NeoAccount              neoAccount;
    
    private boolean                 isLoggedOut;
    
    public NeoMyShop( NeoAccount neoAccount ) {
        this.neoAccount = neoAccount;
        neoDatabaseManager = new NeoDatabaseManager();
        neoTill = new NeoTill();        
        log = new NeoLogger();
    }

    public DefaultHttpClient updateSize( DefaultHttpClient httpClient ) { 

        // Updating neopoints and shopsize.
        log.i( "Updating my shop size." );

        try {
            // Get shoppage, items sorted by price
            HttpGet httpGet = new HttpGet( "http://www.neopets.com/market.phtml?type=your&order_by=price" );
            HttpResponse httpResponse = httpClient.execute( httpGet );
            HttpEntity httpEntity = httpResponse.getEntity();
            String result = EntityUtils.toString( httpResponse.getEntity() );
            EntityUtils.consume( httpEntity );

            String firstPage = result;
            
            if ( firstPage.indexOf( "Hi!</title>" ) != -1 ) {
                log.w( "Logged out while updating the size of the shop." );
                isLoggedOut = true;                
            } else if ( firstPage.indexOf( "<div id=\"footer\">" ) == -1 ) {
                log.w( "Didn't get the complete resultpage while updating the shop size." );
            } else {
                // process NEOPOINTS
            	firstPage = firstPage.substring( firstPage.indexOf( "NP: <a id='npanchor' href=\"/inventory.phtml\">" ) + 45 );
                String neopointsString = firstPage.substring( 0, firstPage.indexOf( "</a>") );
                neopointsString = neopointsString.replace( ',', ' ' );
                neopointsString = neopointsString.replaceAll( " ", "" );
                neopoints = Integer.parseInt( neopointsString );
                
                // process SHOP
                firstPage = firstPage.substring( firstPage.indexOf( "> (size ") + 8 );
                String maxItemsString = firstPage.substring( 0, firstPage.indexOf( ")") );
                shopSize = Integer.parseInt( maxItemsString );
            }
        } catch( Exception e ) {
            log.e ( "Exception in updateSize method in NeoMyShop: " + e.getMessage() );
        }
        
        return httpClient;
    }

    public DefaultHttpClient updateFull( DefaultHttpClient httpClient ) { 

        isLoggedOut = false;
        
        // Updating (& emptying till)
        httpClient = neoTill.update( httpClient );
        if ( !neoTill.isEmpty() ) {
            httpClient = neoTill.emptyTill( httpClient );
        }

        // Updating neopoints and shopsize.
        log.i( "Updating my shop." );

        // Update all items in the shop
        try {
            // Get shoppage, items sorted by price
            HttpGet httpGet = new HttpGet( "http://www.neopets.com/market.phtml?type=your&order_by=price" );
            HttpResponse httpResponse = httpClient.execute( httpGet );
            HttpEntity httpEntity = httpResponse.getEntity();
            String result = EntityUtils.toString( httpResponse.getEntity() );
            EntityUtils.consume( httpEntity );

            String firstPage = result;
            
            if ( firstPage.indexOf( "Hi!</title>" ) != -1 ) {
                log.w( "Logged out while fully updating the shop." );
                isLoggedOut = true;
            } else if ( firstPage.indexOf( "<div id=\"footer\">" ) == -1 ) {
                log.w( "Didn't get the complete resultpage while fully updating my shop." );
            } else {
                // process NEOPOINTS
            	firstPage = firstPage.substring( firstPage.indexOf( "NP: <a id='npanchor' href=\"/inventory.phtml\">" ) + 45 );
                String neopointsString = firstPage.substring( 0, firstPage.indexOf( "</a>") );
                neopointsString = neopointsString.replace( ',', ' ' );
                neopointsString = neopointsString.replaceAll( " ", "" );
                neopoints = Integer.parseInt( neopointsString );
                
                // process SHOP
                if( firstPage.indexOf( "> (size ") == -1 ) {
                    log.w( "Something went wrong. Dumping result: " + firstPage.substring( firstPage.length() - 100 ) );
                }
                
                firstPage = firstPage.substring( firstPage.indexOf( "> (size ") + 8 );
                String maxItemsString = firstPage.substring( 0, firstPage.indexOf( ")") );
                shopSize = Integer.parseInt( maxItemsString );
                
                // Empty shoplist
                neoItems.clear();
                
                if ( firstPage.indexOf( "Items Stocked : <b>" ) != - 1 ) { 
    
                    // Updating shopitems
                    log.i( "Updating all items in my shop." );

                    // Get all shoppages
                    shopPages.clear();

                    result = result.substring( 0, result.indexOf( "Add a PIN" ) );
                    while( result.indexOf( "<a href='market.phtml?order_by=price&type=your&lim=" ) != -1 ) {
                        result = result.substring( result.indexOf( "<a href='market.phtml?order_by=price&type=your&lim=" ) + 9 );
                        shopPages.add( "http://www.neopets.com/" + result.substring( 0, result.indexOf( "'>" ) ) );
                        result = result.substring( result.indexOf( ">") );
                    }

                    // When shop exists only of 1 page, add that page.
                    if ( shopPages.isEmpty() ) {
                        shopPages.add( "http://www.neopets.com/market.phtml?type=your&order_by=price" );
                    }

                    // Proces all shopPages
                    for( int x=0; x<shopPages.size(); x++ ) {
                        
                        log.i( "Updating shoppage " + (x+1) + "/" + shopPages.size() );
                        
                        // Get shoppage
                        httpGet = new HttpGet( shopPages.get( x ) );
                        httpResponse = httpClient.execute( httpGet );
                        httpEntity = httpResponse.getEntity();
                        result = EntityUtils.toString( httpResponse.getEntity() );
                        EntityUtils.consume( httpEntity );

                        // Process all items in this page
                        if ( result.indexOf( "Neopets-Hi!" ) != -1 ) {
                            log.w( "Logged out while fully updating the shop." );
                            isLoggedOut = true;
                            break;
                        } else {
                            while( result.indexOf( "<td width=60 bgcolor='#ffffcc'><b>" ) != -1 ) {
                                // Get item
                                result = result.substring( result.indexOf( "<td width=60 bgcolor='#ffffcc'><b>" ) + 34 );
                                String item = result.substring( 0, result.indexOf( "<") );

                                // Get quantity
                                result = result.substring( result.indexOf( "<b>" ) + 3 );
                                int quantity = Integer.parseInt( result.substring( 0, result.indexOf( "<" ) ) );

                                // Get sellprice
                                // oldcost_x
                                result = result.substring( result.indexOf( "oldcost" ) );
                                String oldcost_x_value = result.substring( result.indexOf( "value" ) + 7, result.indexOf( ">" ) - 1 );
                                int sellPrice = Integer.parseInt( oldcost_x_value );

                                // Add item to shoplist
                                NeoItem neopetsItem = new NeoItem( item );
                                neopetsItem.setQuantity( quantity );
                                neopetsItem.setSellPrice( sellPrice );
                                neoItems.add( neopetsItem );
                            }
                        }
                    }
                }
            }
        } catch( Exception e ) {
            log.e ( "Exception in updateFull method op NeoMyShop (BEGIN): " + e.getMessage() );
            e.printStackTrace();
            log.e ( "Exception in updateFull method op NeoMyShop (END): " + e.getMessage() );
        }
        
        return httpClient;
    }    
    
    public DefaultHttpClient setPrices( DefaultHttpClient httpClient ) {
        
        log.i ( "Updating prices in shop." );
        
        try {
            // Get shoppage, items sorted by price, so items with price=0 are displayed
            HttpGet httpGet = new HttpGet( "http://www.neopets.com/market.phtml?type=your&order_by=price" );
            HttpResponse httpResponse = httpClient.execute( httpGet );
            HttpEntity httpEntity = httpResponse.getEntity();
            String result = EntityUtils.toString( httpResponse.getEntity() );
            EntityUtils.consume( httpEntity );
            String temp = result;
            
            if ( temp.indexOf( ">Rm<" ) != -1 ) {
                // Process result
                HttpPost httpPost = new HttpPost( "http://www.neopets.com/process_market.phtml" );
                List<BasicHeader> defaultHeaders = new ArrayList<BasicHeader>();
                defaultHeaders.add(new BasicHeader( "Referer", "http://www.neopets.com/market.phtml?type=your" ) );
                httpPost.getParams().setParameter( ClientPNames.DEFAULT_HEADERS, defaultHeaders );

                List <NameValuePair> nameValuePairs = new ArrayList <NameValuePair>();
                nameValuePairs.add( new BasicNameValuePair( "type", "update_prices" ) );
                nameValuePairs.add( new BasicNameValuePair( "order by", "" ) );
                nameValuePairs.add( new BasicNameValuePair( "view", "" ) );

                temp = temp.substring( temp.indexOf( ">Rm<" ) + 8 );
                temp = temp.substring( temp.indexOf( ">Rm<" ) + 8 );

                do {
                    // item name
                    temp = temp.substring( temp.indexOf( "<b>" ) + 3 );
                    String item = temp.substring( 0, temp.indexOf( "<" ) );
                    int sellPrice;
                    if ( neoDatabaseManager.itemExists( new NeoItem( item ) ) ) {
                        sellPrice = neoDatabaseManager.getSellPrice( new NeoItem( item ), neoAccount );
                    } else {
                        log.i( "Unknown item in shop. Adding " + item + " to data." );
                        neoDatabaseManager.addItem( new NeoItem( item ) );
                        sellPrice = 0;
                    }

                    // quantity
                    temp = temp.substring( temp.indexOf( "<b>" ) + 3 );
                    int quantity = Integer.parseInt( temp.substring( 0, temp.indexOf( "<" ) ) );

                    // obj_id_x
                    temp = temp.substring( temp.indexOf( "obj_id" ) );
                    String obj_id_x_name = temp.substring( 0, temp.indexOf( "value" ) - 2 );
                    String obj_id_x_value = temp.substring( temp.indexOf( "value" ) + 7, temp.indexOf( ">" ) - 1 );
                    nameValuePairs.add( new BasicNameValuePair( obj_id_x_name, obj_id_x_value ) );

                    // oldcost_x
                    temp = temp.substring( temp.indexOf( "oldcost" ) );
                    String oldcost_x_name = temp.substring( 0, temp.indexOf( "value" ) - 3 );
                    String oldcost_x_value = temp.substring( temp.indexOf( "value" ) + 7, temp.indexOf( ">" ) - 1 );
                    nameValuePairs.add( new BasicNameValuePair( oldcost_x_name, oldcost_x_value ) );

                    // cost_x
                    temp = temp.substring( temp.indexOf( ">" ) + 1 );
                    temp = temp.substring( temp.indexOf( "cost" ) );
                    String cost_x_name = temp.substring( 0, temp.indexOf( "size" ) - 2 );
                    String cost_x_value = Integer.toString( sellPrice );

                    nameValuePairs.add( new BasicNameValuePair( cost_x_name, cost_x_value ) );

                    // back_to_inv[x]
                    temp = temp.substring( temp.indexOf( "back_to_inv" ) );
                    String back_to_inv_name = temp.substring( 0, temp.indexOf( ">") );
                    String back_to_inv_value = "0";
                    nameValuePairs.add( new BasicNameValuePair( back_to_inv_name, back_to_inv_value ) );

                } while ( temp.indexOf( "obj_id" ) != -1 );

                nameValuePairs.add( new BasicNameValuePair( "lim", "30" ) );
                nameValuePairs.add( new BasicNameValuePair( "obj_name", "" ) );

                httpPost.setEntity( new UrlEncodedFormEntity( nameValuePairs, Consts.UTF_8) );
                httpResponse = httpClient.execute( httpPost );

                // Process response
                HttpEntity entity = httpResponse.getEntity();
                EntityUtils.consume( entity );
            
            } else {
                log.w( "W: Didn't get the complete resultpage while setting the prices. "  );
            }
            
        } catch( Exception e ) {
            log.e ( "Exception while setting the prices in the shop: " + e.getMessage() );
        }

        return httpClient;
    }

    public DefaultHttpClient upgrade( DefaultHttpClient httpClient ) {

        // Post the upgrade form
        try {
            HttpPost httpPost = new HttpPost( "http://www.neopets.com/process_market.phtml" );
            List<BasicHeader> defaultHeaders = new ArrayList<BasicHeader>();
            defaultHeaders.add(new BasicHeader( "Referer", "http://www.neopets.com/market.phtml?type=edit" ) );
            httpPost.getParams().setParameter( ClientPNames.DEFAULT_HEADERS, defaultHeaders );
            List<NameValuePair> nameValuePairs = new ArrayList <NameValuePair>();
            nameValuePairs.add( new BasicNameValuePair( "type", "upgrade" ) );
            httpPost.setEntity( new UrlEncodedFormEntity( nameValuePairs, Consts.UTF_8 ) );
            HttpResponse httpResponse = httpClient.execute( httpPost );
            HttpEntity httpEntity = httpResponse.getEntity();
            EntityUtils.consume( httpEntity );
            neopoints = neopoints - getUpgradeCosts();
            shopSize++;
            log.i( "Shop upgraded." );

        } catch( Exception e ) {
            log.e ( "Error while upgrading shop: " + e.getMessage() );
        }

        return httpClient;
    }
    
    public int getExtraSpaceAfterUpgrade() {
        return ITEMS_PER_SIZE;
    }
    
    public int getUpgradeCosts() {
        return shopSize * UPGRADE_COSTS;
    }

    public int getSpaceLeft() {
        return (shopSize * ITEMS_PER_SIZE) - getNumberOfShopItems();
    }

    public int getMaxSpace() {
        return shopSize * ITEMS_PER_SIZE;
    }
    
    public int getSpaceStocked() {
        return getMaxSpace() - getSpaceLeft();
    }
    
    public int getNeopoints() {
        return neopoints;
    }

    public int getNumberOfShopItems() {
        int total = 0;
        
        for( NeoItem item : neoItems ) {
            total = total + item.getQuantity();
        }
        
        return total;
    }

    public int getShopItemsValue() {
        int shopItemsValue = 0;
        
        for( NeoItem item : neoItems ) {
            shopItemsValue = shopItemsValue + (item.getQuantity() * item.getSellPrice() );
        }
        
        return shopItemsValue;
    }

    public List<NeoItem> getItems() {
        return neoItems;
    }

    public boolean itemsToBePriced() {
        if ( neoItems.isEmpty() ) {
            return false;
        } else if ( neoItems.get( 0 ).getSellPrice() == 0 ) {
            return true;
        } else {
            return false;
        }
    }

    public int getShopSpaceValue() {
        
        int value = OPENING_COSTS;
        
        for( int i = 1; i < shopSize; i++ ) {
          value = value + ( shopSize * UPGRADE_COSTS );
        }
        
        return value;
        
    }

    public boolean hasSpaceLeft() {
        if ( getSpaceLeft() > 0 ) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isFull() {
        return !hasSpaceLeft();
    }
    
    public int getnumberOfShopPages() {
        return shopPages.size();
    }

    public void setNeopoints( int neopoints ) {
        this.neopoints = neopoints;
    }

    public void printItems() {
        for( NeoItem item : neoItems ) {
            int q = item.getQuantity();
            if ( q > 20 ) {
                log.i( item.getName() + " : " + item.getQuantity() );
            }
        }        
    }

    public boolean isLoggedOut() {
        return isLoggedOut;
    }

}
