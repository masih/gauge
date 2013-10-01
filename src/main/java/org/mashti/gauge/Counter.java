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

import org.mashti.gauge.util.LongAdder;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class Counter implements Metric {

    LongAdder long_adder = new LongAdder();

    public void increment() {

        add(1);
    }

    public void decrement() {

        subtract(1);
    }

    public void subtract(long n) {

        add(-1 * n);
    }

    public void add(long n) {

        long_adder.add(n);
    }

    public long get() {

        return long_adder.sum();
    }

    public long getAndReset() {

        return long_adder.sumThenReset();
    }
}
