import java.io.* ;
import java.sql.* ;
import java.util.GregorianCalendar ;
import java.util.Calendar ;
import java.util.Timer ;
import java.util.TimerTask ;
import javax.swing.* ;

/** This class simulates hypothetical market activity
 */
public class SimulateMarket {
    // class variables
    private ResultsModel model ;
    private Connection   LiveConnection ;
    private Statement    stmt ;
    private String       currentSQLQuery ;
    private ResultSet    rs ;

    private static String tradingFile = new String() ;
    private static String[][] marketSimulationFile ; // columns, rows

    private static String DateAndTimeOfInstruction = new String() ;
    private static String currentTime = new String() ;
    private static String currentDate = new String() ;

    public static void main(String[] args) { // main
        switch(args.length) {
            case 1:
                tradingFile = args[0] ;
                System.out.print("Market simulation file successfully taken into program\n") ;
                break ;
            default:
                System.err.print("No arguments taken into program\nPlease enter a valid market simulation file and try again\n") ;
                System.exit(1) ;
        }

        // create this class and prepare the data
        SimulateMarket theMarket = new SimulateMarket() ;

        // start the market simulation - this doesn't end until Ok is pressed
        theMarket.startMarketSimulation() ;

        // this simply keeps the whole program alive until Ok is pressed
        JOptionPane.showMessageDialog(null, "Market Simulation Program is now running\nClick OK to exit application", "Market Simulation Program", JOptionPane.INFORMATION_MESSAGE ) ;
        System.exit(0) ;
    } //end main

    //***********************************//
    // constructor
    //***********************************//
    SimulateMarket() {
        initialiseMarketDataArray() ; // convert the CSV file into a 2D array
    } // end of constructor

    //***********************************//
    // startMarketSimulation function    //
    //***********************************//
    private void startMarketSimulation() {
        // start the timer and check for time alignment every second
           Timer repetitionTimer ;
        TimerTask marketTimerTask = new TimerTask() {
                                        public void run() {
                                            checkTradeFile() ;
                                        }
                                    } ;

        repetitionTimer = new Timer(true) ;
        repetitionTimer.scheduleAtFixedRate(marketTimerTask, 0, 60000) ; // 60000 msec = 60 seconds
    } // end of startMarketSimulation function

    //***********************************//
    // checkTradeFile function           //
    //***********************************//
    private void checkTradeFile() {
        // if a time entry in the trade file concurs with the current time,
        // then insert this entry into the database

        GregorianCalendar currentCalendar = new GregorianCalendar() ;

        currentCalendar.setTime(new java.util.Date(System.currentTimeMillis())) ;
        currentTime = String.valueOf(currentCalendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(currentCalendar.get(Calendar.MINUTE)) + ":00" ;    // set the date and time format for the clock.  Format: HH:MM:SS
        currentDate = String.valueOf(currentCalendar.get(Calendar.DATE)) + "/" + String.valueOf(currentCalendar.get(Calendar.MONTH)+1) + "/" + String.valueOf(currentCalendar.get(Calendar.YEAR)) ; // Format: DD/MM/YYYY

        System.out.println("Time (sec set to 00):\t" + currentTime) ;
        System.out.println("Date:\t\t\t" + currentDate) ;

        for (int i=0 ; i<(marketSimulationFile[0].length-1) ; i++) {
            if (currentTime.equals(marketSimulationFile[0][i])) { // time entry in the trade == time entry in marketSimulationFile[][]
                // then all rows of data containing this time can be entered into the database
                openConnection() ;
                try {
                    stmt = LiveConnection.createStatement();
                    currentSQLQuery = new String() ;
                    currentSQLQuery = "INSERT INTO PendingRequests VALUES ('" + marketSimulationFile[1][i] + "', '" + marketSimulationFile[3][i] + "', '" + currentDate + " " + marketSimulationFile[0][i] + "', '" + marketSimulationFile[2][i] + "', " + marketSimulationFile[5][i] + ", '" + marketSimulationFile[4][i] + "', " + marketSimulationFile[6][i] + ", 0, 'automatically generated order')" ;
                    System.out.print("\nA pending request is now being inserted:\n" + currentSQLQuery + "\n");
                    stmt.executeQuery(currentSQLQuery);
                }
                catch(SQLException e) {
                    System.out.print("\nProblem inserting test data into PendingRequests:\n" + e + "\n") ;
                }
                closeConnection() ;
            } // else do nothing
        } // for each of the test elements
    } // end of checkTradeFile function

    //***********************************//
    // initialiseMarketDataArray function//
    //***********************************//
    private void initialiseMarketDataArray() {
        FileReader theData ;

        int rowIndex = 0 ;
        int columnIndex = 0 ;
        int maxColumnIndex = 0 ;
        try {
            theData = new FileReader(tradingFile) ;

            try {
                int n = 0 ;
                for (int i=0 ; n != -1 ; i++ ) { // process this character
                    n = theData.read() ;
                    if ((char)n == '\n') { // then this is the next row
                        rowIndex++ ;
                        columnIndex = 0 ;
                    }
                    else if ((char)n == ',') { // then this is the next column
                        columnIndex++ ;
                        if (maxColumnIndex < columnIndex ) {
                            maxColumnIndex = columnIndex ;
                        }
                    }
                }
            }
            catch(IOException e) {
                System.out.print("IOException: " + e) ;
            }
        }
        catch(FileNotFoundException e) {
            System.out.print("File not found: " + e) ;
            System.exit(1) ;
        }

        rowIndex++ ;
        maxColumnIndex++ ;

        marketSimulationFile = new String [maxColumnIndex][rowIndex] ; // columns, rows

        // initialise each element of the marketSimulationFile
        for (int i=0 ; i<maxColumnIndex ; i++) {
            for (int y=0 ; y<rowIndex ; y++) {
                marketSimulationFile[i][y] = new String() ;
            }
        }

        try {
            theData = new FileReader(tradingFile) ;

            try {
                int n = 0 ;
                columnIndex = 0 ;
                rowIndex    = 0 ;

                for (int i=0 ; n != -1 ; i++) { // process this character
                    n = theData.read() ;

                    if ((char)n == '\n') { // then this is the next row
                        rowIndex++ ;
                        columnIndex = 0 ;
                    }
                    else if ((char)n == ',') { // then this is the next column
                        columnIndex++ ;
                    }
                    else { // is a normal character so store it
                        marketSimulationFile[columnIndex][rowIndex] += Character.toString((char)n) ;
                    }
                }
            }
            catch(IOException e) {
                System.out.print("IOException: " + e) ;
            }

            // test printing out the imported data
            System.out.print("\nImported data:\n--------------------\n") ;
            System.out.print("contents of: " + tradingFile) ;
            System.out.print("\n--------------------\n") ;


            // print out the contents of the marketSimulationFile
            for (int y=0 ; y<rowIndex ; y++) {
                for (int i=0 ; i<maxColumnIndex ; i++) {
                    System.out.print(marketSimulationFile[i][y]) ;
                }
                System.out.print("\n") ;
            }
            System.out.print("--------------------\n") ;

        }
        catch(FileNotFoundException e) {
            System.out.print("File not found: " + e) ;
            System.exit(1) ;
        }
    } // end of initialiseMarketDataArray function

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