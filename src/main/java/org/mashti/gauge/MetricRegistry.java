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
