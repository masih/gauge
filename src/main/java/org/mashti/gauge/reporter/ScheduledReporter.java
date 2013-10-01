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
                        LOGGER.error("failure occured while reporting", e);
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
            report();
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
