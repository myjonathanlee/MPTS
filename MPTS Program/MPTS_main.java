import javax.swing.* ;

/**
 *
 * This is the main runner class of the entire MPTS program.  In running this
 * class, the other required classes are created as and when required.  The
 * only "main" function in this program is located here.<br>
 *
 * In running this program, it can take in 2 arguments.
 *
 * <ol>
 *   <li>user name
 *   <li>password
 * </ol>
 *
 * @author         Jonathan K. W. Lee
 * @version        1.0
 * @see            SystemWindow
 *
 */
public class MPTS_main {

    /**
     * This is the main function.  It takes in two arguments
     * First:  username
     * Second: password
     *
     * @param args  the inputted arguments 1st-user name; 2nd-password
     */
    public static void main(String[] args) {
        String clientUserID   = "default" ; // this is set when the program is first installed
        String clientPassword = "default" ;

        switch(args.length) {
            case 1:
                clientUserID = args[0] ;
                break ;
            case 2:
                clientUserID = args[0] ;
                clientPassword = args[1] ;
                break ;
            default:
                System.out.print("No arguments taken into program\n") ;
                break ;
        }

        SystemWindow window = new SystemWindow(clientUserID, clientPassword);

//        window.setTitle("Market Prediction Trading System - [" + clientUserID + "]"); // initialise the window and display
        window.setSize(800,640); // width, height
        window.setVisible(true);
    } // end main
} //EOF