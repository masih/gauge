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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArraySet;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class MetricRegistry {

    private final ConcurrentSkipListMap<String, Metric> metrics;
    private final String name;

    public MetricRegistry(String name) {

        this.name = name;

        metrics = new ConcurrentSkipListMap<String, Metric>();
    }

    public boolean register(String name, Metric metric) {

        return metrics.putIfAbsent(name, metric) == null;
    }

    public Set<Map.Entry<String, Metric>> getMetrics() {

        return new CopyOnWriteArraySet<Map.Entry<String, Metric>>(metrics.entrySet());
    }

    public String getName() {

        return name;
    }
}
