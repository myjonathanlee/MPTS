import javax.swing.JOptionPane ;
import java.lang.Math ;
import Jama.* ;


/**
 * A class which creates a Ridge Regression Neural Network in a Primary Form.
 * The class will then run through the ridge regression algorithm processing the
 * inputted data and calculating and providing its prediction for the following day.
 *
 * <p>
 * <table>
 *   <tr>
 *     <td><b>Data Arrays</b>
 *     <td><b>Information</b>
 *   </tr>
 *   <tr>
 *     <td>X Inputs
 *     <td>52 week low<br>52 week high<br>volume
 *   </tr>
 *   <tr>
 *     <td>Y Inputs
 *     <td>price today
 *   </tr>
 * </table>
 * </p>
 *
 * @author        Jonathan K. W. Lee
 * @version       1.0
 * @see           IPS
 *
 */
public class IPSPrediction_RidgeRegression_Primary {
    // class variables
    private static double sharePricePredictionNxtBsnssDay ;
    private static double sharePricePrediction1Week ;
    private static double sharePricePrediction1Month ;
    private static double sharePricePrediction3Months ;
    private static double sharePricePrediction6Months ;
    private static double sharePricePrediction12Months ;

    final private static double VALUE_A = 0.1 ; // OPTIMAL "A" VALUE

    private static Matrix xInputs ;
    private static Matrix yLabels ;
    private static Matrix identityMatrix ;

    private int noXRows ;
    private int noXCols ;

    private int noDaysBackwardToTest ;

    private int higherDimensionRequest ;

    //***********************************//
    // constructor                       //
    //***********************************//
    /**
     *
     * Initialises variables and data arrays.
     * @param X_Inputs_Array        array of X Inputs
     * @param Y_Labels_Array        array of Y Labels
     *
     */
    IPSPrediction_RidgeRegression_Primary(double xInputsArray[][],
                                          double yLabelsArray[][],
                                          int numPrevDaysPredictn) {
        sharePricePredictionNxtBsnssDay = 0 ;
        sharePricePrediction1Week       = 0 ;
        sharePricePrediction1Month      = 0 ;
        sharePricePrediction3Months     = 0 ;
        sharePricePrediction6Months     = 0 ;
        sharePricePrediction12Months    = 0 ;

        // ask user if they want to use the "ln" higher expansion function
        // if so, remember that the results need to be "e"'d again!
        higherDimensionRequest = JOptionPane.showConfirmDialog(null, "Do you want to use a higher dimension space to create your data?", "Utilisation of higher dimension space", JOptionPane.YES_NO_OPTION) ;
        if (higherDimensionRequest == JOptionPane.YES_OPTION) {
            // expand the data into a higher dimensional space
            System.out.println("now using ln function to expand x inputs dataspace") ;
            for (int i=0 ; i < xInputsArray[0].length ; i++) {
                for (int j=0 ; j < xInputsArray.length ; j++) {
                    xInputsArray[j][i] = Math.log(xInputsArray[j][i]) ;
                }
            }
            System.out.println("now using ln function to expand y labels dataspace") ;
            for (int i=0 ; i < yLabelsArray.length ; i++) {
                yLabelsArray[0][i] = Math.log(yLabelsArray[0][i]) ;
            }
        }
        // else no or the box was cancelled so just continue with not doing this

        // here, i am normalising the X-inputs by reducing the volumes by 1 million
        for (int i=0 ; i < xInputsArray[2].length ; i++) {
            xInputsArray[2][i] = xInputsArray[2][i] / 1000000.0 ;
        }

        // set the arrays into matrices
        xInputs = new Matrix(xInputsArray) ;
        yLabels = new Matrix(yLabelsArray) ;

        // in creating the original arrays, the data was put the wrong way round, this corrects that mistake
        xInputs = xInputs.transpose() ;
        yLabels = yLabels.transpose() ;

        noXRows = xInputs.getRowDimension() ;
        noXCols = xInputs.getColumnDimension() ;

        noDaysBackwardToTest = numPrevDaysPredictn ;

        System.out.println("\n--- Matrix Information ---") ;
        System.out.println("X Rows:" + xInputs.getRowDimension()) ;
        System.out.println("X Cols:" + xInputs.getColumnDimension()) ;

        System.out.println("Y Rows:" + yLabels.getRowDimension()) ;
        System.out.println("Y Cols:" + yLabels.getColumnDimension()) ;
        System.out.println("--------------------------") ;

        // create and initialise an Identity Matrix
        identityMatrix = new Matrix(noXCols, noXCols) ;
        identityMatrix.identity(noXCols, noXCols) ;

        executeRidgeRegressionCalculation() ;
    } // end constructor

