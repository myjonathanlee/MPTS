import java.io.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.awt.Graphics ;
import java.awt.geom.* ;
import java.sql.* ;
import javax.swing.* ;
import javax.swing.border.* ;
import javax.swing.filechooser.* ;
import javax.swing.filechooser.FileFilter ;
import java.text.SimpleDateFormat ;
import java.util.GregorianCalendar ;
import java.util.* ;

import com.jrefinery.data.BasicTimeSeries ;
import com.jrefinery.data.TimeSeriesCollection ;
import com.jrefinery.data.Day ;
import com.jrefinery.data.XYDataset ;
import com.jrefinery.data.IntervalXYDataset ;
import com.jrefinery.date.SerialDate ;

/**
 *
 * This class deals with the Intelligent Prediction System (IPS).  It is responsible
 * for creating of the IPS tab as well as handling events within the IPS domain.
 * In creating predictions, graphs and volatility analyses, there are other
 * classes associated with IPS.
 * @author      Jonathan K. W. Lee
 * @version     1.0 08-FEB-2003
 * @see         IPSGraph_PriceVolume
 * @see         IPSPrediction_RidgeRegression_Primary
 * @see         MPTSGlobalVariables
 *
 */
public class IPS implements ActionListener {
    // class variables
    private static ResultsModel model ;
    private static Connection   liveConnection ;
    private static Statement    stmt ;
    private static String       currentSQLQuery ;
    private static ResultSet    rs ;

    private static JPanel  currentPanel ;
    private static JPanel  embroideringBox ;
    private static JButton button ;

    private        double highLowLadderTempHigh ;
    private        double highLowLadderTempLow  ;
    private        double highLowLadderTempCurr ;

    private static JTextField graphStartDate = new JTextField() ;
    private static JTextField graphEndDate   = new JTextField() ;

    private static JTextField summaryIPSRecommendationST   = new JTextField() ;
    private static JTextField summaryIPSRecommendationLT   = new JTextField() ;
    private static JTextField summaryPredictionNxtBsnssDay = new JTextField() ;
    private static JTextField summaryPrediction1Week       = new JTextField() ;
    private static JTextField summaryPrediction1Month      = new JTextField() ;
    private static JTextField summaryPrediction3Months     = new JTextField() ;
    private static JTextField summaryPrediction6Months     = new JTextField() ;
    private static JTextField summaryPrediction12Months    = new JTextField() ;

    private static JComboBox  availablePrevDaysPrediction ;

    // a class which interacts with the Central Database
    private static JDBCEvents JDBCMPTSConnection = new JDBCEvents() ;

    // a class which holds all global variables
    private static MPTSGlobalVariables systemWindowGlobalVariables ;

    //***********************************//
    // constructor
    //***********************************//
    /**
     *
     * Initialises variables.  Note that IPS creates a new instance of MPTSGlobalVariables
     * @param systemWindowGlobalVariablesMain    system global variables
     *
     */
    IPS(MPTSGlobalVariables systemWindowGlobalVariablesMain) {
        systemWindowGlobalVariables = new MPTSGlobalVariables(systemWindowGlobalVariablesMain.getUserID(), systemWindowGlobalVariablesMain.getUserPassword()) ;

        summaryIPSRecommendationST.setEditable(false) ;
        summaryIPSRecommendationLT.setEditable(false) ;
        summaryPredictionNxtBsnssDay.setEditable(false) ;
        summaryPrediction1Week.setEditable(false) ;
        summaryPrediction1Month.setEditable(false) ;
        summaryPrediction3Months.setEditable(false) ;
        summaryPrediction6Months.setEditable(false) ;
        summaryPrediction12Months.setEditable(false) ;
    } // end constructor

    //***********************************//
    // createEntireTab function          //
    //***********************************//
    /**
     *
     * The function assigned to oversee the creation of the entire IPS tab
     * @param systemWindowGlobalVariables        System global variables
     * @return                                   The IPS tab panel
     *
     */
    public JPanel createEntireTabPanel(MPTSGlobalVariables systemWindowGlobalVariables) {
        JPanel entirePanel     = new JPanel(new BorderLayout()) ;
        JPanel functionalPanel = new JPanel(new BorderLayout()) ;

        // create the createHighLowLadderPanel panel and put it to the west of the entirePanel
        entirePanel.add(createHighLowLadderPanel("0.0", "0.0", "0.0"), BorderLayout.WEST) ;

        // create the createSecurityDetailsPanel panel and put it to the north of the functionalPanel
        functionalPanel.add(createSecurityDetailsPanel(systemWindowGlobalVariables), BorderLayout.NORTH) ;

        // create the createGraphPanel panel and put it to the center of the functionalPanel
        functionalPanel.add(createChartAndPredictionPanel(systemWindowGlobalVariables), BorderLayout.CENTER) ;

        // create the createSummaryPanel panel and put it to the south of the functionalPanel
        functionalPanel.add(createSummaryPanel(systemWindowGlobalVariables), BorderLayout.SOUTH) ;

        // add the functionalPanel to the center of the entirePanel
        entirePanel.add(functionalPanel, BorderLayout.CENTER) ;

        return entirePanel ;
    } // end of createEntireTab function

