package org.mashti.gauge.jvm;

import org.mashti.gauge.Gauge;

import static org.mashti.gauge.jvm.ThreadCpuUsageGauge.THREAD_MX_BEAN;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class ThreadCountGauge implements Gauge<Integer> {

    /**
     * Gets the current number of live threads including both daemon and non-daemon threads.
     *
     * @return the current number of live threads including both daemon and non-daemon threads.
     */
    @Override
    public Integer get() {

        return THREAD_MX_BEAN.getThreadCount();
    }
}
