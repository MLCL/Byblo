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
package uk.ac.susx.mlcl.dttools.measure;

import com.google.common.io.Files;
import uk.ac.susx.mlcl.dttools.DTTools;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hiam20
 */
public class DiceTest {

    private static final String SAMPLE_DATA_DIR = "sampledata" + File.separator;

    private static final String OUTPUT_DIR = SAMPLE_DATA_DIR + "out" + File.separator;

    public DiceTest() {
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
    }

    @Test
    public void testMainMethodRun_Dice() throws Exception {
        System.out.println("Testing Dice from main method.");

        final String dataSet = "bnc-gramrels-fruit";

        File output = new File(OUTPUT_DIR + dataSet + ".Dice");
        if (output.exists())
            output.delete();

        String[] args = new String[]{
            "allpairs",
            "--charset", "UTF-8",
            "--measure", "Dice",
            "--input", SAMPLE_DATA_DIR + dataSet + ".features",
            "--input-contexts", SAMPLE_DATA_DIR + dataSet + ".contexts",
            "--input-heads", SAMPLE_DATA_DIR + dataSet + ".heads",
            "--output", output.toString()
        };

        DTTools.main(args);

        assertTrue("Output file " + output + " does not exist.", output.exists());
        assertTrue("Output file " + output + " is empty.", output.length() > 0);

        Thread.sleep(100);
    }


    @Test
    public void testMainMethodRun_DiceMI() throws Exception {
        System.out.println("Testing DiceMi from main method.");

        final String dataSet = "bnc-gramrels-fruit";

        File output = new File(OUTPUT_DIR + dataSet + ".DiceMi");
        if (output.exists())
            output.delete();

        String[] args = new String[]{
            "allpairs",
            "--charset", "UTF-8",
            "--measure", "DiceMi",
            "--input", SAMPLE_DATA_DIR + dataSet + ".features",
            "--input-contexts", SAMPLE_DATA_DIR + dataSet + ".contexts",
            "--input-heads", SAMPLE_DATA_DIR + dataSet + ".heads",
            "--output", output.toString()
        };

        DTTools.main(args);

        assertTrue("Output file " + output + " does not exist.", output.exists());
        assertTrue("Output file " + output + " is empty.", output.length() > 0);

        Thread.sleep(100);
    }



    @Test
    public void test_Dice_Symmetry() throws Exception {
        System.out.println("Testing symmetry.");

        final String dataSet = "bnc-gramrels-fruit";

        File output1 = new File(OUTPUT_DIR + dataSet + ".Dice-1");
        File output2 = new File(OUTPUT_DIR + dataSet + ".Dice-2");
        if (output1.exists())
            output1.delete();
        if (output2.exists())
            output2.delete();

        DTTools.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "Dice",
                    "--input", SAMPLE_DATA_DIR + dataSet + ".features",
                    "--input-contexts", SAMPLE_DATA_DIR + dataSet + ".contexts",
                    "--input-heads", SAMPLE_DATA_DIR + dataSet + ".heads",
                    "--output", output1.toString()
                });
        DTTools.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "Dice",
                    "--measure-reversed",
                    "--input", SAMPLE_DATA_DIR + dataSet + ".features",
                    "--input-contexts", SAMPLE_DATA_DIR + dataSet + ".contexts",
                    "--input-heads", SAMPLE_DATA_DIR + dataSet + ".heads",
                    "--output", output2.toString()
                });

        assertTrue(Files.equal(output1, output2));

        Thread.sleep(100);
    }



    @Test
    public void test_DiceMi_Symmetry() throws Exception {
        System.out.println("Testing symmetry.");

        final String dataSet = "bnc-gramrels-fruit";

        File output1 = new File(OUTPUT_DIR + dataSet + ".DiceMi-1");
        File output2 = new File(OUTPUT_DIR + dataSet + ".DiceMi-2");
        if (output1.exists())
            output1.delete();
        if (output2.exists())
            output2.delete();

        DTTools.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "DiceMi",
                    "--input", SAMPLE_DATA_DIR + dataSet + ".features",
                    "--input-contexts", SAMPLE_DATA_DIR + dataSet + ".contexts",
                    "--input-heads", SAMPLE_DATA_DIR + dataSet + ".heads",
                    "--output", output1.toString()
                });
        DTTools.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "DiceMi",
                    "--measure-reversed",
                    "--input", SAMPLE_DATA_DIR + dataSet + ".features",
                    "--input-contexts", SAMPLE_DATA_DIR + dataSet + ".contexts",
                    "--input-heads", SAMPLE_DATA_DIR + dataSet + ".heads",
                    "--output", output2.toString()
                });

        assertTrue(Files.equal(output1, output2));

        Thread.sleep(100);
    }
}