    //***********************************//
    // createSecurityDetailsPanel function  //
    //***********************************//
    /**
     *
     * The function assigned to oversee the creation of the security details panel
     * @param systemWindowGlobalVariables        System global variables
     * @return                                   The security details panel
     *
     */
    private JPanel createSecurityDetailsPanel(MPTSGlobalVariables systemWindowGlobalVariablesMain) {
        currentPanel = new JPanel(new BorderLayout()) ;
        systemWindowGlobalVariables = new MPTSGlobalVariables(systemWindowGlobalVariablesMain.getUserID(), systemWindowGlobalVariablesMain.getUserPassword()) ;

        Box securityDetailsA = Box.createHorizontalBox() ;
        Box securityDetailsB = Box.createHorizontalBox() ;
        Box securityDetailsC = Box.createHorizontalBox() ;
        Box securityDetailsD = Box.createHorizontalBox() ;

        // add elements to the querying section of the screen
        securityDetailsA.add(Box.createHorizontalStrut(10)) ;
        securityDetailsA.add(new JLabel("Security:")) ;
        securityDetailsA.add(Box.createHorizontalStrut(10)) ;
        securityDetailsA.add(systemWindowGlobalVariables.getAvailableSecurities()) ;
        securityDetailsA.add(Box.createHorizontalStrut(50)) ;
        securityDetailsA.add(button = new JButton("Get Details")) ;
        button.addActionListener(this) ;
        securityDetailsA.add(Box.createHorizontalStrut(10)) ;

        securityDetailsB.add(Box.createHorizontalStrut(10)) ;
        securityDetailsB.add(new JLabel("Latest Price (p):")) ;
        securityDetailsB.add(Box.createHorizontalStrut(10)) ;
        securityDetailsB.add(systemWindowGlobalVariables.getLatestPrice()) ;
        securityDetailsB.add(Box.createHorizontalStrut(30)) ;
        securityDetailsB.add(new JLabel("EPIC Code:")) ;
        securityDetailsB.add(Box.createHorizontalStrut(10)) ;
        securityDetailsB.add(systemWindowGlobalVariables.getEPICCode()) ;
        securityDetailsB.add(Box.createHorizontalStrut(30)) ;
        securityDetailsB.add(new JLabel("ISIN Code:")) ;
        securityDetailsB.add(Box.createHorizontalStrut(10)) ;
        securityDetailsB.add(systemWindowGlobalVariables.getISINCode()) ;
        securityDetailsB.add(Box.createHorizontalStrut(10)) ;

        securityDetailsC.add(Box.createHorizontalStrut(10)) ;
        securityDetailsC.add(new JLabel("Sector:")) ;
        securityDetailsC.add(Box.createHorizontalStrut(10)) ;
        securityDetailsC.add(systemWindowGlobalVariables.getSector()) ;
        securityDetailsC.add(Box.createHorizontalStrut(30)) ;
        securityDetailsC.add(new JLabel("Originating Country:")) ;
        securityDetailsC.add(Box.createHorizontalStrut(10)) ;
        securityDetailsC.add(systemWindowGlobalVariables.getOriginatingCountry()) ;
        securityDetailsC.add(Box.createHorizontalStrut(10)) ;

        securityDetailsD.add(Box.createHorizontalStrut(10)) ;
        securityDetailsD.add(new JLabel("52 week high (p):")) ;
        securityDetailsD.add(Box.createHorizontalStrut(10)) ;
        securityDetailsD.add(systemWindowGlobalVariables.getHigh52Week()) ;
        securityDetailsD.add(Box.createHorizontalStrut(30)) ;
        securityDetailsD.add(new JLabel("52 week low (p):")) ;
        securityDetailsD.add(Box.createHorizontalStrut(10)) ;
        securityDetailsD.add(systemWindowGlobalVariables.getLow52Week()) ;
        securityDetailsD.add(Box.createHorizontalStrut(30)) ;
        securityDetailsD.add(new JLabel("Market Capitalisation (£m):")) ;
        securityDetailsD.add(Box.createHorizontalStrut(10)) ;
        securityDetailsD.add(systemWindowGlobalVariables.getMarketCapitalisation()) ;
        securityDetailsD.add(Box.createHorizontalStrut(10)) ;

        // a box to hold all querying boxes
        Box securityDetailsPanel = Box.createVerticalBox() ;
        securityDetailsPanel.add(Box.createVerticalStrut(15)) ;
        securityDetailsPanel.add(securityDetailsA) ;
        securityDetailsPanel.add(Box.createVerticalStrut(15)) ;
        securityDetailsPanel.add(securityDetailsB) ;
        securityDetailsPanel.add(Box.createVerticalStrut(15)) ;
        securityDetailsPanel.add(securityDetailsC) ;
        securityDetailsPanel.add(Box.createVerticalStrut(15)) ;
        securityDetailsPanel.add(securityDetailsD) ;
        securityDetailsPanel.add(Box.createVerticalStrut(15)) ;

        embroideringBox = new JPanel(new BorderLayout()) ;
        embroideringBox.setBorder(new TitledBorder(new EtchedBorder(), "Security Details")) ;
        embroideringBox.add(securityDetailsPanel, BorderLayout.CENTER) ;

        currentPanel.add(embroideringBox, BorderLayout.CENTER) ;
        return currentPanel ;
    } // end of createSecurityDetailsPanel function

    //***********************************//
    // createSummaryPanel function       //
    //***********************************//
    /**
     *
     * The function assigned to oversee the creation of the summary panel
     * @param systemWindowGlobalVariables        System global variables
     * @return                                   The summary panel
     *
     */
    private JPanel createSummaryPanel(MPTSGlobalVariables systemWindowGlobalVariables) {
        currentPanel = new JPanel(new BorderLayout()) ;
        Box summaryPanel = Box.createVerticalBox() ;

        // set summary section
        Box IPSSummaryA = Box.createHorizontalBox() ;

        IPSSummaryA.add(Box.createHorizontalStrut(10)) ;
        IPSSummaryA.add(button = new JButton("Save Security Details", new ImageIcon("images/save.gif"))) ;
        button.addActionListener(this) ;
        IPSSummaryA.add(Box.createHorizontalStrut(30)) ;
        IPSSummaryA.add(button = new JButton("Disclaimer")) ;
        button.addActionListener(this) ;
        IPSSummaryA.add(Box.createHorizontalStrut(10)) ;

        summaryPanel.add(Box.createVerticalStrut(15)) ;
        summaryPanel.add(IPSSummaryA) ;
        summaryPanel.add(Box.createVerticalStrut(15)) ;

        embroideringBox = new JPanel(new BorderLayout()) ;
        embroideringBox.setBorder( new TitledBorder(new EtchedBorder(), "Summary")) ;
        embroideringBox.add(summaryPanel, BorderLayout.CENTER) ;

        currentPanel.add(embroideringBox, BorderLayout.CENTER) ;
        return currentPanel ;
    } // end of createSummaryPanel function

