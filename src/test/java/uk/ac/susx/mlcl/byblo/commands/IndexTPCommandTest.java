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
package uk.ac.susx.mlcl.byblo.commands;

import junit.framework.Assert;
import org.junit.*;
import uk.ac.susx.mlcl.TestConstants;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratorType;

import java.io.File;

import static uk.ac.susx.mlcl.TestConstants.*;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class IndexTPCommandTest {

    @Test
    public void testRunOnFruitAPI() throws Exception {
        System.out.println("Testing " + IndexTPCommandTest.class.getName()
                + " on " + TEST_FRUIT_INPUT);

        final String name = TEST_FRUIT_INPUT.getName();
        final File out = new File(TEST_OUTPUT_DIR, name + ".indexed");
        final File idx1 = new File(TEST_OUTPUT_DIR, name + ".entry-index");
        final File idx2 = new File(TEST_OUTPUT_DIR, name + ".feature-index");

        deleteIfExist(out, idx1, idx2);

        indexTP(TEST_FRUIT_INPUT, out, idx1, idx2, EnumeratorType.Memory, false,
                false, true);


//        assertTrue(format(
//                "Output entries file \"{0}\" differs from expected file \"{1}\".",
//                out, TEST_FRUIT_INPUT_INDEXED),
//                   TokenPairSource.equal(out, TEST_FRUIT_INPUT_INDEXED,
//                                         DEFAULT_CHARSET, false, false));

        // XXX: The files can be out of order
        //        assertTrue("Output features file differs from test data file.",
        //                   Files.equal(idx1, TEST_FRUIT_ENTRY_INDEX));
        //        assertTrue("Output entry/features file differs from test data file.",
        //                   Files.equal(idx2, TEST_FRUIT_FEATURE_INDEX));


        File out2 = suffix(out, ".unindexed");
        unindexTP(out, out2, idx1, idx2, EnumeratorType.Memory, false, false,
                true);


//        TokenPairSource.equal(out, out2, DEFAULT_CHARSET, false, false);

    }

    @Test
    public void testRunOnFruitAPI_skipBoth_compact() throws Exception {
        testRunOnFruitAPI("compact-skipBoth-", EnumeratorType.Memory, true, true,
                true);
    }

    @Test
    public void testRunOnFruitAPI_skipLeft_compact() throws Exception {
        testRunOnFruitAPI("compact-skipLeft-", EnumeratorType.Memory, true,
                false, true);
    }

    @Test
    public void testRunOnFruitAPI_skipRight_compact() throws Exception {
        testRunOnFruitAPI("compact-skipRight-", EnumeratorType.Memory, false,
                true, true);
    }

    @Test
    public void testRunOnFruitAPI_skipBoth_verbose() throws Exception {
        testRunOnFruitAPI("verbose-skipBoth-", EnumeratorType.Memory, true, true,
                false);
    }

    @Test
    public void testRunOnFruitAPI_skipLeft_verbose() throws Exception {
        testRunOnFruitAPI("verbose-skipLeft-", EnumeratorType.Memory, true,
                false, false);
    }

    @Test
    public void testRunOnFruitAPI_skipRight_verbose() throws Exception {
        testRunOnFruitAPI("verbose-skipRight-", EnumeratorType.Memory, false,
                true, false);
    }

    @Test
    public void testRunOnFruitAPI_skipBoth_compact_jdbm() throws Exception {
        testRunOnFruitAPI("compact-skipBoth-jdbm-", EnumeratorType.JDBM, true,
                true, true);
    }

    @Test
    public void testRunOnFruitAPI_skipLeft_compact_jdbm() throws Exception {
        testRunOnFruitAPI("compact-skipLeft-jdbm-", EnumeratorType.JDBM, true,
                false, true);
    }

    @Test
    public void testRunOnFruitAPI_skipRight_compact_jdbm() throws Exception {
        testRunOnFruitAPI("compact-skipRight-jdbm-", EnumeratorType.JDBM, false,
                true, true);
    }

    @Test
    public void testRunOnFruitAPI_skipBoth_verbose_jdbm() throws Exception {
        testRunOnFruitAPI("verbose-skipBoth-jdbm-", EnumeratorType.JDBM, true,
                true, false);
    }

    @Test
    public void testRunOnFruitAPI_skipLeft_verbose_jdbm() throws Exception {
        testRunOnFruitAPI("verbose-skipLeft-jdbm-", EnumeratorType.JDBM, true,
                false, false);
    }

    @Test
    public void testRunOnFruitAPI_skipRight_verbose_jdbm() throws Exception {
        testRunOnFruitAPI("verbose-skipRight-jdbm-", EnumeratorType.JDBM, false,
                true, false);
    }

    void testRunOnFruitAPI(
            String prefix, EnumeratorType type, boolean skip1, boolean skip2,
            boolean compact) throws Exception {
        System.out.println("Testing " + IndexTPCommandTest.class.getName()
                + " on " + TEST_FRUIT_INPUT);

        final String name = TEST_FRUIT_INPUT.getName();
        final File out = new File(TEST_OUTPUT_DIR, prefix + name + ".indexed");
        File out2 = suffix(out, ".unindexed");
        final File idx1 = new File(TEST_OUTPUT_DIR, name + ".entry-index");
        final File idx2 = new File(TEST_OUTPUT_DIR, name + ".feature-index");

        deleteIfExist(out, idx1, idx2);

        indexTP(TEST_FRUIT_INPUT, out, idx1, idx2, type, skip1, skip2, compact);

        unindexTP(out, out2, idx1, idx2, type, skip1, skip2, compact);

//        TokenPairSource.equal(out, out2, DEFAULT_CHARSET, skip1, skip2);

    }

    public static void indexTP(File from, File to, File index1, File index2,
                               EnumeratorType type, boolean skip1, boolean skip2,
                               boolean compact)
            throws Exception {
        assertValidPlaintextInputFiles(from);
        assertValidOutputFiles(to);
        if (type == EnumeratorType.JDBM)
            assertValidJDBMOutputFiles(index1, index2);
        else
            assertValidOutputFiles(index1, index2);

        IndexingCommands.IndexInstances unindex = new IndexingCommands.IndexInstances();
        unindex.getFilesDelegate().setCharset(TestConstants.DEFAULT_CHARSET);
        unindex.getFilesDelegate().setSourceFile(from);
        unindex.getFilesDelegate().setDestinationFile(to);
        unindex.setIndexDelegate(new DoubleEnumeratingDelegate(type, true,
                true, index1, index2));
        Assert.assertTrue(unindex.runCommand());

        assertValidPlaintextInputFiles(to);

        if (type == EnumeratorType.JDBM)
            assertValidJDBMInputFiles(index1, index2);
        else
            assertValidInputFiles(index1, index2);
        assertSizeGT(from, to);
    }

    private static void unindexTP(File from, File to, File index1, File index2,
                                  EnumeratorType type, boolean skip1,
                                  boolean skip2, boolean compact)
            throws Exception {
        assertValidPlaintextInputFiles(from);
        if (type == EnumeratorType.JDBM)
            assertValidJDBMInputFiles(index1, index2);
        else
            assertValidInputFiles(index1, index2);
        assertValidOutputFiles(to);

        IndexingCommands.IndexInstances unindex =
                new IndexingCommands.IndexInstances();
        unindex.getFilesDelegate().setCharset(DEFAULT_CHARSET);
        unindex.getFilesDelegate().setSourceFile(from);
        unindex.getFilesDelegate().setDestinationFile(to);

        unindex.setIndexDelegate(new DoubleEnumeratingDelegate(type, true,
                true, index1, index2));
        Assert.assertTrue(unindex.runCommand());

        assertValidPlaintextInputFiles(to);
        assertSizeGT(to, from);
    }
}
