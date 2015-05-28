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
import java.util.concurrent.atomic.AtomicLong;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Rate implements Metric {

    private final Counter counter;
    private final AtomicLong start_time_nanos;
    private final TimeUnit unit;

    public Rate() {

        this(TimeUnit.SECONDS);
    }

    public Rate(TimeUnit unit) {

        this.unit = unit;
        counter = new Counter();
        start_time_nanos = new AtomicLong(System.nanoTime());
    }

    public void mark() {

        mark(1);
    }

    public void mark(long n) {

        counter.add(n);
    }

    public double getRate() {

        return getRate(false);
    }

    public double getRateAndReset() {

        return getRate(true);
    }

    public long getCount() {

        return counter.get();
    }

    public TimeUnit getUnit() {

        return unit;
    }

    private double getRate(boolean reset) {

        final double count = reset ? counter.getAndReset() : getCount();
        if (count == 0) { return 0; }

        final long now = System.nanoTime();
        final long elapsed = now - (reset ? start_time_nanos.getAndSet(now) : start_time_nanos.get());
        final long time = unit.convert(elapsed, TimeUnit.NANOSECONDS);
        return count / (time < 1 ? 1 : time);
    }
}
