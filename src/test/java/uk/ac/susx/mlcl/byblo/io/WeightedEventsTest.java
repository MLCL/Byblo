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

import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerator;
import uk.ac.susx.mlcl.byblo.enumerators.MemoryBasedStringEnumerator;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import uk.ac.susx.mlcl.lib.io.ObjectIO;
import uk.ac.susx.mlcl.lib.io.Tell;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class WeightedEventsTest {

    private void copyWEF(File a, File b, boolean compact) throws FileNotFoundException, IOException {
        DoubleEnumeratingDeligate del = new DoubleEnumeratingDeligate(
                Enumerating.DEFAULT_TYPE, false, false, null, null);

        WeightedTokenPairSource aSrc = WeightedTokenPairSource.open(
                a, DEFAULT_CHARSET, del, false, false);
        WeightedTokenPairSink bSink = WeightedTokenPairSink.open(
                b, DEFAULT_CHARSET, del, false, false, compact);

        ObjectIO.copy(aSrc, bSink);
        bSink.close();
    }

    private void copyWEFV(File a, File b, boolean compact) throws FileNotFoundException, IOException {
        DoubleEnumeratingDeligate del = new DoubleEnumeratingDeligate(
                Enumerating.DEFAULT_TYPE, false, false, null, null);

        WeightedTokenPairVectorSource aSrc = new WeightedTokenPairVectorSource(
                WeightedTokenPairSource.open(
                a, DEFAULT_CHARSET, del, false, false));

        List<Indexed<SparseDoubleVector>> list = ObjectIO.readAll(aSrc);
        Collections.sort(list);

        FastWeightedTokenPairVectorSink bSink = FastWeightedTokenPairVectorSink.open(
                b, DEFAULT_CHARSET, del, false, false, compact);

//        WeightedTokenPairVectorSink bSink = new WeightedTokenPairVectorSink(
//                tmp);

        ObjectIO.copy(list, bSink);

        bSink.close();
    }

    @Test
    public void testWeightedEventsConversion() throws FileNotFoundException, IOException {
        File a = TEST_FRUIT_EVENTS;
        File b = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_EVENTS.getName() + ".compact");
        File c = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_EVENTS.getName() + ".verbose");

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
    public void testWeightedEventsVectorsConversion() throws FileNotFoundException, IOException {
        File a = TEST_FRUIT_EVENTS;
        File b = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_EVENTS.getName() + ".vecs.compact");
        File c = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_EVENTS.getName() + ".vecs.verbose");

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
        DoubleEnumeratingDeligate del = new DoubleEnumeratingDeligate(
                Enumerating.DEFAULT_TYPE, false, false, null, null);

        WeightedTokenPairSource efSrc = WeightedTokenPairSource.open(
                testSample, charset, del, false, false);
        assertTrue("EventsSource is empty", efSrc.hasNext());

        while (efSrc.hasNext()) {
            Weighted<TokenPair> ef = efSrc.read();
            assertNotNull("Found null EventsRecord", ef);
        }
    }

    public void testRandomAccess(File file) throws FileNotFoundException, IOException {
        final Map<Tell, Weighted<TokenPair>> hist =
                new HashMap<Tell, Weighted<TokenPair>>();

        DoubleEnumeratingDeligate del = new DoubleEnumeratingDeligate(
                Enumerating.DEFAULT_TYPE, false, false, null, null);

        WeightedTokenPairSource src =
                WeightedTokenPairSource.open(
                file, DEFAULT_CHARSET, del,false,false);
        {
            while (src.hasNext()) {
                final Tell pos = src.position();
                final Weighted<TokenPair> record = src.read();
                assertNotNull("Found null EventsRecord", record);
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
                System.out.println(
                        "expected: " + expected.record().toString(del.getEntryEnumerator(), del.getFeatureEnumerator()));

                src.position(pos);

                System.out.println("actual tell: " + src.position());
                assertEquals(pos, src.position());
                assertTrue(src.hasNext());

                Weighted<TokenPair> actual = src.read();
                System.out.println("actual tell: " + src.position());
                System.out.println("actual: " + actual.record().toString(del.getEntryEnumerator(),
                                                                         del.getFeatureEnumerator()));
                System.out.flush();

                assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void testRandomAccess() throws FileNotFoundException, IOException {
        testRandomAccess(TEST_FRUIT_EVENTS);
    }

    @Test
    public void testEventsPairEnumeratorConversion() throws FileNotFoundException, IOException {
        File a = TEST_FRUIT_EVENTS;
        File b = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_EVENTS.getName() + ".enum");
        File c = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_EVENTS.getName() + ".str");

        DoubleEnumeratingDeligate indel = new DoubleEnumeratingDeligate(
                Enumerating.DEFAULT_TYPE, false, false, null, null);
        DoubleEnumeratingDeligate outdel = new DoubleEnumeratingDeligate(
                Enumerating.DEFAULT_TYPE, true, true, null, null);


        {
            WeightedTokenPairSource aSrc = WeightedTokenPairSource.open(
                    a, DEFAULT_CHARSET, indel, false, false);
            WeightedTokenPairSink bSink = WeightedTokenPairSink.open(
                    b, DEFAULT_CHARSET, outdel, false, false, false);
//            bSink.setCompactFormatEnabled(false);
            ObjectIO.copy(aSrc, bSink);
            bSink.close();
        }

        assertTrue("Compact copy is smaller that verbose source.",
                   b.length() <= a.length());

        {
            WeightedTokenPairSource bSrc = WeightedTokenPairSource.open(
                    b, DEFAULT_CHARSET, outdel, false, false);
            WeightedTokenPairSink cSink = WeightedTokenPairSink.open(
                    c, DEFAULT_CHARSET, indel, false, false, false);
//            cSink.setCompactFormatEnabled(false);
            ObjectIO.copy(bSrc, cSink);
            cSink.close();
        }

        assertTrue("Verbose copy is smaller that compact source.",
                   c.length() >= b.length());
        assertTrue(
                "Double converted file is not equal to origion: " + a + " => " + c,
                Files.equal(a, c));
    }

    @Test
    public void testEventsPairCompactEnumeratorConversion() throws FileNotFoundException, IOException {
        File a = TEST_FRUIT_EVENTS;
        File b = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_EVENTS.getName() + ".enum.compact");
        File c = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_EVENTS.getName() + ".enum.compact.str");

        DoubleEnumeratingDeligate indel = new DoubleEnumeratingDeligate(
                Enumerating.DEFAULT_TYPE, false, false, null, null);
        DoubleEnumeratingDeligate outdel = new DoubleEnumeratingDeligate(
                Enumerating.DEFAULT_TYPE, true, true, null, null);


        Enumerator<String> idx = MemoryBasedStringEnumerator.newInstance();

        {
            WeightedTokenPairSource aSrc = WeightedTokenPairSource.open(
                    a, DEFAULT_CHARSET, indel, false, false);

            WeightedTokenPairSink bSink = WeightedTokenPairSink.open(
                    b, DEFAULT_CHARSET, outdel, false, false, true);
//            bSink.setCompactFormatEnabled(true);
            ObjectIO.copy(aSrc, bSink);
            bSink.close();
        }

        assertTrue("Compact copy is smaller that verbose source.",
                   b.length() <= a.length());

        {
            WeightedTokenPairSource bSrc = WeightedTokenPairSource.open(
                    b, DEFAULT_CHARSET, outdel, false, false);
            WeightedTokenPairSink cSink = WeightedTokenPairSink.open(
                    c, DEFAULT_CHARSET, indel, false, false, false);
//            cSink.setCompactFormatEnabled(false);
            ObjectIO.copy(bSrc, cSink);
            cSink.close();
        }

        assertTrue("Verbose copy is smaller that compact source.",
                   c.length() >= b.length());
        assertTrue(
                "Double converted file is not equal to origion: " + a + " " + c,
                Files.equal(a, c));
    }

    @Test
    public void testEventsPairCompactEnumeratorConversion_SkipIndex() throws FileNotFoundException, IOException {
        File a = TEST_FRUIT_EVENTS;
        File b = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_EVENTS.getName() + ".enum.skip.compact");
        File c = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_EVENTS.getName() + ".enum.skip.compact.str");

        DoubleEnumeratingDeligate indel = new DoubleEnumeratingDeligate(
                Enumerating.DEFAULT_TYPE, false, false, null, null);
        DoubleEnumeratingDeligate outdel = new DoubleEnumeratingDeligate(
                Enumerating.DEFAULT_TYPE, true, true, null, null);


        {
            WeightedTokenPairSource aSrc = WeightedTokenPairSource.open(
                    a, DEFAULT_CHARSET, indel, false, false);

            WeightedTokenPairSink bSink = WeightedTokenPairSink.open(
                    b, DEFAULT_CHARSET, outdel,true,true, true);
            ObjectIO.copy(aSrc, bSink);
            bSink.close();
        }

        assertTrue("Compact copy is smaller that verbose source.",
                   b.length() <= a.length());

        {
            WeightedTokenPairSource bSrc = WeightedTokenPairSource.open(
                    b, DEFAULT_CHARSET, outdel,true,true);
            WeightedTokenPairSink cSink = WeightedTokenPairSink.open(
                    c, DEFAULT_CHARSET, indel, false, false,
                    false);
            ObjectIO.copy(bSrc, cSink);
            cSink.close();
        }

        assertTrue("Verbose copy is smaller that compact source.",
                   c.length() >= b.length());
        assertTrue(
                "Double converted file is not equal to origion: " + a + " " + c,
                Files.equal(a, c));
    }

}
