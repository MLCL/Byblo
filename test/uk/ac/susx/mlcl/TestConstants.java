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
package uk.ac.susx.mlcl;

import uk.ac.susx.mlcl.lib.Files;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Random;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;
import static org.junit.Assert.*;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class TestConstants {

    public static final File TEST_DATA_DIR = new File("testdata");

    public static final File TEST_FRUIT_DIR =
            new File(TEST_DATA_DIR, "fruit");

    public static final String FRUIT_NAME = "bnc-gramrels-fruit";

    public static final File TEST_FRUIT_INPUT =
            new File(TEST_FRUIT_DIR, FRUIT_NAME);

    public static final File TEST_FRUIT_ENTRIES =
            new File(TEST_FRUIT_DIR, FRUIT_NAME + ".entries");

    public static final File TEST_FRUIT_FEATURES =
            new File(TEST_FRUIT_DIR, FRUIT_NAME + ".features");

    public static final File TEST_FRUIT_ENTRY_FEATURES =
            new File(TEST_FRUIT_DIR, FRUIT_NAME + ".entryFeatures");

    public static final File TEST_FRUIT_ENTRIES_FILTERED =
            new File(TEST_FRUIT_ENTRIES.getParentFile(), TEST_FRUIT_ENTRIES.getName() + ".filtered");

    public static final File TEST_FRUIT_FEATURES_FILTERED =
            new File(TEST_FRUIT_FEATURES.getParentFile(), TEST_FRUIT_FEATURES.getName() + ".filtered");

    public static final File TEST_FRUIT_ENTRY_FEATURES_FILTERED =
            new File(TEST_FRUIT_ENTRY_FEATURES.getParentFile(),
                     TEST_FRUIT_ENTRY_FEATURES.getName() + ".filtered");

    public static final File TEST_FRUIT_SIMS =
            new File(TEST_FRUIT_DIR, FRUIT_NAME + ".sims");

    public static final File TEST_FRUIT_NEIGHS =
            new File(TEST_FRUIT_DIR, FRUIT_NAME + ".neighs");

    public static final File TEST_OUTPUT_DIR = new File(TEST_DATA_DIR, "out");

    public static final Charset DEFAULT_CHARSET = Files.DEFAULT_CHARSET;

    ;

    static {
        TEST_OUTPUT_DIR.mkdir();
        TEST_OUTPUT_DIR.deleteOnExit();
    }

    public static File makeTempFile(int size) throws IOException {
        final File file = File.createTempFile(TestConstants.class.getName(),
                                              ".tmp");
        final OutputStream out = new BufferedOutputStream(
                new FileOutputStream(file));
        byte[] data = new byte[1024];
        new Random().nextBytes(data);
        int i = 0;
        while (i < size) {
            out.write(data, 0, Math.min(data.length, size - i));
            i += data.length;
        }
        out.flush();
        out.close();
        return file;
    }


}
