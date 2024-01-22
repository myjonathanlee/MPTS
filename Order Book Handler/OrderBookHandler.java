import java.io.* ;
import java.sql.* ;
import javax.swing.* ;
import java.util.GregorianCalendar ;
import java.util.Calendar ;
import java.util.Timer ;
import java.util.TimerTask ;

/** This class frequently checks the order book and matches any trades
  * This piece of software is intended for use at MPTS company
  *
  */
public class OrderBookHandler {
    // class variables
    private ResultsModel model ;
    private Connection   LiveConnection ;
    private Statement    stmt ;
    private String       currentSQLQuery ;
    private ResultSet    rs ;

    private static String[][] latestPendingRequests ; // columns, rows

    private static String currentTime = new String() ;
    private static String currentLongTime = new String() ;

    JTable currentPendingRequests ;
    JTable comparisonPendingRequests ;


    public static void main(String[] args) { // main

        // create this class and prepare the data
        OrderBookHandler theClass = new OrderBookHandler() ;

        // start the order book handler - this doesn't end until Ok is pressed
        theClass.startOrderBookHandler() ;

        // this simply keeps the whole program alive until Ok is pressed
        JOptionPane.showMessageDialog(null, "Order Book Handler Program is now running\nClick OK to exit application", "Order Book Handler Program", JOptionPane.INFORMATION_MESSAGE ) ;
        System.exit(0) ;
    } //end main

    //***********************************//
    // constructor
    //***********************************//
    OrderBookHandler() {
        latestPendingRequests = new String[0][0] ; // columns, rows
    } // end of constructor

    //***********************************//
    // startOrderBookHandler function    //
    //***********************************//
    public void startOrderBookHandler() {
        // start the timer and check for time alignment every second
        Timer repetitionTimer ;
        TimerTask marketTimerTask = new TimerTask() {
                                        public void run() {
                                            checkPendingRequests() ;
                                        }
                                    } ;

        repetitionTimer = new Timer(true) ;
        repetitionTimer.scheduleAtFixedRate(marketTimerTask, 0, 1000) ; // 5000 msec = 5 seconds
    } // end of startOrderBookHandlerfunction

