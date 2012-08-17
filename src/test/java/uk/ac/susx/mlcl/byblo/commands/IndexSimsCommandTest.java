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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratingDeligates;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratorType;
import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSource;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairVectorSource;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import uk.ac.susx.mlcl.lib.io.Tell;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class IndexSimsCommandTest {

    public IndexSimsCommandTest() {
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
    public void testRunOnFruitAPI_noskip_compact() throws Exception {
        testRunOnFruitAPI("compact-noskip-", EnumeratorType.Memory, false, false,
                          true);
    }

    @Test
    public void testRunOnFruitAPI_skipboth_compact() throws Exception {
        testRunOnFruitAPI("compact-skipboth-", EnumeratorType.Memory, true, true,
                          true);
    }

    @Test
    public void testRunOnFruitAPI_skipleft_compact() throws Exception {
        testRunOnFruitAPI("compact-skipleft-", EnumeratorType.Memory, true,
                          false, true);
    }

    @Test
    public void testRunOnFruitAPI_skipright_compact() throws Exception {
        testRunOnFruitAPI("compact-skipright-", EnumeratorType.Memory, false,
                          true, true);
    }

    @Test
    public void testRunOnFruitAPI_noskip_verbose() throws Exception {
        testRunOnFruitAPI("verbose-noskip-", EnumeratorType.Memory, false, false,
                          false);
    }

    @Test
    public void testRunOnFruitAPI_skipboth_verbose() throws Exception {
        testRunOnFruitAPI("verbose-skipboth-", EnumeratorType.Memory, true, true,
                          false);
    }

    @Test
    public void testRunOnFruitAPI_skipleft_verbose() throws Exception {
        testRunOnFruitAPI("verbose-skipleft-", EnumeratorType.Memory, true,
                          false, false);
    }

    @Test
    public void testRunOnFruitAPI_skipright_verbose() throws Exception {
        testRunOnFruitAPI("verbose-skipright-", EnumeratorType.Memory, false,
                          true, false);
    }

    @Test
    public void testRunOnFruitAPI_noskip_compact_jdbm() throws Exception {
        testRunOnFruitAPI("compact-noskip-jdbm-", EnumeratorType.JDBM, false,
                          false, true);
    }

    @Test
    public void testRunOnFruitAPI_skipboth_compact_jdbm() throws Exception {
        testRunOnFruitAPI("compact-skipboth-jdbm-", EnumeratorType.JDBM, true,
                          true, true);
    }

    @Test
    public void testRunOnFruitAPI_skipleft_compact_jdbm() throws Exception {
        testRunOnFruitAPI("compact-skipleft-jdbm-", EnumeratorType.JDBM, true,
                          false, true);
    }

    @Test
    public void testRunOnFruitAPI_skipright_compact_jdbm() throws Exception {
        testRunOnFruitAPI("compact-skipright-jdbm-", EnumeratorType.JDBM, false,
                          true, true);
    }

    @Test
    public void testRunOnFruitAPI_noskip_verbose_jdbm() throws Exception {
        testRunOnFruitAPI("verbose-noskip-jdbm-",
                          EnumeratorType.JDBM, false, false, false);
    }

    @Test
    public void testRunOnFruitAPI_skipboth_verbose_jdbm() throws Exception {
        testRunOnFruitAPI("verbose-skipboth-jdbm-",
                          EnumeratorType.JDBM, true, true, false);
    }

    @Test
    public void testRunOnFruitAPI_skipleft_verbose_jdbm() throws Exception {
        testRunOnFruitAPI("verbose-skipleft-jdbm-",
                          EnumeratorType.JDBM, true, false, false);
    }

    @Test
    public void testRunOnFruitAPI_skipright_verbose_jdbm() throws Exception {
        testRunOnFruitAPI("verbose-skipright-jdbm-",
                          EnumeratorType.JDBM, false, true, false);
    }

    public void testRunOnFruitAPI(
            String prefix, EnumeratorType type, boolean skip1, boolean skip2,
            boolean compact) throws Exception {
        System.out.println("Testing " + IndexSimsCommandTest.class.getName()
                + " on " + TEST_FRUIT_SIMS_100NN);

        final String name = TEST_FRUIT_SIMS_100NN.getName();
        final File out = new File(TEST_OUTPUT_DIR, prefix + name + ".indexed");
        File out2 = suffix(out, ".unindexed");
        final File idx = new File(TEST_OUTPUT_DIR,
                                  prefix + name + ".entry-index");

        deleteIfExist(out, idx);

        indexSims(TEST_FRUIT_SIMS_100NN, out, idx, type, skip1, skip2,
                  compact);

        unindexSims(out, out2, idx, type, skip1, skip2, compact);
    }

    @Test
    @Ignore
    public void testCompareSkipVsNoSkip() throws Exception {
        System.out.println("Testing " + IndexWTPCommandTest.class.getName()
                + " on " + TEST_FRUIT_SIMS);


        final String name = TEST_FRUIT_SIMS.getName();
        String prefixa = "wtp-noskip-";
        String prefixb = "wtp-skip-";

        final File outa = new File(TEST_OUTPUT_DIR, prefixa + name + ".indexed");
        final File outb = new File(TEST_OUTPUT_DIR, prefixb + name + ".indexed");

        final File idxa = new File(TEST_OUTPUT_DIR,
                                   prefixa + name + ".entry-index");
        final File idxb = new File(TEST_OUTPUT_DIR,
                                   prefixb + name + ".entry-index");

        boolean skip1a = false;
        boolean skip2a = false;
        boolean skip1b = true;
        boolean skip2b = true;

        deleteIfExist(outa, idxa, outb, idxb);

        indexSims(TEST_FRUIT_SIMS, outa, idxa, EnumeratorType.Memory, skip1a,
                  skip2a, true);
        indexSims(TEST_FRUIT_SIMS, outb, idxb, EnumeratorType.Memory, skip1b,
                  skip2b, true);

        // Read back the data checking it's identical
        {
            WeightedTokenPairSource wtpsa = WeightedTokenPairSource.open(
                    outa, DEFAULT_CHARSET,
                    new DoubleEnumeratingDeligate(Enumerating.DEFAULT_TYPE, true,
                                                  true, null, null), skip1a,
                    skip2a);
            WeightedTokenPairSource wtpsb = WeightedTokenPairSource.open(
                    outb, DEFAULT_CHARSET,
                    new DoubleEnumeratingDeligate(Enumerating.DEFAULT_TYPE, true,
                                                  true, null, null), skip1a,
                    skip2a);
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
            WeightedTokenPairVectorSource wtpsa = WeightedTokenPairSource.open(
                    outa, DEFAULT_CHARSET,
                    new DoubleEnumeratingDeligate(Enumerating.DEFAULT_TYPE,
                                                  true, true, null, null),
                    skip1a, skip2a).
                    getVectorSource();
            WeightedTokenPairVectorSource wtpsb = WeightedTokenPairSource.open(
                    outb, DEFAULT_CHARSET,
                    new DoubleEnumeratingDeligate(Enumerating.DEFAULT_TYPE,
                                                  true, true, null, null),
                    skip1b, skip2b).
                    getVectorSource();

            List<Tell> pa = new ArrayList<Tell>();
            List<Tell> pb = new ArrayList<Tell>();
            List<Indexed<SparseDoubleVector>> va =
                    new ArrayList<Indexed<SparseDoubleVector>>();
            List<Indexed<SparseDoubleVector>> vb =
                    new ArrayList<Indexed<SparseDoubleVector>>();

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

    public static void indexSims(File from, File to, File index,
                                 EnumeratorType type, boolean skip1,
                                 boolean skip2, boolean compact)
            throws Exception {
        assertValidPlaintextInputFiles(from);
        assertValidOutputFiles(to);

        if (type == EnumeratorType.JDBM)
            assertValidJDBMOutputFiles(index);
        else
            assertValidOutputFiles(index);

        IndexingCommands.IndexSims indexCommand =
                new IndexingCommands.IndexSims();
        indexCommand.getFilesDeligate().setCharset(DEFAULT_CHARSET);
        indexCommand.getFilesDeligate().setSourceFile(from);
        indexCommand.getFilesDeligate().setDestinationFile(to);

        indexCommand.setIndexDeligate(EnumeratingDeligates.toPair(
                new SingleEnumeratingDeligate(type, true, index)));
        indexCommand.runCommand();

        assertValidPlaintextInputFiles(to);
        assertSizeGT(from, to);

        if (type == EnumeratorType.JDBM)
            assertValidJDBMInputFiles(index);
        else
            assertValidInputFiles(index);
    }

    public static void unindexSims(
            File from, File to, File index,
            EnumeratorType type, boolean skip1, boolean skip2, boolean compact)
            throws Exception {
        assertValidPlaintextInputFiles(from);

        if (type == EnumeratorType.JDBM)
            assertValidJDBMInputFiles(index);
        else
            assertValidInputFiles(index);
        assertValidOutputFiles(to);

        IndexingCommands.IndexSims unindex = new IndexingCommands.IndexSims();
        unindex.getFilesDeligate().setCharset(DEFAULT_CHARSET);
        unindex.getFilesDeligate().setSourceFile(from);
        unindex.getFilesDeligate().setDestinationFile(to);
        unindex.setIndexDeligate(EnumeratingDeligates.
                toPair(new SingleEnumeratingDeligate(
                type, true, index)));
        unindex.runCommand();

        assertValidPlaintextInputFiles(to);
        assertSizeGT(to, from);
    }
}
