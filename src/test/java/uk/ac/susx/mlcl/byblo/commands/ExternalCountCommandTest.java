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

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.ac.susx.mlcl.TestConstants;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.byblo.Tools;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import uk.ac.susx.mlcl.lib.test.ExitTrapper;
import static uk.ac.susx.mlcl.lib.test.ExitTrapper.*;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ExternalCountCommandTest {

    private static final String subject = ExternalCountCommand.class.getName();

    private void runWithAPI(
            File inInst, File outE, File outF,
            File outEF, Charset charset, int chunkSize,
            boolean preindexedEntries, boolean preindexedFeatures)
            throws Exception {
        final ExternalCountCommand countCmd = new ExternalCountCommand();
        countCmd.setInstancesFile(inInst);
        countCmd.setEntriesFile(outE);
        countCmd.setFeaturesFile(outF);
        countCmd.setEventsFile(outEF);
        countCmd.getFileDeligate().setCharset(charset);
        countCmd.setMaxChunkSize(chunkSize);
        countCmd.setIndexDeligate(new DoubleEnumeratingDeligate(
                Enumerating.DEFAULT_TYPE, preindexedEntries, preindexedFeatures,
                null, null));
        countCmd.setTempFileFactory(new TempFileFactory(TEST_TMP_DIR));

        countCmd.runCommand();

        assertTrue("Output files not created: " + outE, outE.exists());
        assertTrue("Output files not created: " + outF, outF.exists());
        assertTrue("Output files not created: " + outEF, outEF.exists());

        assertTrue("Empty output file found: " + outE, outE.length() > 0);
        assertTrue("Empty output file found: " + outF, outF.length() > 0);
        assertTrue("Empty output file found: " + outEF, outEF.length() > 0);
    }

    private void runwithCLI(
            File inInst, File outE, File outF,
            File outEF, Charset charset, int chunkSize,
            boolean preindexedEntries, boolean preindexedFeatures)
            throws Exception {

        String[] args = {
            "count",
            "--input", inInst.toString(),
            "--output-entries", outE.toString(),
            "--output-features", outF.toString(),
            "--output-entry-features", outEF.toString(),
            "--charset", charset.name(),
            "--chunk-size", "" + chunkSize,
            "--temporary-directory", TEST_TMP_DIR.toString()
        };

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

    @Test
    public void testRunOnFruitAPI() throws Exception {
        System.out.println("Testing " + subject + " on " + TEST_FRUIT_INPUT);

        final String fruitPrefix = TEST_FRUIT_INPUT.getName();
        final File eActual = new File(TEST_OUTPUT_DIR, fruitPrefix + ".entries");
        final File fActual =
                new File(TEST_OUTPUT_DIR, fruitPrefix + ".features");
        final File efActual = new File(TEST_OUTPUT_DIR,
                                       fruitPrefix + ".events");

        TestConstants.deleteIfExist(eActual, fActual, efActual);

        runWithAPI(TEST_FRUIT_INPUT, eActual, fActual, efActual,
                   DEFAULT_CHARSET, 1000000, false, false);

//        assertTrue("Output entries file differs from sampledata file.",
//                   WeightedTokenSource.equal(eActual, TEST_FRUIT_ENTRIES,
//                                             DEFAULT_CHARSET, true, false));
//        assertTrue("Output features file differs from test data file.",
//                   WeightedTokenSource.equal(fActual, TEST_FRUIT_FEATURES,
//                                             DEFAULT_CHARSET, true, false));
//        assertTrue("Output entry/features file differs from test data file.",
//                   TokenPairSource.equal(efActual, TEST_FRUIT_ENTRY_FEATURES,
//                                         DEFAULT_CHARSET, true, true));
    }

    @Test
    public void testRunOnFruitAPI_Indexed() throws Exception {
        System.out.println(
                "Testing " + subject + " on " + TEST_FRUIT_INPUT_INDEXED);

        final String fruitPrefix = TEST_FRUIT_INPUT_INDEXED.getName();
        final File eActual = new File(TEST_OUTPUT_DIR, fruitPrefix + ".entries");
        final File fActual =
                new File(TEST_OUTPUT_DIR, fruitPrefix + ".features");
        final File efActual = new File(TEST_OUTPUT_DIR,
                                       fruitPrefix + ".events");

        TestConstants.deleteIfExist(eActual, fActual, efActual);

        runWithAPI(TEST_FRUIT_INPUT_INDEXED, eActual, fActual, efActual,
                   DEFAULT_CHARSET, 1000000, true, true);

//        assertTrue("Output entries file differs from sampledata file.",
//                   WeightedTokenSource.equal(eActual, TEST_FRUIT_INDEXED_ENTRIES,
//                                             DEFAULT_CHARSET, true, false));
//        assertTrue("Output features file differs from test data file.",
//                   WeightedTokenSource.equal(fActual, TEST_FRUIT_INDEXED_FEATURES,
//                                             DEFAULT_CHARSET, true, false));
//        assertTrue("Output entry/features file differs from test data file.",
//                   TokenPairSource.equal(efActual, TEST_FRUIT_INDEXED_ENTRY_FEATURES,
//                                         DEFAULT_CHARSET, true, true));
    }

    @Test
    public void testRunOnFruitAPITinyChunk() throws Exception {
        System.out.println("Testing " + subject + " on " + TEST_FRUIT_INPUT);

        final String fruitPrefix = TEST_FRUIT_INPUT.getName();
        final File eActual = new File(TEST_OUTPUT_DIR,
                                      fruitPrefix + ".entries" + ".tiny");
        final File fActual = new File(TEST_OUTPUT_DIR,
                                      fruitPrefix + ".features" + ".tiny");
        final File efActual = new File(TEST_OUTPUT_DIR,
                                       fruitPrefix + ".events" + ".tiny");

        TestConstants.deleteIfExist(eActual, fActual, efActual);

        runWithAPI(TEST_FRUIT_INPUT, eActual, fActual, efActual,
                   DEFAULT_CHARSET, 30000, false, false);

//        assertTrue("Output entries file differs from sampledata file.",
//                   WeightedTokenSource.equal(eActual, TEST_FRUIT_ENTRIES,
//                                             DEFAULT_CHARSET, true, false));
//        assertTrue("Output features file differs from test data file.",
//                   WeightedTokenSource.equal(fActual, TEST_FRUIT_FEATURES,
//                                             DEFAULT_CHARSET, true, false));
//        assertTrue("Output entry/features file differs from test data file.",
//                   TokenPairSource.equal(efActual, TEST_FRUIT_ENTRY_FEATURES,
//                                         DEFAULT_CHARSET, true, true));
    }

    @Test
    public void testRunOnFruitAPITinyChunk_Indexed() throws Exception {
        System.out.println(
                "Testing " + subject + " on " + TEST_FRUIT_INPUT_INDEXED);

        final String fruitPrefix = TEST_FRUIT_INPUT_INDEXED.getName();
        final File eActual = new File(TEST_OUTPUT_DIR,
                                      fruitPrefix + ".entries" + ".tiny");
        final File fActual = new File(TEST_OUTPUT_DIR,
                                      fruitPrefix + ".features" + ".tiny");
        final File efActual = new File(TEST_OUTPUT_DIR,
                                       fruitPrefix + ".events" + ".tiny");

        TestConstants.deleteIfExist(eActual, fActual, efActual);


        runWithAPI(TEST_FRUIT_INPUT_INDEXED, eActual, fActual, efActual,
                   DEFAULT_CHARSET, 30000, true, true);

//        assertTrue("Output entries file differs from sampledata file.",
//                   WeightedTokenSource.equal(eActual, TEST_FRUIT_INDEXED_ENTRIES,
//                                             DEFAULT_CHARSET, true, false));
//        assertTrue("Output features file differs from test data file.",
//                   WeightedTokenSource.equal(fActual, TEST_FRUIT_INDEXED_FEATURES,
//                                             DEFAULT_CHARSET, true, false));
//        assertTrue("Output entry/features file differs from test data file.",
//                   TokenPairSource.equal(efActual, TEST_FRUIT_INDEXED_ENTRY_FEATURES,
//                                         DEFAULT_CHARSET, true, true));
    }

    @Test
    public void testRunOnFruitCLI() throws Exception {

        System.out.println("Testing " + subject + " on " + TEST_FRUIT_INPUT);

        final String fruitPrefix = TEST_FRUIT_INPUT.getName();
        final File eActual = new File(TEST_OUTPUT_DIR, fruitPrefix + ".entries");
        final File fActual =
                new File(TEST_OUTPUT_DIR, fruitPrefix + ".features");
        final File efActual = new File(TEST_OUTPUT_DIR,
                                       fruitPrefix + ".events");

        TestConstants.deleteIfExist(eActual, fActual, efActual);


        runwithCLI(TEST_FRUIT_INPUT, eActual, fActual, efActual,
                   DEFAULT_CHARSET, 1000000, false, false);


//        assertTrue("Output entries file differs from sampledata file.",
//                   WeightedTokenSource.equal(eActual, TEST_FRUIT_ENTRIES,
//                                             DEFAULT_CHARSET, true, false));
//        assertTrue("Output features file differs from test data file.",
//                   WeightedTokenSource.equal(fActual, TEST_FRUIT_FEATURES,
//                                             DEFAULT_CHARSET, true, false));
//        assertTrue("Output entry/features file differs from test data file.",
//                   TokenPairSource.equal(efActual, TEST_FRUIT_ENTRY_FEATURES,
//                                         DEFAULT_CHARSET, true, true));
    }

    @Test
    public void testRunOnFruitCLI_Indexed() throws Exception {

        System.out.println(
                "Testing " + subject + " on " + TEST_FRUIT_INPUT_INDEXED);

        final String fruitPrefix = TEST_FRUIT_INPUT_INDEXED.getName();
        final File eActual = new File(TEST_OUTPUT_DIR, fruitPrefix + ".entries");
        final File fActual =
                new File(TEST_OUTPUT_DIR, fruitPrefix + ".features");
        final File efActual = new File(TEST_OUTPUT_DIR,
                                       fruitPrefix + ".events");

        TestConstants.deleteIfExist(eActual, fActual, efActual);

        runwithCLI(TEST_FRUIT_INPUT_INDEXED, eActual, fActual, efActual,
                   DEFAULT_CHARSET, 1000000, true, true);

    }

    @Test(expected = NullPointerException.class)
    public void testNullParameter1() throws Exception {
        runReplacingParameters(0, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullParameter2() throws Exception {
        runReplacingParameters(1, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullParameter3() throws Exception {
        runReplacingParameters(2, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullParameter4() throws Exception {
        runReplacingParameters(3, null);
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalState1() throws Exception {
        runReplacingParameters(0, TEST_OUTPUT_DIR);
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalState2() throws Exception {
        runReplacingParameters(1, TEST_OUTPUT_DIR);
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalState3() throws Exception {
        runReplacingParameters(2, TEST_OUTPUT_DIR);
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalState4() throws Exception {
        runReplacingParameters(3, TEST_OUTPUT_DIR);
    }

    public void runReplacingParameters(int id, @Nullable File repl) throws Exception {
        final File instIn = TEST_FRUIT_INPUT;
        final String fruitPrefix = TEST_FRUIT_INPUT.getName();
        final File eOut = new File(TEST_OUTPUT_DIR, fruitPrefix + ".entries");
        final File fOut = new File(TEST_OUTPUT_DIR, fruitPrefix + ".features");
        final File vOut = new File(TEST_OUTPUT_DIR, fruitPrefix + ".events");
        File[] files = new File[]{instIn, eOut, fOut, vOut};
        files[id] = repl;
        runWithAPI(files[0], files[1], files[2], files[3],
                   DEFAULT_CHARSET, 10000, false, false);
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
}