    //***********************************//
    // function checkPendingRequests     //
    //***********************************//
    public void checkPendingRequests() {
        // this function checks if data in the PendingRequests table
        // can be executed and inserted into the MyPortfolio table

        GregorianCalendar currentCalendar = new GregorianCalendar() ;

        currentCalendar.setTime(new java.util.Date(System.currentTimeMillis())) ;
        currentTime = String.valueOf(currentCalendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(currentCalendar.get(Calendar.MINUTE)) + ":00" ;    // set the date and time format for the clock.  Format: HH:MM:SS
        currentLongTime = "26/02/2003 " + String.valueOf(currentCalendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(currentCalendar.get(Calendar.MINUTE)) ; // set the date and time format for the clock.  Format: DD/MM/YYYY HH:MM:00
//        currentLongTime = String.valueOf(currentCalendar.get(Calendar.DATE)) + "/" + String.valueOf(currentCalendar.get(Calendar.MONTH)+1) + "/" + String.valueOf(currentCalendar.get(Calendar.YEAR)) + " " + String.valueOf(currentCalendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(currentCalendar.get(Calendar.MINUTE)) ; // set the date and time format for the clock.  Format: DD/MM/YYYY HH:MM:00
//        currentLongTime = String.valueOf(currentCalendar.get(Calendar.DATE)) + "/" + String.valueOf(currentCalendar.get(Calendar.MONTH)+1) + "/" + String.valueOf(currentCalendar.get(Calendar.YEAR)) ; // set the date and time format for the clock.  Format: DD/MM/YYYY HH:MM:SS
//        the second commented out currentLongTime should be used - uncomment this when the program is working properly

        // the dates need to be padded with zero's where appropriate... i.e. jan = 1 --> 01

        System.out.println("Time (sec set to 00):\t\t" + currentTime) ;
        System.out.println("LongTime (sec set to 00):\t" + currentLongTime) ;

        openConnection() ;
        try {
            ResultsModel temporaryResultsModelTable = new ResultsModel() ;

            stmt = LiveConnection.createStatement();
            currentSQLQuery = new String() ;
            currentSQLQuery = "SELECT * FROM PendingRequests WHERE DateAndTimeOfInstruction like '%" + currentLongTime + "%'" ;
//            System.out.print("\nCurrent SQL Query:\n" + currentSQLQuery + "\n");
            rs = stmt.executeQuery(currentSQLQuery);

            temporaryResultsModelTable.setResultSet(rs) ;
            currentPendingRequests = new JTable(temporaryResultsModelTable) ;

            // count the results
            int j = 0 ;
            int k = 0 ;
            j = currentPendingRequests.getRowCount() ; // this includes a value for zero which in effect adds an extra count.
            k = currentPendingRequests.getColumnCount() ; // this includes a value for zero which in effect adds an extra count.

//            System.out.println("results array has " + j + " rows and " + k + " columns") ;

            if (j != 0 && k != 0) {
                System.out.println("\n----------------------------------\nA new pending request was detected\n----------------------------------") ;
                latestPendingRequests = new String[k][j] ;

                // set the array with the results
                for ( int i = 0 ; i < currentPendingRequests.getRowCount() ; i++ ) { // begin at 1 as the first result has been set as "space"
                    for ( int h = 0 ; h < currentPendingRequests.getColumnCount() ; h++ ) {
                        try {
                            String currentResult = currentPendingRequests.getValueAt(i,h).toString() ;
                            latestPendingRequests[h][i] = new String(currentResult) ;
                        }
                        catch(NullPointerException e) {
                             latestPendingRequests[h][i] = new String() ;
                        }
                    }
                }
            }
            else { // the array is blank and there are no requests that need to be processed at this time
                closeConnection() ;
                return ;
            }
        }
        catch(SQLException e) {
            System.out.print("\nProblem selecting data from PendingRequests:\n" + e + "\n") ;
        }
        closeConnection() ;

        // if this point has been reached, the latestPendingRequests array is full and can now be tested...
        for (int i=0 ; i < latestPendingRequests[0].length ; i++ ) {
            if (latestPendingRequests[3][i].equals("Buy")) {
                if (latestPendingRequests[5][i].equals("At Best")) { // buy at best
                    System.out.println("Buy: At Best") ;
                    processAtBestPendingRequest("Buy",i) ;
                }
                else { // limit
                    System.out.println("Buy: Limit") ; // buy at limit
                    processLimitPendingRequest("Buy",i) ;
                }
            }
            else { // sell
                if (latestPendingRequests[5][i].equals("At Best")) { // sell at best
                    System.out.println("Sell: At Best") ;
                    processAtBestPendingRequest("Sell",i) ;
                }
                else { // limit
                    System.out.println("Sell: Limit") ; // sell at limit
                    processLimitPendingRequest("Sell",i) ;
                }
            }
        } // end for loop

    } // end of checkPendingRequests

    //**************************************//
    // processAtBestPendingRequest function //
    //**************************************//
    private void processAtBestPendingRequest(String buyOrSell, int index) {
        // select all rows of data from PendingRequests where ISIN are equal, BuyOrSell is opposite of buyOrSell
        // use the row where the limitprice is the smallest

        openConnection() ;
        try {
            ResultsModel temporaryResultsModelTable = new ResultsModel() ;

            stmt = LiveConnection.createStatement();
            currentSQLQuery = new String() ;
            currentSQLQuery = "SELECT * FROM PendingRequests WHERE ISIN = '" + latestPendingRequests[0][index] + "' AND BuyOrSell <> '" + buyOrSell + "' ORDER BY LimitPrice DESC" ;
            System.out.print("\nCurrent SQL Query:\n" + currentSQLQuery + "\n");
            rs = stmt.executeQuery(currentSQLQuery);

            temporaryResultsModelTable.setResultSet(rs) ;
            comparisonPendingRequests = new JTable(temporaryResultsModelTable) ;
        }
        catch(SQLException e) {
            System.out.print("\nProblem selecting data from PendingRequests:\n" + e + "\n") ;
        }
        closeConnection() ;

        System.out.println("The current pending trade can be matched up alongside: " + comparisonPendingRequests.getRowCount() + " other pending trades.") ;
        System.out.println("In processing, it should be matched with the top one on this list.....") ;

        // print comparisonPendingRequests for testing
        for (int i=0 ; i<comparisonPendingRequests.getRowCount() ; i++ ) {
            for (int j=0 ; j<comparisonPendingRequests.getColumnCount() ; j++ ) {
                try {
                    System.out.print(comparisonPendingRequests.getValueAt(i,j).toString() + ',') ;
                }
                catch(NullPointerException e) {
                    System.out.print("null") ;
                }
            }
            System.out.print("\n") ;
        }

        // if there is nothing that can be matched up, just wait until there is something
        if (comparisonPendingRequests.getRowCount() < 1) { return ; }


        // otherwise, get the top row from comparisonPendingRequests and execute this with the actual request
        System.out.println("\nTherefore... execute this one...") ;
        for (int j=0 ; j<currentPendingRequests.getColumnCount() ; j++ ) {
            try {
                System.out.print(latestPendingRequests[j][index] + ',') ;
            }
            catch(NullPointerException e) {
                System.out.print("nullVal") ;
            }
        }

        System.out.println("\nwith opposing order...") ;
        for (int k=0 ; k<comparisonPendingRequests.getColumnCount() ; k++ ) {
            try {
                System.out.print(comparisonPendingRequests.getValueAt(index,k).toString() + ',') ;
            }
            catch(NullPointerException e) {
                System.out.print("null") ;
            }
        }
        System.out.print("\n") ;

        // check whether or not this security already exists in the portfolio
        if (securityExistsInPortfolio(latestPendingRequests[0][index], latestPendingRequests[1][index])) {
            // alter the values in the portfolio to reflect the new quantity
            int quantityToAdd = currentQuantityOfISINInMyPortfolio(latestPendingRequests[0][index], latestPendingRequests[1][index]) + Integer.parseInt(latestPendingRequests[4][index]) ; // get this value to be added to the thingy
            openConnection() ;
            try {
                ResultsModel temporaryResultsModelTable = new ResultsModel() ;

                stmt = LiveConnection.createStatement() ;
                currentSQLQuery = new String() ;
                currentSQLQuery = "UPDATE MyPortfolio SET QuantityOfISIN = "+ quantityToAdd + " WHERE ISIN = '" + latestPendingRequests[0][index] + "' AND ClientID = '" + latestPendingRequests[1][index] + "'" ;
//                System.out.print("\n*** Updating SQL Query:\n" + currentSQLQuery + "\n") ;
                stmt.executeQuery(currentSQLQuery) ;
            }
            catch(SQLException e) {
//                System.out.print("\nProblem updating data in MyPortfolio:\n" + e + "\n") ;
            }
            // now alter the values in the portfolio to reflect the new latest date of purchase
            try {
                ResultsModel temporaryResultsModelTable = new ResultsModel() ;

                stmt = LiveConnection.createStatement();
                currentSQLQuery = new String() ;
                currentSQLQuery = "UPDATE MyPortfolio SET LatestDateOfPurchase = '" + currentLongTime + "' WHERE ISIN = '" + latestPendingRequests[0][index] + "' AND ClientID = '" + latestPendingRequests[1][index] + "'" ;
//                System.out.print("\n*** Updating SQL Query:\n" + currentSQLQuery + "\n");
                stmt.executeQuery(currentSQLQuery);
            }
            catch(SQLException e) {
//                System.out.print("\nProblem updating data in MyPortfolio:\n" + e + "\n") ;
            }
            closeConnection() ;
        }
        else { // simply insert the data as normal - insert this PendingRequest into MyPortfolio
            openConnection() ;
            try {
                ResultsModel temporaryResultsModelTable = new ResultsModel() ;

                stmt = LiveConnection.createStatement();
                currentSQLQuery = new String() ;
                currentSQLQuery = "INSERT INTO MyPortfolio VALUES ('" + latestPendingRequests[1][index] + "', '" + latestPendingRequests[0][index] + "', " + latestPendingRequests[4][index] + ", '" + currentLongTime + "', " + comparisonPendingRequests.getValueAt(index,6).toString() + ", 0, 0, 0, 0, '" + latestPendingRequests[8][index] + "')" ;
                System.out.print("\n*** Insertion SQL Query:\n" + currentSQLQuery + "\n");
                stmt.executeQuery(currentSQLQuery);
            }
            catch(SQLException e) {
//                System.out.print("\nProblem inserting data into MyPortfolio:\n" + e + "\n") ;
            }
            closeConnection() ;
        }

        // for the opposing PendingRequest, remove the pending request
        // ERROR: at the moment, this removes an entire instance of an ISIN - get this to remove the relative quantity of the request
        openConnection() ;
        try {
            ResultsModel temporaryResultsModelTable = new ResultsModel() ;

            stmt = LiveConnection.createStatement();
            currentSQLQuery = new String() ;
            currentSQLQuery = "DELETE * FROM PendingRequests WHERE ClientID = '" + comparisonPendingRequests.getValueAt(index,1).toString() + "' AND ISIN = '" + comparisonPendingRequests.getValueAt(index,0).toString() + "' AND BuyOrSell = '" + comparisonPendingRequests.getValueAt(index,3).toString() + "'";
            System.out.print("\n*** Deletion SQL Query:\n" + currentSQLQuery + "\n");
            stmt.executeQuery(currentSQLQuery);
        }
        catch(SQLException e) {
//            System.out.print("\nProblem deleting data from PendingRequests:\n" + e + "\n") ;
        }
        closeConnection() ;

        // remove the placed order from PendingRequests
        openConnection() ;
        try {
            String dateToDelete  ;
            dateToDelete =  latestPendingRequests[2][index].toString().substring(8,10) + '/' ;
            dateToDelete += latestPendingRequests[2][index].toString().substring(5,7) + '/' ;
            dateToDelete += latestPendingRequests[2][index].toString().substring(0,4) + ' ' ;
            dateToDelete += latestPendingRequests[2][index].toString().substring(11,16) ;
            // format: DD/MM/YYYY HH:MM

            ResultsModel temporaryResultsModelTable = new ResultsModel() ;

            stmt = LiveConnection.createStatement();
            currentSQLQuery = new String() ;
            currentSQLQuery = "DELETE * FROM PendingRequests WHERE ISIN = '" + latestPendingRequests[0][index] + "' AND ClientID = '" + latestPendingRequests[1][index] + "' AND DateAndTimeOfInstruction like '%" + dateToDelete + "%' AND BuyOrSell = '" + latestPendingRequests[3][index] + "'" ;
            System.out.print("\n*** Deletion SQL Query:\n" + currentSQLQuery + "\n");
            stmt.executeQuery(currentSQLQuery);
        }
        catch(SQLException e) {
//            System.out.print("\nProblem deleting data from PendingRequests:\n" + e + "\n") ;
        }
        closeConnection() ;

    } // end of processAtBestPendingRequest function

    //*************************************//
    // processLimitPendingRequest function //
    //*************************************//
    private void processLimitPendingRequest(String buyOrSell, int index) {
        // select all rows of data from PendingRequests where ISIN are equal, BuyOrSell is opposite of buyOrSell, LimitPrice is greater than LimitPrice on Ydata
        // use the row where the limitprice is the highest

        // insert this PendingRequest into MyPortfolio

        // remove order from PendingRequests
    } // end of processLimitPendingRequest function

    //***********************************//
    // securityExistsInPortfolio function //
    //***********************************//
    private boolean securityExistsInPortfolio(String ISIN, String clientID) {
        openConnection() ;
        try {
            ResultsModel temporaryResultsModelTable = new ResultsModel() ;
            JTable securityExistTestTable ;

            stmt = LiveConnection.createStatement() ;
            currentSQLQuery = new String() ;
            currentSQLQuery = "SELECT * FROM MyPortfolio WHERE ISIN = '" + ISIN + "' AND ClientID = '" + clientID + "'" ;
//            System.out.print("\n*** Selection SQL Query:\n" + currentSQLQuery + "\n") ;
            rs = stmt.executeQuery(currentSQLQuery) ;

            temporaryResultsModelTable.setResultSet(rs) ;
            securityExistTestTable = new JTable(temporaryResultsModelTable) ;

            closeConnection() ;

            if (securityExistTestTable.getRowCount() > 0) { return true ; }
            else { return false ; }
        }
        catch(SQLException e) {
            System.out.print("\nProblem selecting data from MyPortfolio:\n" + e + "\n") ;
        }
        return true ; // this point is never reached
    } // end of securityExistsInPortfolio function

    //***********************************//
    // currentQuantityOfISINInMyPortfolio function           //
    //***********************************//
    private int currentQuantityOfISINInMyPortfolio(String ISIN, String clientID) {
        openConnection() ;
        try {
            ResultsModel temporaryResultsModelTable = new ResultsModel() ;
            JTable quantityOfISINTestTable ;

            stmt = LiveConnection.createStatement() ;
            currentSQLQuery = new String() ;
            currentSQLQuery = "SELECT * FROM MyPortfolio WHERE ISIN = '" + ISIN + "' AND ClientID = '" + clientID + "'" ;
//            System.out.print("\n*** Selection SQL Query:\n" + currentSQLQuery + "\n") ;
            rs = stmt.executeQuery(currentSQLQuery) ;

            temporaryResultsModelTable.setResultSet(rs) ;
            quantityOfISINTestTable = new JTable(temporaryResultsModelTable) ;


            if (quantityOfISINTestTable.getRowCount() != 1) {
                System.err.println("Error in function currentQuantityOfISINInMyPortfolio()") ;
                System.exit(1) ;
            }
//            System.out.println(quantityOfISINTestTable.getValueAt(0,2).toString()) ;

            closeConnection() ;
            return Integer.parseInt(quantityOfISINTestTable.getValueAt(0,2).toString()) ;
        }
        catch(SQLException e) {
            System.out.print("\nProblem deleting data from PendingRequests:\n" + e + "\n") ;
        }
        closeConnection() ;
        return 100 ; // this point is never reached
    } // end of currentQuantityOfISINInMyPortfolio function

    //***********************************//
    // openConnection function           //
    //***********************************//
    private void openConnection() {
        try {  // load the driver
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        }
        catch(java.lang.ClassNotFoundException e) {
            System.err.print("Error in attempting to load database driver:\n      " + e.getMessage() + "\n");
        }

        try {  // make a connection
            LiveConnection = DriverManager.getConnection("jdbc:odbc:CentralDatabase");
        }
        catch(SQLException e) {
            System.err.print("Error in attempting to connect to database:\n      " + e.getMessage() + "\n");
        }
    } // end of openConnection function

    //***********************************//
    // closeConnection function           //
    //***********************************//
    private void closeConnection() {
        try {
            if( LiveConnection != null ) { LiveConnection.close(); }
        }
        catch(SQLException e) {
            System.err.print("Error in attempting to close connection to database:\n      " + e.getMessage() + "\n");
        }
    } // end of closeConnection function
} //EOF