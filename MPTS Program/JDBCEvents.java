import java.io.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.util.* ;
import java.util.Vector ;
import java.util.GregorianCalendar ;
import java.util.Calendar ;
import java.sql.* ;
import java.text.* ;
import java.text.SimpleDateFormat ;
import javax.swing.* ;
import javax.swing.border.* ;
import javax.swing.filechooser.* ;
import javax.swing.filechooser.FileFilter ;

/**
 *
 * In general, this class (JDBCEvents) is the only class which can make
 * connections to the Central Database (with the exception of IPS).  Additionally,
 * it handles button events.
 *
 * @author        Jonathan K. W. Lee
 * @version       1.0
 * @see           ExtensionFilter
 * @see           IPS
 * @see           SystemWindow
 *
 */
public class JDBCEvents {

    // class variables
    private static ResultsModel model ;
    private static Connection   liveConnection ;
    private static Statement    stmt ;
    private static String       currentSQLQuery ;
    private static ResultSet    rs ;

    //***********************************//
    // Constructor
    //***********************************//
    /**
     * Initialises variables, creating a new ResultsModel.
     */
    JDBCEvents() {
        model = new ResultsModel() ;
        testDatabaseConnection() ; // tests the driver, and database validity
    } // end of constructor

    //***********************************//
    // checkUsernameAndPassword function           //
    //***********************************//
    /**
     * Obtains the up to date portfolio of the user ID specified.
     * @param username     the user name to check
     * @param password     the password to check
     * @return             <code>true</code> if username and password combination is valid, <code>false</code> otherwise
     */
    public boolean checkUsernameAndPassword(String username, String password) {
        // true if valid
        // false otherwise

        model = new ResultsModel() ;  // uses the results model class to get the portfolio details

        openConnection() ;
        try {
            stmt = liveConnection.createStatement();
            currentSQLQuery = new String();
            currentSQLQuery = "SELECT * FROM Clients WHERE ClientID = '" + username + "' AND Password = '" + password + "'" ;
//            System.out.println(currentSQLQuery) ;
            rs = stmt.executeQuery(currentSQLQuery);
            model.setResultSet(rs) ;
        }
        catch(SQLException e) {
            System.out.println("Failed in validating username and password") ;
        }
        closeConnection() ;

        JTable validatingTable = new JTable(model) ;

        if (validatingTable.getRowCount() <= 0) { // no record of this username/password combination
            return false ;
        }
        else if (validatingTable.getRowCount() > 1) { // more than 1 record of this username/password combination therefore something went wrong at JBank & Co registration process
            JOptionPane.showMessageDialog(null, "There was a problem in validating your username and login.\nPlease contact JBank & Co client services for more information", "Username and password validation", JOptionPane.ERROR_MESSAGE) ;
            return false ;
        }
        else { // number of rows is 1 so this is valid
            return true ;
        }
    } // end checkUsernameAndPassword function

    //***********************************//
    // getMyPortfolio function           //
    //***********************************//
    /**
     * Obtains the up to date portfolio of the user ID specified.
     * @param UserID    the user name of the portfolio to be retrieved
     * @return             a JTable containing the portfolio
     */
    public JTable getMyPortfolio(String UserID) {
        model = new ResultsModel() ;  // uses the results model class to get the portfolio details

        openConnection() ;
        try {
            stmt = liveConnection.createStatement();
            currentSQLQuery = new String();
            currentSQLQuery = "SELECT MyPortfolio.ISIN As [ISIN], Securities.StockExchangeSymbol As [Ticker Code], Securities.SecurityName As [Stock Name], MyPortfolio.LatestDateOfPurchase As [Purchase Date], MyPortfolio.QuantityOfISIN As [Purchase Qty], MyPortfolio.PurchasePriceAtLatestDateOfPurchase As [Purchase Price (p)], Securities.LatestPrice As [Current Price (p)], MyPortfolio.UserNotes As [Trade Notes] FROM MyPortfolio, Securities WHERE MyPortfolio.ClientID = '" + UserID + "' AND MyPortfolio.ISIN = Securities.ISIN ORDER BY Securities.SecurityName ASC";
            rs = stmt.executeQuery(currentSQLQuery);
            model.setResultSet(rs) ;
        }
        catch(SQLException e) {
            System.out.println("Failed obtaining information for MyPortfolio tab: " + e.getMessage());
        }

        closeConnection() ;

        JTable MyPortfolioTable = new JTable(model) ;
        // now manipulate the table adding required columns and extra calculations
        return MyPortfolioTable ; // of type ResultsModel
    } // end of getMyPortfolio function

