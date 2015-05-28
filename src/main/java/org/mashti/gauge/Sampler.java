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

package org.mashti.gauge;

import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Sampler implements Metric {

    public static final int WINDOW_LIMIT = 10000;
    private final AtomicReference<SynchronizedDescriptiveStatistics> statistics;

    public Sampler() {

        statistics = new AtomicReference<SynchronizedDescriptiveStatistics>(new SynchronizedDescriptiveStatistics(WINDOW_LIMIT));
    }

    public SynchronizedDescriptiveStatistics getAndReset() {

        return statistics.getAndSet(new SynchronizedDescriptiveStatistics(WINDOW_LIMIT));
    }

    public void update(double sample) {

        get().addValue(sample);
    }

    protected SynchronizedDescriptiveStatistics get() {

        return statistics.get();
    }
}
