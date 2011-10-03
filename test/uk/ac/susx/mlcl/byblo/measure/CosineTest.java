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
package uk.ac.susx.mlcl.byblo.measure;

import com.google.common.io.Files;
import uk.ac.susx.mlcl.byblo.Byblo;
import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;
import static uk.ac.susx.mlcl.TestConstants.*;
import static uk.ac.susx.mlcl.ExitTrapper.*;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class CosineTest {

    @Test
    public void testCosineCli() throws Exception {
        System.out.println("Testing Cosine from main method.");

        File outputFile = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".Cosine");
        outputFile.delete();

        enableExistTrapping();
        Byblo.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "Cosine",
                    "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
                    "--input-features", TEST_FRUIT_FEATURES.toString(),
                    "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                    "--output", outputFile.toString()
                });
        disableExitTrapping();

        assertTrue("Output file " + outputFile + " does not exist.", outputFile.
                exists());
        assertTrue("Output file " + outputFile + " is empty.",
                outputFile.length() > 0);
    }

    @Test(timeout = 1000)
    public void testCosineMiCli() throws Exception {
        System.out.println("Testing CosineMi from main method.");

        File outputFile = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".CosineMi");
        outputFile.delete();

        enableExistTrapping();
        Byblo.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "CosineMi",
                    "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
                    "--input-features", TEST_FRUIT_FEATURES.toString(),
                    "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                    "--output", outputFile.toString()
                });
        disableExitTrapping();

        assertTrue("Output file " + outputFile + " does not exist.", outputFile.
                exists());
        assertTrue("Output file " + outputFile + " is empty.",
                outputFile.length() > 0);
    }

    @Test(timeout = 1000)
    public void testCosineSymmetry() throws Exception {
        System.out.println("Testing Cosine symmetry.");

        File output1 = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".Cosine-1");
        File output2 = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".Cosine-2");
        output1.delete();
        output2.delete();


        enableExistTrapping();
        Byblo.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "Cosine",
                    "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
                    "--input-features", TEST_FRUIT_FEATURES.toString(),
                    "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                    "--output", output1.toString()
                });
        Byblo.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "Cosine",
                    "--measure-reversed",
                    "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
                    "--input-features", TEST_FRUIT_FEATURES.toString(),
                    "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                    "--output", output2.toString()
                });
        disableExitTrapping();

        assertTrue(Files.equal(output1, output2));

        Thread.sleep(100);
    }

    @Test(timeout = 1000)
    public void test_CosineMi_Symmetry() throws Exception {
        System.out.println("Testing CosineMi symmetry.");

        File output1 = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".CosineMi-1");
        File output2 = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".CosineMi-2");
        output1.delete();
        output2.delete();

        enableExistTrapping();
        Byblo.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "CosineMi",
                    "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
                    "--input-features", TEST_FRUIT_FEATURES.toString(),
                    "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                    "--output", output1.toString()
                });
        Byblo.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "CosineMi",
                    "--measure-reversed",
                    "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
                    "--input-features", TEST_FRUIT_FEATURES.toString(),
                    "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                    "--output", output2.toString()
                });

        assertTrue(Files.equal(output1, output2));
        disableExitTrapping();
    }
}