    //***********************************//
    // getPendingTrades function         //
    //***********************************//
    /**
     * Obtains the up to date pending trades information of the user ID specified.
     * @param UserID    the user name of the pending trades information to be retrieved
     * @return             a JTable containing the pending trades
     */
    public JTable getPendingTrades(String UserID) {
        model = new ResultsModel() ;  // uses the results model class to get the portfolio details

        openConnection() ;

        try {
            stmt = liveConnection.createStatement();
            currentSQLQuery = new String();
            currentSQLQuery = "SELECT PendingRequests.DateAndTimeOfInstruction As [Date/Time of Instruction], PendingRequests.ISIN As [ISIN], Securities.StockExchangeSymbol As [Ticker Code], Securities.SecurityName As [Stock Name], PendingRequests.BuyOrSell As [Instruction Type], PendingRequests.Quantity As [Quantity], PendingRequests.TradeType As [Trade Type], PendingRequests.LimitPrice As [Limit Price], PendingRequests.FeeCharged As [Fee Charged], PendingRequests.UserNotes As [User Notes] FROM PendingRequests, Securities WHERE PendingRequests.ClientID = '" + UserID + "' AND PendingRequests.ISIN = Securities.ISIN ORDER BY PendingRequests.DateAndTimeOfInstruction ASC";
            rs = stmt.executeQuery(currentSQLQuery);
            model.setResultSet(rs) ;
        }
        catch(SQLException e) {
            System.out.println("Failed obtaining information for PendingRequests tab: " + e.getMessage());
        }

        closeConnection() ;

        JTable PendingTradesTable = new JTable(model) ;
        return PendingTradesTable ; // of type ResultsModel
    } // end of getPendingTrades function

    //***********************************//
    // whenLoginRequested function       //
    //***********************************//
    /**
     * This function is called when "Login" is requested from the "File" menu.  The
     * function verifies a user login
     * @param systemWindowGlobalVariables    global variables for interaction with the system
     * @return             the username if the login is verified; <code>null</code> otherwise
     */
    public String whenLoginRequested(MPTSGlobalVariables systemWindowGlobalVariables) {
        // display dialog box and password to login
        String userLoggingIn = new String() ;
        String attemptedPassword = new String() ;

        userLoggingIn = JOptionPane.showInputDialog(null, "Please enter your MPTS login:", "Login", JOptionPane.OK_CANCEL_OPTION) ;
        attemptedPassword = JOptionPane.showInputDialog(null, "Please enter your MPTS password:", "Login", JOptionPane.OK_CANCEL_OPTION) ;

        // check the login is valid
        if (checkUsernameAndPassword(userLoggingIn, attemptedPassword)) { // valid
            // if you got here, password and username concur so the username can be returned
            return userLoggingIn ;
        }
        else { // invalid
            JOptionPane.showMessageDialog(null, "The username and password combination which you entered do not match.  Please try again.\nIf this problem persists, please contact Client Services", "Username and password validation", JOptionPane.ERROR_MESSAGE) ;
            return null ;
        }
    } // end of whenLoginRequested function

