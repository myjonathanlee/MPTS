import java.io.* ;
import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;
import javax.swing.border.* ;
import javax.swing.filechooser.* ;

/**
 *
 * SystemWindow is the main class in the MPTS system which is responsible
 * for the management and creation of the GUI and windows interface.  There
 * is an exception to this: The Intelligent Prediction System (IPS) is managed
 * by the class IPS.java.  This class is described further in the IPS.java
 * documentation but briefly, it returns a JPanel to this SystemWindow class;
 * thus combining all the JPanels.
 *
 * @author        Jonathan K. W. Lee
 * @version       1.0
 * @see           MPTS_main
 * @see           IPS
 * @see           JDBCEvents
 * @see           MPTSGlobalVariables
 *
 */
public class SystemWindow extends JFrame
                          implements ActionListener, ItemListener {
    // Class variables
    private static JTabbedPane tabPane = new JTabbedPane() ;

    private static JPanel  p ;
    private static JPanel  currentPanel ;
    private static JButton button ;
    private static JPanel  embroideringBox ;

    private static JTable  returnedPendingTradesTable = new JTable() ;
    private static JTable  returnedMyPortfolioTable   = new JTable() ;

    private static JMenuItem loginMenuItem ;
    private static JMenuItem logoutMenuItem ;

    private static JDBCEvents          JDBCMPTSConnection ;          // a class which interacts with the Central Database
    private static MPTSGlobalVariables systemWindowGlobalVariables ; // a class which holds all global variables
    private static IPS                 systemWindowIPSPanel ;        // a class which deals entirely with the IPS

    private static String MPTSHelp ;
    private static String MPTSTermsAndConditions ;

    final private static String MPTS_HELP_FILE            = "information/Help.txt" ;
    final private static String MPTS_TERMS_AND_CONDITIONS = "information/Terms and Conditions.txt" ;

    //***********************************//
    // CONSTRUCTOR                       //
    //***********************************//
    /**
     *
     * SystemWindow constructor
     *
     * Initialises variables.  In particular, the constructor checks the inputted
     * user name and password.  If they do not concur, the system is created with
     * no user logged in.
     *
     *
     * @param clientUserID          The user ID passed into the program
     * @param clientPassword        The user password passed into the program
     *
     */
    SystemWindow(String clientUserID, String clientPassword) {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0) ;
            }
        }) ;

        // create JDBC objects
        JDBCMPTSConnection = new JDBCEvents() ;

        // check validity of clientUserID and clientPassword
        if (JDBCMPTSConnection.checkUsernameAndPassword(clientUserID, clientPassword) == false) { // invalid
            JOptionPane.showMessageDialog(null, "The username and password you have entered is invalid.\nThe system will log you on as default.\nPlease login from the File menu", "Login", JOptionPane.ERROR_MESSAGE) ;
            clientUserID   = "default" ;
            clientPassword = "default" ;
        } // else true so do nothing

        // set the window title
        setTitle("Market Prediction Trading System - [" + clientUserID + "]") ;

        // create user information files
        MPTSHelp               = initialiseMPTSInformationFiles(MPTS_HELP_FILE) ;
        MPTSTermsAndConditions = initialiseMPTSInformationFiles(MPTS_TERMS_AND_CONDITIONS) ;

        // initialise variables
        systemWindowGlobalVariables = new MPTSGlobalVariables(clientUserID, clientPassword) ;
        systemWindowIPSPanel        = new IPS(systemWindowGlobalVariables) ;

        // create the system window
        createSystemWindowJPanels() ;
    } // end of constructor

    //************************************//
    // initialiseMPTSInformationFiles function //
    //************************************//
    /**
     *
     * Creates and initialises user information objects
     * @param fileName       the file name from which information is read
     * @return               A String of the entire information file
     *
     */
    private String initialiseMPTSInformationFiles(String fileName) {
        String informationString = new String() ;

        try {
            BufferedReader informationFile = new BufferedReader(new FileReader(fileName)) ;
            try {
                System.out.print("\nReading data into program for: " + fileName) ;
                int n = 0 ;
                int m = 0 ; // a counter for

                while (n != -1) {
                    n = informationFile.read() ;
                    informationString += Character.toString((char)n) ;

                    if (m % 50 == 0) { // something just to show the user the program is working
                        System.out.print(".") ;
                    }
                    m++ ;
                }
                informationFile.close() ;
            }
            catch(IOException e1) {
                return "Information data may be corrupt.\nPlease reinstall the software" ;
            }
        }
        catch(FileNotFoundException e) {
            return "Information data file could not be found.\nPlease reinstall the software" ;
        }

        return informationString ;
    } // end of function initialiseMPTSInformationFiles

    //************************************//
    // createSystemWindowJPanels function //
    //************************************//
    /**
     *
     * A function assigned to oversee the creation of each of the system windows
     *
     */
    private void createSystemWindowJPanels() {

        ///////////////////////////////////////
        // create and set the menu bar...
        ///////////////////////////////////////
        setJMenuBar(createMenuBar()) ;

        ///////////////////////////////////////
        // create the tab panels...
        ///////////////////////////////////////
        // create EXECUTION OPTIONS tab
        currentPanel = createExecutionOptionsTab() ;
        p.add(currentPanel) ;
        tabPane.addTab("Execution Options", p) ;

        // create PENDING TRADES tab
        currentPanel = createPendingTradesTab() ;
        p.add(currentPanel) ;
        tabPane.addTab("Pending Trades", p) ;

        // create MY PORTFOLIO tab
        currentPanel = createMyPortfolioTab() ;
        p.add(currentPanel) ;
        tabPane.addTab("My Portfolio", p) ;

        // create INTELLIGENT PREDICTION SYSTEM tab
        currentPanel = new JPanel(new BorderLayout()) ; // re-initialise for this panel
        p = new JPanel(new BorderLayout()) ; // re-initialise for this panel
        systemWindowIPSPanel = new IPS(systemWindowGlobalVariables) ; // re-initialise for this panel
        currentPanel = systemWindowIPSPanel.createEntireTabPanel(systemWindowGlobalVariables) ;
        p.add(currentPanel) ;
        tabPane.addTab("Intelligent Prediction System", p) ;

        ///////////////////////////////////////
        // Add the tab panes and status area to the container
        ///////////////////////////////////////
        getContentPane().add(tabPane, BorderLayout.CENTER) ;
        getContentPane().add(systemWindowGlobalVariables.getStatusArea(), BorderLayout.SOUTH) ;
    } // end of createSystemWindowJPanels function

    //***********************************//
    // actionPerformed function          //
    //***********************************//
    /**
     *
     * The implementation of ActionEvent
     *
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
                return ;
            }
            else if (buttonSource.getText() == "Go to website") { // execute function in JDBCEvents class to put details into database
                System.out.println("going to the website now.....") ;
                return ;
            }
            else if (buttonSource.getText() == "Execute Purchase/Sale") { // execute function in JDBCEvents class to put details into database
                JDBCMPTSConnection.whenExecutionPressed(systemWindowGlobalVariables) ;
                return ;
            }
            else if (buttonSource.getText() == "Remove Pending Order") {
                JDBCMPTSConnection.whenRemovePendingOrderPressed(returnedPendingTradesTable, systemWindowGlobalVariables) ;
                return ;
            }
            else if (buttonSource.getText() == "Export Pending Trades") { // export the table to CSV format
                JDBCMPTSConnection.whenExportPressed(returnedPendingTradesTable, systemWindowGlobalVariables) ;
                return ;
            }
            else if (buttonSource.getText() == "Get My Portfolio Summary") {
                JDBCMPTSConnection.whenGetMyPortfolioDetailsPressed(returnedMyPortfolioTable, systemWindowGlobalVariables) ;
                return ;
            }
            else if (buttonSource.getText() == "Export My Portfolio") { // export the table to CSV format
                JDBCMPTSConnection.whenExportPressed(returnedMyPortfolioTable, systemWindowGlobalVariables) ;
                return ;
            }
            else {
                System.err.print("Something weird happened when trying to handle " + classTypeOfTheAction + " action events\n") ;
            }
        } // end action handling for JButtons


        /////////////////////////////////////////////
        // action handling for JMenu
        /////////////////////////////////////////////
        else if (classTypeOfTheAction == "javax.swing.JMenuItem") {
            JMenuItem menuSource = (JMenuItem)source ;
            systemWindowGlobalVariables.setStatusArea(menuSource.getText()) ;

            if (menuSource.getText() == "Login") {
                String userLoggingIn = new String() ;
                try { // userLoggedIn string which is returned ***is*** valid
                    userLoggingIn = JDBCMPTSConnection.whenLoginRequested(systemWindowGlobalVariables) ;
                }
                catch(NullPointerException e2) {}

                if (userLoggingIn != null) {
                    systemWindowGlobalVariables.setUserID(userLoggingIn) ;
                    systemWindowGlobalVariables.setUserLoggedIn() ;
                    setTitle("Market Prediction Trading System - [" + userLoggingIn + "]") ;

                    // refresh the system
                    systemWindowGlobalVariables.resetVariables() ;
                    tabPane.removeAll() ;
                    createSystemWindowJPanels() ; // recreate all the panels
                    loginMenuItem.setEnabled(false) ;
                    logoutMenuItem.setEnabled(true) ;
                }
            }
            else if (menuSource.getText() == "Logout") {
                systemWindowGlobalVariables.setUserID("default") ;
                systemWindowGlobalVariables.setUserLoggedIn() ;
                setTitle("Market Prediction Trading System - please log into the system") ;

                // refresh the system
                systemWindowGlobalVariables.resetVariables() ;
                tabPane.removeAll() ;
                createSystemWindowJPanels() ; // recreate all the panels
                loginMenuItem.setEnabled(true) ;
                logoutMenuItem.setEnabled(false) ;
            }
            else if (menuSource.getText() == "Help Topics") {
                JTextArea helpText = new JTextArea(MPTSHelp, 20, 30) ;
                helpText.setEditable(false) ;
                helpText.setLineWrap(true) ;
                helpText.setWrapStyleWord(true) ;
                JScrollPane helpTextScrollPane = new JScrollPane(helpText) ;
                JOptionPane.showMessageDialog(this, helpTextScrollPane, "Help Topics", JOptionPane.INFORMATION_MESSAGE) ;
            }
            else if (menuSource.getText() == "Terms & Conditions") {
                JTextArea termsAndConditionsText = new JTextArea(MPTSTermsAndConditions, 20, 30) ;
                termsAndConditionsText.setEditable(false) ;
                termsAndConditionsText.setLineWrap(true) ;
                termsAndConditionsText.setWrapStyleWord(true) ;
                JScrollPane termsAndConditionsScrollPane = new JScrollPane(termsAndConditionsText) ;
                JOptionPane.showMessageDialog(this, termsAndConditionsScrollPane, "Terms & Conditions", JOptionPane.INFORMATION_MESSAGE) ;
            }
            else if (menuSource.getText() == "About the Market Prediction Trading System") {
                JOptionPane.showMessageDialog(this, "JBank & Co.\nMarket Prediction Trading System 1.0:  Windows XP Edition\n\nCopyright (c) 2002-2003  Jonathan K. W. Lee (jonlee@cs.rhul.ac.uk)\nAll Rights Reserved\n\nLicensed to: " + systemWindowGlobalVariables.getUserID(), "About the Market Prediction Trading System", JOptionPane.INFORMATION_MESSAGE) ;
            }
            else if (menuSource.getText() == "Refresh") {
                systemWindowGlobalVariables.resetVariables() ;
                tabPane.removeAll() ;
                createSystemWindowJPanels() ; // recreate all the panels
            }
            else {
                System.err.print("undefined action event detected when handling JMenu: " + classTypeOfTheAction + "\n") ;
            }
        } // end action handling for JMenus


        /////////////////////////////////////////////
        // action handling for JComboBox dealt with in class: MPTSGlobalVariables
        /////////////////////////////////////////////

        /////////////////////////////////////////////
        // another class existed for which I have not yet provided action handling
        /////////////////////////////////////////////
        else {
            System.out.print("undefined action event detected of type " + classTypeOfTheAction + "\n") ;
        }
    } // end of actionPerformed function

    //***********************************//
    // itemStateChanged function         //
    //***********************************//
    /**
     *
     * The implementation of ItemStateChanged
     *
     * @param e            The detected item state changed event
     *
     */
    public void itemStateChanged(ItemEvent e) {

        Object source = e.getSource() ;

        String classTypeOfTheAction = new String() ;
        classTypeOfTheAction = source.getClass().getName() ;

        System.out.print(e +"\n\n") ;
        System.out.print(e.getItem() +"\n\n") ;
        System.out.print(source +"\n\n") ;

    } // end of itemStateChanged function

    //***********************************//
    // createBuyTab function
    //***********************************//
    /**
     *
     * A function assigned to oversee the creation of the "Buy" tab
     * @return            Object of type <code>JPanel</code>.
     *
     */
    private JPanel createExecutionOptionsTab() {
        currentPanel = new JPanel(new BorderLayout()) ; // re-initialise for this panel
        p = new JPanel(new BorderLayout()) ; // re-initialise for this panel

        Box ExecutionOptionsPanel      = Box.createVerticalBox() ;

        Box ExecutionOptionsQueryingA  = Box.createHorizontalBox() ;
        Box ExecutionOptionsQueryingB  = Box.createHorizontalBox() ;
        Box ExecutionOptionsQueryingC  = Box.createHorizontalBox() ;
        Box ExecutionOptionsQueryingD  = Box.createHorizontalBox() ;
        Box ExecutionOptionsQueryingE  = Box.createHorizontalBox() ;

        Box ExecutionOptionsExecutingA = Box.createHorizontalBox() ;
        Box ExecutionOptionsExecutingB = Box.createHorizontalBox() ;
        Box ExecutionOptionsExecutingC = Box.createHorizontalBox() ;
        Box ExecutionOptionsExecutingD = Box.createHorizontalBox() ;
        Box ExecutionOptionsExecutingE = Box.createHorizontalBox() ;

        // add elements to the querying section of the screen
        ExecutionOptionsQueryingA.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsQueryingA.add(new JLabel("Security:")) ;
        ExecutionOptionsQueryingA.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsQueryingA.add(systemWindowGlobalVariables.getAvailableSecurities()) ;
        ExecutionOptionsQueryingA.add(Box.createHorizontalStrut(50)) ;
        ExecutionOptionsQueryingA.add(button = new JButton("Get Details")) ;
        button.addActionListener(this) ;
        ExecutionOptionsQueryingA.add(Box.createHorizontalStrut(10)) ;

        ExecutionOptionsQueryingB.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsQueryingB.add(new JLabel("Latest Price (p):")) ;
        ExecutionOptionsQueryingB.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsQueryingB.add(systemWindowGlobalVariables.getLatestPrice()) ;
        ExecutionOptionsQueryingB.add(Box.createHorizontalStrut(50)) ;
        ExecutionOptionsQueryingB.add(new JLabel("EPIC Code:")) ;
        ExecutionOptionsQueryingB.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsQueryingB.add(systemWindowGlobalVariables.getEPICCode()) ;
        ExecutionOptionsQueryingB.add(Box.createHorizontalStrut(50)) ;
        ExecutionOptionsQueryingB.add(new JLabel("ISIN Code:")) ;
        ExecutionOptionsQueryingB.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsQueryingB.add(systemWindowGlobalVariables.getISINCode()) ;
        ExecutionOptionsQueryingB.add(Box.createHorizontalStrut(10)) ;

        ExecutionOptionsQueryingC.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsQueryingC.add(new JLabel("Sector:")) ;
        ExecutionOptionsQueryingC.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsQueryingC.add(systemWindowGlobalVariables.getSector()) ;
        ExecutionOptionsQueryingC.add(Box.createHorizontalStrut(50)) ;
        ExecutionOptionsQueryingC.add(new JLabel("Originating Country:")) ;
        ExecutionOptionsQueryingC.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsQueryingC.add(systemWindowGlobalVariables.getOriginatingCountry()) ;
        ExecutionOptionsQueryingC.add(Box.createHorizontalStrut(10)) ;

        ExecutionOptionsQueryingD.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsQueryingD.add(new JLabel("52 week high (p):")) ;
        ExecutionOptionsQueryingD.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsQueryingD.add(systemWindowGlobalVariables.getHigh52Week()) ;
        ExecutionOptionsQueryingD.add(Box.createHorizontalStrut(50)) ;
        ExecutionOptionsQueryingD.add(new JLabel("52 week low (p):")) ;
        ExecutionOptionsQueryingD.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsQueryingD.add(systemWindowGlobalVariables.getLow52Week()) ;
        ExecutionOptionsQueryingD.add(Box.createHorizontalStrut(50)) ;
        ExecutionOptionsQueryingD.add(new JLabel("Market Capitalisation ($m):")) ;
        ExecutionOptionsQueryingD.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsQueryingD.add(systemWindowGlobalVariables.getMarketCapitalisation()) ;
        ExecutionOptionsQueryingD.add(Box.createHorizontalStrut(10)) ;

        ExecutionOptionsQueryingE.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsQueryingE.add(new JLabel("Share or Bond:")) ;
        ExecutionOptionsQueryingE.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsQueryingE.add(systemWindowGlobalVariables.getShareOrBond()) ;
        ExecutionOptionsQueryingE.add(Box.createHorizontalStrut(50)) ;
        ExecutionOptionsQueryingE.add(new JLabel("Company URL:")) ;
        ExecutionOptionsQueryingE.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsQueryingE.add(systemWindowGlobalVariables.getCompanyURL()) ;
        ExecutionOptionsQueryingE.add(Box.createHorizontalStrut(50)) ;
        ExecutionOptionsQueryingE.add(button = new JButton("Go to website")) ;
        button.addActionListener(this) ;
        ExecutionOptionsQueryingE.add(Box.createHorizontalStrut(10)) ;

        // a box to hold all querying boxes
        Box ExecutionOptionsQueryingAll = Box.createVerticalBox() ;
        ExecutionOptionsQueryingAll.add(Box.createVerticalStrut(15)) ;
        ExecutionOptionsQueryingAll.add(ExecutionOptionsQueryingA) ;
        ExecutionOptionsQueryingAll.add(Box.createVerticalStrut(15)) ;
        ExecutionOptionsQueryingAll.add(ExecutionOptionsQueryingB) ;
        ExecutionOptionsQueryingAll.add(Box.createVerticalStrut(15)) ;
        ExecutionOptionsQueryingAll.add(ExecutionOptionsQueryingC) ;
        ExecutionOptionsQueryingAll.add(Box.createVerticalStrut(15)) ;
        ExecutionOptionsQueryingAll.add(ExecutionOptionsQueryingD) ;
        ExecutionOptionsQueryingAll.add(Box.createVerticalStrut(15)) ;
        ExecutionOptionsQueryingAll.add(ExecutionOptionsQueryingE) ;
        ExecutionOptionsQueryingAll.add(Box.createVerticalStrut(15)) ;

        embroideringBox = new JPanel(new BorderLayout()) ;
        embroideringBox.setBorder( new TitledBorder(new EtchedBorder(), "Security Details")) ;
        embroideringBox.add(ExecutionOptionsQueryingAll, BorderLayout.NORTH) ;

        ExecutionOptionsPanel.add(embroideringBox) ;

        // add elements to the executing section of the screen
        ExecutionOptionsExecutingA.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsExecutingA.add(new JLabel("Security:")) ;
        ExecutionOptionsExecutingA.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsExecutingA.add(systemWindowGlobalVariables.getSecurityGreyedBox()) ;
        ExecutionOptionsExecutingA.add(Box.createHorizontalStrut(10)) ;

        ExecutionOptionsExecutingB.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsExecutingB.add(new JLabel("Sale or Purchase:")) ;
        ExecutionOptionsExecutingB.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsExecutingB.add(systemWindowGlobalVariables.getAvailableSaleOrPurchase()) ;
        ExecutionOptionsExecutingB.add(Box.createHorizontalStrut(50)) ;
        ExecutionOptionsExecutingB.add(new JLabel("Quantity:")) ;
        ExecutionOptionsExecutingB.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsExecutingB.add(systemWindowGlobalVariables.getQuantity()) ;
        ExecutionOptionsExecutingB.add(Box.createHorizontalStrut(10)) ;

        ExecutionOptionsExecutingC.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsExecutingC.add(new JLabel("Trade Type:")) ;
        ExecutionOptionsExecutingC.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsExecutingC.add(systemWindowGlobalVariables.getAvailableTradeTypes()) ;
        ExecutionOptionsExecutingC.add(Box.createHorizontalStrut(50)) ;
        ExecutionOptionsExecutingC.add(new JLabel("Limit Price (p):")) ;
        ExecutionOptionsExecutingC.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsExecutingC.add(systemWindowGlobalVariables.getLimitPrice()) ;
        ExecutionOptionsExecutingC.add(Box.createHorizontalStrut(10)) ;

        ExecutionOptionsExecutingD.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsExecutingD.add(new JLabel("Fee Charged ($):")) ;
        ExecutionOptionsExecutingD.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsExecutingD.add(systemWindowGlobalVariables.getFeeCharged()) ;
        ExecutionOptionsExecutingD.add(Box.createHorizontalStrut(50)) ;
        ExecutionOptionsExecutingD.add(new JLabel("User Notes:")) ;
        ExecutionOptionsExecutingD.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsExecutingD.add(systemWindowGlobalVariables.getNotesOfTrade()) ;
        ExecutionOptionsExecutingD.add(Box.createHorizontalStrut(10)) ;

        ExecutionOptionsExecutingE.add(Box.createHorizontalStrut(10)) ;
        ExecutionOptionsExecutingE.add(button = new JButton("Execute Purchase/Sale")) ;
        button.addActionListener(this) ;
        ExecutionOptionsExecutingE.add(Box.createHorizontalStrut(10)) ;

        // a box to hold all executing boxes
        Box ExecutionOptionsExecutingAll = Box.createVerticalBox() ;
        ExecutionOptionsExecutingAll.add(Box.createVerticalStrut(15)) ;
        ExecutionOptionsExecutingAll.add(ExecutionOptionsExecutingA) ;
        ExecutionOptionsExecutingAll.add(Box.createVerticalStrut(15)) ;
        ExecutionOptionsExecutingAll.add(ExecutionOptionsExecutingB) ;
        ExecutionOptionsExecutingAll.add(Box.createVerticalStrut(15)) ;
        ExecutionOptionsExecutingAll.add(ExecutionOptionsExecutingC) ;
        ExecutionOptionsExecutingAll.add(Box.createVerticalStrut(15)) ;
        ExecutionOptionsExecutingAll.add(ExecutionOptionsExecutingD) ;
        ExecutionOptionsExecutingAll.add(Box.createVerticalStrut(15)) ;
        ExecutionOptionsExecutingAll.add(ExecutionOptionsExecutingE) ;
        ExecutionOptionsExecutingAll.add(Box.createVerticalStrut(15)) ;

        embroideringBox = new JPanel(new BorderLayout()) ;
        embroideringBox.setBorder( new TitledBorder(new EtchedBorder(), "Execution Options")) ;
        embroideringBox.add(ExecutionOptionsExecutingAll, BorderLayout.NORTH) ;

        ExecutionOptionsPanel.add(embroideringBox) ;

        currentPanel.add(ExecutionOptionsPanel, BorderLayout.CENTER) ;
        return currentPanel ;
    } // end of createExecutionOptionsTab function

    //***********************************//
    // createPendingTradesTab function
    //***********************************//
    /**
     *
     * A function assigned to oversee the creation of the "Pending Trades" tab
     * @return            Object of type <code>JPanel</code>.
     *
     */
    private JPanel createPendingTradesTab() {
        currentPanel = new JPanel(new BorderLayout()) ; // re-initialise for this panel
        p = new JPanel(new BorderLayout()) ; // re-initialise for this panel

        JScrollPane PendingTradesResultsScrollPane ;
        JPanel      PendingTradesSummary = new JPanel(new FlowLayout()) ;
        Box         PendingTradesPanel = Box.createVerticalBox() ;

        // set pending trades table
        returnedPendingTradesTable = JDBCMPTSConnection.getPendingTrades(systemWindowGlobalVariables.getUserID()) ;
        returnedPendingTradesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF) ;
        PendingTradesResultsScrollPane = new JScrollPane(returnedPendingTradesTable) ;

        embroideringBox = new JPanel(new BorderLayout()) ;
        embroideringBox.setBorder( new TitledBorder(new EtchedBorder(), "Trades Currently Pending on the Order Queue")) ;
        embroideringBox.add(PendingTradesResultsScrollPane, BorderLayout.CENTER) ;

        PendingTradesPanel.add(embroideringBox) ;

        // set summary section
        Box PendingTradesSummaryA = Box.createHorizontalBox() ;
        Box PendingTradesSummaryB = Box.createHorizontalBox() ; // is this one needed?
        Box PendingTradesSummaryC = Box.createHorizontalBox() ; // is this one needed?

        PendingTradesSummaryA.add(Box.createHorizontalStrut(10)) ;
        PendingTradesSummaryA.add(button = new JButton("Remove Pending Order")) ;
        button.addActionListener(this) ;
        PendingTradesSummaryA.add(Box.createHorizontalStrut(50)) ;
        PendingTradesSummaryA.add(button = new JButton("Export Pending Trades", new ImageIcon("images/save.gif"))) ;
        button.addActionListener(this) ;
