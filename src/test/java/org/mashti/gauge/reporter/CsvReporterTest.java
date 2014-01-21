package org.mashti.gauge.reporter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mashti.gauge.Counter;
import org.mashti.gauge.MetricRegistry;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class CsvReporterTest {

    private MetricRegistry registry;
    private CsvReporter reporter;

    @Before
    public void setUp() throws Exception {
        registry = new MetricRegistry("test_registry");
        registry.register("test_metric", new Counter());

        reporter = new CsvReporter(registry);
    }

    @After
    public void tearDown() throws Exception {

        reporter.stop();
    }

    @Test
    public void testReport() throws Exception {
                        reporter.report();
    }
}
