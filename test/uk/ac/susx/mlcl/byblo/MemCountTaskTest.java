/*
 * Copyright (c) 2010-2011, University of Sussex
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

import com.google.common.io.Files;
import java.io.File;
import java.nio.charset.Charset;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Hamish Morgan (hamish.morgan@sussex.ac.uk)
 */
public class MemCountTaskTest {

    private static final File SAMPLE_DATA = new File("sampledata");

    private static final File OUTPUT = new File(SAMPLE_DATA, "out");

    private static final Charset CHARSET = Charset.forName("UTF-8");

    private static final String FRUIT_PREFIX = "bnc-gramrels-fruit";

    private static final String SPARSE_PREFIX = "bnc-gramrels-sparse";

    private static final File FRUIT_INSTANCES =
            new File(SAMPLE_DATA, FRUIT_PREFIX);

    private static final File SPARSE_INSTANCES =
            new File(SAMPLE_DATA, SPARSE_PREFIX);

    private static final File BIG_DATA = new File(
            "/Volumes/Local Scratch HD/bnc/thesaurus-bnc.rasp2.gramrels/data");

    private static final File BIG_INSTANCES = new File(
            BIG_DATA, "bnc.rasp2.gramrels.lcase");

    public MemCountTaskTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        System.out.flush();
        System.err.flush();
    }

    @Test
    public void testRunOnFruit() throws Exception {
        System.out.println("Testing CountTask on " + FRUIT_INSTANCES);
        final File entries = new File(OUTPUT, FRUIT_PREFIX + ".entries");
        final File contexts = new File(OUTPUT, FRUIT_PREFIX + ".contexts");
        final File features = new File(OUTPUT, FRUIT_PREFIX + ".features");

        final CountTask countTask = new CountTask(
                FRUIT_INSTANCES, features, entries, contexts, CHARSET);
        countTask.run();

        while (countTask.isExceptionThrown())
            countTask.throwException();

        assertTrue("Output files not created.", entries.exists()
                && contexts.exists() && features.exists());
        assertTrue("Empty output file found.", entries.length() > 0
                && contexts.length() > 0 && features.length() > 0);

        // NB: entries file differs due to indexing strategy

//        assertTrue("Output entries file length differs from sampledata file.",
//                entries.length() == new File(SAMPLE_DATA, entries.getName()).length());
        assertTrue("Output context file length differs from sampledata file.",
                   contexts.length() == new File(SAMPLE_DATA, contexts.getName()).
                length());
        assertTrue("Output feature file length differs from sampledata file.",
                   features.length() == new File(SAMPLE_DATA, features.getName()).
                length());

//        assertTrue("Output entries file differs from sampledata file.",
//                Files.equal(entries, new File(SAMPLE_DATA, entries.getName())));
        assertTrue("Output context file differs from sampledata file.",
                   Files.equal(contexts, new File(SAMPLE_DATA,
                                                  contexts.getName())));
        assertTrue("Output feature file differs from sampledata file.",
                   Files.equal(features, new File(SAMPLE_DATA,
                                                  features.getName())));

    }

