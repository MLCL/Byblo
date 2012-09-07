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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSortedSet;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import uk.ac.susx.mlcl.testing.SlowTestCategory;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class AbstractIntSortedSetTest<T extends IntSortedSet> extends
		AbstractIntSetTest<T> {

	@Test
	public void testComparator() {
		System.out.println("testComparator()");
		IntSortedSet instance = newInstance();
		assertNull(instance.comparator());
	}

	@Test
	public void testFirstInt() {
		System.out.println("testFirstInt()");
		IntSortedSet instance = newInstance();

		try {
			instance.firstInt();
			Assert.fail("Expecting NoSuchElementException");
		} catch (NoSuchElementException e) {
			// Pass
		}

		instance.add(99);
		assertEquals(99, instance.firstInt());

		instance.add(100);
		assertEquals(99, instance.firstInt());

		instance.add(95);
		instance.add(97);
		assertEquals(95, instance.firstInt());

		instance.remove(95);
		assertEquals(97, instance.firstInt());

		instance.removeAll(IntArrayList.wrap(new int[] { 99, 100, 97 }));

		try {
			instance.firstInt();
			Assert.fail("Expecting NoSuchElementException");
		} catch (NoSuchElementException e) {
			// Pass
		}
	}

	@Test
	public void testLastInt() {
		System.out.println("testLastInt()");
		IntSortedSet instance = newInstance();

		try {
			instance.lastInt();
			Assert.fail("Expecting NoSuchElementException");
		} catch (NoSuchElementException e) {
			// Pass
		}

		instance.add(99);
		assertEquals(99, instance.lastInt());

		instance.add(100);
		assertEquals(100, instance.lastInt());

		instance.add(98);
		assertEquals(100, instance.lastInt());

		instance.add(101);
		instance.add(102);
		assertEquals(102, instance.lastInt());

		instance.remove(102);
		assertEquals(101, instance.lastInt());

		instance.removeAll(IntArrayList.wrap(new int[] { 98, 99, 100, 101 }));

		try {
			instance.lastInt();
			Assert.fail("Expecting NoSuchElementException");
		} catch (NoSuchElementException e) {
			// Pass
		}

	}

	@Test
	public void testHeadSet() {
		System.out.println("testHeadSet()");

		IntSortedSet instance = newInstance();
		instance.addAll(IntArrayList.wrap(new int[] { 1, 2, 4, 8, 16, 32, 64 }));

		IntSortedSet headSet = instance.headSet(4);
		assertEquals(2, headSet.size());
		assertContainsAllOf(headSet, 1, 2);
		assertContainsNoneOf(headSet, 4, 8, 16, 32, 64);

		// Test iteration
		IntIterator it = headSet.iterator();
		assertTrue(it.hasNext());
		assertEquals(1, it.nextInt());
		assertTrue(it.hasNext());
		assertEquals(2, it.nextInt());
		assertFalse(it.hasNext());

		IntSortedSet headHeadSet = headSet.headSet(2);
		assertEquals(1, headHeadSet.size());
		assertTrue(headHeadSet.contains(1));
		assertContainsNoneOf(headHeadSet, 2, 4, 8, 16, 32, 64);

		IntSortedSet emptyHeadSet = headHeadSet.headSet(0);
		assertEquals(0, emptyHeadSet.size());
		assertContainsNoneOf(emptyHeadSet, 1, 2, 4, 8, 16, 32, 64);

		// TODO: More headset tests
	}

	@Test
	public void testTailSet() {
		System.out.println("testTailSet()");

		IntSortedSet instance = newInstance();
		instance.addAll(IntArrayList.wrap(new int[] { 1, 2, 4, 8, 16, 32, 64 }));

		IntSortedSet tailSet = instance.tailSet(32);
		assertEquals(2, tailSet.size());
		assertContainsAllOf(tailSet, 32, 64);
		assertContainsNoneOf(tailSet, 1, 2, 4, 8, 16);

		IntIterator it = tailSet.iterator();
		assertTrue(it.hasNext());
		assertEquals(32, it.nextInt());
		assertTrue(it.hasNext());
		assertEquals(64, it.nextInt());
		assertFalse(it.hasNext());

		IntSortedSet tailTailSet = tailSet.tailSet(64);
		assertEquals(1, tailTailSet.size());
		assertTrue(tailTailSet.contains(64));
		assertContainsNoneOf(tailTailSet, 1, 2, 4, 8, 16, 32);

		IntSortedSet emptyTailSet = tailTailSet.tailSet(65);
		assertEquals(0, emptyTailSet.size());
		assertContainsNoneOf(emptyTailSet, 1, 2, 4, 8, 16, 32, 64);

		// TODO: More tail-set tests
	}

	@Test
	public void testSubSetSymmetry() {
		System.out.println("testSubSetSymmetry()");

		final int[] arr = new int[] { 2, 3, 5, 7, 8, 11 };
		IntSortedSet instance = newInstance();
		instance.addAll(IntArrayList.wrap(arr));

		for (int i = 0; i < 15; i++)
			for (int j = 0; j < i; j++) {
				IntSortedSet a = instance.headSet(i).tailSet(j);
				IntSortedSet b = instance.tailSet(j).headSet(i);
				IntSortedSet c = instance.subSet(j, i);
				assertEquals(j + ":" + i, a, b);
				assertEquals(j + ":" + i, a, c);
				assertEquals(j + ":" + i, b, c);
				for (int k = 0; k < 15; k++)
					for (int l = 0; l < k; l++) {
						IntSortedSet d = b.headSet(i).tailSet(j);
						IntSortedSet e = c.tailSet(j).headSet(i);
						IntSortedSet f = a.subSet(j, i);
						assertEquals(j + ":" + i, d, e);
						assertEquals(j + ":" + i, d, f);
						assertEquals(j + ":" + i, e, f);
					}
			}
	}

	@Test
	public void testSubSet() {
		System.out.println("testSubSet()");
		IntSortedSet instance = newInstance();
		instance.addAll(IntArrayList.wrap(new int[] { 1, 2, 4, 8, 16, 32, 64 }));

		IntSortedSet subSet = instance.subSet(2, 32);
		assertEquals(4, subSet.size());
		assertContainsAllOf(subSet, 2, 4, 8, 16);
		assertContainsNoneOf(subSet, 1, 32, 64);

		// Use non-existent elements as boundaries
		subSet = instance.subSet(3, 31);
		assertEquals(3, subSet.size());
		assertContainsAllOf(subSet, 4, 8, 16);
		assertContainsNoneOf(subSet, 1, 2, 32, 64);

		// Double subset
		IntSortedSet subSubSet = subSet.subSet(4, 16);
		assertEquals(2, subSubSet.size());
		assertContainsAllOf(subSubSet, 4, 8);
		assertContainsNoneOf(subSubSet, 1, 2, 16, 32, 64);

		// Triple subset
		IntSortedSet subSubSubSet = subSubSet.subSet(5, 99);
		assertEquals(1, subSubSubSet.size());
		assertContainsAllOf(subSubSubSet, 8);
		assertContainsNoneOf(subSubSubSet, 1, 2, 4, 16, 32, 64);

		// TODO: More subset tests
	}

	/**
	 * Test iterating.
	 */
	@Test
	public void testIteration() throws InstantiationException,
			IllegalAccessException {
		System.out.println("testIteration()");

		T rs = newInstance();

		// Test an enumerator over the empty set.
		IntIterator it = rs.iterator();
		assertFalse(it.hasNext());
		assertExhaustedIterator(it);

		// Test with a single element.
		rs.add(5);
		it = rs.iterator();
		assertTrue(it.hasNext());
		assertEquals(5, (int) it.next());
		assertExhaustedIterator(it);

		// Go over a couple of elements in a row.
		rs.add(6);
		it = rs.iterator();
		assertTrue(it.hasNext());
		assertEquals(5, (int) it.next());
		assertTrue(it.hasNext());
		assertEquals(6, (int) it.next());
		assertExhaustedIterator(it);

		// Test a couple of discontinuous ranges.
		rs.add(7);
		rs.add(9);
		rs.add(10);
		rs.add(12);
		it = rs.iterator();
		assertTrue(it.hasNext());
		assertEquals(5, (int) it.next());
		assertTrue(it.hasNext());
		assertEquals(6, (int) it.next());
		assertTrue(it.hasNext());
		assertEquals(7, (int) it.next());
		assertTrue(it.hasNext());
		assertEquals(9, (int) it.next());
		assertTrue(it.hasNext());
		assertEquals(10, (int) it.next());
		assertTrue(it.hasNext());
		assertEquals(12, (int) it.next());
		assertExhaustedIterator(it);
	}

	/**
	 * It was discovered that iteration caused problems with integer max value.
	 */
	@Test
	@Category(SlowTestCategory.class)
	public void testMaxIndexIterators() {
		System.out.println("testMaxIndexIterators()");
		int[] testData = new int[] { 0, 1, 50, 99, Integer.MAX_VALUE / 2,
				Integer.MAX_VALUE - 1, Integer.MAX_VALUE };

		IntArrays.mergeSort(testData);

		IntList forwardsData = IntArrayList.wrap(testData);
		IntList backwardsData = IntArrayList.wrap(IntArrays.reverse(Arrays
				.copyOf(testData, testData.length)));

		T instance = newInstance();

		for (int datum : forwardsData)
			instance.add(datum);

		IntBidirectionalIterator it = instance.iterator();

		Assert.assertFalse(it.hasPrevious());
		Assert.assertTrue(it.hasNext());

		// iterate forwards
		for (int datum : forwardsData) {
			int actual = it.nextInt();
			System.out.println("n: " + actual);
			Assert.assertEquals(datum, actual);
		}

		Assert.assertFalse(it.hasNext());
		Assert.assertTrue(it.hasPrevious());

		// iterate backwards
		for (int datum : backwardsData) {
			int actual = it.previousInt();
			System.out.println("p: " + actual);
			Assert.assertEquals(datum, actual);
		}

		Assert.assertFalse(it.hasPrevious());
		Assert.assertTrue(it.hasNext());

	}

}
