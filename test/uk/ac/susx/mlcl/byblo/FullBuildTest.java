/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo;

import uk.ac.susx.mlcl.byblo.commands.IndexEventsCommand;
import java.io.File;
import java.nio.charset.Charset;
import org.junit.Test;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.byblo.commands.AllPairsCommand;
import uk.ac.susx.mlcl.byblo.commands.CountCommand;
import uk.ac.susx.mlcl.byblo.commands.FilterCommand;
import uk.ac.susx.mlcl.byblo.commands.KnnSimsCommand;
import static org.junit.Assert.*;
import uk.ac.susx.mlcl.byblo.commands.*;
import uk.ac.susx.mlcl.byblo.io.IndexDeligate;
import uk.ac.susx.mlcl.byblo.io.IndexDeligatePair;

/**
 *
 * @author hiam20
 */
public class FullBuildTest {

    @Test
    public void serialBuildTest() throws Exception {
        System.out.println("Testing Full Build (serial)");
        final String affix = "sb.";

        final File instances = TEST_FRUIT_INPUT;
        final Charset charet = DEFAULT_CHARSET;
        boolean preindexedEntries = false;
        boolean preindexedFeatures = false;

        File events = new File(TEST_OUTPUT_DIR,
                               affix + instances.getName() + ".events");
        File entries = new File(TEST_OUTPUT_DIR,
                                affix + instances.getName() + ".entries");
        File features = new File(TEST_OUTPUT_DIR,
                                 affix + instances.getName() + ".features");

        File eventsFiltered = new File(TEST_OUTPUT_DIR,
                                       events.getName() + ".filtered");
        File entriesFiltered = new File(TEST_OUTPUT_DIR,
                                        entries.getName() + ".filtered");
        File featuresFiltered = new File(TEST_OUTPUT_DIR,
                                         features.getName() + ".filtered");

        File similarities = new File(TEST_OUTPUT_DIR,
                                     affix + instances.getName() + ".sims");

        File neighbours = new File(TEST_OUTPUT_DIR,
                                   similarities.getName() + ".neighs");

        // Count the entries, features and events

        assertValidInputFiles(instances);
        deleteIfExist(events, entries, features);

        CountCommand count = new CountCommand(
                instances, events, entries, features,
                new IndexDeligatePair(preindexedEntries, preindexedFeatures),
                charet);
        count.runCommand();

        // Filter 

        assertValidInputFiles(entries, features, events);
        deleteIfExist(eventsFiltered, entriesFiltered, featuresFiltered);

        FilterCommand filter = new FilterCommand(
                events, entries, features,
                eventsFiltered, entriesFiltered, featuresFiltered, charet);
        filter.addEntryFeatureMinimumFrequency(2);
        filter.runCommand();

        assertTrue("Filtered events file is no smaller that events file.",
                   events.length() > eventsFiltered.length());
        assertTrue("Filtered entries file is no smaller that entries file.",
                   entries.length() > entriesFiltered.length());
        assertTrue("Filtered features file is no smaller that features file.",
                   features.length() > featuresFiltered.length());

        // All pairs

        assertValidInputFiles(eventsFiltered, entriesFiltered, featuresFiltered);
        deleteIfExist(similarities);

        AllPairsCommand allpairs = new AllPairsCommand(
                entriesFiltered, featuresFiltered, eventsFiltered, similarities,
                charet);
        allpairs.setSerial(true);
        allpairs.runCommand();

        // KNN
        assertValidInputFiles(similarities);
        deleteIfExist(neighbours);

        KnnSimsCommand knn = new KnnSimsCommand(
                similarities, neighbours, charet,
                new IndexDeligate(preindexedEntries), 5);
        knn.runCommand();

        assertValidInputFiles(neighbours);
        assertTrue("Neighbours file is no smaller that similarities file.",
                   neighbours.length() < similarities.length());

    }

