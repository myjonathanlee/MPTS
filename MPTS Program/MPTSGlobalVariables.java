import java.awt.event.* ;
import java.awt.Color ;
import javax.swing.* ;
import javax.swing.border.* ;

/**
 *
 * Because of the creation of a window system, a "global variable" class has been
 * created.  MPTSGlobalVariables is a class which houses global variables for the
 * duration of the program.
 *
 * <p>
 * <b>For example...</b><br>
 * The MPTS contains a "status bar" at the bottom of the program.  This informs
 * the user of which buttons were selected and what actions have been execute
 * or cancelled.  This status bar text can be affected by more than one function
 * so it is important that this remains a global variable
 * </p>
 *
 * @author        Jonathan K. W. Lee
 * @version       1.0
 * @see           IPS
 * @see           SystemWindow
 *
 */
public class MPTSGlobalVariables implements ActionListener, TextListener {
    // Class variables

    // truly global constants
    final public static double NO_TRADING_DAYS_1YEAR = 252.0 ; // does not include bank holidays, weekends, etc

    // variables for the execution querying tab
    private JTextField limitPrice ;
    private JTextField securityGreyedBox ;

    private JTextField ISINCode ;
    private JTextField latestPrice ;
    private JTextField EPICCode ;
    private JTextField sector ;
    private JTextField originatingCountry ;
    private JTextField marketCapitalisation ;
    private JTextField high52Week ;
    private JTextField low52Week ;
    private JTextField companyURL ;
    private JTextField shareOrBond ;

    private JComboBox  availableTradeTypes ;
    private JComboBox  availableSaleOrPurchase ;
    private JComboBox  availableSecurities ;

    private static JTextArea statusArea ;

    private static boolean userLoggedIn = false ;
    private static String  userID ;
    private static String  userPassword ;

    private static JDBCEvents JDBCMPTSConnection ;

    // variables for the execution tab
    private JTextField quantity ;
    private JTextField feeCharged ;
    private JTextField notesOfTrade ;

    // variables for the My Portfolio tab
    private JTextField presentValueOfPortfolio ;
    private JTextField totalGainLossOfPortfolio ;
    private JTextField latestTradeDateOfPortfolio ;

    //***********************************//
    // CONSTRUCTOR                       //
    //***********************************//
    /**
     *
     * MPTSGlobalVariables constructor
     *
     * Initialises variables.
     *
     */
    MPTSGlobalVariables() {
        JDBCMPTSConnection = new JDBCEvents() ;
    } // end constructor

    //***********************************//
    // CONSTRUCTOR                       //
    //***********************************//
    /**
     *
     * MPTSGlobalVariables constructor
     *
     * Initialises variables.  In particular, the constructor checks the inputted
     * user name and password.  If they do not concur, the system is created with
     * no user logged in.
     *
     * @param clientUserID            The user ID passed into the program
     * @param clientPassword        The user password passed into the program
     *
     */
    MPTSGlobalVariables(String clientUserID, String clientPassword) {
        JDBCMPTSConnection  = new JDBCEvents() ;

        limitPrice          = new JTextField(10) ;
        securityGreyedBox   = new JTextField(25) ;

        ISINCode            = new JTextField(20) ;
        latestPrice         = new JTextField(6) ;
        EPICCode            = new JTextField(6) ;
        sector              = new JTextField(15) ;
        originatingCountry  = new JTextField(5) ;
        marketCapitalisation= new JTextField(10) ;
        high52Week          = new JTextField(6) ;
        low52Week           = new JTextField(6) ;
        companyURL          = new JTextField(30) ;
        shareOrBond         = new JTextField(5) ;

        quantity            = new JTextField(10) ;
        quantity.addActionListener(this) ;
        feeCharged          = new JTextField("20.00", 10) ;
        notesOfTrade        = new JTextField(30) ;

        presentValueOfPortfolio = new JTextField(15) ;
        totalGainLossOfPortfolio = new JTextField(15) ;
        latestTradeDateOfPortfolio = new JTextField(12) ;

        statusArea = new JTextArea("Welcome to the Market Prediction Trading System",1,1) ;
        statusArea.setEditable(false) ;
        statusArea.setBackground(Color.LIGHT_GRAY) ;

        userID              = new String(clientUserID) ;
        userPassword        = new String(clientPassword) ;
        userLoggedIn        = userID.equals("default") ? false : true ;

        securityGreyedBox.setEditable(false) ;

        ISINCode.setEditable(false) ;
        latestPrice.setEditable(false) ;
        EPICCode.setEditable(false) ;
        sector.setEditable(false) ;
        originatingCountry.setEditable(false) ;
        marketCapitalisation.setEditable(false) ;
        high52Week.setEditable(false) ;
        low52Week.setEditable(false) ;
        companyURL.setEditable(false) ;
        shareOrBond.setEditable(false) ;

        feeCharged.setEditable(false) ;

        presentValueOfPortfolio.setEditable(false) ;
        totalGainLossOfPortfolio.setEditable(false) ;
        latestTradeDateOfPortfolio.setEditable(false) ;

        initialiseAvailableLists() ;

    } // end of constructor

