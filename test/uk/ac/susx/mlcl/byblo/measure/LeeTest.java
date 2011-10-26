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

import org.junit.Ignore;
import uk.ac.susx.mlcl.byblo.Main;
import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;
import static uk.ac.susx.mlcl.TestConstants.*;
import static uk.ac.susx.mlcl.ExitTrapper.*;

/**
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class LeeTest {

    @Test(timeout=1000)
    public void testLee_alpha_0_00() throws Exception {
        System.out.println("Testing Lee (alpha=0.00) from main method.");

        File output = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".Lee-alpha-0_00");
        output.delete();

        enableExistTrapping();
        Main.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "Lee",
                    "--lee-alpha", "0.00",
                    "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
                    "--input-features", TEST_FRUIT_FEATURES.toString(),
                    "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                    "--output", output.toString()
                });
        disableExitTrapping();
        assertTrue("Output file " + output + " does not exist.", output.exists());
        assertTrue("Output file " + output + " is empty.", output.length() > 0);
    }

    @Test(timeout=1000)
    public void testMainMethodRun_alpha_0_01() throws Exception {
        System.out.println("Testing Lee (alpha=0.01) from main method.");

        File output = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".Lee-alpha-0_01");
        output.delete();


        enableExistTrapping();
        Main.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "Lee",
                    "--lee-alpha", "0.01",
                    "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
                    "--input-features", TEST_FRUIT_FEATURES.toString(),
                    "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                    "--output", output.toString()
                });
        disableExitTrapping();

        assertTrue("Output file " + output + " does not exist.", output.exists());
        assertTrue("Output file " + output + " is empty.", output.length() > 0);

    }

    @Test(timeout=1000)
    public void testMainMethodRun_alpha_0_50() throws Exception {
        System.out.println("Testing Lee (alpha=0.50) from main method.");

        final String dataSet = "bnc-gramrels-fruit";

        File output = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".Lee-alpha-0_50");
        output.delete();

        enableExistTrapping();
        Main.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "Lee",
                    "--lee-alpha", "0.50",
                    "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
                    "--input-features", TEST_FRUIT_FEATURES.toString(),
                    "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                    "--output", output.toString()
                });
        disableExitTrapping();

        assertTrue("Output file " + output + " does not exist.", output.exists());
        assertTrue("Output file " + output + " is empty.", output.length() > 0);
    }

    @Test(timeout=1000)
    public void testMainMethodRun_alpha_0_99() throws Exception {
        System.out.println("Testing Lee (alpha=0.99) from main method.");
        File output = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".Lee-alpha-0_99");
        output.delete();

        enableExistTrapping();
        Main.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "Lee",
                    "--lee-alpha", "0.99",
                    "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
                    "--input-features", TEST_FRUIT_FEATURES.toString(),
                    "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                    "--output", output.toString()
                });
        disableExitTrapping();

        assertTrue("Output file " + output + " does not exist.", output.exists());
        assertTrue("Output file " + output + " is empty.", output.length() > 0);

    }

    @Test(timeout=1000)
    @Ignore(value = "Fails - presumably due to the log(0) statement.")
    public void testLee_alpha_1_00() throws Exception {
        System.out.println("Testing Lee (alpha=1.00) from main method.");

        final String dataSet = "bnc-gramrels-fruit";

        File output = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".Lee-alpha-1_00");
        output.delete();

        enableExistTrapping();
        Main.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "Lee",
                    "--lee-alpha", "1.00",
                    "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
                    "--input-features", TEST_FRUIT_FEATURES.toString(),
                    "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                    "--output", output.toString()
                });
        disableExitTrapping();

        assertTrue("Output file " + output + " does not exist.", output.exists());
        assertTrue("Output file " + output + " is empty.", output.length() > 0);
    }
}
