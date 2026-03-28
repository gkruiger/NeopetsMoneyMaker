package neopetsmoneymaker;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.impl.client.DefaultHttpClient;

public class NeoMarketExplorer implements Runnable {

    private NeoLogger               log;
    private NeoAccount              sideAccount;
    private NeoDatabaseManager      neoDatabaseManager;
    private NeoMarket               neopetsMarket;
    private DefaultHttpClient       httpClient;
    
    private List<NeoAccount>        accounts;

    public NeoMarketExplorer( NeoAccount account ) {
        log = new NeoLogger();

        sideAccount = account;
        
        accounts = new ArrayList<NeoAccount>();
        neoDatabaseManager = new NeoDatabaseManager();

        accounts.addAll( neoDatabaseManager.getAccounts() );
        
        neopetsMarket = new NeoMarket();

        httpClient = account.login();     
        GoExploring();
    }
    
    private void GoExploring() {        
        new Thread( this ).start();
    }

    @Override
    public void run() {
        int sleepTime = neoDatabaseManager.getMarketWait();

        while( true ) {

            NeoItem item = neoDatabaseManager.getItemForMarketSearch();
            
            if ( item == null ) {
            	log.w( "No items in database (yet) available for market exploring. Sleeping " + sleepTime + " seconds." );
                try {
                    Thread.sleep( sleepTime * 1000 );
                } catch( InterruptedException ex ) {
                    log.e( "Thread interrupted while waiting during shopban for shopexplorer: " + ex.getMessage() );
                }
            } else {
                httpClient = neopetsMarket.researchMarket( item, accounts, httpClient );

                if ( neopetsMarket.isLoggedOut() ) {
                    httpClient = sideAccount.login();
                } else {
                    if ( neopetsMarket.isMarketBanned() ) {
                        neoDatabaseManager.setMarketWait(
                            neoDatabaseManager.getMarketWait() +
                            neoDatabaseManager.getMarketWaitIncrease()
                        );

                        sleepTime = neoDatabaseManager.getMarketbanWait();

                        log.w( "Marketban for: " + sideAccount.getUsername() + ". Sleep " + sleepTime/(60 * 60) + " hours before trying again." );
                    } else {
                        neoDatabaseManager.addMarketResult(
                            neopetsMarket.getResult()
                        );

                        sleepTime = neoDatabaseManager.getMarketWait();
                    }

                    try {
                        Thread.sleep( sleepTime * 1000 );
                    } catch( InterruptedException ex ) {
                        log.e( "Thread interrupted while waiting during shopban for shopexplorer: " + ex.getMessage() );
                    }
                }
            }
            
            neoDatabaseManager.addHistory( new NeoHistory( "items_known", neoDatabaseManager.getKnownItems() )  );        
            neoDatabaseManager.addHistory( new NeoHistory( "items_marketresearched", neoDatabaseManager.getMarketResearchedItems() )  );        
            neoDatabaseManager.addHistory( new NeoHistory( "items_tradeable", neoDatabaseManager.getTradeableItems() )  );        
        
        }        
    }
}