    @Test
    public void parallelBuildTest() throws Exception {
        System.out.println("Testing Full Build (parallel)");
        final String affix = "pb.";

        final File instances = TEST_FRUIT_INPUT;
        final Charset charet = DEFAULT_CHARSET;
        boolean preindexedEntries = false;
        boolean preindexedFeatures = false;

        File events = new File(TEST_OUTPUT_DIR,
                               affix + instances.getName() + ".events");
        File entries = new File(TEST_OUTPUT_DIR,
                                affix + instances.getName() + ".entries");
        File features = new File(TEST_OUTPUT_DIR,
                                 affix + instances.getName() + ".features");

        File eventsFiltered = new File(TEST_OUTPUT_DIR,
                                       events.getName() + ".filtered");
        File entriesFiltered = new File(TEST_OUTPUT_DIR,
                                        entries.getName() + ".filtered");
        File featuresFiltered = new File(TEST_OUTPUT_DIR,
                                         features.getName() + ".filtered");

        File similarities = new File(TEST_OUTPUT_DIR,
                                     affix + instances.getName() + ".sims");

        File neighbours = new File(TEST_OUTPUT_DIR,
                                   similarities.getName() + ".neighs");

        // Count the entries, features and events

        assertValidInputFiles(instances);
        deleteIfExist(events, entries, features);


        ExternalCountCommand count = new ExternalCountCommand(
                instances, events, entries, features, charet,
                new IndexDeligatePair(preindexedEntries, preindexedFeatures));
        count.runCommand();

        // Filter 

        assertValidInputFiles(entries, features, events);
        deleteIfExist(eventsFiltered, entriesFiltered, featuresFiltered);

        FilterCommand filter = new FilterCommand(
                events, entries, features,
                eventsFiltered, entriesFiltered, featuresFiltered, charet);
        filter.addEntryFeatureMinimumFrequency(2);
        filter.runCommand();

        assertTrue("Filtered events file is no smaller that events file.",
                   events.length() > eventsFiltered.length());
        assertTrue("Filtered entries file is no smaller that entries file.",
                   entries.length() > entriesFiltered.length());
        assertTrue("Filtered features file is no smaller that features file.",
                   features.length() > featuresFiltered.length());

        // All pairs

        assertValidInputFiles(eventsFiltered, entriesFiltered, featuresFiltered);
        deleteIfExist(similarities);

        AllPairsCommand allpairs = new AllPairsCommand(
                entriesFiltered, featuresFiltered, eventsFiltered, similarities,
                charet);
        allpairs.setSerial(false);
        allpairs.runCommand();

        // KNN
        assertValidInputFiles(similarities);
        deleteIfExist(neighbours);

        ExternalKnnSimsCommand knn = new ExternalKnnSimsCommand(
                similarities, neighbours, charet,
                new IndexDeligate(preindexedEntries), 5);
        knn.runCommand();

        assertValidInputFiles(neighbours);
        assertTrue("Neighbours file is no smaller that similarities file.",
                   neighbours.length() < similarities.length());

    }