    //***********************************//
    // whenGetDetailsPressed function    //
    //***********************************//
    /**
     * This function is called when a security has been selected and the details
     * require to be retrieved.  In calling this function, details will be retrieved
     * from the Central Database and set into the required text fields.
     * @param systemWindowGlobalVariables    global variables for interaction with the system
     */
    public void whenGetDetailsPressed(MPTSGlobalVariables systemWindowGlobalVariables) {
        openConnection() ;
        try {
            stmt = liveConnection.createStatement() ;
            currentSQLQuery = new String() ;
            currentSQLQuery = "SELECT * FROM Securities WHERE SecurityName = '" + systemWindowGlobalVariables.getAvailableSecurities().getSelectedItem() + "'" ;
            rs = stmt.executeQuery(currentSQLQuery) ;

            try { // store the results in variables
                while (rs.next()) {
                    String ResultA = rs.getString("ISIN") ;
                    String ResultB = rs.getString("SecurityName") ; // currently unused
                    String ResultC = rs.getString("StockExchangeSymbol") ;
                    String ResultD = rs.getString("OriginatingCountry") ;
                    String ResultE = rs.getString("ShareOrBond") ;
                    String ResultF = rs.getString("LatestPrice") ;
                    String ResultG = rs.getString("Sector") ;
                    String ResultH = rs.getString("CompanyURL") ;
                    String ResultI = rs.getString("MarketCapitalisation") ;
                    String ResultJ = rs.getString("52WeekHigh") ;
                    String ResultK = rs.getString("52WeekLow") ;

                    systemWindowGlobalVariables.setISINCode(ResultA) ;
                    systemWindowGlobalVariables.setMarketCapitalisation(ResultI) ;
                    systemWindowGlobalVariables.setHigh52Week(ResultJ) ;
                    systemWindowGlobalVariables.setLow52Week(ResultK) ;
                    systemWindowGlobalVariables.setLatestPrice(ResultF) ;
                    systemWindowGlobalVariables.setSector(ResultG) ;
                    systemWindowGlobalVariables.setOriginatingCountry(ResultD) ;
                    systemWindowGlobalVariables.setEPICCode(ResultC) ;
                    systemWindowGlobalVariables.setCompanyURL(ResultH) ;
                    systemWindowGlobalVariables.setShareOrBond(ResultE) ;
                }
            }
            catch(SQLException e) {
                System.out.print("Failed to set security details into variables: " + e.getMessage()) ;
            }
        }
        catch(SQLException e2) {
            System.out.print("Failed obtaining information for PendingRequests tab: " + e2.getMessage()) ;
        }
        closeConnection() ;

    } // end of whenGetDetailsPressed function

    //*******************************************//
    // whenRemovePendingOrderPressed function    //
    //*******************************************//
    /**
     * This function is called when there is a request to remove a pending order.
     * When selected, the function verifies unary trade removal and then requests
     * user confirmation.
     * @param ReturnedPendingTradesTable    the pending trades table from which to remove the trade
     * @param systemWindowGlobalVariables    global variables for interaction with the system
     */
    public void whenRemovePendingOrderPressed(JTable ReturnedPendingTradesTable, MPTSGlobalVariables systemWindowGlobalVariables) {
        // check if a single row is selected - if not inform the user to select a row
        if (ReturnedPendingTradesTable.getSelectedRowCount() != 1) {
            JOptionPane.showMessageDialog(null, "In removing Pending Orders, a single row must be selected to be removed.\nPlease select a single row and try again", "Remove Pending Order Confirmation", JOptionPane.INFORMATION_MESSAGE) ;
            return ;
        }

        // selecting the first row is index 0
//        System.out.println("Selected row: " + ReturnedPendingTradesTable.getSelectedRow()) ;
//        System.out.println("Selected column: " + ReturnedPendingTradesTable.getSelectedColumn()) ;

        // make final confirmation with user as to remove this order or not
        String finalConfirmationMsg = new String("You have requested to remove this pending order.\n\nPlease confirm click on OK to continue\nOn clicking Ok, you agree to our terms and conditions\nAfter removing this order, please click on Refresh") ;
        int n = JOptionPane.showConfirmDialog(null, finalConfirmationMsg, "Remove Pending Order Confirmation", JOptionPane.OK_CANCEL_OPTION) ;

        if (n == JOptionPane.OK_OPTION) { // remove the order
            String dateToDelete ;
            dateToDelete  = ReturnedPendingTradesTable.getValueAt(ReturnedPendingTradesTable.getSelectedRow(),0).toString().substring(8,10) + '/' ;
            dateToDelete += ReturnedPendingTradesTable.getValueAt(ReturnedPendingTradesTable.getSelectedRow(),0).toString().substring(5,7) + '/' ;
            dateToDelete += ReturnedPendingTradesTable.getValueAt(ReturnedPendingTradesTable.getSelectedRow(),0).toString().substring(0,4) + ' ' ;
            dateToDelete += ReturnedPendingTradesTable.getValueAt(ReturnedPendingTradesTable.getSelectedRow(),0).toString().substring(11,16) ;

            System.out.println(dateToDelete) ;

            // connect to the database, remove the pending trade by using primary key searches
            openConnection() ;
            try {
                stmt = liveConnection.createStatement();
                currentSQLQuery = new String() ;
                currentSQLQuery = "DELETE * FROM PendingRequests WHERE ISIN = '" + ReturnedPendingTradesTable.getValueAt(ReturnedPendingTradesTable.getSelectedRow(),1).toString() + "' AND ClientID = '" + systemWindowGlobalVariables.getUserID() + "' AND DateAndTimeOfInstruction like '%" + dateToDelete + "%' AND BuyOrSell = '" +  ReturnedPendingTradesTable.getValueAt(ReturnedPendingTradesTable.getSelectedRow(),4).toString() + "'" ;
//                System.out.print("\nThe query is:\n" + currentSQLQuery+ "\n");
                stmt.executeQuery(currentSQLQuery);
            }
            catch(SQLException e) {
//                System.out.print("\nProblem removing Pending Order:\n" + e + "\n") ;
            }
            closeConnection() ;
        }
        // else do nothing
    } // end of whenRemovePendingOrderPressed function

