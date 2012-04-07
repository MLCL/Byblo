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

        File events = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".events");
        File entries = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".entries");
        File features = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".features");

        File eventsFiltered = new File(TEST_OUTPUT_DIR, events.getName() + ".filtered");
        File entriesFiltered = new File(TEST_OUTPUT_DIR, entries.getName() + ".filtered");
        File featuresFiltered = new File(TEST_OUTPUT_DIR, features.getName() + ".filtered");

        File similarities = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".sims");

        File neighbours = new File(TEST_OUTPUT_DIR, similarities.getName() + ".neighs");

        // Count the entries, features and events

        assertValidInputFiles(instances);
        deleteIfExist(events, entries, features);

        CountCommand count = new CountCommand(
                instances, events, entries, features,
                new IndexDeligatePair(preindexedEntries, preindexedFeatures), charet);
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
                entriesFiltered, featuresFiltered, eventsFiltered, similarities, charet);
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

        File events = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".events");
        File entries = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".entries");
        File features = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".features");

        File eventsFiltered = new File(TEST_OUTPUT_DIR, events.getName() + ".filtered");
        File entriesFiltered = new File(TEST_OUTPUT_DIR, entries.getName() + ".filtered");
        File featuresFiltered = new File(TEST_OUTPUT_DIR, features.getName() + ".filtered");

        File similarities = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".sims");

        File neighbours = new File(TEST_OUTPUT_DIR, similarities.getName() + ".neighs");

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
                entriesFiltered, featuresFiltered, eventsFiltered, similarities, charet);
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

        File instancesIndexed = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".indexed");
        File entryIndex = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".entry-index");
        File featureIndex = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".feature-index");

        File events = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".events");
        File entries = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".entries");
        File features = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".features");

        File eventsFiltered = new File(TEST_OUTPUT_DIR, events.getName() + ".filtered");
        File entriesFiltered = new File(TEST_OUTPUT_DIR, entries.getName() + ".filtered");
        File featuresFiltered = new File(TEST_OUTPUT_DIR, features.getName() + ".filtered");

        File similarities = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".sims");

        File neighbours = new File(TEST_OUTPUT_DIR, similarities.getName() + ".neighs");

        File neighboursStrings = new File(TEST_OUTPUT_DIR, neighbours.getName() + ".strings");



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
                new IndexDeligatePair(preindexedEntries, preindexedFeatures), charet);
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
                entriesFiltered, featuresFiltered, eventsFiltered, similarities, charet);
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

        File instancesIndexed = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".indexed");
        File entryIndex = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".entry-index");
        File featureIndex = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".feature-index");

        File events = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".events");
        File entries = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".entries");
        File features = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".features");

        File eventsFiltered = new File(TEST_OUTPUT_DIR, events.getName() + ".filtered");
        File entriesFiltered = new File(TEST_OUTPUT_DIR, entries.getName() + ".filtered");
        File featuresFiltered = new File(TEST_OUTPUT_DIR, features.getName() + ".filtered");

        File similarities = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".sims");

        File neighbours = new File(TEST_OUTPUT_DIR, similarities.getName() + ".neighs");

        File neighboursStrings = new File(TEST_OUTPUT_DIR, neighbours.getName() + ".strings");



        // Index the strings, reproducing the instances file in indexed form

        assertValidInputFiles(instances);
        deleteIfExist(entryIndex, featureIndex, instancesIndexed);


        IndexEventsCommand index = new IndexEventsCommand(
                instances, charet, instancesIndexed,
                entryIndex, featureIndex);
        index.getIndexDeligate().setSkipindexed1(skipIndexEntries);
        index.getIndexDeligate().setSkipindexed2(skipIndexFeatures);
        index.runCommand();

        assertSizeGT(instances, instancesIndexed);
        assertValidInputFiles(instancesIndexed, entryIndex, featureIndex);

        // Count the entries, features and events

        deleteIfExist(events, entries, features);

        CountCommand count = new CountCommand(
                instancesIndexed, events, entries, features,
                new IndexDeligatePair(preindexedEntries, preindexedFeatures), charet);
        count.getIndexDeligate().setSkipindexed1(skipIndexEntries);
        count.getIndexDeligate().setSkipindexed2(skipIndexFeatures);
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
        filter.getIndexDeligate().setSkipindexed1(skipIndexEntries);
        filter.getIndexDeligate().setSkipindexed2(skipIndexFeatures);
        filter.addEntryFeatureMinimumFrequency(2);


        filter.runCommand();
        assertSizeGT(events, eventsFiltered);
        assertSizeGT(entries, entriesFiltered);
        assertSizeGT(features, featuresFiltered);

        // All pairs

        assertValidInputFiles(eventsFiltered, entriesFiltered, featuresFiltered);
        deleteIfExist(similarities);

        AllPairsCommand allpairs = new AllPairsCommand(
                entriesFiltered, featuresFiltered, eventsFiltered, similarities, charet);
        allpairs.setSerial(true);
        allpairs.getIndexDeligate().setSkipindexed1(skipIndexEntries);
        allpairs.getIndexDeligate().setSkipindexed2(skipIndexFeatures);
        allpairs.runCommand();

        assertValidInputFiles(similarities);
        assertSizeGT(TEST_FRUIT_SIMS, similarities);

        // KNN


        deleteIfExist(neighbours);

        KnnSimsCommand knn = new KnnSimsCommand(
                similarities, neighbours, charet,
                new IndexDeligate(preindexedEntries), 5);
        knn.getIndexDeligate().setSkipindexed1(skipIndexEntries);
        knn.getIndexDeligate().setSkipindexed2(skipIndexFeatures);
        knn.runCommand();

        assertValidInputFiles(neighbours);
//        assertSizeGT(similarities, neighbours);
        assertSizeGT(TEST_FRUIT_SIMS_100NN, neighbours);

        // Finally, convert neighbours back to strings

        deleteIfExist(neighboursStrings);

        UnindexSimsCommand unindex = new UnindexSimsCommand(
                neighbours, neighboursStrings, charet, entryIndex);
        unindex.getIndexDeligate().setSkipIndexed(skipIndexEntries);
        unindex.runCommand();

        assertValidInputFiles(neighboursStrings);
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
