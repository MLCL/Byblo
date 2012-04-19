/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.tasks;

import uk.ac.susx.mlcl.lib.tasks.ObjectMergeTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import org.junit.*;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;
import static org.junit.Assert.*;

/**
 *
 * @author hiam20
 */
public class MergeTaskTest {

    public MergeTaskTest() {
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

        int n = 1000;
        Random rand = new Random(0);
        List<Integer> in1 = new ArrayList<Integer>();
        List<Integer> in2 = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            in1.add(rand.nextInt(100));
            in2.add(rand.nextInt(100));
        }

        Collections.sort(in1);
        Collections.sort(in2);

        List<Integer> out = new ArrayList<Integer>();

        Source<Integer> src1 = IOUtil.asSource(in1);
        Source<Integer> src2 = IOUtil.asSource(in2);
        Sink<Integer> sink = IOUtil.asSink(out);

        Comparator<Integer> comparator = new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }

        };

        ObjectMergeTask<Integer> instance = new ObjectMergeTask<Integer>();
        instance.setSourceA(src1);
        instance.setSourceB(src2);
        instance.setSink(sink);
        instance.setComparator(comparator);

        instance.run();

        assertEquals(n * 2, out.size());
        for (int i = 1; i < out.size(); i++)
            assertTrue(out.get(i - 1) <= out.get(i));
    }

}
