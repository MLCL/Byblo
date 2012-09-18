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
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratorType;
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
public class IndexWTPCommandTest extends
        AbstractCommandTest<IndexingCommands.IndexEvents> {

    @Override
    public Class<? extends IndexingCommands.IndexEvents> getImplementation() {
        return IndexingCommands.IndexEvents.class;
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
    public void testRunOnFruitAPI_noSkip_compact_jdbm() throws Exception {
        testRunOnFruitAPI("compact-noSkip-jdbm-", EnumeratorType.JDBM, false,
                false, true);
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
        testRunOnFruitAPI("compact-skipRight-jdbm-", EnumeratorType.JDBM,
                false, true, true);
    }

    @Test
    public void testRunOnFruitAPI_noSkip_verbose_jdbm() throws Exception {
        testRunOnFruitAPI("verbose-noSkip-jdbm-", EnumeratorType.JDBM, false,
                false, false);
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
        testRunOnFruitAPI("verbose-skipRight-jdbm-", EnumeratorType.JDBM,
                false, true, false);
    }

    void testRunOnFruitAPI(String prefix, EnumeratorType type,
                           boolean skip1, boolean skip2, boolean compact) throws Exception {
        System.out.println("Testing " + IndexWTPCommandTest.class.getName()
                + " on " + TEST_FRUIT_EVENTS);

        final String name = TEST_FRUIT_EVENTS.getName();
        final File out = new File(TEST_OUTPUT_DIR, prefix + name + ".indexed");
        File out2 = suffix(out, ".unindexed");
        final File idx1 = new File(TEST_OUTPUT_DIR, name + ".entry-index");
        final File idx2 = new File(TEST_OUTPUT_DIR, name + ".feature-index");

        deleteIfExist(out);
        TestConstants.deleteJDBMIfExist(idx1, idx2);

        indexWTP(TEST_FRUIT_EVENTS, out, idx1, idx2, type, skip1, skip2,
                compact);

        unindexWTP(out, out2, idx1, idx2, type, skip1, skip2, compact);

        // TokenPairSource.equal(out, out2, DEFAULT_CHARSET, skip1, skip2);

    }

    @Test
    @Ignore
    public void testCompareSkipVsnoSkip() throws Exception {
        System.out.println("Testing " + IndexWTPCommandTest.class.getName()
                + " on " + TEST_FRUIT_EVENTS);

        final String name = TEST_FRUIT_EVENTS.getName();
        String prefixA = "wtp-noSkip-";
        String prefixB = "wtp-skip-";

        final File outA = new File(TEST_OUTPUT_DIR, prefixA + name + ".indexed");
        final File outB = new File(TEST_OUTPUT_DIR, prefixB + name + ".indexed");

        final File idx1A = new File(TEST_OUTPUT_DIR, prefixA + name + ".entry-index");
        final File idx2A = new File(TEST_OUTPUT_DIR, prefixA + name + ".feature-index");
        final File idx1B = new File(TEST_OUTPUT_DIR, prefixB + name + ".entry-index");
        final File idx2B = new File(TEST_OUTPUT_DIR, prefixB + name + ".feature-index");

        boolean skip1a = false;
        boolean skip2a = false;
        boolean skip1b = true;
        boolean skip2b = true;

        deleteIfExist(outA, idx1A, idx2A, outB, idx1B, idx2B);

        indexWTP(TEST_FRUIT_EVENTS, outA, idx1A, idx2A, EnumeratorType.Memory,
                skip1a, skip2a, true);
        indexWTP(TEST_FRUIT_EVENTS, outB, idx1B, idx2B, EnumeratorType.Memory,
                skip1b, skip2b, true);

        // Read back the data checking it's identical
        {
            WeightedTokenPairSource tokenSourceA = WeightedTokenPairSource.open(outA,
                    DEFAULT_CHARSET, new DoubleEnumeratingDelegate(
                    Enumerating.DEFAULT_TYPE, true, true, null, null),
                    skip1a, skip2a);
            WeightedTokenPairSource tokenSourceB = WeightedTokenPairSource.open(outB,
                    DEFAULT_CHARSET, new DoubleEnumeratingDelegate(
                    Enumerating.DEFAULT_TYPE, true, true, null, null),
                    skip1b, skip2b);
            List<Tell> pa = new ArrayList<Tell>();
            List<Tell> pb = new ArrayList<Tell>();
            List<Weighted<TokenPair>> va = new ArrayList<Weighted<TokenPair>>();
            List<Weighted<TokenPair>> vb = new ArrayList<Weighted<TokenPair>>();

            // sequential
            while (tokenSourceA.hasNext() && tokenSourceB.hasNext()) {
                pa.add(tokenSourceA.position());
                pb.add(tokenSourceB.position());
                Weighted<TokenPair> a = tokenSourceA.read();
                Weighted<TokenPair> b = tokenSourceB.read();
                va.add(a);
                vb.add(b);
                assertEquals(a, b);
            }
            assertTrue(!tokenSourceA.hasNext());
            assertTrue(!tokenSourceB.hasNext());

            // random
            Random rand = new Random(0);
            for (int i = 0; i < 1000; i++) {
                int j = rand.nextInt(pa.size());
                tokenSourceA.position(pa.get(j));
                tokenSourceB.position(pb.get(j));
                Weighted<TokenPair> a = tokenSourceA.read();
                Weighted<TokenPair> b = tokenSourceB.read();

                assertEquals(va.get(j), a);
                assertEquals(vb.get(j), b);
                assertEquals(a, b);
            }
        }

        // Read back the data again, this time as vectors
        {
            WeightedTokenPairVectorSource vectorSourceA = new WeightedTokenPairVectorSource(WeightedTokenPairSource.open(
                    outA,
                    DEFAULT_CHARSET,
                    new DoubleEnumeratingDelegate(Enumerating.DEFAULT_TYPE,
                            true, true, null, null), skip1a, skip2a));
            WeightedTokenPairVectorSource vectorSourceB = new WeightedTokenPairVectorSource(WeightedTokenPairSource.open(
                    outB,
                    DEFAULT_CHARSET,
                    new DoubleEnumeratingDelegate(Enumerating.DEFAULT_TYPE,
                            true, true, null, null), skip1b, skip2b));

            List<Tell> pa = new ArrayList<Tell>();
            List<Tell> pb = new ArrayList<Tell>();
            List<Indexed<SparseDoubleVector>> va = new ArrayList<Indexed<SparseDoubleVector>>();
            List<Indexed<SparseDoubleVector>> vb = new ArrayList<Indexed<SparseDoubleVector>>();

            // sequential
            while (vectorSourceA.hasNext() && vectorSourceB.hasNext()) {
                pa.add(vectorSourceA.position());
                pb.add(vectorSourceB.position());

                Indexed<SparseDoubleVector> a = vectorSourceA.read();
                Indexed<SparseDoubleVector> b = vectorSourceB.read();
                va.add(a);
                vb.add(b);
                assertEquals(a, b);
            }
            assertTrue(!vectorSourceA.hasNext());
            assertTrue(!vectorSourceB.hasNext());

            // random
            Random rand = new Random(0);
            for (int i = 0; i < 1000; i++) {
                int j = rand.nextInt(pa.size());
                vectorSourceA.position(pa.get(j));
                vectorSourceB.position(pb.get(j));
                Indexed<SparseDoubleVector> a = vectorSourceA.read();
                Indexed<SparseDoubleVector> b = vectorSourceB.read();

                assertEquals(va.get(j), a);
                assertEquals(va.get(j).value(), a.value());
                assertEquals(vb.get(j), b);
                assertEquals(vb.get(j).value(), b.value());
                assertEquals(a, b);
                assertEquals(a.value(), b.value());
            }
        }
    }

    private static void indexWTP(File from, File to, File index1, File index2,
                                 EnumeratorType type, boolean skip1, boolean skip2, boolean compact)
            throws Exception {
        assertValidPlaintextInputFiles(from);
        assertValidOutputFiles(to);
        if (type == EnumeratorType.JDBM)
            TestConstants.assertValidJDBMOutputFiles(index1, index2);
        else
            assertValidOutputFiles(index1, index2);

        IndexingCommands.IndexEvents unindex = new IndexingCommands.IndexEvents();
        unindex.getFilesDelegate().setCharset(DEFAULT_CHARSET);
        unindex.getFilesDelegate().setSourceFile(from);
        unindex.getFilesDelegate().setDestinationFile(to);
        unindex.setIndexDelegate(new DoubleEnumeratingDelegate(type, true,
                true, index1, index2));
        assertTrue(unindex.runCommand());

        assertValidPlaintextInputFiles(to);
        if (type == EnumeratorType.JDBM)
            TestConstants.assertValidJDBMInputFiles(index1, index2);
        else
            assertValidInputFiles(index1, index2);
        assertSizeGT(from, to);
    }

    private static void unindexWTP(File from, File to, File index1, File index2,
                                   EnumeratorType type, boolean skip1, boolean skip2, boolean compact)
            throws Exception {
        assertValidPlaintextInputFiles(from);

        if (type == EnumeratorType.JDBM)
            TestConstants.assertValidJDBMInputFiles(index1, index2);
        else
            assertValidInputFiles(index1, index2);
        assertValidOutputFiles(to);

        IndexingCommands.UnindexEvents unindex = new IndexingCommands.UnindexEvents();
        unindex.getFilesDelegate().setCharset(DEFAULT_CHARSET);
        unindex.getFilesDelegate().setSourceFile(from);
        unindex.getFilesDelegate().setDestinationFile(to);
        unindex.setIndexDelegate(new DoubleEnumeratingDelegate(type, true,
                true, index1, index2));
        assertTrue(unindex.runCommand());

        assertValidPlaintextInputFiles(to);
        assertSizeGT(to, from);
    }

}
