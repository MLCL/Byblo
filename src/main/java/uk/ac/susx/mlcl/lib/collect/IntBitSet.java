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

import static java.lang.Math.min;
import it.unimi.dsi.fastutil.ints.AbstractIntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.AbstractIntSortedSet;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSortedSet;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Signed;
import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

/**
 * An ordered set of <em>positive</em> integers that uses
 * {@link java.util.BitSet} to store elements.
 * 
 * Performance is extremely fast when allocated, running in constant time. It is
 * many times fast than even an highly optimized hash-set such as
 * {@link it.unimi.dsi.fastutil.ints.IntOpenHashSet}. Allocation can take some
 * time, though even a worst case allocation pattern is unlikely to take more
 * than a few 10s of milliseconds on modern hardware.
 * 
 * Memory requirements are dependent on the value of the largest element stored
 * in the set, rather than the number of elements. For densely packed, low
 * valued elements the memory requirements are typically substantially lower
 * than competitive implementations such as
 * {@link it.unimi.dsi.fastutil.ints.IntArrayList}. When elements are sparsely
 * distributed (< 1/32) or high valued the memory requirement can be much
 * higher. However, the memory is bounded at 256MiB for all positive integers,
 * which can be an attractive characteristic. For comparison
 * {@link it.unimi.dsi.fastutil.ints.IntArrayList} and
 * {@link it.unimi.dsi.fastutil.ints.IntOpenHashSet} are bounded at 2GiB, though
 * the latter will only be able to store about 75% the integers effectively.
 * 
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@NotThreadSafe
@Nonnull
public final class IntBitSet extends AbstractIntSortedSet implements
		Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4491432473600170796L;
	/**
	 * The default initial size is 64.
	 */
	private static final int DEFAULT_INITIAL_MAX_ELEMENT = 1 << 6;
	/**
	 * Constant used to indicate a null element.
	 */
	public static final int NO_ELEMENT = -1;
	/**
	 * Backing BitSet
	 */
	private final BitSet bits;

	//
	// ========================================================
	// Constructors
	// ========================================================
	//

	/**
	 * Instantiate a new <code>IntBitSet</code> initializing it to stores values
	 * up to and including <code>expectedMaxElement</code>.
	 * 
	 * @param expectedMaxElement
	 *            largest expected element value
	 */
	public IntBitSet(int expectedMaxElement) {
		if (expectedMaxElement < 0)
			throw new IllegalArgumentException("expectedMaxElement < 0");
		bits = new BitSet(expectedMaxElement);
	}

	/**
	 * Instantiate a new <code>IntBitSet</code>.
	 */
	public IntBitSet() {
		this(DEFAULT_INITIAL_MAX_ELEMENT);
	}

	/**
	 * Instantiate a new <code>IntBitSet</code> initializing it with the
	 * elements contained in <code>collection</code>.
	 * 
	 * @param collection
	 */
	public IntBitSet(@Nonnull final IntCollection collection) {
		this(collection.size() > 0 ? max(collection)
				: DEFAULT_INITIAL_MAX_ELEMENT);
		addAll(collection);
	}

	/**
	 * Instantiate a new <code>IntBitSet</code> initializing it with the
	 * elements contained in <code>collection</code>.
	 * 
	 * @param collection
	 */
	public IntBitSet(@Nonnull final Collection<? extends Integer> collection) {
		this(collection.size() > 0 ? max(collection)
				: DEFAULT_INITIAL_MAX_ELEMENT);

		addAll(collection);
	}

	/**
	 * Instantiate a new <code>IntBitSet</code> initializing it with the
	 * elements contained in <code>array</code>.
	 * 
	 * @param array
	 */
	public IntBitSet(int... array) {
		this(array.length > 0 ? ArrayUtil.max(array)
				: DEFAULT_INITIAL_MAX_ELEMENT);
		addAll(array);
	}

	/**
	 * Cloning constructor.
	 * 
	 * @param other
	 *            instance to clone
	 */
	private IntBitSet(@Nonnull final IntBitSet other) {
		Preconditions.checkNotNull(other, "other");
		this.bits = (BitSet) other.bits.clone();
	}

	//
	// ========================================================
	// Public methods
	// ========================================================
	//

	@Override
	public boolean contains(@Nonnegative int element) {
		assert element >= 0;
		return bits.get(element);
	}

	@Override
	public boolean add(@Nonnegative final int element) {
		assert element >= 0;
		if (bits.get(element)) {
			return false;
		} else {
			bits.set(element);
			return true;
		}
	}

	public boolean addAll(int... array) {
		assert array != null;
		boolean modified = false;
		for (int element : array)
			modified = add(element) || modified;
		return modified;
	}

	@Override
	public boolean remove(@Nonnegative final int element) {
		assert element >= 0;
		if (bits.get(element)) {
			bits.clear(element);
			return true;
		} else {
			return false;
		}
	}

	public boolean removeAll(int... array) {
		assert array != null;
		boolean modified = false;
		for (int element : array)
			modified = remove(element) || modified;
		return modified;
	}

	@Override
	public void clear() {
		bits.clear();
	}

	@Override
	public boolean isEmpty() {
		return bits.isEmpty();
	}

	@Override
	public int size() {
		return bits.cardinality();
	}

	@Override
	public String toString() {
		return bits.toString();
	}

	@Override
	@Nullable
	public IntComparator comparator() {
		return null;
	}

	@Override
	public IntSortedSet subSet(@Nonnegative final int fromElement,
			@Nonnegative final int toElement) {
		assert toElement >= 0;
		assert fromElement >= 0;
		assert toElement >= fromElement;
		return new SubSet(fromElement, toElement);
	}

	@Override
	public IntSortedSet headSet(@Nonnegative final int toElement) {
		assert toElement >= 0;
		return new SubSet(NO_ELEMENT, toElement);
	}

	@Override
	public IntSortedSet tailSet(@Nonnegative final int fromElement) {
		assert fromElement >= 0;
		return new SubSet(fromElement, NO_ELEMENT);
	}

	@Override
	public int firstInt() {
		final int result = bits.nextSetBit(0);
		if (result == NO_ELEMENT)
			throw new NoSuchElementException(Integer.toString(result));
		return result;
	}

	@Override
	public int lastInt() {
		final int result = previousSetBit(Integer.MAX_VALUE);
		if (result == NO_ELEMENT)
			throw new NoSuchElementException(Integer.toString(result));
		return result;
	}

	@Override
	public IntBidirectionalIterator iterator() {
		return new IteratorImpl(0, NO_ELEMENT, NO_ELEMENT);
	}

	@Override
	public IntBidirectionalIterator iterator(@Nonnegative final int fromElement) {
		assert fromElement >= 0;
		return new IteratorImpl(fromElement, NO_ELEMENT, NO_ELEMENT);
	}

	@Override
	public Object clone() {
		return new IntBitSet(this);
	}

	//
	// ========================================================
	// Private methods
	// ========================================================
	//

	/**
	 * Returns the index of the first bit that is set to <code>true</code> that
	 * occurs on or after the specified starting index. If no such bit exists
	 * then -1 is returned.
	 * 
	 * @param from
	 *            the index to start checking from (inclusive).
	 * @return the index of the next set bit.
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative.
	 */
	private int nextSetBit(@Nonnegative final int from)
			throws IndexOutOfBoundsException {
		assert from >= 0;
		return bits.nextSetBit(from);
	}

	/**
	 * Returns the index of the last bit that is set to <code>true</code> that
	 * occurs strictly before the specified starting index. If no such bit
	 * exists then -1 is returned.
	 * 
	 * @param from
	 *            the index to start checking from (exclusive).
	 * @return the index of the previous set bit.
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative.
	 */
	@CheckReturnValue
	@Nonnegative
	private int previousSetBit(@Nonnegative final int from)
			throws IndexOutOfBoundsException {
		if (from < 0 && from != NO_ELEMENT)
			throw new IndexOutOfBoundsException("from < 0");
		// TODO A linear search takes forever; binary search may improve things
		int element = (from == NO_ELEMENT ? bits.length() : Math.min(from,
				bits.length())) - 1;
		while (element >= 0 && !bits.get(element))
			--element;
		assert (from == NO_ELEMENT ? true : element < from) : from + ">="
				+ element;
		assert element >= 0 || element == NO_ELEMENT;
		return element;
	}

	//
	// ========================================================
	// Private classes
	// ========================================================
	//

	@VisibleForTesting
	protected final class SubSet extends AbstractIntSortedSet {
		private final int fromElement;
		private final int toElement;
		private final boolean isFromElementSet;
		private final boolean isToElementSet;

		private SubSet(final int fromElement, final int toElement) {
			if (fromElement < 0 && fromElement != NO_ELEMENT)
				throw new IllegalArgumentException("fromElement < 0");
			if (toElement < 0 && toElement != NO_ELEMENT)
				throw new IllegalArgumentException("toElement < 0");
			if (fromElement != NO_ELEMENT && toElement != NO_ELEMENT
					&& toElement < fromElement)
				throw new IllegalArgumentException("toElement < fromElement");
			if (fromElement == NO_ELEMENT && toElement == NO_ELEMENT)
				throw new IllegalArgumentException(
						"Meaningless attempt to create a subset without range delibmeters.");

			this.fromElement = fromElement;
			this.toElement = toElement;
			this.isFromElementSet = fromElement != NO_ELEMENT;
			this.isToElementSet = toElement != NO_ELEMENT;
		}

		@Override
		@CheckReturnValue
		public IntComparator comparator() {
			return IntBitSet.this.comparator();
		}

		@Override
		public boolean contains(int k) {
			if (isFromElementSet && k < fromElement)
				return false;
			if (isToElementSet && k >= toElement)
				return false;
			return IntBitSet.this.contains(k);
		}

		@Override
		@CheckReturnValue
		public IntSortedSet subSet(final int fromElement, final int toElement) {
			if (fromElement == NO_ELEMENT && toElement == NO_ELEMENT)
				throw new IllegalArgumentException(
						"Meaningless attempt to create a subset without range delibmeters.");
			return new SubSet(Math.max(this.fromElement, fromElement), min(
					this.toElement, toElement));
		}

		@Override
		@CheckReturnValue
		public IntSortedSet headSet(final int toElement) {
			if (toElement == NO_ELEMENT)
				throw new IllegalArgumentException(
						"Meaningless attempt to create a headset without end delibmeter.");
			final int to = !isToElementSet ? toElement : min(this.toElement,
					toElement);
			return new SubSet(fromElement, to);
		}

		@Override
		@CheckReturnValue
		public IntSortedSet tailSet(final int fromElement) {
			if (fromElement == NO_ELEMENT)
				throw new IllegalArgumentException(
						"Meaningless attempt to create a tailset without start delibmeter.");
			final int from = !isFromElementSet ? fromElement : Math.max(
					this.fromElement, fromElement);
			return new SubSet(from, toElement);
		}

		@Override
		@CheckReturnValue
		public int firstInt() {
			return nextSetBit(fromElement);
		}

		@Override
		@CheckReturnValue
		public int lastInt() {
			return previousSetBit(toElement);
		}

		@Override
		@CheckReturnValue
		public int size() {
			int size = 0;
			final int start = nextSetBit(isFromElementSet ? fromElement : 0);
			final int end = isToElementSet ? toElement : bits.length();
			for (int e = start; e != NO_ELEMENT && e < end; e = nextSetBit(e + 1)) {
				++size;
			}
			return size;
		}

		@Override
		@CheckReturnValue
		public IntBidirectionalIterator iterator() {
			return iterator(isFromElementSet ? fromElement : 0);
		}

		@Override
		@CheckReturnValue
		public IntBidirectionalIterator iterator(final int startElement) {
			int boundedStartElement = startElement;
			if (isFromElementSet)
				boundedStartElement = Math
						.max(fromElement, boundedStartElement);
			if (isToElementSet)
				boundedStartElement = Math.min(toElement, boundedStartElement);
			return new IteratorImpl(boundedStartElement, fromElement, toElement);
		}

	}

	final class IteratorImpl extends AbstractIntBidirectionalIterator {

		/**
		 * The first element (inclusive) in the iteration range, or
		 * {@link IntBitSet#NO_ELEMENT} if there is no start. Note this is not
		 * necessarily the starting element.
		 */
		private final int fromElement;
		/**
		 * The last element (exclusive) in the iteration range, or
		 * {@link IntBitSet#NO_ELEMENT} if there no end. Note this is not
		 * necessarily the starting element.
		 */
		private final int toElement;

		private final boolean isFromElementSet;
		private final boolean isToElementSet;

		/**
		 * The element before the current position in the iteration. This value
		 * will be returned by a subsequent call to {@link #previous()} or
		 * {@link #previousInt()}. If there is no previous element that value
		 * will be {@link IntBitSet#NO_ELEMENT}.
		 */
		private int prevElement;
		/**
		 * The element after the current position in the iteration. This value
		 * will be returned by a subsequent call to {@link #next()} or
		 * {@link #nextInt()}. If there is no next element that value will be
		 * {@link IntBitSet#NO_ELEMENT}.
		 */
		private int nextElement;
		/**
		 * Current direction of the iterator. False if last call was to
		 * <code>previous()</code>, true otherwise.
		 */
		private boolean forwards;
		/**
		 * Whether or not a {@link #remove()} operation can be performed. A
		 * remove operation can only be performed after a call to one of
		 * {@link #next()}, {@link #nextInt()}, {@link #previous()} or
		 * {@link #previousInt()} which will clear the element returned by that
		 * operation. Further, a remove operation can only be performed once per
		 * iterator advancement.
		 */
		private boolean canRemove;

		/**
		 * Construct a new iterator over the containing IntBitSet instance, in
		 * the range <code>fromElement</code> to <code>toElement</code>, with
		 * the first call to {@link #next()} returning the the first element
		 * equal to or larger than <code>startElement</code>
		 * 
		 * @param startElement
		 *            minimum value for first call to {@link #next()}
		 * @param fromElement
		 *            iteration range minimum value (inclusive)
		 * @param toElement
		 *            iteration range maximum value (exclusive)
		 */
		IteratorImpl(@Nonnegative final int startElement,
				final int fromElement, final int toElement) {
			if (startElement < 0)
				throw new IllegalArgumentException("startElement < 0");
			if (fromElement < 0 && fromElement != NO_ELEMENT)
				throw new IllegalArgumentException("fromElement < 0");
			if (toElement < fromElement && toElement != NO_ELEMENT)
				throw new IllegalArgumentException("toElement < fromElement");

			this.fromElement = fromElement;
			this.toElement = toElement;
			this.isFromElementSet = fromElement != NO_ELEMENT;
			this.isToElementSet = toElement != NO_ELEMENT;

			prevElement = previousSetBit(startElement);
			nextElement = nextSetBit(startElement);
			forwards = true;
			canRemove = false;
		}

		@Override
		@CheckReturnValue
		public boolean hasNext() {
			return nextElement != NO_ELEMENT
					&& (!isToElementSet || nextElement < toElement);
		}

		@Override
		@CheckReturnValue
		public boolean hasPrevious() {
			return prevElement != NO_ELEMENT
					&& (!isFromElementSet || fromElement <= prevElement);
		}

		@Override
		@Nonnegative
		public int nextInt() {
			if (!hasNext())
				throw new NoSuchElementException(
						"iteration has no more elements");
			prevElement = nextElement;
			nextElement = nextSetBit(nextElement + 1);
			forwards = true;
			canRemove = true;
			return prevElement;
		}

		@Override
		@Nonnegative
		public int previousInt() {
			if (!hasPrevious())
				throw new NoSuchElementException(
						"iteration has no more elements");
			nextElement = prevElement;
			prevElement = previousSetBit(prevElement - 1);
			forwards = false;
			canRemove = true;
			return nextElement;
		}

		@Override
		public void remove() {
			if (!canRemove)
				throw new IllegalStateException("next/previous methods have "
						+ "not yet been called, or the remove method has "
						+ "already been called after the last call to the next"
						+ " method.");
			final boolean removed = IntBitSet.this
					.remove(forwards ? prevElement : nextElement);
			assert removed == true;
			canRemove = false;
		}

		@Override
		@CheckReturnValue
		@Nonnull
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("IntBitSetIterator[");

			builder.append("fromElement=");
			builder.append(!isFromElementSet ? "none" : fromElement);

			builder.append(", toElement=");
			builder.append(!isToElementSet ? "none" : toElement);

			builder.append(", prevElement=");
			builder.append(prevElement == NO_ELEMENT ? "none" : prevElement);

			builder.append(", nextElement=");
			builder.append(nextElement == NO_ELEMENT ? "none" : nextElement);

			builder.append(", direction=");
			builder.append(forwards ? "forwards" : "backwards");

			builder.append(", canRemove=");
			builder.append(canRemove);

			builder.append("]");
			return builder.toString();
		}

	}

	//
	// ===========================================================
	// Private static functions
	// ===========================================================
	//

	/**
	 * Find and return the largest integer value in the
	 * <code>IntCollection</code>.
	 * 
	 * TODO: Move to a utility class
	 * 
	 * @param iterable
	 * @return largest value in <code>collection</code>
	 * @throws IllegalArgumentException
	 *             if collection is null or empty
	 */
	@CheckReturnValue
	@Signed
	private static int max(@Nonnull final IntIterable iterable)
			throws IllegalArgumentException {
		Preconditions.checkNotNull(iterable, "iterable");
		final IntIterator it = iterable.iterator();
		if (!it.hasNext())
			throw new IllegalArgumentException("iterable is empty");
		int max = it.nextInt();
		while (it.hasNext())
			max = Math.max(max, it.nextInt());
		return max;
	}

	/**
	 * Find and return the smallest integer value in the
	 * <code>IntCollection</code>.
	 * 
	 * TODO: Move to a utility class
	 * 
	 * @param iterable
	 * @return smallest value in <code>collection</code>
	 * @throws IllegalArgumentException
	 *             if collection is null or empty
	 */
	@CheckReturnValue
	@Signed
	private static int max(@Nonnull final Iterable<? extends Integer> iterable)
			throws IllegalArgumentException {
		Preconditions.checkNotNull(iterable, "iterable");
		final Iterator<? extends Integer> it = iterable.iterator();
		if (!it.hasNext())
			throw new IllegalArgumentException("iterable is empty");
		int max = it.next();
		while (it.hasNext())
			max = Math.max(max, it.next());
		return max;
	}

}
