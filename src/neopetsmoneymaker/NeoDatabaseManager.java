package neopetsmoneymaker;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

public class NeoDatabaseManager {

    private Connection          connection;
    private NeoConfigurator     neoConfigurator;
    private NeoLogger           log;
    
    private final String        FILEPATH = "C:\\Users\\Gertjan & Chiela\\Documents\\NetBeansProjects\\NeopetsMoneyMaker\\files\\";
    private final String        DBNAME = "npmmdb.sqlite";

    public NeoDatabaseManager() {
        
        log = new NeoLogger( false );
        
        connect();
    }
    
    private void connect() {
    	
        boolean fillErUp = false;
        
        File f = new File( FILEPATH + DBNAME );
        if( !f.exists() ) {
            fillErUp = true;
        }
        
        try { 
            Class.forName( "org.sqlite.JDBC" );
            connection = DriverManager.getConnection( "jdbc:sqlite:" + FILEPATH + DBNAME );
        } catch( Exception e ) {
            log.e( "Error opening database: " + e.getMessage() );
        }
        
        if ( fillErUp ) {
            resetDatabase();
        }
        
    }

    private void resetDatabase() {

        neoConfigurator = new NeoConfigurator();
        
        log.i( "Resetting database." );

        try {
            Statement statement = connection.createStatement();
            
            log.i( "Dropping table SETTING.." );
            statement.executeUpdate("drop table if exists SETTING;" );
            log.i( "Dropping table MARKETRESEACH.." );
            statement.executeUpdate("drop table if exists MARKETRESEARCH;" );
            log.i( "Dropping table BUYLIST.." );
            statement.executeUpdate("drop table if exists BUYLIST;" );
            log.i( "Dropping table ITEM.." );
            statement.executeUpdate("drop table if exists ITEM;" );
            log.i( "Dropping table SHOP.." );
            statement.executeUpdate("drop table if exists SHOP;" );
            log.i( "Dropping table ACCOUNT.." );
            statement.executeUpdate("drop table if exists ACCOUNT;" );
            log.i( "Dropping table MARKETPART.." );
            statement.executeUpdate("drop table if exists MARKETPART;" );
            log.i( "Dropping table HISTORY.." );
            statement.executeUpdate("drop table if exists HISTORY;" );
            log.i( "Dropping table LOG.." );
            statement.executeUpdate("drop table if exists LOG;" );

            log.i( "Creating table SETTING.." );
            statement.executeUpdate(
                "create table SETTING(" +
                    "id         INTEGER PRIMARY KEY," +
                    "name       TEXT UNIQUE," +
                    "value      TEXT" +
                ");");
            
            log.i( "Creating table ACCOUNT.." );
            statement.executeUpdate(
                "create table ACCOUNT(" +
                    "id             INTEGER PRIMARY KEY," +
                    "username       TEXT UNIQUE," +
                    "password       TEXT," +
                    "date_of_birth  INTEGER " +
                ");");

            log.i( "Creating table SHOP.." );
            statement.executeUpdate(
                "create table SHOP(" +
                    "id         INTEGER PRIMARY KEY," +
                    "name       TEXT UNIQUE," +
                    "link       TEXT," +
                    "shopban    INTEGER" +
                ");");

            log.i( "Creating table ITEM.." );
            statement.executeUpdate(
                "create table ITEM ( " +
                    "id                     INTEGER PRIMARY KEY," +
                    "name                   TEXT UNIQUE," +
                    "shop_price             INTEGER," +
                    "discovered             INTEGER," +
                    "shop_id                INTEGER," +
                    "researched             INTEGER," +
                    "timessold              INTEGER," +
                    "FOREIGN KEY (shop_id) REFERENCES shop(id)" +
                ");");

            log.i( "Creating table MARKETPART.." );
            statement.executeUpdate(
                "create table MARKETPART ( " +
                    "id         INTEGER PRIMARY KEY," +
                    "name       TEXT UNIQUE" +
                ");");

            log.i( "Creating table MARKETRESEARCH.." );
            statement.executeUpdate(
                "create table MARKETRESEARCH ( " +
                    "id             INTEGER PRIMARY KEY," +
                    "item_id        INTEGER," +
                    "marketpart_id  INTEGER," +
                    "price          INTEGER," +
                    "researched     INTEGER," +
                    "UNIQUE (item_id, marketpart_id)," +
                    "FOREIGN KEY (item_id) REFERENCES item(id)," +
                    "FOREIGN KEY (marketpart_id) REFERENCES marketpart(id)" +
                ");");
            
            log.i( "Creating table BUYLIST.." );
            statement.executeUpdate(
                "create table BUYLIST ( " +
                    "id             INTEGER PRIMARY KEY," +
                    "item_id        INTEGER UNIQUE," +
                    "price          INTEGER," +
                    "quantity       INTEGER," +
                    "hagglelink     TEXT," +
                    "shoplink       TEXT," +
                    "found          DATETIME," +
                    "FOREIGN KEY (item_id) REFERENCES item(id)" +
                ");");
                        
            log.i( "Creating table HISTORY.." );
            statement.executeUpdate(
                "create table HISTORY ( " +
                    "id             INTEGER PRIMARY KEY," +
                    "date           STRING," +
                    "type           STRING," +
                    "account        STRING," +
                    "value          INTEGER," +
                    "UNIQUE (date, type, account)" +
                ");");

            log.i( "Creating table LOG.." );
            statement.executeUpdate(
                "create table LOG ( " +
                    "id             INTEGER PRIMARY KEY," +
                    "severity       TEXT," +
                    "message        TEXT," +
                    "timestamp      INTEGER" +
                ");");   
            
            statement.close();
            
        } catch( Exception e ) {
            log.e( "Error resetting database: " + e.getMessage() );
        }

        log.i( "Initializing database." );
        
        try {
            log.i( "Inserting records for table SETTING.." );
            
            Statement statement = connection.createStatement();
            
            String sql;
            
            List<NeoSetting> settings = neoConfigurator.getSettings();

            for( int x=0; x<settings.size(); x++ ) {
            	sql =
                    "insert into SETTING (" +
                        "name, " +
                        "value) " +
                    "VALUES ( " +
                    	"'" + settings.get( x ).getName() + "', " +
                    	"'" + settings.get( x ).getValue() + "' );";

                statement.executeUpdate( sql );     
            }

            log.i( "Inserting records for table MARKETPARTS.." );


            StringTokenizer stringTokenizer = new StringTokenizer( getMarketparts(), ",");

            while( stringTokenizer.hasMoreTokens() ) {
                sql = 
                    "insert into MARKETPART ( " +
                        "name ) " +
                    "VALUES ( " +
                        "'" + stringTokenizer.nextToken() + "' ) ";
                statement.executeUpdate( sql ); 
            }
            
            log.i( "Inserting records for table ACCOUNT.." );
            
            List<NeoAccount> accounts = neoConfigurator.getAccounts();

            SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd" );

            for( int x=0; x<accounts.size(); x++ ) {
                sql = 
                    "insert into ACCOUNT (" +
                        "username, " +
                        "password, " +
                        "date_of_birth) " +
                    "VALUES ( " +
                        "'" + accounts.get( x ).getUsername() + "', " +
                        "'" + accounts.get( x ).getPassword() + "', " +
                        "'" + sdf.format( accounts.get( x ).getDateOfBirth() ) + "'";

                statement.executeUpdate( sql ); 
            }

            log.i( "Inserting records for table SHOP.." );

            List<NeoShop> shops = neoConfigurator.getShops();

            for( int x=0; x<shops.size(); x++ ) {
            	
                int boolFlag = shops.get( x ).getCheckForShopban()? 1 : 0;
                
                sql =
                    "insert into SHOP (" +
                        "name, " +
                        "link, " +
                        "shopban) " +
                    "VALUES ( " +
                    "'" + shops.get( x ).getName() + "', " +
                    "'" + shops.get( x ).getLink() + "', " +
                    "" + boolFlag + " ) ";
            	
            	statement.executeUpdate( sql ); 
            }

            statement.close();
            
        } catch( Exception e ) {
            log.e( "Error initializing database: " + e.getMessage() );
        }

    }

