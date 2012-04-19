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

import uk.ac.susx.mlcl.lib.test.ExitTrapper;
import java.io.File;
import org.junit.Test;
import uk.ac.susx.mlcl.TestConstants;
import static org.junit.Assert.*;
import org.junit.Ignore;
import uk.ac.susx.mlcl.byblo.Main;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;

/**
 *
 * @author Hamish Morgan &ly;hamish.morgan@sussex.ac.uk&gt;
 */
public class ExternalSimsKnnCommandTest {

    private static final String subject = ExternalKnnSimsCommand.class.getName();

    @Test//(timeout = 2000)
    public void testRunOnFruit() throws Exception {
        System.out.println("Testing " + subject + " on " + TEST_FRUIT_SIMS);

        final File in = TEST_FRUIT_SIMS;
        final File out = new File(TEST_OUTPUT_DIR,
                                  FRUIT_NAME + ".neighs");

        assertTrue(in.exists());
        assertTrue(in.length() > 0);

        final ExternalKnnSimsCommand knnCmd = new ExternalKnnSimsCommand();

        knnCmd.getFileDeligate().setSourceFile(in);
        knnCmd.getFileDeligate().setDestinationFile(out);
        knnCmd.getFileDeligate().setCharset(DEFAULT_CHARSET);

        knnCmd.setIndexDeligate(new DoubleEnumeratingDeligate(
                Enumerating.DEFAULT_TYPE, false, false, null, null));

        knnCmd.setMaxChunkSize(100000);
        knnCmd.setClassComparator(Weighted.recordOrder(TokenPair.firstIndexOrder()));
        knnCmd.setNearnessComparator(Comparators.reverse(Weighted.<TokenPair>weightOrder()));
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
            Main.main(new String[]{"knn"});
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
            File in = new File(TestConstants.TEST_OUTPUT_DIR, "extknn-test-empty.in");
            in.createNewFile();
            File out = new File(TestConstants.TEST_OUTPUT_DIR, "extknn-test-empty.out");

            ExitTrapper.enableExistTrapping();
            Main.main(new String[]{"knn", "-i", in.toString(),
                        "-o", out.toString()});
        } catch (ExitTrapper.ExitException ex) {
            assertTrue("Expecting non-zero exit status.", ex.getStatus() == 0);
        } finally {
            ExitTrapper.disableExitTrapping();
        }
    }

}
