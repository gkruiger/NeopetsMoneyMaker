package neopetsmoneymaker;

public class NeoLogger {

    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLACK = "\u001B[30m";
    
    private NeoDatabaseManager  neoDatabaseManager;
    private NeoAccount          neoAccount;
    
    
    public NeoLogger() {
        this( false ); 
    }
    
    public NeoLogger( Boolean createDatabaseManager ) {
        if ( createDatabaseManager ) {
            neoDatabaseManager = new NeoDatabaseManager();
        }
    }
    
    public void i( String message ) {
        if ( neoDatabaseManager != null ) {
            neoDatabaseManager.addLog( "Info", message );
        }
        System.out.println( ANSI_BLACK + "I: " + message );
    }

    public void w( String message ) {
        if ( neoDatabaseManager != null ) {
            neoDatabaseManager.addLog( "Warning", message );
        }
        System.out.println( ANSI_YELLOW + "W: " + message );
    }

    public void e( String message ) {
        if ( neoDatabaseManager != null ) {
            neoDatabaseManager.addLog( "Error", message );
        }
        System.out.println( ANSI_RED + "E: " + message );
    }	

}
