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
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
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
    private final JFreeChart chart;
    private final YIntervalSeries series;

    public GaugeLineChart(String title, File csv) throws IOException {

        this.title = title;
        this.csv = csv;
        series = getYIntervalSeries();
        chart = createChart(createDataset(series));
    }

    public static void saveAsJFC(JFreeChart chart, File destination) throws IOException {

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

    /**
     * Exports a JFreeChart to a SVG file.
     *
     * @param chart JFreeChart to export
     * @param bounds the dimensions of the viewport
     * @param svgFile the output file.
     * @throws IOException if writing the svgFile fails.
     */
    public static void saveAsSVG(JFreeChart chart, Rectangle bounds, File svgFile) throws IOException {

        // Get a DOMImplementation and create an XML document
        final DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        final Document document = domImpl.createDocument(null, "svg", null);
        final SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
        final OutputStream outputStream = new FileOutputStream(svgFile);
        final Writer out;
        try {
            out = new OutputStreamWriter(outputStream, "UTF-8");
            chart.draw(svgGenerator, bounds);
            svgGenerator.stream(out, true /* use css */);
            outputStream.flush();
        }
        finally {
            outputStream.close();
        }
    }

    public JFreeChart getChart() {

        return chart;
    }

    public YIntervalSeries getSeries() {

        return series;
    }

    private JFreeChart createChart(IntervalXYDataset intervalxydataset) {

        NumberAxis x_axis = new NumberAxis("Time Thrugh Experiment (s)");
        NumberAxis y_axis = new NumberAxis(title);

        XYErrorRenderer xyerrorrenderer = new XYErrorRenderer();
        xyerrorrenderer.setBaseLinesVisible(true);

        xyerrorrenderer.setDrawYError(true);
        XYPlot xyplot = new XYPlot(intervalxydataset, x_axis, y_axis, xyerrorrenderer);
        xyplot.setDomainPannable(true);
        xyplot.setRangePannable(true);
        xyplot.setBackgroundPaint(Color.lightGray);
        xyplot.setDomainGridlinePaint(Color.white);
        xyplot.setRangeGridlinePaint(Color.white);
        xyplot.getRangeAxis().setLowerBound(0);
        JFreeChart jfreechart = new JFreeChart(title, xyplot);

        ChartUtilities.applyCurrentTheme(jfreechart);
        return jfreechart;
    }

    private IntervalXYDataset createDataset(final YIntervalSeries y_interval_series) {

        final YIntervalSeriesCollection series_collection = new YIntervalSeriesCollection();

        series_collection.addSeries(y_interval_series);
        return series_collection;
    }

    private YIntervalSeries getYIntervalSeries() throws IOException {

        final YIntervalSeries y_interval_series = new YIntervalSeries(csv.getName().replace(".csv", "").replace("_", " ").replace(" gauge", ""));

        final CsvListReader reader = new CsvListReader(new BufferedReader(new FileReader(csv)), CsvPreference.STANDARD_PREFERENCE);
        reader.getHeader(true);

        List<String> row;

        while ((row = reader.read()) != null) {

            final Long t_bucket = Long.valueOf(row.get(0));

            final Long v_count = Long.valueOf(row.get(1));
            Double v_mean = Double.valueOf(row.get(3));
            if (v_mean.equals(Double.NaN)) {
                v_mean = 0D;
            }
            final Double v_stdev = Double.valueOf(row.get(5));
            Double v_ci = v_count > 1 ? Statistics.confidenceInterval(v_count, v_stdev, 0.95D).doubleValue() : 0;
            if (v_ci.equals(Double.NaN)) {
                v_ci = 0D;
            }
            final Double v_low = v_mean - v_ci;
            final Double v_high = v_mean + v_ci;

            y_interval_series.add(t_bucket, v_mean, v_low, v_high);
        }

        reader.close();
        return y_interval_series;
    }
}