    private void disconnect() {
    	log.i( "Disonnecting from database." );
                
        try {
            connection.close();
        } catch ( Exception e ) {
            log.e( "Error closing database: " + e.getMessage() );
        }
    }
    
    public synchronized List<NeoAccount> getAccounts() {
    	
    	List<NeoAccount> accounts = new ArrayList<NeoAccount>();

        try {
            String sql =
                "select " +
                    "username, " +
                    "password, " +
                    "date_of_birth " +
                "from account";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( sql ); 

            SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd" );
            
            while( resultSet.next() ) {
                accounts.add(
                    new NeoAccount(
                        resultSet.getString( 1 ),
                        resultSet.getString( 2 ),
                        sdf.parse( resultSet.getString( 3 ) )
                    )
                );
            }
            
            resultSet.close();
            statement.close();

        } catch( Exception e ) {
            log.e( "Error getting accounts from database: " + e.getMessage() );
        }

        return accounts;
    }    

    public synchronized List<NeoShop> getShops() {
        
        List<NeoShop> shops = new ArrayList<NeoShop>();

        try {
            
            Statement statement = connection.createStatement();
            
            String sql =
                "select " +
                    "name, " +
                    "link, " +
                    "shopban " +
                "from shop";
            
            ResultSet resultSet = statement.executeQuery( sql ); 
            
            while( resultSet.next() ) {
                shops.add( 
                    new NeoShop (
                        resultSet.getString( 1 ), 		
                        resultSet.getString( 2 ), 		
                        resultSet.getBoolean( 3 )		
                    ) 
                );
            }
            
            resultSet.close();
            statement.close();

        } catch( Exception e ) {
            log.e( "Error getting shops from database: " + e.getMessage() );
        }

        return shops;
    }

    public synchronized boolean itemsExist() {
        Boolean itemsExists = false;
    	
    	try {
            String sql = "select count(*) from item";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( sql ); 
				
            if ( resultSet.next() ) {
                if ( resultSet.getInt( 1 ) > 0 ) {
                    itemsExists = true;
                }
            }
            
            resultSet.close();
            statement.close();

        } catch( Exception e ) {
            log.e( "Error checking for existing items in database: " + e.getMessage() );
        }

    	return itemsExists;
    }
    
