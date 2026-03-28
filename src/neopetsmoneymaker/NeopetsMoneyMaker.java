package neopetsmoneymaker;

import java.util.ArrayList;
import java.util.List;


public class NeopetsMoneyMaker {

    public static void main( String[] args ) {        
    
        NeopetsMoneyMaker neopetsMoneyMaker = new NeopetsMoneyMaker();        
    
    }
    
    public NeopetsMoneyMaker() {
    
        NeoTray neoTray = new NeoTray();
        
        NeoDatabaseManager neoDatabaseManager = new NeoDatabaseManager();
        List<NeoAccount> accounts = new ArrayList<NeoAccount>();
        accounts.addAll( neoDatabaseManager.getAccounts() );
        
        // NeoMainAccounts
        for( int x=0; x<accounts.size(); x++ ) {
            NeoMainAccountService neoMainAccountService = new NeoMainAccountService( accounts.get( x ) );
        } 
        
        // NeoMarketExplorers
        for( int x=0; x<accounts.size(); x++ ) {
            NeoMarketExplorer neoMarketExplorer = new NeoMarketExplorer( accounts.get( x ) );
        } 

        // NeoshopExplorers
        List<NeoShop> shops = new ArrayList<NeoShop>();
        shops.addAll( neoDatabaseManager.getShops() );
                
        for( int x=0; x<accounts.size(); x++ ) {
            int firstShop = (x) * ( shops.size() / accounts.size() );
            NeoShopExplorer neopetsShopExplorer = new NeoShopExplorer( shops, accounts.get( x ), firstShop, neoDatabaseManager );
        }         
        
    }

}