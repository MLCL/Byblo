/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import org.junit.Test;
import static org.junit.Assert.*;
import static uk.ac.susx.mlcl.TestConstants.*;

/**
 *
 * @author hiam20
 */
public class EntryFeatureSourceTest {

    @Test
    public void testEndOfLineTab() throws FileNotFoundException, IOException {
        File testSample = new File(TEST_DATA_DIR, "lm-medline-input-sample");
        Charset charset = Charset.forName("UTF-8");
        EntryFeatureSource efSrc = new EntryFeatureSource(testSample, charset);
        assertTrue("EntryFeatureSource is empty", efSrc.hasNext());

        while (efSrc.hasNext()) {
            EntryFeatureRecord ef = efSrc.read();
            assertNotNull("Found null EntryFeatureRecord", ef);
        }
    }
}
