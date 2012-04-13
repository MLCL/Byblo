/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.io.EnumeratorPairBaringDeligate;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import org.junit.*;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSink;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSource;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import static org.junit.Assert.*;
import uk.ac.susx.mlcl.lib.Comparators;

/**
 *
 * @author hiam20
 */
public class SortWeightedTokenPairCommandTest {

    public SortWeightedTokenPairCommandTest() {
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
    public void testSortWeightedTokenPairCommand() throws IOException, Exception {
        System.out.println("Testing SortWeightedTokenPairCommand");

        final File inputFile = TEST_FRUIT_SIMS;

        final boolean preindexedTokens1 = false;
        final boolean preindexedTokens2 = false;

        File randomisedFile = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".sims.randomised");
        File sortedFile = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".sims.sorted");
        File entriesIndex = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".entry-index");
        File featuresIndex = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".feature-index");

        final EnumeratorPairBaringDeligate idx = new EnumeratorPairBaringDeligate(
                preindexedTokens1, preindexedTokens2, entriesIndex, featuresIndex, false, false);

        Comparator<Weighted<TokenPair>> comparator = Comparators.fallback(
                Weighted.recordOrder(TokenPair.firstStringOrder(idx.getEntriesEnumeratorCarriar())),
                Comparators.reverse(Weighted.<TokenPair>weightOrder()));

        testSortWeightedTokenPairCommand(
                inputFile, randomisedFile, sortedFile,
                idx, comparator);

    }

    @Test
    public void testSortWeightedTokenPairCommand_Indexed() throws IOException, Exception {
        System.out.println("Testing SortWeightedTokenPairCommand (Indexed)");

        final File inputFile = TEST_FRUIT_INDEXED_SIMS;

        final boolean preindexedTokens1 = true;
        final boolean preindexedTokens2 = true;

        File randomisedFile = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".indexed.sims.randomised");
        File sortedFile = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".indexed.sims.sorted");
        File entriesIndex = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".entry-index");
        File featuresIndex = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".feature-index");


        final EnumeratorPairBaringDeligate idx = new EnumeratorPairBaringDeligate(
                preindexedTokens1, preindexedTokens2, entriesIndex, featuresIndex, false, false);


        Comparator<Weighted<TokenPair>> comparator = Comparators.fallback(
                Weighted.recordOrder(TokenPair.firstIndexOrder()),
                Comparators.reverse(Weighted.<TokenPair>weightOrder()));

        testSortWeightedTokenPairCommand(inputFile, randomisedFile, sortedFile, idx, comparator);


    }

    private void testSortWeightedTokenPairCommand(
            File inputFile, File randomisedFile,
            File sortedFile, EnumeratorPairBaringDeligate idx,
            Comparator<Weighted<TokenPair>> comparator)
            throws IOException, Exception {


        assertTrue("Input file does not exist", inputFile.exists());
        assertTrue("Input file is not a regular file", inputFile.isFile());
        assertTrue("Input file length differs from input", inputFile.length() > 0);


        // load a weighted token pair file
        WeightedTokenPairSource inputSource = openSource(inputFile, idx);
        List<Weighted<TokenPair>> list = IOUtil.readAll(inputSource);
        inputSource.close();

        assertTrue("Input list is empty", list.size() > 0);

        // scamble it up
        shuffle(list);


        // write to a temporary file

        WeightedTokenPairSink randomisedSink = openSink(randomisedFile, idx, false);
        IOUtil.copy(list, randomisedSink);
        randomisedSink.flush();
        randomisedSink.close();

        assertTrue("Randomised file does not exist", randomisedFile.exists());
        assertTrue("Randomised file is not a regular file", randomisedFile.isFile());
        assertTrue("Randomised file length differs from input", randomisedFile.length() == inputFile.length());


        // run the command



        SortWeightedTokenPairCommand cmd =
                new SortWeightedTokenPairCommand(
                randomisedFile, sortedFile, DEFAULT_CHARSET,
                idx);
        cmd.setIndexDeligate(idx);
        cmd.setComparator(comparator);
        cmd.getFilesDeligate().setCompactFormatDisabled(true);
        cmd.runCommand();


        assertTrue("Sorted file does not exist", sortedFile.exists());
        assertTrue("Sorted file is not a regular file", sortedFile.isFile());
        assertTrue("Sorted file length differs from input", sortedFile.length() == inputFile.length());

        // load the sorted output file and check it's sensible

        WeightedTokenPairSource sortedSource = openSource(sortedFile, idx);
        List<Weighted<TokenPair>> sorted = IOUtil.readAll(sortedSource);
        inputSource.close();


        List<Weighted<TokenPair>> listCopy = new ArrayList<Weighted<TokenPair>>(list);
        Collections.sort(listCopy, comparator);


        assertEquals(sorted, listCopy);

        for (int i = 1; i < sorted.size(); i++) {
            Weighted<TokenPair> a = sorted.get(i - 1);
            Weighted<TokenPair> b = sorted.get(i);
            assertTrue("Sorted data does not match comparator: "
                    + a + " > " + b, comparator.compare(a, b) <= 0);
        }
    }

    private static <T> void swap(List<T> list, int i, int j) {
        if (i != j) {
            T tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
    }

    private static <T> void shuffle(List<T> list) {
        Random rand = new Random(0);
        for (int i = 0; i < list.size(); i++)
            swap(list, i, rand.nextInt(list.size()));
    }

    private static WeightedTokenPairSource openSource(File file, EnumeratorPairBaringDeligate idx)
            throws IOException {
        return WeightedTokenPairSource.open(
                file, DEFAULT_CHARSET,
                idx);
    }

    private static WeightedTokenPairSink openSink(
            File file, EnumeratorPairBaringDeligate idx, boolean compact)
            throws IOException {
        WeightedTokenPairSink sink = WeightedTokenPairSink.open(
                file, DEFAULT_CHARSET,
                idx, compact);
//        sink.setCompactFormatEnabled(false);
        return sink;
    }

}
