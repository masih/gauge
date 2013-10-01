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
package org.mashti.gauge.reporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.mashti.gauge.Counter;
import org.mashti.gauge.Gauge;
import org.mashti.gauge.Metric;
import org.mashti.gauge.MetricRegistry;
import org.mashti.gauge.Rate;
import org.mashti.gauge.Sampler;
import org.mashti.gauge.Timer;
import org.mashti.sina.distribution.statistic.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class CsvReporter extends ScheduledReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvReporter.class);
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private final File directory;

    public CsvReporter(final MetricRegistry registry, File directory) {

        super(registry);
        this.directory = directory;
    }

    @Override
    public void report() {

        final long timestamp = System.nanoTime();
        for (Map.Entry<String, Metric> entry : getRegistry().getMetrics()) {
            final String name = entry.getKey();
            final Metric metric = entry.getValue();

            if (metric instanceof Counter) {
                Counter counter = (Counter) metric;
                reportCounter(timestamp, name, counter);
            }
            else if (metric instanceof Rate) {
                Rate rate = (Rate) metric;
                reportRate(timestamp, name, rate);
            }
            else if (metric instanceof Sampler) {
                Sampler sampler = (Sampler) metric;
                reportSampler(timestamp, name, sampler);
            }
            else if (metric instanceof Timer) {
                Timer timer = (Timer) metric;
                reportTimer(timestamp, name, timer);
            }
            else if (metric instanceof Gauge) {
                Gauge<?> gauge = (Gauge<?>) metric;
                reportGauge(timestamp, name, gauge);
            }
            else {
                LOGGER.warn("unknown metric {}, named {}: skipped from csv report at time {}", metric, name, timestamp);
            }
        }
    }

    private void reportTimer(long timestamp, String name, Timer timer) {

        final Statistics statistics = timer.getAndReset();
        report(timestamp, name, "count,min,mean,max,standard_deviation,0.1th_p,1th_p,2th_p,5th_p,25th_p,50th_p,75th_p,95th_p,98th_p,99th_p,99.9th_p,unit", "%d,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%s", statistics.getSampleSize(), statistics.getMin(), statistics.getMean(),
                        statistics.getMax(), statistics.getStandardDeviation(), statistics.getPercentile(0.1), statistics.getPercentile(1), statistics.getPercentile(2), statistics.getPercentile(5), statistics.getPercentile(25), statistics.getPercentile(50), statistics.getPercentile(75),
                        statistics.getPercentile(95), statistics.getPercentile(98), statistics.getPercentile(99), statistics.getPercentile(99.9), timer.getUnit());
    }

    private void reportSampler(long timestamp, String name, Sampler sampler) {

        final Statistics statistics = sampler.getAndReset();
        report(timestamp, name, "count,min,mean,max,standard_deviation,0.1th_p,1th_p,2th_p,5th_p,25th_p,50th_p,75th_p,95th_p,98th_p,99th_p,99.9th_p", "%d,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f", statistics.getSampleSize(), statistics.getMin(), statistics.getMean(), statistics.getMax(),
                        statistics.getStandardDeviation(), statistics.getPercentile(0.1), statistics.getPercentile(1), statistics.getPercentile(2), statistics.getPercentile(5), statistics.getPercentile(25), statistics.getPercentile(50), statistics.getPercentile(75), statistics.getPercentile(95),
                        statistics.getPercentile(98), statistics.getPercentile(99), statistics.getPercentile(99.9));
    }

    private void reportRate(long timestamp, String name, Rate rate) {

        report(timestamp, name, "count,rate,rate_unit", "%d,%f,calls/%s", rate.getCount(), rate.getRateAndReset(), rate.getUnit());
    }

    private void reportCounter(long timestamp, String name, Counter counter) {

        report(timestamp, name, "count", "%d", counter.get());
    }

    private void reportGauge(long timestamp, String name, Gauge<?> gauge) {

        report(timestamp, name, "value", "%s", gauge.get());
    }

    private void report(long timestamp, String name, String header, String line, Object... values) {

        try {
            final File file = new File(directory, name + ".csv");
            final boolean already_exists = file.exists();
            if (already_exists || file.createNewFile()) {
                PrintWriter out = null;
                try {
                    out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true), UTF_8));
                    if (!already_exists) {
                        out.println("time," + header);
                    }
                    out.printf(String.format("%d,%s%n", timestamp, line), values);
                }
                finally {
                    IOUtils.closeQuietly(out);
                }
            }
        }
        catch (IOException e) {
            LOGGER.warn("Error writing to {}", name, e);
        }
    }
}
