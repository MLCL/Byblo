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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import uk.ac.susx.mlcl.lib.io.Lexer.Tell;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import static uk.ac.susx.mlcl.TestConstants.*;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class EntryTest {

    private void copyE(File a, File b, boolean compact) throws FileNotFoundException, IOException {
        EntrySource aSrc = new EntrySource(a, DEFAULT_CHARSET);
        EntrySink bSink = new EntrySink(b, DEFAULT_CHARSET,
                aSrc.getStringIndex());
        bSink.setCompactFormatEnabled(compact);

        copy(aSrc, bSink);
        bSink.close();
    }

    @Test
    public void testEntriesConversion() throws FileNotFoundException, IOException {
        File a = TEST_FRUIT_ENTRIES;
        File b = new File(TEST_OUTPUT_DIR,
                TEST_FRUIT_ENTRIES.getName() + ".compact");
        File c = new File(TEST_OUTPUT_DIR,
                TEST_FRUIT_ENTRIES.getName() + ".verbose");

        copyE(a, b, true);

        assertTrue("Compact copy is smaller that verbose source.",
                b.length() <= a.length());

        copyE(b, c, false);


        assertTrue("Verbose copy is smaller that compact source.",
                c.length() >= b.length());
        assertTrue("Double converted file is not equal to origion.",
                Files.equal(a, c));
    }

    public void testRandomAccess(File file) throws FileNotFoundException, IOException {
        final Map<Tell, EntryRecord> hist =
                new HashMap<Tell, EntryRecord>();

        EntrySource src = new EntrySource(file, DEFAULT_CHARSET);
        {
            while (src.hasNext()) {
                final Tell pos = src.position();
                final EntryRecord record = src.read();

                System.out.println(pos.toString() + ": " + record.toString(src.
                        getStringIndex()));

                assertNotNull("Found null EntryRecord", record);
                hist.put(pos, record);
            }
        }

        {
            List<Tell> positions = new ArrayList<Tell>(hist.keySet());
            Random rand = new Random(2);

            for (int i = 0; i < 10; i++) {
                final Tell pos = positions.get(rand.nextInt(positions.size()));
                final EntryRecord expected = hist.get(pos);

                System.out.println("expected tell: " + pos);
                System.out.println("expected: " + expected.toString(src.
                        getStringIndex()));

                src.position(pos);

                assertTrue(src.hasNext());
                assertEquals(pos, src.position());

                EntryRecord actual = src.read();
                System.out.println("actual tell: " + src.position());
                System.out.println("actual: " + actual.toString(src.
                        getStringIndex()));

                assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void testRandomAccess()
            throws FileNotFoundException, IOException {
        testRandomAccess(TEST_FRUIT_ENTRIES);
    }
}
