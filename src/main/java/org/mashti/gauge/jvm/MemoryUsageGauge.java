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

package org.mashti.gauge.jvm;

import org.mashti.gauge.Gauge;

/**
 * Measures heap and non-heap memory usage of this JVM in bytes.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class MemoryUsageGauge implements Gauge<Long> {

    private static final HeapMemoryUsageGauge HEAP_MEMORY_USAGE_GAUGE = new HeapMemoryUsageGauge();
    private static final NonHeapMemoryUsageGauge NON_HEAP_MEMORY_USAGE_GAUGE = new NonHeapMemoryUsageGauge();

    /**
     * Gets the current usage of the heap and the non-heap memory in bytes.
     *
     * @return the current usage of the heap and the non-heap memory in bytes
     */
    @Override
    public Long get() {

        return HEAP_MEMORY_USAGE_GAUGE.get() + NON_HEAP_MEMORY_USAGE_GAUGE.get();
    }
}
