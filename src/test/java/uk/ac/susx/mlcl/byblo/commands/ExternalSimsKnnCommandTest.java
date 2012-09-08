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

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import uk.ac.susx.mlcl.TestConstants;
import uk.ac.susx.mlcl.byblo.Tools;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.io.BybloIO;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSink;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import uk.ac.susx.mlcl.lib.test.ExitTrapper;
import uk.ac.susx.mlcl.testing.SlowTestCategory;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertTrue;
import static uk.ac.susx.mlcl.TestConstants.*;

/**
 * @author Hamish Morgan &ly;hamish.morgan@sussex.ac.uk&gt;
 */
public class ExternalSimsKnnCommandTest extends
        AbstractCommandTest<ExternalKnnSimsCommand> {

    @Override
    public Class<? extends ExternalKnnSimsCommand> getImplementation() {
        return ExternalKnnSimsCommand.class;
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private static final String subject = ExternalKnnSimsCommand.class
            .getName();

    @Test
    public void testRunOnFruit() throws Exception {
        System.out.println("Testing " + subject + " on " + TEST_FRUIT_SIMS);

        final File in = TEST_FRUIT_SIMS;
        final File out = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".neighs");

        assertTrue(in.exists());
        assertTrue(in.length() > 0);

        final ExternalKnnSimsCommand knnCmd = new ExternalKnnSimsCommand();

        knnCmd.getFileDelegate().setSourceFile(in);
        knnCmd.getFileDelegate().setDestinationFile(out);
        knnCmd.getFileDelegate().setCharset(DEFAULT_CHARSET);

        knnCmd.setIndexDelegate(new DoubleEnumeratingDelegate(
                Enumerating.DEFAULT_TYPE, false, false, null, null));

        knnCmd.setClassComparator(Weighted.recordOrder(TokenPair
                .firstIndexOrder()));
        knnCmd.setNearnessComparator(Comparators.reverse(Weighted
                .<TokenPair>weightOrder()));
        knnCmd.setK(100);

        knnCmd.setTempFileFactory(new TempFileFactory(TEST_TMP_DIR));

        knnCmd.runCommand();

        assertTrue("Output files not created.", out.exists());
        assertTrue("Empty output file found.", out.length() > 0);
    }

    @Test
    public void testExitStatus() throws Exception {
        try {
            ExitTrapper.enableExistTrapping();
            Tools.main(new String[]{"knn"});
        } catch (ExitTrapper.ExitException ex) {
            assertTrue("Expecting non-zero exit status.", ex.getStatus() != 0);
        } finally {
            ExitTrapper.disableExitTrapping();
        }
    }

    @Test
    @Ignore
    public void testEmptyInputFile() throws Exception {
        try {
            File in = new File(TestConstants.TEST_OUTPUT_DIR,
                    "extknn-test-empty.in");
            in.createNewFile();
            File out = new File(TestConstants.TEST_OUTPUT_DIR,
                    "extknn-test-empty.out");

            ExitTrapper.enableExistTrapping();
            Tools.main(new String[]{"knn", "-i", in.toString(), "-o",
                    out.toString()});
        } catch (ExitTrapper.ExitException ex) {
            assertTrue("Expecting non-zero exit status.", ex.getStatus() == 0);
        } finally {
            ExitTrapper.disableExitTrapping();
        }
    }


    @Test
    @Category(SlowTestCategory.class)
    public void testRunOnGeneratedData() throws Exception {
        System.out.println("Testing " + subject + " on " + TEST_FRUIT_SIMS);

        final int nEntries = 4000;
        final File inSimsFile = new File(TEST_OUTPUT_DIR, String.format(
                "%s-%dx%d-sims", testName.getMethodName(), nEntries, nEntries));
        final File outNeighsFile = new File(TEST_OUTPUT_DIR, inSimsFile.getName() + "-neighbours");

        if(!inSimsFile.exists()) {
            generateSimsData(inSimsFile, nEntries);
        }

        if(outNeighsFile.exists()) {
            Assert.assertTrue(outNeighsFile.delete());
        }

        assertTrue(inSimsFile.exists());
        assertTrue(inSimsFile.length() > 0);

        final ExternalKnnSimsCommand knnCmd = new ExternalKnnSimsCommand();

        knnCmd.getFileDelegate().setSourceFile(inSimsFile);
        knnCmd.getFileDelegate().setDestinationFile(outNeighsFile);
        knnCmd.getFileDelegate().setCharset(DEFAULT_CHARSET);

        knnCmd.setIndexDelegate(new DoubleEnumeratingDelegate(
                Enumerating.DEFAULT_TYPE, false, false, null, null));

        knnCmd.setClassComparator(Weighted.recordOrder(TokenPair
                .firstIndexOrder()));
        knnCmd.setNearnessComparator(Comparators.reverse(Weighted
                .<TokenPair>weightOrder()));
        knnCmd.setK(100);
        knnCmd.setTempFileFactory(new TempFileFactory(TEST_TMP_DIR));
        knnCmd.addProgressListener(new InfoProgressListener());

        knnCmd.runCommand();


        assertTrue("Output files not created.", outNeighsFile.exists());
        assertTrue("Empty output file found.", outNeighsFile.length() > 0);
    }

    /**
     * Routine that creates a large amount of data, that should be the absolute
     * worst case for counting stage of the pipeline. That is data where entries
     * and features only ever appear once, and consequently events also are
     * unique. This causes the counting maps to be at the upper bound of their
     * potential size.
     *
     * @throws java.io.IOException
     */
    public static void generateSimsData(
            final File outFile, final int nEntries) throws IOException {

        System.out.printf("Generating sims data for KnnTask (nEntries=%d)...%n", nEntries);

        WeightedTokenPairSink sink = null;
        Random random = newRandom();
        try {
            final SingleEnumeratingDelegate ded = new SingleEnumeratingDelegate(
                    Enumerating.DEFAULT_TYPE, true, null);

            sink = BybloIO.openSimsSink(outFile, DEFAULT_CHARSET, ded);

            for (int entryId1 = 0; entryId1 < nEntries; entryId1++) {
                for (int entryId2 = 0; entryId2 < nEntries; entryId2++) {

                    sink.write(new Weighted<TokenPair>(new TokenPair(entryId1, entryId2),
                            1 - 1 / Math.abs(1 + random.nextGaussian())));

                    if (entryId1 * nEntries + entryId2 % 5000000 == 0
                            || (entryId1 == nEntries - 1 && entryId2 == nEntries - 1)) {
                        System.out.printf("> generated %d of %d events (%.2f%% complete)%n",
                                entryId1 * nEntries + entryId2, nEntries * nEntries,
                                (100.0d * entryId1 * nEntries + entryId2) / (nEntries * nEntries));
                    }
                }
            }
        } finally {
            if (sink != null)
                sink.close();
        }

        System.out.println("Generation completed.");
    }
}