    /**
     * Returns the current ISIN code
     * @return            the ISIN code of type <code>JTextField</code>.
     */
    public JTextField getISINCode() { return ISINCode ; }

    /**
     * Returns the current company website address
     * @return            the company website address of type <code>JTextField</code>.
     */
    public JTextField getCompanyURL() { return companyURL ; }

    /**
     * Returns the type of security
     * @return            the type of the security of type <code>JTextField</code>.
     */
    public JTextField getShareOrBond() { return shareOrBond ; }

    /**
     * Returns the current latest price
     * @return            the latest price of type <code>JTextField</code>.
     */
    public JTextField getLatestPrice() { return latestPrice ; }

    /**
     * Returns the current EPIC code
     * @return            the EPIC code of type <code>JTextField</code>.
     */
    public JTextField getEPICCode() { return EPICCode ; }

    /**
     * Returns the current industry sector
     * @return            the industry sector of type <code>JTextField</code>.
     */
    public JTextField getSector() { return sector ; }

    /**
     * Returns the current originating country
     * @return            the originating country of type <code>JTextField</code>.
     */
    public JTextField getOriginatingCountry() { return originatingCountry ; }

    /**
     * Returns the current market capitalisation
     * @return            the market capitalisation of type <code>JTextField</code>.
     */
    public JTextField getMarketCapitalisation() { return marketCapitalisation ; }

    /**
     * Returns the current 52 week high price
     * @return            the 52 week high price of type <code>JTextField</code>.
     */
    public JTextField getHigh52Week() { return high52Week ; }

    /**
     * Returns the current 52 week low price
     * @return            the 52 week low price of type <code>JTextField</code>.
     */
    public JTextField getLow52Week() { return low52Week ; }

    /**
     * Sets the current ISIN code
     * @param settingValue            the ISIN code to be set
     */
    public void setISINCode(String settingValue) { ISINCode.setText(settingValue) ; }

    /**
     * Sets the current security company website
     * @param settingValue            the website url link to be set
     */
    public void setCompanyURL(String settingValue) { companyURL.setText(settingValue) ; }

    /**
     * Sets the current share or bond
     * @param settingValue            the share or bond indicator to be set
     */
    public void setShareOrBond(String settingValue) { shareOrBond.setText(settingValue) ; }

    /**
     * Sets the current latest price
     * @param settingValue            the latest price to be set
     */
    public void setLatestPrice(String settingValue) { latestPrice.setText(settingValue) ; }

    /**
     * Sets the current EPIC code
     * @param settingValue            the EPIC code to be set
     */
    public void setEPICCode(String settingValue) { EPICCode.setText(settingValue) ; }

    /**
     * Sets the current industry sector
     * @param settingValue            the industry sector to be set
     */
    public void setSector(String settingValue) { sector.setText(settingValue) ; }

    /**
     * Sets the current originating country
     * @param settingValue            the originating country to be set
     */
    public void setOriginatingCountry(String settingValue) { originatingCountry.setText(settingValue) ; }

    /**
     * Sets the current market capitalisation
     * @param settingValue            the market capitalisation to be set
     */
    public void setMarketCapitalisation(String settingValue) { marketCapitalisation.setText(settingValue) ; }

    /**
     * Sets the current 52 week high
     * @param settingValue            the 52 week high to be set
     */
    public void setHigh52Week(String settingValue) { high52Week.setText(settingValue) ; }

    /**
     * Sets the current 52 week low
     * @param settingValue            the 52 week low to be set
     */
    public void setLow52Week(String settingValue) { low52Week.setText(settingValue) ; }

    /**
     * Returns the status area text box
     * @return             the status area of type <code>JTextArea</code>.
     */
    public JTextArea getStatusArea() { return statusArea ; }

    /**
     * Sets the current status area text box
     * @param settingValue            the status area text to be set
     */
    public void setStatusArea(String settingValue) { statusArea.setText(settingValue) ; }