    //***********************************//
    // createChartAndPredictionPanel function         //
    //***********************************//
    /**
     *
     * The function assigned to oversee the creation of the graph panel
     * @param systemWindowGlobalVariables        System global variables
     * @return                                   The graph panel
     *
     */
    private JPanel createChartAndPredictionPanel(MPTSGlobalVariables systemWindowGlobalVariables) {
        currentPanel = new JPanel(new BorderLayout()) ;
        JPanel graphPanel = new JPanel(new BorderLayout()) ;
        //BorderBox.createVerticalBox() ;

        // set chart section
        Box IPSChartLHS     = Box.createVerticalBox() ;
        Box IPSChartButtons = Box.createVerticalBox() ;

        Box IPSChartB = Box.createHorizontalBox() ;
        Box IPSChartC = Box.createHorizontalBox() ;
        Box IPSChartD = Box.createHorizontalBox() ;

        IPSChartB.add(Box.createHorizontalStrut(10)) ;
        IPSChartB.add(new JLabel("Start Date (DD/MM/YYYY):")) ;
        IPSChartB.add(Box.createHorizontalStrut(55)) ;
        IPSChartB.add(graphStartDate) ;
        IPSChartB.add(Box.createHorizontalStrut(10)) ;

        IPSChartC.add(Box.createHorizontalStrut(10)) ;
        IPSChartC.add(new JLabel("End Date (blank for today's date):")) ;
        IPSChartC.add(Box.createHorizontalStrut(10)) ;
        IPSChartC.add(graphEndDate) ;
        IPSChartC.add(Box.createHorizontalStrut(10)) ;

        String[] availablePrevDaysPredictionArray = { "5", "10", "25", "50", "100", "200", "365" } ;
        availablePrevDaysPrediction = new JComboBox(availablePrevDaysPredictionArray) ;
        availablePrevDaysPrediction.setName("availablePrevDaysPrediction") ;
        IPSChartD.add(Box.createHorizontalStrut(10)) ;
        IPSChartD.add(new JLabel("Number of days historical data for attribute prediction:")) ;
        IPSChartD.add(Box.createHorizontalStrut(10)) ;
        IPSChartD.add(availablePrevDaysPrediction) ;
        IPSChartD.add(Box.createHorizontalStrut(10)) ;

        IPSChartLHS.add(Box.createVerticalStrut(15)) ;
        IPSChartLHS.add(IPSChartB) ;
        IPSChartLHS.add(Box.createVerticalStrut(15)) ;
        IPSChartLHS.add(IPSChartC) ;
        IPSChartLHS.add(Box.createVerticalStrut(15)) ;
        IPSChartLHS.add(IPSChartD) ;
        IPSChartLHS.add(Box.createVerticalStrut(15)) ;
//        IPSChartLHS.add(Box.createGlue()) ;

        IPSChartButtons.add(Box.createVerticalStrut(10)) ;
        IPSChartButtons.add(button = new JButton("Show Graph")) ;
        button.addActionListener(this) ;
        IPSChartButtons.add(Box.createVerticalStrut(10)) ;
        IPSChartButtons.add(button = new JButton("Create Prediction (RR Primary form)")) ;
        button.addActionListener(this) ;
        IPSChartButtons.add(Box.createVerticalStrut(10)) ;

        Box IPSChartPredSummaryA   = Box.createHorizontalBox() ;
        Box IPSChartPredSummaryB   = Box.createHorizontalBox() ;
        Box IPSChartPredSummaryC   = Box.createHorizontalBox() ;
        Box IPSChartPredSummaryAll = Box.createVerticalBox() ;

        IPSChartPredSummaryA.add(Box.createHorizontalStrut(10)) ;
        IPSChartPredSummaryA.add(new JLabel("IPS RECOMMENDATION:")) ;
        IPSChartPredSummaryA.add(Box.createHorizontalStrut(5)) ;
        IPSChartPredSummaryA.add(new JLabel("Short-term:")) ;
        IPSChartPredSummaryA.add(Box.createHorizontalStrut(10)) ;
        IPSChartPredSummaryA.add(summaryIPSRecommendationST) ;
        IPSChartPredSummaryA.add(Box.createHorizontalStrut(20)) ;
        IPSChartPredSummaryA.add(new JLabel("Long-term:")) ;
        IPSChartPredSummaryA.add(Box.createHorizontalStrut(10)) ;
        IPSChartPredSummaryA.add(summaryIPSRecommendationLT) ;
        IPSChartPredSummaryA.add(Box.createHorizontalStrut(10)) ;

        IPSChartPredSummaryB.add(Box.createHorizontalStrut(10)) ;
        IPSChartPredSummaryB.add(new JLabel("PREDICTIONS (p):")) ;
        IPSChartPredSummaryB.add(Box.createHorizontalStrut(5)) ;
        IPSChartPredSummaryB.add(new JLabel("next business day:")) ;
        IPSChartPredSummaryB.add(Box.createHorizontalStrut(5)) ;
        IPSChartPredSummaryB.add(summaryPredictionNxtBsnssDay) ;
        IPSChartPredSummaryB.add(Box.createHorizontalStrut(10)) ;
        IPSChartPredSummaryB.add(new JLabel("1 week:")) ;
        IPSChartPredSummaryB.add(Box.createHorizontalStrut(5)) ;
        IPSChartPredSummaryB.add(summaryPrediction1Week) ;
        IPSChartPredSummaryB.add(Box.createHorizontalStrut(10)) ;
        IPSChartPredSummaryB.add(new JLabel("1 month:")) ;
        IPSChartPredSummaryB.add(Box.createHorizontalStrut(5)) ;
        IPSChartPredSummaryB.add(summaryPrediction1Month) ;
        IPSChartPredSummaryB.add(Box.createHorizontalStrut(10)) ;

        IPSChartPredSummaryC.add(Box.createHorizontalStrut(10)) ;
        IPSChartPredSummaryC.add(new JLabel("3 months:")) ;
        IPSChartPredSummaryC.add(Box.createHorizontalStrut(5)) ;
        IPSChartPredSummaryC.add(summaryPrediction3Months) ;
        IPSChartPredSummaryC.add(Box.createHorizontalStrut(10)) ;
        IPSChartPredSummaryC.add(new JLabel("6 months:")) ;
        IPSChartPredSummaryC.add(Box.createHorizontalStrut(5)) ;
        IPSChartPredSummaryC.add(summaryPrediction6Months) ;
        IPSChartPredSummaryC.add(Box.createHorizontalStrut(10)) ;
        IPSChartPredSummaryC.add(new JLabel("12 months:")) ;
        IPSChartPredSummaryC.add(Box.createHorizontalStrut(5)) ;
        IPSChartPredSummaryC.add(summaryPrediction12Months) ;
        IPSChartPredSummaryC.add(Box.createHorizontalStrut(10)) ;

        IPSChartPredSummaryAll.add(Box.createVerticalStrut(15)) ;
        IPSChartPredSummaryAll.add(IPSChartPredSummaryA) ;
        IPSChartPredSummaryAll.add(Box.createVerticalStrut(15)) ;
        IPSChartPredSummaryAll.add(IPSChartPredSummaryB) ;
        IPSChartPredSummaryAll.add(Box.createVerticalStrut(15)) ;
        IPSChartPredSummaryAll.add(IPSChartPredSummaryC) ;
        IPSChartPredSummaryAll.add(Box.createVerticalStrut(15)) ;

        graphPanel.add(IPSChartLHS, BorderLayout.CENTER) ;
        graphPanel.add(IPSChartButtons, BorderLayout.EAST) ;
        graphPanel.add(IPSChartPredSummaryAll, BorderLayout.SOUTH) ;

        embroideringBox = new JPanel(new BorderLayout()) ;
        embroideringBox.setBorder( new TitledBorder(new EtchedBorder(), "Chart & Prediction")) ;
        embroideringBox.add(graphPanel, BorderLayout.CENTER) ;

        currentPanel.add(embroideringBox, BorderLayout.CENTER) ;
        return currentPanel ;
    } // end of createChartAndPredictionPanel function

    //***********************************//
    // createPriceDataset function       //
    //***********************************//
    /**
     *
     * The function which prepares the price dataset to create the IPS chart
     * @param startDate           dataset start date
     * @param endDate             dataset end date
     * @param securitySelection   dataset security
     * @return                    The price dataset
     *
     */
    private XYDataset createPriceDataset(String startDate, String endDate, String securitySelection) {
        BasicTimeSeries seriesCOBPrice = new BasicTimeSeries("Price", Day.class);
        ResultsModel temporaryResultsModelTable = new ResultsModel() ;
        JTable securityHistoryArray ;

        openConnection() ;
        try {
            stmt = liveConnection.createStatement() ;
            currentSQLQuery = new String() ;
            currentSQLQuery = "SELECT Date, COBPrice FROM SecurityHistory WHERE ISIN = '" + securitySelection + "' AND Date BETWEEN #" + startDate + "# AND #" + endDate + "# ORDER BY Date ASC" ;
            System.out.print("\nThe query is:\n" + currentSQLQuery+ "\n");
            rs = stmt.executeQuery(currentSQLQuery) ;

            temporaryResultsModelTable.setResultSet(rs) ;
            securityHistoryArray = new JTable(temporaryResultsModelTable) ;

            if (securityHistoryArray.getRowCount() <= 1) { // more than 1 row is required otherwise the graph will not plot
                JOptionPane.showMessageDialog(null, "For the given date range and security that you entered, there is no data.\nPlease select another date range and/or security", "No Data", JOptionPane.INFORMATION_MESSAGE) ;
            }

            int    IPS_day   = 0 ;
            int    IPS_month = 0 ;
            int    IPS_year  = 0 ;
            double IPS_price = 0 ;

            for (int i=0; i<securityHistoryArray.getRowCount() ; i++) {
                IPS_day   = Integer.parseInt(securityHistoryArray.getValueAt(i,0).toString().substring(8,10)) ;
                IPS_month = Integer.parseInt(securityHistoryArray.getValueAt(i,0).toString().substring(5,7)) ;
                IPS_year  = Integer.parseInt(securityHistoryArray.getValueAt(i,0).toString().substring(0,4)) ;

                IPS_price = Double.parseDouble(securityHistoryArray.getValueAt(i,1).toString()) ; // for COB Price

                System.out.println(i + "\t" + IPS_day + "/" + IPS_month + "/" + IPS_year + ", " + IPS_price) ;
                seriesCOBPrice.add(new Day(IPS_day, IPS_month, IPS_year), IPS_price) ;
            }
        }
        catch(SQLException e) {
            System.out.print("SQL Exception occurred in createPriceDataset:\n " + e.getMessage() + "\n");
        }
        closeConnection() ;
        return new TimeSeriesCollection(seriesCOBPrice);
    } // end function createPriceDataset

