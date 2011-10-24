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
package uk.ac.susx.mlcl.byblo.io;

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

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class WeightedEntryFeatureTest {

    private void copyWEF(File a, File b, boolean compact) throws FileNotFoundException, IOException {
        WeightedEntryFeatureSource aSrc = new WeightedEntryFeatureSource(a,
                DEFAULT_CHARSET);
        WeightedEntryFeatureSink bSink = new WeightedEntryFeatureSink(b,
                DEFAULT_CHARSET,
                aSrc.getEntryIndex(), aSrc.getFeatureIndex());
        bSink.setCompactFormatEnabled(compact);

        copy(aSrc, bSink);
        bSink.close();
    }

    private void copyWEFV(File a, File b, boolean compact) throws FileNotFoundException, IOException {
        WeightedEntryFeatureVectorSource aSrc = new WeightedEntryFeatureVectorSource(
                new WeightedEntryFeatureSource(a, DEFAULT_CHARSET));

        List<Indexed<SparseDoubleVector>> list = readAll(aSrc);
        Collections.sort(list);

        WeightedEntryFeatureSink tmp = new WeightedEntryFeatureSink(b,
                DEFAULT_CHARSET, aSrc.getEntryIndex(), aSrc.getFeatureIndex());
        tmp.setCompactFormatEnabled(compact);



        WeightedEntryFeatureVectorSink bSink = new WeightedEntryFeatureVectorSink(
                tmp);

        copy(list, bSink);

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
        WeightedEntryFeatureSource efSrc = new WeightedEntryFeatureSource(
                testSample, charset);
        assertTrue("EntryFeatureSource is empty", efSrc.hasNext());

        while (efSrc.hasNext()) {
            Weighted<EntryFeature> ef = efSrc.read();
            assertNotNull("Found null EntryFeatureRecord", ef);
        }
    }

    public void testRandomAccess(File file) throws FileNotFoundException, IOException {
        final Map<Tell, Weighted<EntryFeature>> hist =
                new HashMap<Tell, Weighted<EntryFeature>>();

        WeightedEntryFeatureSource src =
                new WeightedEntryFeatureSource(file, DEFAULT_CHARSET);
        {
            while (src.hasNext()) {
                final Tell pos = src.position();
                final Weighted<EntryFeature> record = src.read();
                assertNotNull("Found null EntryFeatureRecord", record);
                hist.put(pos, record);
            }
        }

        {
            List<Tell> positions = new ArrayList<Tell>(hist.keySet());
            Random rand = new Random(0);

            for (int i = 0; i < 10; i++) {
                final Tell pos = positions.get(rand.nextInt(positions.size()));
                final Weighted<EntryFeature> expected = hist.get(pos);

                System.out.println("expected tell: " + pos);
                System.out.println("expected: " + expected.get().toString(src.
                        getEntryIndex(), src.getFeatureIndex()));

                src.position(pos);

                assertEquals(pos, src.position());
                assertTrue(src.hasNext());

                Weighted<EntryFeature> actual = src.read();
                System.out.println("actual tell: " + src.position());
                System.out.println("actual: " + actual.get().toString(src.
                        getEntryIndex(), src.getFeatureIndex()));

                assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void testRandomAccess() throws FileNotFoundException, IOException {
        testRandomAccess(TEST_FRUIT_ENTRY_FEATURES);
    }
}
