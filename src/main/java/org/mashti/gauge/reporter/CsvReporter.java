/**
 * Copyright © 2015, Masih H. Derkani
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