    //***********************************//
    // createVolumeDataset function      //
    //***********************************//
    /**
     *
     * The function which prepares the volume dataset to create the IPS chart
     * @param startDate           dataset start date
     * @param endDate             dataset end date
     * @param securitySelection   dataset security
     * @return                    The volume dataset
     *
     */
    private IntervalXYDataset createVolumeDataset(String startDate, String endDate, String securitySelection) {
        BasicTimeSeries seriesVolume = new BasicTimeSeries("Volume", Day.class);
        ResultsModel temporaryResultsModelTable = new ResultsModel() ;
        JTable securityHistoryArray ;

        openConnection() ;
        try {
            stmt = liveConnection.createStatement() ;
            currentSQLQuery = new String() ;
            currentSQLQuery = "SELECT Date, Volume FROM SecurityHistory WHERE ISIN = '" + securitySelection + "' AND Date BETWEEN #" + startDate + "# AND #" + endDate + "# ORDER BY Date ASC" ;
            System.out.print("\nThe query is:\n" + currentSQLQuery+ "\n");
            rs = stmt.executeQuery(currentSQLQuery) ;

            temporaryResultsModelTable.setResultSet(rs) ;
            securityHistoryArray = new JTable(temporaryResultsModelTable) ;

            // blank securityHistoryArray check has already been performed in function createPriceDataset()

            int    IPS_day    = 0 ;
            int    IPS_month  = 0 ;
            int    IPS_year   = 0 ;
            double IPS_volume = 0 ;

            for (int i=0; i<securityHistoryArray.getRowCount() ; i++) {
                IPS_day    = Integer.parseInt(securityHistoryArray.getValueAt(i,0).toString().substring(8,10)) ;
                IPS_month  = Integer.parseInt(securityHistoryArray.getValueAt(i,0).toString().substring(5,7)) ;
                IPS_year   = Integer.parseInt(securityHistoryArray.getValueAt(i,0).toString().substring(0,4)) ;

                IPS_volume = Double.parseDouble(securityHistoryArray.getValueAt(i,1).toString()) ; // for COB Price

                System.out.println(i + "\t" + IPS_day + "/" + IPS_month + "/" + IPS_year + ", " + IPS_volume) ;
                seriesVolume.add(new Day(IPS_day, IPS_month, IPS_year), IPS_volume) ;
            }
        }
        catch(SQLException e) {
            System.out.print("SQL Exception occurred in createVolumeDataset:\n " + e.getMessage() + "\n");
        }
        closeConnection() ;
        return new TimeSeriesCollection(seriesVolume);
    } // end function createVolumeDataset

    //***********************************//
    // createHighLowLadderPanel function //
    //***********************************//
    /**
     *
     * The function which prepares and maintains the High-Low Ladder Panel
     * @param highVal        security 52 week high value
     * @param lowVal         security 52 week low value
     * @param currentVal     security current value
     * @return               The High-Low Ladder Panel
     *
     */
    private JPanel createHighLowLadderPanel(String highVal, String lowVal, String currentVal) {
        currentPanel = new JPanel(new BorderLayout()) ;
        JPanel ladderPanel = new JPanel(new BorderLayout()) ;

        try {
            highLowLadderTempHigh = Double.parseDouble(highVal) ;
            highLowLadderTempLow  = Double.parseDouble(lowVal) ;
            highLowLadderTempCurr = Double.parseDouble(currentVal) ;

            IPSLine createdIPSLine = new IPSLine(highLowLadderTempHigh, highLowLadderTempLow, highLowLadderTempCurr) ;
            createdIPSLine.repaint() ;

            JLabel widthPreserver = new JLabel(".                                  .") ; // this preserves the width of this panel
//            widthPreserver.setVisible(false) ;
            ladderPanel.add(widthPreserver, BorderLayout.NORTH) ;
            ladderPanel.add(createdIPSLine, BorderLayout.CENTER) ;
        }
        catch(NumberFormatException e) { highLowLadderTempHigh = highLowLadderTempLow = highLowLadderTempCurr = 0.0 ; }

        embroideringBox = new JPanel(new BorderLayout()) ;
        embroideringBox.setBorder(new TitledBorder(new EtchedBorder(), "High-Low Ladder")) ;
        embroideringBox.add(ladderPanel, BorderLayout.CENTER) ;

        currentPanel.add(embroideringBox, BorderLayout.CENTER) ;
        return currentPanel ;
    } // end of createHighLowLadder function

    private class IPSLine extends JPanel {
        private double currentValuePlacementLevel ;

        IPSLine(double highVal, double lowVal, double currentVal) {
            // integrity check
            if (lowVal > highVal || currentVal > highVal || currentVal < lowVal) {
                System.err.println("ERROR: In creating ladder, security values do not concur") ;
                return ;
            }
        }

        public void paint(Graphics g) {
//            System.out.println("paint called") ;
            Dimension d = getSize() ;
            int boxWidth = d.width - 20 ;
            int boxHeight = d.height - 30 ;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // draw the ladder
            g2.setColor(Color.RED) ;
            g2.draw(new Line2D.Double(30, 30, 30, boxHeight));
            g2.draw(new Line2D.Double(20, 30, 40, 30));
            g2.draw(new Line2D.Double(20, boxHeight, 40, boxHeight));

            g2.setColor(Color.BLACK) ;
            g2.drawString("High (p): " + String.valueOf(highLowLadderTempHigh), 5, 24) ;
            g2.drawString("Low (p): " + String.valueOf(highLowLadderTempLow), 5, boxHeight+14) ;

            // now set and draw the intersection of current price
            currentValuePlacementLevel = boxHeight - ((highLowLadderTempCurr / highLowLadderTempHigh) * (boxHeight-30)) ;
            g2.setColor(Color.BLUE) ;
            g2.drawString("Current (p): " + String.valueOf(highLowLadderTempCurr), 5, (int)currentValuePlacementLevel-4);
            g2.draw(new Line2D.Double(20, (int)currentValuePlacementLevel, 40, (int)currentValuePlacementLevel));
        }
    } // end of IPSLine class

