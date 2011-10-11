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

import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;
import static uk.ac.susx.mlcl.TestConstants.*;

/**
 *
 * @author Hamish Morgan &ly;hamish.morgan@sussex.ac.uk&gt;
 */
public class ExternalKnnTaskTest {

    private static final String subject = ExternalCountTask.class.getName();

    @Test(timeout=2000)
    public void testRunOnFruit() throws Exception {
        System.out.println("Testing " + subject + " on " + TEST_FRUIT_INPUT);

        final File in = TEST_FRUIT_SIMS;
        final File out = new File(TEST_OUTPUT_DIR,
                FRUIT_NAME + ".neighs");

        final ExternalKnnTask knnTask = new ExternalKnnTask(
                in, out, DEFAULT_CHARSET, 2);
        knnTask.setMaxChunkSize(1000);
        knnTask.run();

        while (knnTask.isExceptionThrown()) {
            knnTask.throwException();
        }

        assertTrue("Output files not created.", out.exists());
        assertTrue("Empty output file found.", out.length() > 0);
    }
}
