package neopetsmoneymaker;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NeoTray {

    private NeoLogger log;
    private final String FILEPATH = "C:\\Users\\Gertjan & Chiela\\Documents\\NetBeansProjects\\NeopetsMoneyMaker\\files\\";
    private final String ICONFILE = "trayicon.png";
    
    
    public NeoTray() {

        log = new NeoLogger();
        
        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage( FILEPATH + ICONFILE );

        PopupMenu popup = new PopupMenu();

        ActionListener statisticsListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NeoGrapher neoGrapher = new NeoGrapher();
            }
        };

        MenuItem statisticsMenuItem = new MenuItem( "Show statistics" );
        statisticsMenuItem.addActionListener( statisticsListener );
        popup.add( statisticsMenuItem );

        ActionListener quitListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };

        MenuItem quitMenuItem = new MenuItem( "Quit" );
        quitMenuItem.addActionListener( quitListener );
        popup.add( quitMenuItem );
        
        TrayIcon trayIcon = new TrayIcon( image, "Tray Demo", popup );
        trayIcon.setImageAutoSize( true );
        trayIcon.setToolTip( "NeopetsMoneyMaker is running." );

        try {
            tray.add( trayIcon );
        } catch ( AWTException e ) {
            log.e( "Error setting up NeoTray, trayicon could not be added: " + e.getMessage() );
        }
    }
}
