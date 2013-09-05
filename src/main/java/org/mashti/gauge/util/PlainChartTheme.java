/*
 * Copyright 2013 Masih Hajiarabderkani
 *
 * This file is part of Trombone.
 *
 * Trombone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Trombone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Trombone.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mashti.gauge.util;

import java.awt.Color;
import java.awt.Font;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;

public class PlainChartTheme extends StandardChartTheme implements ChartTheme {

    public static final Font LARGE_FONT = new Font("CMU Bright", Font.PLAIN, 20);
    private static final Font SMALL_FONT = LARGE_FONT.deriveFont(12f);
    private static final Font SMALLEST_FONT = LARGE_FONT.deriveFont(10f);
    private static final Font MEDIUM_FONT = SMALL_FONT.deriveFont(14f);

    public PlainChartTheme() {

        super("plain");
    }

    @Override
    public DrawingSupplier getDrawingSupplier() {

        return super.getDrawingSupplier();
    }

    @Override
    public void apply(final JFreeChart chart) {

        super.apply(chart);
        chart.getTitle().setFont(LARGE_FONT);
        final LegendTitle legend = chart.getLegend();
        decorateLegend(legend);

        final Plot plot = chart.getPlot();

        decoratePlot(plot);
        if (plot instanceof XYPlot) {
            final XYPlot xy_plot = chart.getXYPlot();
            decorateXYPlot(xy_plot);
        }
        else if (plot instanceof CategoryPlot) {
            final CategoryPlot category_plot = chart.getCategoryPlot();
            decorateCategoryPlot(category_plot);
        }
    }

    private void decorateCategoryPlot(final CategoryPlot category_plot) {

        throw new UnsupportedOperationException("category plot decoration is not supported by this theme yet!");

    }

    private void decorateXYPlot(final XYPlot xy_plot) {

        final ValueAxis domain_axis = xy_plot.getDomainAxis();
        decorateValueAxis(domain_axis);
        final ValueAxis range_axis = xy_plot.getRangeAxis();
        decorateValueAxis(range_axis);
        xy_plot.setDomainGridlinesVisible(false);
        xy_plot.setRangeGridlinesVisible(false);
    }

    private void decorateValueAxis(final ValueAxis axis) {

        axis.setAxisLinePaint(Color.BLACK);
        //        axis.setAxisLineStroke(new BasicStroke(2f));
        axis.setTickLabelFont(SMALL_FONT);
        axis.setTickLabelPaint(Color.BLACK);
        axis.setTickMarkPaint(Color.BLACK);
        axis.setLabelFont(MEDIUM_FONT);
    }

    private void decoratePlot(final Plot plot) {

        plot.setOutlineVisible(false);
        plot.setBackgroundPaint(Color.WHITE);
    }

    private void decorateLegend(final LegendTitle legend) {

        legend.setItemFont(SMALLEST_FONT);
        legend.setBorder(0, 0, 0, 0);
    }
}
