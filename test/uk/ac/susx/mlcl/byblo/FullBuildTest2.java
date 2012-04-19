/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo;

import java.io.File;
import java.nio.charset.Charset;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.byblo.commands.*;
import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import static uk.ac.susx.mlcl.byblo.commands.IndexSimsCommandTest.*;
import static uk.ac.susx.mlcl.byblo.commands.IndexTPCommandTest.*;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratorType;

/**
 *
 * @author hiam20
 */
public class FullBuildTest2 {

//    @Test
//    public void serialBuildTest() throws Exception {
//        System.out.println("Testing Full Build (serial)");
//        final String affix = "sb.";
//
//        final File instances = TEST_FRUIT_INPUT;
//        final Charset charet = DEFAULT_CHARSET;
//        boolean preindexedEntries = false;
//        boolean preindexedFeatures = false;
//
//        File events = new File(TEST_OUTPUT_DIR,
//                               affix + instances.getName() + ".events");
//        File entries = new File(TEST_OUTPUT_DIR,
//                                affix + instances.getName() + ".entries");
//        File features = new File(TEST_OUTPUT_DIR,
//                                 affix + instances.getName() + ".features");
//
//        File eventsFiltered = new File(TEST_OUTPUT_DIR,
//                                       events.getName() + ".filtered");
//        File entriesFiltered = new File(TEST_OUTPUT_DIR,
//                                        entries.getName() + ".filtered");
//        File featuresFiltered = new File(TEST_OUTPUT_DIR,
//                                         features.getName() + ".filtered");
//
//        File similarities = new File(TEST_OUTPUT_DIR,
//                                     affix + instances.getName() + ".sims");
//
//        File neighbours = new File(TEST_OUTPUT_DIR,
//                                   similarities.getName() + ".neighs");
//
//        // Count the entries, features and events
//
//        assertValidPlaintextInputFiles(instances);
//        deleteIfExist(events, entries, features);
//
//        CountCommand count = new CountCommand(
//                instances, events, entries, features,
//                new DoubleEnumeratingDeligate(Enumerating.DEFAULT_TYPE,
//                                              preindexedEntries, preindexedFeatures,
//                                              null, null, false, false),
//                charet);
//        count.runCommand();
//
//        // Filter 
//
//        assertValidPlaintextInputFiles(entries, features, events);
//        deleteIfExist(eventsFiltered, entriesFiltered, featuresFiltered);
//
//        FilterCommand filter = new FilterCommand(
//                events, entries, features,
//                eventsFiltered, entriesFiltered, featuresFiltered, charet);
//        filter.addEntryFeatureMinimumFrequency(2);
//        filter.runCommand();
//
//        assertTrue("Filtered events file is no smaller that events file.",
//                   events.length() > eventsFiltered.length());
//        assertTrue("Filtered entries file is no smaller that entries file.",
//                   entries.length() > entriesFiltered.length());
//        assertTrue("Filtered features file is no smaller that features file.",
//                   features.length() > featuresFiltered.length());
//
//        // All pairs
//
//        assertValidPlaintextInputFiles(eventsFiltered, entriesFiltered, featuresFiltered);
//        deleteIfExist(similarities);
//
//        AllPairsCommand allpairs = new AllPairsCommand(
//                entriesFiltered, featuresFiltered, eventsFiltered, similarities,
//                charet, new DoubleEnumeratingDeligate(Enumerating.DEFAULT_TYPE,
//                                                      preindexedEntries, preindexedFeatures,
//                                                      null, null, false, false));
//        allpairs.setnThreads(1);
//        allpairs.runCommand();
//
//        // KNN
//        assertValidPlaintextInputFiles(similarities);
//        deleteIfExist(neighbours);
//
//        KnnSimsCommand knn = new KnnSimsCommand(
//                similarities, neighbours, charet,
//                new SingleEnumeratingDeligate(Enumerating.DEFAULT_TYPE, preindexedEntries, null, false, false), 5);
//        knn.runCommand();
//
//        assertValidPlaintextInputFiles(neighbours);
//        assertTrue("Neighbours file is no smaller that similarities file.",
//                   neighbours.length() < similarities.length());
//
//    }
//
//    @Test
//    public void parallelBuildTest() throws Exception {
//        System.out.println("Testing Full Build (parallel)");
//        final String affix = "pb.";
//
//        final File instances = TEST_FRUIT_INPUT;
//        final Charset charet = DEFAULT_CHARSET;
//        boolean preindexedEntries = false;
//        boolean preindexedFeatures = false;
//
//        File events = new File(TEST_OUTPUT_DIR,
//                               affix + instances.getName() + ".events");
//        File entries = new File(TEST_OUTPUT_DIR,
//                                affix + instances.getName() + ".entries");
//        File features = new File(TEST_OUTPUT_DIR,
//                                 affix + instances.getName() + ".features");
//
//        File eventsFiltered = new File(TEST_OUTPUT_DIR,
//                                       events.getName() + ".filtered");
//        File entriesFiltered = new File(TEST_OUTPUT_DIR,
//                                        entries.getName() + ".filtered");
//        File featuresFiltered = new File(TEST_OUTPUT_DIR,
//                                         features.getName() + ".filtered");
//
//        File similarities = new File(TEST_OUTPUT_DIR,
//                                     affix + instances.getName() + ".sims");
//
//        File neighbours = new File(TEST_OUTPUT_DIR,
//                                   similarities.getName() + ".neighs");
//
//        // Count the entries, features and events
//
//        assertValidPlaintextInputFiles(instances);
//        deleteIfExist(events, entries, features);
//
//
//        ExternalCountCommand count = new ExternalCountCommand(
//                instances, events, entries, features, charet,
//                new DoubleEnumeratingDeligate(Enumerating.DEFAULT_TYPE,
//                                              preindexedEntries, preindexedFeatures,
//                                              null, null, false, false));
//        count.runCommand();
//
//        // Filter 
//
//        assertValidPlaintextInputFiles(entries, features, events);
//        deleteIfExist(eventsFiltered, entriesFiltered, featuresFiltered);
//
//        FilterCommand filter = new FilterCommand(
//                events, entries, features,
//                eventsFiltered, entriesFiltered, featuresFiltered, charet);
//        filter.addEntryFeatureMinimumFrequency(2);
//        filter.runCommand();
//
//        assertTrue("Filtered events file is no smaller that events file.",
//                   events.length() > eventsFiltered.length());
//        assertTrue("Filtered entries file is no smaller that entries file.",
//                   entries.length() > entriesFiltered.length());
//        assertTrue("Filtered features file is no smaller that features file.",
//                   features.length() > featuresFiltered.length());
//
//        // All pairs
//
//        assertValidPlaintextInputFiles(eventsFiltered, entriesFiltered, featuresFiltered);
//        deleteIfExist(similarities);
//
//        AllPairsCommand allpairs = new AllPairsCommand(
//                entriesFiltered, featuresFiltered, eventsFiltered, similarities,
//                charet, new DoubleEnumeratingDeligate(Enumerating.DEFAULT_TYPE,
//                                                      preindexedEntries, preindexedFeatures,
//                                                      null, null, false, false));
//        allpairs.runCommand();
//
//        // KNN
//        assertValidPlaintextInputFiles(similarities);
//        deleteIfExist(neighbours);
//
//        ExternalKnnSimsCommand knn = new ExternalKnnSimsCommand(
//                similarities, neighbours, charet,
//                new SingleEnumeratingDeligate(Enumerating.DEFAULT_TYPE,
//                                              preindexedEntries, null, false, false), 5);
//        knn.runCommand();
//
//        assertValidPlaintextInputFiles(neighbours);
//        assertTrue("Neighbours file is no smaller that similarities file.",
//                   neighbours.length() < similarities.length());
//
//    }
//
//    @Test
//    public void serialBuildTest_Indexed() throws Exception {
//        System.out.println("Testing Full Build (serial, preindexed)");
//        final String affix = "sbi.";
//
//        final File instances = TEST_FRUIT_INPUT;
//        final Charset charet = DEFAULT_CHARSET;
//        boolean preindexedEntries = true;
//        boolean preindexedFeatures = true;
//
//        File instancesIndexed = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".indexed");
//        File entryIndex = new File(TEST_OUTPUT_DIR,
//                                   affix + instances.getName() + ".entry-index");
//        File featureIndex = new File(TEST_OUTPUT_DIR,
//                                     affix + instances.getName() + ".feature-index");
//
//        File events = new File(TEST_OUTPUT_DIR,
//                               affix + instances.getName() + ".events");
//        File entries = new File(TEST_OUTPUT_DIR,
//                                affix + instances.getName() + ".entries");
//        File features = new File(TEST_OUTPUT_DIR,
//                                 affix + instances.getName() + ".features");
//
//        File eventsFiltered = new File(TEST_OUTPUT_DIR,
//                                       events.getName() + ".filtered");
//        File entriesFiltered = new File(TEST_OUTPUT_DIR,
//                                        entries.getName() + ".filtered");
//        File featuresFiltered = new File(TEST_OUTPUT_DIR,
//                                         features.getName() + ".filtered");
//
//        File similarities = new File(TEST_OUTPUT_DIR,
//                                     affix + instances.getName() + ".sims");
//
//        File neighbours = new File(TEST_OUTPUT_DIR,
//                                   similarities.getName() + ".neighs");
//
//        File neighboursStrings = new File(TEST_OUTPUT_DIR,
//                                          neighbours.getName() + ".strings");
//
//
//
//        // Index the strings, reproducing the instances file in indexed form
//
//        deleteIfExist(entryIndex, featureIndex, instancesIndexed);
//
//        indexTP(instances, instancesIndexed, entryIndex, featureIndex, Enumerating.DEFAULT_TYPE,
//                false,
//                false, false);
//
//
//        // Count the entries, features and events
//
//        deleteIfExist(events, entries, features);
//
//        CountCommand count = new CountCommand(
//                instancesIndexed, events, entries, features,
//                new DoubleEnumeratingDeligate(Enumerating.DEFAULT_TYPE,
//                                              preindexedEntries, preindexedFeatures,
//                                              null, null, false, false),
//                charet);
//        count.runCommand();
//
//        assertValidPlaintextInputFiles(entries, features, events);
//        assertSizeGT(TEST_FRUIT_ENTRY_FEATURES, events);
//        assertSizeGT(TEST_FRUIT_ENTRIES, entries);
//        assertSizeGT(TEST_FRUIT_FEATURES, features);
//
//        // Filter 
//
//
//
//        deleteIfExist(eventsFiltered, featuresFiltered, entriesFiltered);
//
//
//        filter(events, entries, features,
//               eventsFiltered, entriesFiltered, featuresFiltered,
//               entryIndex, featureIndex, preindexedEntries, preindexedFeatures, false, false);
//
//        assertSizeGT(events, eventsFiltered);
//        assertSizeGT(entries, entriesFiltered);
//        assertSizeGT(features, featuresFiltered);
//
//        // All pairs
//
//        assertValidPlaintextInputFiles(eventsFiltered, entriesFiltered, featuresFiltered);
//        deleteIfExist(similarities);
//
//        AllPairsCommand allpairs = new AllPairsCommand(
//                entriesFiltered, featuresFiltered, eventsFiltered, similarities,
//                charet, new DoubleEnumeratingDeligate(Enumerating.DEFAULT_TYPE,
//                                                      preindexedEntries, preindexedFeatures,
//                                                      null, null, false, false));
//        allpairs.setnThreads(1);
//        allpairs.runCommand();
//
//        assertValidPlaintextInputFiles(similarities);
//        assertSizeGT(TEST_FRUIT_SIMS, similarities);
//
//        // KNN
//
//
//        deleteIfExist(neighbours);
//
//        KnnSimsCommand knn = new KnnSimsCommand(
//                similarities, neighbours, charet,
//                new SingleEnumeratingDeligate(Enumerating.DEFAULT_TYPE,
//                                              preindexedEntries, null, false, false), 5);
//        knn.runCommand();
//
//        assertValidPlaintextInputFiles(neighbours);
//        assertSizeGT(similarities, neighbours);
//        assertSizeGT(TEST_FRUIT_SIMS_100NN, neighbours);
//
//        // Finally, convert neighbours back to strings
//
//        deleteIfExist(neighboursStrings);
//
//        unindexSims(neighbours, suffix(neighbours, ".strings"), entryIndex,
//                    EnumeratorType.Memory, false, false, false);
//
//        assertValidPlaintextInputFiles(neighboursStrings);
//    }
//
//    @Test
//    public void parallelBuildTest_Indexed() throws Exception {
//        System.out.println("Testing Full Build (parallel, preindexed)");
//        final String affix = "pbi.";
//
//        final File instances = TEST_FRUIT_INPUT;
//        final Charset charet = DEFAULT_CHARSET;
//        boolean preindexedEntries = true;
//        boolean preindexedFeatures = true;
//
//        File instancesIndexed = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".indexed");
//        File entryIndex = new File(TEST_OUTPUT_DIR,
//                                   affix + instances.getName() + ".entry-index");
//        File featureIndex = new File(TEST_OUTPUT_DIR,
//                                     affix + instances.getName() + ".feature-index");
//
//        File events = new File(TEST_OUTPUT_DIR,
//                               affix + instances.getName() + ".events");
//        File entries = new File(TEST_OUTPUT_DIR,
//                                affix + instances.getName() + ".entries");
//        File features = new File(TEST_OUTPUT_DIR,
//                                 affix + instances.getName() + ".features");
//
//        File eventsFiltered = new File(TEST_OUTPUT_DIR,
//                                       events.getName() + ".filtered");
//        File entriesFiltered = new File(TEST_OUTPUT_DIR,
//                                        entries.getName() + ".filtered");
//        File featuresFiltered = new File(TEST_OUTPUT_DIR,
//                                         features.getName() + ".filtered");
//
//        File similarities = new File(TEST_OUTPUT_DIR,
//                                     affix + instances.getName() + ".sims");
//
//        File neighbours = new File(TEST_OUTPUT_DIR,
//                                   similarities.getName() + ".neighs");
//
//        File neighboursStrings = new File(TEST_OUTPUT_DIR,
//                                          neighbours.getName() + ".strings");
//
//
//
//        // Index the strings, reproducing the instances file in indexed form
//
//        deleteIfExist(entryIndex, featureIndex, instancesIndexed);
//
//        indexTP(instances, instancesIndexed, entryIndex, featureIndex, Enumerating.DEFAULT_TYPE, false,
//                false, false);
//
//
//        // Count the entries, features and events
//
//        deleteIfExist(events, entries, features);
//
//        ExternalCountCommand count = new ExternalCountCommand();
//        count.setInstancesFile(instancesIndexed);
//        count.setEntriesFile(entries);
//        count.setFeaturesFile(features);
//        count.setEntryFeaturesFile(events);
//        count.setIndexDeligate(new DoubleEnumeratingDeligate(Enumerating.DEFAULT_TYPE,
//                                                             preindexedEntries, preindexedFeatures,
//                                                             null, null, false, false));
//        count.getFileDeligate().setCharset(charet);
//        count.runCommand();
//
//        assertValidPlaintextInputFiles(entries, features, events);
//        assertSizeGT(TEST_FRUIT_ENTRY_FEATURES, events);
//        assertSizeGT(TEST_FRUIT_ENTRIES, entries);
//        assertSizeGT(TEST_FRUIT_FEATURES, features);
//
//        // Filter 
//
//
//
//        deleteIfExist(eventsFiltered, featuresFiltered, entriesFiltered);
//
//
//        filter(events, entries, features, eventsFiltered, entriesFiltered,
//               featuresFiltered, entryIndex, featureIndex, preindexedEntries,
//               preindexedFeatures, false, false);
//
//        // All pairs
//
//        assertValidPlaintextInputFiles(eventsFiltered, entriesFiltered, featuresFiltered);
//        deleteIfExist(similarities);
//
//        AllPairsCommand allpairs = new AllPairsCommand(
//                entriesFiltered, featuresFiltered, eventsFiltered, similarities,
//                charet, new DoubleEnumeratingDeligate(Enumerating.DEFAULT_TYPE,
//                                                      preindexedEntries, preindexedFeatures,
//                                                      null, null, false, false));
//        allpairs.runCommand();
//
//        assertValidPlaintextInputFiles(similarities);
//        assertSizeGT(TEST_FRUIT_SIMS, similarities);
//
//        // KNN
//
//
//        deleteIfExist(neighbours);
//
//        ExternalKnnSimsCommand knn = new ExternalKnnSimsCommand(
//                similarities, neighbours, charet,
//                new SingleEnumeratingDeligate(Enumerating.DEFAULT_TYPE,
//                                              preindexedEntries, null, false, false), 5);
//        knn.runCommand();
//
//        assertValidPlaintextInputFiles(neighbours);
//        assertSizeGT(similarities, neighbours);
//        assertSizeGT(TEST_FRUIT_SIMS_100NN, neighbours);
//
//        // Finally, convert neighbours back to strings
//
//        deleteIfExist(neighboursStrings);
//
//        unindexSims(neighbours, suffix(neighbours, ".strings"), entryIndex,
//                    EnumeratorType.Memory, false, false, false);
//
//        assertValidPlaintextInputFiles(neighboursStrings);
//    }
//
//    @Test
//    public void serialBuildTest_SkipIndexed() throws Exception {
//        System.out.println("Testing Full Build (serial, preindexed, skip)");
//        final String affix = "sbsi.";
//
//        final File instances = TEST_FRUIT_INPUT;
//        final Charset charet = DEFAULT_CHARSET;
//        boolean preindexedEntries = true;
//        boolean preindexedFeatures = true;
//        boolean skipIndex1 = true;
//        boolean skipIndex2 = true;
//   EnumeratorType type = EnumeratorType.Memory;
//   
//        File instancesIndexed = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".indexed");
//        File entryIndex = new File(TEST_OUTPUT_DIR,
//                                   affix + instances.getName() + ".entry-index");
//        File featureIndex = new File(TEST_OUTPUT_DIR,
//                                     affix + instances.getName() + ".feature-index");
//
//        File events = new File(TEST_OUTPUT_DIR,
//                               affix + instances.getName() + ".events");
//        File entries = new File(TEST_OUTPUT_DIR,
//                                affix + instances.getName() + ".entries");
//        File features = new File(TEST_OUTPUT_DIR,
//                                 affix + instances.getName() + ".features");
//
//        File eventsFiltered = new File(TEST_OUTPUT_DIR,
//                                       events.getName() + ".filtered");
//        File entriesFiltered = new File(TEST_OUTPUT_DIR,
//                                        entries.getName() + ".filtered");
//        File featuresFiltered = new File(TEST_OUTPUT_DIR,
//                                         features.getName() + ".filtered");
//
//        File similarities = new File(TEST_OUTPUT_DIR,
//                                     affix + instances.getName() + ".sims");
//
//        File neighbours = new File(TEST_OUTPUT_DIR,
//                                   similarities.getName() + ".neighs");
//
//        File neighboursStrings = new File(TEST_OUTPUT_DIR,
//                                          neighbours.getName() + ".strings");
//
//
//
//        // Index the strings, reproducing the instances file in indexed form
//
//        deleteIfExist(entryIndex, featureIndex, instancesIndexed);
//
//        indexTP(instances, instancesIndexed, entryIndex, featureIndex, Enumerating.DEFAULT_TYPE,
//                skipIndex1, skipIndex2, false);
//
//        unindexTP(instancesIndexed, suffix(instancesIndexed, ".strings"),
//                  entryIndex, featureIndex,
//                  skipIndex1, skipIndex2);
//
//        // Count the entries, features and events
//
//        deleteIfExist(events, entries, features);
//
//        CountCommand count = new CountCommand(
//                instancesIndexed, events, entries, features,
//                new DoubleEnumeratingDeligate(Enumerating.DEFAULT_TYPE,
//                                              preindexedEntries, preindexedFeatures,
//                                              null, null, skipIndex1, skipIndex2),
//                charet);
//
//        count.runCommand();
//
//        assertValidPlaintextInputFiles(entries, features, events);
//        assertSizeGT(TEST_FRUIT_ENTRY_FEATURES, events);
//        assertSizeGT(TEST_FRUIT_ENTRIES, entries);
//        assertSizeGT(TEST_FRUIT_FEATURES, features);
//
//        unindexWT(entries, suffix(entries, ".strings"), entryIndex,
//                  skipIndex1, skipIndex2);
//        unindexWT(features, suffix(features, ".strings"), featureIndex,
//                  skipIndex1, skipIndex2);
//        unindexWTP(events, suffix(events, ".strings"), entryIndex, featureIndex,
//                   skipIndex1, skipIndex2);
//
//        // Filter 
//
//
//
//        deleteIfExist(eventsFiltered, featuresFiltered, entriesFiltered);
//
//
//        filter(events, entries, features, eventsFiltered, entriesFiltered,
//               featuresFiltered, entryIndex, featureIndex, preindexedEntries,
//               preindexedFeatures, skipIndex1, skipIndex2);
//
//
//        unindexWT(entriesFiltered, suffix(entriesFiltered, ".strings"),
//                  entryIndex, skipIndex1, skipIndex2);
//        unindexWT(featuresFiltered, suffix(featuresFiltered, ".strings"),
//                  featureIndex, skipIndex1, skipIndex2);
//        unindexWTP(eventsFiltered, suffix(eventsFiltered, ".strings"),
//                   entryIndex, featureIndex, skipIndex1, skipIndex2);
//
//        // All pairs
//
//        assertValidPlaintextInputFiles(eventsFiltered, entriesFiltered, featuresFiltered);
//        deleteIfExist(similarities);
//
//        AllPairsCommand allpairs = new AllPairsCommand(
//                entriesFiltered, featuresFiltered, eventsFiltered, similarities,
//                charet,
//                new DoubleEnumeratingDeligate(Enumerating.DEFAULT_TYPE,
//                                              preindexedEntries, preindexedFeatures,
//                                              null, null, skipIndex1, skipIndex2));
//        allpairs.setnThreads(1);
//        allpairs.runCommand();
//
//        assertValidPlaintextInputFiles(similarities);
//        assertSizeGT(TEST_FRUIT_SIMS, similarities);
//
//        unindexSims(similarities, suffix(similarities, ".strings"), entryIndex,
//                    EnumeratorType.Memory, skipIndex1, skipIndex2, false);
//
//        // KNN
//
//
//        deleteIfExist(neighbours);
//
//        knn(similarities, neighbours, preindexedEntries,
//            skipIndex1, skipIndex2);
//
//        // Finally, convert neighbours back to strings
//
//        deleteIfExist(neighboursStrings);
//
//        unindexSims(neighbours, suffix(neighbours, ".strings"), entryIndex,
//                    EnumeratorType.Memory, skipIndex1, skipIndex2, false);
//
//    }
//    @Test
//    public void parallelBuildTest_SkipIndexed() throws Exception {
//        System.out.println("Testing Full Build (parallel, preindexed, skip)");
//        final String affix = "x.";
//        boolean serial = true;
//        boolean preindexedEntries = true;
//        boolean preindexedFeatures = true;
//        boolean skipIndex1 = true;
//        boolean skipIndex2 = true;
//        EnumeratorType type = EnumeratorType.Memory;
//
//            File instancesIndexed = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".indexed");
//        File entryIndex = new File(TEST_OUTPUT_DIR,
//                                   affix + instances.getName() + ".entry-index");
//        File featureIndex = new File(TEST_OUTPUT_DIR,
//                                     affix + instances.getName() + ".feature-index");
//
//        File events = new File(TEST_OUTPUT_DIR,
//                               affix + instances.getName() + ".events");
//        File entries = new File(TEST_OUTPUT_DIR,
//                                affix + instances.getName() + ".entries");
//        File features = new File(TEST_OUTPUT_DIR,
//                                 affix + instances.getName() + ".features");
//
//        File eventsFiltered = new File(TEST_OUTPUT_DIR,
//                                       events.getName() + ".filtered");
//        File entriesFiltered = new File(TEST_OUTPUT_DIR,
//                                        entries.getName() + ".filtered");
//        File featuresFiltered = new File(TEST_OUTPUT_DIR,
//                                         features.getName() + ".filtered");
//
//        File similarities = new File(TEST_OUTPUT_DIR,
//                                     affix + instances.getName() + ".sims");
//
//        File neighbours = new File(TEST_OUTPUT_DIR,
//                                   similarities.getName() + ".neighs");
//
//        File neighboursStrings = new File(TEST_OUTPUT_DIR,
//                                          neighbours.getName() + ".strings");
//
//
//
//        // Index the strings, reproducing the instances file in indexed form
//
//        deleteIfExist(entryIndex, featureIndex, instancesIndexed);
//
//        indexTP(instances, instancesIndexed, entryIndex, featureIndex, Enumerating.DEFAULT_TYPE,
//                skipIndex1, skipIndex2, false);
//
//        unindexTP(instancesIndexed, suffix(instancesIndexed, ".strings"),
//                  entryIndex, featureIndex,
//                  skipIndex1, skipIndex2);
//
//        // Count the entries, features and events
//
//        deleteIfExist(events, entries, features);
//
//        ExternalCountCommand count = new ExternalCountCommand();
//        count.setInstancesFile(instancesIndexed);
//        count.setEntriesFile(entries);
//        count.setFeaturesFile(features);
//        count.setEntryFeaturesFile(events);
//        count.setIndexDeligate(new DoubleEnumeratingDeligate(
//                Enumerating.DEFAULT_TYPE,
//                preindexedEntries, preindexedFeatures,
//                null, null, skipIndex1, skipIndex2));
//        count.getFileDeligate().setCharset(charet);
//        count.runCommand();
//
//        assertValidPlaintextInputFiles(entries, features, events);
//        assertSizeGT(TEST_FRUIT_ENTRY_FEATURES, events);
//        assertSizeGT(TEST_FRUIT_ENTRIES, entries);
//        assertSizeGT(TEST_FRUIT_FEATURES, features);
//
//        unindexWT(entries, suffix(entries, ".strings"), entryIndex,
//                  skipIndex1, skipIndex2);
//        unindexWT(features, suffix(features, ".strings"), featureIndex,
//                  skipIndex1, skipIndex2);
//        unindexWTP(events, suffix(events, ".strings"), entryIndex, featureIndex,
//                   skipIndex1, skipIndex2);
//
//        // Filter 
//
//
//
//        deleteIfExist(eventsFiltered, featuresFiltered, entriesFiltered);
//
//
//        filter(events, entries, features, eventsFiltered, entriesFiltered,
//               featuresFiltered, entryIndex, featureIndex, preindexedEntries,
//               preindexedFeatures, skipIndex1, skipIndex2);
//
//
//        unindexWT(entriesFiltered, suffix(entriesFiltered, ".strings"),
//                  entryIndex, skipIndex1, skipIndex2);
//        unindexWT(featuresFiltered, suffix(featuresFiltered, ".strings"),
//                  featureIndex, skipIndex1, skipIndex2);
//        unindexWTP(eventsFiltered, suffix(eventsFiltered, ".strings"),
//                   entryIndex, featureIndex, skipIndex1, skipIndex2);
//
//        // All pairs
//
//        assertValidPlaintextInputFiles(eventsFiltered, entriesFiltered, featuresFiltered);
//        deleteIfExist(similarities);
//
//        AllPairsCommand allpairs = new AllPairsCommand(
//                entriesFiltered, featuresFiltered, eventsFiltered, similarities,
//                charet, new DoubleEnumeratingDeligate(Enumerating.DEFAULT_TYPE,
//                                                      preindexedEntries, preindexedFeatures,
//                                                      null, null, skipIndex1, skipIndex2));
//        allpairs.runCommand();
//
//        assertValidPlaintextInputFiles(similarities);
//        assertSizeGT(TEST_FRUIT_SIMS, similarities);
//
//        unindexSims(similarities, suffix(similarities, ".strings"), entryIndex,
//                    EnumeratorType.Memory, skipIndex1, skipIndex2, false);
//
//        // KNN
//
//
//        deleteIfExist(neighbours);
//
//        extknn(similarities, neighbours, preindexedEntries,
//               skipIndex1, skipIndex2);
//
//        // Finally, convert neighbours back to strings
//
//        deleteIfExist(neighboursStrings);
//
//        unindexSims(neighbours, suffix(neighbours, ".strings"), entryIndex,
//                    EnumeratorType.Memory, skipIndex1, skipIndex2, false);
//
//    }
    @Test
    public void BuildTest() throws Exception {
        final String affix = "1-";
        boolean serial = true;
        boolean preindexedEntries = false;
        boolean preindexedFeatures = false;
        boolean skipIndex1 = false;
        boolean skipIndex2 = false;
        EnumeratorType type = EnumeratorType.Memory;

        parallelBuildTest(affix, serial, type,
                          preindexedEntries, preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    public void BuildTest_parallel() throws Exception {
        final String affix = "2-";
        boolean serial = false;
        boolean preindexedEntries = false;
        boolean preindexedFeatures = false;
        boolean skipIndex1 = false;
        boolean skipIndex2 = false;
        EnumeratorType type = EnumeratorType.Memory;

        parallelBuildTest(affix, serial, type,
                          preindexedEntries, preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    public void BuildTest_preindex() throws Exception {
        final String affix = "3-";
        boolean serial = true;
        boolean preindexedEntries = true;
        boolean preindexedFeatures = true;
        boolean skipIndex1 = false;
        boolean skipIndex2 = false;
        EnumeratorType type = EnumeratorType.Memory;

        parallelBuildTest(affix, serial, type,
                          preindexedEntries, preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    public void BuildTest_parallel_preindex() throws Exception {
        final String affix = "4-";
        boolean serial = false;
        boolean preindexedEntries = true;
        boolean preindexedFeatures = true;
        boolean skipIndex1 = false;
        boolean skipIndex2 = false;
        EnumeratorType type = EnumeratorType.Memory;

        parallelBuildTest(affix, serial, type,
                          preindexedEntries, preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    public void BuildTest_skipindex() throws Exception {
        final String affix = "5-";
        boolean serial = true;
        boolean preindexedEntries = true;
        boolean preindexedFeatures = true;
        boolean skipIndex1 = true;
        boolean skipIndex2 = true;
        EnumeratorType type = EnumeratorType.Memory;

        parallelBuildTest(affix, serial, type,
                          preindexedEntries, preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    public void BuildTest_parallel_skipindex() throws Exception {
        final String affix = "6-";
        boolean serial = false;
        boolean preindexedEntries = true;
        boolean preindexedFeatures = true;
        boolean skipIndex1 = true;
        boolean skipIndex2 = true;
        EnumeratorType type = EnumeratorType.Memory;

        parallelBuildTest(affix, serial, type,
                          preindexedEntries, preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    public void BuildTest_jdbc() throws Exception {
        final String affix = "7-";
        boolean serial = true;
        boolean preindexedEntries = false;
        boolean preindexedFeatures = false;
        boolean skipIndex1 = false;
        boolean skipIndex2 = false;
        EnumeratorType type = EnumeratorType.JDBC;

        parallelBuildTest(affix, serial, type,
                          preindexedEntries, preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    public void BuildTest_parallel_jdbc() throws Exception {
        final String affix = "8-";
        boolean serial = false;
        boolean preindexedEntries = false;
        boolean preindexedFeatures = false;
        boolean skipIndex1 = false;
        boolean skipIndex2 = false;
        EnumeratorType type = EnumeratorType.JDBC;

        parallelBuildTest(affix, serial, type,
                          preindexedEntries, preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    public void BuildTest_preindex_jdbc() throws Exception {
        final String affix = "9-";
        boolean serial = true;
        boolean preindexedEntries = true;
        boolean preindexedFeatures = true;
        boolean skipIndex1 = false;
        boolean skipIndex2 = false;
        EnumeratorType type = EnumeratorType.JDBC;

        parallelBuildTest(affix, serial, type,
                          preindexedEntries, preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    public void BuildTest_parallel_preindex_jdbc() throws Exception {
        final String affix = "10-";
        boolean serial = false;
        boolean preindexedEntries = true;
        boolean preindexedFeatures = true;
        boolean skipIndex1 = false;
        boolean skipIndex2 = false;
        EnumeratorType type = EnumeratorType.JDBC;

        parallelBuildTest(affix, serial, type,
                          preindexedEntries, preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    public void BuildTest_skipindex_jdbc() throws Exception {
        final String affix = "11-";
        boolean serial = true;
        boolean preindexedEntries = true;
        boolean preindexedFeatures = true;
        boolean skipIndex1 = true;
        boolean skipIndex2 = true;
        EnumeratorType type = EnumeratorType.JDBC;

        parallelBuildTest(affix, serial, type,
                          preindexedEntries, preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    public void BuildTest_parallel_skipindex_jdbc() throws Exception {
        final String affix = "12-";
        boolean serial = false;
        boolean preindexedEntries = true;
        boolean preindexedFeatures = true;
        boolean skipIndex1 = true;
        boolean skipIndex2 = true;
        EnumeratorType type = EnumeratorType.JDBC;

        parallelBuildTest(affix, serial, type,
                          preindexedEntries, preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    public void BuildTestall() throws Exception {
        for (boolean serial : new boolean[]{true, false}) {
            for (boolean preindexedEntries : new boolean[]{true, false}) {
                for (boolean preindexedFeatures : new boolean[]{true, false}) {
                    for (boolean skipIndex1 : new boolean[]{true, false}) {
                        for (boolean skipIndex2 : new boolean[]{true, false}) {
                            for (EnumeratorType type : EnumeratorType.values()) {
                                String affix = "X";
                                affix += serial ? ".ser" : ".par";
                                affix += preindexedEntries ? ".xe" : "";
                                affix += preindexedFeatures ? ".xf" : "";
                                affix += skipIndex1 ? ".s1" : "";
                                affix += skipIndex2 ? ".s2" : "";
                                affix += "." + type.toString();
                                affix += "__";
                                System.out.println(affix);
                                parallelBuildTest(
                                        affix, serial, type,
                                        preindexedEntries, preindexedFeatures,
                                        skipIndex1, skipIndex2);
                            }
                        }
                    }
                }
            }
        }

    }

    public void parallelBuildTest(
            String affix,
            boolean serial, EnumeratorType type,
            boolean preindexedEntries, boolean preindexedFeatures,
            boolean skipIndex1, boolean skipIndex2)
            throws Exception {

        File instances = TEST_FRUIT_INPUT;
        final Charset charet = DEFAULT_CHARSET;

        File instancesIndexed = new File(TEST_OUTPUT_DIR, affix + instances.getName() + ".indexed");

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



        if (preindexedEntries || preindexedFeatures) {
            // Index the strings, reproducing the instances file in indexed form

            deleteIfExist(entryIndex, featureIndex, instancesIndexed);

            indexTP(instances, instancesIndexed, entryIndex, featureIndex,
                    type, skipIndex1, skipIndex2, false);


            instances = instancesIndexed;
        }
        // Count the entries, features and events

        deleteIfExist(events, entries, features);

        DoubleEnumeratingDeligate countIndex = new DoubleEnumeratingDeligate(
                type, preindexedEntries, preindexedFeatures,
                entryIndex, featureIndex);
        if (serial) {
            CountCommand count = new CountCommand();
            count.setInstancesFile(instances);
            count.setEntriesFile(entries);
            count.setFeaturesFile(features);
            count.setEventsFile(events);
            count.setIndexDeligate(countIndex);
            count.setCharset(charet);
            count.runCommand();
        } else {
            ExternalCountCommand count = new ExternalCountCommand();
            count.setInstancesFile(instances);
            count.setEntriesFile(entries);
            count.setFeaturesFile(features);
            count.setEventsFile(events);
            count.setIndexDeligate(countIndex);
            count.getFileDeligate().setCharset(charet);
            count.runCommand();
        }

        assertValidPlaintextInputFiles(entries, features, events);


        // Filter 



        deleteIfExist(eventsFiltered, featuresFiltered, entriesFiltered);


        filter(type, events, entries, features, eventsFiltered, entriesFiltered,
               featuresFiltered, entryIndex, featureIndex, preindexedEntries,
               preindexedFeatures, skipIndex1, skipIndex2);


        // All pairs

        assertValidPlaintextInputFiles(eventsFiltered, entriesFiltered, featuresFiltered);
        deleteIfExist(similarities);

        DoubleEnumeratingDeligate allpairsIndex = new DoubleEnumeratingDeligate(
                type, preindexedEntries, preindexedFeatures,
                entryIndex, featureIndex);
        AllPairsCommand allpairs = new AllPairsCommand(
                entriesFiltered, featuresFiltered, eventsFiltered, similarities,
                charet, allpairsIndex);
        if (serial)
            allpairs.setNumThreads(1);
        allpairs.runCommand();

        assertValidPlaintextInputFiles(similarities);
        assertSizeGT(TEST_FRUIT_SIMS, similarities);

        // KNN


        deleteIfExist(neighbours);

        if (serial) {
            knn(similarities, neighbours, type, preindexedEntries,
                skipIndex1, skipIndex2);
        } else {
            extknn(similarities, neighbours, type, preindexedEntries,
                   skipIndex1, skipIndex2);
        }

        // Finally, convert neighbours back to strings

        deleteIfExist(neighboursStrings);

        if (preindexedEntries || preindexedFeatures) {
            unindexSims(neighbours, suffix(neighbours, ".strings"), entryIndex,
                        type, skipIndex1, skipIndex2, false);
        }

    }

    private static void filter(
            EnumeratorType type,
            File events, File entries, File features,
            File eventsFiltered, File entriesFiltered, File featuresFiltered,
            File entryIndex, File featureIndex,
            boolean preindexedEntries, boolean preindexedFeatures,
            boolean skipIndex1, boolean skipIndex2) throws Exception {

        assertValidPlaintextInputFiles(events, entries, features);
        assertValidOutputFiles(eventsFiltered, entriesFiltered, featuresFiltered);

        FilterCommand filter = new FilterCommand(
                events, entries, features,
                eventsFiltered, entriesFiltered, featuresFiltered,
                DEFAULT_CHARSET);
        filter.setIndexDeligate(new DoubleEnumeratingDeligate(
                type,
                preindexedEntries, preindexedFeatures,
                entryIndex, featureIndex));
        filter.addEntryFeatureMinimumFrequency(2);

        filter.runCommand();

        assertValidPlaintextInputFiles(eventsFiltered, entriesFiltered, featuresFiltered);
//        assertSizeGT(events, eventsFiltered);
//        assertSizeGT(entries, entriesFiltered);
//        assertSizeGT(features, featuresFiltered);
    }

    private static void knn(File from, File to, EnumeratorType type,
                            boolean enumerated,
                            boolean skip1, boolean skip2)
            throws Exception {
        assertValidPlaintextInputFiles(from);

        KnnSimsCommand knn = new KnnSimsCommand(
                from, to, DEFAULT_CHARSET,
                new SingleEnumeratingDeligate(type,
                                              enumerated, null), 5);
        knn.runCommand();

        assertValidPlaintextInputFiles(to);
        assertSizeGT(from, to);
    }

    private static void extknn(File from, File to, EnumeratorType type,
                               boolean enumerated,
                               boolean skip1, boolean skip2)
            throws Exception {
        assertValidPlaintextInputFiles(from);

        ExternalKnnSimsCommand knn = new ExternalKnnSimsCommand(
                from, to, DEFAULT_CHARSET,
                new SingleEnumeratingDeligate(type,
                                              enumerated, null), 5);
        knn.runCommand();

        assertValidPlaintextInputFiles(to);
        assertSizeGT(from, to);
    }

//    private static void indexTP(File from, File to,
//                                File index1, File index2,
//                                boolean skip1, boolean skip2)
//            throws Exception {
//        assertValidPlaintextInputFiles(from);
//        assertValidOutputFiles(to);
//
//        IndexTPCommand unindex = new IndexTPCommand();
//        unindex.getFilesDeligate().setCharset(DEFAULT_CHARSET);
//        unindex.getFilesDeligate().setSourceFile(from);
//        unindex.getFilesDeligate().setDestinationFile(to);
//        unindex.getFilesDeligate().setCompactFormatDisabled(false);
//        unindex.setIndexDeligate(new EnumeratorPairBaringDeligate(
//                true, true, index1, index2, skip1, skip2));
//        unindex.runCommand();
//
//        assertValidPlaintextInputFiles(to);
//        assertSizeGT(from, to);
//    }
//
//    private static void unindexSims(File from, File to, File index,
//                                    boolean skip1, boolean skip2)
//            throws Exception {
//        assertValidPlaintextInputFiles(from);
//
//        UnindexSimsCommand unindex = new UnindexSimsCommand();
//        unindex.getFilesDeligate().setCharset(DEFAULT_CHARSET);
//        unindex.getFilesDeligate().setSourceFile(from);
//        unindex.getFilesDeligate().setDestinationFile(to);
//        unindex.getFilesDeligate().setCompactFormatDisabled(false);
//        unindex.setIndexDeligate(new EnumeratorSingleBaringDeligate(true, index, null, skip1, skip2));
//        unindex.runCommand();
//
//        assertValidPlaintextInputFiles(to);
//        assertSizeGT(to, from);
//    }
    private static void unindexWT(EnumeratorType type, File from, File to, File index,
                                  boolean skip1, boolean skip2)
            throws Exception {
        assertValidPlaintextInputFiles(from);

        UnindexEntriesCommand unindex = new UnindexEntriesCommand();
        unindex.getFilesDeligate().setCharset(DEFAULT_CHARSET);
        unindex.getFilesDeligate().setSourceFile(from);
        unindex.getFilesDeligate().setDestinationFile(to);
        unindex.setIndexDeligate(new SingleEnumeratingDeligate(type, true, index));
        unindex.runCommand();

        assertValidPlaintextInputFiles(to);
        assertSizeGT(to, from);
    }

    private static void unindexWTP(EnumeratorType type, File from, File to,
                                   File index1, File index2,
                                   boolean skip1, boolean skip2)
            throws Exception {
        assertValidPlaintextInputFiles(from);

        UnindexEventsCommand unindex = new UnindexEventsCommand();
        unindex.getFilesDeligate().setCharset(DEFAULT_CHARSET);
        unindex.getFilesDeligate().setSourceFile(from);
        unindex.getFilesDeligate().setDestinationFile(to);
        unindex.setIndexDeligate(new DoubleEnumeratingDeligate(type, true, true,
                                                               index1, index2));
        unindex.runCommand();

        assertValidPlaintextInputFiles(to);
        assertSizeGT(to, from);
    }

    private static void unindexTP(EnumeratorType type, File from, File to,
                                  File index1, File index2,
                                  boolean skip1, boolean skip2)
            throws Exception {
        assertValidPlaintextInputFiles(from);

        UnindexInstancesCommand unindex = new UnindexInstancesCommand();
        unindex.getFilesDeligate().setCharset(DEFAULT_CHARSET);
        unindex.getFilesDeligate().setSourceFile(from);
        unindex.getFilesDeligate().setDestinationFile(to);
        unindex.setIndexDeligate(new DoubleEnumeratingDeligate(type, true, true, index1, index2));
        unindex.runCommand();

        assertValidPlaintextInputFiles(to);
        assertSizeGT(to, from);
    }

}
