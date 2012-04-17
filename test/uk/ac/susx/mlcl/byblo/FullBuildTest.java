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
 * POSSIBILITY OF SUCH DAMAGE.To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import static uk.ac.susx.mlcl.TestConstants.*;

/**
 *
 * @author hiam20
 */
public class FullBuildTest {

    public FullBuildTest() {
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
//

    @Test
    public void testRunCommand_Fruit() throws Exception {
        System.out.println("Test on fruit");


        FullBuild instance = new FullBuild();
        instance.setCharset(DEFAULT_CHARSET);
        instance.setInstancesFile(TEST_FRUIT_INPUT);
        instance.setOutputDir(TEST_OUTPUT_DIR);
        instance.setTempBaseDir(TEST_OUTPUT_DIR);
        instance.runCommand();


    }

    @Test
    public void testRunCommand_Medtest100k() throws Exception {
        System.out.println("Test on fruit");

        File medtestDir = new File(TEST_DATA_DIR, "medtest");
        File input = new File(medtestDir, "medtest-tb-cb-ng-nl-nr-vhl-vhrpl-pr-cw-55-sample100k");

        FullBuild instance = new FullBuild();
        instance.setCharset(DEFAULT_CHARSET);
        instance.setInstancesFile(input);
        instance.setOutputDir(TEST_OUTPUT_DIR);
        instance.setTempBaseDir(TEST_OUTPUT_DIR);
        instance.setMinSimilarity(0.1);
        instance.setFilterEntryMinFreq(2);
        instance.setFilterFeatureMinFreq(2);
        instance.setFilterEventMinFreq(2);
        instance.runCommand();


    }

    @Test
    @Ignore
    public void testRunCommand_Medtest1m() throws Exception {
        System.out.println("Test on fruit");

        File medtestDir = new File(TEST_DATA_DIR, "medtest");
        File input = new File(medtestDir, "medtest-tb-cb-ng-nl-nr-vhl-vhrpl-pr-cw-55-sample1m");

        FullBuild instance = new FullBuild();
        instance.setCharset(DEFAULT_CHARSET);
        instance.setInstancesFile(input);
        instance.setOutputDir(TEST_OUTPUT_DIR);
        instance.setTempBaseDir(TEST_OUTPUT_DIR);
        instance.setMinSimilarity(0.1);
        instance.setFilterEntryMinFreq(16);
        instance.setFilterFeatureMinFreq(9);
        instance.setFilterEventMinFreq(4);
        instance.setCompactFormatDisabled(false);
        instance.runCommand();


    }

    @Test
    @Ignore
    public void testRunCommand_Medtest10m() throws Exception {
        System.out.println("Test on fruit");

        File medtestDir = new File(TEST_DATA_DIR, "medtest");
        File input = new File(medtestDir, "medtest-tb-cb-ng-nl-nr-vhl-vhrpl-pr-cw-55-sample10m");

        FullBuild instance = new FullBuild();
        instance.setCharset(DEFAULT_CHARSET);
        instance.setInstancesFile(input);
        instance.setOutputDir(TEST_OUTPUT_DIR);
        instance.setTempBaseDir(TEST_OUTPUT_DIR);
        instance.setMinSimilarity(0.1);
        instance.setFilterEntryMinFreq(160);
        instance.setFilterFeatureMinFreq(90);
        instance.setFilterEventMinFreq(40);
        instance.setCompactFormatDisabled(false);
        instance.runCommand();


    }

}
