/**
 * This file is part of gauge copyright.
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.mashti.gauge.Counter;
import org.mashti.gauge.Gauge;
import org.mashti.gauge.Metric;
import org.mashti.gauge.MetricRegistry;
import org.mashti.gauge.Rate;
import org.mashti.gauge.Sampler;
import org.mashti.gauge.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class CsvReporter extends ScheduledReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvReporter.class);
    private static final File WORKING_DIRECTORY = new File(System.getProperty("user.dir"));
    private final Path reports_home;
    private final AtomicLong report_counter = new AtomicLong();
    private boolean count_as_timestamp;

    public CsvReporter(final MetricRegistry registry) {

        this(registry, WORKING_DIRECTORY);
    }

    public CsvReporter(final MetricRegistry registry, File directory) {

        this(registry, directory.toPath());
    }

    public CsvReporter(final MetricRegistry registry, Path reports_home) {

        super(registry);
        this.reports_home = reports_home;
        LOGGER.debug("CSV reporter directory is set to {}", reports_home);
    }

    public void setUseCountAsTimestamp(boolean count_as_timestamp) {

        this.count_as_timestamp = count_as_timestamp;
    }

    @Override
    public void report() {

        final long timestamp = getTimeStamp();
        for (Map.Entry<String, Metric> registered_metric : getRegistry().getRegisteredMetrics()
                .entrySet()) {
            final String name = registered_metric.getKey();
            final Metric metric = registered_metric.getValue();

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

    private long getTimeStamp() {

        final long report_count = report_counter.getAndIncrement();
        return count_as_timestamp ? report_count : System.nanoTime();
    }

    private void reportTimer(long timestamp, String name, Timer timer) {

        final SynchronizedDescriptiveStatistics statistics = timer.getAndReset();
        report(timestamp, name, "count,min,mean,max,standard_deviation,0.1th_p,1th_p,2th_p,5th_p,25th_p,50th_p,75th_p,95th_p,98th_p,99th_p,99.9th_p,unit", "%d,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%s", statistics.getN(), statistics.getMin(), statistics.getMean(), statistics.getMax(), statistics.getStandardDeviation(), statistics.getPercentile(0.1), statistics.getPercentile(1), statistics.getPercentile(2), statistics.getPercentile(5), statistics.getPercentile(25), statistics.getPercentile(50), statistics.getPercentile(75), statistics.getPercentile(95), statistics.getPercentile(98), statistics.getPercentile(99), statistics.getPercentile(99.9), timer.getUnit());
    }

    private void reportSampler(long timestamp, String name, Sampler sampler) {

        final SynchronizedDescriptiveStatistics statistics = sampler.getAndReset();
        report(timestamp, name, "count,min,mean,max,standard_deviation,0.1th_p,1th_p,2th_p,5th_p,25th_p,50th_p,75th_p,95th_p,98th_p,99th_p,99.9th_p", "%d,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f", statistics.getN(), statistics.getMin(), statistics.getMean(), statistics.getMax(), statistics.getStandardDeviation(), statistics.getPercentile(0.1), statistics.getPercentile(1), statistics.getPercentile(2), statistics.getPercentile(5), statistics.getPercentile(25), statistics.getPercentile(50), statistics.getPercentile(75), statistics.getPercentile(95), statistics.getPercentile(98), statistics.getPercentile(99), statistics.getPercentile(99.9));
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

            final Path report_path = reports_home.resolve(name + ".csv");
            final List<String> lines = new ArrayList<>();

            if (Files.notExists(report_path)) {
                lines.add("time," + header);
            }

            lines.add(String.format(String.format("%d,%s", timestamp, line), values));
            Files.write(report_path, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        }
        catch (IOException e) {
            LOGGER.warn("Error writing to {}", name, e);
        }
    }
}
