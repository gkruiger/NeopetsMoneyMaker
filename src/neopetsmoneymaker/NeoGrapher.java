package neopetsmoneymaker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GridLayout;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;


public class NeoGrapher {

    private NeoDatabaseManager  neoDatabaseManager;
    
    private final Color[]       colors = { 
                                    Color.BLACK,
                                    Color.RED,
                                    Color.BLUE,
                                    Color.ORANGE,
                                    Color.CYAN,
                                    Color.BLACK,
                                    Color.RED,
                                    Color.BLUE,
                                    Color.ORANGE,
                                    Color.CYAN
                                };
    
    private List<NeoAccount>    neoAccounts = new ArrayList<NeoAccount>();
    
    public NeoGrapher() {

        neoDatabaseManager = new NeoDatabaseManager();
        
        neoAccounts.addAll( neoDatabaseManager.getAccounts() );
        
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
        frame.setTitle( "Neopets Money Maker Graphs" );
        
        GridLayout myLayout = new GridLayout(2,2);  
        frame.setLayout(myLayout);  

        ChartPanel neopointsPanel = (ChartPanel) 
            createPanel( 
                "Neopoints", 
                "Date", 
                "Neopoints", 
                new String[] { 
                    "neopoints_cash", 
                    "neopoints_invested_in_shopspace", 
                    "neopoints_invested_in_shopitems", 
                    "neopoints_total", 
                    "neopoints_earned_today" 
                }          
            );
        frame.add( neopointsPanel );
        
        ChartPanel shopspacePanel = (ChartPanel) 
            createPanel( 
                "Shop space", 
                "Date", 
                "Space", 
                new String[] { 
                    "shopspace_total", 
                    "shopspace_free", 
                    "shopspace_occupied" 
                }
            );
        frame.add( shopspacePanel );

        ChartPanel itemsPanel = (ChartPanel) 
            createPanel( 
                "Items", 
                "Date", 
                "Items", 
                new String[] { 
                    "items_known", 
                    "items_marketresearched", 
                    "items_tradeable", 
                    "items_traded_today", 
                    "items_bought_unique"
                }
            );
        frame.add( itemsPanel );

        ChartPanel activityPanel = (ChartPanel) 
            createPanel( 
                "Activity", 
                "Date", 
                "Seconds", 
                new String[] { 
                    "activity_seconds"  
                } 
            );
        frame.add( activityPanel );

        frame.pack();
        frame.setExtendedState( JFrame.MAXIMIZED_BOTH);
        RefineryUtilities.centerFrameOnScreen( frame );
        frame.setVisible(true);
        
    }
    
    private JPanel createPanel( String title, String xaxis, String yaxis, String[] dataset ) {

        TimeSeriesCollection tsc = new TimeSeriesCollection();
        
        for ( String dataSetItem : dataset ) { 

            TimeSeries ts = new TimeSeries( dataSetItem );
            List<NeoHistory> neoHistoryNeopoints = new ArrayList<NeoHistory>();

            neoHistoryNeopoints.addAll( neoDatabaseManager.getHistoryAsSomOfAccounts( dataSetItem ) );
            
            for( NeoHistory neoHistory : neoHistoryNeopoints ) {
                ts.add( 
                    new Day( 
                        neoHistory.getDate()
                    ),
                    neoHistory.getValue()
                );
            }   

            tsc.addSeries( ts );            
            
        }

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            title,              // title
            xaxis,              // x-axis label
            yaxis,              // y-axis label
            tsc,                // data
            true,               // create legend?
            true,               // generate tooltips?
            false               // generate URLs?
        );

        chart.setBackgroundPaint(Color.white);
                
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
            renderer.setDrawSeriesLineAsPath(true);
            for ( int q=0; q<tsc.getSeriesCount(); q++ ) {
                renderer.setSeriesStroke( q, new BasicStroke( 2 ) );
                renderer.setSeriesPaint( q, colors[ q ] );
            }
        }

        DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("dd-MMM"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date minimumDate = null;
        try {
            minimumDate = sdf.parse("13/04/2013");
        } catch (ParseException ex) {
            Logger.getLogger(NeoGrapher.class.getName()).log(Level.SEVERE, null, ex);
        }
        dateAxis.setMinimumDate( minimumDate );
        
        // Only for the Activity graph, convert the seconds into days
        if ( title.equals( "Activity" ) ) {           
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            int maxValue = (int) rangeAxis.getUpperBound();
            if ( maxValue > 60 * 60 * 24 * 3 ) {
                rangeAxis.setLabel( "Days" );
                rangeAxis.setNumberFormatOverride( new NumberFormat() {
                    int divider = 60 * 60 * 24;
                    @Override
                    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) { return new StringBuffer( String.format("%d", (int) number / divider ) ); }
                    @Override
                    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) { return null; }
                    @Override
                    public Number parse(String source, ParsePosition parsePosition) { return null; }
                } );    
            } else if ( maxValue > 60 * 60 * 3 ) {
                rangeAxis.setLabel( "Hours" );
                rangeAxis.setNumberFormatOverride( new NumberFormat() {
                    int divider = 60 * 60;
                    @Override
                    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) { return new StringBuffer( String.format("%d", (int) number / divider ) ); }
                    @Override
                    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) { return null; }
                    @Override
                    public Number parse(String source, ParsePosition parsePosition) { return null; }
                } );    
            } else if ( maxValue > 60 * 3 ) {
                rangeAxis.setLabel( "Minutes" );
                rangeAxis.setNumberFormatOverride( new NumberFormat() {
                    int divider = 60;
                    @Override
                    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) { return new StringBuffer( String.format("%d", (int) number / divider ) ); }
                    @Override
                    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) { return null; }
                    @Override
                    public Number parse(String source, ParsePosition parsePosition) { return null; }
                } );    
            } else {
                rangeAxis.setLabel( "Seconds" );
                rangeAxis.setNumberFormatOverride( new NumberFormat() {
                    int divider = 1;
                    @Override
                    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) { return new StringBuffer( String.format("%d", (int) number / divider ) ); }
                    @Override
                    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) { return null; }
                    @Override
                    public Number parse(String source, ParsePosition parsePosition) { return null; }
                } );    
                
            }
        } else {
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setNumberFormatOverride( new NumberFormat() {
                @Override
                public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) { return new StringBuffer( String.format("%,d", (int) number ) ); }
                @Override
                public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) { return null; }
                @Override
                public Number parse(String source, ParsePosition parsePosition) { return null; }
            } );    
        }
        
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        return panel;
    }

}
