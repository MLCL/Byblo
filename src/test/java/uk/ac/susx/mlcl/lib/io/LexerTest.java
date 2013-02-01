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
package uk.ac.susx.mlcl.lib.io;

import com.google.common.base.CharMatcher;
import com.google.common.io.Closeables;
import com.google.common.io.Flushables;
import org.junit.*;
import uk.ac.susx.mlcl.TestConstants;
import uk.ac.susx.mlcl.lib.io.Lexer.Type;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class LexerTest {

    private static final String CFB = "come friendly bombs\nand fall on slough.";

    private static final int[] CFB_numbers = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
            11, 12, 13};

    private static final Type[] CFB_types = {Type.Value, Type.Whitespace,
            Type.Value, Type.Whitespace, Type.Value, Type.Whitespace, Type.Value,
            Type.Whitespace, Type.Value, Type.Whitespace, Type.Value,
            Type.Whitespace, Type.Value, Type.Delimiter};

    private static final int[] CFB_starts = {0, 4, 5, 13, 14, 19, 20, 23, 24,
            28, 29, 31, 32, 38};

    private static final int[] CFB_ends = {4, 5, 13, 14, 19, 20, 23, 24, 28,
            29, 31, 32, 38, 39};

    private static final String[] CFB_values = new String[]{"come", " ",
            "friendly", " ", "bombs", "\n", "and", " ", "fall", " ", "on", " ",
            "slough", "."};

    private static final int[] CFB_lines = {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1,
            1, 1};

    private static final int[] CFB_columns = {0, 4, 5, 13, 14, 19, 0, 3, 4, 8,
            9, 11, 12, 18};

    private static final char[] CFB_charAt0 = {'c', ' ', 'f', ' ', 'b', '\n',
            'a', ' ', 'f', ' ', 'o', ' ', 's', '.'};

    public LexerTest() {
    }

    private File makeTmpData(String str, Charset charset) throws IOException {
        File tmp = File.createTempFile(this.getClass().getName() + ".", "");
        tmp.deleteOnExit();

        OutputStream out = null;
        try {
            out = new FileOutputStream(tmp);
            out.write(str.getBytes(charset));
        } finally {
            if (out != null) {
                Flushables.flushQuietly(out);
                Closeables.closeQuietly(out);
            }
        }
        return tmp;
    }

    @Test
    public void basicTest() throws IOException {
        System.out.println("basicTest");


        Charset charset = Files.DEFAULT_CHARSET;
        File tmp = makeTmpData(CFB, charset);

        Lexer lexer = new Lexer(tmp, charset);
        lexer.setDelimiterMatcher(CharMatcher.is('.'));

        int i = 0;
        while (lexer.hasNext()) {
            lexer.advance();
            assertEquals("type", CFB_types[i], lexer.type());
            assertEquals("start", CFB_starts[i], lexer.start());
            assertEquals("end", CFB_ends[i], lexer.end());
            assertEquals("value", CFB_values[i], lexer.value().toString());
            assertEquals("charAt0", CFB_charAt0[i], lexer.charAt(0));
            i++;
        }

        assertEquals("numbers length", i, CFB_numbers.length);
        assertEquals("types length", i, CFB_types.length);
        assertEquals("starts length", i, CFB_starts.length);
        assertEquals("ends length", i, CFB_ends.length);
        assertEquals("values length", i, CFB_values.length);
        assertEquals("lines length", i, CFB_lines.length);
        assertEquals("columns length", i, CFB_columns.length);
        assertEquals("charAt0 length", i, CFB_charAt0.length);
    }

    @Test
    public void seekTest() throws IOException {
        System.out.println("seekTest");

        Charset charset = Files.DEFAULT_CHARSET;
        File tmp = makeTmpData(CFB, charset);

        Lexer lexer = new Lexer(tmp, charset);
        lexer.setDelimiterMatcher(CharMatcher.is('.'));

        // Iterator of the whole string, storing the tell offsets for every
        // lexeme in a list
        Tell[] tells = new Tell[CFB_numbers.length];
        int i = 0;
        while (lexer.hasNext()) {
            lexer.advance();
            tells[i] = lexer.position();
            i++;
        }

        // Now randomly seek into the lexer at various offsets, and check that
        // they are what they are supposed to be
        Random rand = new Random(1);
        for (int j = 0; j < 500; j++) {
            i = rand.nextInt(CFB_numbers.length);
            lexer.position(tells[i]);
            assertEquals("type[" + i + "]", CFB_types[i], lexer.type());
            assertEquals("value[" + i + "]", CFB_values[i], lexer.value().
                    toString());
            assertEquals("charAt0[" + i + "]", CFB_charAt0[i], lexer.charAt(0));
        }

    }

    @Test
    public void seekTestFruitEntries() throws IOException {
        seekTest(TestConstants.TEST_FRUIT_ENTRIES);
    }

    @Test
    public void seekTestFruitFeatures() throws IOException {
        seekTest(TestConstants.TEST_FRUIT_FEATURES);

    }

    @Test
    public void seekTestFruitEvents() throws IOException {
        seekTest(TestConstants.TEST_FRUIT_EVENTS);
    }

    @Test
    public void seekTestFruitInput() throws IOException {
        seekTest(TestConstants.TEST_FRUIT_INPUT);
    }

    @Test
    public void seekTestFruitSims() throws IOException {
        seekTest(TestConstants.TEST_FRUIT_SIMS);
    }

    void seekTest(File file) throws IOException {
        System.out.println("Test Lexer seek with " + file.toString() + "");

        Charset charset = Files.DEFAULT_CHARSET;

        Lexer lexer = new Lexer(file, charset);

        List<Tell> tells = new ArrayList<Tell>();
        List<String> values = new ArrayList<String>();
        while (lexer.hasNext()) {
            lexer.advance();
            tells.add(lexer.position());
            values.add(lexer.value().toString());
        }

        // check a bunch random seeks
        Random rand = new Random(0);
        for (int j = 0; j < 500; j++) {
            final int i = rand.nextInt(tells.size());

            lexer.position(tells.get(i));

            assertEquals(values.get(i), lexer.value().toString());
            assertEquals(tells.get(i), lexer.position());
        }

        // Check the edge cases
        for (int i : new int[]{tells.size() - 1, 0, tells.size() - 2, 1}) {
            lexer.position(tells.get(i));
            assertEquals(values.get(i), lexer.value().toString());
            assertEquals(tells.get(i), lexer.position());
        }

    }
}
