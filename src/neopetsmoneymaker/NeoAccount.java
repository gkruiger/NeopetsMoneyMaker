package neopetsmoneymaker;

import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class NeoAccount {

    private String              username,
                                password;
    private Date                dateOfBirth;
    private NeoLogger           log;
    private boolean             isLoggedIn;
    
    private final int           SLEEP_SECONDS_WHILE_NEOPETS_IS_TEMPORARILY_OFFLINE = 60,
                                SLEEP_SECONDS_BECAUSE_OF_TOO_MANY_LOGIN_TRIES = 60,
                                SLEEP_SECONDS_BECAUSE_OF_BAD_PASSWORD = 3600;

    public NeoAccount( String username, String password, Date dateOfBirth ) {
        this.username = username;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
    
        log = new NeoLogger();
    }
    
    public NeoAccount( String username ) {
        this.username = username;

        log = new NeoLogger();
    }

    public DefaultHttpClient login() {
        return login( null );
    }

    public DefaultHttpClient login( DefaultHttpClient httpClient ) {
        isLoggedIn = false;
        
        if ( httpClient == null ) {
            log.i( "Logging in '" + username + "'" );
            httpClient = new DefaultHttpClient();
            httpClient.getParams().setParameter( ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY );
            httpClient.setRedirectStrategy( new LaxRedirectStrategy() );
        } else {
            log.i( "Logging in (again): '" + username + "'" );
        }

        do { 
            try {
                // Get loginpage. Set cookies right.
                HttpGet httpGet = new HttpGet( "http://www.neopets.com/loginpage.phtml" );
                HttpResponse httpResponse = httpClient.execute( httpGet );
                HttpEntity httpEntity = httpResponse.getEntity();
                String result = EntityUtils.toString( httpEntity );
                EntityUtils.consume( httpEntity );

                // Checking if user is already logged in
                if ( result.indexOf( "<b>Logout</b>" ) != -1 ) {
                    log.i( "Already logged in with '" + username + "'" );
                    isLoggedIn = true;
                } else if ( result.indexOf( "Neopets is temporarily offline." ) != - 1 ) {
                    log.w( "Neopets is temporarily offline while logging in. Trying again in one minute." );
                    try {
                        Thread.sleep( SLEEP_SECONDS_WHILE_NEOPETS_IS_TEMPORARILY_OFFLINE * 1000 );
                    } catch ( InterruptedException ex ) {
                        log.e( "Error while sleeping: " + ex.getMessage() );
                    }
                } else {
                    // Send username & password & date of birth
                    HttpPost httpPost = new HttpPost( "http://www.neopets.com/login.phtml") ;
                    ArrayList<NameValuePair> nameValuePairs = new ArrayList <NameValuePair>();
                    nameValuePairs.add( new BasicNameValuePair( "username", username ) );
                    nameValuePairs.add( new BasicNameValuePair( "password", password ) );

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime( dateOfBirth );

                    String month = Integer.toString( calendar.get( Calendar.MONTH ) + 1 );
                    if ( month.length() == 1 ) {
                        month = "0".concat( month );
                    }
                    String day = Integer.toString( calendar.get( Calendar.DAY_OF_MONTH ) );
                    if ( day.length() == 1 ) {
                        day = "0".concat( day );
                    }
                    String year = Integer.toString( calendar.get( Calendar.YEAR ) );

                    // Calendar.MONTH starts at 0 for january!
                    nameValuePairs.add( new BasicNameValuePair( "dob_m", month ) );
                    nameValuePairs.add( new BasicNameValuePair( "dob_d", day ) );
                    nameValuePairs.add( new BasicNameValuePair( "dob_y", year ) );

                    httpPost.setEntity( new UrlEncodedFormEntity( nameValuePairs, Consts.UTF_8 ) );
                    httpResponse = httpClient.execute( httpPost );
                    httpEntity = httpResponse.getEntity();                
                    result = EntityUtils.toString( httpEntity );
                    EntityUtils.consume( httpEntity );

                    // Checking if user is indeed logged in
                    if ( result.indexOf( "Logout" ) != -1 ) {
                        isLoggedIn = true;
                        log.i( "Logged in with '" + username + "'" ); 
                    } else if ( result.indexOf( "Incorrect birthdate entered!") != -1 ) {
                        log.e( "Incorrent birthdate for '" + username + "'" ); 
                    } else if ( result.indexOf( "Sorry, we did not find an account with that username." ) != - 1 ) {
                        log.e( "'" + username + "' not known as username" ); 
                    } else if ( result.indexOf( "Neopets - Bad Password" ) != - 1 ) {
                        log.e( "Bad Password while logging in. Really? Trying again in one minute." );
                        try {
                            Thread.sleep( SLEEP_SECONDS_BECAUSE_OF_BAD_PASSWORD * 1000 );
                        } catch ( InterruptedException ex ) {
                            log.e( "Error while sleeping: " + ex.getMessage() );
                        }
                    } else if ( result.indexOf( "you have tried too many times to guess this password" ) != - 1 ) {
                        log.e( "Tried too many times to log in. Trying again in one hour." );
                        try {
                            Thread.sleep( SLEEP_SECONDS_BECAUSE_OF_TOO_MANY_LOGIN_TRIES * 1000 );
                        } catch ( InterruptedException ex ) {
                            log.e( "Error while sleeping: " + ex.getMessage() );
                        }
                    } else {
                        log.e( "Login with '" + username + "' failed. Unknown error. Printing result: " + result );
                    }
                }
            } catch( Exception e ) {
                log.e( "Exception in login method: " + e.getMessage() );
            }
        } while ( !isLoggedIn );

        return httpClient;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

}
