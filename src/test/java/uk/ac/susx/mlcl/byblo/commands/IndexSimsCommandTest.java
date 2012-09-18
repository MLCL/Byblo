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

import org.junit.Ignore;
import org.junit.Test;
import uk.ac.susx.mlcl.TestConstants;
import uk.ac.susx.mlcl.byblo.enumerators.*;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSource;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairVectorSource;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import uk.ac.susx.mlcl.lib.commands.AbstractCommandTest;
import uk.ac.susx.mlcl.lib.io.Tell;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.ac.susx.mlcl.TestConstants.*;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class IndexSimsCommandTest extends
        AbstractCommandTest<IndexingCommands.IndexSims> {

    @Override
    public Class<? extends IndexingCommands.IndexSims> getImplementation() {
        return IndexingCommands.IndexSims.class;
    }

    @Test
    public void testRunOnFruitAPI_noSkip_compact() throws Exception {
        testRunOnFruitAPI("compact-noSkip-", EnumeratorType.Memory, false,
                false, true);
    }

    @Test
    public void testRunOnFruitAPI_skipBoth_compact() throws Exception {
        testRunOnFruitAPI("compact-skipBoth-", EnumeratorType.Memory, true,
                true, true);
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
    public void testRunOnFruitAPI_noSkip_verbose() throws Exception {
        testRunOnFruitAPI("verbose-noSkip-", EnumeratorType.Memory, false,
                false, false);
    }

    @Test
    public void testRunOnFruitAPI_skipBoth_verbose() throws Exception {
        testRunOnFruitAPI("verbose-skipBoth-", EnumeratorType.Memory, true,
                true, false);
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
    public void testRunOnFruitAPI_noSkip_compact_JDBM() throws Exception {
        testRunOnFruitAPI("compact-noSkip-jdbm-", EnumeratorType.JDBM, false,
                false, true);
    }

    @Test
    public void testRunOnFruitAPI_skipBoth_compact_JDBM() throws Exception {
        testRunOnFruitAPI("compact-skipBoth-jdbm-", EnumeratorType.JDBM, true,
                true, true);
    }

    @Test
    public void testRunOnFruitAPI_skipLeft_compact_JDBM() throws Exception {
        testRunOnFruitAPI("compact-skipLeft-jdbm-", EnumeratorType.JDBM, true,
                false, true);
    }

    @Test
    public void testRunOnFruitAPI_skipRight_compact_JDBM() throws Exception {
        testRunOnFruitAPI("compact-skipRight-jdbm-", EnumeratorType.JDBM,
                false, true, true);
    }

    @Test
    public void testRunOnFruitAPI_noSkip_verbose_JDBM() throws Exception {
        testRunOnFruitAPI("verbose-noSkip-jdbm-", EnumeratorType.JDBM, false,
                false, false);
    }

    @Test
    public void testRunOnFruitAPI_skipBoth_verbose_JDBM() throws Exception {
        testRunOnFruitAPI("verbose-skipBoth-jdbm-", EnumeratorType.JDBM, true,
                true, false);
    }

    @Test
    public void testRunOnFruitAPI_skipLeft_verbose_JDBM() throws Exception {
        testRunOnFruitAPI("verbose-skipLeft-jdbm-", EnumeratorType.JDBM, true,
                false, false);
    }

    @Test
    public void testRunOnFruitAPI_skipRight_verbose_JDBM() throws Exception {
        testRunOnFruitAPI("verbose-skipRight-jdbm-", EnumeratorType.JDBM,
                false, true, false);
    }

    void testRunOnFruitAPI(String prefix, EnumeratorType type,
                           boolean skip1, boolean skip2, boolean compact) throws Exception {
        System.out.println("Testing " + IndexSimsCommandTest.class.getName()
                + " on " + TEST_FRUIT_SIMS_100NN);

        final String name = TEST_FRUIT_SIMS_100NN.getName();
        final File out = new File(TEST_OUTPUT_DIR, prefix + name + ".indexed");
        File out2 = suffix(out, ".unindexed");
        final File idx = new File(TEST_OUTPUT_DIR, prefix + name
                + ".entry-index");

        deleteIfExist(out, idx);

        indexSims(TEST_FRUIT_SIMS_100NN, out, idx, type, skip1, skip2, compact);

        unindexSims(out, out2, idx, type, skip1, skip2, compact);
    }

    @Test
    @Ignore
    public void testCompareSkipVsnoSkip() throws Exception {
        System.out.println("Testing " + IndexWTPCommandTest.class.getName()
                + " on " + TEST_FRUIT_SIMS);

        final String name = TEST_FRUIT_SIMS.getName();
        String prefixA = "wtp-noSkip-";
        String prefixB = "wtp-skip-";

        final File outA = new File(TEST_OUTPUT_DIR, prefixA + name + ".indexed");
        final File outB = new File(TEST_OUTPUT_DIR, prefixB + name + ".indexed");

        final File idxA = new File(TEST_OUTPUT_DIR, prefixA + name + ".entry-index");
        final File idxB = new File(TEST_OUTPUT_DIR, prefixB + name + ".entry-index");

        boolean skip1a = false;
        boolean skip2a = false;
        boolean skip1b = true;
        boolean skip2b = true;

        deleteIfExist(outA, idxA, outB, idxB);

        indexSims(TEST_FRUIT_SIMS, outA, idxA, EnumeratorType.Memory, skip1a,
                skip2a, true);
        indexSims(TEST_FRUIT_SIMS, outB, idxB, EnumeratorType.Memory, skip1b,
                skip2b, true);

        // Read back the data checking it's identical
        {
            WeightedTokenPairSource wtpsa = WeightedTokenPairSource.open(outA,
                    DEFAULT_CHARSET, new DoubleEnumeratingDelegate(
                    Enumerating.DEFAULT_TYPE, true, true, null, null),
                    skip1a, skip2a);
            WeightedTokenPairSource wtpsb = WeightedTokenPairSource.open(outB,
                    DEFAULT_CHARSET, new DoubleEnumeratingDelegate(
                    Enumerating.DEFAULT_TYPE, true, true, null, null),
                    skip1a, skip2a);
            List<Tell> pa = new ArrayList<Tell>();
            List<Tell> pb = new ArrayList<Tell>();
            List<Weighted<TokenPair>> va = new ArrayList<Weighted<TokenPair>>();
            List<Weighted<TokenPair>> vb = new ArrayList<Weighted<TokenPair>>();

            // sequential
            while (wtpsa.hasNext() && wtpsb.hasNext()) {
                pa.add(wtpsa.position());
                pb.add(wtpsb.position());
                Weighted<TokenPair> a = wtpsa.read();
                Weighted<TokenPair> b = wtpsb.read();
                va.add(a);
                vb.add(b);
                assertEquals(a, b);
            }
            assertTrue(!wtpsa.hasNext());
            assertTrue(!wtpsb.hasNext());

            // random
            Random rand = new Random(0);
            for (int i = 0; i < 1000; i++) {
                int j = rand.nextInt(pa.size());
                wtpsa.position(pa.get(j));
                wtpsb.position(pb.get(j));
                Weighted<TokenPair> a = wtpsa.read();
                Weighted<TokenPair> b = wtpsb.read();

                assertEquals(va.get(j), a);
                assertEquals(vb.get(j), b);
                assertEquals(a, b);
            }
        }

        // Read back the data again, this time as vectors
        {
            WeightedTokenPairVectorSource wtpsa = new WeightedTokenPairVectorSource(WeightedTokenPairSource.open(
                    outA, DEFAULT_CHARSET,
                    new DoubleEnumeratingDelegate(Enumerating.DEFAULT_TYPE, true, true, null, null), skip1a, skip2a));
            WeightedTokenPairVectorSource wtpsb = new WeightedTokenPairVectorSource(WeightedTokenPairSource.open(
                    outB, DEFAULT_CHARSET,
                    new DoubleEnumeratingDelegate(Enumerating.DEFAULT_TYPE, true, true, null, null), skip1b, skip2b));

            List<Tell> pa = new ArrayList<Tell>();
            List<Tell> pb = new ArrayList<Tell>();
            List<Indexed<SparseDoubleVector>> va = new ArrayList<Indexed<SparseDoubleVector>>();
            List<Indexed<SparseDoubleVector>> vb = new ArrayList<Indexed<SparseDoubleVector>>();

            // sequential
            while (wtpsa.hasNext() && wtpsb.hasNext()) {
                pa.add(wtpsa.position());
                pb.add(wtpsb.position());

                Indexed<SparseDoubleVector> a = wtpsa.read();
                Indexed<SparseDoubleVector> b = wtpsb.read();
                va.add(a);
                vb.add(b);
                assertEquals(a, b);
            }
            assertTrue(!wtpsa.hasNext());
            assertTrue(!wtpsb.hasNext());

            // random
            Random rand = new Random(0);
            for (int i = 0; i < 1000; i++) {
                int j = rand.nextInt(pa.size());
                wtpsa.position(pa.get(j));
                wtpsb.position(pb.get(j));
                Indexed<SparseDoubleVector> a = wtpsa.read();
                Indexed<SparseDoubleVector> b = wtpsb.read();

                assertEquals(va.get(j), a);
                assertEquals(va.get(j).value(), a.value());
                assertEquals(vb.get(j), b);
                assertEquals(vb.get(j).value(), b.value());
                assertEquals(a, b);
                assertEquals(a.value(), b.value());
            }
        }
    }

    private static void indexSims(File from, File to, File index,
                                  EnumeratorType type, boolean skip1, boolean skip2, boolean compact)
            throws Exception {
        assertValidPlaintextInputFiles(from);
        assertValidOutputFiles(to);

        if (type == EnumeratorType.JDBM)
            TestConstants.assertValidJDBMOutputFiles(index);
        else
            assertValidOutputFiles(index);

        IndexingCommands.IndexSims indexCommand = new IndexingCommands.IndexSims();
        indexCommand.getFilesDelegate().setCharset(DEFAULT_CHARSET);
        indexCommand.getFilesDelegate().setSourceFile(from);
        indexCommand.getFilesDelegate().setDestinationFile(to);

        indexCommand.setIndexDelegate(EnumeratingDelegates
                .toPair(new SingleEnumeratingDelegate(type, true, index)));
        assertTrue(indexCommand.runCommand());

        assertValidPlaintextInputFiles(to);
        assertSizeGT(from, to);

        if (type == EnumeratorType.JDBM)
            TestConstants.assertValidJDBMInputFiles(index);
        else
            assertValidInputFiles(index);
    }

    public static void unindexSims(File from, File to, File index,
                                   EnumeratorType type, boolean skip1, boolean skip2, boolean compact)
            throws Exception {
        assertValidPlaintextInputFiles(from);

        if (type == EnumeratorType.JDBM)
            TestConstants.assertValidJDBMInputFiles(index);
        else
            assertValidInputFiles(index);
        assertValidOutputFiles(to);

        IndexingCommands.IndexSims unindex = new IndexingCommands.IndexSims();
        unindex.getFilesDelegate().setCharset(DEFAULT_CHARSET);
        unindex.getFilesDelegate().setSourceFile(from);
        unindex.getFilesDelegate().setDestinationFile(to);
        unindex.setIndexDelegate(EnumeratingDelegates
                .toPair(new SingleEnumeratingDelegate(type, true, index)));
        assertTrue(unindex.runCommand());

        assertValidPlaintextInputFiles(to);
        // assertSizeGT(to, from);
    }

}
