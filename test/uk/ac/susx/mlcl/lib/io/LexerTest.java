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
package uk.ac.susx.mlcl.lib.io;

import com.google.common.base.CharMatcher;
import uk.ac.susx.mlcl.lib.Strings;
import uk.ac.susx.mlcl.lib.io.Lexer.Type;
import java.io.OutputStream;
import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
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

    @BeforeClass
    public static void setUpClass() throws Exception {
        Level lvl = Level.FINE;
        Logger.getLogger("").setLevel(lvl);
        for (Handler h : Logger.getLogger("").getHandlers()) {
            h.setLevel(lvl);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        System.out.println();
    }

    private File makeTmpData(String str) throws IOException {
        File tmp = File.createTempFile(this.getClass().getName() + ".", "");
        tmp.deleteOnExit();

        OutputStream out = new FileOutputStream(tmp);
        out.write(str.getBytes());
        out.flush();
        out.close();

        return tmp;
    }

    @Test(timeout = 1000)
    public void basicTest() throws FileNotFoundException, IOException {
        System.out.println("basicTest");


        Charset charset = IOUtil.DEFAULT_CHARSET;
        File tmp = makeTmpData(CFB);

        Lexer lexer = new Lexer(tmp, charset);
        lexer.setDelimiterMatcher(CharMatcher.is('.'));

        System.out.printf("%-6s %-12s %-6s %-6s %-15s (%s,%s)%n",
                "num", "type", "start", "end", "value", "line",
                "column");
        int i = 0;
        while (lexer.hasNext()) {
            lexer.advance();
            System.out.printf("%-12s %-6d %-6d %-15s%n",
                    lexer.type(), lexer.start(), lexer.end(),
                    Strings.escape(lexer.value()));
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

    @Test(timeout = 10000)
    public void seekTest() throws FileNotFoundException, IOException {
        System.out.println("seekTest");

        Charset charset = IOUtil.DEFAULT_CHARSET;
        File tmp = makeTmpData(CFB);

        Lexer lexer = new Lexer(tmp, charset);
        lexer.setDelimiterMatcher(CharMatcher.is('.'));

        // Iterator of the whole string, storing the tell offsets for every
        // lexeme in a list
        Lexer.Tell[] tells = new Lexer.Tell[CFB_numbers.length];
        int i = 0;
        while (lexer.hasNext()) {
            lexer.advance();
            tells[i] = lexer.tell();
            i++;
        }

        // Now randomly seek into the lexer at various offsets, and check that
        // they are what they are supposed to be
        Random rand = new Random(1);
        System.out.printf("%-6s %-6s %-12s %-6s %-6s %-15s (%s,%s)%n",
                "seek", "num", "type", "start", "end", "value", "line",
                "column");
        for (int j = 0; j < 500; j++) {
            i = rand.nextInt(CFB_numbers.length);
            lexer.seek(tells[i]);
            System.out.printf("%-6d %-6d %-12s %-6d %-6d %-15s (%d,%d)%n",
                    i, 0/*lexer.number()*/, lexer.type(), lexer.start(), lexer.end(),
                    Strings.escape(lexer.value()), 0/*lexer.line()*/, 0/*lexer.column()*/);
            assertEquals("type[" + i + "]", CFB_types[i], lexer.type());
            assertEquals("value[" + i + "]", CFB_values[i], lexer.value().
                    toString());
            assertEquals("charAt0[" + i + "]", CFB_charAt0[i], lexer.charAt(0));
        }

    }

}
