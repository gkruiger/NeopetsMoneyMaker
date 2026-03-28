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

public class NeoMarket {

    private NeoItem             researchResultItem;
    private boolean             isMarketBanned,
                                isLoggedOut;
    
    private NeoLogger           log;

    public NeoMarket() {
    	log = new NeoLogger();
    }

    public DefaultHttpClient researchMarket( NeoItem neopetsItem, List<NeoAccount> accounts, DefaultHttpClient httpClient ) {

        isMarketBanned  = false;
        isLoggedOut     = false;

        try {

            log.i( "Exploring market for: " + neopetsItem.getName() );

            // Open Shop Wizard. If send for a quest, say that you dont want it
            HttpGet httpGet = new HttpGet( "http://www.neopets.com/market.phtml?type=wizard" );
            HttpResponse httpResponse = httpClient.execute( httpGet );
            HttpEntity httpEntity = httpResponse.getEntity();
            String result = EntityUtils.toString( httpEntity );
            EntityUtils.consume( httpEntity );

            if ( result.indexOf( "Faerie Quest" ) != -1 ) {
                log.i( "Argh! A quest. Getting rid of it. " );

                // Visit faery quest page
                httpGet = new HttpGet( "http://www.neopets.com/market.phtml?type=wizard" );
                httpResponse = httpClient.execute( httpGet );
                httpEntity = httpResponse.getEntity();
                EntityUtils.consume( httpEntity );

                // Press button
                HttpPost httpPost = new HttpPost( "http://www.neopets.com/process_quests.phtml") ;
                List<BasicHeader> defaultHeaders = new ArrayList<BasicHeader>();
                defaultHeaders.add(new BasicHeader("Referer", "http://www.neopets.com/quests.phtml") );
                httpPost.getParams().setParameter(ClientPNames.DEFAULT_HEADERS, defaultHeaders );
                List <NameValuePair> nameValuePairs = new ArrayList <NameValuePair>();
                nameValuePairs.add( new BasicNameValuePair( "subbyno", "I dont want to do this quest" ));
                httpPost.setEntity( new UrlEncodedFormEntity( nameValuePairs, Consts.UTF_8 ) );
                httpResponse = httpClient.execute( httpPost );
                httpEntity = httpResponse.getEntity();
                EntityUtils.consume( httpEntity );
            }

            // Send request to Shop Wizard
            HttpPost httpPost = new HttpPost( "http://www.neopets.com/market.phtml" );
            List<BasicHeader> defaultHeaders = new ArrayList<BasicHeader>();
            defaultHeaders.add(new BasicHeader( "Referer", "http://www.neopets.com/market.phtml?type=wizard" ) );
            httpPost.getParams().setParameter( ClientPNames.DEFAULT_HEADERS, defaultHeaders );
            List <NameValuePair> nameValuePairs = new ArrayList <NameValuePair>();
            nameValuePairs.add( new BasicNameValuePair( "type", "process_wizard" ) );
            nameValuePairs.add( new BasicNameValuePair( "feedset", "0" ) );
            nameValuePairs.add( new BasicNameValuePair( "shopwizard", neopetsItem.getName() ) );
            nameValuePairs.add( new BasicNameValuePair( "table", "shop" ) );
            nameValuePairs.add( new BasicNameValuePair( "criteria", "exact" ) );
            nameValuePairs.add( new BasicNameValuePair( "min_price", "0" ) );
            nameValuePairs.add( new BasicNameValuePair( "max_price", "99999" ) );
            httpPost.setEntity( new UrlEncodedFormEntity( nameValuePairs, Consts.UTF_8) );
            httpResponse = httpClient.execute( httpPost );

            // Process response
            httpEntity = httpResponse.getEntity();
            result = EntityUtils.toString( httpEntity );
            if ( result.indexOf( "I did not find anything" ) != -1 ) {
                // Nothing found
                log.i ( "Did not find anything for: " + neopetsItem.getName() );
                researchResultItem = new NeoItem( neopetsItem.getName() );
            } else if ( result.indexOf( "Whoa there, too many searches!" ) != -1 ) {
                // Too many search requests
                log.w( "Warning: shop wizard reported TOO MANY SEARCHES.");
                isMarketBanned = true;
            } else if ( result.indexOf( "<a href=\"/browseshop.phtml?owner=" ) != -1 ) {
                // Get first result.
                
                boolean itsMe;
                do { 
                    itsMe = false;
                    
                    result = result.substring( result.indexOf( "<a href=\"/browseshop.phtml?owner=" ) );
                    result = result.substring( result.indexOf( "<b>" ) + 3 );
                    String owner = result.substring( 0, result.indexOf( "</b>" ) );

                    result = result.substring( result.indexOf( "<td align=\"center\" bgcolor=\"#" ) + 37 );
                    String quantity = result.substring( 0, result.indexOf( "<" ) );

                    result = result.substring( result.indexOf( "<b>" ) + 3 );
                    String price = result.substring( 0, result.indexOf( " NP" ) ).replace( ',', ' ' ).replaceAll( " ", "" );

                    researchResultItem = new NeoItem(
                        neopetsItem.getName(),
                        owner,
                        Integer.parseInt( quantity ),
                        Integer.parseInt( price )
                    );
                    
                    for( NeoAccount account : accounts ) {
                        
                        if ( researchResultItem.getOwner().equals( account.getUsername() ) ) {
                            itsMe = true;
                            break;
                        }
                    }
                    
                } while ( ( result.indexOf( "<a href=\"/browseshop.phtml?owner=" ) != - 1 & itsMe ) );

            } else if ( result.indexOf( result.indexOf( "Login to Neopets!" ) ) == -1 ) {
                log.w( "Warning: logged out while researching the market." );
                isLoggedOut = true;
            } else {
                log.e( "Something unexpected occured in searchmarket() method. Printing result: " + result );
            }
            EntityUtils.consume( httpEntity );
        } catch( Exception e ) {
            log.e( "Exception in searchMarket method: " + e.getMessage() );
        }

        return httpClient;
    }

    public boolean isMarketBanned() {
        return isMarketBanned;
    }

    public boolean isLoggedOut() {
        return isLoggedOut;
    }

    public NeoItem getResult() {
        return researchResultItem;
    }


}
