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
package org.mashti.gauge.ganglia;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.mashti.gauge.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import static org.apache.commons.io.IOUtils.closeQuietly;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class GangliaMetricGauge implements Gauge<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GangliaMetricGauge.class);
    private static final int DEFAULT_GMETAD_INTERACTIVE_PORT = 8652;
    private static final String LOCALHOST = "localhost";
    private static final String METRIC_TAG_NAME = "METRIC";
    private static final String METRIC_NAME_ATTRIBUTE = "NAME";
    private static final String METRIC_VALUE_ATTRIBUTE = "VAL";
    private static final String DELIMITER = "/";
    private static final SAXParserFactory SAX_PARSER_FACTORY = SAXParserFactory.newInstance();
    private final String host_name;
    private final int port;
    private final String metric_name;
    private final String query;
    private final MetricValueHandler handler;
    private final XMLReader xml_reader;
    private final SAXParser parser;

    public GangliaMetricGauge(String cluster_name, String node_name, String metric_name) throws ParserConfigurationException, SAXException {

        this(LOCALHOST, DEFAULT_GMETAD_INTERACTIVE_PORT, cluster_name, node_name, metric_name);
    }

    public GangliaMetricGauge(String host_name, int gmetad_interactive_port, String cluster_name, String node_name, String metric_name) throws ParserConfigurationException, SAXException {

        this.host_name = host_name;
        this.metric_name = metric_name;
        port = gmetad_interactive_port;
        query = constructQuery(cluster_name, node_name, metric_name);
        handler = new MetricValueHandler();
        parser = SAX_PARSER_FACTORY.newSAXParser();
        xml_reader = parser.getXMLReader();
        xml_reader.setContentHandler(handler);
    }

    @Override
    public String get() {

        String result;
        Socket connection = null;
        try {
            connection = connect();
            writeQuery(connection);
            result = readResult(connection);
        }
        catch (Exception e) {
            LOGGER.error("failed to query {} on host {}:{}. cause: {}", query, host_name, port, e);
            LOGGER.debug("failure to query ganglia", e);
            result = null;
        }
        finally {
            closeQuietly(connection);
        }

        return result;
    }

    private String readResult(final Socket connection) throws IOException, SAXException {

        xml_reader.parse(new InputSource(connection.getInputStream()));
        return handler.value;
    }

    private void writeQuery(final Socket connection) throws IOException {

        final PrintWriter writer = new PrintWriter(connection.getOutputStream());
        writer.println(query);
        writer.flush();
    }

    private Socket connect() throws IOException {

        return new Socket(host_name, port);
    }

    private static String constructQuery(final String cluster_name, final String node_name, final String metric_name) {

        return String.format("%s%s%s%s%s%s", DELIMITER, cluster_name, DELIMITER, node_name, DELIMITER, metric_name);
    }

    private class MetricValueHandler extends DefaultHandler {

        private String value;

        @Override
        public void startElement(final String uri, final String local_name, final String q_name, final Attributes attributes) throws SAXException {

            if (q_name.equals(METRIC_TAG_NAME)) {
                String name = attributes.getValue(METRIC_NAME_ATTRIBUTE);
                if (name.equals(metric_name)) {
                    value = attributes.getValue(METRIC_VALUE_ATTRIBUTE);
                }
            }
        }
    }
}
