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
package org.mashti.gauge;

import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;

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

    public SynchronizedDescriptiveStatistics getAndReset() {

        return sampler.getAndReset();
    }

    public Time time() {

        return new Time();
    }

    public TimeUnit getUnit() {

        return unit;
    }

    protected SynchronizedDescriptiveStatistics get() {

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