    //***********************************//
    // executeRidgeRegressionCalculation function
    //***********************************//
    /**
     * This function oversees the creation of the ridge regression Neural Network
     * producing and processing the data to produce prediction results.  It
     * automatically sets the results into global variables which can be accessed
     * by calling the given functions.
     */
    private void executeRidgeRegressionCalculation() { // do the Ridge Regression calculation
        double noWorkingDays ;
        Matrix tempMatrix_XTransposed = new Matrix(noXRows, noXCols) ;
        Matrix tempMatrix_NN          = new Matrix(noXCols, noXCols) ;
        Matrix tempMatrix_NN2         = new Matrix(noXCols, noXCols) ;
        Matrix tempMatrix_NN3         = new Matrix(noXCols, noXCols) ;
        Matrix tempMatrix_NN4         = new Matrix(noXCols, noXCols) ;
        Matrix tempMatrix_N1          = new Matrix(noXCols, 1) ;
        Matrix wResult_N1             = new Matrix(noXCols, 1) ; // (m, n) (rows, cols)

        System.out.println("--- X Inputs printed out ---") ;
        xInputs.print(15,1) ;
        System.out.println("-------------------") ;

        tempMatrix_XTransposed = xInputs.transpose() ;

        tempMatrix_NN = tempMatrix_XTransposed.times(xInputs) ;

        tempMatrix_NN2 = identityMatrix.times(VALUE_A) ;

        tempMatrix_NN3 = tempMatrix_NN.plus(tempMatrix_NN2) ;

        tempMatrix_N1 = tempMatrix_XTransposed.times(yLabels) ;

        tempMatrix_NN4 = tempMatrix_NN3.inverse() ;

        wResult_N1 = tempMatrix_NN4.times(tempMatrix_N1) ;

        System.out.println("*** Neural Network w0 calculation Complete - w0 RESULTS... ***") ;
        System.out.println("-------------------") ;
        wResult_N1.print(25,6) ; // row, col
        System.out.println("-------------------") ;

        // test that there is enough noDaysBackwardToTest data to be used in the prediction
        if (xInputs.getRowDimension()-1 < noDaysBackwardToTest) {
            JOptionPane.showMessageDialog(null, "We were unable to provide you with predictions for your required settings.\nThe value that you set in \"Number of days historical data for attribute prediction\"\nwas too large for the date range you specified.\nPlease increase the date range or decrease the days historical data value and try again", "Change the data or date range", JOptionPane.ERROR_MESSAGE) ;
            return ;
        }

        ///////////////////////////////////////////////////////
        // set results for next business day prediction
        ///////////////////////////////////////////////////////
        noWorkingDays = 1 ;
        sharePricePredictionNxtBsnssDay = setPredictionResults(wResult_N1, noDaysBackwardToTest, noWorkingDays) ;

        ///////////////////////////////////////////////////////
        // set results for 1 week prediction
        ///////////////////////////////////////////////////////
        noWorkingDays = MPTSGlobalVariables.NO_TRADING_DAYS_1YEAR / 52.0 ;
        sharePricePrediction1Week = setPredictionResults(wResult_N1, noDaysBackwardToTest, noWorkingDays) ;

        ///////////////////////////////////////////////////////
        // set results for 1 month prediction
        ///////////////////////////////////////////////////////
        noWorkingDays = MPTSGlobalVariables.NO_TRADING_DAYS_1YEAR / 12.0 ;
        sharePricePrediction1Month = setPredictionResults(wResult_N1, noDaysBackwardToTest, noWorkingDays) ;

        ///////////////////////////////////////////////////////
        // set results for 3 months prediction
        ///////////////////////////////////////////////////////
        noWorkingDays = MPTSGlobalVariables.NO_TRADING_DAYS_1YEAR / 4.0 ;
        sharePricePrediction3Months = setPredictionResults(wResult_N1, noDaysBackwardToTest, noWorkingDays) ;

        ///////////////////////////////////////////////////////
        // set results for 6 months prediction
        ///////////////////////////////////////////////////////
        noWorkingDays = MPTSGlobalVariables.NO_TRADING_DAYS_1YEAR / 2.0 ;
        sharePricePrediction6Months = setPredictionResults(wResult_N1, noDaysBackwardToTest, noWorkingDays) ;

        ///////////////////////////////////////////////////////
        // set results for 12 months prediction
        ///////////////////////////////////////////////////////
        noWorkingDays = MPTSGlobalVariables.NO_TRADING_DAYS_1YEAR ;
        sharePricePrediction12Months = setPredictionResults(wResult_N1, noDaysBackwardToTest, noWorkingDays) ;

    } // end function executeRidgeRegressionCalculation

