/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import java.io.File;
import static java.text.MessageFormat.*;
import org.junit.*;
import static org.junit.Assert.*;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratorType;
import uk.ac.susx.mlcl.byblo.io.*;

/**
 *
 * @author hamish
 */
public class IndexTPCommandTest {

    public IndexTPCommandTest() {
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

    @Test
    public void testRunOnFruitAPI() throws Exception {
        System.out.println("Testing " + IndexTPCommandTest.class.getName()
                + " on " + TEST_FRUIT_INPUT);

        final String name = TEST_FRUIT_INPUT.getName();
        final File out = new File(TEST_OUTPUT_DIR, name + ".indexed");
        final File idx1 = new File(TEST_OUTPUT_DIR, name + ".entry-index");
        final File idx2 = new File(TEST_OUTPUT_DIR, name + ".feature-index");

        deleteIfExist(out, idx1, idx2);

        indexTP(TEST_FRUIT_INPUT, out, idx1, idx2, EnumeratorType.Memory, false, false, true);


//        assertTrue(format(
//                "Output entries file \"{0}\" differs from expected file \"{1}\".",
//                out, TEST_FRUIT_INPUT_INDEXED),
//                   TokenPairSource.equal(out, TEST_FRUIT_INPUT_INDEXED,
//                                         DEFAULT_CHARSET, false, false));

        // XXX: The files can be out of order
        //        assertTrue("Output features file differs from test data file.",
        //                   Files.equal(idx1, TEST_FRUIT_ENTRY_INDEX));
        //        assertTrue("Output entry/features file differs from test data file.",
        //                   Files.equal(idx2, TEST_FRUIT_FEATURE_INDEX));


        File out2 = suffix(out, ".unindexed");
        unindexTP(out, out2, idx1, idx2, EnumeratorType.Memory, false, false, true);



//        TokenPairSource.equal(out, out2, DEFAULT_CHARSET, false, false);

    }

    @Test
    public void testRunOnFruitAPI_skipboth_compact() throws Exception {
        testRunOnFruitAPI("compact-skipboth-", EnumeratorType.Memory, true, true, true);
    }

    @Test
    public void testRunOnFruitAPI_skipleft_compact() throws Exception {
        testRunOnFruitAPI("compact-skipleft-", EnumeratorType.Memory, true, false, true);
    }

    @Test
    public void testRunOnFruitAPI_skipright_compact() throws Exception {
        testRunOnFruitAPI("compact-skipright-", EnumeratorType.Memory, false, true, true);
    }

    @Test
    public void testRunOnFruitAPI_skipboth_verbose() throws Exception {
        testRunOnFruitAPI("verbose-skipboth-", EnumeratorType.Memory, true, true, false);
    }

    @Test
    public void testRunOnFruitAPI_skipleft_verbose() throws Exception {
        testRunOnFruitAPI("verbose-skipleft-", EnumeratorType.Memory, true, false, false);
    }

    @Test
    public void testRunOnFruitAPI_skipright_verbose() throws Exception {
        testRunOnFruitAPI("verbose-skipright-", EnumeratorType.Memory, false, true, false);
    }

    @Test
    public void testRunOnFruitAPI_skipboth_compact_JDBC() throws Exception {
        testRunOnFruitAPI("compact-skipboth-jdbc-", EnumeratorType.JDBC, true, true, true);
    }

    @Test
    public void testRunOnFruitAPI_skipleft_compact_JDBC() throws Exception {
        testRunOnFruitAPI("compact-skipleft-jdbc-", EnumeratorType.JDBC, true, false, true);
    }

    @Test
    public void testRunOnFruitAPI_skipright_compact_JDBC() throws Exception {
        testRunOnFruitAPI("compact-skipright-jdbc-", EnumeratorType.JDBC, false, true, true);
    }

    @Test
    public void testRunOnFruitAPI_skipboth_verbose_JDBC() throws Exception {
        testRunOnFruitAPI("verbose-skipboth-jdbc-", EnumeratorType.JDBC, true, true, false);
    }

    @Test
    public void testRunOnFruitAPI_skipleft_verbose_JDBC() throws Exception {
        testRunOnFruitAPI("verbose-skipleft-jdbc-", EnumeratorType.JDBC, true, false, false);
    }

    @Test
    public void testRunOnFruitAPI_skipright_verbose_JDBC() throws Exception {
        testRunOnFruitAPI("verbose-skipright-jdbc-", EnumeratorType.JDBC, false, true, false);
    }

    public void testRunOnFruitAPI(
            String prefix, EnumeratorType type, boolean skip1, boolean skip2, boolean compact) throws Exception {
        System.out.println("Testing " + IndexTPCommandTest.class.getName()
                + " on " + TEST_FRUIT_INPUT);

        final String name = TEST_FRUIT_INPUT.getName();
        final File out = new File(TEST_OUTPUT_DIR, prefix + name + ".indexed");
        File out2 = suffix(out, ".unindexed");
        final File idx1 = new File(TEST_OUTPUT_DIR, name + ".entry-index");
        final File idx2 = new File(TEST_OUTPUT_DIR, name + ".feature-index");

        deleteIfExist(out, idx1, idx2);

        indexTP(TEST_FRUIT_INPUT, out, idx1, idx2, type, skip1, skip2, compact);

        unindexTP(out, out2, idx1, idx2, type, skip1, skip2, compact);

//        TokenPairSource.equal(out, out2, DEFAULT_CHARSET, skip1, skip2);

    }

    public static void indexTP(File from, File to, File index1, File index2,
                               EnumeratorType type, boolean skip1, boolean skip2, boolean compact)
            throws Exception {
        assertValidPlaintextInputFiles(from);
        assertValidOutputFiles(to);
        if (type == EnumeratorType.JDBC)
            assertValidJDBCOutputFiles(index1, index2);
        else
            assertValidOutputFiles(index1, index2);

        IndexInstancesCommand unindex = new IndexInstancesCommand();
        unindex.getFilesDeligate().setCharset(DEFAULT_CHARSET);
        unindex.getFilesDeligate().setSourceFile(from);
        unindex.getFilesDeligate().setDestinationFile(to);
        unindex.setIndexDeligate(new DoubleEnumeratingDeligate(type, true, true, index1, index2));
        unindex.runCommand();

        assertValidPlaintextInputFiles(to);

        if (type == EnumeratorType.JDBC)
            assertValidJDBCInputFiles(index1, index2);
        else
            assertValidInputFiles(index1, index2);
        assertSizeGT(from, to);
    }

    public static void unindexTP(File from, File to, File index1, File index2,
                                 EnumeratorType type, boolean skip1, boolean skip2, boolean compact)
            throws Exception {
        assertValidPlaintextInputFiles(from);
        if (type == EnumeratorType.JDBC)
            assertValidJDBCInputFiles(index1, index2);
        else
            assertValidInputFiles(index1, index2);
        assertValidOutputFiles(to);

        UnindexInstancesCommand unindex = new UnindexInstancesCommand();
        unindex.getFilesDeligate().setCharset(DEFAULT_CHARSET);
        unindex.getFilesDeligate().setSourceFile(from);
        unindex.getFilesDeligate().setDestinationFile(to);

        unindex.setIndexDeligate(new DoubleEnumeratingDeligate(type, true, true, index1, index2));
        unindex.runCommand();

        assertValidPlaintextInputFiles(to);
        assertSizeGT(to, from);
    }

}
