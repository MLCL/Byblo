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
package uk.ac.susx.mlcl.byblo.io;

import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import java.util.Random;
import java.util.ArrayList;
import java.util.Map;
import org.junit.Ignore;
import java.nio.charset.Charset;
import java.util.List;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import org.junit.Test;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import uk.ac.susx.mlcl.lib.io.Lexer.Tell;
import static org.junit.Assert.*;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.SimpleEnumerator;
import uk.ac.susx.mlcl.lib.io.TSVSink;
import uk.ac.susx.mlcl.lib.io.TSVSource;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class WeightedEntryFeatureTest {

    private void copyWEF(File a, File b, boolean compact) throws FileNotFoundException, IOException {
        Enumerator<String> idx = new SimpleEnumerator<String>();
        WeightedTokenPairSource aSrc = new WeightedTokenPairSource(
                new TSVSource(a, DEFAULT_CHARSET),
                Token.stringDecoder(idx),
                Token.stringDecoder(idx));
        WeightedTokenPairSink bSink = new WeightedTokenPairSink(
                new TSVSink(b, DEFAULT_CHARSET),
                Token.stringEncoder(idx), Token.stringEncoder(idx));
        bSink.setCompactFormatEnabled(compact);

        IOUtil.copy(aSrc, bSink);
        bSink.close();
    }

    private void copyWEFV(File a, File b, boolean compact) throws FileNotFoundException, IOException {
        Enumerator<String> idx = new SimpleEnumerator<String>();
        WeightedTokenPairVectorSource aSrc = new WeightedTokenPairVectorSource(
                new WeightedTokenPairSource(
                new TSVSource(a, DEFAULT_CHARSET),
                Token.stringDecoder(idx),
                Token.stringDecoder(idx)));

        List<Indexed<SparseDoubleVector>> list = IOUtil.readAll(aSrc);
        Collections.sort(list);

        WeightedTokenPairSink tmp = new WeightedTokenPairSink(
                new TSVSink(b, DEFAULT_CHARSET),
                Token.stringEncoder(idx),
                Token.stringEncoder(idx));
        tmp.setCompactFormatEnabled(compact);

        WeightedTokenPairVectorSink bSink = new WeightedTokenPairVectorSink(
                tmp);

        IOUtil.copy(list, bSink);

        bSink.close();
    }

    @Test
    public void testWeightedEntryFeaturesConversion() throws FileNotFoundException, IOException {
        File a = TEST_FRUIT_ENTRY_FEATURES;
        File b = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_ENTRY_FEATURES.getName() + ".compact");
        File c = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_ENTRY_FEATURES.getName() + ".verbose");

        copyWEF(a, b, true);

        assertTrue("Compact copy is smaller that verbose source.",
                   b.length() <= a.length());

        copyWEF(b, c, false);


        assertTrue("Verbose copy is smaller that compact source.",
                   c.length() >= b.length());
        assertTrue("Double converted file is not equal to origion.",
                   Files.equal(a, c));
    }

    @Test
    @Ignore
    public void testWeightedEntryFeatureVectorsConversion() throws FileNotFoundException, IOException {
        File a = TEST_FRUIT_ENTRY_FEATURES;
        File b = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_ENTRY_FEATURES.getName() + ".vecs.compact");
        File c = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_ENTRY_FEATURES.getName() + ".vecs.verbose");

        copyWEFV(a, b, true);

        assertTrue("Compact copy is smaller that verbose source.",
                   b.length() <= a.length());

        copyWEFV(b, c, false);


        assertTrue("Verbose copy is smaller that compact source.",
                   c.length() >= b.length());




        assertTrue("Double converted file is not equal to origion.",
                   Files.equal(a, c));
    }

    @Test
    public void testLMSample() throws FileNotFoundException, IOException {
        File testSample = new File(TEST_DATA_DIR, "lm-medline-ef-sample");
        Charset charset = Charset.forName("UTF-8");
        Enumerator<String> idx = new SimpleEnumerator<String>();
        WeightedTokenPairSource efSrc = new WeightedTokenPairSource(
                new TSVSource(testSample, charset), Token.stringDecoder(idx), Token.stringDecoder(idx));
        assertTrue("EntryFeatureSource is empty", efSrc.hasNext());

        while (efSrc.hasNext()) {
            Weighted<TokenPair> ef = efSrc.read();
            assertNotNull("Found null EntryFeatureRecord", ef);
        }
    }

    public void testRandomAccess(File file) throws FileNotFoundException, IOException {
        final Map<Tell, Weighted<TokenPair>> hist =
                new HashMap<Tell, Weighted<TokenPair>>();

        Enumerator<String> idx = new SimpleEnumerator<String>();
        WeightedTokenPairSource src =
                new WeightedTokenPairSource(
                new TSVSource(file, DEFAULT_CHARSET), Token.stringDecoder(idx), Token.stringDecoder(idx));
        {
            while (src.hasNext()) {
                final Tell pos = src.position();
                final Weighted<TokenPair> record = src.read();
                assertNotNull("Found null EntryFeatureRecord", record);
                hist.put(pos, record);
            }
        }

        {
            List<Tell> positions = new ArrayList<Tell>(hist.keySet());
            Random rand = new Random(0);

            for (int i = 0; i < 10; i++) {
                final Tell pos = positions.get(rand.nextInt(positions.size()));
                final Weighted<TokenPair> expected = hist.get(pos);

                System.out.println("expected tell: " + pos);
                System.out.println("expected: " + expected.record().toString(idx,idx));

                src.position(pos);

                assertEquals(pos, src.position());
                assertTrue(src.hasNext());

                Weighted<TokenPair> actual = src.read();
                System.out.println("actual tell: " + src.position());
                System.out.println("actual: " + actual.record().toString(idx,idx));

                assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void testRandomAccess() throws FileNotFoundException, IOException {
        testRandomAccess(TEST_FRUIT_ENTRY_FEATURES);
    }

    @Test
    public void testEntryFeaturePairEnumeratorConversion() throws FileNotFoundException, IOException {
        File a = TEST_FRUIT_ENTRY_FEATURES;
        File b = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_ENTRY_FEATURES.getName() + ".enum");
        File c = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_ENTRY_FEATURES.getName() + ".str");

        Enumerator<String> idx = new SimpleEnumerator<String>();

        {
            WeightedTokenPairSource aSrc = new WeightedTokenPairSource(
                    new TSVSource(a, DEFAULT_CHARSET), 
                    Token.stringDecoder(idx), Token.stringDecoder(idx));
            WeightedTokenPairSink bSink = new WeightedTokenPairSink(new TSVSink(b, DEFAULT_CHARSET));
            IOUtil.copy(aSrc, bSink);
            bSink.close();
        }

        assertTrue("Compact copy is smaller that verbose source.",
                   b.length() <= a.length());

        {
            WeightedTokenPairSource bSrc = new WeightedTokenPairSource(
                    new TSVSource(b, DEFAULT_CHARSET));
            WeightedTokenPairSink cSink = new WeightedTokenPairSink(
                    new TSVSink(c, DEFAULT_CHARSET),
                    Token.stringEncoder(idx), Token.stringEncoder(idx));
            IOUtil.copy(bSrc, cSink);
            cSink.close();
        }

        assertTrue("Verbose copy is smaller that compact source.",
                   c.length() >= b.length());
        assertTrue("Double converted file is not equal to origion.",
                   Files.equal(a, c));
    }

    @Test
    public void testEntryFeaturePairCompactEnumeratorConversion() throws FileNotFoundException, IOException {
        File a = TEST_FRUIT_ENTRY_FEATURES;
        File b = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_ENTRY_FEATURES.getName() + ".enum.compact");
        File c = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_ENTRY_FEATURES.getName() + ".enum.compact.str");

        Enumerator<String> idx = new SimpleEnumerator<String>();

        {
            WeightedTokenPairSource aSrc = new WeightedTokenPairSource(new TSVSource(a, DEFAULT_CHARSET), 
                    Token.stringDecoder(idx),Token.stringDecoder(idx));
            WeightedTokenPairSink bSink = new WeightedTokenPairSink(new TSVSink(b, DEFAULT_CHARSET));
            bSink.setCompactFormatEnabled(true);
            IOUtil.copy(aSrc, bSink);
            bSink.close();
        }

        assertTrue("Compact copy is smaller that verbose source.",
                   b.length() <= a.length());

        {
            WeightedTokenPairSource bSrc = new WeightedTokenPairSource(new TSVSource(b, DEFAULT_CHARSET));
            WeightedTokenPairSink cSink = new WeightedTokenPairSink(
                    new TSVSink(c, DEFAULT_CHARSET), Token.stringEncoder(idx), Token.stringEncoder(idx));
            IOUtil.copy(bSrc, cSink);
            cSink.close();
        }

        assertTrue("Verbose copy is smaller that compact source.",
                   c.length() >= b.length());
        assertTrue("Double converted file is not equal to origion.",
                   Files.equal(a, c));
    }

}
