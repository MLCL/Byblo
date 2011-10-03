/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.Permission;
import java.util.Random;
import uk.ac.susx.mlcl.lib.io.IOUtil;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class TestConstants {

    public static final File TEST_DATA_DIR = new File("testdata");

    public static final File TEST_CHARSETS_DIR =
            new File(TEST_DATA_DIR, "charsets");

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
            new File(TEST_FRUIT_ENTRIES.getParentFile(), TEST_FRUIT_ENTRIES.
            getName() + ".filtered");

    public static final File TEST_FRUIT_FEATURES_FILTERED =
            new File(TEST_FRUIT_FEATURES.getParentFile(), TEST_FRUIT_FEATURES.
            getName() + ".filtered");

    public static final File TEST_FRUIT_ENTRY_FEATURES_FILTERED =
            new File(TEST_FRUIT_ENTRY_FEATURES.getParentFile(),
            TEST_FRUIT_ENTRY_FEATURES.getName() + ".filtered");

    public static final File TEST_FRUIT_SIMS =
            new File(TEST_FRUIT_DIR, FRUIT_NAME + ".sims");

    public static final File TEST_FRUIT_NEIGHS =
            new File(TEST_FRUIT_DIR, FRUIT_NAME + ".neighs");

    public static final File TEST_OUTPUT_DIR = new File(TEST_DATA_DIR, "out");

    public static final Charset DEFAULT_CHARSET = IOUtil.DEFAULT_CHARSET;

    ;

    static {
        TEST_OUTPUT_DIR.mkdir();
        TEST_OUTPUT_DIR.deleteOnExit();
    }

    public static File makeTempFile(int size) throws IOException {
        final File file = File.createTempFile(TestConstants.class.getName(), ".tmp");
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
