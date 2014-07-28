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

import java.util.HashMap;
import java.util.Map;

/**
 * Presents a collection of named {@link Metric metrics}.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class MetricSet {

    private final HashMap<String, Metric> metrics;

    public MetricSet() {

        metrics = new HashMap<>();

    }

    protected Metric putMetric(String name, Metric metric) {

        return metrics.put(name, metric);
    }

    public Map<String, Metric> getMetrics() {

        return metrics;
    }
}