    //*******************************************//
    // whenExportPressed function                //
    //*******************************************//
    /**
     * This function is called when there is a request to export (save) a table
     * to an external file (usually csv - comma separated variables).  When
     * selected, the function shows confirmation boxes and saves the data to a
     * text format adding a comma to provide for csv format.
     * @param ReturnedTable                    the table to export to an external file
     * @param systemWindowGlobalVariables    global variables for interaction with the system
     */
    public void whenExportPressed(JTable ReturnedTable, MPTSGlobalVariables systemWindowGlobalVariables) {
        final JFileChooser fc = new JFileChooser();
        ExtensionFilter MPTSFileFilter ;

        MPTSFileFilter = new ExtensionFilter(".txt", "Text Files (*.txt)") ;
        fc.addChoosableFileFilter(MPTSFileFilter) ;
        fc.setFileFilter(MPTSFileFilter) ;
        MPTSFileFilter = new ExtensionFilter(".csv", "Comma Separated Values (*.csv)") ;
        fc.addChoosableFileFilter(MPTSFileFilter) ;
        fc.setFileFilter(MPTSFileFilter) ;

        int approveReturnValue = fc.showSaveDialog(null) ;
        int confirmOverwriteReturnValue = JOptionPane.YES_OPTION ;

        if (approveReturnValue == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile() ;
            System.out.println(file.getName()) ;

            if (file.exists()) { // file overwrite warning
//                confirmOverwriteReturnValue = JOptionPane.showInternalConfirmDialog(fc, "The file already exists.\nDo you want to replace the existing file?", "Export Data", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) ;
                confirmOverwriteReturnValue = JOptionPane.showConfirmDialog(null, "The file already exists.\nDo you want to replace the existing file?", "Export Data", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) ;
            }

            // if it is okay to overwrite, do the following, otherwise do nothing
            if (confirmOverwriteReturnValue == JOptionPane.YES_OPTION) {
                try {
                    systemWindowGlobalVariables.setStatusArea("Saving: " + file.getName()) ;

                    DataOutputStream savingFile = new DataOutputStream(new FileOutputStream(file)) ;

                    for (int i=0 ; i < ReturnedTable.getColumnCount() ; i++) {
                        savingFile.writeBytes(ReturnedTable.getColumnName(i).toString()) ;
                        savingFile.writeBytes(",") ;
                    }
                    savingFile.writeBytes("\n") ;

                    for (int y=0 ; y < ReturnedTable.getRowCount() ; y++) {
                        for (int i=0 ; i < ReturnedTable.getColumnCount() ; i++) {
                            try {
                                savingFile.writeBytes(ReturnedTable.getValueAt(y,i).toString()) ;
                            }
                            catch(NullPointerException e) { } // do nothing for null values
                            savingFile.writeBytes(",") ;
                        }
                        savingFile.writeBytes("\n") ;
                    }
                    savingFile.close() ;
                }
                catch(IOException e) {
                    System.out.print("Error attempting to save file: " + e) ;
                }
            }
        }
        else { systemWindowGlobalVariables.setStatusArea("Save command cancelled") ; }
    } // end of whenExportPressed function

    //*******************************************//
    // whenPrintPendingTradesPressed function //
    //*******************************************//
    /**
     * This function is called when there is a request to print a table.
     * Currently this functionality does not work
     * @param ReturnedPendingTradesTable    the table to print
     * @param systemWindowGlobalVariables    global variables for interaction with the system
     */
    public void whenPrintPendingTradesPressed(JTable ReturnedPendingTradesTable, MPTSGlobalVariables systemWindowGlobalVariables) {

    } // end of whenPrintPendingTradesPressed function