    //***********************************//
    // whenSaveSecurityDetailsPressed function    //
    //***********************************//
    /**
     *
     * Exports security details to a text file
     * @param systemWindowGlobalVariables    system global variables
     *
     */
    private void whenSaveSecurityDetailsPressed(MPTSGlobalVariables systemWindowGlobalVariables) {

        // if no security is selected, display error information to user
        if (((String)systemWindowGlobalVariables.getAvailableSecurities().getSelectedItem()).equals("")) {
            JOptionPane.showMessageDialog(null, "Please select a security of which the details you wish to save", "Select a security", JOptionPane.INFORMATION_MESSAGE) ;
            return ;
        }
        // if you got here, a security is selected, so obtain latest details
        if ((systemWindowGlobalVariables.getISINCode().getText()).equals("")) {
            JDBCMPTSConnection.whenGetDetailsPressed(systemWindowGlobalVariables) ;
        }

        final JFileChooser fc = new JFileChooser();
        ExtensionFilter MPTSFileFilter ;

        MPTSFileFilter = new ExtensionFilter(".txt", "Text Files (*.txt)") ;
        fc.addChoosableFileFilter(MPTSFileFilter) ;
        fc.setFileFilter(MPTSFileFilter) ;

        int approveReturnValue          = fc.showSaveDialog(null) ;
        int confirmOverwriteReturnValue = JOptionPane.YES_OPTION ;

        if (approveReturnValue == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile() ;

            // check if extension has been added, if not, add it.
            if (file.getName().indexOf(".") == -1) { // if a dot doesn't exist, this means that an extension has not been added, therefore add one
                file.renameTo( new File(file.getParentFile(), file.getName() + ".txt")) ;
            }
//            System.out.println(file.getName()) ;

            if (file.exists()) { // file overwrite warning
//              confirmOverwriteReturnValue = JOptionPane.showInternalConfirmDialog(fc, "The file already exists.\nDo you want to replace the existing file?", "Export Data", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) ;
                confirmOverwriteReturnValue = JOptionPane.showConfirmDialog(null, "The file already exists.\nDo you want to replace the existing file?", "Export Data", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) ;
            }

            // if it is okay to overwrite, do the following, otherwise do nothing
            if (confirmOverwriteReturnValue == JOptionPane.YES_OPTION) {
                try {
                    systemWindowGlobalVariables.setStatusArea("Saving: " + file.getName()) ;

                    DataOutputStream savingFile = new DataOutputStream(new FileOutputStream(file)) ;

                    // the save time
                    GregorianCalendar currentCalendar = new GregorianCalendar() ;
                    String currentDate = String.valueOf(currentCalendar.get(Calendar.DATE)) + "/" + String.valueOf(currentCalendar.get(Calendar.MONTH)+1) + "/" + String.valueOf(currentCalendar.get(Calendar.YEAR)) ;
                    String currentTime = String.valueOf(currentCalendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(currentCalendar.get(Calendar.MINUTE)) + ":" + String.valueOf(currentCalendar.get(Calendar.SECOND)) ;

                    savingFile.writeBytes("Saved Security Details for \"" + (String)systemWindowGlobalVariables.getAvailableSecurities().getSelectedItem() + "\".\n") ;
                    savingFile.writeBytes("Save Date:   " + currentDate + " " + currentTime + "\n\n") ;

                    // save the security details
                    savingFile.writeBytes("-------------------------------------------------------------------------------\n") ;
                    savingFile.writeBytes("SECURITY DETAILS\n") ;
                    savingFile.writeBytes("-------------------------------------------------------------------------------\n") ;
                    savingFile.writeBytes("Security:                      " + (String)systemWindowGlobalVariables.getAvailableSecurities().getSelectedItem() + "\n") ;
                    savingFile.writeBytes("ISIN:                          " + systemWindowGlobalVariables.getISINCode().getText() + "\n") ;
                    savingFile.writeBytes("EPIC Code:                     " + systemWindowGlobalVariables.getEPICCode().getText() + "\n") ;
                    savingFile.writeBytes("Share Or Bond:                 " + systemWindowGlobalVariables.getShareOrBond().getText() + "\n") ;
                    savingFile.writeBytes("Sector:                        " + systemWindowGlobalVariables.getSector().getText() + "\n") ;
                    savingFile.writeBytes("Originating Country:           " + systemWindowGlobalVariables.getOriginatingCountry().getText() + "\n") ;
                    savingFile.writeBytes("Latest price (p):              " + systemWindowGlobalVariables.getLatestPrice().getText() + "\n") ;
                    savingFile.writeBytes("52 week high (p):              " + systemWindowGlobalVariables.getHigh52Week().getText() + "\n") ;
                    savingFile.writeBytes("52 week low (p):               " + systemWindowGlobalVariables.getLow52Week().getText() + "\n") ;
                    savingFile.writeBytes("Market Capitalisation (£m):    " + systemWindowGlobalVariables.getMarketCapitalisation().getText() + "\n") ;
                    savingFile.writeBytes("Corporate Website URL:         " + systemWindowGlobalVariables.getCompanyURL().getText() + "\n") ;
                    savingFile.writeBytes("-------------------------------------------------------------------------------\n") ;

                    // now save the prediction details and the given dates, etc
                    savingFile.writeBytes("\n") ;
                    savingFile.writeBytes("-------------------------------------------------------------------------------\n") ;
                    savingFile.writeBytes("PREDICTION DETAILS\n") ;
                    savingFile.writeBytes("-------------------------------------------------------------------------------\n") ;
                    savingFile.writeBytes("User Prediction Start Date:           " + graphStartDate.getText() + "\n") ;
                    savingFile.writeBytes("User Prediction End Date:             " + graphEndDate.getText() + "\n") ;
                    savingFile.writeBytes("No days historical data usage:        " + (String)availablePrevDaysPrediction.getSelectedItem() + "\n") ;
                    savingFile.writeBytes("-------------------------------------------------------------------------------\n") ;
                    savingFile.writeBytes("Security Recommendation (short-term): " + summaryIPSRecommendationST.getText() + "\n") ;
                    savingFile.writeBytes("Security Recommendation (long-term):  " + summaryIPSRecommendationLT.getText() + "\n") ;
                    savingFile.writeBytes("Prediction Next Business Day (p):     " + summaryPredictionNxtBsnssDay.getText() + "\n") ;
                    savingFile.writeBytes("Prediction 1 Week (p):                " + summaryPrediction1Week.getText() + "\n") ;
                    savingFile.writeBytes("Prediction 1 Month (p):               " + summaryPrediction1Month.getText() + "\n") ;
                    savingFile.writeBytes("Prediction 3 Months (p):              " + summaryPrediction3Months.getText() + "\n") ;
                    savingFile.writeBytes("Prediction 6 Months (p):              " + summaryPrediction6Months.getText() + "\n") ;
                    savingFile.writeBytes("Prediction 12 Months (p):             " + summaryPrediction12Months.getText() + "\n") ;
                    savingFile.writeBytes("-------------------------------------------------------------------------------\n") ;

                    savingFile.close() ;
                }
                catch(IOException e) {
                    JOptionPane.showMessageDialog(null, "There was an error in attempting to save the file.\nPlease retry.  If this error persists, please contact technical support", "Error saving file", JOptionPane.ERROR_MESSAGE) ;
                }
            }
        }
        else { systemWindowGlobalVariables.setStatusArea("Save command cancelled") ; }

    } // end of whenSaveSecurityDetailsPressed function

    //***********************************//
    // whenShowGraphPressed function     //
    //***********************************//
    /**
     *
     * Creates and shows the IPS graph
     *
     */
    private void whenShowGraphPressed() {
        // create datasets...

        // if no security is selected, display error information to user
        if (((String)systemWindowGlobalVariables.getAvailableSecurities().getSelectedItem()).equals("")) {
            JOptionPane.showMessageDialog(null, "Please select a security which you want to graph", "Select a security", JOptionPane.INFORMATION_MESSAGE) ;
            return ;
        }
        // if you got here, a security is selected, so obtain latest details
        if ((systemWindowGlobalVariables.getISINCode().getText()).equals("")) {
            JDBCMPTSConnection.whenGetDetailsPressed(systemWindowGlobalVariables) ;
        }

        // now do a check on the graph start date
        if (!isDateFormatValid(graphStartDate.getText())) { // if not valid
            JOptionPane.showMessageDialog(null, "Please enter a valid graphing start date in a valid format 'DD/MM/YYYY'", "Enter a valid Start Date", JOptionPane.INFORMATION_MESSAGE) ;
            return ;
        }

        // if graphEndDate is invalid, then take in todays date for this value
        if (!isDateFormatValid(graphEndDate.getText())) {
            GregorianCalendar currentCalendar = new GregorianCalendar() ;
            String currentDate = new String() ;

            if (currentCalendar.get(Calendar.DATE) < 10) { // padding required for "zero"
                currentDate += "0" + String.valueOf(currentCalendar.get(Calendar.DATE)) + "/" ;
            }
            else { // no padding required
                currentDate += String.valueOf(currentCalendar.get(Calendar.DATE)) + "/" ;
            }

            if ((currentCalendar.get(Calendar.MONTH)+1) < 10) { // padding required for "zero"
                currentDate += "0" + String.valueOf(currentCalendar.get(Calendar.MONTH)+1) + "/" ;
            }
            else { // no padding required
                currentDate += String.valueOf(currentCalendar.get(Calendar.MONTH)+1) + "/" ;
            }

            // add the remaining year value
            currentDate += String.valueOf(currentCalendar.get(Calendar.YEAR)) ;
            graphEndDate.setText(currentDate) ;
        }

        String securitySelection           = systemWindowGlobalVariables.getISINCode().getText() ;

        // for data queries across MS Access, convert the dates to US (ANSI-89) standard: MM/DD/YYYY
        String graphStartDateANSI89Format  = graphStartDate.getText().substring(3,6)  ; // month and a forward slash
               graphStartDateANSI89Format += graphStartDate.getText().substring(0,3)  ; // day and a forward slash
               graphStartDateANSI89Format += graphStartDate.getText().substring(6,10) ; // year

        String graphEndDateANSI89Format    = graphEndDate.getText().substring(3,6)    ; // month and a forward slash
               graphEndDateANSI89Format   += graphEndDate.getText().substring(0,3)    ; // day and a forward slash
               graphEndDateANSI89Format   += graphEndDate.getText().substring(6,10)   ; // year

        XYDataset         priceData  = createPriceDataset(graphStartDateANSI89Format, graphEndDateANSI89Format, securitySelection) ;
        IntervalXYDataset volumeData = createVolumeDataset(graphStartDateANSI89Format, graphEndDateANSI89Format, securitySelection) ;

        // execute the function which creates the graph and opens the thingy
        IPSGraph_PriceVolume combinedPVGraph = new IPSGraph_PriceVolume("Price/Volume Graph, ISIN:" + securitySelection + ", (" + graphStartDate.getText() + " - " + graphEndDate.getText() + ")" ,
                                                                        priceData,
                                                                        volumeData );
        JPanel graphPanel = combinedPVGraph.getChart() ;
        Box    graphInfo  = Box.createVerticalBox() ;
        Box    graphInfoA = Box.createHorizontalBox() ;
        Box    graphInfoB = Box.createHorizontalBox() ;

        graphInfoA.add(new JLabel("EXPORTING: To save or print the chart, please right-click on the graph area.")) ;
        graphInfoB.add(new JLabel("In doing this, personalised options and properties can be specified")) ;

        graphInfo.add(Box.createVerticalStrut(5)) ;
        graphInfo.add(graphInfoA) ;
        graphInfo.add(graphInfoB) ;
        graphInfo.add(Box.createVerticalStrut(5)) ;

        JPanel allGraphInfo = new JPanel(new BorderLayout()) ;
        allGraphInfo.add(graphInfo, BorderLayout.NORTH) ;
        allGraphInfo.add(graphPanel, BorderLayout.CENTER) ;

        // show panel consisting of information, a graphPanel and OK button
        JOptionPane.showMessageDialog(null, allGraphInfo, "MPTS Chart", JOptionPane.INFORMATION_MESSAGE) ;
    } // end of whenShowGraphPressed function

    //***********************************//
    // whenPredictionRRPrimaryPressed function  //
    //***********************************//
    /**
     *
     * Creates data to enter into Ridge Regression in order to predict the share
     * price.  The data is processed and results are shown in a popup dialog box.
     *
     */
    private void whenPredictionRRPrimaryPressed() {
        ResultsModel temporaryResultsModelTable = new ResultsModel() ;
        JTable       securityHistoryArray ;

        // if no security is selected, display error information to user
        if (((String)systemWindowGlobalVariables.getAvailableSecurities().getSelectedItem()).equals("")) {
            JOptionPane.showMessageDialog(null, "Please select a security which you want to predict", "Select a security", JOptionPane.INFORMATION_MESSAGE) ;
            return ;
        }
        // if you got here, a security is selected, so obtain latest details
        if ((systemWindowGlobalVariables.getISINCode().getText()).equals("")) {
            JDBCMPTSConnection.whenGetDetailsPressed(systemWindowGlobalVariables) ;
        }

        // now do a check on the graph start date
        if (!isDateFormatValid(graphStartDate.getText())) {
            JOptionPane.showMessageDialog(null, "Please enter a valid prediction algorithm start date in a valid format 'DD/MM/YYYY'", "Enter a valid Start Date", JOptionPane.INFORMATION_MESSAGE) ;
            return ;
        }

        // if graphEndDate is invalid, then take in todays date for this value
        if (!isDateFormatValid(graphEndDate.getText())) {
            GregorianCalendar currentCalendar = new GregorianCalendar() ;
            String currentDate = new String() ;

            if (currentCalendar.get(Calendar.DATE) < 10) { // padding required for "zero"
                currentDate += "0" + String.valueOf(currentCalendar.get(Calendar.DATE)) + "/" ;
            }
            else { // no padding required
                currentDate += String.valueOf(currentCalendar.get(Calendar.DATE)) + "/" ;
            }

            if ((currentCalendar.get(Calendar.MONTH)+1) < 10) { // padding required for "zero"
                currentDate += "0" + String.valueOf(currentCalendar.get(Calendar.MONTH)+1) + "/" ;
            }
            else { // no padding required
                currentDate += String.valueOf(currentCalendar.get(Calendar.MONTH)+1) + "/" ;
            }

            // add the remaining year value
            currentDate += String.valueOf(currentCalendar.get(Calendar.YEAR)) ;
            graphEndDate.setText(currentDate) ;
        }

        String securitySelection     = systemWindowGlobalVariables.getISINCode().getText() ;
        String startDate             = graphStartDate.getText() ;
        String endDate               = graphEndDate.getText() ;
        int    numPrevDaysPrediction = Integer.parseInt((String)availablePrevDaysPrediction.getSelectedItem()) ;

        // access database and get values specified between the dates

        double X_Inputs_Array[][] = new double[3][0] ;
        openConnection() ;
        try {
            stmt = liveConnection.createStatement() ;
            currentSQLQuery = new String() ;
            currentSQLQuery = "SELECT DayHighPrice, DayLowPrice, Volume FROM SecurityHistory WHERE ISIN = '" + securitySelection + "' AND Date BETWEEN #" + startDate + "# AND #" + endDate + "#" ;
//            System.out.print("\nThe query is:\n" + currentSQLQuery+ "\n");
            rs = stmt.executeQuery(currentSQLQuery) ;

            temporaryResultsModelTable.setResultSet(rs) ;
            securityHistoryArray = new JTable(temporaryResultsModelTable) ;

            if (securityHistoryArray.getRowCount() <= 1) { // more than 1 row is required otherwise the matrix will be singular giving rise to an inversion error
                JOptionPane.showMessageDialog(null, "For the given date range and security that you entered, there is no data.\nPlease select another date range and/or security", "No Data", JOptionPane.INFORMATION_MESSAGE) ;
                return ; // CHANGE THIS TO A BREAK OR SOMETHING TO QUIT THIS FUNCTION
            }

//            System.out.println("rows: " + securityHistoryArray.getRowCount()) ;
//            System.out.println("cols: " + securityHistoryArray.getColumnCount()) ;

            // set the arrays to the required sizes
            X_Inputs_Array[0] = new double[securityHistoryArray.getRowCount()] ;
            X_Inputs_Array[1] = new double[securityHistoryArray.getRowCount()] ;
            X_Inputs_Array[2] = new double[securityHistoryArray.getRowCount()] ;

//            System.out.println(X_Inputs_Array.length) ;
//            System.out.println(X_Inputs_Array[0].length) ;

            for (int i=0; i<securityHistoryArray.getRowCount() ; i++) {
                for (int j=0; j<securityHistoryArray.getColumnCount() ; j++) {
//                    System.out.print(i + ", ") ;
                    X_Inputs_Array[j][i] = Double.parseDouble(securityHistoryArray.getValueAt(i,j).toString()) ;
                }
            }
        }
        catch(SQLException e) {
            System.out.print("SQL Exception occurred in whenPredictionRRPressed:\n " + e.getMessage() + "\n");
        }
        closeConnection() ;


        double Y_Labels_Array[][] = new double[1][0] ;
        openConnection() ;
        try {
            stmt = liveConnection.createStatement() ;
            currentSQLQuery = new String() ;
            currentSQLQuery = "SELECT COBPrice FROM SecurityHistory WHERE ISIN = '" + securitySelection + "' AND Date BETWEEN #" + startDate + "# AND #" + endDate + "#" ;
//            System.out.print("\nThe query is:\n" + currentSQLQuery+ "\n");
            rs = stmt.executeQuery(currentSQLQuery) ;

            temporaryResultsModelTable.setResultSet(rs) ;
            securityHistoryArray = new JTable(temporaryResultsModelTable) ;

//            System.out.println("rows: " + securityHistoryArray.getRowCount()) ;
//            System.out.println("cols: " + securityHistoryArray.getColumnCount()) ;

            // set the array to the required size
            Y_Labels_Array[0] = new double[securityHistoryArray.getRowCount()] ; // length obtained from resultsset

//            System.out.println(Y_Labels_Array.length) ;
//            System.out.println(Y_Labels_Array[0].length) ;

            for (int i=0; i<securityHistoryArray.getRowCount() ; i++) {
//                System.out.print(i + ", ") ;
                Y_Labels_Array[0][i] = Double.parseDouble(securityHistoryArray.getValueAt(i,0).toString()) ;
            }
        }
        catch(SQLException e) {
            System.out.print("SQL Exception occurred in whenPredictionRRPressed:\n " + e.getMessage() + "\n");
        }
        closeConnection() ;

        // create and run the neural network
        IPSPrediction_RidgeRegression_Primary IPSridgeRegression ;
        IPSridgeRegression = new IPSPrediction_RidgeRegression_Primary(X_Inputs_Array, Y_Labels_Array, numPrevDaysPrediction) ;

        // now obtain the results and display to user
        double value_SharePricePredictionNxtBsnssDay = IPSridgeRegression.getSharePricePredictionNxtBsnssDay() ;
        double value_SharePricePrediction1Week       = IPSridgeRegression.getSharePricePrediction1Week() ;
        double value_SharePricePrediction1Month      = IPSridgeRegression.getSharePricePrediction1Month() ;
        double value_SharePricePrediction3Months     = IPSridgeRegression.getSharePricePrediction3Months() ;
        double value_SharePricePrediction6Months     = IPSridgeRegression.getSharePricePrediction6Months() ;
        double value_SharePricePrediction12Months    = IPSridgeRegression.getSharePricePrediction12Months() ;
        double currentPrice                          = Double.parseDouble(systemWindowGlobalVariables.getLatestPrice().getText()) ;
        double percentagePrediction ;

        // calculate the IPS recommendations SHORT TERM
        percentagePrediction = (((value_SharePricePredictionNxtBsnssDay-currentPrice)/currentPrice)*100) ;
        summaryIPSRecommendationST.setText(getRecommendation(percentagePrediction)) ;

        // calculate the IPS recommendations LONG TERM
        percentagePrediction = (((value_SharePricePrediction12Months-currentPrice)/currentPrice)*100) ;
        summaryIPSRecommendationLT.setText(getRecommendation(percentagePrediction)) ;

        summaryPredictionNxtBsnssDay.setText(Double.toString(value_SharePricePredictionNxtBsnssDay)) ;
        summaryPrediction1Week.setText(Double.toString(value_SharePricePrediction1Week)) ;
        summaryPrediction1Month.setText(Double.toString(value_SharePricePrediction1Month)) ;
        summaryPrediction3Months.setText(Double.toString(value_SharePricePrediction3Months)) ;
        summaryPrediction6Months.setText(Double.toString(value_SharePricePrediction6Months)) ;
        summaryPrediction12Months.setText(Double.toString(value_SharePricePrediction12Months)) ;

    } // end of whenPredictionRRPrimaryPressed function

    //***********************************//
    // getRecommendation function        //
    //***********************************//
    /**
     *
     * Obtains a recommendation on what to do with this security
     * @param percentagePrediction       The percentage closeness
     *
     */
    private String getRecommendation(double percentagePrediction) {
//        System.out.println("Percentage nearing prediction (%): " + percentagePrediction) ;

        // if nxtBsnssDays predicted price is 6%+ above current price, then recommend strong buy
        if (percentagePrediction >= 6) {
            return "Strong Buy" ;
        }
        // if nxtBsnssDays predicted price is 3%-5.9% above current price, then recommend buy
        else if (percentagePrediction >= 3 && percentagePrediction < 6) {
            return "Buy" ;
        }
        // if nxtBsnssDays predicted price is 1%-2.9% above current price, then recommend weak buy
        else if (percentagePrediction >= 1 && percentagePrediction < 3) {
            return "Weak Buy" ;
        }
        // if nxtBsnssDays predicted price is 1%-2.9% below current price, then recommend weak sell
        else if (percentagePrediction <= -1 && percentagePrediction > -3) {
            return "Weak Sell" ;
        }
        // if nxtBsnssDays predicted price is 3%-5.9% below current price, then recommend sell
        else if (percentagePrediction <= -3 && percentagePrediction > -6) {
            return "Sell" ;
        }
        // if nxtBsnssDays predicted price is 6%+ below current price, then recommend strong sell
        else if (percentagePrediction <= -6) {
            return "Strong Sell" ;
        }
        // else price is between -1 and +1 % so recommend hold
        else {
            return "Hold" ;
        }
    } // end getRecommendation function

    //***********************************//
    // actionPerformed function          //
    //***********************************//
    /**
     *
     * The implementation of ActionEvent
     * @param e            The detected action event
     *
     */
    public void actionPerformed(ActionEvent e) {

        Object source = e.getSource() ;

        String classTypeOfTheAction = new String() ;
        classTypeOfTheAction = source.getClass().getName() ;


        /////////////////////////////////////////////
        // action handling for JButton
        /////////////////////////////////////////////
        if (classTypeOfTheAction == "javax.swing.JButton") {
            JButton buttonSource = (JButton)source ;
            systemWindowGlobalVariables.setStatusArea(buttonSource.getText()) ;

            if (buttonSource.getText() == "Get Details") {
                JDBCMPTSConnection.whenGetDetailsPressed(systemWindowGlobalVariables) ;
                createHighLowLadderPanel(systemWindowGlobalVariables.getHigh52Week().getText(), systemWindowGlobalVariables.getLow52Week().getText(), systemWindowGlobalVariables.getLatestPrice().getText()) ;
                return ;
            }
            else if (buttonSource.getText() == "Show Graph") {
                whenShowGraphPressed() ;
                return ;
            }
            else if (buttonSource.getText() == "Save Security Details") {
                whenSaveSecurityDetailsPressed(systemWindowGlobalVariables) ;
                return ;
            }
            else if (buttonSource.getText() == "Create Prediction (RR Primary form)") {
                whenPredictionRRPrimaryPressed() ;
                return ;
            }
            else if (buttonSource.getText() == "Disclaimer") {
                String disclaimerMessage = new String("In using the information provided on the Intelligent Prediction System,\nMPTS take no responsibility for any loss incurred") ;
                JOptionPane.showMessageDialog(null, disclaimerMessage, "Disclaimer", JOptionPane.INFORMATION_MESSAGE) ;
                return ;
            }
            else {
                System.err.print("Something weird happened when trying to handle " + classTypeOfTheAction + " action events\n") ;
            }
        } // end action handling for JButtons
    } // end of actionPerformed function

    //***********************************//
    // isDateFormatValid function     //
    //***********************************//
    /**
     *
     * With an inputted date string, the date is checked to be valid or not
     * @param dateToCheck    the date to check
     * @return               <code>true</code> if the date is valid; <code>false</code> otherwise.
     *
     */
    private boolean isDateFormatValid(String dateToCheck) {
        // return true  --> date is valid
        // return false --> date is invalid

        SimpleDateFormat tempSimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy") ;
        tempSimpleDateFormat.setLenient(false) ;

        // check for an empty date
        if (dateToCheck.equals("")) {
//            JOptionPane.showMessageDialog(null, "Please enter a valid date in the format 'DD/MM/YYYY'", "Enter a valid date", JOptionPane.ERROR_MESSAGE) ;
            return false ; // date field is empty
        }

        // check the length of the string
        if (dateToCheck.length() != 10) {
            JOptionPane.showMessageDialog(null, "Please enter a valid date in the format 'DD/MM/YYYY'", "Enter a valid date", JOptionPane.ERROR_MESSAGE) ;
            return false ;
        }

        // check that forwardslashes are located in the correct place
        if (!(dateToCheck.substring(2,3).compareTo("/") == 0) || // first divider
            !(dateToCheck.substring(5,6).compareTo("/") == 0)) { // second divider
            JOptionPane.showMessageDialog(null, "Please use forward slashes (/) as the date divider", "Invalid date divider", JOptionPane.ERROR_MESSAGE) ;
            return false ; // a divider is invalid
        }

        // check that the day is a number
        try {
            int day = Integer.parseInt(dateToCheck.substring(0,2)) ;
        }
        catch(NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter a valid day - not characters", "Invalid day", JOptionPane.ERROR_MESSAGE) ;
            return false ;
        }

        // check that the month is a number
        try {
            int month = Integer.parseInt(dateToCheck.substring(3,5)) ;
        }
        catch(NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter a valid month - not characters", "Invalid month", JOptionPane.ERROR_MESSAGE) ;
            return false ;
        }

        // check that the year is number
        try {
            int year = Integer.parseInt(dateToCheck.substring(6,10)) ;
        }
        catch(NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter a valid year - not characters", "Invalid year", JOptionPane.ERROR_MESSAGE) ;
            return false ;
        }

        // final checks,leap years, etc
        try {
            java.util.Date parseAttempt = tempSimpleDateFormat.parse(dateToCheck) ;
//            System.out.println(parseAttempt.toString()) ;
            return true ;
        }
        catch (java.text.ParseException e) {
            JOptionPane.showMessageDialog(null, "Please enter a valid date in the format 'DD/MM/YYYY'", "Enter a valid date", JOptionPane.ERROR_MESSAGE) ;
            return false ;
        }
    } // end function isDateFormatValid

    //***********************************//
    // openConnection function           //
    //***********************************//
    /**
     *
     * This function creates a live connection to the Central Database.
     *
     */
    private void openConnection() {
        try {  // load the driver
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        }
        catch(java.lang.ClassNotFoundException e) {
            System.err.print("Error in attempting to load database driver:\n      " + e.getMessage() + "\n");
        }

        try {  // make a connection
            liveConnection = DriverManager.getConnection("jdbc:odbc:CentralDatabase");
        }
        catch(SQLException e) {
            System.err.print("Error in attempting to connect to database:\n      " + e.getMessage() + "\n");
        }
    } // end of openConnection function

    //***********************************//
    // closeConnection function           //
    //***********************************//
    /**
     *
     * This function closes a live connection to the Central Database.
     *
     */
    private void closeConnection() {
        try {
            if( liveConnection != null) { liveConnection.close(); }
        }
        catch(SQLException e) {
            System.err.print("Error in attempting to close connection to database:\n      " + e.getMessage() + "\n");
        }
    } // end of closeConnection function

} //EOF