    public synchronized void addItemsFromShop( List<NeoItem> items ) {
       
        if ( items.size() > 0 ) {

            // Adding results to ITEM table if unknown
            try {
            	Statement statement = connection.createStatement();
                
                for( int x=0; x<items.size(); x++ ) {
                    
                    String sql = 
                        "select id, shop_price from item " +
                        "where item.name = '" + items.get( x ).getName() + "'";
                    
                    ResultSet resultSet = statement.executeQuery( sql );  
                    
                    int itemId = 0;
                    int shopPriceFromDB = 0;
                    
                    if ( resultSet.next() ) { 
                        itemId = resultSet.getInt( 1 );
                        shopPriceFromDB = resultSet.getInt( 2 );
                    }
                    resultSet.close();
                    
                    if ( itemId != 0 ) {
                    
                        if ( items.get( x ).getShopPrice() != shopPriceFromDB ) {
                            
                            sql =
                                "update ITEM set " +
                                    "shop_price = '" + items.get( x ).getShopPrice() + "' " +
                                "where id = '" + itemId + "'";
                            
                            statement.executeUpdate( sql );  
                            
                        }
                    
                    } else {
                        
                        sql = 
                            "select id " +
                            "from shop " +
                            "where " +
                                "name = '" + items.get( x ).getShopName().replaceAll( "'", "''" ) + "'";
                        
                        ResultSet resultSet2 = statement.executeQuery( sql );  
                        resultSet2.next();
                        int shopId = resultSet2.getInt( 1 );
                        resultSet2.close();
                        
                        sql =
                            "insert into item (" +
                                "name, " +
                                "shop_id, " +
                                "shop_price, " +
                                "discovered) " +
                            "values ( " +
                                "'" + items.get( x ).getName() + "', " +
                                "'" + shopId + "', " +
                                "'" + Integer.toString( items.get( x ).getShopPrice() ) + "', " +
                                "'" + Long.toString( Calendar.getInstance().getTimeInMillis() ) + "' ) "; 
                        
                        statement.executeUpdate( sql );                         
                    }
                    
                    resultSet.close();          		
                }

                statement.close();
                
            } catch( Exception e ) {
                log.e( "Error adding item from shop to database (item): " + e.getMessage() );
            }

            // Adding results to BUYLIST
            // But only those worth buying!

            try {
                
                Statement statement = connection.createStatement();
                int addedToBuyList = 0;
                int updatedBuyList = 0;
                  
                for( int x=0; x<items.size(); x++ ) {
                    if ( items.get( x ).getShopPrice() <= getBuyPrice( items.get( x ) ) ) { 
                    	
                        String sql =
                            "select id " +
                            "from item " +
                            "where " +
                                "name = '" + items.get( x ).getName() + "'";

                        ResultSet resultSet = statement.executeQuery( sql ); 
                        resultSet.next();
                        int itemId = resultSet.getInt( 1 );
                        resultSet.close();

                        sql = 
                            "select * from BUYLIST " +
                            "where " +
                                "buylist.item_id = '" + itemId + "'";
                    
                        resultSet = statement.executeQuery( sql );  

                        if ( !resultSet.next() ) {                        
                        
                            sql =
                                "insert into BUYLIST (" +
                                    "item_id," +
                                    "price," +
                                    "quantity," +
                                    "hagglelink," +
                                    "shoplink," +
                                    "found )" +
                                "values ( " +
                                    "'" + itemId + "'," +
                                    "'" + Integer.toString( items.get( x ).getShopPrice() ) + "'," +
                                    "'" + Integer.toString( items.get( x ).getQuantity() ) + "'," +
                                    "'" + items.get( x ).getHaggleLink() + "'," +
                                    "'" + items.get( x ).getShopLink() + "'," +
                                    "'" + Long.toString( Calendar.getInstance().getTimeInMillis() ) + "' ) ";

                            statement.executeUpdate( sql );   
                            addedToBuyList++;
                            
                        } else {
                            
                            sql = 
                                "update BUYLIST set " +
                                    "quantity = '" + Integer.toString( items.get( x ).getQuantity() ) + "', " +
                                    "found = '" + Long.toString( Calendar.getInstance().getTimeInMillis() ) + "' " +
                                "where " +
                                    "item_id = '" + itemId + "'";

                            statement.executeUpdate( sql );                               
                            updatedBuyList++;                            
                        }
                        resultSet.close();
                    }                
                    statement.close();                    
                }
                
                if ( addedToBuyList + updatedBuyList > 0 ) {
                    log.i( "Items added to buylist: " + addedToBuyList + ", items updated in buylist: " + updatedBuyList );
                }
            } catch( Exception e ) {
                log.e( "Error adding item from shop to database (buylist): " + e.getMessage() );
            }
        }
    }

    public synchronized NeoItem getItemForMarketSearch() {

        NeoItem neopetsItem = null;

        try {
            String sql =
                "select item.name " +
                "from item " +
                "where researched is null " +
                "order by shop_id " + // shop_id met null eerst, want deze komen uit de eigen shop!
                "limit 1";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( sql ); 
            
            if ( resultSet.next() ) {
            	neopetsItem = new NeoItem( resultSet.getString( 1 ) );
                resultSet.close();
            } else {

                sql =
                    "select item.name, item.id " +
                    "from item " +
                    "order by researched " +
                    "limit 1";

                resultSet = statement.executeQuery( sql ); 
                
                if ( resultSet.next() ) {
                    neopetsItem = new NeoItem( resultSet.getString( 1 ) );
                    
                    int itemId = resultSet.getInt( 2 );
                    
                    // Set researched to null for this item
                    sql = 
                        "update ITEM set " +
                            "researched = NULL " +
                        "where id = '" + itemId + "'";
                    
                    statement.executeUpdate( sql );
                                                        
                    // Delete all info from marketresearch for this item
                    sql = 
                        "delete from MARKETRESEARCH " + 
                        "where item_id = '" + itemId + "'";
                    
                    statement.executeUpdate( sql );                
                }
                
                resultSet.close();
                statement.close();
            }
        } catch( Exception e ) {
            log.e( "Error getting the next item for marketsearch from database: " + e.getMessage() );
        }

        return neopetsItem;
    }

