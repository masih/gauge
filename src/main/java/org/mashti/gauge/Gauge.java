package org.mashti.gauge;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public interface Gauge<Value extends Number> extends Metric {

    Value get();

}
