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
package org.mashti.gauge.reporter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.mashti.gauge.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public abstract class ScheduledReporter implements Reporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledReporter.class);
    private final MetricRegistry registry;
    private final ScheduledExecutorService scheduler;
    private volatile ScheduledFuture<?> scheduled_report;

    public ScheduledReporter(MetricRegistry registry) {

        this.registry = registry;
        scheduler = constructSchedulerService(registry);
    }

    public synchronized void start(long interval, TimeUnit unit) {

        if (!isReportScheduled()) {
            scheduled_report = scheduler.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {

                    LOGGER.debug("reporter started for registry {}", registry.getName());
                    try {
                        report();
                    }
                    catch (final Exception e) {
                        LOGGER.error("failure occurred while reporting", e);
                    }
                    finally {
                        LOGGER.debug("reporter stopped for registry {}", registry.getName());
                    }
                }
            }, 0, interval, unit);
        }
    }

    public synchronized void stop() {

        if (isReportScheduled()) {
            scheduled_report.cancel(false);
        }
    }

    public synchronized boolean isReportScheduled() {

        return scheduled_report != null && !scheduled_report.isDone();
    }

    protected MetricRegistry getRegistry() {

        return registry;
    }

    private static ScheduledExecutorService constructSchedulerService(final MetricRegistry registry) {

        final SecurityManager s = System.getSecurityManager();
        return Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

            private AtomicInteger counter = new AtomicInteger();
            private final ThreadGroup group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();

            @Override
            public Thread newThread(final Runnable r) {

                final Thread thread = new Thread(group, r, registry.getName() + "_reporter_" + counter.getAndIncrement(), 0);
                configure(thread);
                return thread;
            }

            private void configure(final Thread thread) {

                thread.setDaemon(true);
                thread.setPriority(Thread.MAX_PRIORITY);
            }
        });
    }
}
