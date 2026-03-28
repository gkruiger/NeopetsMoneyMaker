package neopetsmoneymaker;

import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class NeoCaptchaBreaker {

    private int              	xPos,
                                yPos;
    private String              captchaURL;

    private NeoLogger           log;
    
    public NeoCaptchaBreaker() {
        log = new NeoLogger();
    }

    public DefaultHttpClient breakIt( String hagglePage, DefaultHttpClient httpClient ) {
    	// Extract captcha image url from haggle-page
        String temp = hagglePage;
        xPos = 0;
        yPos = 0;
                
        if (  temp.indexOf( "src=\"/captcha_show.phtml" ) != -1 ) {
            
            temp = temp.substring( temp.indexOf( "src=\"/captcha_show.phtml" ) + 5 );
            temp = temp.substring( 0, temp.indexOf( "style" ) - 2 );
            captchaURL = "http://www.neopets.com" + temp;
            
            BufferedImage captchaImage = getImage( captchaURL, httpClient );
            
            getDarkestPixel( captchaImage );
            
        } else if ( temp.indexOf( "<div id=\"footer\">" ) == -1 ) {
            log.w( "Didn't get the complete resultpage in BeopetsCaptchaBreaker() method. This captcha won't be broken." );
        } else {
            log.e( "Something unexpected occured in NeopetsCaptchaBreaker() method: could not parse link to captcha imgage." );
        }       

        return httpClient;
    }

    private BufferedImage getImage( String imageURL, DefaultHttpClient httpClient ) {
        
        BufferedImage bi = null;
        
        try {
            HttpGet httpGet = new HttpGet( imageURL );
            HttpResponse httpResponse = httpClient.execute( httpGet );
            HttpEntity httpEntity = httpResponse.getEntity();
            bi = ImageIO.read( httpEntity.getContent() );
            EntityUtils.consume( httpEntity );
            
        } catch( Exception e ) {
            log.e( "Exception in getImage method: " + e.getMessage() );
        }
        
        return bi;
    }
    
    private void getDarkestPixel( BufferedImage captchaImage ) {
    	int[] pixels = new int[ captchaImage.getWidth() * captchaImage.getHeight() ];
    	PixelGrabber pg = new PixelGrabber( captchaImage, 0, 0, captchaImage.getWidth(), captchaImage.getHeight(), pixels, 0, captchaImage.getWidth() );
        
        try {
            pg.grabPixels();
        } catch ( InterruptedException e ) {
            log.w( "Exception in grabbing pixels from captcha image: " + e.getMessage() );
        }
        
        int lowestValue = 255 * 4;
    	
        for ( int q=0; q<pixels.length; q++ ) {

            int alpha = (pixels[ q ] >> 24) & 0xff;
            int red   = (pixels[ q ] >> 16) & 0xff;
            int green = (pixels[ q ] >>  8) & 0xff;
            int blue  = (pixels[ q ]      ) & 0xff;

            int total = alpha + red + green + blue;
            
            if ( total < lowestValue ) {
                xPos = q % captchaImage.getWidth();
                yPos = q / captchaImage.getWidth();
                lowestValue = total;
            }
        }		
    }

    public int getX() {
        return xPos;
    }

    public int getY() {
        return yPos;
    }

}