    //***********************************//
    // setPredictionResults function
    //***********************************//
    /**
     *
     * This function calculates and returns prediction results
     * for a given number of days forward
     * @return            the calculated prediction results for a given number of days forward
     *
     */
    private double setPredictionResults(Matrix wResult_N1,
                                        double noDaysBackwardToTest, // take the previous number of days data as an average to project...
                                        double noDaysForwardToPredict) {
        double sharePricePrediction     = 0 ;
        double tempCumulativeDifference = 0 ;
        double cumulativeDifference     = 0 ;
        double predictionXInputValues[] = new double[xInputs.getColumnDimension()] ;

        // create a TECHNICAL prediction for each attribute for "noDaysBackwardToTest" days time
        for (int j=0 ; j < xInputs.getColumnDimension() ; j++) {
            for (int i=0 ; i < noDaysBackwardToTest ; i++) {
                tempCumulativeDifference = xInputs.get(xInputs.getRowDimension()-1-i,j) - xInputs.get(xInputs.getRowDimension()-1-i-1,j) ;
                cumulativeDifference += tempCumulativeDifference ;
//                System.out.println(tempCumulativeDifference) ;
            }
            cumulativeDifference /= noDaysBackwardToTest ; // taking a simple arithmetic "mean" average calculation

            predictionXInputValues[j] = xInputs.get(xInputs.getRowDimension()-1-1,j) + (cumulativeDifference * noDaysForwardToPredict) ;

            if (higherDimensionRequest == JOptionPane.YES_OPTION) {
                // expand the predicted x attributes to the required result
                for (int i=0 ; i < predictionXInputValues.length ; i++) {
                    predictionXInputValues[i] = Math.log(predictionXInputValues[i]) ;
                }
            }

            System.out.println("--------------") ;
            System.out.println("Attribute " + j + ", Original value:   " + xInputs.get(xInputs.getRowDimension()-1-1,j)) ;
            System.out.println("Attribute " + j + ", Predicted value:  " + predictionXInputValues[j]) ;

            tempCumulativeDifference = 0 ; // reset temporary variables for reuse
            cumulativeDifference     = 0 ;
        }

        System.out.println("--------------") ;
        System.out.println("Now multiplying the predicted attributes with w0 matrix...") ;
        for (int i = 0 ; i < xInputs.getColumnDimension() ; i++) {
            sharePricePrediction += wResult_N1.get(i,0) * predictionXInputValues[i] ;
        }
        System.out.println("*** Calculation complete ***") ;
        System.out.println("share price prediction for " + noDaysForwardToPredict + " days time: " + sharePricePrediction) ;
        System.out.println("---**************************************---\n\n") ;

        if (higherDimensionRequest == JOptionPane.YES_OPTION) {
            // expand the result using eulers number "e"
            sharePricePrediction = Math.exp(sharePricePrediction) ;
        }

        // cut the result to 4 decimal places
        try {
            String sharePricePredictionString = new String(Double.toString(sharePricePrediction)) ;
            int    decimalPlace               = sharePricePredictionString.indexOf('.') ;
                   sharePricePredictionString = sharePricePredictionString.substring(0,decimalPlace+5) ; // 4 decimal places
                   sharePricePrediction       = Double.parseDouble(sharePricePredictionString) ;
        }
        catch (NumberFormatException e) { } // otherwise the result is infinty or NaN so don't cut
        catch (StringIndexOutOfBoundsException e) { }

        return sharePricePrediction ;
    } // end function setPredictionResults

    //***********************************//
    // getSharePricePredictionNxtBsnssDay function
    //***********************************//
    /**
     *
     * This function returns the calculated predicted share price for nxtBsnssDay.
     * @return            the calculated predicted share price for nxtBsnssDay
     *
     */
    public double getSharePricePredictionNxtBsnssDay() { return sharePricePredictionNxtBsnssDay ; } // end function getSharePricePredictionNxtBsnssDay

    //***********************************//
    // getSharePricePrediction1Week function
    //***********************************//
    /**
     *
     * This function returns the calculated predicted share price for 1 weeks time.
     * @return            the calculated predicted share price for 1 weeks time
     *
     */
    public double getSharePricePrediction1Week() { return sharePricePrediction1Week ; } // end function getSharePricePrediction1Week

    //***********************************//
    // getSharePricePrediction1Month function
    //***********************************//
    /**
     *
     * This function returns the calculated predicted share price for 1 months time.
     * @return            the calculated predicted share price for 1 months time
     *
     */
    public double getSharePricePrediction1Month() { return sharePricePrediction1Month ; } // end function getSharePricePrediction1Month

    //***********************************//
    // getSharePricePrediction3Months function
    //***********************************//
    /**
     *
     * This function returns the calculated predicted share price for 3 months time.
     * @return            the calculated predicted share price for 3 months time
     *
     */
    public double getSharePricePrediction3Months() { return sharePricePrediction3Months ; } // end function getSharePricePrediction3Months

    //***********************************//
    // getSharePricePrediction6Months function
    //***********************************//
    /**
     *
     * This function returns the calculated predicted share price for 6 months time.
     * @return            the calculated predicted share price for 6 months time
     *
     */
    public double getSharePricePrediction6Months() { return sharePricePrediction6Months ; } // end function getSharePricePrediction6Months

    //***********************************//
    // getSharePricePrediction12Months function
    //***********************************//
    /**
     *
     * This function returns the calculated predicted share price for 12 months time.
     * @return            the calculated predicted share price for 12 months time
     *
     */
    public double getSharePricePrediction12Months() { return sharePricePrediction12Months ; } // end function getSharePricePrediction12Months

} // EOF