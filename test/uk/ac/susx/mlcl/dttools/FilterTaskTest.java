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
package uk.ac.susx.mlcl.dttools;

import uk.ac.susx.mlcl.dttools.DTTools;
import com.google.common.io.Files;
import java.nio.charset.Charset;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hamish
 */
public class FilterTaskTest {

    private static final File INPUT_DIR = new File("sampledata");

    private static final File OUTPUT_DIR = new File(INPUT_DIR, "out");

    public FilterTaskTest() {
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
    public void testMainMethodRun_fruit_headFreqFilter() throws Exception {
        System.out.println(
                "Testing FilterHeads: on fruit, from main method, filter by head freqency.");

        final String dataSet = "bnc-gramrels-fruit";

        File inputHeads = new File(INPUT_DIR, dataSet + ".heads");
        File inputContexts = new File(INPUT_DIR, dataSet + ".contexts");
        File inputFeatures = new File(INPUT_DIR, dataSet + ".features");
        File outputHeads = new File(OUTPUT_DIR,
                dataSet + ".heads.headFreqFilter");
        File outputContexts = new File(OUTPUT_DIR,
                dataSet + ".contexts.headFreqFilter");
        File outputFeatures = new File(OUTPUT_DIR,
                dataSet + ".features.headFreqFilter");

        if (outputHeads.exists())
            outputHeads.delete();
        if (outputContexts.exists())
            outputContexts.delete();
        if (outputFeatures.exists())
            outputFeatures.delete();

        String[] args = new String[]{
            "filter",
            "--charset", "UTF-8",
            "--input-heads", inputHeads.toString(),
            "--input-contexts", inputContexts.toString(),
            "--input-features", inputFeatures.toString(),
            "--output-heads", outputHeads.toString(),
            "--output-contexts", outputContexts.toString(),
            "--output-features", outputFeatures.toString(),
            "--filter-head-freq", "50"
        };

        DTTools.main(args);

        assertTrue("Output file " + outputHeads + " does not exist.", outputHeads.
                exists());
        assertTrue("Output file " + outputContexts + " does not exist.", outputContexts.
                exists());
        assertTrue("Output file " + outputFeatures + " does not exist.", outputFeatures.
                exists());

        Thread.sleep(100);
    }

    @Test
    public void testMainMethodRun_fruit_contextFreqFilter() throws Exception {
        System.out.println(
                "Testing FilterHeads on fruit from main method, filter by context freqency.");

        final String dataSet = "bnc-gramrels-fruit";

        File inputHeads = new File(INPUT_DIR, dataSet + ".heads");
        File inputContexts = new File(INPUT_DIR, dataSet + ".contexts");
        File inputFeatures = new File(INPUT_DIR, dataSet + ".features");
        File outputHeads = new File(OUTPUT_DIR,
                dataSet + ".heads.contextFreqFilter");
        File outputContexts = new File(OUTPUT_DIR,
                dataSet + ".contexts.contextFreqFilter");
        File outputFeatures = new File(OUTPUT_DIR,
                dataSet + ".features.contextFreqFilter");

        if (outputHeads.exists())
            outputHeads.delete();
        if (outputContexts.exists())
            outputContexts.delete();
        if (outputFeatures.exists())
            outputFeatures.delete();

        String[] args = new String[]{
            "filter",
            "--charset", "UTF-8",
            "--input-heads", inputHeads.toString(),
            "--input-contexts", inputContexts.toString(),
            "--input-features", inputFeatures.toString(),
            "--output-heads", outputHeads.toString(),
            "--output-contexts", outputContexts.toString(),
            "--output-features", outputFeatures.toString(),
            "--filter-context-freq", "50"
        };

        DTTools.main(args);

        assertTrue("Output file " + outputHeads + " does not exist.", outputHeads.
                exists());
        assertTrue("Output file " + outputContexts + " does not exist.", outputContexts.
                exists());
        assertTrue("Output file " + outputFeatures + " does not exist.", outputFeatures.
                exists());

        Thread.sleep(100);
    }

    @Test
    public void testMainMethodRun_fruit_featureFreqFilter() throws Exception {
        System.out.println(
                "Testing FilterHeads on fruit from main method, filter by feature freqency.");

        final String dataSet = "bnc-gramrels-fruit";

        File inputHeads = new File(INPUT_DIR, dataSet + ".heads");
        File inputContexts = new File(INPUT_DIR, dataSet + ".contexts");
        File inputFeatures = new File(INPUT_DIR, dataSet + ".features");
        File outputHeads = new File(OUTPUT_DIR,
                dataSet + ".heads.featureFreqFilter");
        File outputContexts = new File(OUTPUT_DIR,
                dataSet + ".contexts.featureFreqFilter");
        File outputFeatures = new File(OUTPUT_DIR,
                dataSet + ".features.featureFreqFilter");

        if (outputHeads.exists())
            outputHeads.delete();
        if (outputContexts.exists())
            outputContexts.delete();
        if (outputFeatures.exists())
            outputFeatures.delete();

        String[] args = new String[]{
            "filter",
            "--charset", "UTF-8",
            "--input-heads", inputHeads.toString(),
            "--input-contexts", inputContexts.toString(),
            "--input-features", inputFeatures.toString(),
            "--output-heads", outputHeads.toString(),
            "--output-contexts", outputContexts.toString(),
            "--output-features", outputFeatures.toString(),
            "--filter-feature-freq", "5"
        };

        DTTools.main(args);

        assertTrue("Output file " + outputHeads + " does not exist.", outputHeads.
                exists());
        assertTrue("Output file " + outputContexts + " does not exist.", outputContexts.
                exists());
        assertTrue("Output file " + outputFeatures + " does not exist.", outputFeatures.
                exists());

        Thread.sleep(100);
    }

    @Test
    public void testMainMethodRun_fruit_headWordlistFilter() throws Exception {
        System.out.println(
                "Testing FilterHeads: on fruit, from main method, filter by head word list.");


        final String dataSet = "bnc-gramrels-fruit";


        File inputHeads = new File(INPUT_DIR, dataSet + ".heads");
        File inputContexts = new File(INPUT_DIR, dataSet + ".contexts");
        File inputFeatures = new File(INPUT_DIR, dataSet + ".features");
        File outputHeads = new File(OUTPUT_DIR,
                dataSet + ".heads.headWordlistFilter");
        File outputContexts = new File(OUTPUT_DIR,
                dataSet + ".contexts.headWordlistFilter");
        File outputFeatures = new File(OUTPUT_DIR,
                dataSet + ".features.headWordlistFilter");
        File headWorldList = new File(OUTPUT_DIR, dataSet + ".headWordList");

        // Create the word list
        Files.write("apple\norange\npear\nbanana", headWorldList, Charset.
                forName("UTF-8"));

        if (outputHeads.exists())
            outputHeads.delete();
        if (outputContexts.exists())
            outputContexts.delete();
        if (outputFeatures.exists())
            outputFeatures.delete();

        String[] args = new String[]{
            "filter",
            "--charset", "UTF-8",
            "--input-heads", inputHeads.toString(),
            "--input-contexts", inputContexts.toString(),
            "--input-features", inputFeatures.toString(),
            "--output-heads", outputHeads.toString(),
            "--output-contexts", outputContexts.toString(),
            "--output-features", outputFeatures.toString(),
            "--filter-head-wordlist", headWorldList.toString()
        };

        DTTools.main(args);

        assertTrue("Output file " + outputHeads + " does not exist.", outputHeads.
                exists());
        assertTrue("Output file " + outputContexts + " does not exist.", outputContexts.
                exists());
        assertTrue("Output file " + outputFeatures + " does not exist.", outputFeatures.
                exists());

        Thread.sleep(100);
    }

    @Test
    public void testMainMethodRun_fruit_contextWordlistFilter() throws Exception {
        System.out.println(
                "Testing FilterHeads: on fruit, from main method, filter by context word list.");

        final String dataSet = "bnc-gramrels-fruit";


        File inputHeads = new File(INPUT_DIR, dataSet + ".heads");
        File inputContexts = new File(INPUT_DIR, dataSet + ".contexts");
        File inputFeatures = new File(INPUT_DIR, dataSet + ".features");
        File outputHeads = new File(OUTPUT_DIR,
                dataSet + ".heads.contextWordlistFilter");
        File outputContexts = new File(OUTPUT_DIR,
                dataSet + ".contexts.contextWordlistFilter");
        File outputFeatures = new File(OUTPUT_DIR,
                dataSet + ".features.contextWordlistFilter");
        File contextWorldList = new File(OUTPUT_DIR,
                dataSet + ".contextWordList");

        // Create the word list
        Files.write("det:the\ndet:a\niobj:of\nncmod:back\nncmod:for\npassive",
                contextWorldList, Charset.forName("UTF-8"));

        if (outputHeads.exists())
            outputHeads.delete();
        if (outputContexts.exists())
            outputContexts.delete();
        if (outputFeatures.exists())
            outputFeatures.delete();

        String[] args = new String[]{
            "filter",
            "--charset", "UTF-8",
            "--input-heads", inputHeads.toString(),
            "--input-contexts", inputContexts.toString(),
            "--input-features", inputFeatures.toString(),
            "--output-heads", outputHeads.toString(),
            "--output-contexts", outputContexts.toString(),
            "--output-features", outputFeatures.toString(),
            "--filter-context-wordlist", contextWorldList.toString()
        };

        DTTools.main(args);

        assertTrue("Output file " + outputHeads + " does not exist.",
                outputHeads.exists());
        assertTrue("Output file " + outputContexts + " does not exist.",
                outputContexts.exists());
        assertTrue("Output file " + outputFeatures + " does not exist.",
                outputFeatures.exists());

        Thread.sleep(100);
    }

    @Test
    public void testMainMethodRun_fruit_headPatternFilter() throws Exception {
        System.out.println(
                "Testing FilterHeads: on fruit, from main method, filter by head pattern.");


        final String dataSet = "bnc-gramrels-fruit";


        File inputHeads = new File(INPUT_DIR, dataSet + ".heads");
        File inputContexts = new File(INPUT_DIR, dataSet + ".contexts");
        File inputFeatures = new File(INPUT_DIR, dataSet + ".features");
        File outputHeads = new File(OUTPUT_DIR,
                dataSet + ".heads.headPatternFilter");
        File outputContexts = new File(OUTPUT_DIR,
                dataSet + ".contexts.headPatternFilter");
        File outputFeatures = new File(OUTPUT_DIR,
                dataSet + ".features.headPatternFilter");

        if (outputHeads.exists())
            outputHeads.delete();
        if (outputContexts.exists())
            outputContexts.delete();
        if (outputFeatures.exists())
            outputFeatures.delete();

        String[] args = new String[]{
            "filter",
            "--charset", "UTF-8",
            "--input-heads", inputHeads.toString(),
            "--input-contexts", inputContexts.toString(),
            "--input-features", inputFeatures.toString(),
            "--output-heads", outputHeads.toString(),
            "--output-contexts", outputContexts.toString(),
            "--output-features", outputFeatures.toString(),
            "--filter-head-pattern", "^.{0,5}$"
        };

        DTTools.main(args);

        assertTrue("Output file " + outputHeads + " does not exist.", outputHeads.
                exists());
        assertTrue("Output file " + outputContexts + " does not exist.", outputContexts.
                exists());
        assertTrue("Output file " + outputFeatures + " does not exist.", outputFeatures.
                exists());

        Thread.sleep(100);
    }

    @Test
    public void testMainMethodRun_fruit_contextPatternFilter() throws Exception {
        System.out.println(
                "Testing FilterHeads: on fruit, from main method, filter by context pattern.");


        final String dataSet = "bnc-gramrels-fruit";


        File inputHeads = new File(INPUT_DIR, dataSet + ".heads");
        File inputContexts = new File(INPUT_DIR, dataSet + ".contexts");
        File inputFeatures = new File(INPUT_DIR, dataSet + ".features");
        File outputHeads = new File(OUTPUT_DIR,
                dataSet + ".heads.contextPatternFilter");
        File outputContexts = new File(OUTPUT_DIR,
                dataSet + ".contexts.contextPatternFilter");
        File outputFeatures = new File(OUTPUT_DIR,
                dataSet + ".features.contextPatternFilter");

        if (outputHeads.exists())
            outputHeads.delete();
        if (outputContexts.exists())
            outputContexts.delete();
        if (outputFeatures.exists())
            outputFeatures.delete();

        String[] args = new String[]{
            "filter",
            "--charset", "UTF-8",
            "--input-heads", inputHeads.toString(),
            "--input-contexts", inputContexts.toString(),
            "--input-features", inputFeatures.toString(),
            "--output-heads", outputHeads.toString(),
            "--output-contexts", outputContexts.toString(),
            "--output-features", outputFeatures.toString(),
            "--filter-context-pattern", "det:.*"
        };

        DTTools.main(args);

        assertTrue("Output file " + outputHeads + " does not exist.", outputHeads.
                exists());
        assertTrue("Output file " + outputContexts + " does not exist.", outputContexts.
                exists());
        assertTrue("Output file " + outputFeatures + " does not exist.", outputFeatures.
                exists());

        Thread.sleep(100);
    }

    @Test
    public void testMainMethodRun_fruit_allFilters() throws Exception {
        System.out.println(
                "Testing FilterHeads: on fruit, from main method, using all filters.");


        final String dataSet = "bnc-gramrels-fruit";


        File inputHeads = new File(INPUT_DIR, dataSet + ".heads");
        File inputContexts = new File(INPUT_DIR, dataSet + ".contexts");
        File inputFeatures = new File(INPUT_DIR, dataSet + ".features");
        File outputHeads = new File(OUTPUT_DIR,
                dataSet + ".heads.allFilters");
        File outputContexts = new File(OUTPUT_DIR,
                dataSet + ".contexts.allFilters");
        File outputFeatures = new File(OUTPUT_DIR,
                dataSet + ".features.allFilters");
        File headWorldList = new File(OUTPUT_DIR, dataSet + ".allFilters-headWordList");
        File contextWorldList = new File(OUTPUT_DIR, dataSet + ".allFilters-contextWordList");

        // Create the word list
        Files.write("apple\napricot\navocado\nbanana\nbilberry\nblackberry\n"
                + "blackcap\nblackcurrant\nblueberry\ncantaloupe\ncherry\n"
                + "clementine\ncurrant\ndamson\ndate\ndurian\neggplant\n"
                + "elderberry\ngooseberry\ngrape\ngrapefruit\nhuckleberry\n"
                + "kumquat", headWorldList, Charset.forName("UTF-8"));

         Files.write("det:the\ndet:a\niobj:of\nncmod:back\nncmod:for\npassive", contextWorldList, Charset.forName("UTF-8"));

        if (outputHeads.exists())
            outputHeads.delete();
        if (outputContexts.exists())
            outputContexts.delete();
        if (outputFeatures.exists())
            outputFeatures.delete();

        String[] args = new String[]{
            "filter",
            "--charset", "UTF-8",
            "--input-heads", inputHeads.toString(),
            "--input-contexts", inputContexts.toString(),
            "--input-features", inputFeatures.toString(),
            "--output-heads", outputHeads.toString(),
            "--output-contexts", outputContexts.toString(),
            "--output-features", outputFeatures.toString(),
            "--filter-head-wordlist", headWorldList.toString(), // a-k
            "--filter-head-freq", "10",
            "--filter-context-freq", "5",
            "--filter-feature-freq", "2",
            "--filter-context-pattern", ":", // removes "passive"
            "--filter-context-wordlist", contextWorldList.toString(),
            "--filter-head-pattern", "a" // remove gooseberry, cherry...

        };

        DTTools.main(args);

        assertTrue("Output file " + outputHeads + " does not exist.", outputHeads.
                exists());
        assertTrue("Output file " + outputContexts + " does not exist.", outputContexts.
                exists());
        assertTrue("Output file " + outputFeatures + " does not exist.", outputFeatures.
                exists());

        Thread.sleep(100);
    }

    /*
     * Test ignored because it uses bnc gram rels dense which doesn't exist any more
     */
    @Test
    @Ignore 
    public void testMainMethodRun_Dense() throws Exception {
        System.out.println("Testing FilterHeads on dense from main method.");

        final String dataSet = "bnc-gramrels-dense";

        File inputHeads = new File(INPUT_DIR, dataSet + ".heads");
        File inputContexts = new File(INPUT_DIR, dataSet + ".contexts");
        File inputFeatures = new File(INPUT_DIR, dataSet + ".features");
        File outputHeads = new File(OUTPUT_DIR, dataSet + ".heads.filtered");
        File outputContexts = new File(OUTPUT_DIR,
                dataSet + ".contexts.filtered");
        File outputFeatures = new File(OUTPUT_DIR,
                dataSet + ".features.filtered");

        if (outputHeads.exists())
            outputHeads.delete();
        if (outputContexts.exists())
            outputContexts.delete();
        if (outputFeatures.exists())
            outputFeatures.delete();

        String[] args = new String[]{
            "filter",
            "--charset", "UTF-8",
            "--input-heads", inputHeads.toString(),
            "--input-contexts", inputContexts.toString(),
            "--input-features", inputFeatures.toString(),
            "--output-heads", outputHeads.toString(),
            "--output-contexts", outputContexts.toString(),
            "--output-features", outputFeatures.toString(),
            "--filter-head-freq", "50"
        };

        DTTools.main(args);

        assertTrue("Output file " + outputHeads + " does not exist.", outputHeads.
                exists());
        assertTrue("Output file " + outputContexts + " does not exist.", outputContexts.
                exists());
        assertTrue("Output file " + outputFeatures + " does not exist.", outputFeatures.
                exists());

        Thread.sleep(100);
    }

    @Test
    public void testMainMethodRun_Sparse() throws Exception {
        System.out.println("Testing FilterHeads on sparse from main method.");

        final String dataSet = "bnc-gramrels-sparse";

        File inputHeads = new File(INPUT_DIR, dataSet + ".heads");
        File inputContexts = new File(INPUT_DIR, dataSet + ".contexts");
        File inputFeatures = new File(INPUT_DIR, dataSet + ".features");
        File outputHeads = new File(OUTPUT_DIR, dataSet + ".heads.filtered");
        File outputContexts = new File(OUTPUT_DIR,
                dataSet + ".contexts.filtered");
        File outputFeatures = new File(OUTPUT_DIR,
                dataSet + ".features.filtered");

        if (outputHeads.exists())
            outputHeads.delete();
        if (outputContexts.exists())
            outputContexts.delete();
        if (outputFeatures.exists())
            outputFeatures.delete();

        String[] args = new String[]{
            "filter",
            "--charset", "UTF-8",
            "--input-heads", inputHeads.toString(),
            "--input-contexts", inputContexts.toString(),
            "--input-features", inputFeatures.toString(),
            "--output-heads", outputHeads.toString(),
            "--output-contexts", outputContexts.toString(),
            "--output-features", outputFeatures.toString(),
            "--filter-head-freq", "50"
        };

        DTTools.main(args);

        assertTrue("Output file " + outputHeads + " does not exist.", outputHeads.
                exists());
        assertTrue("Output file " + outputContexts + " does not exist.", outputContexts.
                exists());
        assertTrue("Output file " + outputFeatures + " does not exist.", outputFeatures.
                exists());

        Thread.sleep(100);
    }
}