    /**
     * Returns the user ID currently logged in
     * @return             the user ID currently logged in.
     */
    public String getUserID() { return userID ; }

    /**
     * Returns the password of the user ID currently logged in
     * @return             the password of the user ID currently logged in.
     */
    public String getUserPassword() { return userPassword ; }

    /**
     * Identifies whether or not a user is logged in.
     * @return             <code>true</code> if a user is currently logged into the
     *                    MPTS system from this software; <code>false</code> otherwise.
     */
    public boolean getUserLoggedIn() { return userLoggedIn ; }

    /**
     * Sets the current user ID
     * @param clientUserID            the user name to be set.
     */
    public void setUserID(String clientUserID) { userID = clientUserID ; }

    /**
     * Automatically sets whether or not a user is logged into the system
     */
    public void setUserLoggedIn() { userLoggedIn = userID.equals("default") ? false : true ; }

    /**
     * Returns the current security selected
     * @return             the current security selected
     */
    public JTextField getSecurityGreyedBox() { return securityGreyedBox ; }

    /**
     * Returns the current limit price selected
     * @return             the limit price selected
     */
    public JTextField getLimitPrice() { return limitPrice ; }

    /**
     * Sets the current security in the greyed box
     * @param settingValue            the security to be set
     */
    public void setSecurityGreyedBox(String settingValue) { securityGreyedBox.setText(settingValue) ; }

    /**
     * Sets the current limit price
     * @param settingValue            the limit price to be set
     */
    public void setLimitPrice(String settingValue) { limitPrice.setText(settingValue) ; }

    /**
     * Returns the trade types available to the user
     * @return             a JComboBox list of available trade types
     */
    public JComboBox getAvailableTradeTypes() { return availableTradeTypes ; }

    /**
     * Returns the sale or purchase options available to the user
     * @return             a JComboBox list of available sale or purchase options
     */
    public JComboBox getAvailableSaleOrPurchase() { return availableSaleOrPurchase ; }

    /**
     * Returns the securities available to the user
     * @return             a JComboBox list of available securities
     */
    public JComboBox getAvailableSecurities() { return availableSecurities ; }

    /**
     * Returns the quantity of stock currently set
     * @return             the quantity of stock currently set
     */
    public JTextField getQuantity() { return quantity ; }

    /**
     * Returns the fee charged to the user
     * @return             the fee charged to the client
     */
    public JTextField getFeeCharged() { return feeCharged ; }

    /**
     * Returns the notes of a trade that the user has defined
     * @return             trade notes created by the user
     */
    public JTextField getNotesOfTrade() { return notesOfTrade ; }

    /**
     * Sets the current quantity
     * @param settingValue            the quantity to be set
     */
    public void setQuantity(String settingValue) { quantity.setText(settingValue) ; }

    /**
     * Sets the current fee charged
     * @param settingValue            the fee the user is to be charged
     */
    public void setFeeCharged(String settingValue) { feeCharged.setText(settingValue) ; }

    /**
     * Sets the current note of a trade
     * @param settingValue            the notes the user has typed about this trade
     */
    public void setNotesOfTrade(String settingValue) { notesOfTrade.setText(settingValue) ; }

    /**
     * Returns (does not calculate) the present value of the current portfolio
     * @return             the present value of the current portfolio
     */
    public JTextField getPresentValueOfPortfolio() { return presentValueOfPortfolio ; }

    /**
     * Returns (does not calculate) the total gain or loss of the current portfolio
     * @return             the gain or loss value of the current portfolio
     */
    public JTextField getTotalGainLossOfPortfolio() { return totalGainLossOfPortfolio ; }

    /**
     * Returns (does not calculate) the latest trade date of the current portfolio
     * @return             the latest trade date of the current portfolio
     */
    public JTextField getLatestTradeDateOfPortfolio() { return latestTradeDateOfPortfolio ; }

    /**
     * Sets (does not calculate) the present value of the portfolio
     * @param settingValue            the portfolio present value to be set
     */
    public void setPresentValueOfPortfolio(String settingValue) { presentValueOfPortfolio.setText(settingValue) ; }

    /**
     * Sets (does not calculate) the current gain or loss of the portfolio
     * @param settingValue            the total gain or loss to be set
     */
    public void setTotalGainLossOfPortfolio(String settingValue) { totalGainLossOfPortfolio.setText(settingValue) ; }

