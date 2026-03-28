package neopetsmoneymaker;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NeoConfigurator {

    private List<NeoAccount>        accounts    = new ArrayList<NeoAccount>();
    private List<NeoShop>           shops       = new ArrayList<NeoShop>();
    private List<NeoSetting>        settings    = new ArrayList<NeoSetting>();
    
    private NeoLogger               log;

    public NeoConfigurator() {

    	log = new NeoLogger();
    	
        log.i( "Initializing configuration file." );

        // First, set all the settings
        settings.add( new NeoSetting( "buymarge", "10" ) );
        settings.add( new NeoSetting( "profitmarge", "10" ) );
        settings.add( new NeoSetting( "marketshare", "70" ) );
        settings.add( new NeoSetting( "minimalprofit", "0" ) );
        settings.add( new NeoSetting( "shopwait", "5" ) );
        settings.add( new NeoSetting( "marketwait", "20" ) );
        settings.add( new NeoSetting( "buywait", "1" ) );
        settings.add( new NeoSetting( "buywaitincrease", "1" ) );
        settings.add( new NeoSetting( "shopbanwait", "3600" ) );
        settings.add( new NeoSetting( "marketbanwait", "3600" ) );
        settings.add( new NeoSetting( "shopwaitincrease", "1" ) );
        settings.add( new NeoSetting( "marketwaitincrease", "1" ) );
        settings.add( new NeoSetting( "hagglingpercentage", "0" ) );
        settings.add( new NeoSetting( "hagglingpercentageincrease", "1" ) );
        settings.add( new NeoSetting( "marketparts", "AN0,BO1,CP2,DQ3,ER4,FS5,GT6,HU7,IV8,JW9,KX_,LY,MZ" ) );
        
        // Secondly, set all the accounts
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd" );
        try {       
            accounts.add( new NeoAccount( "symolsymen75", "75symolsymen", sdf.parse( "1975-04-05" ) ) );
            accounts.add( new NeoAccount( "zumaroo", "zumaroo12", sdf.parse( "1959-06-14" ) ) );
            accounts.add( new NeoAccount( "grarrlermen", "12menmen", sdf.parse( "1988-10-12" ) ) );
            accounts.add( new NeoAccount( "jettyjetsammy", "sammy64", sdf.parse( "1964-02-20" ) ) );
            accounts.add( new NeoAccount( "ahbrahcahdahbrah", "joepie12", sdf.parse( "1958-09-03" ) ) );
            accounts.add( new NeoAccount( "breakiebreakie", "12iebreak", sdf.parse( "1976-03-01" ) ) );
            accounts.add( new NeoAccount( "crocodilemanny", "fearme54", sdf.parse( "1974-08-10" ) ) );
            accounts.add( new NeoAccount( "dongdingdangdung", "kraslot45", sdf.parse( "1960-10-10" ) ) );
            accounts.add( new NeoAccount( "ernieandbert", "donotguessme43", sdf.parse( "1996-10-05" ) ) );
            accounts.add( new NeoAccount( "hohohoho70", "itssanta89", sdf.parse( "1970-01-10" ) ) );
            accounts.add( new NeoAccount( "iaminvinceable", "youare12not", sdf.parse( "1994-11-04" ) ) );
            accounts.add( new NeoAccount( "kabongkabong", "doega12", sdf.parse( "1993-03-03" ) ) );
            accounts.add( new NeoAccount( "lantaarhoofd", "ikbengek12", sdf.parse( "1971-11-13" ) ) );
            
            //accounts.add( new NeoAccount( "de_winkelman", "winkelman12", sdf.parse( "1981-11-22" ) ) );
            //accounts.add( new NeoAccount( "toliveornottolive", "whoah56", sdf.parse( "1978-05-17" ) ) );
            //accounts.add( new NeoAccount( "mmmmeerca", "arcaarca95", sdf.parse( "1995-08-19" ) ) );
            //accounts.add( new NeoAccount( "slorgano", "59slorgano", sdf.parse( "1987-03-22" ) ) );
        } catch ( ParseException e ) {
            //
        }

        // Thirdly, set all the shops
        shops.add( new NeoShop( "Neopian Fresh Foods", "http://www.neopets.com/objects.phtml?type=shop&obj_type=1", false ) );
        shops.add( new NeoShop( "Kauvara''s Magic Shop", "http://www.neopets.com/objects.phtml?type=shop&obj_type=2", false ) );
        shops.add( new NeoShop( "Toy Shop", "http://www.neopets.com/objects.phtml?type=shop&obj_type=3", false ) );
        shops.add( new NeoShop( "Unis Clothing Shop", "http://www.neopets.com/objects.phtml?type=shop&obj_type=4", false ) );
        shops.add( new NeoShop( "Grooming Parlour", "http://www.neopets.com/objects.phtml?type=shop&obj_type=5", false ) );
        // no. 6 is nothing
        shops.add( new NeoShop( "Magical Bookshop", "http://www.neopets.com/objects.phtml?type=shop&obj_type=7", false ) );
        shops.add( new NeoShop( "Collectable Card Shop", "http://www.neopets.com/objects.phtml?type=shop&obj_type=8", false ) );
        shops.add( new NeoShop( "Battle Magic", "http://www.neopets.com/objects.phtml?type=shop&obj_type=9", false ) );
        shops.add( new NeoShop( "Defence Magic", "http://www.neopets.com/objects.phtml?type=shop&obj_type=10", false ) );
        // no. 11 is nothing
        shops.add( new NeoShop( "Neopian Garden Centre", "http://www.neopets.com/objects.phtml?type=shop&obj_type=12", false ) );
        shops.add( new NeoShop( "Neopian Pharmacy", "http://www.neopets.com/objects.phtml?type=shop&obj_type=13", false ) );
        shops.add( new NeoShop( "Chocolate Factory", "http://www.neopets.com/objects.phtml?type=shop&obj_type=14", false ) );
        shops.add( new NeoShop( "The Bakery", "http://www.neopets.com/objects.phtml?type=shop&obj_type=15", false ) );
        shops.add( new NeoShop( "Neopian Health Foods", "http://www.neopets.com/objects.phtml?type=shop&obj_type=16", false ) );
        shops.add( new NeoShop( "Neopian Gift Shop", "http://www.neopets.com/objects.phtml?type=shop&obj_type=17", false ) );
        shops.add( new NeoShop( "Smoothie Store", "http://www.neopets.com/objects.phtml?type=shop&obj_type=18", false ) );
        // no. 19 is nothing
        shops.add( new NeoShop( "Tropical Food Shop", "http://www.neopets.com/objects.phtml?type=shop&obj_type=20", false ) );
        shops.add( new NeoShop( "Tiki Tack", "http://www.neopets.com/objects.phtml?type=shop&obj_type=21", true ) );
        shops.add( new NeoShop( "Grundos Cafe", "http://www.neopets.com/objects.phtml?type=shop&obj_type=22", false ) );
        shops.add( new NeoShop( "Space Weaponry", "http://www.neopets.com/objects.phtml?type=shop&obj_type=23", false ) );
        shops.add( new NeoShop( "Space Armour", "http://www.neopets.com/objects.phtml?type=shop&obj_type=24", false ) );
        shops.add( new NeoShop( "The Neopian Petpet Shop", "http://www.neopets.com/objects.phtml?type=shop&obj_type=25", false ) );
        shops.add( new NeoShop( "The Robo-Petpet Shop", "http://www.neopets.com/objects.phtml?type=shop&obj_type=26", false ) );
        shops.add( new NeoShop( "The Rock Pool", "http://www.neopets.com/objects.phtml?type=shop&obj_type=27", false ) );
        // no. 28 is nothing
        // no. 29 is nothing
        shops.add( new NeoShop( "Spooky Food", "http://www.neopets.com/objects.phtml?type=shop&obj_type=30", false ) );
        shops.add( new NeoShop( "Spooky Petpets", "http://www.neopets.com/objects.phtml?type=shop&obj_type=31", false ) );
        shops.add( new NeoShop( "The Coffee Cave", "http://www.neopets.com/objects.phtml?type=shop&obj_type=34", false ) );
        shops.add( new NeoShop( "Slushie Shop", "http://www.neopets.com/objects.phtml?type=shop&obj_type=35", false ) );
        shops.add( new NeoShop( "Ice Crystal Shop", "http://www.neopets.com/objects.phtml?type=shop&obj_type=36", false ) );
        shops.add( new NeoShop( "Super Happy Icy Fun Snow Shop", "http://www.neopets.com/objects.phtml?type=shop&obj_type=37", false ) );
        shops.add( new NeoShop( "Faerieland Bookshop", "http://www.neopets.com/objects.phtml?type=shop&obj_type=38", false ) );
        shops.add( new NeoShop( "Faerie Foods", "http://www.neopets.com/objects.phtml?type=shop&obj_type=39", false ) );
        shops.add( new NeoShop( "Faerieland Petpets", "http://www.neopets.com/objects.phtml?type=shop&obj_type=40", false ) );
        shops.add( new NeoShop( "Neopian Furniture", "http://www.neopets.com/objects.phtml?type=shop&obj_type=41", false ) );
        shops.add( new NeoShop( "Tyrannian Foods", "http://www.neopets.com/objects.phtml?type=shop&obj_type=42", false ) );
        shops.add( new NeoShop( "Tyrannian Furniture", "http://www.neopets.com/objects.phtml?type=shop&obj_type=43", false ) );
        shops.add( new NeoShop( "Tyrannian Petpets", "http://www.neopets.com/objects.phtml?type=shop&obj_type=44", false ) );
        shops.add( new NeoShop( "Tyrannian Weaponry", "http://www.neopets.com/objects.phtml?type=shop&obj_type=45", false ) );
        shops.add( new NeoShop( "Hubert''s Hot Dogs", "http://www.neopets.com/objects.phtml?type=shop&obj_type=46", false ) );
        shops.add( new NeoShop( "Pizzaroo", "http://www.neopets.com/objects.phtml?type=shop&obj_type=47", false ) );
        shops.add( new NeoShop( "Usukiland", "http://www.neopets.com/objects.phtml?type=shop&obj_type=48", false ) );
        shops.add( new NeoShop( "Lost Desert Foods", "http://www.neopets.com/objects.phtml?type=shop&obj_type=49", false ) );
        shops.add( new NeoShop( "Peopatra''s Petpets", "http://www.neopets.com/objects.phtml?type=shop&obj_type=50", false ) );
        shops.add( new NeoShop( "Sutek''s Scrolls", "http://www.neopets.com/objects.phtml?type=shop&obj_type=51", false ) );
        // no. 52 is nothing
        shops.add( new NeoShop( "Neopian School Supplies", "http://www.neopets.com/objects.phtml?type=shop&obj_type=53", false ) );
        shops.add( new NeoShop( "Sakhmet Battle Supplies", "http://www.neopets.com/objects.phtml?type=shop&obj_type=54", false ) );
        shops.add( new NeoShop( "Osiri''s Pottery", "http://www.neopets.com/objects.phtml?type=shop&obj_type=55", false ) );
        shops.add( new NeoShop( "Merifoods", "http://www.neopets.com/objects.phtml?type=shop&obj_type=56", false ) );
        shops.add( new NeoShop( "Ye Olde Petpets", "http://www.neopets.com/objects.phtml?type=shop&obj_type=57", false ) );
        shops.add( new NeoShop( "Neopian Post Office", "http://www.neopets.com/objects.phtml?type=shop&obj_type=58", false ) );
        shops.add( new NeoShop( "Haunted Weaponry", "http://www.neopets.com/objects.phtml?type=shop&obj_type=59", false ) );
        shops.add( new NeoShop( "Spooky Furniture", "http://www.neopets.com/objects.phtml?type=shop&obj_type=60", false ) );
        shops.add( new NeoShop( "Wintery Petpets", "http://www.neopets.com/objects.phtml?type=shop&obj_type=61", false ) );
        shops.add( new NeoShop( "Jelly Foods", "http://www.neopets.com/objects.phtml?type=shop&obj_type=62", false ) );
        shops.add( new NeoShop( "Refreshments", "http://www.neopets.com/objects.phtml?type=shop&obj_type=63", false ) );
        // no. 64 is nothing
        // no. 65 is nothing        
        shops.add( new NeoShop( "Kiko Lake Treats", "http://www.neopets.com/objects.phtml?type=shop&obj_type=66", false ) );
        shops.add( new NeoShop( "Kiko Lake Carpentry", "http://www.neopets.com/objects.phtml?type=shop&obj_type=67", false ) );
        shops.add( new NeoShop( "Collectable Coins", "http://www.neopets.com/objects.phtml?type=shop&obj_type=68", false ) );
        shops.add( new NeoShop( "Petpet Supplies", "http://www.neopets.com/objects.phtml?type=shop&obj_type=69", false ) );
        shops.add( new NeoShop( "Booktastic Books", "http://www.neopets.com/objects.phtml?type=shop&obj_type=70", false ) );
        shops.add( new NeoShop( "Kreludan Homes", "http://www.neopets.com/objects.phtml?type=shop&obj_type=71", false ) );
        shops.add( new NeoShop( "Cafe Kreludor", "http://www.neopets.com/objects.phtml?type=shop&obj_type=72", false ) );
        shops.add( new NeoShop( "Kayla''s Potion Shop", "http://www.neopets.com/objects.phtml?type=shop&obj_type=73", false ) );
        shops.add( new NeoShop( "Darigan Toys", "http://www.neopets.com/objects.phtml?type=shop&obj_type=74", false ) );
        shops.add( new NeoShop( "Faerie Furniture", "http://www.neopets.com/objects.phtml?type=shop&obj_type=75", false ) );
        shops.add( new NeoShop( "Roo Island Souvenirs", "http://www.neopets.com/objects.phtml?type=shop&obj_type=76", false ) );
        shops.add( new NeoShop( "Brightvale Books", "http://www.neopets.com/objects.phtml?type=shop&obj_type=77", false ) );
        shops.add( new NeoShop( "The Scrollery", "http://www.neopets.com/objects.phtml?type=shop&obj_type=78", false ) );
        shops.add( new NeoShop( "Brightvale Glaziers", "http://www.neopets.com/objects.phtml?type=shop&obj_type=79", false ) );
        shops.add( new NeoShop( "Brightvale Armoury", "http://www.neopets.com/objects.phtml?type=shop&obj_type=80", false ) );
        shops.add( new NeoShop( "Brightvale Fruits", "http://www.neopets.com/objects.phtml?type=shop&obj_type=81", false ) );
        shops.add( new NeoShop( "Brightvale Motery", "http://www.neopets.com/objects.phtml?type=shop&obj_type=82", false ) );
        shops.add( new NeoShop( "Royal Potionery", "http://www.neopets.com/objects.phtml?type=shop&obj_type=83", false ) );
        shops.add( new NeoShop( "Neopian Music Shop", "http://www.neopets.com/objects.phtml?type=shop&obj_type=84", false ) );
        shops.add( new NeoShop( "Lost Desert Medicine", "http://www.neopets.com/objects.phtml?type=shop&obj_type=85", false ) );
        shops.add( new NeoShop( "Collectable Sea Shells", "http://www.neopets.com/objects.phtml?type=shop&obj_type=86", false ) );
        shops.add( new NeoShop( "Maractite Marvels", "http://www.neopets.com/objects.phtml?type=shop&obj_type=87", false ) );
        shops.add( new NeoShop( "Maraquan Petpets", "http://www.neopets.com/objects.phtml?type=shop&obj_type=88", false ) );
        shops.add( new NeoShop( "Geraptiku Petpets", "http://www.neopets.com/objects.phtml?type=shop&obj_type=89", false ) );
        shops.add( new NeoShop( "Qasalan Delights", "http://www.neopets.com/objects.phtml?type=shop&obj_type=90", false ) );
        shops.add( new NeoShop( "Desert Arms", "http://www.neopets.com/objects.phtml?type=shop&obj_type=91", false ) );
        shops.add( new NeoShop( "Words of Antiquity", "http://www.neopets.com/objects.phtml?type=shop&obj_type=92", false ) );
        shops.add( new NeoShop( "Faerie Weapon Shop", "http://www.neopets.com/objects.phtml?type=shop&obj_type=93", false ) );
        shops.add( new NeoShop( "Illustrious Armoury", "http://www.neopets.com/objects.phtml?type=shop&obj_type=94", false ) );
        shops.add( new NeoShop( "Exquisite Ambrosia", "http://www.neopets.com/objects.phtml?type=shop&obj_type=95", false ) );
        shops.add( new NeoShop( "Magical Marvels", "http://www.neopets.com/objects.phtml?type=shop&obj_type=96", false ) );
        shops.add( new NeoShop( "Legendary Petpets", "http://www.neopets.com/objects.phtml?type=shop&obj_type=97", false ) );
        shops.add( new NeoShop( "Plushie Palace", "http://www.neopets.com/objects.phtml?type=shop&obj_type=98", false ) );
        shops.add( new NeoShop( "Wonderous Weaponry", "http://www.neopets.com/objects.phtml?type=shop&obj_type=100", false ) );
        shops.add( new NeoShop( "Exotic Foods", "http://www.neopets.com/objects.phtml?type=shop&obj_type=101", false ) );
        shops.add( new NeoShop( "Remarkable Restoratives", "http://www.neopets.com/objects.phtml?type=shop&obj_type=102", false ) );
        shops.add( new NeoShop( "Fanciful Fauna", "http://www.neopets.com/objects.phtml?type=shop&obj_type=103", false ) );
        shops.add( new NeoShop( "Chesterdrawers'' Antiques", "http://www.neopets.com/objects.phtml?type=shop&obj_type=104", false ) );
        shops.add( new NeoShop( "The Crumpetmonger", "http://www.neopets.com/objects.phtml?type=shop&obj_type=105", false ) );
        shops.add( new NeoShop( "Neovian Printing Press", "http://www.neopets.com/objects.phtml?type=shop&obj_type=106", false ) );
        shops.add( new NeoShop( "Prigpants & Swolthy, Tailors", "http://www.neopets.com/objects.phtml?type=shop&obj_type=107", false ) );
        shops.add( new NeoShop( "Mystical Surroundings", "http://www.neopets.com/objects.phtml?type=shop&obj_type=108", false ) );
        // no. 109 is nothing 
        shops.add( new NeoShop( "Lampwyck''s Lights Fantastic", "http://www.neopets.com/objects.phtml?type=shop&obj_type=110", false ) );
        shops.add( new NeoShop( "Cog''s Togs", "http://www.neopets.com/objects.phtml?type=shop&obj_type=111", false ) );
        shops.add( new NeoShop( "Molten Morsels", "http://www.neopets.com/objects.phtml?type=shop&obj_type=112", false ) );
        shops.add( new NeoShop( "Moltaran Petpets", "http://www.neopets.com/objects.phtml?type=shop&obj_type=113", false ) );
        shops.add( new NeoShop( "Moltaran Books", "http://www.neopets.com/objects.phtml?type=shop&obj_type=114", false ) );
        // no. 115 is nothing        
        shops.add( new NeoShop( "Springy Things", "http://www.neopets.com/objects.phtml?type=shop&obj_type=116", false ) );
               
    }

    public List<NeoSetting> getSettings() {
        return settings;
    }

    public List<NeoAccount> getAccounts() {
        return accounts;
    }

    public List<NeoShop> getShops() {
        return shops;
    }

}

