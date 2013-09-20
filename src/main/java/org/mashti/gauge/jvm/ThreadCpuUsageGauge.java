package org.mashti.gauge.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.mashti.gauge.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ThreadCpuUsageGauge implements Gauge<Float> {

    static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();
    static final RuntimeMXBean RUNTIME_MX_BEAN = ManagementFactory.getRuntimeMXBean();
    static final OperatingSystemMXBean OPERATING_SYSTEM_MX_BEAN = ManagementFactory.getOperatingSystemMXBean();
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadCpuUsageGauge.class);
    private static final int AVAILABLE_PROCESSORS = OPERATING_SYSTEM_MX_BEAN.getAvailableProcessors();
    private final AtomicLong previous_time;
    private final AtomicLong previous_thread_time;

    public ThreadCpuUsageGauge() {

        previous_time = new AtomicLong(getJvmStartTimeInNanos());
        previous_thread_time = new AtomicLong(getTotalThreadCpuTime());
    }

    @Override
    public Float get() {

        final long total_thread_cpu_time = getTotalThreadCpuTime();
        final long time = System.nanoTime();
        final double load = (total_thread_cpu_time - previous_thread_time.getAndSet(total_thread_cpu_time)) / ((double) (time - previous_time.getAndSet(time)) * AVAILABLE_PROCESSORS);
        return (float) load;
    }

    static long getJvmStartTimeInNanos() {

        return TimeUnit.NANOSECONDS.convert(RUNTIME_MX_BEAN.getStartTime(), TimeUnit.MILLISECONDS);
    }

    static long getTotalThreadCpuTime() {

        long time = 0;
        if (THREAD_MX_BEAN.isThreadCpuTimeSupported()) {
            for (long thread_id : THREAD_MX_BEAN.getAllThreadIds()) {
                final long cpu_time = THREAD_MX_BEAN.getThreadCpuTime(thread_id);
                if (cpu_time != -1) {
                    time += cpu_time;
                }
            }
        }
        else {
            LOGGER.warn("Thread CPU time is not supported by this JVM");
        }
        return time;
    }
}
