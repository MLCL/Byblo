/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.byblo.commands;

import com.google.common.io.Files;
import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import org.junit.*;
import static org.junit.Assert.*;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.byblo.io.TokenPairSource;

/**
 *
 * @author hamish
 */
public class IndexEventsCommandTest {

    private static final String SUBJECT = IndexEventsCommandTest.class.getName();

    public IndexEventsCommandTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private void runWithAPI(File in, File out, File index1, File index2,
                            Charset charset)
            throws Exception {
        final IndexEventsCommand task = new IndexEventsCommand();
        task.getFileDeligate().setSourceFile(in);
        task.getFileDeligate().setDestinationFile(out);
        task.getIndexDeligate().setIndexFile1(index1);
        task.getIndexDeligate().setIndexFile2(index2);
        task.getFileDeligate().setCharset(charset);

        task.runCommand();

        assertTrue("Output file not created: " + out, out.exists());
        assertTrue("Entry index file not created: " + index1, index1.exists());
        assertTrue("Feature index file not created: " + index2, index2.exists());

        assertTrue("Empty output file found: " + out, out.length() > 0);
        assertTrue("Empty entry index file found: " + index1,
                   index1.length() > 0);
        assertTrue("Empty feature index file found: " + index2,
                   index2.length() > 0);
    }

    @Test
    public void testRunOnFruitAPI() throws Exception {
        System.out.println("Testing " + SUBJECT + " on " + TEST_FRUIT_INPUT);

        final String fruitPrefix = TEST_FRUIT_INPUT.getName();

        final File out = new File(TEST_OUTPUT_DIR, fruitPrefix + ".indexed");
        final File idx1 = new File(TEST_OUTPUT_DIR, fruitPrefix + ".entry-index");
        final File idx2 = new File(TEST_OUTPUT_DIR,
                                   fruitPrefix + ".feature-index");

        out.delete();
        idx1.delete();
        idx2.delete();

        runWithAPI(TEST_FRUIT_INPUT, out, idx1, idx2, DEFAULT_CHARSET);


        assertTrue(out.exists());
        assertTrue(idx1.exists());
        assertTrue(idx2.exists());
        assertTrue(out.length() > 0);
        assertTrue(idx1.length() > 0);
        assertTrue(idx2.length() > 0);


        assertTrue(MessageFormat.format("Output entries file \"{0}\" differs from expected data file \"{1}\".",out, TEST_FRUIT_INPUT_INDEXED),
                   TokenPairSource.equal(out, TEST_FRUIT_INPUT_INDEXED, DEFAULT_CHARSET));
        assertTrue("Output features file differs from test data file.",
                   Files.equal(idx1, TEST_FRUIT_ENTRY_INDEX));
        assertTrue("Output entry/features file differs from test data file.",
                   Files.equal(idx2, TEST_FRUIT_FEATURE_INDEX));
    }
//    /**
//     * Test of initialiseTask method, of class IndexEventsTask.
//     */
//    @Test
//    public void testInitialiseTask() throws Exception {
//        System.out.println("initialiseTask");
//        IndexEventsTask instance = new IndexEventsTask();
//        instance.initialiseTask();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of finaliseTask method, of class IndexEventsTask.
//     */
//    @Test
//    public void testFinaliseTask() throws Exception {
//        System.out.println("finaliseTask");
//        IndexEventsTask instance = new IndexEventsTask();
//        instance.finaliseTask();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of runTask method, of class IndexEventsTask.
//     */
//    @Test
//    public void testRunTask() throws Exception {
//        System.out.println("runTask");
//        IndexEventsTask instance = new IndexEventsTask();
//        instance.runTask();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of main method, of class IndexEventsTask.
//     */
//    @Test
//    public void testMain() throws Exception {
//        System.out.println("main");
//        String[] args = null;
//        IndexEventsTask.main(args);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
