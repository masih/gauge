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
        final long total_cpu_time = getTotalThreadCpuTimeInNanos();
        final long previous_total_cpu_time = this.previous_total_cpu_time.getAndSet(total_cpu_time);
        final long previous_start_time = this.previous_start_time.getAndSet(start_time);
        final double usage = (double) (total_cpu_time - previous_total_cpu_time) / (start_time - previous_start_time);
        return max(usage, 0) / NUMBER_OF_PROCESSORS;
    }

    static long getTotalThreadCpuTimeInNanos() {

        long total_cpu_time = 0;
        for (long thread_id : THREAD_MX_BEAN.getAllThreadIds()) {

            final long cpu_time = THREAD_MX_BEAN.getThreadCpuTime(thread_id);
            total_cpu_time += max(cpu_time, 0);
        }
        return total_cpu_time;
    }
}