//    @Test
//    public void testRunOnSparse() throws Exception {
//        System.out.println("Testing CountTask on " + SPARSE_INSTANCES);
//        File entries = new File(OUTPUT, SPARSE_PREFIX + ".entries");
//        File contexts = new File(OUTPUT, SPARSE_PREFIX + ".contexts");
//        File features = new File(OUTPUT, SPARSE_PREFIX + ".features");
//
//        CountTask countTask = new CountTask(
//                SPARSE_INSTANCES, features, entries, contexts, CHARSET);
//        countTask.run();
//        while (countTask.isExceptionThrown())
//            countTask.throwException();
//
//        // NB: entries file differs due to indexing strategy
////        assertTrue("Output entries file length differs from sampledata file.",
////                entries.length() == new File(SAMPLE_DATA, entries.getName()).length());
//
//        assertTrue(
//                "Output context file (" + contexts + ") length differs from sampledata file.",
//                contexts.length() == new File(SAMPLE_DATA, contexts.getName()).
//                length());
//        assertTrue(
//                "Output feature file (" + features + ") length differs from sampledata file.",
//                features.length() == new File(SAMPLE_DATA, features.getName()).
//                length());
//
//        // NB: entries file differs due to indexing strategy
////        assertTrue("Output entries file differs from sampledata file.",
////                Files.equal(entries, new File(SAMPLE_DATA, entries.getName())));
//
//        // NB: Context words aren't concatonated with ':' in the new version
////        assertTrue(
////                "Output context file (" + contexts + ") differs from sampledata file.",
////                   Files.equal(contexts, new File(SAMPLE_DATA,
////
////        assertTrue(
////                "Output feature file (" + features + ") differs from sampledata file.",
////                Files.equal(features, new File(SAMPLE_DATA,
////                                               features.getName())));
//
//    }
//
//    @Test
//    @Ignore(value="Takes an inordinate amount of time.")
//    public void testRunOnLargedata() throws Exception {
//        System.out.println("Testing CountTask on " + BIG_INSTANCES);
//
//        // Don't run the tests if the data doesn't exist
//        assumeTrue(BIG_INSTANCES.exists());
//
//        File entriesFile = new File(OUTPUT, "bnc.rasp2.gramrels.lcase.entries");
//        File contextFile = new File(OUTPUT,
//                                    "bnc.rasp2.gramrels.lcase.contexts");
//        File featureFile = new File(OUTPUT,
//                                    "bnc.rasp2.gramrels.lcase.features");
//
//        CountTask countTask = new CountTask(BIG_INSTANCES, featureFile,
//                                            entriesFile, contextFile, CHARSET);
//        countTask.run();
//        while (countTask.isExceptionThrown())
//            countTask.throwException();
//
//        assertTrue("Output files not created.", entriesFile.exists()
//                && contextFile.exists() && featureFile.exists());
//        assertTrue("Empty output file found.", entriesFile.length() > 0
//                && contextFile.length() > 0 && featureFile.length() > 0);
//
//    }
//
//    @Test
//    public void testMissingParameters() throws Exception {
//        System.out.println("Testing CountTask for bad parameterisation.");
//
//        final File instances = new File(SAMPLE_DATA, FRUIT_PREFIX);
//        final File entries = new File(OUTPUT, FRUIT_PREFIX + ".entries");
//        final File contexts = new File(OUTPUT, FRUIT_PREFIX + ".contexts");
//        final File features = new File(OUTPUT, FRUIT_PREFIX + ".features");
//
//        // Tests for required parameters not being set at all
//
//        try {
//            final CountTask countTask = new CountTask();
////            countTask.setFeaturesFile(contexts);
//            countTask.setEntriesFile(entries);
//            countTask.setInstancesFile(instances);
//            countTask.setEntryFeaturesFile(features);
//            countTask.run();
//            while (countTask.isExceptionThrown())
//                countTask.throwException();
//            fail("NullPointerException should have been "
//                    + "thrown due to missing parameter.");
//        } catch (NullPointerException ex) {
//            // pass
//        }
//
//        try {
//            final CountTask countTask = new CountTask();
//            countTask.setFeaturesFile(contexts);
////            countTask.setEntriesFile(entries);
//            countTask.setInstancesFile(instances);
//            countTask.setEntryFeaturesFile(features);
//            countTask.run();
//            while (countTask.isExceptionThrown())
//                countTask.throwException();
//            fail("NullPointerException should have been "
//                    + "thrown due to missing parameter.");
//        } catch (NullPointerException ex) {
//            // pass
//        }
//
//        try {
//            final CountTask countTask = new CountTask();
//            countTask.setFeaturesFile(contexts);
//            countTask.setEntriesFile(entries);
////            countTask.setInstancesFile(instances);
//            countTask.setEntryFeaturesFile(features);
//            countTask.run();
//            while (countTask.isExceptionThrown())
//                countTask.throwException();
//            fail("NullPointerException should have been "
//                    + "thrown due to missing parameter.");
//        } catch (NullPointerException ex) {
//            // pass
//        }
//
//        try {
//            final CountTask countTask = new CountTask();
//            countTask.setFeaturesFile(contexts);
//            countTask.setEntriesFile(entries);
//            countTask.setInstancesFile(instances);
////            countTask.setEntryFeaturesFile(features);
//            countTask.run();
//            while (countTask.isExceptionThrown())
//                countTask.throwException();
//            fail("NullPointerException should have been "
//                    + "thrown due to missing parameter.");
//        } catch (NullPointerException ex) {
//            // pass
//        }
//
//
//        // Tests for required files being set to a directory
//
//        try {
//            final CountTask countTask = new CountTask();
//            countTask.setFeaturesFile(OUTPUT);
//            countTask.setEntriesFile(entries);
//            countTask.setInstancesFile(instances);
//            countTask.setEntryFeaturesFile(features);
//            countTask.run();
//            while (countTask.isExceptionThrown())
//                countTask.throwException();
//            fail("NullPointerException should have been "
//                    + "thrown due to missing parameter.");
//        } catch (IllegalStateException ex) {
//            // pass
//        }
//
//
//
//        try {
//            final CountTask countTask = new CountTask();
//            countTask.setFeaturesFile(contexts);
//            countTask.setEntriesFile(OUTPUT);
//            countTask.setInstancesFile(instances);
//            countTask.setEntryFeaturesFile(features);
//            countTask.run();
//            while (countTask.isExceptionThrown())
//                countTask.throwException();
//            fail("NullPointerException should have been "
//                    + "thrown due to missing parameter.");
//        } catch (IllegalStateException ex) {
//            // pass
//        }
//
//        try {
//            final CountTask countTask = new CountTask();
//            countTask.setFeaturesFile(contexts);
//            countTask.setEntriesFile(entries);
//            countTask.setInstancesFile(OUTPUT);
//            countTask.setEntryFeaturesFile(features);
//            countTask.run();
//            while (countTask.isExceptionThrown())
//                countTask.throwException();
//            fail("NullPointerException should have been "
//                    + "thrown due to missing parameter.");
//        } catch (IllegalStateException ex) {
//            // pass
//        }
//
//        try {
//            final CountTask countTask = new CountTask();
//            countTask.setFeaturesFile(contexts);
//            countTask.setEntriesFile(entries);
//            countTask.setInstancesFile(instances);
//            countTask.setEntryFeaturesFile(OUTPUT);
//            countTask.run();
//            while (countTask.isExceptionThrown())
//                countTask.throwException();
//            fail("NullPointerException should have been "
//                    + "thrown due to missing parameter.");
//        } catch (IllegalStateException ex) {
//            // pass
//        }
//    }
}
