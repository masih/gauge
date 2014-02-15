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

package org.mashti.gauge.jvm;

import org.mashti.gauge.Gauge;

import static org.mashti.gauge.jvm.HeapMemoryUsageGauge.MEMORY_MX_BEAN;

/**
 * Measures non-heap  memory usage of this JVM in bytes.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class NonHeapMemoryUsageGauge implements Gauge<Long> {

    /**
     * Gets the current usage of the non-heap memory in bytes.
     *
     * @return the current usage of the non-heap memory in bytes
     */
    @Override
    public Long get() {

        return MEMORY_MX_BEAN.getNonHeapMemoryUsage().getUsed();
    }
}
