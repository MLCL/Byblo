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

import java.util.Map;
import java.io.File;
import java.nio.charset.Charset;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author hamish
 */
public class IOUtilTest {

    static final File CHARSET_SAMPLES_DIR = new File("sampledata/charsets");

    static final String[] CHARSET_SAMPLES = new String[]{
        "chinese-big5.meta.html",
        "chinese-hz.meta.html",
        "chinese.meta.html",
        "french.meta.html",
        "german.meta.html",
        "greek.meta.html",
        "hebrew-visual.meta.html",
        //        "japanese.meta.html",
        "korean.meta.html",
        "russian-cp1251.meta.html",
        "russian-koi.meta.html",
        "russian.meta.html",
        "sample-euc.meta.html",
        "sample-jis.meta.html",
        "sample-sjis.meta.html",
        "UTF-8-demo.txt",
        "UTF-16-demo.txt",
        "UTF-32-demo.txt"
    };

    static final String[] CHARSET = new String[]{
        "big5",
        "gb2312", //       "hz-gb-2312",
        "GB2312",
        "iso-8859-1",
        "iso-8859-1",
        "iso-8859-7",
        "iso-8859-8",
        //               "iso-2022-jp",
        "ksc_5601",
        "cp1251",
        "koi8",
        "iso-8859-5",
        "x-euc-jp",
        "iso-2022-jp",
        "x-sjis",
        "UTF-8",
        "UTF-16",
        "UTF-32"
    };

    public IOUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Map<String, Charset> charsets = Charset.availableCharsets();
        for (Charset c : charsets.values()) {
            System.out.printf("%-20s %s %s %s%n", c,
                    c.canEncode() ? "e" : "-",
                    c.isRegistered() ? "r" : "-",
                    c.aliases());

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
        System.err.flush();
        System.out.flush();
    }

    /**
     * Test of readAll method, of class IOUtil.
     */
    @Test
    public void testCharsetCoding() throws Exception {
        System.out.println("testCharsetCoding()");
        int n = CHARSET_SAMPLES.length;
        boolean[][] decoded = new boolean[n][n];
        boolean[][] encoded = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            Charset charset = Charset.forName(CHARSET[i]);
            for (int j = 0; j < n; j++) {
                File file = new File(CHARSET_SAMPLES_DIR, CHARSET_SAMPLES[j]);

                StringBuilder dst = new StringBuilder();
                try {
                    IOUtil.readAll(file, charset, dst);
                    decoded[i][j] = true;
                } catch (Exception t) {
                    decoded[i][j] = false;
                    if (i == j)
                        throw t;
                }
            }
        }
        for (int i = 0; i < n; i++) {
            Charset charset = Charset.forName(CHARSET[i]);
            File file = new File(CHARSET_SAMPLES_DIR, CHARSET_SAMPLES[i]);
            StringBuilder dst = new StringBuilder();
            IOUtil.readAll(file, charset, dst);
            for (int j = 0; j < n; j++) {
                File tmp = File.createTempFile(this.getClass().getName(),
                        CHARSET_SAMPLES[j]);
                tmp.deleteOnExit();

                try {
                    IOUtil.writeAll(tmp, Charset.forName(CHARSET[j]), dst);
                    encoded[i][j] = true;
                } catch (Exception t) {
                    encoded[i][j] = false;
                    if (i == j)
                        throw t;
                }
            }
        }
        for (int i = 0; i < n; i++) {
            System.out.printf("%-13s ", Charset.forName(CHARSET[i]));
            for (int j = 0; j < n; j++) {
                System.out.print(decoded[i][j] ? "D" : ".");
                System.out.print(decoded[i][j] ? "E" : ".");
                System.out.print(" ");
            }
            System.out.println();
        }
    }
}
