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

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import org.mashti.gauge.Gauge;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class SystemLoadAverageGauge implements Gauge<Double> {

    static final OperatingSystemMXBean OS_MX_BEAN = ManagementFactory.getOperatingSystemMXBean();

    /**
     * Gets the system load average for the last minute.
     *
     * @return the system load average for the last minute
     * @see OperatingSystemMXBean#getSystemLoadAverage()
     */
    @Override
    public Double get() {

        return OS_MX_BEAN.getSystemLoadAverage();
    }
}