    @Test
    public void serialBuildTest_Indexed() throws Exception {
        System.out.println("Testing Full Build (serial, preindexed)");
        final String affix = "sbi.";

        final File instances = TEST_FRUIT_INPUT;
        final Charset charet = DEFAULT_CHARSET;
        boolean preindexedEntries = true;
        boolean preindexedFeatures = true;

        File instancesIndexed = new File(TEST_OUTPUT_DIR, affix + instances.
                getName() + ".indexed");
        File entryIndex = new File(TEST_OUTPUT_DIR,
                                   affix + instances.getName() + ".entry-index");
        File featureIndex = new File(TEST_OUTPUT_DIR,
                                     affix + instances.getName() + ".feature-index");

        File events = new File(TEST_OUTPUT_DIR,
                               affix + instances.getName() + ".events");
        File entries = new File(TEST_OUTPUT_DIR,
                                affix + instances.getName() + ".entries");
        File features = new File(TEST_OUTPUT_DIR,
                                 affix + instances.getName() + ".features");

        File eventsFiltered = new File(TEST_OUTPUT_DIR,
                                       events.getName() + ".filtered");
        File entriesFiltered = new File(TEST_OUTPUT_DIR,
                                        entries.getName() + ".filtered");
        File featuresFiltered = new File(TEST_OUTPUT_DIR,
                                         features.getName() + ".filtered");

        File similarities = new File(TEST_OUTPUT_DIR,
                                     affix + instances.getName() + ".sims");

        File neighbours = new File(TEST_OUTPUT_DIR,
                                   similarities.getName() + ".neighs");

        File neighboursStrings = new File(TEST_OUTPUT_DIR,
                                          neighbours.getName() + ".strings");



        // Index the strings, reproducing the instances file in indexed form

        assertValidInputFiles(instances);
        deleteIfExist(entryIndex, featureIndex, instancesIndexed);


        IndexEventsCommand index = new IndexEventsCommand(
                instances, charet, instancesIndexed,
                entryIndex, featureIndex);
        index.runCommand();

        assertSizeGT(instances, instancesIndexed);
        assertValidInputFiles(instancesIndexed, entryIndex, featureIndex);

        // Count the entries, features and events

        deleteIfExist(events, entries, features);

        CountCommand count = new CountCommand(
                instancesIndexed, events, entries, features,
                new IndexDeligatePair(preindexedEntries, preindexedFeatures),
                charet);
        count.runCommand();

        assertValidInputFiles(entries, features, events);
        assertSizeGT(TEST_FRUIT_ENTRY_FEATURES, events);
        assertSizeGT(TEST_FRUIT_ENTRIES, entries);
        assertSizeGT(TEST_FRUIT_FEATURES, features);

        // Filter 



        deleteIfExist(eventsFiltered, featuresFiltered, entriesFiltered);

        FilterCommand filter = new FilterCommand(
                events, entries, features,
                eventsFiltered, entriesFiltered, featuresFiltered, charet);
        filter.getIndexDeligate().setIndexFile1(entryIndex);
        filter.getIndexDeligate().setIndexFile2(featureIndex);
        filter.getIndexDeligate().setPreindexedTokens1(preindexedEntries);
        filter.getIndexDeligate().setPreindexedTokens2(preindexedFeatures);
        filter.addEntryFeatureMinimumFrequency(2);


        filter.runCommand();
        assertSizeGT(events, eventsFiltered);
        assertSizeGT(entries, entriesFiltered);
        assertSizeGT(features, featuresFiltered);

        // All pairs

        assertValidInputFiles(eventsFiltered, entriesFiltered, featuresFiltered);
        deleteIfExist(similarities);

        AllPairsCommand allpairs = new AllPairsCommand(
                entriesFiltered, featuresFiltered, eventsFiltered, similarities,
                charet);
        allpairs.setSerial(true);
        allpairs.runCommand();

        assertValidInputFiles(similarities);
        assertSizeGT(TEST_FRUIT_SIMS, similarities);

        // KNN


        deleteIfExist(neighbours);

        KnnSimsCommand knn = new KnnSimsCommand(
                similarities, neighbours, charet,
                new IndexDeligate(preindexedEntries), 5);
        knn.runCommand();

        assertValidInputFiles(neighbours);
        assertSizeGT(similarities, neighbours);
        assertSizeGT(TEST_FRUIT_SIMS_100NN, neighbours);

        // Finally, convert neighbours back to strings

        deleteIfExist(neighboursStrings);

        UnindexSimsCommand unindex = new UnindexSimsCommand(
                neighbours, neighboursStrings, charet, entryIndex);
        unindex.runCommand();

        assertValidInputFiles(neighboursStrings);
    }