    public synchronized void addMarketResult( NeoItem neoItem ) {

        try {
            
            if ( neoItem.getOwner() != null ) {
            
                Statement statement = connection.createStatement();

                String sql =
                    "select id " +
                    "from item " +
                    "where " +
                        "name = '" + neoItem.getName() + "'";
                
                ResultSet resultSet = statement.executeQuery( sql ); 
                resultSet.next();
                int itemId = resultSet.getInt( 1 );
                resultSet.close();
                
                sql =
                    "select id " +
                    "from marketpart " +
                    "where " +
                        "name = '" + getMarketPart( neoItem ) + "'";
                
                resultSet = statement.executeQuery( sql ); 
                resultSet.next();
                int marketPartId = resultSet.getInt( 1 );
                resultSet.close();
                
                sql =
                    "select item_id, marketpart_id " +
                    "from MARKETRESEARCH " +
                    "where " +
                        "item_id = '" + itemId + "' AND " +
                        "marketpart_id = '" + marketPartId + "'";
                        
                resultSet = statement.executeQuery( sql ); 
                if ( !resultSet.next() ) {
                    sql =
                        "insert into MARKETRESEARCH (" +
                            "item_id, " +
                            "marketpart_id, " +
                            "price, " +
                            "researched ) " +
                        "VALUES (" +
                            "'" + itemId + "', " +
                            "'" + marketPartId + "', " +
                            "'" + Integer.toString( neoItem.getMarketPrice() ) + "', " +
                            "'" + Long.toString( Calendar.getInstance().getTimeInMillis() ) + "' ) ";

                    statement.executeUpdate( sql );   
                } 
                resultSet.close();
                
                // Set researched timestamp when market is searched enough
                sql =
                    "select count(*) " +
                    "from marketresearch " +
                    "where " +
                        "item_id = '" + itemId + "' " +
                        "and marketpart_id is not null";

                resultSet = statement.executeQuery( sql ); 
                resultSet.next();
                int marketParts = resultSet.getInt( 1 );
                resultSet.close();

                if ( marketParts >= getMinimalMarketParts() ) {

                    sql =
                        "update item set " +
                             "researched = '" + Long.toString( Calendar.getInstance().getTimeInMillis() ) + "' " +
                        "where " +
                            "name = '" + neoItem.getName() + "'";

                    statement.executeUpdate( sql ); 

                    statement.close();
                }
            } else {

                Statement statement = connection.createStatement();
                
                String sql =
                    "update item set " +
                        "researched = '" + Long.toString( Calendar.getInstance().getTimeInMillis() ) + "' " +                        
                    "where " +
                        "name = '" + neoItem.getName() + "'";

                statement.executeUpdate( sql ); 

                statement.close();

            }

        } catch( Exception e ) {
            log.e( "Error adding marketsearch result to database: " + e.getMessage() );
        }
    }
    
    public synchronized int getItemId( NeoItem item ) { 
        
        int itemId = 0;

        try {
            Statement statement = connection.createStatement();
             
            String sql =
                "select id " +
                "from item " +
                "where " +
                    "name = '" + item.getName() + "'";

            ResultSet resultSet = statement.executeQuery( sql ); 
            resultSet.next();
            itemId = resultSet.getInt( 1 );
            resultSet.close();
            statement.close();
            
        } catch( Exception e ) {
            log.e( "Error getting item id for " + item.getName() + ": " + e.getMessage() );
        }

        return itemId;
    
    }

    public synchronized int getMarketPrice( int itemId, int marketPartId ) { 
        
        int marketPrice = 0;

        try {
            Statement statement = connection.createStatement();
             
            String sql =
                "select price " +
                "from MARKETRESEARCH " +
                "where " +
                    "item_id = '" + itemId + "' AND " +
                    "marketpart_id = '" + marketPartId + "'";

            ResultSet resultSet = statement.executeQuery( sql ); 
            if ( resultSet.next() ) {
                marketPrice = resultSet.getInt( 1 );
            }
            
            resultSet.close();
            statement.close();
            
        } catch( Exception e ) {
            log.e( "Error getting marketprice for item id " + itemId + " and marketPart id " + marketPartId + ": " + e.getMessage() );
        }

        return marketPrice;
    
    }

    public synchronized int getMarketPrice( int itemId ) { 
        
        int marketPrice = 0;

        try {
            Statement statement = connection.createStatement();
             
            String sql =
                "select min( price ) " +
                "from MARKETRESEARCH " +
                "where " +
                    "item_id = '" + itemId + "'";

            ResultSet resultSet = statement.executeQuery( sql ); 
            if ( resultSet.next() ) {
                marketPrice = resultSet.getInt( 1 );
            }
            resultSet.close();
            statement.close();
            
        } catch( Exception e ) {
            log.e( "Error getting marketprice for item id " + itemId + ": " + e.getMessage() );
        }

        return marketPrice;
    
    }

    public synchronized int getBuyPrice( NeoItem neoItem, NeoAccount neoAccount ) {

        int buyPrice = 0;

        try {
             
            int itemId = getItemId( neoItem );

            int marketPartId = getMarketPartId( neoAccount );
            
            int minimalMarketPrice = getMarketPrice( itemId, marketPartId );
            
            buyPrice = (  minimalMarketPrice * (100 - getProfitMarge() - getBuyMarge() + getHagglingPercentage() ) ) / 100;
            
        } catch( Exception e ) {
            log.e( "Error while getting the buy price for item " + neoItem.getName() + " and account " + neoAccount.getUsername() + ": " + e.getMessage() );
        }

        return buyPrice;

    }

