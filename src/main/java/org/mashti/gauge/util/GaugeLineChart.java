/**
 * This file is part of gauge.
 *
 * gauge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gauge is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with gauge.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mashti.gauge.util;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.mashti.sight.PlainChartTheme;
import org.mashti.sina.distribution.statistic.Statistics;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class GaugeLineChart {

    static {
        ChartFactory.setChartTheme(new PlainChartTheme());
    }

    private final String title;
    private final File csv;

    public GaugeLineChart(String title, File csv) throws IOException {

        this.title = title;
        this.csv = csv;
    }

    public void saveAsJFC(File destination) throws IOException {

        final JFreeChart chart = createChart(createDataset());

        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(destination));
            out.writeObject(chart);
            out.flush();
            out.close();
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public void saveAsSVG(File destination) throws IOException {

        final JFreeChart chart = createChart(createDataset());
        final Rectangle bounds = new Rectangle(0, 0, 600, 400);
        exportChartAsSVG(chart, bounds, destination);
    }

    private JFreeChart createChart(IntervalXYDataset intervalxydataset) {

        NumberAxis numberaxis = new NumberAxis("Time Thrugh Experiment");
        NumberAxis numberaxis1 = new NumberAxis(title);
        XYErrorRenderer xyerrorrenderer = new XYErrorRenderer();
        xyerrorrenderer.setBaseLinesVisible(true);
        XYPlot xyplot = new XYPlot(intervalxydataset, numberaxis, numberaxis1, xyerrorrenderer);
        xyplot.setDomainPannable(true);
        xyplot.setRangePannable(true);
        xyplot.setBackgroundPaint(Color.lightGray);
        xyplot.setDomainGridlinePaint(Color.white);
        xyplot.setRangeGridlinePaint(Color.white);
        JFreeChart jfreechart = new JFreeChart(title, xyplot);
        ChartUtilities.applyCurrentTheme(jfreechart);
        return jfreechart;
    }

    private IntervalXYDataset createDataset() throws IOException {

        final XYIntervalSeriesCollection xyintervalseriescollection = new XYIntervalSeriesCollection();
        final XYIntervalSeries xyintervalseries = new XYIntervalSeries("Series 1");

        final CsvListReader reader = new CsvListReader(new BufferedReader(new FileReader(csv)), CsvPreference.STANDARD_PREFERENCE);
        reader.getHeader(true);

        List<String> row;

        while ((row = reader.read()) != null) {

            final Long t_count = Long.valueOf(row.get(0));
            final Double t_mean = Double.valueOf(row.get(2));
            final Double t_stdev = Double.valueOf(row.get(4));
            final Double t_ci = t_count > 1 ? Statistics.confidenceInterval(t_count, t_stdev, 0.95D).doubleValue() : 0;
            final Double t_low = t_mean - t_ci;
            final Double t_high = t_mean + t_ci;

            final Long v_count = Long.valueOf(row.get(5));
            final Double v_mean = Double.valueOf(row.get(7));
            final Double v_stdev = Double.valueOf(row.get(9));
            final Double v_ci = v_count > 1 ? Statistics.confidenceInterval(v_count, v_stdev, 0.95D).doubleValue() : 0;
            final Double v_low = v_mean - v_ci;
            final Double v_high = v_mean + v_ci;

            xyintervalseries.add(t_mean, t_low, t_high, v_mean, v_low, v_high);
        }
        reader.close();

        xyintervalseriescollection.addSeries(xyintervalseries);
        return xyintervalseriescollection;
    }

    /**
     * Exports a JFreeChart to a SVG file.
     *
     * @param chart JFreeChart to export
     * @param bounds the dimensions of the viewport
     * @param svgFile the output file.
     * @throws IOException if writing the svgFile fails.
     */
    void exportChartAsSVG(JFreeChart chart, Rectangle bounds, File svgFile) throws IOException {

        // Get a DOMImplementation and create an XML document
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        Document document = domImpl.createDocument(null, "svg", null);

        // Create an instance of the SVG Generator
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // draw the chart in the SVG generator
        chart.draw(svgGenerator, bounds);

        // Write svg file
        OutputStream outputStream = new FileOutputStream(svgFile);
        Writer out = new OutputStreamWriter(outputStream, "UTF-8");
        svgGenerator.stream(out, true /* use css */);
        outputStream.flush();
        outputStream.close();
    }
}
