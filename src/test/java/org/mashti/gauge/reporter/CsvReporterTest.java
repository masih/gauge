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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mashti.gauge.Counter;
import org.mashti.gauge.MetricRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
public class CsvReporterTest {

    public static final String TEST_METRIC_NAME = "test_metric";
    private MetricRegistry registry;
    private CsvReporter reporter;
    private Path reports_home;
    private Path test_report_path;

    @Before
    public void setUp() throws Exception {

        registry = new MetricRegistry("test_registry");
        registry.register(TEST_METRIC_NAME, new Counter());
        reports_home = Files.createTempDirectory("test_dir");
        test_report_path = reports_home.resolve(TEST_METRIC_NAME + ".csv");
        reporter = new CsvReporter(registry, reports_home);
    }

    @After
    public void tearDown() throws Exception {
        Files.delete(test_report_path);
        Files.delete(reports_home);
        reporter.stop();
    }

    @Test
    public void testReport() throws Exception {

        assertTrue(Files.exists(reports_home));
        assertFalse(Files.exists(test_report_path));
        reporter.report();
        assertTrue(Files.exists(test_report_path));
        assertEquals(2, Files.readAllLines(test_report_path, StandardCharsets.UTF_8).size());
        reporter.report();
        assertEquals(3, Files.readAllLines(test_report_path, StandardCharsets.UTF_8).size());
    }
}