    //*******************************************//
    // whenPrintMyPortfolioPressed function //
    //*******************************************//
    /**
     * This function is called when there is a request to print a table.
     * Currently this functionality does not work
     * @param MyReturnedPortfolioTable        the table to print
     * @param systemWindowGlobalVariables    global variables for interaction with the system
     */
    public void whenPrintMyPortfolioPressed(JTable MyReturnedPortfolioTable, MPTSGlobalVariables systemWindowGlobalVariables) {

    } // end of whenPrintMyPortfolioPressed function

    //*******************************************//
    // whenGetMyPortfolioDetailsPressed function //
    //*******************************************//
    /**
     * This function is called when My Portfolio summary details are requested.
     * Currently this function calculates three things:
     *
     * <ul>
     *   <li>Present Value of the portfolio
     *   <li>Total gain or loss of portfolio to date
     *   <li>Latest trade date
     * </ul>
     * @param MyReturnedPortfolioTable       the My Portfolio table to be calculated
     * @param systemWindowGlobalVariables    global variables for interaction with the system
     */
    public void whenGetMyPortfolioDetailsPressed(JTable MyReturnedPortfolioTable, MPTSGlobalVariables systemWindowGlobalVariables) {
        // calculate the present value of the portfolio and set
        double cumulativeTotal = 0.0 ;
        double quantity      = 0.0 ;
        double currentPrice    = 0.0 ;

        for (int i=0 ; i < MyReturnedPortfolioTable.getRowCount() ; i++) {
            currentPrice  = Double.parseDouble((String)MyReturnedPortfolioTable.getValueAt(i,6)) ; // row, column
            quantity      = Double.parseDouble((String)MyReturnedPortfolioTable.getValueAt(i,4)) ;
            cumulativeTotal += (currentPrice * quantity) ;
        }
        cumulativeTotal /= 100 ; // to convert to £
        systemWindowGlobalVariables.setPresentValueOfPortfolio(Double.toString(cumulativeTotal)) ;

        // calculate the total gain/loss of the portfolio and set
        /* the mathematics is...
            for each row...
            (current price - purchase price) * quantity
            6                 5                   4
        */

        double totalGainLoss = 0.0 ;
               quantity      = 0.0 ;
               currentPrice  = 0.0 ;
        double purchasePrice = 0.0 ;

        for (int i = 0 ; i < MyReturnedPortfolioTable.getRowCount() ; i++) {
            currentPrice  = Double.parseDouble((String)MyReturnedPortfolioTable.getValueAt(i,6)) ; // row, column
            purchasePrice = Double.parseDouble((String)MyReturnedPortfolioTable.getValueAt(i,5)) ;
            quantity      = Double.parseDouble((String)MyReturnedPortfolioTable.getValueAt(i,4)) ;

//            System.out.println(MyReturnedPortfolioTable.getValueAt(i,4)) ;
            totalGainLoss = totalGainLoss + ((currentPrice-purchasePrice)*quantity) ;
//            System.out.println(totalGainLoss) ;
        }
        totalGainLoss /= 100 ; // to convert to £
        systemWindowGlobalVariables.setTotalGainLossOfPortfolio(Double.toString(totalGainLoss)) ;

        // calculate the latest trade date and set the value
        try {
            java.util.Date maxDate       = new java.util.Date() ;
            java.util.Date dateToCompare = new java.util.Date() ;
            SimpleDateFormat tempDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
            maxDate = tempDateFormat.parse((String)MyReturnedPortfolioTable.getValueAt(1,3)) ; // just take the first date to initialise this date

            for (int i = 0 ; i < MyReturnedPortfolioTable.getRowCount() ; i++) {
                dateToCompare = tempDateFormat.parse((String)MyReturnedPortfolioTable.getValueAt(i,3)) ;
                if (dateToCompare.after(maxDate)) {    maxDate = dateToCompare ; }
//                System.out.println(maxDate) ;
            }
            systemWindowGlobalVariables.setLatestTradeDateOfPortfolio(maxDate.toString()) ;
        }
        catch(ParseException e) {
            System.out.println("There was a problem converting the date string from the database, into a Date Object") ;
        }
    } // end of whenGetMyPortfolioDetailsPressed function

