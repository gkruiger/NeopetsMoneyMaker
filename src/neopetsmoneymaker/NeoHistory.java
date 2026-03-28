package neopetsmoneymaker;

import java.util.Calendar;
import java.util.Date;

public class NeoHistory {

    private Date    date;
    private int     value;
    private String  type;
    
    public NeoHistory( String type, int value ) {
        this.type = type;
        this.value = value;
        Calendar today = Calendar.getInstance();
        date = today.getTime();
    }

    public NeoHistory( Date date, String type, int value ) {
        this.date = date;
        this.type = type;
        this.value = value;
    }
    
    public Date getDate() {
        return date;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getType() {
        return type;
    }
       
}