    @Test
    public void serialBuildTest_SkipIndexed() throws Exception {
        System.out.println("Testing Full Build (serial, preindexed)");
        final String affix = "sbsi.";

        final File instances = TEST_FRUIT_INPUT;
        final Charset charet = DEFAULT_CHARSET;
        boolean preindexedEntries = true;
        boolean preindexedFeatures = true;
        boolean skipIndexEntries = true;
        boolean skipIndexFeatures = true;

        File instancesIndexed = new File(TEST_OUTPUT_DIR, affix + instances.
                getName() + ".indexed");
        File entryIndex = new File(TEST_OUTPUT_DIR,
                                   affix + instances.getName() + ".entry-index");
        File featureIndex = new File(TEST_OUTPUT_DIR,
                                     affix + instances.getName() + ".feature-index");

        File events = new File(TEST_OUTPUT_DIR,
                               affix + instances.getName() + ".events");
        File entries = new File(TEST_OUTPUT_DIR,
                                affix + instances.getName() + ".entries");
        File features = new File(TEST_OUTPUT_DIR,
                                 affix + instances.getName() + ".features");

        File eventsFiltered = new File(TEST_OUTPUT_DIR,
                                       events.getName() + ".filtered");
        File entriesFiltered = new File(TEST_OUTPUT_DIR,
                                        entries.getName() + ".filtered");
        File featuresFiltered = new File(TEST_OUTPUT_DIR,
                                         features.getName() + ".filtered");

        File similarities = new File(TEST_OUTPUT_DIR,
                                     affix + instances.getName() + ".sims");

        File neighbours = new File(TEST_OUTPUT_DIR,
                                   similarities.getName() + ".neighs");

        File neighboursStrings = new File(TEST_OUTPUT_DIR,
                                          neighbours.getName() + ".strings");



        // Index the strings, reproducing the instances file in indexed form

        assertValidInputFiles(instances);
        deleteIfExist(entryIndex, featureIndex, instancesIndexed);


        IndexEventsCommand index = new IndexEventsCommand(
                instances, charet, instancesIndexed,
                entryIndex, featureIndex);
        index.getIndexDeligate().setSkipIndexed1(skipIndexEntries);
        index.getIndexDeligate().setSkipIndexed2(skipIndexFeatures);
        index.runCommand();

        assertSizeGT(instances, instancesIndexed);
        assertValidInputFiles(instancesIndexed, entryIndex, featureIndex);

        unindexTP(instancesIndexed, suffix(instancesIndexed, ".strings"), entryIndex, featureIndex,
                   skipIndexEntries, skipIndexFeatures);
        
        // Count the entries, features and events

        deleteIfExist(events, entries, features);

        CountCommand count = new CountCommand(
                instancesIndexed, events, entries, features,
                new IndexDeligatePair(preindexedEntries, preindexedFeatures),
                charet);
        count.getIndexDeligate().setSkipIndexed1(skipIndexEntries);
        count.getIndexDeligate().setSkipIndexed2(skipIndexFeatures);
        count.runCommand();

        assertValidInputFiles(entries, features, events);
        assertSizeGT(TEST_FRUIT_ENTRY_FEATURES, events);
        assertSizeGT(TEST_FRUIT_ENTRIES, entries);
        assertSizeGT(TEST_FRUIT_FEATURES, features);

        unindexWT(entries, suffix(entries, ".strings"), entryIndex,
                  skipIndexEntries);
        unindexWT(features, suffix(features, ".strings"), featureIndex,
                  skipIndexFeatures);
        unindexWTP(events, suffix(events, ".strings"), entryIndex, featureIndex,
                   skipIndexEntries, skipIndexFeatures);

        // Filter 



        deleteIfExist(eventsFiltered, featuresFiltered, entriesFiltered);

        FilterCommand filter = new FilterCommand(
                events, entries, features,
                eventsFiltered, entriesFiltered, featuresFiltered, charet);
        filter.getIndexDeligate().setIndexFile1(entryIndex);
        filter.getIndexDeligate().setIndexFile2(featureIndex);
        filter.getIndexDeligate().setPreindexedTokens1(preindexedEntries);
        filter.getIndexDeligate().setPreindexedTokens2(preindexedFeatures);
        filter.getIndexDeligate().setSkipIndexed1(skipIndexEntries);
        filter.getIndexDeligate().setSkipIndexed2(skipIndexFeatures);
        filter.addEntryFeatureMinimumFrequency(2);


        filter.runCommand();
        assertSizeGT(events, eventsFiltered);
        assertSizeGT(entries, entriesFiltered);
        assertSizeGT(features, featuresFiltered);

        unindexWT(entriesFiltered, suffix(entriesFiltered, ".strings"),
                  entryIndex, skipIndexEntries);
        unindexWT(featuresFiltered, suffix(featuresFiltered, ".strings"),
                  featureIndex, skipIndexFeatures);
        unindexWTP(eventsFiltered, suffix(eventsFiltered, ".strings"),
                   entryIndex, featureIndex, skipIndexEntries, skipIndexFeatures);

        // All pairs

        assertValidInputFiles(eventsFiltered, entriesFiltered, featuresFiltered);
        deleteIfExist(similarities);

        AllPairsCommand allpairs = new AllPairsCommand(
                entriesFiltered, featuresFiltered, eventsFiltered, similarities,
                charet);
        allpairs.setSerial(true);
        allpairs.getIndexDeligate().setSkipIndexed1(skipIndexEntries);
        allpairs.getIndexDeligate().setSkipIndexed2(skipIndexFeatures);
        allpairs.runCommand();

        assertValidInputFiles(similarities);
        assertSizeGT(TEST_FRUIT_SIMS, similarities);

        unindexSims(similarities, suffix(similarities, ".strings"), entryIndex, skipIndexEntries);

        // KNN


        deleteIfExist(neighbours);

        KnnSimsCommand knn = new KnnSimsCommand(
                similarities, neighbours, charet,
                new IndexDeligate(preindexedEntries), 5);
        knn.getIndexDeligate().setSkipIndexed1(skipIndexEntries);
        knn.getIndexDeligate().setSkipIndexed2(skipIndexFeatures);
        knn.runCommand();

        assertValidInputFiles(neighbours);
//        assertSizeGT(similarities, neighbours);
        assertSizeGT(TEST_FRUIT_SIMS_100NN, neighbours);

        // Finally, convert neighbours back to strings

        deleteIfExist(neighboursStrings);

        unindexSims(neighbours, suffix(neighbours, ".strings"), entryIndex, skipIndexEntries);

        assertValidInputFiles(neighboursStrings);
    }