/*        PendingTradesSummaryA.add(Box.createHorizontalStrut(50)) ;
        PendingTradesSummaryA.add(button = new JButton("Print Pending Trades", new ImageIcon("images/print.gif"))) ;
        button.addActionListener(this) ;
*/        PendingTradesSummaryA.add(Box.createHorizontalStrut(10)) ;

        Box PendingTradesSummaryAll = Box.createVerticalBox() ;
        PendingTradesSummaryAll.add(Box.createVerticalStrut(15)) ;
        PendingTradesSummaryAll.add(PendingTradesSummaryA) ;
        PendingTradesSummaryAll.add(Box.createVerticalStrut(15)) ;
        PendingTradesSummaryAll.add(PendingTradesSummaryB) ;
        PendingTradesSummaryAll.add(Box.createVerticalStrut(15)) ;
        PendingTradesSummaryAll.add(PendingTradesSummaryC) ;
        PendingTradesSummaryAll.add(Box.createVerticalStrut(15)) ;

        embroideringBox = new JPanel(new BorderLayout()) ;
        embroideringBox.setBorder( new TitledBorder(new EtchedBorder(), "Summary")) ;
        embroideringBox.add(PendingTradesSummaryAll, BorderLayout.NORTH) ;

        PendingTradesPanel.add(embroideringBox) ;

        currentPanel.add(PendingTradesPanel, BorderLayout.CENTER) ;
        return currentPanel ;
    } // end of createPendingTradesTab function

    //***********************************//
    // createMyPortfolioTab function
    //***********************************//
    /**
     *
     * A function assigned to oversee the creation of the "My Portfolio" tab
     * @return            Object of type <code>JPanel</code>.
     *
     */
    private JPanel createMyPortfolioTab() {
        currentPanel = new JPanel(new BorderLayout()) ; // re-initialise for this panel
        p = new JPanel(new BorderLayout()) ; // re-initialise for this panel

        JScrollPane MyPortfolioResultsScrollPane ;
        Box         MyPortfolioPanel = Box.createVerticalBox() ;

        // set portfolio table
        returnedMyPortfolioTable = JDBCMPTSConnection.getMyPortfolio(systemWindowGlobalVariables.getUserID()) ;
        returnedMyPortfolioTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF) ;
        MyPortfolioResultsScrollPane = new JScrollPane(returnedMyPortfolioTable) ;

        embroideringBox = new JPanel(new BorderLayout()) ;
        embroideringBox.setBorder( new TitledBorder(new EtchedBorder(), "Latest Portfolio")) ;
        embroideringBox.add(MyPortfolioResultsScrollPane, BorderLayout.CENTER) ;

        MyPortfolioPanel.add(embroideringBox) ;

        // set summary section
        Box MyPortfolioSummaryA = Box.createHorizontalBox() ;
        Box MyPortfolioSummaryB = Box.createHorizontalBox() ;
        Box MyPortfolioSummaryC = Box.createHorizontalBox() ;
        Box MyPortfolioSummaryD = Box.createHorizontalBox() ;

        MyPortfolioSummaryA.add(Box.createHorizontalStrut(10)) ;
        MyPortfolioSummaryA.add(button = new JButton("Get My Portfolio Summary")) ;
        button.addActionListener(this) ;
        MyPortfolioSummaryA.add(Box.createHorizontalStrut(50)) ;
        MyPortfolioSummaryA.add(button = new JButton("Export My Portfolio", new ImageIcon("images/save.gif"))) ;
        button.addActionListener(this) ;
        MyPortfolioSummaryA.add(Box.createHorizontalStrut(10)) ;

        MyPortfolioSummaryB.add(Box.createHorizontalStrut(10)) ;
        MyPortfolioSummaryB.add(new JLabel("Present value of portfolio ($):")) ;
        MyPortfolioSummaryB.add(Box.createHorizontalStrut(14)) ;
        MyPortfolioSummaryB.add(systemWindowGlobalVariables.getPresentValueOfPortfolio()) ;
        MyPortfolioSummaryB.add(Box.createHorizontalStrut(10)) ;

        MyPortfolioSummaryC.add(Box.createHorizontalStrut(10)) ;
        MyPortfolioSummaryC.add(new JLabel("Total gain/loss of portfolio ($):")) ;
        MyPortfolioSummaryC.add(Box.createHorizontalStrut(10)) ;
        MyPortfolioSummaryC.add(systemWindowGlobalVariables.getTotalGainLossOfPortfolio()) ;
        MyPortfolioSummaryC.add(Box.createHorizontalStrut(10)) ;

        MyPortfolioSummaryD.add(Box.createHorizontalStrut(10)) ;
        MyPortfolioSummaryD.add(new JLabel("Latest Trade Date & Time:")) ;
        MyPortfolioSummaryD.add(Box.createHorizontalStrut(30)) ;
        MyPortfolioSummaryD.add(systemWindowGlobalVariables.getLatestTradeDateOfPortfolio()) ;
        MyPortfolioSummaryD.add(Box.createHorizontalStrut(10)) ;

        Box MyPortfolioSummaryAll = Box.createVerticalBox() ;
        MyPortfolioSummaryAll.add(Box.createVerticalStrut(15)) ;
        MyPortfolioSummaryAll.add(MyPortfolioSummaryA) ;
        MyPortfolioSummaryAll.add(Box.createVerticalStrut(15)) ;
        MyPortfolioSummaryAll.add(MyPortfolioSummaryB) ;
        MyPortfolioSummaryAll.add(Box.createVerticalStrut(15)) ;
        MyPortfolioSummaryAll.add(MyPortfolioSummaryC) ;
        MyPortfolioSummaryAll.add(Box.createVerticalStrut(15)) ;
        MyPortfolioSummaryAll.add(MyPortfolioSummaryD) ;
        MyPortfolioSummaryAll.add(Box.createVerticalStrut(15)) ;

        embroideringBox = new JPanel(new BorderLayout()) ;
        embroideringBox.setBorder( new TitledBorder(new EtchedBorder(), "Summary")) ;
        embroideringBox.add(MyPortfolioSummaryAll, BorderLayout.NORTH) ;

        MyPortfolioPanel.add(embroideringBox) ;

        currentPanel.add(MyPortfolioPanel, BorderLayout.CENTER) ;
        return currentPanel ;
    } // end of createMyPortfolioTab function

    //***********************************//
    // createMenuBar function
    //***********************************//
    /**
     *
     * A function assigned to oversee the creation of the system menu bar
     * @return            Object of type <code>JMenuBar</code>.
     *
     */
    private JMenuBar createMenuBar() {

        JMenuBar  menuBar ;
        JMenu     menu ;
        JMenuItem menuItem ;

        menuBar = new JMenuBar() ;

        //*---------------------------------*//
        // Build FILE menu                   //
        //*---------------------------------*//
        menu = new JMenu("File") ;
        menu.setMnemonic(KeyEvent.VK_F) ;
        menu.getAccessibleContext().setAccessibleDescription("The File Menu") ;
        menuBar.add(menu) ;

        loginMenuItem = new JMenuItem("Login") ;
        loginMenuItem.setMnemonic(KeyEvent.VK_I) ;
        loginMenuItem.addActionListener(this) ;
        menu.add(loginMenuItem) ;

        logoutMenuItem = new JMenuItem("Logout") ;
        logoutMenuItem.setMnemonic(KeyEvent.VK_T) ;
        logoutMenuItem.addActionListener(this) ;
        menu.add(logoutMenuItem) ;

        if (systemWindowGlobalVariables.getUserLoggedIn()) {
            loginMenuItem.setEnabled(false) ;
            logoutMenuItem.setEnabled(true) ;
        }
        else {
            loginMenuItem.setEnabled(true) ;
            logoutMenuItem.setEnabled(false) ;
        }

        menu.addSeparator() ;

        menuItem = new JMenuItem("Exit") ;
        menuItem.setMnemonic(KeyEvent.VK_X) ;
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK)) ;
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { System.exit(0) ; }
        }) ;
        menu.add(menuItem) ;

        //*---------------------------------*//
        // Build TOOLS menu                  //
        //*---------------------------------*//
        menu = new JMenu("Tools") ;
        menu.setMnemonic(KeyEvent.VK_T) ;
        menu.getAccessibleContext().setAccessibleDescription("Tools Menu") ;
        menuBar.add(menu) ;

        menuItem = new JMenuItem("Refresh") ;
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, ActionEvent.CTRL_MASK)) ;
        menuItem.addActionListener(this) ;
        menu.add(menuItem) ;

        //*---------------------------------*//
        // Build HELP menu                   //
        //*---------------------------------*//
        menu = new JMenu("Help") ;
        menu.setMnemonic(KeyEvent.VK_H) ;
        menu.getAccessibleContext().setAccessibleDescription("Help Menu") ;
        menuBar.add(menu) ;

        menuItem = new JMenuItem("Help Topics") ;
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, ActionEvent.CTRL_MASK)) ;
        menuItem.setMnemonic(KeyEvent.VK_H) ;
        menuItem.addActionListener(this) ;
        menu.add(menuItem) ;

        menu.addSeparator() ;

        menuItem = new JMenuItem("Terms & Conditions") ;
        menuItem.setMnemonic(KeyEvent.VK_T) ;
        menuItem.addActionListener(this) ;
        menu.add(menuItem) ;

        menuItem = new JMenuItem("About the Market Prediction Trading System", new ImageIcon("images/phone.gif")) ;
        menuItem.setMnemonic(KeyEvent.VK_A) ;
        menuItem.addActionListener(this) ;
        menu.add(menuItem) ;

        return menuBar ;
    } // end of createMenuBar function

} //EOF