    public synchronized int getBuyPrice( NeoItem neoItem ) {

        int buyPrice = 0;

        try {
             
            int itemId = getItemId( neoItem );

            int minimalMarketPrice = getMarketPrice( itemId );
            
            buyPrice = (  minimalMarketPrice * (100 - getProfitMarge() - getBuyMarge() + getHagglingPercentage() ) ) / 100;
            
        } catch( Exception e ) {
            log.e( "Error while getting the buy price for item " + neoItem.getName() + ": " + e.getMessage() );
        }

        return buyPrice;

    }

    public synchronized int getSellPrice( NeoItem neoItem, NeoAccount neoAccount ) {

        int sellPrice = 0;

        try {
             
            int itemId = getItemId( neoItem );

            int marketPartId = getMarketPartId( neoAccount );
            
            int minimalMarketPrice = getMarketPrice( itemId, marketPartId );
            
            sellPrice = ( minimalMarketPrice * (100 - getProfitMarge()) ) / 100;
            
        } catch( Exception e ) {
            log.e( "Error while getting the sell price for " + neoItem.getName() + " account " + neoAccount.getUsername() + ": " + e.getMessage() );
        }

        return sellPrice;
    }

    public synchronized boolean anythingToBuy() {
        
        Boolean somethingForSale = false;

        try {
            String sql = "select count(*) from buylist";
           
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( sql ); 

            resultSet.next();
            if ( resultSet.getInt( 1 ) > 0 ) {
                somethingForSale = true;
            }

            resultSet.close();
            statement.close();
            
        } catch( Exception e ) {
            log.e( "Error getting the number of items in the buylist in the database: " + e.getMessage() );
        }

        return somethingForSale;
    }

    public synchronized NeoItem getNextItemToBuy( int neopoints, List<NeoItem> shopItems, List<NeoItem> inventoryItems, NeoAccount neoAccount ) {

        NeoItem nextItemToBuy = null;

        try {
            
            int marketPartId = this.getMarketPartId( neoAccount );
            
            // Select all items from the buylist, in order of marge
            String sql =
                "select " +
                    "item.name, " +
                    "buylist.price, " +
                    "buylist.quantity, " +
                    "buylist.hagglelink, " +
                    "buylist.shoplink, " +
                    "buylist.found," +
                    "marketresearch.price - buylist.price as marge " +
                "from " +
                    "item, " +
                    "buylist," +
                    "marketresearch " +
                "where " +
                    "item.id = buylist.item_id AND " +
                    "item.id = marketresearch.item_id AND " +
                    "marketresearch.marketpart_id = '" + marketPartId + "' " +
                "order by " +
                    "marge desc";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( sql ); 

            boolean itemsLeft = true;
            boolean enoughNeopoints = true;
            boolean alreadyInShopOrInventory = false;
            
            do {
                
                if ( resultSet.next() ) { 
                
                    nextItemToBuy = new NeoItem(
                        resultSet.getString( 1 ),
                        resultSet.getInt( 2 ),
                        resultSet.getInt( 3 ),
                        resultSet.getString( 4 ),
                        resultSet.getString( 5 ),
                        new Timestamp( resultSet.getLong( 6 ) )
                    );

                    nextItemToBuy.setSellPrice( getSellPrice( nextItemToBuy, neoAccount ) );   
                    nextItemToBuy.setHagglingPrice( (int) ( ( nextItemToBuy.getShopPrice() * ( 100 - getHagglingPercentage() ) ) / 100 ) );

                    // Check if enough neopoints are available for buying the item
                    if ( neopoints >= nextItemToBuy.getShopPrice() ) {
                        enoughNeopoints = true;
                    } else {
                        enoughNeopoints = false;
                        nextItemToBuy = null;
                    }

                    if ( enoughNeopoints ) { 
                    
                        // Check if this item already is in shop or inventory
                        alreadyInShopOrInventory = false;

                        for( int x=0; x<shopItems.size(); x++ ) {

                            if ( shopItems.get( x ).getName().equals( nextItemToBuy.getName() ) ) {
                                alreadyInShopOrInventory = true;
                                nextItemToBuy = null;
                                break;
                            }
                        }
                    
                        if ( !alreadyInShopOrInventory ) { 

                            for( int x=0; x<inventoryItems.size(); x++ ) {

                                if ( inventoryItems.get( x ).getName().equals( nextItemToBuy.getName() ) ) {
                                    alreadyInShopOrInventory = true;
                                    nextItemToBuy = null;
                                    break;
                                }
                            }                    
                        }
                    }

                } else {
                    itemsLeft = false;
                }
                
            } while ( ( alreadyInShopOrInventory | !enoughNeopoints ) & itemsLeft );

            resultSet.close();
            statement.close();

        } catch( Exception e ) {
            log.e( "Error getting next item to buy from database: " + e.getMessage() );
        }

        return nextItemToBuy;
    }

    public synchronized void removeFromBuyList( NeoItem neopetsItem ) {

        try {
            Statement statement = connection.createStatement();
            
            String sql =
                "select id " +
                "from item " +
                "where " +
                    "name = '" + neopetsItem.getName() + "'" ;

            ResultSet resultSet = statement.executeQuery( sql ); 
            int itemId = 0;
            if ( resultSet.next() ) {
                itemId = resultSet.getInt( 1 );
            }
            resultSet.close();
            
            sql =
                "delete from buylist " +
                "where item_id = " + itemId;
            
            statement.executeUpdate( sql ); 

            statement.close();            

        } catch( Exception e ) {
            log.e( "Error removing item from buylist from database: " + e.getMessage() );
        }

    }

