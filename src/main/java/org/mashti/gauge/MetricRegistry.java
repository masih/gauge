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

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class MetricRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricRegistry.class);
    private final Map<String, Metric> registered_metrics;
    private final String name;

    public MetricRegistry(String name) {

        this.name = name;
        registered_metrics = new LinkedHashMap<>(); // respect insertion order
    }

    public Metric register(String name, Metric metric) {

        return registered_metrics.put(name, metric);
    }

    public synchronized void registerAll(MetricSet metric_set) {

        for (Map.Entry<String, Metric> metric_entry : metric_set.getMetrics().entrySet()) {

            final String name = metric_entry.getKey();
            final Metric metric = metric_entry.getValue();

            final Metric replaced = register(name, metric);
            if (replaced != null) {
                LOGGER.warn("metric named {} replaced {}", name, replaced);
            }
        }
    }

    public Map<String, Metric> getRegisteredMetrics() {

        return registered_metrics;
    }

    public String getName() {

        return name;
    }
}