    //***********************************//
    // whenExecutionPressed function     //
    //***********************************//
    /**
     * This function is called when a trade execution is requested.  I.e. a "buy"
     * or "sell" request.
     *
     * Additionally, the function checks all inputted data to verify data integrity
     * (returning and closing the function if a check was failed).
     * If all checks have been passed, the function inserts the pending request into
     * the Order Book.
     *
     * @param systemWindowGlobalVariables    global variables for interaction with the system
     */
    public void whenExecutionPressed(MPTSGlobalVariables systemWindowGlobalVariables) {
        whenGetDetailsPressed(systemWindowGlobalVariables) ; // get up to date information

        GregorianCalendar currentCalendar = new GregorianCalendar() ;
        String DateAndTimeOfInstruction = new String() ;

        String ISIN = new String(systemWindowGlobalVariables.getISINCode().getText()) ;
        String ClientID = new String(systemWindowGlobalVariables.getUserID()) ;
               DateAndTimeOfInstruction = String.valueOf(currentCalendar.get(Calendar.DATE)) + "/" + String.valueOf(currentCalendar.get(Calendar.MONTH)+1) + "/" + String.valueOf(currentCalendar.get(Calendar.YEAR)) + " " + String.valueOf(currentCalendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(currentCalendar.get(Calendar.MINUTE)) + ":" + String.valueOf(currentCalendar.get(Calendar.SECOND)) ;         // set the date and time format for the clock.  Format: DD/MM/YYYY HH:MM:SS
        String BuyOrSell = new String((String)systemWindowGlobalVariables.getAvailableSaleOrPurchase().getSelectedItem()) ;
        String Quantity = new String(systemWindowGlobalVariables.getQuantity().getText()) ;
        String TradeType = new String((String)systemWindowGlobalVariables.getAvailableTradeTypes().getSelectedItem()) ;
        String LimitPrice = new String(systemWindowGlobalVariables.getLimitPrice().getText()) ;
        String FeeCharged = new String(systemWindowGlobalVariables.getFeeCharged().getText()) ;
        String UserNotes = new String(systemWindowGlobalVariables.getNotesOfTrade().getText()) ;

        int    QuantityInteger = 0 ;
        double LimitPriceDouble = 0.0 ;
        double FeeChargedDouble = Double.parseDouble(FeeCharged) ;

        // printing out the variables for testing
/*        System.out.print("\n\t----------------------------------------\n") ;
        System.out.print("\tISIN:\t\t\t\t" + ISIN + "\n") ;
        System.out.print("\tClientID:\t\t\t" + ClientID + "\n") ;
        System.out.print("\tDateAndTimeOfInstruction:\t" + DateAndTimeOfInstruction + "\n") ;
        System.out.print("\tBuyOrSell:\t\t\t" + BuyOrSell + "\n") ;
        System.out.print("\tQuantity:\t\t\t" + Quantity + "\n") ;
        System.out.print("\tTradeType:\t\t\t" + TradeType + "\n") ;
        System.out.print("\tLimitPrice:\t\t\t" + LimitPrice + "\n") ;
        System.out.print("\tFeeCharged:\t\t\t" + FeeCharged + "\n") ;
        System.out.print("\tUserNotes:\t\t\t" + UserNotes + "\n") ;
        System.out.print("\t----------------------------------------\n") ;
*/
        // run tests on data to check for validity
        if (!systemWindowGlobalVariables.getUserLoggedIn()) {
            JOptionPane.showMessageDialog(null, "You cannot currently make a sale or purchase execution because you are not logged in.\nPlease log into the system by selecting Login from the File menu", "Please Enter Login", JOptionPane.INFORMATION_MESSAGE) ;
            return ;
        }
        if (ISIN.equals("")) {
            JOptionPane.showMessageDialog(null, "Please select a security before attempting to execute a purchase or sale", "Select a stock", JOptionPane.INFORMATION_MESSAGE) ;
            return ;
        }
        if (BuyOrSell.equals("")) {
            JOptionPane.showMessageDialog(null, "Please select whether this is a sale or purchase", "Buy or Sell", JOptionPane.INFORMATION_MESSAGE) ;
            return ;
        }
        if (Quantity.equals("")) {
            JOptionPane.showMessageDialog(null, "Please enter a quantity of stock which you would like", "Number of Stock", JOptionPane.INFORMATION_MESSAGE) ;
            return ;
        }
        if (TradeType.equals("")) {
            JOptionPane.showMessageDialog(null, "Please select a Trade Type", "Trade Type", JOptionPane.INFORMATION_MESSAGE) ;
            return ;
        }
        if ((LimitPrice.equals("")) && (systemWindowGlobalVariables.getAvailableTradeTypes().getSelectedItem() == "Limit")) {
            JOptionPane.showMessageDialog(null, "Please enter a limit price since you have selected a trade type of \"Limit\".\n\nIf you wish to make a sale or purchase at the the best price possible, please select a Trade Type of \"At Best\"", "Trade Type", JOptionPane.INFORMATION_MESSAGE) ;
            return ;
        }

        try { // check that quantity is a whole number
            QuantityInteger = Integer.parseInt(Quantity) ;
        }
        catch(NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter a valid input for the quantity.\n\nIt seems that a character was typed or a decimal place was entered.\nQuantities must be a whole number without a decimal place.\nAdditionally, please ensure that the quantity is 10 digits long or less.\nPlease re-check the details", "Quantity", JOptionPane.INFORMATION_MESSAGE) ;
            return ;
        }

        // if a limit price is included in this transaction, check that limit price is a number
        if (systemWindowGlobalVariables.getAvailableTradeTypes().getSelectedItem() == "Limit") {
            // check that the limit price is a number
            try {
                LimitPriceDouble = Double.parseDouble(LimitPrice) ;
            }
            catch(NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter a valid input for the limit price.\n\nIt seems that a character was typed or more than 1 decimal place was entered.\nPlease recheck the details", "Limit Price", JOptionPane.INFORMATION_MESSAGE) ;
                return ;
            }

            // check that the limit price has less than 4 decimal places
            int decimalPlace = LimitPrice.indexOf('.') ;
            if (LimitPrice.substring(decimalPlace+1).length() > 4) {
                JOptionPane.showMessageDialog(null, "The limit price should contain a number with less than 4 decimal places.\n\nPlease recheck the details", "Limit Price", JOptionPane.INFORMATION_MESSAGE) ;
                return ;
            }
        }

        // do a check over the user notes that any invalid characters are converted to a valid character representation
        UserNotes = UserNotes.replaceAll("\'", "\u001B") ;

        // after the previous check, now ensure check that the user notes field is less than 250 characters in length
        if (UserNotes.length() > 250){
            JOptionPane.showMessageDialog(null, "Please shorten the length of the User Notes field to 250 characters.\nCurrently, the field contains " + UserNotes.length() + " characters", "User Notes", JOptionPane.INFORMATION_MESSAGE) ;
            return ;
        }


        // now make a final confirmation with the user
        String finalConfirmationMsg = new String("You have requested to " + BuyOrSell + " " + Quantity + " shares in " + (String)systemWindowGlobalVariables.getAvailableSecurities().getSelectedItem() + "\nThis trade will be done with a trade type of: " + (String)systemWindowGlobalVariables.getAvailableTradeTypes().getSelectedItem() + "\nMPTS will charge you a fee of £" + FeeCharged + " for this transaction.\n\nDo you want to continue with this order?\nOn clicking Yes, you agree to our terms and conditions") ;
        int n = JOptionPane.showConfirmDialog(null, finalConfirmationMsg, "Final Order Confirmation", JOptionPane.YES_NO_OPTION) ;

        if (n == JOptionPane.YES_OPTION) {
            // if you have reached this point, the data can be entered into the database
            openConnection() ;
            try {
                stmt = liveConnection.createStatement();
                currentSQLQuery = new String();
                currentSQLQuery = "INSERT INTO PendingRequests VALUES ('" + ISIN + "', '" + ClientID + "', '" + DateAndTimeOfInstruction + "', '" + BuyOrSell + "', " + QuantityInteger + ", '" + TradeType + "', " + LimitPriceDouble + ", " + FeeChargedDouble + ", '" + UserNotes + "')" ;
//                System.out.print("\nThe query is:\n" + currentSQLQuery+ "\n");
                stmt.executeQuery(currentSQLQuery);
            }
            catch(SQLException e) {
                System.out.print("\nProblem inserting purchase/sale request into the Central Database:\n***Note that if inserting a new trade, this error is normal as no resultset is produced***\n" + e + "\n") ;
            }
            closeConnection() ;

            // display a confirmation that the trade is now on the pending queue
            String tradeDoneMsg = new String("Your trade order has now been received by us.\n\nYou can view your order on the Pending Trades tab.\nThank you for placing this trade with us") ;
            JOptionPane.showMessageDialog(null, tradeDoneMsg, "Order Confirmation", JOptionPane.PLAIN_MESSAGE) ;

        } // otherwise window was closed, or NO_OPTION was selected - do nothing

        // refresh the system - this might not be possible

    } // end whenExecutionPressed function

    //***********************************//
    // initialiseAvailableSecurities function
    //***********************************//
    /**
     * This function initialises the entire list of available securities.
     * This list of available securities is populated by accessing the data
     * directly from the MPTS Central Database.
     * @return            an array of Strings which contain the security name
     */
    public String[] initialiseAvailableSecurities() {
        String[] availableSecurities = new String[0] ; // empty array of available securities
        ResultsModel temporaryResultsModelTable = new ResultsModel() ;
        JTable securityListTable ;

        openConnection() ;
        try {
            stmt = liveConnection.createStatement() ;
            currentSQLQuery = new String() ;
            currentSQLQuery = "SELECT DISTINCT ISIN, SecurityName FROM Securities ORDER BY SecurityName ASC" ;
            rs = stmt.executeQuery(currentSQLQuery) ;

            temporaryResultsModelTable.setResultSet(rs) ;
            securityListTable = new JTable(temporaryResultsModelTable) ;

            // count the results
            int j = securityListTable.getRowCount() ; // this includes a value for zero which in effect adds an extra count.  However, we need an extra count for the "blank" selection field.

            // set the array to the required size and reset the resultset cursor to the beginning
            availableSecurities = new String[j] ; // length obtained from resultsset
            availableSecurities[0] = new String("") ; // set the first security selection as "blank"

            // set the array with the results
            for (int i = 1 ; i < securityListTable.getRowCount() ; i++) { // begin at 1 as the first result has been set as "space"
                String ResultA = securityListTable.getValueAt(i,0).toString() ; // for ISIN
                String ResultB = securityListTable.getValueAt(i,1).toString() ; // for security name
//                 availableSecurities[i] = new String(ResultB + " - [" + ResultA + "]") ; // a different way of formatting the results
                 availableSecurities[i] = new String(ResultB) ;
            }
        }
        catch(SQLException e) {
            System.out.print("SQL Exception occurred: " + e.getMessage() + "\n");
        }
        closeConnection() ;

        return availableSecurities ;
    } // end of initialiseAvailableSecurities function

    //***********************************//
    // openConnection function           //
    //***********************************//
    /**
     * This function creates a live connection to the Central Database.
     */
    private void openConnection() {
        try {  // load the driver
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        }
        catch(ClassNotFoundException e) {
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
    // closeConnection function          //
    //***********************************//
    /**
     * This function closes a live connection to the Central Database.
     */
    private void closeConnection() {
        try {
            if( liveConnection != null) {
                liveConnection.close();
            }
        }
        catch(SQLException e) {
            System.err.print("Error in attempting to close connection to database:\n      " + e.getMessage() + "\n");
        }
    } // end of closeConnection function

    //***********************************//
    // testDatabaseConnection function   //
    //***********************************//
    /**
     * This function tests connection to the database
     */
    private void testDatabaseConnection() {
        try {  // load the driver
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        }
        catch(ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "There was an error in loading the database driver.  Please run Windows setup and install the sun.jdbc.odbc.JdbcOdbcDriver database driver.\n\nThe MPTS program will now exit", "Loading the Database Driver", JOptionPane.ERROR_MESSAGE) ;
            System.exit(1) ;
        }
        // test for a valid database connection
        try {  // make a connection
            liveConnection = DriverManager.getConnection("jdbc:odbc:CentralDatabase");
        }
        catch(SQLException e) {
            JOptionPane.showMessageDialog(null, "There was an error connecting to the Central Database.\nPlease ensure that you have a valid network/internet connection and try again.\n\nThe MPTS program will now exit", "Connection to Central Database", JOptionPane.ERROR_MESSAGE) ;
            System.exit(1) ;
        }

        // if got here, the database has been verified.  However, there is now a live connection so close it
        try {
            if( liveConnection != null) {
                liveConnection.close();
            }
        }
        catch(SQLException e) {
            System.err.print("Error in attempting to close connection to database:\n      " + e.getMessage() + "\n");
        }
    } // end of testDatabaseConnection function

} //EOF