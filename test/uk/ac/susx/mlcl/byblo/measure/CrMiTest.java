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
package uk.ac.susx.mlcl.byblo.measure;

import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.Main;
import java.io.File;
import org.junit.Test;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSource;
import static org.junit.Assert.*;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.byblo.io.Token;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;
import uk.ac.susx.mlcl.lib.SimpleEnumerator;
import uk.ac.susx.mlcl.lib.io.TSVSource;
import static uk.ac.susx.mlcl.lib.test.ExitTrapper.*;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class CrMiTest {

    private void testCrmiCli(double beta, double gamma) throws Exception {
        System.out.println(
                "Testing CrMi (beta=" + beta + ", gamma=" + gamma + ") from main method.");

        File output = new File(TEST_OUTPUT_DIR,
                               FRUIT_NAME + ".CrMi-beta_" + beta + "-gamma_" + gamma + "");
        output.delete();

        try {
            enableExistTrapping();
            Main.main(new String[]{
                        "allpairs",
                        "--charset", "UTF-8",
                        "--measure", "CrMi",
                        "--crmi-beta", "" + beta,
                        "--crmi-gamma", "" + gamma,
                        "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
                        "--input-features", TEST_FRUIT_FEATURES.toString(),
                        "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                        "--output", output.toString()
                    });
        } finally {
            disableExitTrapping();
        }


        assertTrue("Output file " + output + " does not exist.", output.exists());
        assertTrue("Output file " + output + " is empty.", output.length() > 0);
    }

    @Test(timeout = 1000)
    public void testCRMI_beta_0_gamma_0() throws Exception {
        testCrmiCli(0, 0);
    }

    @Test(timeout = 1000)
    public void testCrmiCli_beta_0_5_gamma_0() throws Exception {
        testCrmiCli(0.5, 0);
    }

    @Test(timeout = 1000)
    public void testCrmiCli_beta_1_gamma_0() throws Exception {
        testCrmiCli(1, 0);
    }

    @Test(timeout = 1000)
    public void testCrmiCli_beta_NA_gamma_1() throws Exception {
        testCrmiCli(1, 1);
    }

    @Test(timeout = 1000, expected = IllegalArgumentException.class)
    public void testCrmiCli_fail1() throws Exception {
        testCrmiCli(-0.001, 1);
    }

    @Test(timeout = 1000, expected = IllegalArgumentException.class)
    public void testCrmiCli_fail2() throws Exception {
        testCrmiCli(1.001, 1);
    }

    @Test(timeout = 1000, expected = IllegalArgumentException.class)
    public void testCrmiCli_fail3() throws Exception {
        testCrmiCli(0, -0.001);
    }

    @Test(timeout = 1000, expected = IllegalArgumentException.class)
    public void testCrmiCli_fail4() throws Exception {
        testCrmiCli(0, 1.001);
    }

    @Test(timeout = 1000)
    public void testCrmiCli_recallCheck() throws Exception {
        System.out.println(
                "Testing CrMi(beta=0, gamma=0) equals RecallMi from main method.");

        File expectedOutput = new File(TEST_OUTPUT_DIR,
                                       FRUIT_NAME + ".CrMi-recall-expected");
        File crmiOutput = new File(TEST_OUTPUT_DIR,
                                   FRUIT_NAME + ".CrMi-recall-actual");

        crmiOutput.delete();
        expectedOutput.delete();

        try {
            enableExistTrapping();
            Main.main(new String[]{
                        "allpairs",
                        "--charset", "UTF-8",
                        "--measure", "RecallMi",
                        "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
                        "--input-features", TEST_FRUIT_FEATURES.toString(),
                        "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                        "--output", expectedOutput.toString()
                    });
            Main.main(new String[]{
                        "allpairs",
                        "--charset", "UTF-8",
                        "--measure", "CrMi",
                        "--crmi-beta", "0.00",
                        "--crmi-gamma", "0.00",
                        "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
                        "--input-features", TEST_FRUIT_FEATURES.toString(),
                        "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                        "--output", crmiOutput.toString()
                    });
        } finally {
            disableExitTrapping();
        }


        assertTrue(WeightedTokenPairSource.equal(expectedOutput, crmiOutput,
                                                 DEFAULT_CHARSET));

    }

    @Test(timeout = 1000)
    public void testCrmiCli_precisionCheck() throws Exception {
        System.out.println(
                "Testing CrMi(beta=1, gamma=0) equals PrecisionMi from main method.");

        File expectedOutput = new File(TEST_OUTPUT_DIR,
                                       FRUIT_NAME + ".CrMi-precision-expected");
        File crmiOutput = new File(TEST_OUTPUT_DIR,
                                   FRUIT_NAME + ".CrMi-precision-actual");

        crmiOutput.delete();
        expectedOutput.delete();

        try {
            enableExistTrapping();
            Main.main(new String[]{
                        "allpairs",
                        "--charset", "UTF-8",
                        "--measure", "RecallMi",
                        "--measure-reversed",
                        "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
                        "--input-features", TEST_FRUIT_FEATURES.toString(),
                        "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                        "--output", expectedOutput.toString()
                    });
            Main.main(new String[]{
                        "allpairs",
                        "--charset", "UTF-8",
                        "--measure", "CrMi",
                        "--crmi-beta", "1.00",
                        "--crmi-gamma", "0.00",
                        "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
                        "--input-features", TEST_FRUIT_FEATURES.toString(),
                        "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                        "--output", crmiOutput.toString()
                    });

        } finally {
            disableExitTrapping();
        }


        assertTrue(WeightedTokenPairSource.equal(expectedOutput, crmiOutput,
                                                 DEFAULT_CHARSET));

    }

    @Test(timeout = 1000)
    public void testCrmiCli_diceCheck() throws Exception {
        System.out.println(
                "Testing CrMi(beta=NA, gamma=1) \"harmonic mean\" equals DiceMi  from main method.");

        File expectedOutput = new File(TEST_OUTPUT_DIR,
                                       FRUIT_NAME + ".CrMi-dice-expected");
        File crmiOutput = new File(TEST_OUTPUT_DIR,
                                   FRUIT_NAME + ".CrMi-dice-actual");

        crmiOutput.delete();
        expectedOutput.delete();

        try {
            enableExistTrapping();
            Main.main(new String[]{
                        "allpairs",
                        "--charset", "UTF-8",
                        "--measure", "DiceMi",
                        "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
                        "--input-features", TEST_FRUIT_FEATURES.toString(),
                        "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                        "--output", expectedOutput.toString()
                    });
            Main.main(new String[]{
                        "allpairs",
                        "--charset", "UTF-8",
                        "--measure", "CrMi",
                        "--crmi-beta", "0.00",
                        "--crmi-gamma", "1.00",
                        "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
                        "--input-features", TEST_FRUIT_FEATURES.toString(),
                        "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                        "--output", crmiOutput.toString()
                    });

        } finally {
            disableExitTrapping();
        }



        // The ranking should be identical, but the absolute similarity values
        // will be different.


        Enumerator<String> idx = Enumerators.newDefaultStringEnumerator();
        WeightedTokenPairSource expected = new WeightedTokenPairSource(
                new TSVSource(expectedOutput, DEFAULT_CHARSET), Token.stringDecoder(idx), Token.stringDecoder(idx));
        WeightedTokenPairSource actual = new WeightedTokenPairSource(
                new TSVSource(crmiOutput, DEFAULT_CHARSET), Token.stringDecoder(idx), Token.stringDecoder(idx));

        while (expected.hasNext() && actual.hasNext()) {
            Weighted<TokenPair> e = expected.read();
            Weighted<TokenPair> a = actual.read();
            assertEquals(e.record().id1(), a.record().id1());
            assertEquals(e.record().id2(), a.record().id2());
        }

        assertEquals(expected.hasNext(), actual.hasNext());
    }

}
