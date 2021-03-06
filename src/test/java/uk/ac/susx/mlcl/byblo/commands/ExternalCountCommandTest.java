/*
 * Copyright (c) 2010-2013, University of Sussex
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

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.susx.mlcl.TestConstants;
import uk.ac.susx.mlcl.TestConstants.InfoProgressListener;
import uk.ac.susx.mlcl.byblo.Tools;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
import uk.ac.susx.mlcl.lib.commands.AbstractCommandTest;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import uk.ac.susx.mlcl.lib.test.ExitTrapper;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static uk.ac.susx.mlcl.lib.test.ExitTrapper.disableExitTrapping;
import static uk.ac.susx.mlcl.lib.test.ExitTrapper.enableExistTrapping;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ExternalCountCommandTest extends
        AbstractCommandTest<ExternalCountCommand> {

    @Override
    public Class<? extends ExternalCountCommand> getImplementation() {
        return ExternalCountCommand.class;
    }

    private static final String subject = ExternalCountCommand.class.getName();

    private void runWithAPI(File inInst, File outE, File outF, File outEF,
                            Charset charset, boolean preindexedEntries,
                            boolean preindexedFeatures)  {
        final ExternalCountCommand countCmd = new ExternalCountCommand();
        countCmd.setInstancesFile(inInst);
        countCmd.setEntriesFile(outE);
        countCmd.setFeaturesFile(outF);
        countCmd.setEventsFile(outEF);
        countCmd.getFileDelegate().setCharset(charset);
        countCmd.setIndexDelegate(new DoubleEnumeratingDelegate(
                Enumerating.DEFAULT_TYPE, preindexedEntries,
                preindexedFeatures, null, null));
        countCmd.setTempFileFactory(new TempFileFactory(TestConstants.TEST_TMP_DIR));

        boolean success = countCmd.runCommand();

        assertTrue("Output files not created: " + outE, outE.exists());
        assertTrue("Output files not created: " + outF, outF.exists());
        assertTrue("Output files not created: " + outEF, outEF.exists());

        assertTrue("Empty output file found: " + outE, outE.length() > 0);
        assertTrue("Empty output file found: " + outF, outF.length() > 0);
        assertTrue("Empty output file found: " + outEF, outEF.length() > 0);
    }

    private void runWithCLI(File inInst, File outE, File outF, File outEF,
                            Charset charset, boolean preindexedEntries,
                            boolean preindexedFeatures) throws Exception {

        String[] args = {"count", "--input", inInst.toString(),
                "--output-entries", outE.toString(), "--output-features",
                outF.toString(), "--output-entry-features", outEF.toString(),
                "--charset", charset.name(), "--temporary-directory",
                TestConstants.TEST_TMP_DIR.toString()};

        if (preindexedEntries) {
            List<String> tmp = new ArrayList<String>(Arrays.asList(args));
            tmp.add("--enumerated-entries");
            args = tmp.toArray(new String[tmp.size()]);
        }
        if (preindexedFeatures) {
            List<String> tmp = new ArrayList<String>(Arrays.asList(args));
            tmp.add("--enumerated-features");
            args = tmp.toArray(new String[tmp.size()]);
        }

        try {
            enableExistTrapping();
            Tools.main(args);
        } finally {
            disableExitTrapping();
        }

        assertTrue("Output files not created: " + outE, outE.exists());
        assertTrue("Output files not created: " + outF, outF.exists());
        assertTrue("Output files not created: " + outEF, outEF.exists());

        assertTrue("Empty output file found: " + outE, outE.length() > 0);
        assertTrue("Empty output file found: " + outF, outF.length() > 0);
        assertTrue("Empty output file found: " + outEF, outEF.length() > 0);
    }

    private void runExpectingNullPointer(File inInst, File outE, File outF,
                                         File outEF, Charset charset, boolean preindexedEntries,
                                         boolean preindexedFeatures) {
        try {
            runWithAPI(inInst, outE, outF, outEF, charset, preindexedEntries,
                    preindexedFeatures);
            fail("NullPointerException should have been thrown.");
        } catch (NullPointerException ex) {
            // pass
        }
    }

    private void runExpectingIllegalState(File inInst, File outE, File outF,
                                          File outEF, Charset charset, boolean preindexedEntries,
                                          boolean preindexedFeatures) {
        try {
            runWithAPI(inInst, outE, outF, outEF, charset, preindexedEntries, preindexedFeatures);
            fail("IllegalStateException should have been thrown.");
        } catch (IllegalStateException ex) {
            // pass
        }
    }

    @Test
    public void testRunOnFruitAPI() throws Exception {
        System.out.println("Testing " + subject + " on " + TestConstants.TEST_FRUIT_INPUT);

        final String fruitPrefix = TestConstants.TEST_FRUIT_INPUT.getName();
        final File eActual = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix + ".entries");
        final File fActual = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix
                + ".features");
        final File efActual = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix + ".events");

        TestConstants.deleteIfExist(eActual, fActual, efActual);

        runWithAPI(TestConstants.TEST_FRUIT_INPUT, eActual, fActual, efActual,
                TestConstants.DEFAULT_CHARSET, false, false);

    }

    @Test
    public void testRunOnFruitAPI_Indexed() throws Exception {
        System.out.println("Testing " + subject + " on "
                + TestConstants.TEST_FRUIT_INPUT_INDEXED);

        final String fruitPrefix = TestConstants.TEST_FRUIT_INPUT_INDEXED.getName();
        final File eActual = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix + ".entries");
        final File fActual = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix
                + ".features");
        final File efActual = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix + ".events");

        TestConstants.deleteIfExist(eActual, fActual, efActual);

        runWithAPI(TestConstants.TEST_FRUIT_INPUT_INDEXED, eActual, fActual, efActual,
                TestConstants.DEFAULT_CHARSET, true, true);

    }

    @Test
    public void testRunOnFruitAPITinyChunk() throws Exception {
        System.out.println("Testing " + subject + " on " + TestConstants.TEST_FRUIT_INPUT);

        final String fruitPrefix = TestConstants.TEST_FRUIT_INPUT.getName();
        final File eActual = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix + ".entries"
                + ".tiny");
        final File fActual = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix
                + ".features" + ".tiny");
        final File efActual = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix + ".events"
                + ".tiny");

        TestConstants.deleteIfExist(eActual, fActual, efActual);


        runWithAPI(TestConstants.TEST_FRUIT_INPUT, eActual, fActual, efActual,
                TestConstants.DEFAULT_CHARSET, false, false);

    }

    @Test
    public void testRunOnFruitAPITinyChunk_Indexed() throws Exception {
        System.out.println("Testing " + subject + " on "
                + TestConstants.TEST_FRUIT_INPUT_INDEXED);

        final String fruitPrefix = TestConstants.TEST_FRUIT_INPUT_INDEXED.getName();
        final File eActual = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix + ".entries"
                + ".tiny");
        final File fActual = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix
                + ".features" + ".tiny");
        final File efActual = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix + ".events"
                + ".tiny");

        TestConstants.deleteIfExist(eActual, fActual, efActual);

        runWithAPI(TestConstants.TEST_FRUIT_INPUT_INDEXED, eActual, fActual, efActual,
                TestConstants.DEFAULT_CHARSET, true, true);

    }

    @Test
    public void testRunOnFruitCLI() throws Exception {

        System.out.println("Testing " + subject + " on " + TestConstants.TEST_FRUIT_INPUT);

        final String fruitPrefix = TestConstants.TEST_FRUIT_INPUT.getName();
        final File eActual = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix + ".entries");
        final File fActual = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix
                + ".features");
        final File efActual = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix + ".events");

        TestConstants.deleteIfExist(eActual, fActual, efActual);

        runWithCLI(TestConstants.TEST_FRUIT_INPUT, eActual, fActual, efActual,
                TestConstants.DEFAULT_CHARSET, false, false);

    }

    @Test
    public void testRunOnFruitCLI_Indexed() throws Exception {

        System.out.println("Testing " + subject + " on "
                + TestConstants.TEST_FRUIT_INPUT_INDEXED);

        final String fruitPrefix = TestConstants.TEST_FRUIT_INPUT_INDEXED.getName();
        final File eActual = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix + ".entries");
        final File fActual = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix
                + ".features");
        final File efActual = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix + ".events");

        TestConstants.deleteIfExist(eActual, fActual, efActual);

        runWithCLI(TestConstants.TEST_FRUIT_INPUT_INDEXED, eActual, fActual, efActual,
                TestConstants.DEFAULT_CHARSET, true, true);

    }

    public void runReplacingParameters(int id, @Nullable File replacementParameter) {
        final File instIn = TestConstants.TEST_FRUIT_INPUT;
        final String fruitPrefix = TestConstants.TEST_FRUIT_INPUT.getName();
        final File eOut = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix + ".entries");
        final File fOut = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix + ".features");
        final File vOut = new File(TestConstants.TEST_OUTPUT_DIR, fruitPrefix + ".events");
        File[] files = new File[]{instIn, eOut, fOut, vOut};
        files[id] = replacementParameter;
        runWithAPI(files[0], files[1], files[2], files[3],
                TestConstants.DEFAULT_CHARSET, false, false);
    }

    @Test
    public void testExitStatus() throws Exception {
        try {
            ExitTrapper.enableExistTrapping();
            Tools.main(new String[]{"count"});
        } catch (ExitTrapper.ExitException ex) {
            assertTrue("Expecting non-zero exit status.", ex.getStatus() != 0);
        } finally {
            ExitTrapper.disableExitTrapping();
        }
    }

    /**
     * Tests ExternalCountCommand on a large amount of data that has been
     * generated to conform to worst-case memory usage scenario.
     *
     * @throws Exception
     */
    @Test
    @Ignore(value = "Takes a rather a long time.")
    public void testExternalCountCommandOnLargeData() throws Exception {
        System.out.println("testExternalCountCommand()");

        final int nEntries = 1 << 12;
        final int nFeaturesPerEntry = 1 << 12;
        final File instancesFile = new File(TestConstants.TEST_OUTPUT_DIR, String.format(
                "testExternalCountCommand-%dx%d-instances", nEntries,
                nFeaturesPerEntry));
        final File entriesFile = new File(TestConstants.TEST_OUTPUT_DIR,
                instancesFile.getName() + "-entries");
        final File featuresFile = new File(TestConstants.TEST_OUTPUT_DIR,
                instancesFile.getName() + "-features");
        final File eventsFile = new File(TestConstants.TEST_OUTPUT_DIR,
                instancesFile.getName() + "-events");

        // Create the test data if necessary
        if (!instancesFile.exists())
            TestConstants.generateUniqueInstanceData(instancesFile, nEntries,
                    nFeaturesPerEntry);

        TestConstants.assertValidPlaintextInputFiles(instancesFile);
        TestConstants.assertValidOutputFiles(entriesFile, featuresFile, eventsFile);

        TestConstants.deleteIfExist(entriesFile, featuresFile, eventsFile);

        // Instantiate and configure the command
        final ExternalCountCommand countCmd = new ExternalCountCommand();
        countCmd.setInstancesFile(instancesFile);
        countCmd.setEntriesFile(entriesFile);
        countCmd.setFeaturesFile(featuresFile);
        countCmd.setEventsFile(eventsFile);
        countCmd.getFileDelegate().setCharset(TestConstants.DEFAULT_CHARSET);
        countCmd.setIndexDelegate(new DoubleEnumeratingDelegate(
                Enumerating.DEFAULT_TYPE, true, true, null, null));
        countCmd.setTempFileFactory(new TempFileFactory(TestConstants.TEST_TMP_DIR));

        // The highest number of cores attempted so far.
        // countCmd.setNumThreads(64);

        countCmd.addProgressListener(new InfoProgressListener());

        countCmd.runCommand();

        TestConstants.assertValidPlaintextInputFiles(entriesFile, featuresFile, eventsFile);

    }

}
