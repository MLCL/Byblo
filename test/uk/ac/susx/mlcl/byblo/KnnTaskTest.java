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

import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.lib.tasks.ReverseComparator;

/**
 *
 * @author Hamish Morgan &ly;hamish.morgan@sussex.ac.uk&gt;
 */
public class KnnTaskTest {

    private static final String subject = ExternalKnnTask.class.getName();

    @Test(timeout = 8000)
    public void testRunOnFruit() throws Exception {
        System.out.println("Testing " + subject + " on " + TEST_FRUIT_INPUT);

        final File in = TEST_FRUIT_SIMS;
        final File out = new File(TEST_OUTPUT_DIR,
                                  FRUIT_NAME + ".neighs");

        final KnnTask knnTask = new KnnTask();
        knnTask.setSrcFile(in);
        knnTask.setDstFile(out);
        knnTask.setCharset(DEFAULT_CHARSET);
        knnTask.setK(100);
        knnTask.setPreindexedTokens1(false);
        knnTask.setPreindexedTokens2(false);
        knnTask.setClassComparator(Weighted.recordOrder(TokenPair.
                firstIndexOrder()));
        knnTask.setNearnessComparator(
                new ReverseComparator<Weighted<TokenPair>>(
                Weighted.<TokenPair>weightOrder()));

        knnTask.run();

        while (knnTask.isExceptionThrown()) {
            knnTask.throwException();
        }

        assertTrue("Output files not created.", out.exists());
        assertTrue("Empty output file found.", out.length() > 0);
    }

    @Test(timeout = 8000)
    public void testRunOnFruit_Indexed() throws Exception {
        System.out.println(
                "Testing " + subject + " on " + TEST_FRUIT_INPUT_INDEXED);

        final File in = TEST_FRUIT_INDEXED_SIMS;
        final File out = new File(TEST_OUTPUT_DIR,
                                  FRUIT_NAME + ".indexed.neighs");

        final KnnTask knnTask = new KnnTask();
        knnTask.setSrcFile(in);
        knnTask.setDstFile(out);
        knnTask.setCharset(DEFAULT_CHARSET);
        knnTask.setK(100);
        knnTask.setPreindexedTokens1(true);
        knnTask.setPreindexedTokens2(true);
        knnTask.setClassComparator(Weighted.recordOrder(TokenPair.
                firstIndexOrder()));
        knnTask.setNearnessComparator(
                new ReverseComparator<Weighted<TokenPair>>(
                Weighted.<TokenPair>weightOrder()));

        knnTask.run();

        while (knnTask.isExceptionThrown()) {
            knnTask.throwException();
        }

        assertTrue("Output files not created.", out.exists());
        assertTrue("Empty output file found.", out.length() > 0);
    }
}
