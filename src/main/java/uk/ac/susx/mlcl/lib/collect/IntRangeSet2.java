package uk.ac.susx.mlcl.lib.collect;

import it.unimi.dsi.fastutil.ints.AbstractIntIterator;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntIterator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * 
 * 
 * @author hiam20
 * @see RangeSet: A Low-Memory Set Data Structure for Integers by Scott McMaster
 *      http
 *      ://www.codeproject.com/Articles/10308/RangeSet-A-Low-Memory-Set-Data-
 *      Structure-for-Integ
 */
public class IntRangeSet2 extends AbstractIntSet implements Serializable {

	private static final int INITIAL_CAPACITY = 16;
	/**
	 * 
	 */
	private static final long serialVersionUID = -1230172450875537978L;

	private int[] ranges;

	private int nRanges = 0;

	private int size;

	public IntRangeSet2() {
		this.ranges = new int[INITIAL_CAPACITY];
		size = 0;
	}

	public IntRangeSet2(int... elements) {
		this();
		addAllFromEmpty(elements);
	}

	@Override
	public IntIterator iterator() {
		return new IntRangeSetIterator();
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean contains(final int element) {
		return findIndexOfRangeFor(element) >= 0;
	}

	@Override
	public void clear() {
		ranges = new int[INITIAL_CAPACITY];
		nRanges = 0;
		size = 0;
	}

	@Override
	public boolean add(final int element) {
		final int rangeIdx = findIndexOfRangeFor(element);
		if (rangeIdx >= 0)
			return false;

		final int insertIdx = (-rangeIdx - 1) * 2;

		// Try to find a range we can expand to include this element.

		// XXX: hack to make the following pass
		ensureCapacity(nRanges + 1);

		// final int rangeBelowIdx = findIndexOfRangeFor(element - 1);
		final int rangeBelowIdx;
		if (insertIdx == 0)
			rangeBelowIdx = -2;
		else if (ranges[insertIdx - 2 + 1] == element - 1)
			rangeBelowIdx = insertIdx - 2;
		else
			rangeBelowIdx = -2;

		// final int rangeAboveIdx = findIndexOfRangeFor(element + 1);
		final int rangeAboveIdx;
		if (insertIdx == nRanges * 2)
			rangeAboveIdx = -2;
		else if (ranges[insertIdx] == element + 1)
			rangeAboveIdx = insertIdx;
		else
			rangeAboveIdx = -2;

		// Note: kinder to GC if we adjust if MultiElementRange is mutable
		if (rangeBelowIdx >= 0) {
			if (rangeAboveIdx >= 0) {
				// We have ranges below and above, so we can merge two existing
				// ranges.
				ranges[rangeBelowIdx + 1] = ranges[rangeAboveIdx + 1];
				
				System.arraycopy(ranges, rangeAboveIdx + 2, ranges,
						rangeAboveIdx, nRanges * 2 - (rangeAboveIdx + 2));
				--nRanges;
			} else {
				// We don't have a range above, so just expand the existing
				// range below.
				++ranges[rangeBelowIdx + 1];
			}
		} else if (rangeAboveIdx >= 0) {
			// We have an existing range above that we can expand downward.
			--ranges[rangeAboveIdx];
		} else {
			// We need a new single-element range.
			ensureCapacity(nRanges + 1);
			System.arraycopy(ranges, insertIdx, ranges, insertIdx + 2, nRanges
					* 2 - insertIdx);
			ranges[insertIdx] = ranges[insertIdx + 1] = element;
			++nRanges;
		}

		++size;
		return true;
	}

	public boolean addAll(int... elements) {
		if (size == 0) {
			return addAllFromEmpty(elements);
		} else {
			boolean modified = false;
			for (int element : elements)
				modified = add(element) || modified;
			return modified;
		}
	}

	public void trim() {
		if (ranges.length > nRanges * 2)
			ranges = Arrays.copyOf(ranges, nRanges * 2);
	}

	@Override
	public boolean remove(final int element) {

		final int rangeIdx = findIndexOfRangeFor(element);
		if (rangeIdx < 0)
			return false;

		// OK, we have it. There are four cases.
		final int low = ranges[rangeIdx * 2];
		final int high = ranges[rangeIdx * 2 + 1];
		// final Range range = ranges.get(rangeIdx);
		if (low == element && high == element) {

			System.arraycopy(ranges, (rangeIdx + 1) * 2, ranges, rangeIdx * 2,
					nRanges * 2 - (rangeIdx + 1) * 2);
			--nRanges;

		} else if (low == element) {
			// 2. We have a range that we need to shrink from the bottom.

			++ranges[rangeIdx * 2];
		} else if (high == element) {
			// 3. We have a range that we need to shrink from the top.
			--ranges[rangeIdx * 2 + 1];
		} else {
			// 4. Darn, we have to split a range.
			ranges[rangeIdx * 2 + 1] = element - 1;
			ensureCapacity(nRanges + 1);
			System.arraycopy(ranges, (rangeIdx + 1) * 2, ranges,
					(rangeIdx + 2) * 2, nRanges * 2 - (rangeIdx + 1) * 2);
			ranges[(rangeIdx + 1) * 2] = element + 1;
			ranges[(rangeIdx + 1) * 2 + 1] = high;
			++nRanges;
		}

		// We also have to make sure to decrement the cached count.
		--size;
		return true;
	}

	private void ensureCapacity(int nRangesRequired) {
		if (nRangesRequired * 2 > ranges.length) {
			int newLength = (int) ((nRangesRequired * 2 + 1) * 1.5);
			ranges = Arrays.copyOf(ranges, newLength);
		}
	}

	/**
	 * Find the index of the range containing the given element currently in the
	 * list, if any.
	 * 
	 * @param element
	 *            what we're looking for.
	 * @return index of the containing range if found, or the appropriate
	 *         insertion point (a negative number) otherwise
	 */
	private int findIndexOfRangeFor(final int element) {
		if (nRanges == 0)
			return -1;

		int low = 0;
		int high = nRanges - 1;
		while (low <= high) {
			final int middle = (low == high) ? low : ((low + high) / 2);

			final int curRangeLow = ranges[middle * 2];
			final int curRangeHigh = ranges[middle * 2 + 1];

			// TODO: optimise comparisons
			if (curRangeLow <= element && element <= curRangeHigh)
				return middle;
			else if (curRangeLow > element)
				high = middle - 1;
			else if (curRangeHigh < element)
				low = middle + 1;
			else
				return -middle;
		}

		// Standard data structures hack to indicate the appropriate insertion
		// point as a part of this call.
		return -(low + 1);
	}

	/**
	 * Add a range of the appropriate type depending on whether the start and
	 * end of the range are equal.
	 */
	private void addRange(final int low, final int high) {
		assert nRanges == 0 || ranges[(nRanges - 1) * 2 + 1] < low;
		ensureCapacity(nRanges + 1);
		ranges[(nRanges - 1) * 2] = low;
		ranges[(nRanges - 1) * 2 + 1] = high;
		++nRanges;
	}

	/**
	 * Add the given elements under the assumption that the set is currently
	 * empty.
	 * 
	 * @param elements
	 */
	private boolean addAllFromEmpty(final int[] elements)
			throws IllegalStateException {
		assert size == 0;

		if (elements.length == 0)
			return false;

		Arrays.sort(elements);
		int startElement = elements[0];
		int lastElement = elements[0];
		size = 1;
		for (int currentElementIdx = 1; currentElementIdx < elements.length; ++currentElementIdx) {
			if (elements[currentElementIdx] == lastElement) {
				// Duplicate -- skip it.
				continue;
			}

			// Else we need to bump the count.
			++size;

			// Now see if we need to add a new range.
			if (elements[currentElementIdx] == lastElement + 1) {
				// We're in the middle of a run -- continue.
				++lastElement;
			} else {
				// We need to add a new range.
				addRange(startElement, lastElement);

				// Move up.
				startElement = lastElement = elements[currentElementIdx];
			}
		}

		// Have to add the last range.
		addRange(startElement, lastElement);
		return true;
	}

	/**
	 * 
	 */
	class IntRangeSetIterator extends AbstractIntIterator implements
			IntIterator {

		private int currentRangeId = -1;
		private int nextElement = 0;

		IntRangeSetIterator() {
			currentRangeId = 0;
			if (currentRangeId < nRanges) {
				nextElement = ranges[currentRangeId * 2];
			}
		}

		@Override
		public boolean hasNext() {
			return currentRangeId < nRanges;
		}

		@Override
		public int nextInt() {
			if (!hasNext())
				throw new NoSuchElementException();

			final int element = nextElement;

			if (element < ranges[currentRangeId * 2 + 1]) {
				nextElement = element + 1;
			} else {
				++currentRangeId;
				if (currentRangeId < nRanges)
					nextElement = ranges[currentRangeId * 2];
			}
			return element;
		}
	}

}