    /**
     * Sets (does not calculate) the latest trade date on the portfolio
     * @param settingValue            the latest trade date to be set
     */
    public void setLatestTradeDateOfPortfolio(String settingValue) { latestTradeDateOfPortfolio.setText(settingValue) ; }

    //***********************************//
    // resetVariables function
    //***********************************//
    /**
     * Sets all variables back to initial values - with the exception of userID
     */
    public void resetVariables() {
        setSecurityGreyedBox(null) ;
        setISINCode(null) ;
        setLatestPrice(null) ;
        setEPICCode(null) ;
        setSector(null) ;
        setOriginatingCountry(null) ;
        setMarketCapitalisation(null) ;
        setHigh52Week(null) ;
        setLow52Week(null) ;
        setStatusArea(null) ;
        setLimitPrice(null) ;
        setQuantity(null) ;
        setNotesOfTrade(null) ;
        setPresentValueOfPortfolio(null) ;
        setTotalGainLossOfPortfolio(null) ;
        setLatestTradeDateOfPortfolio(null) ;

        availableTradeTypes.setSelectedIndex(0) ;
        availableSaleOrPurchase.setSelectedIndex(0) ;
        availableSecurities.setSelectedIndex(0) ;
    } // end of resetVariables function

    //***********************************//
    // initialiseAvailableLists function
    //***********************************//
    /**
     * Initialises all lists:<br>
     *
     * <ul>
     *   <li>availableTradeTypes
     *   <li>availableSaleOrPurchase
     *   <li>availableSecurities
     * </ul>
     */
    private void initialiseAvailableLists() {

        // Trade Types
        ///////////////////////////////////////
        String[] availableTradeTypesArray = { "", "At Best", "Limit" } ;
        availableTradeTypes = new JComboBox(availableTradeTypesArray) ;
        availableTradeTypes.setName("availableTradeTypes") ;
        availableTradeTypes.addActionListener(this) ;

        // Sale or Purchase
        ///////////////////////////////////////
        String[] availableSaleOrPurchaseArray = { "", "Buy", "Sell" } ;
        availableSaleOrPurchase = new JComboBox(availableSaleOrPurchaseArray) ;
        availableSaleOrPurchase.setName("availableSaleOrPurchase") ;

        // Securities
        ///////////////////////////////////////
        String[] availableSecuritiesArray = JDBCMPTSConnection.initialiseAvailableSecurities() ;
        availableSecurities = new JComboBox(availableSecuritiesArray) ;
        availableSecurities.setName("availableSecurities") ;
        availableSecurities.addActionListener(this) ;
    } // end of initialiseAvailableLists function



    //***********************************//
    // actionPerformed function
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
        // action handling for JComboBox
        /////////////////////////////////////////////

        if (classTypeOfTheAction == "javax.swing.JComboBox") {
            JComboBox eventSource = (JComboBox)source ;
            String choiceString ;

            // for Security
            if (eventSource.getName() == "availableSecurities") {
                securityGreyedBox.setText((String)eventSource.getSelectedItem()) ; // set the greyed box to the required security

                // reset all variables to become blank
                setISINCode("") ;
                setLatestPrice("") ;
                setEPICCode("") ;
                setSector("") ;
                setOriginatingCountry("") ;
                setMarketCapitalisation("") ;
                setHigh52Week("") ;
                setLow52Week("") ;
                setShareOrBond("") ;
                setCompanyURL("") ;
            }
            // for limitPrice
            if (eventSource.getName() == "availableTradeTypes") {
                if (eventSource.getSelectedItem() == "Limit") {
                    limitPrice.setEditable(true) ; // ungrey limit price selection box and require an entry
                }
                else if (eventSource.getSelectedItem() == "At Best" ){ // trade type is at best
                    setLimitPrice(null) ;
                    limitPrice.setEditable(false) ; // grey out limit price box
                }
                else {
                    limitPrice.setEditable(true) ; // for clean code dealing with all possible combinations
                }
            }

        } // end item event handling for JComboBoxes

    } // end of actionPerformed function


    //***********************************//
    // textValueChanged function
    //***********************************//
    /**
     *
     * The implementation of TextEvent
     * @param e            The detected text value changed
     *
     */
    public void textValueChanged(TextEvent e) {

        Object source = e.getSource() ;

        String classTypeOfTheAction = new String() ;
        classTypeOfTheAction = source.getClass().getName() ;

        System.out.print(e +"\n\n") ;
        System.out.print(source +"\n\n") ;

    } // end of textValueChanged function
} //EOF