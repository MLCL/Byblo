/*
 * Copyright (c) 2010-2013, University of Sussex
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
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.susx.mlcl.byblo.enumerators.*;
import uk.ac.susx.mlcl.lib.io.ObjectIO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static uk.ac.susx.mlcl.TestConstants.*;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class EventTest {

    @Test
    @Ignore
    public void testLMMedlineSample() throws IOException {
        File testSample = new File(TEST_DATA_DIR, "lm-medline-input-sample");
        Charset charset = Charset.forName("UTF-8");
        DoubleEnumeratingDelegate del = new DoubleEnumeratingDelegate(
                Enumerating.DEFAULT_TYPE, false, false, null, null);

        TokenPairSource efSrc = TokenPairSource.open(
                testSample, charset, del, false, false);
        assertTrue("EventSource is empty", efSrc.hasNext());

        while (efSrc.hasNext()) {
            TokenPair ef = efSrc.read();
            assertNotNull("Found null EventRecord", ef);
        }
    }

    private void copyEF(File a, File b, boolean compact) throws IOException {
        DoubleEnumeratingDelegate idx = new DoubleEnumeratingDelegate(
                Enumerating.DEFAULT_TYPE, false, false, null, null);
        TokenPairSource src = TokenPairSource.open(
                a, DEFAULT_CHARSET, idx, false, false);
        TokenPairSink sink = TokenPairSink.open(
                b, DEFAULT_CHARSET, idx, compact, false, false);
        ObjectIO.copy(src, sink);
        sink.close();
    }

    @Test
    public void testEvents_CompactConversion() throws IOException {
        File a = TEST_FRUIT_INPUT;
        File b = new File(TEST_OUTPUT_DIR,
                TEST_FRUIT_INPUT.getName() + ".compact");
        File c = new File(TEST_OUTPUT_DIR,
                TEST_FRUIT_INPUT.getName() + ".verbose");

        copyEF(a, b, true);

        assertTrue("Compact copy is smaller that verbose source.",
                b.length() <= a.length());

        copyEF(b, c, false);

        assertTrue("Verbose copy is smaller that compact source.",
                c.length() >= b.length());
        assertTrue("Double converted file is not equal to origin.",
                Files.equal(a, c));
    }

    @Test
    public void testEntryPair_EnumeratorConversion() throws IOException, ClassNotFoundException {
        File a = TEST_FRUIT_INPUT;
        File b = new File(TEST_OUTPUT_DIR,
                TEST_FRUIT_INPUT.getName() + ".enum");
        File c = new File(TEST_OUTPUT_DIR,
                TEST_FRUIT_INPUT.getName() + ".str");
        File idxFile = new File(TEST_OUTPUT_DIR,
                TEST_FRUIT_INPUT.getName() + ".index");


        DoubleEnumerating inDelegate = EnumeratingDelegates.toPair(
                new SingleEnumeratingDelegate(Enumerating.DEFAULT_TYPE, false, idxFile));
        DoubleEnumerating outDelegate = EnumeratingDelegates.toPair(
                new SingleEnumeratingDelegate(Enumerating.DEFAULT_TYPE, true, idxFile));

        {


            TokenPairSource aSrc = TokenPairSource.open(
                    a, DEFAULT_CHARSET, inDelegate, false, false);

            TokenPairSink bSink = TokenPairSink.open(
                    b, DEFAULT_CHARSET, outDelegate, true, true, true);
            ObjectIO.copy(aSrc, bSink);

            inDelegate.saveEntriesEnumerator();

            bSink.close();
        }

        assertTrue("Compact copy is smaller that verbose source.",
                b.length() <= a.length());

        {
            TokenPairSource bSrc = TokenPairSource.open(
                    b, DEFAULT_CHARSET, outDelegate, true, true);
            TokenPairSink cSink = TokenPairSink.open(
                    c, DEFAULT_CHARSET, inDelegate,
                    false, false, false);
            ObjectIO.copy(bSrc, cSink);
            cSink.close();
        }

        assertTrue("Verbose copy is smaller that compact source.",
                c.length() >= b.length());
        assertTrue("Double converted file is not equal to origin.",
                Files.equal(a, c));
    }

}
