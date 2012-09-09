/*
 * Copyright (c) 2010-2012, University of Sussex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of the University of Sussex nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.susx.mlcl.byblo.commands;

import com.google.common.io.Files;
import it.unimi.dsi.fastutil.ints.IntIterator;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import uk.ac.susx.mlcl.TestConstants;
import uk.ac.susx.mlcl.byblo.Tools;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.PoissonDistribution;
import uk.ac.susx.mlcl.lib.ZipfianIntGenerator;
import uk.ac.susx.mlcl.lib.commands.AbstractCommandTest;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import uk.ac.susx.mlcl.testing.SlowTestCategory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertTrue;
import static uk.ac.susx.mlcl.TestConstants.*;
import static uk.ac.susx.mlcl.lib.test.ExitTrapper.disableExitTrapping;
import static uk.ac.susx.mlcl.lib.test.ExitTrapper.enableExistTrapping;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class FilterCommandTest extends AbstractCommandTest<FilterCommand> {

    @Override
    public Class<? extends FilterCommand> getImplementation() {
        return FilterCommand.class;
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        OUTPUT_ENTRIES.delete();
        OUTPUT_FEATURES.delete();
        OUTPUT_ENTRY_FEATURES.delete();
    }

    private static final String SUBJECT = FilterCommand.class.getName();

    private final static File OUTPUT_ENTRIES = new File(TEST_OUTPUT_DIR,
            TEST_FRUIT_ENTRIES_FILTERED.getName());

    private final static File OUTPUT_FEATURES = new File(TEST_OUTPUT_DIR,
            TEST_FRUIT_FEATURES_FILTERED.getName());

    private final static File OUTPUT_ENTRY_FEATURES = new File(TEST_OUTPUT_DIR,
            TEST_FRUIT_EVENTS_FILTERED.getName());

    private void runWithCLI(String[] runArgs) throws Exception {

        String[] commonArgs = {"filter", "--charset", "UTF-8",
                "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                "--input-features", TEST_FRUIT_FEATURES.toString(),
                "--input-events", TEST_FRUIT_EVENTS.toString(),
                "--output-entries", OUTPUT_ENTRIES.toString(),
                "--output-features", OUTPUT_FEATURES.toString(),
                "--output-events", OUTPUT_ENTRY_FEATURES.toString(),
                "--temp-dir", TEST_TMP_DIR.toString()};

        String[] args = new String[commonArgs.length + runArgs.length];
        System.arraycopy(commonArgs, 0, args, 0, commonArgs.length);
        System.arraycopy(runArgs, 0, args, commonArgs.length, runArgs.length);

        try {
            enableExistTrapping();
            Tools.main(args);
        } finally {
            disableExitTrapping();
        }

        assertTrue("Output file " + OUTPUT_ENTRIES + " does not exist.",
                OUTPUT_ENTRIES.exists());
        assertTrue("Output file " + OUTPUT_FEATURES + " does not exist.",
                OUTPUT_FEATURES.exists());
        assertTrue("Output file " + OUTPUT_ENTRY_FEATURES + " does not exist.",
                OUTPUT_ENTRY_FEATURES.exists());
    }

    @Test
    public void testMainMethodRun_fruit_entryFreqFilter() throws Exception {
        System.out.println("Testing " + SUBJECT
                + ": on fruit, from main method, filter by Entry frequency.");

        runWithCLI(new String[]{"--filter-entry-freq", "50"});
    }

    @Test
    public void testMainMethodRun_fruit_featureFreqFilter() throws Exception {
        System.out.println("Testing " + SUBJECT
                + " on fruit from main method, filter by context frequency.");

        runWithCLI(new String[]{"--filter-feature-freq", "50"});
    }

    @Test
    public void testMainMethodRun_fruit_eventFreqFilter() throws Exception {
        System.out
                .println("Testing FilterTask on fruit from main method, filter by feature frequency.");

        runWithCLI(new String[]{"--filter-event-freq", "5"});
    }

    @Test
    public void testMainMethodRun_fruit_EntryWhitelistFilter() throws Exception {
        System.out
                .println("Testing FilterTask: on fruit, from main method, filter by entry word list.");

        File entryWorldList = new File(TEST_OUTPUT_DIR,
                TEST_FRUIT_INPUT.getName() + ".entry-whitelist");

        Files.write("apple\norange\npear\nbanana", entryWorldList,
                Charset.forName("UTF-8"));

        runWithCLI(new String[]{"--filter-entry-whitelist",
                entryWorldList.toString()});
    }

    @Test
    public void testMainMethodRun_fruit_featureWhitelistFilter()
            throws Exception {
        System.out
                .println("Testing FilterTask: on fruit, from main method, filter by context word list.");

        File contextWorldList = new File(TEST_OUTPUT_DIR,
                TEST_FRUIT_INPUT.getName() + ".contextWordList");

        Files.write("det:the\ndet:a\niobj:of\nncmod:back\nncmod:for\npassive",
                contextWorldList, Charset.forName("UTF-8"));

        runWithCLI(new String[]{"--filter-feature-whitelist",
                contextWorldList.toString()});
    }

    @Test
    public void testMainMethodRun_fruit_entryPatternFilter() throws Exception {
        System.out
                .println("Testing FilterTask: on fruit, from main method, filter by entry pattern.");

        runWithCLI(new String[]{"--filter-entry-pattern", "^.{0,5}$"});
    }

    @Test
    public void testMainMethodRun_fruit_contextPatternFilter() throws Exception {
        System.out
                .println("Testing FilterTask: on fruit, from main method, filter by context pattern.");

        runWithCLI(new String[]{"--filter-feature-pattern", "det:.*"});
    }

    @Test
    public void testMainMethodRun_fruit_allFilters() throws Exception {
        System.out
                .println("Testing FilterTask: on fruit, from main method, using all filters.");

        File entryWorldList = new File(TEST_OUTPUT_DIR, FRUIT_NAME
                + ".allFilters-EntryList");
        File contextWorldList = new File(TEST_OUTPUT_DIR, FRUIT_NAME
                + ".allFilters-contextWordList");

        Files.write("apple\napricot\navocado\nbanana\nbilberry\nblackberry\n"
                + "blackcap\nblackcurrant\nblueberry\ncantaloupe\ncherry\n"
                + "clementine\ncurrant\ndamson\ndate\ndurian\neggplant\n"
                + "elderberry\ngooseberry\ngrape\ngrapefruit\nhuckleberry\n"
                + "kumquat", entryWorldList, Charset.forName("UTF-8"));

        Files.write("det:the\ndet:a\niobj:of\nncmod:back\nncmod:for\npassive",
                contextWorldList, Charset.forName("UTF-8"));

        runWithCLI(new String[]{
                "--filter-entry-whitelist",
                entryWorldList.toString(), // a-k
                "--filter-entry-freq", "10", "--filter-feature-freq", "5",
                "--filter-event-freq", "2", "--filter-feature-pattern", ":",
                "--filter-feature-whitelist", contextWorldList.toString(),
                "--filter-entry-pattern", "a"});
    }

    @Test
    @Ignore(value = "Takes a rather a long time.")
    @Category(SlowTestCategory.class)
    public void testFilterCommandOnWorstCaseData() throws Exception {
        System.out.println("testFilterCommandOnWorstCaseData()");

        // worst case is that you never see an entry of feature twice
        final int nEntries = 1 << 12;
        final int nFeaturesPerEntry = 1 << 12;

        final String inFileNamePrefix = String.format(
                "testFilterCommandOnWorstCaseData-%dx%d", nEntries,
                nFeaturesPerEntry);
        final File inEvents = new File(TEST_OUTPUT_DIR, inFileNamePrefix
                + "-events");
        final File inEntries = new File(TEST_OUTPUT_DIR, inFileNamePrefix
                + "-entries");
        final File inFeatures = new File(TEST_OUTPUT_DIR, inFileNamePrefix
                + "-features");

        // Create the test data if necessary
        if (!inEvents.exists()) {
            generateUniqueEventsData(inEntries, inFeatures, inEvents, nEntries,
                    nFeaturesPerEntry);
        }

        final File outEvents = new File(TEST_OUTPUT_DIR, inEvents.getName()
                + "-filtered");
        final File outEntries = new File(TEST_OUTPUT_DIR, inEntries.getName()
                + "-filtered");
        final File outFeatures = new File(TEST_OUTPUT_DIR, inFeatures.getName()
                + "-filtered");

        assertValidPlaintextInputFiles(inEvents, inEntries, inFeatures);
        assertValidOutputFiles(outEvents, outEntries, outFeatures);
        deleteIfExist(outEvents, outEntries, outFeatures);

        final FilterCommand filter = new FilterCommand();
        filter.setCharset(DEFAULT_CHARSET);
        filter.setIndexDelegate(new DoubleEnumeratingDelegate(
                Enumerating.DEFAULT_TYPE, true, true, null, null));
        filter.setTempFiles(new TempFileFactory(TEST_TMP_DIR));
        filter.setInputEntriesFile(inEntries);
        filter.setInputFeaturesFile(inFeatures);
        filter.setInputEventsFile(inEvents);
        filter.setOutputEntriesFile(outEntries);
        filter.setOutputFeaturesFile(outFeatures);
        filter.setOutputEventsFile(outEvents);

        filter.setFilterEntryMinFreq(2);

        filter.addProgressListener(new TestConstants.InfoProgressListener());
        filter.runCommand();

        assertValidPlaintextInputFiles(outEntries, outFeatures, outEvents);
    }

    @Test
    @Ignore(value = "Takes a rather a long time.")
    @Category(SlowTestCategory.class)
    public void testFilterCommandOnWorstCaseData2() throws Exception {
        System.out.println("testFilterCommandOnWorstCaseData()");

        // worst case is that you never see an entry of feature twice
        final int nEntries = 1 << 12;
        final int nFeaturesPerEntry = 1 << 12;

        final String inFileNamePrefix = String.format(
                "testFilterCommandOnWorstCase2Data-%dx%d", nEntries,
                nFeaturesPerEntry);
        final File inEvents = new File(TEST_OUTPUT_DIR, inFileNamePrefix
                + "-events");
        final File inEntries = new File(TEST_OUTPUT_DIR, inFileNamePrefix
                + "-entries");
        final File inFeatures = new File(TEST_OUTPUT_DIR, inFileNamePrefix
                + "-features");

        // Create the test data if necessary
        if (!inEvents.exists()) {
            generateEventsData(inEntries, inFeatures, inEvents, nEntries,
                    nFeaturesPerEntry);
        }

        final File outEvents = new File(TEST_OUTPUT_DIR, inEvents.getName()
                + "-filtered");
        final File outEntries = new File(TEST_OUTPUT_DIR, inEntries.getName()
                + "-filtered");
        final File outFeatures = new File(TEST_OUTPUT_DIR, inFeatures.getName()
                + "-filtered");

        assertValidPlaintextInputFiles(inEvents, inEntries, inFeatures);
        assertValidOutputFiles(outEvents, outEntries, outFeatures);
        deleteIfExist(outEvents, outEntries, outFeatures);

        final FilterCommand filter = new FilterCommand();
        filter.setCharset(DEFAULT_CHARSET);
        filter.setIndexDelegate(new DoubleEnumeratingDelegate(
                Enumerating.DEFAULT_TYPE, true, true, null, null));
        filter.setTempFiles(new TempFileFactory(TEST_TMP_DIR));
        filter.setInputEntriesFile(inEntries);
        filter.setInputFeaturesFile(inFeatures);
        filter.setInputEventsFile(inEvents);
        filter.setOutputEntriesFile(outEntries);
        filter.setOutputFeaturesFile(outFeatures);
        filter.setOutputEventsFile(outEvents);

        filter.setFilterEntryMinFreq(2);
        filter.setFilterEventMinFreq(2);
        filter.setFilterFeatureMinFreq(2);

        filter.addProgressListener(new TestConstants.InfoProgressListener());
        filter.runCommand();

        assertValidPlaintextInputFiles(outEntries, outFeatures, outEvents);
    }

    /**
     * Routine that creates a large amount of data, that should be the absolute
     * worst case for filtering stage of the pipeline.
     *
     * @throws IOException
     */
    public static void generateUniqueEventsData(final File entriesFile,
                                                final File featuresFile, final File eventsFile, final int nEntries,
                                                final int nFeatures) throws IOException {
        assert nEntries < Integer.MAX_VALUE / nFeatures : "number of events must be less than max_integer";
        final int nEvents = nEntries * nFeatures;

        System.out.printf("Generating worst-case data for FilterCommand "
                + "(nEntries=%d, nFeaturesPerEntry=%d, nEvents=%d)...%n",
                nEntries, nFeatures, nEvents);

        WeightedTokenSink entriesSink = null;
        WeightedTokenSink featuresSink = null;
        WeightedTokenPairSink eventsSink = null;
        try {
            final DoubleEnumeratingDelegate ded = new DoubleEnumeratingDelegate(
                    Enumerating.DEFAULT_TYPE, true, true, null, null);
            entriesSink = BybloIO.openEntriesSink(entriesFile, DEFAULT_CHARSET,
                    ded);
            featuresSink = BybloIO.openEntriesSink(featuresFile,
                    DEFAULT_CHARSET, ded);
            eventsSink = BybloIO.openEventsSink(eventsFile, DEFAULT_CHARSET,
                    ded);

            for (int i = 0; i < nEvents; i++) {
                entriesSink.write(new Weighted<Token>(new Token(i), 1));
                featuresSink.write(new Weighted<Token>(new Token(i), 1));
                eventsSink
                        .write(new Weighted<TokenPair>(new TokenPair(i, i), 1));
                if (i % 5000000 == 0 || i == nEvents - 1) {
                    System.out.printf(
                            "> generated %d of %d events (%.2f%% complete)%n",
                            i, nEvents, (100.0d * i) / nEvents);
                }
            }
        } finally {
            if (entriesSink != null) {
                entriesSink.flush();
                entriesSink.close();
            }
            if (featuresSink != null) {
                featuresSink.flush();
                featuresSink.close();
            }
            if (eventsSink != null) {
                eventsSink.flush();
                eventsSink.close();
            }
        }

        System.out.println("Generation completed.");
    }

    /**
     * Routine that creates a large amount of data, that should be the absolute
     * worst case for filtering stage of the pipeline.
     *
     * @throws IOException
     */
    public static void generateEventsData(final File entriesFile,
                                          final File featuresFile, final File eventsFile, final int nEntries,
                                          final int nFeatures) throws IOException {
        assert nEntries < Integer.MAX_VALUE / nFeatures : "number of events must be less than max_integer";
        final int nEvents = nEntries * nFeatures;

        System.out.printf("Generating worst-case data for FilterCommand "
                + "(nEntries=%d, nFeaturesPerEntry=%d, nEvents=%d)...%n",
                nEntries, nFeatures, nEvents);

        IntIterator featureIdGenerator = new ZipfianIntGenerator();
        IntIterator nFeatureGenerator = new PoissonDistribution(3).generator();

        WeightedTokenSink entriesSink = null;
        WeightedTokenSink featuresSink = null;
        WeightedTokenPairSink eventsSink = null;
        try {
            final DoubleEnumeratingDelegate ded = new DoubleEnumeratingDelegate(
                    Enumerating.DEFAULT_TYPE, true, true, null, null);
            entriesSink = BybloIO.openEntriesSink(entriesFile, DEFAULT_CHARSET,
                    ded);
            featuresSink = BybloIO.openEntriesSink(featuresFile,
                    DEFAULT_CHARSET, ded);
            eventsSink = BybloIO.openEventsSink(eventsFile, DEFAULT_CHARSET,
                    ded);

            for (int i = 0; i < nEvents; i++) {
                int j = featureIdGenerator.nextInt();
                int freq = nFeatureGenerator.nextInt();
                entriesSink.write(new Weighted<Token>(new Token(Integer.MAX_VALUE - i), freq));
                featuresSink.write(new Weighted<Token>(new Token(Integer.MAX_VALUE - j), freq));
                eventsSink.write(new Weighted<TokenPair>(
                        new TokenPair(Integer.MAX_VALUE - i, Integer.MAX_VALUE - j), freq));
                if (i % 5000000 == 0 || i == nEvents - 1) {
                    System.out.printf(
                            "> generated %d of %d events (%.2f%% complete)%n",
                            i, nEvents, (100.0d * i) / nEvents);
                }
            }
        } finally {
            if (entriesSink != null) {
                entriesSink.flush();
                entriesSink.close();
            }
            if (featuresSink != null) {
                featuresSink.flush();
                featuresSink.close();
            }
            if (eventsSink != null) {
                eventsSink.flush();
                eventsSink.close();
            }
        }

        System.out.println("Generation completed.");
    }

}
