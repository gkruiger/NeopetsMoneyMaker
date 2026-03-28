package neopetsmoneymaker;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class NeoShop {

    private String              name,
                                link;

    private boolean             isLoggedOut,
                                checkForShopBan,
                                shopbanned;

    private List<NeoItem>       neopetsItems;

    private NeoLogger           log;

    public NeoShop( String name, String link, boolean checkForShopBan ) {
        this.name = name;
        this.link = link;
        this.checkForShopBan = checkForShopBan;
        
        log = new NeoLogger();
    }

    public DefaultHttpClient visitShop( DefaultHttpClient httpClient ) {

        log.i( "Visiting '" + name + "'" );

        neopetsItems = new ArrayList<NeoItem>();
        isLoggedOut = false;
        shopbanned = false;

        try {
            // Open shop page
            HttpGet httpGet = new HttpGet( link );
            HttpResponse httpResponse = httpClient.execute( httpGet );
            HttpEntity httpEntity = httpResponse.getEntity();
            String result = EntityUtils.toString( httpEntity );
            EntityUtils.consume( httpEntity );
            
            if ( result.indexOf( "Log In!" ) != -1 ) {
                log.w( "Logged out while exploring a shop." );
                isLoggedOut = true;
            } else if ( result.indexOf( "Sorry, we are sold out of everything!" ) != -1 ) {
                log.i( "Nothing in the shop" );
                
                if( checkForShopBan ) {
                    shopbanned = true;
                }
            } else if ( result.indexOf( "onClick=\"this.href='" ) != -1 ) {
                while( result.indexOf( "onClick=\"this.href='" ) != -1 ) {
                    // Get hagglelink
                    result = result.substring( result.indexOf( "onClick=\"this.href='" ) + 20 );
                    String haggleLink = "http://www.neopets.com/" + result.substring( 0, result.indexOf( "';if" ) );
                    haggleLink = haggleLink.substring( 0, haggleLink.indexOf( "'+'" ) ) + haggleLink.substring(  haggleLink.indexOf( "'+'" ) + 3 );
                    // Get item
                    result = result.substring( result.indexOf( "<BR><B>" ) + 7 );
                    String item = result.substring( 0, result.indexOf( "</B><BR>" ) );
                    // Get quantity
                    result = result.substring( result.indexOf( "</B><BR>" ) + 8 );
                    int quantity = Integer.parseInt( result.substring( 0, result.indexOf( " in stock" ) ) );
                    // Get price
                    result = result.substring( result.indexOf( "Cost: "  ) + 6 );
                    int price = Integer.parseInt( result.substring( 0, result.indexOf( " NP" ) ).replace( ',', ' ' ).replaceAll( " ", "" ) );
                    // Process item

                    neopetsItems.add( new NeoItem( item, price, quantity, name, haggleLink, link ) );
                }

            } else if ( result.indexOf( "Neopets is temporarily offline") != -1 ) {
                log.w( "Shopvisit not succesfull: Neopets is temporarily offline.");
            } else {
                log.e( "Something unexpected occured in visitShop() method. Printing result: " + result );
            }
        } catch( Exception e ) {
            log.e( "Exception in opening shop: " + e.getMessage() );
        }     

        return httpClient;
    }

    public List<NeoItem> getItems() {
        return neopetsItems;
    }

    public Boolean isShopBanned() {
        return shopbanned;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public boolean getCheckForShopban() {
        return checkForShopBan;
    }

    public boolean isLoggedOut() {
        return isLoggedOut;
    }

}
