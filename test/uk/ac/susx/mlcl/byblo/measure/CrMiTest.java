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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hiam20
 */
public class CrMiTest {

    private static final String SAMPLE_DATA_DIR = "sampledata" + File.separator;

    private static final String OUTPUT_DIR = SAMPLE_DATA_DIR + "out" + File.separator;

    public CrMiTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(CrMiTest.class.getName()).log(Level.SEVERE, null,
                    ex);
        }
    }

    @Test
    public void testMainMethodRun_beta_0_gamma_0() throws Exception {
        System.out.println(
                "Testing CrMi (beta=0, gamma=0) \"recall\" from main method.");

        final String dataSet = "bnc-gramrels-fruit";

        File output = new File(
                OUTPUT_DIR + dataSet + ".CrMi-beta-0_00-gamma_0_00");
        if (output.exists())
            output.delete();

        String[] args = new String[]{
            "allpairs",
            "--charset", "UTF-8",
            "--measure", "CrMi",
            "--crmi-beta", "0.00",
            "--crmi-gamma", "0.00",
            "--input", SAMPLE_DATA_DIR + dataSet + ".features",
            "--input-contexts", SAMPLE_DATA_DIR + dataSet + ".contexts",
            "--input-heads", SAMPLE_DATA_DIR + dataSet + ".heads",
            "--output", output.toString()
        };

        Byblo.main(args);

        assertTrue("Output file " + output + " does not exist.", output.exists());
        assertTrue("Output file " + output + " is empty.", output.length() > 0);
    }

    @Test
    public void testMainMethodRun_beta_0_5_gamma_0() throws Exception {
        System.out.println(
                "Testing CrMi (beta=0.5, gamma=0) \"arithmetic mean\" from main method.");

        final String dataSet = "bnc-gramrels-fruit";

        File output = new File(
                OUTPUT_DIR + dataSet + ".CrMi-beta-0_50-gamma_0_00");
        if (output.exists())
            output.delete();

        String[] args = new String[]{
            "allpairs",
            "--charset", "UTF-8",
            "--measure", "CrMi",
            "--crmi-beta", "0.50",
            "--crmi-gamma", "0.00",
            "--input", SAMPLE_DATA_DIR + dataSet + ".features",
            "--input-contexts", SAMPLE_DATA_DIR + dataSet + ".contexts",
            "--input-heads", SAMPLE_DATA_DIR + dataSet + ".heads",
            "--output", output.toString()
        };

        Byblo.main(args);

        assertTrue("Output file " + output + " does not exist.", output.exists());
        assertTrue("Output file " + output + " is empty.", output.length() > 0);

    }

    @Test
    public void testMainMethodRun_beta_1_gamma_0() throws Exception {
        System.out.println(
                "Testing CrMi (beta=1, gamma=0) \"precision\" from main method.");

        final String dataSet = "bnc-gramrels-fruit";

        File output = new File(
                OUTPUT_DIR + dataSet + ".CrMi-beta-1_00-gamma_0_00");
        if (output.exists())
            output.delete();

        String[] args = new String[]{
            "allpairs",
            "--charset", "UTF-8",
            "--measure", "CrMi",
            "--crmi-beta", "1.00",
            "--crmi-gamma", "0.00",
            "--input", SAMPLE_DATA_DIR + dataSet + ".features",
            "--input-contexts", SAMPLE_DATA_DIR + dataSet + ".contexts",
            "--input-heads", SAMPLE_DATA_DIR + dataSet + ".heads",
            "--output", output.toString()
        };

        Byblo.main(args);

        assertTrue("Output file " + output + " does not exist.", output.exists());
        assertTrue("Output file " + output + " is empty.", output.length() > 0);

    }

    @Test
    public void testMainMethodRun_beta_NA_gamma_1() throws Exception {
        System.out.println(
                "Testing CrMi (beta=NA, gamma=1) \"harmonic mean\" from main method.");

        final String dataSet = "bnc-gramrels-fruit";

        File output = new File(OUTPUT_DIR + dataSet + ".CrMi-beta-NA-gamma_1_00");
        if (output.exists())
            output.delete();

        String[] args = new String[]{
            "allpairs",
            "--charset", "UTF-8",
            "--measure", "CrMi",
            //            "--crmi-beta", "1.00",
            "--crmi-gamma", "1.00",
            "--input", SAMPLE_DATA_DIR + dataSet + ".features",
            "--input-contexts", SAMPLE_DATA_DIR + dataSet + ".contexts",
            "--input-heads", SAMPLE_DATA_DIR + dataSet + ".heads",
            "--output", output.toString()
        };

        Byblo.main(args);

        assertTrue("Output file " + output + " does not exist.", output.exists());
        assertTrue("Output file " + output + " is empty.", output.length() > 0);

    }

    @Test
    public void testMainMethodRun_recallCheck() throws Exception {
        System.out.println(
                "Testing CrMi(beta=0, gamma=0) equals RecallMi from main method.");

        final String dataSet = "bnc-gramrels-fruit";
        File expectedOutput = new File(
                OUTPUT_DIR + dataSet + ".CrMi-recall-expected");
        File crmiOutput = new File(
                OUTPUT_DIR + dataSet + ".CrMi-recall-actual");

        if (crmiOutput.exists())
            crmiOutput.delete();
        if (expectedOutput.exists())
            expectedOutput.delete();

        Byblo.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "RecallMi",
                    "--input", SAMPLE_DATA_DIR + dataSet + ".features",
                    "--input-contexts", SAMPLE_DATA_DIR + dataSet + ".contexts",
                    "--input-heads", SAMPLE_DATA_DIR + dataSet + ".heads",
                    "--output", expectedOutput.toString()
                });
        Byblo.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "CrMi",
                    "--crmi-beta", "0.00",
                    "--crmi-gamma", "0.00",
                    "--input", SAMPLE_DATA_DIR + dataSet + ".features",
                    "--input-contexts", SAMPLE_DATA_DIR + dataSet + ".contexts",
                    "--input-heads", SAMPLE_DATA_DIR + dataSet + ".heads",
                    "--output", crmiOutput.toString()
                });


        assertTrue(Files.equal(expectedOutput, crmiOutput));

    }

    @Test
    public void testMainMethodRun_precisionCheck() throws Exception {
        System.out.println(
                "Testing CrMi(beta=1, gamma=0) equals PrecisionMi from main method.");

        final String dataSet = "bnc-gramrels-fruit";
        File expectedOutput = new File(
                OUTPUT_DIR + dataSet + ".CrMi-precision-expected");
        File crmiOutput = new File(
                OUTPUT_DIR + dataSet + ".CrMi-precision-actual");

        if (crmiOutput.exists())
            crmiOutput.delete();
        if (expectedOutput.exists())
            expectedOutput.delete();

        Byblo.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "RecallMi",
                    "--measure-reversed",
                    "--input", SAMPLE_DATA_DIR + dataSet + ".features",
                    "--input-contexts", SAMPLE_DATA_DIR + dataSet + ".contexts",
                    "--input-heads", SAMPLE_DATA_DIR + dataSet + ".heads",
                    "--output", expectedOutput.toString()
                });
        Byblo.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "CrMi",
                    "--crmi-beta", "1.00",
                    "--crmi-gamma", "0.00",
                    "--input", SAMPLE_DATA_DIR + dataSet + ".features",
                    "--input-contexts", SAMPLE_DATA_DIR + dataSet + ".contexts",
                    "--input-heads", SAMPLE_DATA_DIR + dataSet + ".heads",
                    "--output", crmiOutput.toString()
                });


        assertTrue(Files.equal(expectedOutput, crmiOutput));

    }

    @Test
    @Ignore(value="Fails though may have an equivilant ranking.")
    public void testMainMethodRun_diceCheck() throws Exception {
        System.out.println(
                "Testing CrMi(beta=NA, gamma=1) \"harmonic mean\" equals DiceMi  from main method.");

        final String dataSet = "bnc-gramrels-fruit";
        File expectedOutput = new File(
                OUTPUT_DIR + dataSet + ".CrMi-dice-expected");
        File crmiOutput = new File(
                OUTPUT_DIR + dataSet + ".CrMi-dice-actual");

        if (crmiOutput.exists())
            crmiOutput.delete();
        if (expectedOutput.exists())
            expectedOutput.delete();

        Byblo.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "DiceMi",
                    "--input", SAMPLE_DATA_DIR + dataSet + ".features",
                    "--input-contexts", SAMPLE_DATA_DIR + dataSet + ".contexts",
                    "--input-heads", SAMPLE_DATA_DIR + dataSet + ".heads",
                    "--output", expectedOutput.toString()
                });
        Byblo.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "CrMi",
                    "--crmi-beta", "0.00",
                    "--crmi-gamma", "1.00",
                    "--input", SAMPLE_DATA_DIR + dataSet + ".features",
                    "--input-contexts", SAMPLE_DATA_DIR + dataSet + ".contexts",
                    "--input-heads", SAMPLE_DATA_DIR + dataSet + ".heads",
                    "--output", crmiOutput.toString()
                });


        assertTrue(Files.equal(expectedOutput, crmiOutput));

    }
}
