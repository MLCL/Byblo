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

package uk.ac.susx.mlcl.lib.io;

import junit.framework.Assert;
import org.junit.Test;
import uk.ac.susx.mlcl.lib.collect.ArrayUtil;
import uk.ac.susx.mlcl.testing.AbstractObjectTest;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA. User: hiam20 Date: 17/09/2012 Time: 15:09 To change this template use File | Settings |
 * File Templates.
 */
public class MergingObjectSourceTest extends AbstractObjectTest<MergingObjectSource> {

    @Override
    protected Class<? extends MergingObjectSource> getImplementation() {
        return MergingObjectSource.class;
    }

    @Test
    public void testSimpleExample() throws IOException {
        final int[][] arrays = {{5, 10, 15, 20}, {10, 13, 16, 19}, {2, 19, 26, 40}, {18, 22, 23, 24}};
        final int[] expected = {2, 5, 10, 10, 13, 15, 16, 18, 19, 19, 20, 22, 23, 24, 26, 40};
        simpleIntegerExampleTest(arrays, expected);
    }

    @Test
    public void testOddNumberOfLists() throws IOException {
        final int[][] arrays = {{5, 10, 15, 20}, {10, 13, 16, 19}, {2, 19, 26, 40}, {10, 13}, {2, 19}};
        final int[] expected = {2, 2, 5, 10, 10, 10, 13, 13, 15, 16, 19, 19, 19, 20, 26, 40};
        simpleIntegerExampleTest(arrays, expected);
    }

    @Test
    public void testSingleLists() throws IOException {
        final int[][] arrays = {{5, 10, 15, 20}};
        final int[] expected = {5, 10, 15, 20};
        simpleIntegerExampleTest(arrays, expected);
    }

    @Test
    public void testEmptyLists() throws IOException {
        final int[][] arrays = {{}, {}, {}, {}};
        final int[] expected = {};
        simpleIntegerExampleTest(arrays, expected);
    }

    @Test
    public void testSizeOneLists() throws IOException {
        final int[][] arrays = {{2}, {5}, {10}, {10}, {13}, {}, {15}, {16}, {19}, {19}, {20}, {26}, {40}, {}};
        final int[] expected = {2, 5, 10, 10, 13, 15, 16, 19, 19, 20, 26, 40};
        simpleIntegerExampleTest(arrays, expected);
    }


    @Test
    public void testRandomIntLists() throws IOException {
        final int numLists = 100;
        final int listLength = 100;

        final int[][] arrays = new int[numLists][listLength];
        final int[] expected = new int[numLists * listLength];
        final Random random = newRandom();
        for (int i = 0; i < numLists; i++) {
            for (int j = 0; j < listLength; j++) {
                arrays[i][j] = random.nextBoolean() ? random.nextInt() : -random.nextInt() - 1;
                expected[i * listLength + j] = arrays[i][j];
            }
            Arrays.sort(arrays[i]);
        }
        Arrays.sort(expected);

        simpleIntegerExampleTest(arrays, expected);
    }


    public void simpleIntegerExampleTest(final int[][] arrays, final int[] expected) throws IOException {


        final ObjectSource<Integer>[] sources = new ObjectSource[arrays.length];
        for (int i = 0; i < arrays.length; i++) {
            sources[i] = ObjectIO.asSource(Arrays.asList(ArrayUtil.box(arrays[i])));
        }

        final AtomicInteger comparisonCount = new AtomicInteger(0);
        final Comparator<Integer> comparator = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                comparisonCount.incrementAndGet();
                return o1 - o2;
            }
        };

        final ObjectSource<Integer> mergeSource = MergingObjectSource.merge(comparator, sources);

//
//        if (mergeSource.getClass() == MergingObjectSource.class) {
//            System.out.println(((MergingObjectSource) mergeSource).treeString());
//        }

        final int maxDepth = (mergeSource.getClass() == MergingObjectSource.class)
                ? ((MergingObjectSource) mergeSource).getMaxHeight() : 0;
        Assert.assertTrue("Depth exceeds log(k) implying an unbalanced tree.",
                maxDepth <= Math.ceil(Math.log(sources.length) / Math.log(2)));

        final boolean balanced = (mergeSource.getClass() == MergingObjectSource.class)
                ? ((MergingObjectSource) mergeSource).isBalanced() : true;
        Assert.assertTrue("unbalanced tree.", balanced);


        for (int i = 0; i < expected.length; i++) {
            Assert.assertTrue(mergeSource.hasNext());
            Assert.assertEquals(expected[i], (int) mergeSource.read());


            final int k = arrays.length;
            final int k_log_k = (int) Math.ceil(Math.log(k) / Math.log(2));
            int lowerBound = k > 1 ? (i + 1) : 0;
            int upperBound = ((i + k) * k_log_k);
            boolean boundsCorrect = lowerBound <= comparisonCount.get() && comparisonCount.get() <= upperBound;
//            System.out.printf("%d < %d < %d %s%n", lowerBound,  comparisonCount.get(), upperBound,boundsCorrect );
            Assert.assertTrue(boundsCorrect);
        }

        Assert.assertFalse(mergeSource.hasNext());
        try {
            mergeSource.read();
            Assert.fail("Expected IOException when reading an empty list.");
        } catch (IOException ex) {
            // pass
        } catch (NoSuchElementException ex) {
            // pass
        }

        if (mergeSource instanceof Closeable)
            ((Closeable) mergeSource).close();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
