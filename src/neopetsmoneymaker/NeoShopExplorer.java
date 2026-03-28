package neopetsmoneymaker;

import java.util.List;
import org.apache.http.impl.client.DefaultHttpClient;

public class NeoShopExplorer extends Exception implements Runnable {

    private List<NeoShop>           shops;
    private NeoAccount              account;
    private int                     firstShop;
    private NeoDatabaseManager      neopetsDatabaseManager;
    private DefaultHttpClient       httpClient;

    private NeoLogger	log;

    public NeoShopExplorer( List<NeoShop> shops, NeoAccount account, int firstShop, NeoDatabaseManager neopetsDatabaseManager ) {

        this.shops = shops;
        this.account = account;
        this.firstShop = firstShop;
        this.neopetsDatabaseManager = neopetsDatabaseManager;
        
        httpClient = account.login();
        
        log = new NeoLogger();

        GoExploring();
    }
    
    private void GoExploring() {        
        new Thread( this ).start();
    }    

    @Override
    public void run() {
        // For every shop, visit the shop and return the items found to the database
        int x = firstShop;
        int sleepTime = 0;

        while( true ) {

            httpClient = shops.get( x ).visitShop( httpClient );
            
            if ( shops.get( x ).isLoggedOut() ) {
                httpClient = account.login();
            } else {
            
                neopetsDatabaseManager.addItemsFromShop( shops.get( x ).getItems() );

                if ( shops.get( x ).isShopBanned() ) {
                    neopetsDatabaseManager.setShopWait(
                        neopetsDatabaseManager.getShopWait() +
                        neopetsDatabaseManager.getShopWaitIncrease()
                    );

                    sleepTime = neopetsDatabaseManager.getShopbanWait();
                }

                while ( shops.get( x ).isShopBanned() ) {

                    log.w( "Shopban for: " + account.getUsername() + ". Sleep " + sleepTime/(60 * 60) + " hours before trying again." );

                    try {
                        Thread.sleep( sleepTime * 1000 );
                    } catch( InterruptedException ex ) {
                        log.e( "Thread interrupted while waiting during shopban for shopexplorer: " + ex.getMessage() );
                    }

                    httpClient = shops.get( x ).visitShop( httpClient );
                    neopetsDatabaseManager.addItemsFromShop( shops.get( x ).getItems() );
                }

                try {
                    Thread.sleep( neopetsDatabaseManager.getShopWait() * 1000 );
                } catch( InterruptedException ex ) {
                    log.e( "Thread interrupted while waiting for the next shop visit by shopexplorer: " + ex.getMessage() );
                }

                x++;
                if ( x >= shops.size() ) {
                    x = 0;
                }
            }
        }
    }
}
