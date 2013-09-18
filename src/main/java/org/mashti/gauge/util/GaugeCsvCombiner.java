package org.mashti.gauge.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.mashti.sina.distribution.statistic.Statistics;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.LongCellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class GaugeCsvCombiner {

    private final List<File> csvs;

    public GaugeCsvCombiner() {

        csvs = new ArrayList<File>();
    }

    public synchronized void addRepetitions(File directory, String csv_file_name) throws FileNotFoundException {

        final File[] sub_directories = directory.listFiles(new FileFilter() {

            @Override
            public boolean accept(final File pathname) {

                return pathname.isDirectory();
            }
        });

        for (File sub_directory : sub_directories) {
            final File csv = new File(sub_directory, csv_file_name);
            if (!csv.isFile()) { throw new FileNotFoundException("not found: " + csv); }
            addCsvFile(csv);
        }
    }

    public synchronized boolean addCsvFile(File csv) {

        return csvs.add(csv);
    }

    public synchronized void combine(File destination) throws IOException {

        final List<CellProcessor[]> processors = new ArrayList<CellProcessor[]>();
        final List<CsvListReader> readers = new ArrayList<CsvListReader>();

        for (File csv : csvs) {
            final CellProcessor[] processor = getCellProcessors();
            processors.add(processor);

            final CsvListReader reader = getCsvListReader(csv);
            readers.add(reader);
            reader.getHeader(true);
        }

        final CsvListWriter writer = new CsvListWriter(new FileWriter(destination), CsvPreference.STANDARD_PREFERENCE);
        writer.writeHeader("time_count", "time_min", "time_mean", "time_max", "time_stdev", "value_count", "value_min", "value_mean", "value_max", "value_stdev");

        while (!readers.isEmpty()) {

            final Statistics time_statistics = new Statistics();
            final Statistics value_statistics = new Statistics();
            final Iterator<CsvListReader> readers_itterator = readers.iterator();
            final Iterator<CellProcessor[]> processors_itterator = processors.iterator();

            while (readers_itterator.hasNext()) {
                final CsvListReader reader = readers_itterator.next();
                final CellProcessor[] processor = processors_itterator.next();
                final List<Object> row = reader.read(processor);
                if (row != null) {
                    time_statistics.addSample((Number) row.get(0));
                    value_statistics.addSample((Number) row.get(1));
                }
                else {
                    reader.close();
                    readers_itterator.remove();
                    processors_itterator.remove();
                }
            }
            writer.write(time_statistics.getSampleSize(), time_statistics.getMin(), time_statistics.getMean(), time_statistics.getMax(), time_statistics.getStandardDeviation(), value_statistics.getSampleSize(), value_statistics.getMin(), value_statistics.getMean(), value_statistics.getMax(), value_statistics.getStandardDeviation());
            writer.flush();

        }
        writer.close();
    }

    private CsvListReader getCsvListReader(final File csv) throws FileNotFoundException {

        return new CsvListReader(new FileReader(csv), CsvPreference.STANDARD_PREFERENCE);
    }

    private CellProcessor[] getCellProcessors() {

        return new CellProcessor[] {new ParseRelativeTime(new ConvertTime(TimeUnit.NANOSECONDS, TimeUnit.SECONDS)), new ParseDouble()};
    }

    class ParseRelativeTime extends ParseLong implements LongCellProcessor {

        private Long first_time;

        ParseRelativeTime() {

        }

        ParseRelativeTime(final LongCellProcessor next) {

            super(next);
        }

        @Override
        public Object execute(final Object value, final CsvContext context) {

            final Long time = (Long) super.execute(value, context);
            if (first_time == null) {
                first_time = time;
            }
            final Long relative_time = time - first_time;
            return relative_time;
        }
    }

    class ConvertTime extends CellProcessorAdaptor implements LongCellProcessor {

        private final TimeUnit source_unit;
        private final TimeUnit target_unit;

        ConvertTime(TimeUnit source_unit, TimeUnit target_unit) {

            this.source_unit = source_unit;
            this.target_unit = target_unit;
        }

        @Override
        public Object execute(final Object value, final CsvContext context) {

            return next.execute(target_unit.convert((Long) value, source_unit), context);
        }
    }
}