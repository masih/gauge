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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.mashti.gauge.Gauge;

import static org.mashti.gauge.jvm.ThreadCpuUsageGauge.RUNTIME_MX_BEAN;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class GarbageCollectorCpuUsageGauge implements Gauge<Double> {

    static final List<GarbageCollectorMXBean> GARBAGE_COLLECTOR_MX_BEANS = ManagementFactory.getGarbageCollectorMXBeans();
    private final AtomicLong previous_start_time_millis;
    private final AtomicLong previous_total_cpu_time_millis;
    private final AtomicLong previous_total_gc_time_millis;

    public GarbageCollectorCpuUsageGauge() {

        previous_start_time_millis = new AtomicLong(RUNTIME_MX_BEAN.getStartTime());
        previous_total_cpu_time_millis = new AtomicLong();
        previous_total_gc_time_millis = new AtomicLong();
    }

    /**
     * Gets the percentage of CPU time that is collectively consumed by Garbage Collectors in the current JVM.
     * The percentage is within range {@code 0.0 <= cpu_usage_percentage <= 1.0}.
     *
     * @return the percentage of CPU time that is collectively consumed by Garbage Collectors in the current JVM as a number between {@code 0.0} and {@code 1.0} (inclusive)
     */
    @Override
    public Double get() {

        final long start_time = System.currentTimeMillis();
        final long total_gc_time = getTotalGCTimeInMillis();
        final long total_cpu_time = getTotalThreadCpuTimeInMillis();
        final long previous_total_cpu_time_millis = this.previous_total_cpu_time_millis.getAndSet(total_cpu_time);
        final long previous_total_gc_time_millis = this.previous_total_gc_time_millis.getAndSet(total_gc_time);
        final long previous_start_time_millis = this.previous_start_time_millis.getAndSet(start_time);
        
        
        return (double) (total_gc_time - previous_total_gc_time_millis) / (total_cpu_time - previous_total_cpu_time_millis) / (start_time - previous_start_time_millis);
    }

    static long getTotalThreadCpuTimeInMillis() {

        final long total_thread_cpu_time_in_nanos = ThreadCpuUsageGauge.getTotalThreadCpuTimeInNanos();
        return TimeUnit.MILLISECONDS.convert(total_thread_cpu_time_in_nanos, TimeUnit.NANOSECONDS);
    }

    static long getTotalGCTimeInMillis() {

        long total_gc_time = 0;
        for (final GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            final long collection_time = gc.getCollectionTime();
            total_gc_time += collection_time != -1 ? collection_time : 0;
        }
        return total_gc_time;
    }
}
