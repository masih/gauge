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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class MetricRegistry {

    private final List<RegisteredMetric> registered_metrics;
    private final String name;

    public MetricRegistry(String name) {

        this.name = name;
        registered_metrics = new ArrayList<>();
    }

    public synchronized boolean register(String name, Metric metric) {

        return registered_metrics.add(new RegisteredMetric(name, metric));
    }

    public List<RegisteredMetric> getRegisteredMetrics() {

        return new CopyOnWriteArrayList<RegisteredMetric>(registered_metrics);
    }

    public String getName() {

        return name;
    }

    public static class RegisteredMetric {

        private final String name;
        private final Metric metric;

        RegisteredMetric(final String name, final Metric metric) {

            this.name = name;
            this.metric = metric;
        }

        public String getName() {

            return name;
        }

        public Metric getMetric() {

            return metric;
        }
    }
}
