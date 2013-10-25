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
package org.mashti.gauge;

import java.util.concurrent.TimeUnit;
import org.mashti.sina.distribution.statistic.Statistics;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Timer implements Metric {

    private final Sampler sampler;
    private final TimeUnit unit;

    public Timer() {

        this(TimeUnit.NANOSECONDS);
    }

    public Timer(TimeUnit unit) {

        this.unit = unit;
        sampler = new Sampler();
    }

    public void update(long length, TimeUnit unit) {

        sampler.update(normalizeUnit(length, unit));
    }

    public Statistics getAndReset() {

        return sampler.getAndReset();
    }

    public Time time() {

        return new Time();
    }

    public TimeUnit getUnit() {

        return unit;
    }

    protected Statistics get() {

        return sampler.get();
    }

    private long normalizeUnit(final long length, final TimeUnit unit) {

        return this.unit.convert(length, unit);
    }

    public class Time {

        private final long start_time_nanos;

        private Time() {

            start_time_nanos = System.nanoTime();
        }

        /**
         * Stops this timing and returns the elapsed time in nanoseconds.
         *
         * @return the elapsed time since the construction if this instance in nanoseconds.
         */
        public long stop() {

            final long elapsed = System.nanoTime() - start_time_nanos;
            update(elapsed, TimeUnit.NANOSECONDS);
            return elapsed;
        }

        /**
         * Gets the start time in nanoseconds.
         *
         * @return the start time in nanoseconds
         */
        public long getStartTimeInNanos() {

            return start_time_nanos;
        }
    }
}
