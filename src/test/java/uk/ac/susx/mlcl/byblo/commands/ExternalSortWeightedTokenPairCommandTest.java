/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import java.io.File;
import java.io.IOException;
import static java.text.MessageFormat.*;
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
import static org.junit.Assert.*;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.io.ObjectIO;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;

/**
 *
 * @author hiam20
 */
public class ExternalSortWeightedTokenPairCommandTest {

    public ExternalSortWeightedTokenPairCommandTest() {
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
        File entryIndex = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".entry-index");
        File featureIndex = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".feature-index");

        final DoubleEnumerating idx = new DoubleEnumeratingDeligate(
                Enumerating.DEFAULT_TYPE,
                preindexedTokens1, preindexedTokens2,
                null, null);

        Comparator<Weighted<TokenPair>> comparator = Comparators.fallback(
                Weighted.recordOrder(TokenPair.firstStringOrder(idx.getEntriesEnumeratorCarriar())),
                Comparators.reverse(Weighted.<TokenPair>weightOrder()));

        testSortWeightedTokenPairCommand(
                inputFile, randomisedFile, sortedFile,
                idx, comparator);

    }

    @Test
    @Ignore 
    public void testSortWeightedTokenPairCommand_Indexed() throws IOException, Exception {
        System.out.println("Testing SortWeightedTokenPairCommand (Indexed)");

        final File inputFile = TEST_FRUIT_INDEXED_SIMS;

        final boolean preindexedTokens1 = true;
        final boolean preindexedTokens2 = true;

        File randomisedFile = new File(TEST_OUTPUT_DIR,
                                       FRUIT_NAME + ".indexed.sims.randomised");
        File sortedFile = new File(TEST_OUTPUT_DIR,
                                   FRUIT_NAME + ".indexed.sims.sorted");
        File entryIndex = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".entry-index");
        File featureIndex = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".feature-index");


        final DoubleEnumerating idx = new DoubleEnumeratingDeligate(
                Enumerating.DEFAULT_TYPE,
                preindexedTokens1, preindexedTokens2,
                null, null);


        Comparator<Weighted<TokenPair>> comparator = Comparators.fallback(
                Weighted.recordOrder(TokenPair.secondIndexOrder()),
                Comparators.reverse(Weighted.<TokenPair>weightOrder()));

        testSortWeightedTokenPairCommand(inputFile, randomisedFile, sortedFile,
                                         idx, comparator);


    }

    private void testSortWeightedTokenPairCommand(
            File inputFile, File randomisedFile,
            File sortedFile, DoubleEnumerating idx,
            Comparator<Weighted<TokenPair>> comparator)
            throws IOException, Exception {


        assertTrue("Input file does not exist", inputFile.exists());
        assertTrue("Input file is not a regular file", inputFile.isFile());
        assertTrue("Input file length differs from input",
                   inputFile.length() > 0);


        // load a weighted token pair file
        WeightedTokenPairSource inputSource = openSource(inputFile, idx, false, false);
        List<Weighted<TokenPair>> inputList = ObjectIO.readAll(inputSource);
        inputSource.close();

        assertTrue("Input list is empty", inputList.size() > 0);

        // scamble it up
        shuffle(inputList);


        // write to a temporary file

        WeightedTokenPairSink randomisedSink =
                openSink(randomisedFile, idx, false, false, false);
        ObjectIO.copy(inputList, randomisedSink);
        randomisedSink.flush();
        randomisedSink.close();

        assertTrue("Randomised file does not exist", randomisedFile.exists());
        assertTrue("Randomised file is not a regular file", randomisedFile.isFile());
        {
            WeightedTokenPairSource x = openSource(randomisedFile, idx, false, false);
            WeightedTokenPairSource y = openSource(inputFile, idx, false, false);
            assertTrue("Randomised file length differs from input",
                       ObjectIO.flush(x) == ObjectIO.flush(y));
            x.close();
            y.close();
        }


        // run the command

        ExternalSortEventsCommand cmd = new ExternalSortEventsCommand();
        cmd.setSourceFile(randomisedFile);
        cmd.setDestinationFile(sortedFile);
        cmd.setCharset(DEFAULT_CHARSET);
        cmd.setMaxChunkSize(1000);
        cmd.setNumThreads(6);
        cmd.setTempFileFactory(new TempFileFactory(TEST_TMP_DIR));
        cmd.setIndexDeligate(idx);
        cmd.setComparator(comparator);

        cmd.runCommand();


        assertTrue("Sorted file does not exist", sortedFile.exists());
        assertTrue("Sorted file is not a regular file", sortedFile.isFile());


        {
            WeightedTokenPairSource x = openSource(inputFile, idx, false, false);
            WeightedTokenPairSource y = openSource(sortedFile, idx, false, false);
            assertTrue("Sorted file length differs from input",
                       ObjectIO.flush(x) == ObjectIO.flush(y));
            x.close();
            y.close();

        }
        // load the sorted output file and check it's sensible

        WeightedTokenPairSource sortedSource = openSource(sortedFile, idx, false, false);
        List<Weighted<TokenPair>> actual = ObjectIO.readAll(sortedSource);
        inputSource.close();

        for (int i = 1; i < actual.size(); i++) {
            Weighted<TokenPair> a = actual.get(i - 1);
            Weighted<TokenPair> b = actual.get(i);
            assertTrue("Sorted data does not match comparator: "
                    + a + " > " + b, comparator.compare(a, b) <= 0);
        }

        List<Weighted<TokenPair>> expected = new ArrayList<Weighted<TokenPair>>(inputList);
        Collections.sort(expected, comparator);


        for (int i = 0; i < actual.size(); i++) {
            assertTrue(format("Mismatch on row {0}: expected {1} but found {2} ",
                              i, expected.get(i), actual.get(i)),
                       comparator.compare(actual.get(i), expected.get(i)) == 0);
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

    private static WeightedTokenPairSource openSource(
            File file, DoubleEnumerating idx, boolean skip1, boolean skip2)
            throws IOException {
        return WeightedTokenPairSource.open(
                file, DEFAULT_CHARSET,
                idx, skip1, skip2);
    }

    private static WeightedTokenPairSink openSink(
            File file, DoubleEnumerating idx, boolean compact, boolean skip1, boolean skip2)
            throws IOException {
        return WeightedTokenPairSink.open(
                file, DEFAULT_CHARSET,
                idx, skip1, skip2, compact);
    }

}
