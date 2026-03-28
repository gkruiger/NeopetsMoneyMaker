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

public class NeoTill {

    private int         neopoints;
    private NeoLogger	log;

    public NeoTill() {
        neopoints = 0;

        log = new NeoLogger();
    }

    public DefaultHttpClient update( DefaultHttpClient httpClient ) {
            
        log.i( "Updating till." );

        try {
            // Visit till
            HttpGet httpGet = new HttpGet( "http://www.neopets.com/market.phtml?type=till" );
            HttpResponse httpResponse = httpClient.execute( httpGet );
            HttpEntity httpEntity = httpResponse.getEntity();
            String result = EntityUtils.toString( httpEntity );

            // Get neopoints
            if ( result.indexOf( "You currently have <b>" ) != -1 ) {
                result = result.substring( result.indexOf( "You currently have <b>" ) + 22 );
                if ( result.indexOf( "NP" ) != -1 ) {
                    result = result.substring( 0, result.indexOf( "NP" ) - 1 );
                } else {
                    result = "0";
                }
            } else {
                result = "0";
            }

            EntityUtils.consume( httpEntity );
            result = result.replace( ',', ' ' );
            result = result.replaceAll( " ", "" );

            neopoints = Integer.parseInt( result );
        } catch( Exception e ) {
            log.e( "Exception in update method of NeopetsTill: " + e.getMessage() );
        }
        return httpClient;
    }

    public DefaultHttpClient emptyTill( DefaultHttpClient httpClient ) {
        log.i( "Emptying till (NP: " + neopoints + ")" );

        try {
            HttpPost httpPost = new HttpPost( "http://www.neopets.com/process_market.phtml" );
            List<BasicHeader> defaultHeaders = new ArrayList<BasicHeader>();
            defaultHeaders.add(new BasicHeader( "Referer", "http://www.neopets.com/market.phtml?type=till" ) );
            httpPost.getParams().setParameter( ClientPNames.DEFAULT_HEADERS, defaultHeaders );
            List <NameValuePair> nameValuePairs = new ArrayList <NameValuePair>();
            nameValuePairs.add( new BasicNameValuePair( "type", "withdraw" ) );
            nameValuePairs.add( new BasicNameValuePair( "amount", Integer.toString( neopoints ) ) );
            httpPost.setEntity( new UrlEncodedFormEntity( nameValuePairs, Consts.UTF_8));
            HttpResponse httpResponse = httpClient.execute( httpPost );
            HttpEntity httpEntity = httpResponse.getEntity();
            EntityUtils.consume( httpEntity );
            neopoints = 0;
        } catch( Exception e ) {
            log.e( "Exception in update method of NeopetsTill: " + e.getMessage() );
        }
        return httpClient;
    }

    public int getNeopoints() {
        return neopoints;
    }
    
    public boolean isEmpty() {
        if ( neopoints == 0 ) {
            return true;
        } else {
            return false;
        }
    }
}