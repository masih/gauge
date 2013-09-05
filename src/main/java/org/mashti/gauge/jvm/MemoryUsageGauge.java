package org.mashti.gauge.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import org.mashti.gauge.Gauge;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class MemoryUsageGauge implements Gauge<Long> {

    static final MemoryMXBean MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();

    /**
     * Gets the current usage of the heap and the non-heap memory in bytes.
     *
     * @return the current usage of the heap and the non-heap memory in bytes
     */
    @Override
    public Long get() {

        final MemoryUsage heap_usage = MEMORY_MX_BEAN.getHeapMemoryUsage();
        final MemoryUsage non_heap_usage = MEMORY_MX_BEAN.getNonHeapMemoryUsage();
        return heap_usage.getUsed() + non_heap_usage.getUsed();
    }
}
