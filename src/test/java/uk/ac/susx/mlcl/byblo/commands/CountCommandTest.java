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
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
import static uk.ac.susx.mlcl.lib.test.ExitTrapper.*;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class CountCommandTest {

    private static final String subject = CountCommand.class.getName();

    private void runWithAPI(File inInst, File outE, File outF, File outEF,
                            Charset charset, boolean preIndexEntries,
                            boolean preIndexFeatures)
            throws Exception {
        final CountCommand countTask = new CountCommand();
        countTask.setInstancesFile(inInst);
        countTask.setEntriesFile(outE);
        countTask.setFeaturesFile(outF);
        countTask.setEventsFile(outEF);
        countTask.setCharset(charset);
        DoubleEnumeratingDeligate idx = new DoubleEnumeratingDeligate(
                Enumerating.DEFAULT_TYPE, preIndexEntries, preIndexFeatures,
                null, null);
        countTask.setIndexDeligate(idx);
        countTask.runCommand();

        assertTrue("Output files not created: " + outE, outE.exists());
        assertTrue("Output files not created: " + outF, outF.exists());
        assertTrue("Output files not created: " + outEF, outEF.exists());

        assertTrue("Empty output file found: " + outE, outE.length() > 0);
        assertTrue("Empty output file found: " + outF, outF.length() > 0);
        assertTrue("Empty output file found: " + outEF, outEF.length() > 0);
    }

    private void runWithCLI(
            File inInst, File outE, File outF, File outEF, Charset charset,
            boolean preindexedEntries, boolean preindexedFeatures)
            throws Exception {

        String[] args = {
            "--input", inInst.toString(),
            "--output-entries", outE.toString(),
            "--output-features", outF.toString(),
            "--output-entry-features", outEF.toString(),
            "--charset", charset.name()
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
            CountCommand.main(args);
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
                   DEFAULT_CHARSET, false, false);

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

        runWithCLI(TEST_FRUIT_INPUT, eActual, fActual, efActual,
                   DEFAULT_CHARSET, false, false);

    }

    @Test
    public void testRunOnFruitAPI_Indexed() throws Exception {
        System.out.
                println(
                "Testing " + subject + " on " + TEST_FRUIT_INPUT_INDEXED
                + " (Indexed)");

        final String fruitPrefix = TEST_FRUIT_INPUT_INDEXED.getName();
        final File eActual = new File(TEST_OUTPUT_DIR, fruitPrefix + ".entries");
        final File fActual =
                new File(TEST_OUTPUT_DIR, fruitPrefix + ".features");
        final File efActual = new File(TEST_OUTPUT_DIR,
                                       fruitPrefix + ".events");

        TestConstants.deleteIfExist(eActual, fActual, efActual);

        runWithAPI(TEST_FRUIT_INPUT_INDEXED, eActual, fActual, efActual,
                   DEFAULT_CHARSET, true, true);

    }

    @Test
    public void testRunOnFruitCLI_Indexed() throws Exception {
        System.out.
                println(
                "Testing " + subject + " on " + TEST_FRUIT_INPUT_INDEXED
                + " (Indexed)");

        final String fruitPrefix = TEST_FRUIT_INPUT_INDEXED.getName();
        final File eActual = new File(TEST_OUTPUT_DIR, fruitPrefix + ".entries");
        final File fActual =
                new File(TEST_OUTPUT_DIR, fruitPrefix + ".features");
        final File efActual = new File(TEST_OUTPUT_DIR,
                                       fruitPrefix + ".events");

        TestConstants.deleteIfExist(eActual, fActual, efActual);

        runWithCLI(TEST_FRUIT_INPUT_INDEXED, eActual, fActual, efActual,
                   DEFAULT_CHARSET, true, true);

    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalState1() throws Exception {
        runReplacingFile(0, TEST_OUTPUT_DIR);
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalState2() throws Exception {
        runReplacingFile(1, TEST_OUTPUT_DIR);
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalState3() throws Exception {
        runReplacingFile(2, TEST_OUTPUT_DIR);
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalState4() throws Exception {
        runReplacingFile(3, TEST_OUTPUT_DIR);
    }

    @Test(expected = NullPointerException.class)
    public void testNullParameter1() throws Exception {
        runReplacingFile(0, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullParameter2() throws Exception {
        runReplacingFile(1, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullParameter3() throws Exception {
        runReplacingFile(2, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullParameter4() throws Exception {
        runReplacingFile(3, null);
    }

    void runReplacingFile(int repId, @Nullable File repFile)
            throws Exception {
        final File instIn = TEST_FRUIT_INPUT;
        final String fruitPrefix = TEST_FRUIT_INPUT.getName();
        final File eOut = new File(TEST_OUTPUT_DIR, fruitPrefix + ".entries");
        final File fOut = new File(TEST_OUTPUT_DIR, fruitPrefix + ".features");
        final File efOut = new File(TEST_OUTPUT_DIR, fruitPrefix + ".events");
        File[] files = new File[]{instIn, eOut, fOut, efOut};
        files[repId] = repFile;
        runWithAPI(files[0], files[1], files[2], files[3], DEFAULT_CHARSET,
                   false, false);
    }
}
