package neopetsmoneymaker;

import org.apache.http.impl.client.DefaultHttpClient;

public class NeoMainAccountService implements Runnable {
    
    private NeoLogger           log;
    private NeoDatabaseManager  neoDatabaseManager;
    private NeoAccount          neoAccount;
    private NeoInventory        neoInventory;
    private NeoMyShop           neoMyShop;
    private NeoBuyer            neoBuyer;

    private DefaultHttpClient   httpClient;
    private int                 activity_seconds,
                                items_traded_today;
    private long                timestamp;
    private int                 MAXIMUM_DURATION_OF_MAINACCOUNT_CYCLE = 60;
    
    public NeoMainAccountService( NeoAccount neoAccount ) {        
        this.neoAccount         = neoAccount;
        
        log = new NeoLogger();

        neoDatabaseManager      = new NeoDatabaseManager();
        neoInventory            = new NeoInventory();
        neoMyShop               = new NeoMyShop( neoAccount );
        neoBuyer                = new NeoBuyer( neoAccount );
        
        neoDatabaseManager.clearBuylist();
        
        NeoHistory neoHistory   = neoDatabaseManager.getHistoryForToday( neoAccount, "items_traded_today" );
        if ( neoHistory == null ) {
            items_traded_today = 0;
        } else {
            items_traded_today = neoHistory.getValue();
        }
        
        httpClient = neoAccount.login();   
        GetGoing();
    }
    
    private void GetGoing() {
        new Thread( this ).start();
    }

    @Override
    public void run() {
        
        int updates = 0;
        
        while( true ) {

            timestamp = System.currentTimeMillis();
            
            if ( updates % ( neoInventory.getMaxItems() ) == 0 || neoInventory.isFull() ) {

                httpClient = neoInventory.update( httpClient );

                if ( neoInventory.isLoggedOut() ) {
                    httpClient = neoAccount.login( httpClient );
                }

                httpClient = neoMyShop.updateSize( httpClient );
                if ( neoMyShop.isLoggedOut() ) {
                    httpClient = neoAccount.login( httpClient );
                }
                
                if ( neoInventory.hasItems() & neoMyShop.hasSpaceLeft() ) {                
                    httpClient = neoInventory.stockShop( httpClient, neoMyShop.getSpaceLeft() );
                    httpClient = neoInventory.update( httpClient );
                }

                httpClient = neoMyShop.updateFull( httpClient );
                if ( neoMyShop.isLoggedOut() ) {
                    httpClient = neoAccount.login( httpClient );
                }

                
                if ( neoMyShop.itemsToBePriced() ) {
                    httpClient = neoMyShop.setPrices( httpClient );
                }

                // updating History
                neoDatabaseManager.addHistory( neoAccount, new NeoHistory( "neopoints_cash", neoMyShop.getNeopoints() )  );
                neoDatabaseManager.addHistory( neoAccount, new NeoHistory( "neopoints_invested_in_shopspace", neoMyShop.getShopSpaceValue() )  );
                neoDatabaseManager.addHistory( neoAccount, new NeoHistory( "neopoints_invested_in_shopitems", neoMyShop.getShopItemsValue() )  );
                int neoPointsTotal = neoMyShop.getNeopoints() +  neoMyShop.getShopSpaceValue() + neoMyShop.getShopItemsValue();
                neoDatabaseManager.addHistory( neoAccount, new NeoHistory( "neopoints_total", neoPointsTotal ));
                neoDatabaseManager.addHistory( neoAccount, new NeoHistory( "neopoints_earned_today", neoPointsTotal - neoDatabaseManager.getHistoryForYesterday( neoAccount, "neopoints_total" ).getValue() ) );

                neoDatabaseManager.addHistory( neoAccount, new NeoHistory( "shopspace_total", neoMyShop.getMaxSpace() )  );
                neoDatabaseManager.addHistory( neoAccount, new NeoHistory( "shopspace_free", neoMyShop.getSpaceLeft() )  );
                neoDatabaseManager.addHistory( neoAccount, new NeoHistory( "shopspace_occupied", neoMyShop.getSpaceStocked() )  );

                neoDatabaseManager.addHistory( neoAccount, new NeoHistory( "items_traded_today", items_traded_today )  );

                activity_seconds = neoDatabaseManager.getLatestHistory( neoAccount, "activity_seconds" ).getValue();
                int difference = (int) ((System.currentTimeMillis() - timestamp) / 1000);
                if ( difference < MAXIMUM_DURATION_OF_MAINACCOUNT_CYCLE ) {
                    activity_seconds = activity_seconds + difference;
                }
                neoDatabaseManager.addHistory( neoAccount, new NeoHistory( "activity_seconds", activity_seconds )  );

                neoDatabaseManager.addHistory( new NeoHistory( "items_bought_unique", neoDatabaseManager.getUniqueBoughtItems() ) );

            } 
            
            if ( neoMyShop.getSpaceLeft() > neoInventory.getNumberOfItems() ) { 

                httpClient = neoBuyer.buySomething( httpClient, neoMyShop.getNeopoints(), neoMyShop.getItems(), neoInventory.getItems() );
                if ( neoBuyer.isLoggedOut() ) {
                    httpClient = neoAccount.login( httpClient );
                }
                if ( neoBuyer.couterOffer() ) {
                    neoDatabaseManager.setHagglingPercentage(
                        neoDatabaseManager.getHagglingPercentage() -
                        neoDatabaseManager.getHagglingPercentageIncrease()
                    );
                }

                if ( neoBuyer.tooFast() ) {
                    neoDatabaseManager.setBuyWait(
                        neoDatabaseManager.getBuywait() +
                        neoDatabaseManager.getBuywaitIncrease()
                    );
                }

                if ( neoBuyer.soldOut() ) {
                    neoDatabaseManager.removeFromBuyList( neoBuyer.getItemToBuy() );
                }

                if ( neoBuyer.itemBought() ) {
                    neoDatabaseManager.setHagglingPercentage(
                        neoDatabaseManager.getHagglingPercentage() +
                        neoDatabaseManager.getHagglingPercentageIncrease()
                    );
                    neoDatabaseManager.updateTimesBought( neoBuyer.getItemToBuy() );
                    neoInventory.addItem( neoBuyer.getItemToBuy() );                            
                    items_traded_today++;                            
                }                    

            } else {
                log.i( "Shop hasn't enought space left. Gonna try to upgrade the shop." );
                if ( neoMyShop.getUpgradeCosts() <= neoMyShop.getNeopoints()  ) {
                    httpClient = neoMyShop.upgrade( httpClient );
                } else {
                    log.i( "Upgrade not affordable (upgrade costs: " + neoMyShop.getUpgradeCosts() + ", neopoints: " + neoMyShop.getNeopoints() + ")" );
                }
            }
            
            if ( neoBuyer.shopVisited() ) {
                try {
                    Thread.sleep( neoDatabaseManager.getBuywait() * 1000 );
                } catch ( InterruptedException ex ) {
                    log.e( "Error while sleeping before buying next item: " + ex.getMessage() );
                }
            } else {
                try {
                    Thread.sleep( 1000 );
                } catch ( InterruptedException ex ) {
                    log.e( "Error while sleeping before buying next item: " + ex.getMessage() );
                }
            }

            updates++;
            
        }
    }
}
