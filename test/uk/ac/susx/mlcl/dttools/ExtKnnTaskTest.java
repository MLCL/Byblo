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

import java.nio.charset.Charset;
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
public class ExtKnnTaskTest {

    private static final File SAMPLE_DATA = new File("sampledata");

    private static final File OUTPUT = new File(SAMPLE_DATA, "out");

    private static final Charset CHARSET = Charset.forName("UTF-8");

    private static final String FRUIT_PREFIX = "bnc-gramrels-fruit";

    private static final String SPARSE_PREFIX = "bnc-gramrels-sparse";

    private static final File FRUIT_INSTANCES =
            new File(SAMPLE_DATA, FRUIT_PREFIX);

    private static final File SPARSE_INSTANCES =
            new File(SAMPLE_DATA, SPARSE_PREFIX);

    private static final File BIG_DATA = new File(
            "/Volumes/Local Scratch HD/bnc/thesaurus-bnc.rasp2.gramrels/data");

    private static final File BIG_INSTANCES = new File(
            BIG_DATA, "bnc.rasp2.gramrels.lcase");

    public ExtKnnTaskTest() {
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
    public void testRunOnFruit() throws Exception {
        System.out.println("Testing CountTask on " + FRUIT_INSTANCES);

        final File in = new File(SAMPLE_DATA, FRUIT_PREFIX + ".pairs-lin");
        final File out = new File(OUTPUT, FRUIT_PREFIX + ".pairs-lin" + ".knn");

        final ExtKnnTask knnTask = new ExtKnnTask(
                in, out, CHARSET, 2);
        knnTask.setMaxChunkSize(111);
        knnTask.run();

        while (knnTask.isExceptionThrown()) {
            knnTask.throwException();
        }

        assertTrue("Output files not created.", out.exists());
        assertTrue("Empty output file found.", out.length() > 0);
    }
}