    public synchronized void clearBuylist() {
        
        try {
            Statement statement = connection.createStatement();

            String sql =
                "delete from BUYLIST";

            statement.executeUpdate( sql ); 

            statement.close();

        } catch( Exception e ) {
            log.e( "Error clearing buylist in database: " + e.getMessage() );
        }
    
    }
    
    public synchronized boolean itemExists( NeoItem neopetsItem ) {

        boolean exists = false;

        try {
            String sql =
                "select count(*) " +
                "from item " +
                "where name = '" + neopetsItem.getName() + "'" ;

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( sql ); 

            resultSet.next();
            if ( resultSet.getInt( 1 ) > 0 ) {
                exists = true;
            }

            resultSet.close();
            statement.close();
            
        } catch( Exception e ) {
            log.e( "Error while checking if the items exists in the database: " + e.getMessage() );
        }

        return exists;
    }

    public synchronized void addItem( NeoItem neopetsItem ) {
        
        try {
            Statement statement = connection.createStatement();

            String sql = 
                "select id " +
                "from shop " +
                "where " +
                    "name = '" + neopetsItem.getShopName().replaceAll( "'", "''" ) + "'";

            ResultSet resultSet = statement.executeQuery( sql );  
            int shopId = -1;
            if ( resultSet.next() ) {
                shopId = resultSet.getInt( 1 );
            }
            resultSet.close();

            sql =
                "insert into item (" +
                    "name, " +
                    "shop_id, " +
                    "shop_price, " +
                    "discovered) " +
                "values ( " +
                    "'" + neopetsItem.getName() + "', " +
                    "'" + shopId + "', " +
                    "'" + Integer.toString( neopetsItem.getShopPrice() ) + "', " +
                    "'" + Long.toString( Calendar.getInstance().getTimeInMillis() ) + "') ";

            statement.executeUpdate( sql ); 
            
            statement.close();
        	
        } catch( Exception e ) {
            log.e( "Error adding item (" + neopetsItem.getName() + ") to database: " + e.getMessage() );
        }
    }

    public synchronized int getShopWait() {
        return Integer.parseInt( getSetting( "shopwait" ) );
    }

    public synchronized int getShopbanWait() {
        return Integer.parseInt( getSetting( "shopbanwait" ) );
    }

    public synchronized int getShopWaitIncrease() {
        return Integer.parseInt( getSetting( "shopwaitincrease" ) );
    }

    public synchronized int getMarketWait() {
        return Integer.parseInt( getSetting( "marketwait" ) );
    }

    public synchronized int getMarketbanWait() {
        return Integer.parseInt( getSetting( "marketbanwait" ) );
    }

    public synchronized int getMarketWaitIncrease() {
        return Integer.parseInt( getSetting( "marketwaitincrease" ) );
    }

    public synchronized String getMarketparts() {
        return (String) getSetting( "marketparts" );
    }

    public synchronized int getMarketShare() {
        return Integer.parseInt( getSetting( "marketshare" ) );
    }

    public synchronized int getNumberOfMarketParts() {

        int numberOfMarketParts = 0;

        try {
            String sql = 
                "select count(*) from marketpart";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( sql ); 

            resultSet.next();
            numberOfMarketParts = resultSet.getInt( 1 );

            resultSet.close();
            statement.close();
	
        } catch( Exception e ) {
            log.e( "Error getting number of marketparts from database: " + e.getMessage() );
        }

        return numberOfMarketParts;
    }

    public synchronized int getProfitMarge() {
        return Integer.parseInt( getSetting( "profitmarge" ) );
    }

    public synchronized int getBuyMarge() {
        return Integer.parseInt( getSetting( "buymarge" ) );
    }

    public synchronized int getBuywait() {
        return Integer.parseInt( getSetting( "buywait" ) );
    }

    public synchronized int getBuywaitIncrease() {
        return Integer.parseInt( getSetting( "buywaitincrease" ) );
    }

    public synchronized String getMarketPart( NeoItem neopetsItem ) {

        String marketPart = "";

        try {
            String sql = 
                "select name from marketpart " +
                "where "
                    + "name like '%" + neopetsItem.getOwner().substring( 0, 1 ) +  "%'";
            
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( sql ); 

            resultSet.next();
            marketPart = resultSet.getString( 1 );

            resultSet.close();
            statement.close();
            
        } catch( Exception e ) {
            log.e( "Error getting marketpart for item " + neopetsItem.getName() + " from database: " + e.getMessage() );
        }

        return marketPart;
    }
    
    public synchronized int getMarketPartId( NeoAccount neoAccount ) {

        int marketPartId = 0;

        try {
            String sql = 
                "select id from marketpart " +
                "where "
                    + "name like '%" + neoAccount.getUsername().substring( 0, 1 ) +  "%'";
            
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( sql ); 

            resultSet.next();
            marketPartId = resultSet.getInt( 1 );

            resultSet.close();
            statement.close();
            
        } catch( Exception e ) {
            log.e( "Error getting marketpart for account " + neoAccount.getUsername() + " from database: " + e.getMessage() );
        }

        return marketPartId;
    }    

    public synchronized int getMinimalMarketParts() {
        return ((getNumberOfMarketParts() * getMarketShare()) / 100);
    }

    public synchronized int getHagglingPercentage() {
        return Integer.parseInt( getSetting( "hagglingpercentage" ) );
    }
    
    public synchronized int getHagglingPercentageIncrease() {
        return Integer.parseInt( getSetting( "hagglingpercentageincrease" ) );
    }

