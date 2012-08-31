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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.susx.mlcl.testing.AbstractObjectTest;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class AbstractIntSetTest<T extends IntSet> extends
		AbstractObjectTest<T> {

	public T newInstance(IntCollection c) {
		return newInstance(IntCollection.class, c);
	}

	public T newInstance(Collection<? extends Integer> c) {
		return newInstance(Collection.class, c);
	}

	public T newInstance(int[] c) {
		return newInstance(int[].class, c);
	}

	/**
	 * Test the ctor that allows us to create from an integer array.
	 * 
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testIntIterableConstructors() throws IllegalArgumentException,
			SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		System.out.println("testConstructors()");

		// make sure to feed it unordered data.

		T rs;
		// = newInstance(6, 5, 7, 10, 12, 9);
		// assertContainsAllOf(rs, 6, 5, 7, 10, 12, 9);
		// assertEquals(6, rs.size());

		// Let's give it a regular array in addition to just the param array.
		rs = newInstance(new int[] { 6, 5, 7, 10, 12, 9 });
		assertContainsAllOf(IntIterators.pour(rs.iterator()), 6, 5, 7, 10, 12,
				9);
		assertEquals(6, IntIterators.pour(rs.iterator()).size());

		// Try an empty one.
		rs = newInstance(new int[] {});
		assertEquals(0, IntIterators.pour(rs.iterator()).size());

		// IntCollection

		rs = newInstance(IntArrayList.wrap(new int[] { 6, 5, 7, 10, 12, 9 }));
		assertContainsAllOf(IntIterators.pour(rs.iterator()), 6, 5, 7, 10, 12,
				9);
		assertEquals(6, IntIterators.pour(rs.iterator()).size());

		// Collection<Integer>

		rs = newInstance(Arrays.asList(new Integer[] { 6, 5, 7, 10, 12, 9 }));
		assertContainsAllOf(IntIterators.pour(rs.iterator()), 5, 6, 7, 9, 10,
				12);
		assertEquals(6, IntIterators.pour(rs.iterator()).size());

	}

	@Test
	public void testIntIterableClone() {
		T s = newInstance();
		assertEquals(s, clone(s));

		s = newInstance(IntArrayList.wrap(new int[] { 1, 10, 100, 1000 }));
		assertEquals(s, clone(s));

	}

	@Test
	public void testIntIterableCloneWithSerialization() {
		T s = newInstance();
		assertEquals(s, assertCloneWithSerialization(s));

		s = newInstance(IntArrayList.wrap(new int[] { 1, 10, 100, 1000 }));
		assertEquals(s, assertCloneWithSerialization(s));

	}

	//
	// ===============================================================
	//
	static void assertContainsAnyOf(IntCollection set, int... elements) {
		for (int element : elements)
			if (set.contains(element))
				return;
		fail("contained none of " + set);
	}

	static void assertContainsAllOf(IntCollection set, int... elements) {
		for (int element : elements)
			assertTrue("does not contain " + element, set.contains(element));
	}

	static void assertContainsNoneOf(IntCollection set, int... elements) {
		for (int element : elements)
			assertFalse("contains " + element, set.contains(element));
	}

	static void assertAddAllOf(IntCollection set, int... elements) {
		for (int element : elements)
			assertTrue("failed to add " + element, set.add(element));
	}

	/**
	 * Test the isEmpty method.
	 */
	@Test
	public void isIntCollectionEmpty() throws InstantiationException,
			IllegalAccessException {
		System.out.println("isEmpty()");

		T rs = newInstance();
		assertTrue(rs.isEmpty());

		rs.add(42);
		assertFalse(rs.isEmpty());

		rs.remove(42);
		assertTrue(rs.isEmpty());

		rs.addAll(IntArrayList.wrap(new int[] { 42, 32, 43, 54, 76 }));
		assertFalse(rs.isEmpty());

		rs.clear();
		assertTrue(rs.isEmpty());
		assertEquals(0, rs.size());
	}

	/**
	 * Test the Clear method.
	 */
	@Test
	public void testIntCollectionClear() throws InstantiationException,
			IllegalAccessException {
		System.out.println("testClear()");

		T rs = newInstance();

		// Clear on an empty set.
		rs.clear();
		assertEquals(0, rs.size());

		// Add a few items and try again.
		assertAddAllOf(rs, 6, 7, 8, 10);
		assertEquals(4, rs.size());

		rs.clear();
		assertEquals(0, rs.size());
	}

	@Test
	public void testIntCollectionContains() {
		System.out.println("testIntCollectionContains()");

		T instance = newInstance();
		assertContainsNoneOf(instance, 0, 1, 2, 3, Integer.MAX_VALUE - 1,
				Integer.MAX_VALUE);

		instance.add(1);
		assertFalse(instance.contains(0));
		assertTrue(instance.contains(1));
		assertFalse(instance.contains(2));

	}

	@Test
	public void testIntCollectionAdd() {
		System.out.println("testIntCollectionAdd()");

		T rs = newInstance();

		assertTrue(rs.add(5));
		assertEquals(1, rs.size());
		assertContainsAllOf(rs, 5);

		// Test expanding the range downward.
		assertTrue(rs.add(4));
		assertContainsAllOf(rs, 4, 5);

		// Test expanding the range upward.
		assertTrue(rs.add(6));
		assertContainsAllOf(rs, 4, 5, 6);

		// Test forcing addition of a new range.
		assertTrue(rs.add(8));
		assertContainsAllOf(rs, 4, 5, 6, 8);

		for (int i = 1; i <= 1; i++) {
			final T s = newInstance(IntArrayList.wrap(new int[i]));
			assertTrue(s.add(1));
			assertEquals(1 + i, s.size());
			assertTrue(s.contains(1));
			assertTrue(s.add(2));
			assertTrue(s.contains(2));
			assertEquals(2 + i, s.size());
			assertFalse(s.add(1));
			assertFalse(s.remove(3));
			assertTrue(s.add(3));
			assertEquals(3 + i, s.size());
			assertTrue(s.contains(1));
			assertTrue(s.contains(2));
			assertTrue(s.contains(2));
			assertEquals(new IntOpenHashSet(i == 0 ? new int[] { 1, 2, 3 }
					: new int[] { 0, 1, 2, 3 }),
					new IntOpenHashSet(s.iterator()));
			assertTrue(s.remove(3));
			assertEquals(2 + i, s.size());
			assertTrue(s.remove(1));
			assertEquals(1 + i, s.size());
			assertFalse(s.contains(1));
			assertTrue(s.remove(2));
			assertEquals(0 + i, s.size());
			assertFalse(s.contains(1));
		}
	}

	@Test
	public void testIntCollectionClone() {
		T s = newInstance();

		assertEquals(s, clone(s));
		s.add(0);
		assertEquals(s, clone(s));
		s.add(0);
		assertEquals(s, clone(s));
		s.add(1);
		assertEquals(s, clone(s));
		s.add(2);
		assertEquals(s, clone(s));
		s.remove(0);
		assertEquals(s, clone(s));
	}

	/**
	 * Test the ctor that allows us to create from an integer array.
	 * 
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testIntCollectionConstructors()
			throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		System.out.println("testIntCollectionConstructors()");

		// make sure to feed it unordered data.

		T rs;
		// = newInstance(6, 5, 7, 10, 12, 9);
		// assertContainsAllOf(rs, 6, 5, 7, 10, 12, 9);
		// assertEquals(6, rs.size());

		// Let's give it a regular array in addition to just the param array.
		rs = newInstance(new int[] { 6, 5, 7, 10, 12, 9 });
		assertContainsAllOf(rs, 6, 5, 7, 10, 12, 9);
		assertEquals(6, rs.size());

		// Try an empty one.
		rs = newInstance(new int[] {});
		assertEquals(0, rs.size());

		// IntCollection

		rs = newInstance(IntArrayList.wrap(new int[] { 6, 5, 7, 10, 12, 9 }));
		assertContainsAllOf(rs, 6, 5, 7, 10, 12, 9);
		assertEquals(6, rs.size());

		// Collection<Integer>

		rs = newInstance(Arrays.asList(new Integer[] { 6, 5, 7, 10, 12, 9 }));
		assertContainsAllOf(rs, 5, 6, 7, 9, 10, 12);
		assertEquals(6, rs.size());

	}

	/**
	 * Basic tests for the Count property.
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@Test
	public void testSize() throws InstantiationException,
			IllegalAccessException {
		System.out.println("testSize()");

		T rs = newInstance();
		assertEquals(0, rs.size());

		// Add one.
		rs.add(5);
		assertEquals(1, rs.size());

		// Remove it.
		rs.remove(5);
		assertEquals(0, rs.size());

		// Add two adjacent.
		rs.add(6);
		rs.add(7);
		assertEquals(2, rs.size());

		// Add a third one that's adjacent. Interesting because it gets us a
		// nontrivial range in the implementation.
		rs.add(8);
		assertEquals(3, rs.size());

		rs.remove(7);
		assertEquals(2, rs.size());

		rs.remove(7);
		assertEquals(2, rs.size());
	}

	/**
	 * Test adding and removing elements, making sure to hit all of the
	 * different types of ranges.
	 */
	@Test
	public void testRangeTypes() throws InstantiationException,
			IllegalAccessException {
		System.out.println("testRangeTypes()");

		T rs = newInstance();

		// Default is empty.
		assertEquals(0, rs.size());

		// Add one.
		rs.add(5);
		assertTrue(rs.contains(5));
		assertEquals(1, rs.size());

		// Add a non-adjacent one.
		rs.add(7);
		assertContainsAllOf(rs, 5, 7);
		assertEquals(2, rs.size());

		// Add one that causes the ranges to be merged.
		rs.add(6);
		assertContainsAllOf(rs, 5, 6, 7);
		assertEquals(3, rs.size());

		// Test removing an element that leaves a multiple-element range.
		rs.add(8);
		rs.add(9);
		assertContainsAllOf(rs, 5, 6, 7, 8, 9);
		assertEquals(5, rs.size());
		rs.remove(8);
		assertContainsAllOf(rs, 5, 6, 7, 9);
		assertContainsNoneOf(rs, 8);
		assertEquals(4, rs.size());

		// Remove one off the end.
		rs.remove(9);
		assertContainsAllOf(rs, 5, 6, 7);
		assertContainsNoneOf(rs, 8, 9);
		assertEquals(3, rs.size());

		// Remove one in the middle leaving two single-element ranges.
		rs.remove(6);
		assertContainsAllOf(rs, 5, 7);
		assertContainsNoneOf(rs, 8, 9, 6);
		assertEquals(2, rs.size());
	}

	/**
	 * Test the different conditions that can occur when adding an element.
	 */
	@Test
	public void testIntSetAdd() {
		System.out.println("testIntSetAdd()");

		T rs = newInstance();

		assertTrue(rs.add(5));
		assertEquals(1, rs.size());
		assertContainsAllOf(rs, 5);

		// Test expanding the range downward.
		assertTrue(rs.add(4));
		assertContainsAllOf(rs, 4, 5);

		// Test expanding the range upward.
		assertTrue(rs.add(6));
		assertContainsAllOf(rs, 4, 5, 6);

		// Test forcing addition of a new range.
		assertTrue(rs.add(8));
		assertContainsAllOf(rs, 4, 5, 6, 8);

		// Test adding an already-existing element (no-op).
		assertFalse(rs.add(8));
		assertContainsAllOf(rs, 4, 5, 6, 8);
		assertEquals(4, rs.size());
	}

	/**
	 * Test the different conditions that can occur when removing an element.
	 */
	@Test
	public void testRemove() throws InstantiationException,
			IllegalAccessException {
		System.out.println("testRemove()");

		T rs = newInstance();

		// Test removing with nothing there (no-op).
		assertEquals(0, rs.size());
		assertFalse(rs.remove(5));
		assertEquals(0, rs.size());

		assertAddAllOf(rs, 4, 5, 6, 7, 8, 12);
		assertContainsAllOf(rs, 4, 5, 6, 7, 8, 12);
		assertContainsNoneOf(rs, 3, 9, 10, 11, 13);

		// Test removing from a single-element range.
		assertTrue(rs.remove(12));
		assertContainsAllOf(rs, 4, 5, 6, 7, 8);
		assertContainsNoneOf(rs, 12);

		// Test removing from the top of a range.
		assertTrue(rs.remove(8));
		assertContainsAllOf(rs, 4, 5, 6, 7);
		assertContainsNoneOf(rs, 12, 8);

		// Test removing from the bottom of a range.
		assertTrue(rs.remove(4));
		assertContainsAllOf(rs, 5, 6, 7);
		assertContainsNoneOf(rs, 12, 8, 4);

		// Test removing from the middle of a range.
		assertTrue(rs.remove(6));
		assertContainsAllOf(rs, 5, 7);
		assertContainsNoneOf(rs, 12, 8, 4, 6);

		// remove everything
		assertTrue(rs.remove(5));
		assertTrue(rs.remove(7));
		assertContainsNoneOf(rs, 12, 8, 4, 6, 5, 7);

	}

	@Test
	public void testRetainAll() {
		System.out.println("testRetainAll()");

		T instance = newInstance();
		assertAddAllOf(instance, 1, 2, 3, 4, 5, 6, 7, 8, 9);

		instance.retainAll(IntArrayList.wrap(new int[] { 2, 4, 6, 8, 10, 12 }));
		assertContainsAllOf(instance, 2, 4, 6, 8);
		assertContainsNoneOf(instance, 1, 3, 5, 7, 9, 10, 12);
		assertEquals(4, instance.size());
	}

	/**
	 * Randomly test and verify with some larger sets.
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@Test
	public void testStress() throws InstantiationException,
			IllegalAccessException {
		System.out.println("testStress()");

		Random rand = newRandom();

		for (int setNum = 0; setNum < 50; ++setNum) {
			String description = "Iteration #" + setNum;

			int setSize = rand.nextInt(1000);
			int setRange = 1 + rand.nextInt(1000);
			T rs = newInstance();
			IntOpenHashSet hs = new IntOpenHashSet();

			int[] values = new int[setSize];

			// Test Add.
			for (int j = 0; j < setSize; ++j) {
				values[j] = rand.nextInt(setRange);
				rs.add(values[j]);
				hs.add(values[j]);

				// Verify.
				assertTrue(description, rs.contains(values[j]));
				assertEquals(description, hs.size(), rs.size());
				for (int k = 0; k < setRange; ++k) {
					assertEquals(description, hs.contains(k), rs.contains(k));
				}
			}

			// Test Remove.
			for (int j = 0; j < setSize; ++j) {
				rs.remove(values[j]);
				hs.remove(values[j]);

				// Verify.
				assertFalse(description, rs.contains(values[j]));
				assertEquals(description, hs.size(), rs.size());
				for (int k = 0; k < setRange; ++k) {
					assertEquals(description, hs.contains(k), rs.contains(k));
				}
			}
		}
	}

	@Test
	public void testStress2() {
		System.out.println("testStress2()");

		final IntSet refInstance = new IntRBTreeSet();
		final IntSet instance = newInstance();

		int repeats = 5000;
		Random rand = newRandom();
		int maxValue = 10000;
		int maxArrayLength = 1000;

		for (int r = 0; r < repeats; r++) {
			final int x = rand.nextInt(Integer.MAX_VALUE);

			if (x % 2 == 0) {
				// add(int)

				final int element = rand.nextInt(maxValue);
				assertEquals(Integer.toString(element), instance.add(element),
						refInstance.add(element));

			} else if (x % 3 == 0) {
				// remove(int)

				final int element = rand.nextInt(maxValue);
				assertEquals(Integer.toString(element),
						instance.remove(element), refInstance.remove(element));

			} else if (x % 5 == 0) {
				// rem(int)

				final int element = rand.nextInt(maxValue);
				assertEquals(Integer.toString(element), instance.rem(element),
						refInstance.rem(element));

			} else if (x % 7 == 0) {
				// contains(int)

				final int element = rand.nextInt(maxValue);
				assertEquals(Integer.toString(element),
						instance.contains(element),
						refInstance.contains(element));

			} else if (x % 11 == 0) {
				// addAll(IntCollection)

				final IntCollection elements = randomIntArrayList(rand,
						maxValue, rand.nextInt(maxArrayLength));
				assertEquals(elements.toString(), instance.addAll(elements),
						refInstance.addAll(elements));

			} else if (x % 13 == 0) {
				// containsAll(IntCollection)

				final IntCollection elements = randomIntArrayList(rand,
						maxValue, rand.nextInt(maxArrayLength));
				assertEquals(elements.toString(),
						instance.containsAll(elements),
						refInstance.containsAll(elements));

			} else if (x % 17 == 0) {
				// removeAll(IntCollection)

				final IntCollection elements = randomIntArrayList(rand,
						maxValue, rand.nextInt(maxArrayLength));
				assertEquals(elements.toString(), instance.removeAll(elements),
						refInstance.removeAll(elements));

			} else if (x % 19 == 0) {
				// toIntArray()

				Assert.assertEquals(new IntOpenHashSet(instance.toIntArray()),
						new IntOpenHashSet(refInstance.toIntArray()));

			} else if (x % 23 == 0) {
				// toIntArray()

				Assert.assertEquals(
						new IntOpenHashSet(instance.toArray(new int[instance
								.size()])),
						new IntOpenHashSet(refInstance
								.toIntArray(new int[refInstance.size()])));

			} else if (x % 29 == 0) {
				// retainAll(IntCollection)
				assertEquals(Integer.toString(x), refInstance, instance);

				final IntCollection elements = randomIntArrayList(rand,
						maxValue, rand.nextInt(maxArrayLength));

				assertEquals(elements.toString(), instance.retainAll(elements),
						refInstance.retainAll(elements));

			} else if (x % 31 == 0) {
				// iterator()

				assertEquals(IntIterators.pour(instance.iterator(),
						new IntOpenHashSet()), IntIterators.pour(
						refInstance.iterator(), new IntOpenHashSet()));

				// } else {
				// assertEquals(refInstance, instance);
			}

			assertEquals(Integer.toString(x), refInstance, instance);
		}
	}

	/**
	 * Test the method that adds multiple elements to the set in one call.
	 */
	@Test
	public void testAddMultiple() throws InstantiationException,
			IllegalAccessException {
		System.out.println("testAddMultiple()");

		T rs = newInstance();

		// Add some into an empty set.
		assertTrue(rs.addAll(IntArrayList
				.wrap(new int[] { 5, 6, 7, 9, 10, 12 })));
		assertContainsAllOf(rs, 5, 6, 7, 9, 10, 12);
		assertEquals(6, rs.size());

		// Add some into the existing set. This is a different code path than
		// the empty case.
		assertTrue(rs.addAll(IntArrayList.wrap(new int[] { 2, 3, 8, 14, 15 })));
		assertContainsAllOf(rs, 2, 3, 5, 6, 7, 8, 9, 10, 12, 14, 15);
		assertEquals(11, rs.size());

		// Add the everything again
		assertFalse(rs.addAll(IntArrayList.wrap(new int[] { 5, 6, 7, 9, 10, 12,
				2, 3, 8, 14, 15 })));
		assertEquals(11, rs.size());

		// add IntCollection
		assertTrue(rs.addAll(IntArrayList.wrap(new int[] { 10, 1, 30, 13 })));
		assertContainsAllOf(rs, 1, 2, 3, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 30);
		assertEquals(14, rs.size());

		assertTrue(rs.addAll(Arrays
				.asList(new Integer[] { 6, 0, 31, 29, 12, 16 })));
		assertContainsAllOf(rs, 6, 0, 31, 29, 12, 16);
		assertEquals(18, rs.size());
	}

	/**
	 * Test the ctor that allows us to create from an integer array.
	 * 
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testSetConstructors() throws IllegalArgumentException,
			SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		System.out.println("testSetConstructors()");

		// make sure to feed it unordered data.

		// Throw in some duplicates.
		T rs = newInstance(new int[] { 6, 5, 7, 10, 12, 9, 6, 5, 7, 10, 12, 9 });
		assertContainsAllOf(rs, 6, 5, 7, 10, 12, 9);
		assertEquals(6, rs.size());

	}

}