    private static File suffix(File file, String suffix) {
        return new File(file.getParentFile(), file.getName() + suffix);
    }

     private static void unindexSims(File from, File to, File index, boolean skip)
            throws Exception {
        UnindexSimsCommand unindex = new UnindexSimsCommand();
        unindex.getFilesDeligate().setCharset(DEFAULT_CHARSET);
        unindex.getFilesDeligate().setSourceFile(from);
        unindex.getFilesDeligate().setDestinationFile(to);
        unindex.getFilesDeligate().setCompactFormatDisabled(false);
        unindex.getIndexDeligate().setIndexFile(index);
        unindex.getIndexDeligate().setSkipIndexed(skip);
        unindex.runCommand();
    }
     
    private static void unindexWT(File from, File to, File index, boolean skip)
            throws Exception {
        UnindexWTCommand unindex = new UnindexWTCommand();
        unindex.getFilesDeligate().setCharset(DEFAULT_CHARSET);
        unindex.getFilesDeligate().setSourceFile(from);
        unindex.getFilesDeligate().setDestinationFile(to);
        unindex.getFilesDeligate().setCompactFormatDisabled(false);
        unindex.getIndexDeligate().setIndexFile(index);
        unindex.getIndexDeligate().setSkipIndexed(skip);
        unindex.runCommand();
    }

    private static void unindexWTP(File from, File to,
                                   File index1, File index2,
                                   boolean skip1, boolean skip2)
            throws Exception {
        UnindexWTPCommand unindex = new UnindexWTPCommand();
        unindex.getFilesDeligate().setCharset(DEFAULT_CHARSET);
        unindex.getFilesDeligate().setSourceFile(from);
        unindex.getFilesDeligate().setDestinationFile(to);
        unindex.getFilesDeligate().setCompactFormatDisabled(false);
        unindex.getIndexDeligate().setIndexFile1(index1);
        unindex.getIndexDeligate().setIndexFile2(index2);
        unindex.getIndexDeligate().setSkipIndexed1(skip1);
        unindex.getIndexDeligate().setSkipIndexed2(skip2);
        unindex.runCommand();
    }

    private static void unindexTP(File from, File to,
                                  File index1, File index2,
                                  boolean skip1, boolean skip2)
            throws Exception {
        UnindexTPCommand unindex = new UnindexTPCommand();
        unindex.getFilesDeligate().setCharset(DEFAULT_CHARSET);
        unindex.getFilesDeligate().setSourceFile(from);
        unindex.getFilesDeligate().setDestinationFile(to);
        unindex.getFilesDeligate().setCompactFormatDisabled(false);
        unindex.getIndexDeligate().setIndexFile1(index1);
        unindex.getIndexDeligate().setIndexFile2(index2);
        unindex.getIndexDeligate().setSkipIndexed1(skip1);
        unindex.getIndexDeligate().setSkipIndexed2(skip2);
        unindex.runCommand();
    }

    private static void deleteIfExist(File... files) {
        for (File file : files) {
            if (file.exists())
                file.delete();
        }
    }

    private static void assertSizeGT(File bigger, File smaller) {
        assertValidInputFiles(bigger);
        assertValidInputFiles(smaller);
        assertTrue(smaller + " is not smaller than " + bigger,
                   bigger.length() > smaller.length());
    }
}
