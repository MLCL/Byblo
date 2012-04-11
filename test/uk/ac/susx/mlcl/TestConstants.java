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
package uk.ac.susx.mlcl;

import java.io.*;
import uk.ac.susx.mlcl.lib.io.Files;
import java.nio.charset.Charset;
import static java.text.MessageFormat.*;
import java.util.Random;
import net.kotek.jdbm.DBMaker;
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

    public static final File TEST_FRUIT_INPUT_INDEXED =
            new File(TEST_FRUIT_DIR, FRUIT_NAME + ".indexed");

    public static final File TEST_FRUIT_ENTRY_INDEX =
            new File(TEST_FRUIT_DIR, FRUIT_NAME + ".entry-index");

    public static final File TEST_FRUIT_FEATURE_INDEX =
            new File(TEST_FRUIT_DIR, FRUIT_NAME + ".feature-index");

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

    public static final File TEST_FRUIT_SIMS_100NN =
            new File(TEST_FRUIT_DIR, TEST_FRUIT_SIMS.getName() + ".100nn");

    public static final File TEST_FRUIT_INDEXED_ENTRIES =
            new File(TEST_FRUIT_DIR, FRUIT_NAME + ".indexed.entries");

    public static final File TEST_FRUIT_INDEXED_FEATURES =
            new File(TEST_FRUIT_DIR, FRUIT_NAME + ".indexed.features");

    public static final File TEST_FRUIT_INDEXED_ENTRY_FEATURES =
            new File(TEST_FRUIT_DIR, FRUIT_NAME + ".indexed.entryFeatures");

    public static final File TEST_FRUIT_INDEXED_SIMS =
            new File(TEST_FRUIT_DIR, FRUIT_NAME + ".indexed.sims");

    public static final File TEST_FRUIT_INDEXED_SIMS_100NN =
            new File(TEST_FRUIT_DIR, FRUIT_NAME + ".indexed.sims.100nn");

    public static final File TEST_OUTPUT_DIR = new File(TEST_DATA_DIR, "out");

    public static final File TEST_TMP_DIR = new File(TEST_OUTPUT_DIR, "tmp");

    public static final Charset DEFAULT_CHARSET = Files.DEFAULT_CHARSET;

    ;

    static {
        TEST_OUTPUT_DIR.mkdir();
        TEST_TMP_DIR.mkdir();
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

    public static void assertValidInputFiles(File... files) throws IOException {
        for (File file : files) {
            assertNotNull("File is null.", file);
            assertTrue(format("Input file is null: \"{0}\"", file), file != null);
            assertTrue(format("Input file does not exist: \"{0}\" ", file),
                       file.exists());
            assertTrue(
                    format("Input file is not a regular file: \"{0}\" ", file),
                    file.isFile());
            assertTrue(format("Input file is empty: ", file) + file,
                       file.length() > 0);

        }
    }

    public static void assertValidPlaintextInputFiles(File... files) throws IOException {
        assertValidInputFiles(files);
        for (File file : files) {
            // The last character should be a newline.
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(file.length() - 1);
            int ch = raf.read();
            assertEquals(format("Expecting newline chracter at end of inout file: \"{0}\"", file), ch, '\n');
            raf.close();
        }
    }

    public static void assertValidJDBCInputFiles(File... files) throws IOException {
        for (File file : files) {
            assertNotNull("File is null.", file);
            assertTrue(format("Input file is null: \"{0}\"", file), file != null);

            File data = new File(file.getParentFile(), file.getName() + ".d.0");
            File index = new File(file.getParentFile(), file.getName() + ".i.0");
            File trans = new File(file.getParentFile(), file.getName() + ".t");

            assertValidInputFiles(data, index, trans);
        }
    }

    public static void assertValidIndexInputFiles(File... files) throws IOException {
        for (File file : files) {
            assertNotNull("File is null.", file);
            assertTrue(format("Input file is null: \"{0}\"", file), file != null);
            File data = new File(file.getParentFile(), file.getName() + ".d.0");
            File index = new File(file.getParentFile(), file.getName() + ".i.0");
            File trans = new File(file.getParentFile(), file.getName() + ".t");

            assertValidInputFiles(data, index, trans);
        }
    }

    public static void assertValidOutputFiles(File... files) throws IOException {
        for (File file : files) {
            assertNotNull("File is null.", file);
            if (file.exists()) {
                assertTrue(format("Input file is not a regular: \"{0}\"", file),
                           file.isFile());
                assertTrue(format("Input file is not writeable: \"{0}\"", file),
                           file.canWrite());
            } else {
                assertTrue(format("Cannot be created: \"{0}\"", file),
                           file.getParentFile().canWrite());
            }
        }
    }

    public static void assertValidJDBCOutputFiles(File... files) throws IOException {
        for (File file : files) {
            assertNotNull("File is null.", file);
            File data = new File(file.getParentFile(), file.getName() + ".d.0");
            File index = new File(file.getParentFile(), file.getName() + ".i.0");
            File trans = new File(file.getParentFile(), file.getName() + ".t");
            assertValidOutputFiles(data, index, trans);
        }
    }

    public static void assertSizeGT(File bigger, File smaller) throws IOException {
        assertValidPlaintextInputFiles(bigger, smaller);
        assertTrue(
                format("\"{0}\" is not smaller than \"{1}\"", smaller, bigger),
                bigger.length() > smaller.length());
    }

    public static void deleteIfExist(File... files) {
        for (File file : files) {
            if (file.exists())
                file.delete();
        }
    }

    public static void deleteJDBCIfExist(File... files) {
        for (File file : files) {
            File data = new File(file.getParentFile(), file.getName() + ".d.0");
            File index = new File(file.getParentFile(), file.getName() + ".i.0");
            File trans = new File(file.getParentFile(), file.getName() + ".t");
            deleteIfExist(data, index, trans);
        }
    }

    public static File suffix(File file, String suffix) {
        return new File(file.getParentFile(), file.getName() + suffix);
    }

}
