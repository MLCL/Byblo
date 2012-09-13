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
package uk.ac.susx.mlcl.lib.collect;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import uk.ac.susx.mlcl.lib.MemoryUsage;
import uk.ac.susx.mlcl.lib.MiscUtil;
import uk.ac.susx.mlcl.testing.HighMemoryTestCategory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class IntBitSetTest extends AbstractIntSortedSetTest<IntBitSet> {

    @Override
    public Class<? extends IntBitSet> getImplementation() {
        return IntBitSet.class;
    }

    @Test
    @Category(HighMemoryTestCategory.class)
    @Ignore
    public void testPeekMemoryUsage() {
        System.out.println("testPeekMemoryUsage()");

        // Test needs at least 500MB of memory. Fail if it isn't available.
        final long requiredMemory = (1L << 29);
        Assert.assertTrue(String.format(
                "Please re-run tests with at least %s of available memory.",
                MiscUtil.humanReadableBytes(requiredMemory)), Runtime
                .getRuntime().maxMemory() > requiredMemory);

        IntBitSet instance = newInstance();
        instance.add(0);
        instance.add(Integer.MAX_VALUE);

        final MemoryUsage mu = new MemoryUsage();
        mu.add(instance);
        System.out.println("> object memory: " + mu.getInfoString());
        System.out.println("> system memory: " + MiscUtil.memoryInfoString());

        // Expected memory usage is 256 MiB of 2^31 bits @ 8 bits per bytes
        final double expectedSizeMB = (Integer.MAX_VALUE + 1.0)
                / (8 * 1024 * 1024);
        final double actualSizeMB = (double) mu.getInstanceSizeBytes()
                / (1024 * 1024);
        final double allowedErrorMB = 1.0 / 1024;// Allow +/- 1KiB
        Assert.assertEquals(expectedSizeMB, actualSizeMB, allowedErrorMB);

    }

    /**
     * Test the method that adds multiple elements to the set in one call.
     */
    @Test
    public void testAddAll_IntBitSet() throws InstantiationException,
            IllegalAccessException {

        final IntBitSet instance = newInstance();

        IntBitSet other1 = newInstance();
        other1.addAll(5, 6, 7, 9, 10, 12);
        assertTrue(instance.addAll(other1));
        assertContainsAllOf(instance, 5, 6, 7, 9, 10, 12);
        assertEquals(6, instance.size());

        // Add some into the existing set.
        IntBitSet other2 = newInstance();
        other2.addAll(2, 3, 8, 14, 15);
        assertTrue(instance.addAll(other2));
        assertContainsAllOf(instance, 2, 3, 5, 6, 7, 8, 9, 10, 12, 14, 15);
        assertEquals(11, instance.size());

        // Add everything again
        Assert.assertFalse(instance.addAll(other1));
        Assert.assertFalse(instance.addAll(other2));
        assertContainsAllOf(instance, 2, 3, 5, 6, 7, 8, 9, 10, 12, 14, 15);
        assertEquals(11, instance.size());

    }

    /**
     * Test the method that adds multiple elements to the set in one call.
     */
    @Test
    public void testRemoveAll_IntBitSet() throws InstantiationException,
            IllegalAccessException {

        final IntBitSet instance = newInstance();
        instance.addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

        IntBitSet other1 = newInstance();
        other1.addAll(5, 6, 7, 9, 10, 12);
        assertTrue(instance.removeAll(other1));
        assertContainsNoneOf(instance, 0, 5, 6, 7, 9, 10, 12, 16);
        assertContainsAllOf(instance, 1, 2, 3, 4, 8, 11, 13, 14, 15);
        assertEquals(9, instance.size());

        IntBitSet other2 = newInstance();
        other2.addAll(2, 3, 8, 14, 15);
        assertTrue(instance.removeAll(other2));
        assertContainsNoneOf(instance, 0, 2, 3, 5, 6, 7, 8, 9, 10, 12, 14, 15,
                16);
        assertContainsAllOf(instance, 1, 4, 11, 13);
        assertEquals(4, instance.size());

        // Remove everything again
        Assert.assertFalse(instance.removeAll(other1));
        Assert.assertFalse(instance.removeAll(other2));
        assertContainsNoneOf(instance, 0, 2, 3, 5, 6, 7, 8, 9, 10, 12, 14, 15,
                16);
        assertContainsAllOf(instance, 1, 4, 11, 13);
        assertEquals(4, instance.size());

    }

    /**
     * Test the method that adds multiple elements to the set in one call.
     */
    @Test
    public void testRetainAll_IntBitSet() throws InstantiationException,
            IllegalAccessException {

        final IntBitSet instance = newInstance();
        instance.addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

        IntBitSet other1 = newInstance();
        other1.addAll(5, 6, 7, 9, 10, 12);
        assertTrue(instance.retainAll(other1));
        assertContainsNoneOf(instance, 0, 1, 2, 3, 4, 8, 11, 13, 14, 15, 16);
        assertContainsAllOf(instance, 5, 6, 7, 9, 10, 12);
        assertEquals(6, instance.size());

        IntBitSet other2 = newInstance();
        other2.addAll(2, 3, 5, 8, 10, 14, 15);
        assertTrue(instance.retainAll(other2));
        assertContainsNoneOf(instance, 0, 1, 2, 3, 4, 8, 9, 11, 12, 13, 14, 15,
                16);
        assertContainsAllOf(instance, 5, 10);
        assertEquals(2, instance.size());

        // Retain everything again
        Assert.assertFalse(instance.retainAll(other1));
        Assert.assertFalse(instance.retainAll(other2));
        assertContainsNoneOf(instance, 0, 1, 2, 3, 4, 8, 9, 11, 12, 13, 14, 15,
                16);
        assertContainsAllOf(instance, 5, 10);
        assertEquals(2, instance.size());

    }

    /**
     * Test the method that adds multiple elements to the set in one call.
     */
    @Test
    public void testContainsAll_IntBitSet() throws InstantiationException,
            IllegalAccessException {

        final IntBitSet instance = newInstance();
        instance.addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

        IntBitSet other1 = newInstance();
        other1.addAll(5, 6, 7, 9, 10, 12);
        assertTrue(instance.containsAll(other1));

        IntBitSet other2 = newInstance();
        other2.addAll(2, 3, 5, 8, 10, 14, 15, 16);
        Assert.assertFalse(instance.containsAll(other2));

    }
}
