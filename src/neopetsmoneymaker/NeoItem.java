package neopetsmoneymaker;

import java.sql.Timestamp;

public class NeoItem {
    private String      itemName        = null;
    private int         shopPrice       = 0;
    private int         sellPrice       = 0;
    private int         hagglingPrice   = 0;
    private int         quantity        = 1;
    private String      haggleLink      = null;
    private String      shopName        = "";
    private String      owner           = null;
    private int         marketPrice     = 0;
    private String      shopLink        = null;
    private Timestamp   foundInShop     = null;
    
    public NeoItem( String item, int shopPrice, int quantity, String shopName, String haggleLink, String shopLink ) {
        this.itemName       = item;
        this.shopPrice      = shopPrice;
        this.quantity       = quantity;
        this.shopName       = shopName;
        this.haggleLink     = haggleLink;
        this.shopLink       = shopLink;
    }

    public NeoItem( String item, int shopPrice, int quantity, String haggleLink, String shopLink, Timestamp foundInShop ) {
        this.itemName       = item;
        this.shopPrice      = shopPrice;
        this.quantity       = quantity;
        this.haggleLink     = haggleLink;
        this.shopLink       = shopLink;
        this.foundInShop    = foundInShop;
    }

    public NeoItem( String name, String owner, int quantity, int marketPrice ) {
        this.itemName = name;
        this.owner = owner;
        this.quantity = quantity;
        this.marketPrice = marketPrice;
    }

    public NeoItem( String name, int sellPrice ) {
        this.itemName = name;
        this.sellPrice = sellPrice;
    }

    public NeoItem( String name ) {
        this.itemName = name;
    }

    public void setQuantity( int quantity ) {
        this.quantity = quantity;
    }

    public void setSellPrice( int sellPrice ) {
        this.sellPrice = sellPrice;
    }
    
    public String getName() {
        return itemName;
    }

    public String getShopName() {
        return shopName;
    }

    public int getMarketPrice() {
        return marketPrice;
    }

    public String getOwner() {
        return owner;
    }

    public int getShopPrice() {
        return shopPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getHaggleLink() {
        return haggleLink;
    }

    public String getShopLink() {
        return shopLink;
    }

    public int getSellPrice() {
        return sellPrice;
    }

    public Timestamp getFoundDateTime() {
        return foundInShop;
    }

    public int getHagglingPrice() {
        return hagglingPrice;
    }

    public void setHagglingPrice( int hagglingPrice ) {
        this.hagglingPrice = hagglingPrice;
    }

}