    public synchronized String getSetting( String name ) {

        String value = "";

        try {

            String sql = 
                "select value " +
                "from setting " + 
                "where name = '" + name + "'";
            
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( sql ); 

            resultSet.next();
            value = resultSet.getString( 1 );
            
            resultSet.close();
            statement.close();
            
        } catch( Exception e ) {
            log.e( "Error getting setting (name = '" + name + "') from database: " + e.getMessage() );
        }

        return value;
    }

    public synchronized void setShopWait( int waittime ) {
        setSetting( "shopwait", waittime );
    }

    public synchronized void setMarketWait( int waittime ) {
        setSetting( "marketwait", waittime );
    }

    public synchronized void setBuyWait( int waittime ) {
        setSetting( "buywait", waittime );
    }

    public synchronized void setHagglingPercentage( int hagglingPercentage ) {
        setSetting( "hagglingpercentage", hagglingPercentage );
    }
    
    public synchronized void setSetting( String name, int value ) {

        try {
            Statement statement = connection.createStatement();
            
            String sql = 
                "update setting set " +
                "value = '" + Integer.toString( value ) + "' " +
                        "where name = '" + name + "'";
            
            statement.executeUpdate( sql ); 
            
            statement.close();

        } catch( Exception e ) {
            log.e( "Error setting setting to database: " + e.getMessage() );
        }

    }
    
    public synchronized void addLog( String severity, String message ) {

        try {
            Statement statement = connection.createStatement();

            String sql =
                "insert into LOG (" +
                    "severity, " +
                    "message, " +
                    "timestamp ) " +
                "values ( " +
                    "'" + severity + "', " +
                    "'" + message.replaceAll( "'", "''" ) + "', " +
                    "'" + Long.toString( Calendar.getInstance().getTimeInMillis() ) + "' ) ";

            statement.executeUpdate( sql ); 

            statement.close();

        } catch( Exception e ) {
            log.e( "Error adding log database: " + e.getMessage() );
        }

    }
    
    public synchronized void addHistory( NeoAccount neoAccount, NeoHistory neoHistory ) {
    
        try {
            Statement statement = connection.createStatement();

            SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd" );
            String date = sdf.format( neoHistory.getDate() );

            String sql;
            if ( neoAccount.getUsername() == null ) {
                sql =
                    "select * from HISTORY " +
                    "where " +
                        "date = '" + date + "' AND " +
                        "type = '" + neoHistory.getType() + "'";
            } else {
                sql =
                    "select * from HISTORY " +
                    "where " +
                        "date = '" + date + "' AND " +
                        "type = '" + neoHistory.getType() + "' AND " +
                        "account = '" + neoAccount.getUsername() + "'";
            }
            
            ResultSet resultSet = statement.executeQuery( sql ); 
            
            if ( resultSet.next() ) {
                // History item for this type and day already existst
                if ( neoAccount.getUsername() == null ) {
                    sql = 
                        "update HISTORY set " +
                            "value = '" + neoHistory.getValue()+ "' " +
                        "where " +
                            "date = '" + date + "' AND " + 
                            "type = '" + neoHistory.getType() + "'";
                } else {
                    sql = 
                        "update HISTORY set " +
                            "value = '" + neoHistory.getValue()+ "' " +
                        "where " +
                            "date = '" + date + "' AND " + 
                            "type = '" + neoHistory.getType() + "' AND " +
                            "account = '" + neoAccount.getUsername() + "'";
                }                
                statement.executeUpdate( sql );             		
                statement.close();
            } else {
                if ( neoAccount.getUsername() == null ) {
                    sql =
                        "insert into HISTORY ( " +
                            "date, " +
                            "type, " +
                            "value " + 
                        ") values ( " +
                            "'" + date + "', " +
                            "'" + neoHistory.getType() + "', " +
                            "'" + neoHistory.getValue()+ "' " +
                        ")";
                } else {
                    sql =
                        "insert into HISTORY ( " +
                            "date, " +
                            "type, " +
                            "account, " +
                            "value " + 
                        ") values ( " +
                            "'" + date + "', " +
                            "'" + neoHistory.getType() + "', " +
                            "'" + neoAccount.getUsername() + "', " +
                            "'" + neoHistory.getValue()+ "' " +
                        ")";
                }
                statement.executeUpdate( sql ); 
                statement.close();
            }

            resultSet.close();

        } catch( Exception e ) {
            log.e( "Error adding history in database: " + e.getMessage() );
        }            

    }
    
    public synchronized void addHistory( NeoHistory neoHistory ) {
    
        addHistory( new NeoAccount( null ), neoHistory );
        
    }
    
    public synchronized List<NeoHistory> getHistoryAsSomOfAccounts( String type ) {
        
        List<NeoHistory> neoHistory = new ArrayList<NeoHistory>();
                
        try {
            String sql =
                "select " +
                    "date, " + 
                    "sum( value ) " +
                "from history " +
                "where " +
                    "type = '" + type + "' and " +
                    "(account <> 'de_winkelman' OR account is null) " + 
                "group by date";
            
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( sql ); 

            SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd" );
            
            while( resultSet.next() ) {

                java.util.Date date = sdf.parse( resultSet.getString( 1 ) );

                neoHistory.add(
                    new NeoHistory(
                        date,
                        type,
                        resultSet.getInt( 2 )
                    )
                );
            }
            
            resultSet.close();
            statement.close();

        } catch( Exception e ) {
            log.e( "Error getting history as sum of accounts (type: " + type + ") from database: " + e.getMessage() );
        }
        
        return neoHistory;        
    }

