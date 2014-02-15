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

package org.mashti.gauge.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.mashti.gauge.Gauge;

import static java.lang.Math.max;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ThreadCpuUsageGauge implements Gauge<Double> {

    static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();
    static final RuntimeMXBean RUNTIME_MX_BEAN = ManagementFactory.getRuntimeMXBean();
    static final long JVM_START_TIME_NANOS = TimeUnit.NANOSECONDS.convert(RUNTIME_MX_BEAN.getStartTime(), TimeUnit.MILLISECONDS);
    static final int NUMBER_OF_PROCESSORS = Runtime.getRuntime().availableProcessors();
    private final AtomicLong previous_start_time;
    private final AtomicLong previous_total_cpu_time;

    public ThreadCpuUsageGauge() {

        if (!THREAD_MX_BEAN.isThreadCpuTimeSupported()) { throw new IllegalStateException("Thread CPU time is not supported by this JVM"); }
        previous_start_time = new AtomicLong(JVM_START_TIME_NANOS);
        previous_total_cpu_time = new AtomicLong();
    }

    /**
     * Gets the percentage of CPU time that is collectively consumed by threads in the current JVM.
     * The percentage is within range {@code 0.0 <= cpu_usage_percentage <= 1.0}.
     *
     * @return the percentage of CPU time that is collectively consumed by threads in the current JVM as a number between {@code 0.0} and {@code 1.0} (inclusive)
     */
    @Override
    public Double get() {

        final long start_time = System.nanoTime();
        final long total_cpu_time = getTotalThreadCpuTime();
        final long previous_total_cpu_time = this.previous_total_cpu_time.getAndSet(total_cpu_time);
        final long previous_start_time = this.previous_start_time.getAndSet(start_time);
        final double usage = (double) (total_cpu_time - previous_total_cpu_time) / (start_time - previous_start_time);
        return max(usage, 0) / NUMBER_OF_PROCESSORS;
    }

    static long getTotalThreadCpuTime() {

        long total_cpu_time = 0;
        for (long thread_id : THREAD_MX_BEAN.getAllThreadIds()) {

            final long cpu_time = THREAD_MX_BEAN.getThreadCpuTime(thread_id);
            total_cpu_time += max(cpu_time, 0);
        }
        return total_cpu_time;
    }
}
