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
package uk.ac.susx.mlcl.byblo;

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import uk.ac.susx.mlcl.TestConstants;
import uk.ac.susx.mlcl.byblo.commands.*;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratingDelegates;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratorType;
import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumeratingDelegate;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import uk.ac.susx.mlcl.testing.SlowTestCategory;

import java.io.File;
import java.nio.charset.Charset;

import static uk.ac.susx.mlcl.TestConstants.*;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class FullBuildTest2 {


    @Test
    public void BuildTest() throws Exception {
        final String affix = "1-";
        boolean serial = true;
        boolean preindexedEntries = false;
        boolean preindexedFeatures = false;
        boolean skipIndex1 = false;
        boolean skipIndex2 = false;
        EnumeratorType type = EnumeratorType.Memory;

        parallelBuildTest(affix, serial, type, preindexedEntries,
                preindexedFeatures, skipIndex1, skipIndex2);

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

        parallelBuildTest(affix, serial, type, preindexedEntries,
                preindexedFeatures, skipIndex1, skipIndex2);

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

        parallelBuildTest(affix, serial, type, preindexedEntries,
                preindexedFeatures, skipIndex1, skipIndex2);

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

        parallelBuildTest(affix, serial, type, preindexedEntries,
                preindexedFeatures, skipIndex1, skipIndex2);

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

        parallelBuildTest(affix, serial, type, preindexedEntries,
                preindexedFeatures, skipIndex1, skipIndex2);

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

        parallelBuildTest(affix, serial, type, preindexedEntries,
                preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    public void BuildTest_jdbm() throws Exception {
        final String affix = "7-";
        boolean serial = true;
        boolean preindexedEntries = false;
        boolean preindexedFeatures = false;
        boolean skipIndex1 = false;
        boolean skipIndex2 = false;
        EnumeratorType type = EnumeratorType.JDBM;

        parallelBuildTest(affix, serial, type, preindexedEntries,
                preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    public void BuildTest_parallel_jdbm() throws Exception {
        final String affix = "8-";
        boolean serial = false;
        boolean preindexedEntries = false;
        boolean preindexedFeatures = false;
        boolean skipIndex1 = false;
        boolean skipIndex2 = false;
        EnumeratorType type = EnumeratorType.JDBM;

        parallelBuildTest(affix, serial, type, preindexedEntries,
                preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    public void BuildTest_preindex_jdbm() throws Exception {
        final String affix = "9-";
        boolean serial = true;
        boolean preindexedEntries = true;
        boolean preindexedFeatures = true;
        boolean skipIndex1 = false;
        boolean skipIndex2 = false;
        EnumeratorType type = EnumeratorType.JDBM;

        parallelBuildTest(affix, serial, type, preindexedEntries,
                preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    public void BuildTest_parallel_preindex_jdbm() throws Exception {
        final String affix = "10-";
        boolean serial = false;
        boolean preindexedEntries = true;
        boolean preindexedFeatures = true;
        boolean skipIndex1 = false;
        boolean skipIndex2 = false;
        EnumeratorType type = EnumeratorType.JDBM;

        parallelBuildTest(affix, serial, type, preindexedEntries,
                preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    public void BuildTest_skipindex_jdbm() throws Exception {
        final String affix = "11-";
        boolean serial = true;
        boolean preindexedEntries = true;
        boolean preindexedFeatures = true;
        boolean skipIndex1 = true;
        boolean skipIndex2 = true;
        EnumeratorType type = EnumeratorType.JDBM;

        parallelBuildTest(affix, serial, type, preindexedEntries,
                preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    public void BuildTest_parallel_skipindex_jdbm() throws Exception {
        final String affix = "12-";
        boolean serial = false;
        boolean preindexedEntries = true;
        boolean preindexedFeatures = true;
        boolean skipIndex1 = true;
        boolean skipIndex2 = true;
        EnumeratorType type = EnumeratorType.JDBM;

        parallelBuildTest(affix, serial, type, preindexedEntries,
                preindexedFeatures, skipIndex1, skipIndex2);

    }

    @Test
    @Category(SlowTestCategory.class)
    @Ignore
    public void BuildTestAll() throws Exception {
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
                                parallelBuildTest(affix, serial, type,
                                        preindexedEntries, preindexedFeatures,
                                        skipIndex1, skipIndex2);
                            }
                        }
                    }
                }
            }
        }

    }

    void parallelBuildTest(String affix, boolean serial,
                           EnumeratorType type, boolean preindexedEntries,
                           boolean preindexedFeatures, boolean skipIndex1, boolean skipIndex2)
            throws Exception {

        File instances = TEST_FRUIT_INPUT;
        final Charset charset = DEFAULT_CHARSET;

        File instancesIndexed = new File(TEST_OUTPUT_DIR, affix
                + instances.getName() + ".indexed");

        File entryIndex = new File(TEST_OUTPUT_DIR, affix + instances.getName()
                + ".entry-index");
        File featureIndex = new File(TEST_OUTPUT_DIR, affix
                + instances.getName() + ".feature-index");

        File events = new File(TEST_OUTPUT_DIR, affix + instances.getName()
                + ".events");
        File entries = new File(TEST_OUTPUT_DIR, affix + instances.getName()
                + ".entries");
        File features = new File(TEST_OUTPUT_DIR, affix + instances.getName()
                + ".features");

        File eventsFiltered = new File(TEST_OUTPUT_DIR, events.getName()
                + ".filtered");
        File entriesFiltered = new File(TEST_OUTPUT_DIR, entries.getName()
                + ".filtered");
        File featuresFiltered = new File(TEST_OUTPUT_DIR, features.getName()
                + ".filtered");

        File similarities = new File(TEST_OUTPUT_DIR, affix
                + instances.getName() + ".sims");

        File neighbours = new File(TEST_OUTPUT_DIR, similarities.getName()
                + ".neighs");

        File neighboursStrings = new File(TEST_OUTPUT_DIR, neighbours.getName()
                + ".strings");

        if (preindexedEntries || preindexedFeatures) {
            // Index the strings, reproducing the instances file in indexed form

            deleteIfExist(entryIndex, featureIndex, instancesIndexed);

            IndexTPCommandTest.indexTP(instances, instancesIndexed, entryIndex,
                    featureIndex,
                    type, skipIndex1, skipIndex2, false);

            instances = instancesIndexed;
        }
        // Count the entries, features and events

        deleteIfExist(events, entries, features);

        DoubleEnumeratingDelegate countIndex = new DoubleEnumeratingDelegate(
                type, preindexedEntries, preindexedFeatures, entryIndex,
                featureIndex);
        if (serial) {
            CountCommand count = new CountCommand();
            count.setInstancesFile(instances);
            count.setEntriesFile(entries);
            count.setFeaturesFile(features);
            count.setEventsFile(events);
            count.setIndexDelegate(countIndex);
            count.setCharset(charset);
            Assert.assertTrue(count.runCommand());
        } else {
            ExternalCountCommand count = new ExternalCountCommand();
            count.setInstancesFile(instances);
            count.setEntriesFile(entries);
            count.setFeaturesFile(features);
            count.setEventsFile(events);
            count.setIndexDelegate(countIndex);
            count.getFileDelegate().setCharset(charset);
            Assert.assertTrue(count.runCommand());
        }

        assertValidPlaintextInputFiles(entries, features, events);

        // Filter

        deleteIfExist(eventsFiltered, featuresFiltered, entriesFiltered);

        filter(type, events, entries, features, eventsFiltered,
                entriesFiltered, featuresFiltered, entryIndex, featureIndex,
                preindexedEntries, preindexedFeatures, skipIndex1, skipIndex2);

        // All pairs

        assertValidPlaintextInputFiles(eventsFiltered, entriesFiltered,
                featuresFiltered);
        deleteIfExist(similarities);

        DoubleEnumeratingDelegate allpairsIndex = new DoubleEnumeratingDelegate(
                type, preindexedEntries, preindexedFeatures, entryIndex,
                featureIndex);
        AllPairsCommand allpairs = new AllPairsCommand(entriesFiltered,
                featuresFiltered, eventsFiltered, similarities, charset,
                allpairsIndex);
        if (serial)
            allpairs.setNumThreads(1);
        Assert.assertTrue(allpairs.runCommand());

        assertValidPlaintextInputFiles(similarities);
        assertSizeGT(TEST_FRUIT_SIMS, similarities);

        // KNN

        deleteIfExist(neighbours);

        if (serial) {
            knn(similarities, neighbours, type, preindexedEntries, skipIndex1,
                    skipIndex2);
        } else {
            extknn(similarities, neighbours, type, preindexedEntries,
                    skipIndex1, skipIndex2);
        }

        // Finally, convert neighbours back to strings

        deleteIfExist(neighboursStrings);

        if (preindexedEntries || preindexedFeatures) {
            IndexSimsCommandTest.unindexSims(neighbours,
                    suffix(neighbours, ".strings"), entryIndex,
                    type, skipIndex1, skipIndex2, false);
        }

    }

    private static void filter(EnumeratorType type, File events, File entries,
                               File features, File eventsFiltered, File entriesFiltered,
                               File featuresFiltered, File entryIndex, File featureIndex,
                               boolean preindexedEntries, boolean preindexedFeatures,
                               boolean skipIndex1, boolean skipIndex2) throws Exception {

        assertValidPlaintextInputFiles(events, entries, features);
        assertValidOutputFiles(eventsFiltered, entriesFiltered,
                featuresFiltered);

        FilterCommand filter = new FilterCommand(events, entries, features,
                eventsFiltered, entriesFiltered, featuresFiltered,
                DEFAULT_CHARSET);
        filter.setIndexDelegate(new DoubleEnumeratingDelegate(type,
                preindexedEntries, preindexedFeatures, entryIndex, featureIndex));
        filter.addEventMinimumFrequency(2);

        filter.setTempFiles(new TempFileFactory(TestConstants.TEST_TMP_DIR));
        Assert.assertTrue(filter.runCommand());

        assertValidPlaintextInputFiles(eventsFiltered, entriesFiltered,
                featuresFiltered);
        // assertSizeGT(events, eventsFiltered);
        // assertSizeGT(entries, entriesFiltered);
        // assertSizeGT(features, featuresFiltered);
    }

    private static void knn(File from, File to, EnumeratorType type,
                            boolean enumerated, boolean skip1, boolean skip2) throws Exception {
        assertValidPlaintextInputFiles(from);

        KnnSimsCommand knn = new KnnSimsCommand(from, to, DEFAULT_CHARSET,
                new SingleEnumeratingDelegate(type, enumerated, null), 5);
        Assert.assertTrue(knn.runCommand());

        assertValidPlaintextInputFiles(to);
        assertSizeGT(from, to);
    }

    private static void extknn(File from, File to, EnumeratorType type,
                               boolean enumerated, boolean skip1, boolean skip2) throws Exception {
        assertValidPlaintextInputFiles(from);

        ExternalKnnSimsCommand knn = new ExternalKnnSimsCommand(from, to,
                DEFAULT_CHARSET, new SingleEnumeratingDelegate(type,
                enumerated, null), 5);
        Assert.assertTrue(knn.runCommand());

        assertValidPlaintextInputFiles(to);
        assertSizeGT(from, to);
    }

    private static void unindexWT(EnumeratorType type, File from, File to,
                                  File index, boolean skip1, boolean skip2) throws Exception {
        assertValidPlaintextInputFiles(from);

        IndexingCommands.UnindexEntries unindex = new IndexingCommands.UnindexEntries();
        unindex.getFilesDelegate().setCharset(DEFAULT_CHARSET);
        unindex.getFilesDelegate().setSourceFile(from);
        unindex.getFilesDelegate().setDestinationFile(to);
        unindex.setIndexDelegate(EnumeratingDelegates
                .toPair(new SingleEnumeratingDelegate(type, true, index)));
        unindex.runCommand();

        assertValidPlaintextInputFiles(to);
        assertSizeGT(to, from);
    }

    private static void unindexWTP(EnumeratorType type, File from, File to,
                                   File index1, File index2, boolean skip1, boolean skip2)
            throws Exception {
        assertValidPlaintextInputFiles(from);

        IndexingCommands.IndexEvents unindex = new IndexingCommands.IndexEvents();
        unindex.getFilesDelegate().setCharset(DEFAULT_CHARSET);
        unindex.getFilesDelegate().setSourceFile(from);
        unindex.getFilesDelegate().setDestinationFile(to);
        unindex.setIndexDelegate(new DoubleEnumeratingDelegate(type, true,
                true, index1, index2));
        unindex.runCommand();

        assertValidPlaintextInputFiles(to);
        assertSizeGT(to, from);
    }

    private static void unindexTP(EnumeratorType type, File from, File to,
                                  File index1, File index2, boolean skip1, boolean skip2)
            throws Exception {
        assertValidPlaintextInputFiles(from);

        IndexingCommands.IndexInstances unindex = new IndexingCommands.IndexInstances();
        unindex.getFilesDelegate().setCharset(DEFAULT_CHARSET);
        unindex.getFilesDelegate().setSourceFile(from);
        unindex.getFilesDelegate().setDestinationFile(to);
        unindex.setIndexDelegate(new DoubleEnumeratingDelegate(type, true,
                true, index1, index2));
        unindex.runCommand();

        assertValidPlaintextInputFiles(to);
        assertSizeGT(to, from);
    }

}
