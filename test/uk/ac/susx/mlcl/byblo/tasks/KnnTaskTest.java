/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.tasks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import org.junit.*;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;
import static org.junit.Assert.*;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.lib.Comparators;

/**
 *
 * @author hiam20
 */
public class KnnTaskTest {

    public KnnTaskTest() {
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
    public void testSortTask() {

        int nClasses = 100;
        int nNeighbours = 100;

        int n = nClasses * nNeighbours;
        Random rand = new Random(0);
        List<Weighted<TokenPair>> in = new ArrayList<Weighted<TokenPair>>();
        for (int classId = 0; classId < nClasses; classId++) {
            for (int j = 0; j < nNeighbours; j++) {
                int neighbourId = rand.nextInt(100);
                double proximity = rand.nextDouble();
                in.add(new Weighted<TokenPair>(
                        new TokenPair(classId, neighbourId), proximity));
            }
        }

        List<Weighted<TokenPair>> out = new ArrayList<Weighted<TokenPair>>();

        Source<Weighted<TokenPair>> src = IOUtil.asSource(in);
        Sink<Weighted<TokenPair>> sink = IOUtil.asSink(out);


        KnnTask<Weighted<TokenPair>> instance = new KnnTask<Weighted<TokenPair>>();

        int k = 5;
        instance.setSource(src);
        instance.setSink(sink);
        instance.setClassComparator(Weighted.recordOrder(TokenPair.firstIndexOrder()));
        instance.setNearnessComparator(Comparators.reverse(Weighted.<TokenPair>weightOrder()));
        instance.setK(k);

        instance.run();


        assertEquals(nClasses * k, out.size());

        for (int i = 1; i < out.size(); i++) {
            Weighted<TokenPair> a = out.get(i - 1);
            Weighted<TokenPair> b = out.get(i);
            if (a.record().id1() == b.record().id1()) {
                assertTrue(a.weight() >= b.weight());
            }

        }

    }

}
