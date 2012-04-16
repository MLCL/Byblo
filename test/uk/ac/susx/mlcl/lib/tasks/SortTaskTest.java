/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.tasks;

import uk.ac.susx.mlcl.lib.tasks.ObjectSortTask;
import java.util.ArrayList;
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
public class SortTaskTest {

    public SortTaskTest() {
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
        List<Integer> in = new ArrayList<Integer>();
        for (int i = 0; i < n; i++)
            in.add(rand.nextInt(100));

        List<Integer> out = new ArrayList<Integer>();

        Source<Integer> src = IOUtil.asSource(in);
        Sink<Integer> sink = IOUtil.asSink(out);

        Comparator<Integer> comparator = new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        };

        ObjectSortTask<Integer> instance = new ObjectSortTask<Integer>();
        instance.setSource(src);
        instance.setSink(sink);
        instance.setComparator(comparator);

        instance.run();

        assertEquals(n, out.size());
        for (int i = 1; i < out.size(); i++)
            assertTrue(out.get(i - 1) <= out.get(i));

    }

}
