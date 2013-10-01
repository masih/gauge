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