    public synchronized NeoHistory getHistoryForYesterday( NeoAccount neoAccount, String type ) {
        
        Calendar calendar = Calendar.getInstance();
        calendar.add( Calendar.DATE, -1 );  
        java.util.Date yesterdayDate = calendar.getTime(); 
        
        return getHistoryFromDate( neoAccount, type, yesterdayDate );

    }

    public synchronized NeoHistory getHistoryForToday( NeoAccount neoAccount, String type ) {
        
        Calendar calendar = Calendar.getInstance();
        java.util.Date todayDay = calendar.getTime(); 
        
        return getHistoryFromDate( neoAccount, type, todayDay );

    }

    private synchronized NeoHistory getHistoryFromDate( NeoAccount neoAccount, String type, java.util.Date date ) {

        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd" );
        String dateString = sdf.format( date );
        
        NeoHistory neoHistory = new NeoHistory( date, type, 0 );
        
        try {
            String sql =
                "select " +
                    "value " +
                "from history " +
                "where " +
                    "type = '" + type + "' AND " +
                    "date = '" + dateString + "' AND " +
                    "account = '" + neoAccount.getUsername() + "'";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( sql ); 

            if( resultSet.next() ) {
                neoHistory = new NeoHistory( date, type, resultSet.getInt( 1 ) );
            }
            
            resultSet.close();
            statement.close();

        } catch( Exception e ) {
            log.e( "Error getting history (account: " + neoAccount.getUsername() + ", date: " + date + ", type: " + type + ") from database: " + e.getMessage() );
        }
        
        return neoHistory;        
    }    
    
    public synchronized NeoHistory getLatestHistory( NeoAccount neoAccount, String type ) {

        NeoHistory neoHistory = new NeoHistory( type, 0 );
        
        try {
            String sql =
                "select " +
                    "value " +
                "from history " +
                "where " +
                    "history.type = '" + type + "' AND " +
                    "account = '" + neoAccount.getUsername() + "' " + 
                "order by history.date desc " +
                "limit 1 ";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( sql ); 

            if( resultSet.next() ) {
                neoHistory = new NeoHistory( type, resultSet.getInt( 1 ) );
            }
            
            resultSet.close();
            statement.close();

        } catch( Exception e ) {
            log.e( "Error getting latest history for account: " + neoAccount.getUsername() + ", type: " + type + " from database: " + e.getMessage() );
        }
        
        return neoHistory;        
    }

    public synchronized int getKnownItems() {

        int items = 0;
    	
    	try {
            String sql = 
                "select count(*) " +
                "from item";
            
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( sql ); 
				
            if ( resultSet.next() ) {
                items = resultSet.getInt( 1 );
            }
            
            resultSet.close();
            statement.close();

        } catch( Exception e ) {
            log.e( "Error getting number of items known from database: " + e.getMessage() );
        }

    	return items;
    }

    public synchronized int getMarketResearchedItems() {

        int items = 0;
    	
    	try {
            String sql = 
                "select count(*) " +
                "from item " +
                "where researched not null";
            
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( sql ); 
				
            if ( resultSet.next() ) {
                items = resultSet.getInt( 1 );
            }
            
            resultSet.close();
            statement.close();

        } catch( Exception e ) {
            log.e( "Error getting number of items marketresearched from database: " + e.getMessage() );
        }

    	return items;
    }


    public synchronized int getTradeableItems() {

        int items = 0;
    	
    	try {
            String sql = 
                "select count( distinct ( item.id ) ) " +
                "from marketresearch, item " +
                "where " +
                    "item.id = marketresearch.item_id AND " + 
                    "marketresearch.price > item.shop_price";
            
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( sql ); 
				
            if ( resultSet.next() ) {
                items = resultSet.getInt( 1 );
            }
            
            resultSet.close();
            statement.close();

        } catch( Exception e ) {
            log.e( "Error getting number of items tradeable from database: " + e.getMessage() );
        }

    	return items;
    }

    public synchronized int getUniqueBoughtItems() {

        int items = 0;
    	
    	try {
            String sql = 
                "select count(*) " +
                "from item " +
                "where times_bought not null";
            
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( sql ); 
				
            if ( resultSet.next() ) {
                items = resultSet.getInt( 1 );
            }
            
            resultSet.close();
            statement.close();

        } catch( Exception e ) {
            log.e( "Error getting number of items unique bought from database: " + e.getMessage() );
        }

    	return items;
    }

    public synchronized void updateTimesBought( NeoItem item ) {
        try {
            Statement statement = connection.createStatement();

            String sql =
                "select id, times_bought " +
                "from ITEM " +
                "where " +
                    "name = '" + item.getName() + "'" ;

            ResultSet resultSet = statement.executeQuery( sql ); 
            int itemId = 0;
            int times_bought = 0;
            if ( resultSet.next() ) {
                itemId = resultSet.getInt( 1 );
                times_bought = resultSet.getInt( 2 );
            }
            resultSet.close();
            
            times_bought++;
            
            sql =
                "update ITEM " +
                "set times_bought = '" + times_bought + "' " +
                "where id = " + itemId;

            statement.executeUpdate( sql ); 

            statement.close();

        } catch( Exception e ) {
            log.e( "Error updating times bought for item in database: " + e.getMessage() );
        }
    }

    public int getBuyListLength() {
        int length = 0;

        try {
            String sql = "select count(*) from buylist";
           
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( sql ); 

            if ( resultSet.next() ) {
                length = resultSet.getInt( 1 );
            }

            resultSet.close();
            statement.close();
            
        } catch( Exception e ) {
            log.e( "Error getting the buylist length from database: " + e.getMessage() );
        }
        
        return length;
    }

}
