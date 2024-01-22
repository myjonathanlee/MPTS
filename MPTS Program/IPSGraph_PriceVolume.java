import java.awt.Color;
import java.awt.BorderLayout ;
import javax.swing.JPanel ;

import com.jrefinery.data.XYDataset ;
import com.jrefinery.data.IntervalXYDataset ;
import com.jrefinery.chart.JFreeChart ;
import com.jrefinery.chart.ChartPanel ;
import com.jrefinery.chart.XYPlot ;
import com.jrefinery.chart.XYItemRenderer ;
import com.jrefinery.chart.StandardXYItemRenderer ;
import com.jrefinery.chart.VerticalXYBarRenderer ;
import com.jrefinery.chart.HorizontalDateAxis ;
import com.jrefinery.chart.NumberAxis ;
import com.jrefinery.chart.VerticalNumberAxis ;
import com.jrefinery.chart.CombinedXYPlot ;
import com.jrefinery.chart.tooltips.TimeSeriesToolTipGenerator ;

/**
 *
 * This class creates a time series chart overlaid with a vertical XY bar chart.
 *
 * @author        Jonathan K. W. Lee
 * @version       1.0
 * @see           IPS
 *
 */
public class IPSGraph_PriceVolume {
    // class variable
    private ChartPanel panel ; // ChartPanel extends JPanel

    //***********************************//
    // constructor                       //
    //***********************************//
    /**
     *
     * Initialises variables and <i>completes</i> creation of a new chart object
     *
     * @param chartTitle        the title of the chart
     * @param priceData            the price data of the top chart
     * @param volumeData        the volume data of the bottom chart
     *
     */
    IPSGraph_PriceVolume(String            chartTitle,
                         XYDataset         priceData,
                         IntervalXYDataset volumeData) {
        JFreeChart chart = createCombinedChart(chartTitle, priceData, volumeData) ;
        panel = new ChartPanel(chart, true, true, true, false, true) ;
    } // end constructor

    //***********************************//
    // function createCombinedChart      //
    //***********************************//
    /**
     *
     * Creates instances of both of the subcharts and combines this returning
     * a JFreeChart object containing the entire chart
     *
     * @param chartTitle        the title of the chart
     * @param priceData         the price data of the top chart
     * @param volumeData        the volume data of the bottom chart
     *
     */
    private JFreeChart createCombinedChart(String            chartTitle,
                                           XYDataset         priceData,
                                           IntervalXYDataset volumeData) {
        // create priceData subplot...
        XYItemRenderer renderer1 = new StandardXYItemRenderer() ;
        renderer1.setToolTipGenerator(new TimeSeriesToolTipGenerator("d-MMM-yyyy", "0.00")) ;
        NumberAxis axis = new VerticalNumberAxis("Price (p)") ;
        axis.setAutoRangeIncludesZero(false) ;
        XYPlot subplot1 = new XYPlot(priceData, null, axis, renderer1) ;
        subplot1.setSeriesPaint(0, Color.red) ;

        // create volumeData subplot...
        XYItemRenderer renderer2 = new VerticalXYBarRenderer(0.20) ;
        renderer2.setToolTipGenerator(new TimeSeriesToolTipGenerator("d-MMM-yyyy", "0.00")) ;
        axis = new VerticalNumberAxis("Volume") ;
        XYPlot subplot2 = new XYPlot(volumeData, null, axis, renderer2) ;
        subplot2.setSeriesPaint(0, Color.blue) ;

        // make a combined plot...
        CombinedXYPlot plot = new CombinedXYPlot(new HorizontalDateAxis("Date"), CombinedXYPlot.VERTICAL) ;
        plot.add(subplot1, 3) ;  // weight of 75%
        plot.add(subplot2, 1) ;  // weight of 25%

        // return a new JFreeChart object containing the graph
        return new JFreeChart(chartTitle,
                              JFreeChart.DEFAULT_TITLE_FONT,
                              plot,
                              true);
    } // end function createCombinedChart

    //***********************************//
    // function getChart                 //
    //***********************************//
    /**
     *
     * Returns the created chart as a JPanel object
     *
     * @return                the entire chart as a JPanel object
     *
     */
    public JPanel getChart() { return panel ; } // end function getChart

} // EOF