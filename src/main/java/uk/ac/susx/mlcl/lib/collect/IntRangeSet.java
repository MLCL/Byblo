package uk.ac.susx.mlcl.lib.collect;

import it.unimi.dsi.fastutil.ints.AbstractIntIterator;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;

/**
 * 
 * TODO: Implement IntSortedSet interface
 * 
 * @author hiam20
 * @see RangeSet: A Low-Memory Set Data Structure for Integers by Scott McMaster
 *      http
 *      ://www.codeproject.com/Articles/10308/RangeSet-A-Low-Memory-Set-Data-
 *      Structure-for-Integ
 */
public class IntRangeSet extends AbstractIntSet implements Serializable,
		Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1230172450875537978L;

	private final ObjectArrayList<Range> rangeList;

	private int size;

	IntRangeSet(IntRangeSet other) {
		this.rangeList = other.rangeList.clone();
		size = other.size;
	}

	public IntRangeSet() {
		this.rangeList = new ObjectArrayList<Range>();
		size = 0;
	}

	public IntRangeSet(int... elements) {
		this();
		addAllFromEmpty(elements);
	}

	public IntRangeSet(IntCollection elements) {
		this();
		addAllFromEmpty(elements);
	}

	public IntRangeSet(Collection<? extends Integer> elements) {
		this();
		addAllFromEmpty(elements);
	}

	@Override
	public Object clone() {
		return new IntRangeSet(this);
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
		rangeList.clear();
		size = 0;
	}

	@Override
	public boolean add(final int element) {
		final int rangeIdx = findIndexOfRangeFor(element);
		if (rangeIdx >= 0)
			return false;

		final int insertIdx = -rangeIdx - 1;

		// Try to find a range we can expand to include this element.

		// final int rangeBelowIdx = findIndexOfRangeFor(element - 1);
		final int rangeBelowIdx;
		if (insertIdx == 0)
			rangeBelowIdx = -1;
		else if (rangeList.get(insertIdx - 1).getHigh() == element - 1)
			rangeBelowIdx = insertIdx - 1;
		else
			rangeBelowIdx = -1;

		// final int rangeAboveIdx = findIndexOfRangeFor(element + 1);
		final int rangeAboveIdx;
		if (insertIdx == rangeList.size())
			rangeAboveIdx = -1;
		else if (rangeList.get(insertIdx).getLow() == element + 1)
			rangeAboveIdx = insertIdx;
		else
			rangeAboveIdx = -1;

		// Note: kinder to GC if we adjust if MultiElementRange is mutable
		if (rangeBelowIdx >= 0) {
			final Range below = rangeList.get(rangeBelowIdx);
			if (rangeAboveIdx >= 0) {
				// We have ranges below and above, so we can merge two existing
				// ranges.
				final Range above = rangeList.get(rangeAboveIdx);
				if (below.getClass() == MultiElementRange.class) {
					((MultiElementRange) below).high = above.getHigh();
				} else
					rangeList.set(rangeBelowIdx,
							newRange(below.getLow(), above.getHigh()));
				rangeList.remove(rangeAboveIdx);
			} else {
				// We don't have a range above, so just expand the existing
				// range below.
				if (below.getClass() == MultiElementRange.class) {
					((MultiElementRange) below).high++;
				} else
					rangeList.set(rangeBelowIdx,
							newRange(below.getLow(), element));

			}
		} else if (rangeAboveIdx >= 0) {
			// We have an existing range above that we can expand downward.
			final Range above = rangeList.get(rangeAboveIdx);

			if (above.getClass() == MultiElementRange.class) {
				((MultiElementRange) above).low--;
			} else
				rangeList
						.set(rangeAboveIdx, newRange(element, above.getHigh()));
		} else {
			// We need a new single-element range.
			rangeList.add(insertIdx, newRange(element));
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
		rangeList.trim();
	}

	@Override
	public boolean remove(final int element) {

		final int rangeIdx = findIndexOfRangeFor(element);
		if (rangeIdx < 0)
			return false;

		// OK, we have it. There are four cases.
		final Range range = rangeList.get(rangeIdx);
		if (range.getLow() == element && range.getHigh() == element) {

			// 1. It's part of a single-element range. Remove the whole thing.
			rangeList.remove(rangeIdx);

		} else if (range.getLow() == element) {
			// 2. We have a range that we need to shrink from the bottom.
			rangeList.set(rangeIdx,
					newRange(range.getLow() + 1, range.getHigh()));
		} else if (range.getHigh() == element) {
			// 3. We have a range that we need to shrink from the top.
			rangeList.set(rangeIdx,
					newRange(range.getLow(), range.getHigh() - 1));
		} else {
			// 4. Darn, we have to split a range.
			rangeList.set(rangeIdx, newRange(range.getLow(), element - 1));

			rangeList.add(rangeIdx + 1, newRange(element + 1, range.getHigh()));
		}

		// We also have to make sure to decrement the cached count.
		--size;
		return true;
	}

	@Override
	public String toString() {
		return rangeList.toString();
	}

	/**
	 * Factory method
	 * 
	 * @param element
	 * @return
	 */
	private Range newRange(int element) {
		return new SingleElementRange(element);
	}

	/**
	 * 
	 * @param low
	 * @param high
	 * @return
	 */
	private Range newRange(int low, int high) {
		if (low == high)
			return new SingleElementRange(low);
		else
			return new MultiElementRange(low, high);
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
		if (rangeList.isEmpty())
			return -1;

		int low = 0;
		int high = rangeList.size() - 1;
		while (low <= high) {
			final int middle = (low == high) ? low : ((low + high) / 2);
			final Range curRange = rangeList.get(middle);

			if (curRange.contains(element))
				return middle;
			else if (curRange.getLow() > element)
				high = middle - 1;
			else if (curRange.getHigh() < element)
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
		assert rangeList.isEmpty()
				|| rangeList.get(rangeList.size() - 1).getHigh() < low;
		rangeList.add(newRange(low, high));
	}

	private boolean addAllFromEmpty(final IntCollection elements)
			throws IllegalStateException {
		return addAllFromEmpty(elements.toArray(new int[elements.size()]));
	}

	private boolean addAllFromEmpty(final Collection<? extends Integer> elements)
			throws IllegalStateException {
		return addAllFromEmpty(ArrayUtil.unbox(elements
				.toArray(new Integer[elements.size()])));
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
	 * Interface for a range of integers
	 */
	interface Range extends Serializable, Comparable<Range> {

		/**
		 * @return the low end of the range
		 */
		int getLow();

		/**
		 * @return the high end of the range
		 */
		int getHigh();

		/**
		 * 
		 * @param element
		 * @return whether or not the range contains <code>element</code>
		 */
		boolean contains(int element);
	}

	/**
	 * An implementation of <code>Range</code> for a single element
	 */
	@Immutable
	@CheckReturnValue
	static final class SingleElementRange implements Range {

		private static final long serialVersionUID = -1286296855911253261L;
		private final int value;

		SingleElementRange(final int value) {
			this.value = value;
		}

		@Override
		public int getLow() {
			return value;
		}

		@Override
		public int getHigh() {
			return value;
		}

		@Override
		public boolean contains(int element) {
			return element == this.value;
		}

		@Override
		public String toString() {
			return Integer.toString(value);
		}

		@Override
		public int compareTo(Range o) {
			return value < o.getLow() ? -1 : value > o.getHigh() ? +1 : 0;
		}

	}

	/**
	 * An implementation of <code>Range</code> for two or more contiguous
	 * elements
	 */
	static final class MultiElementRange implements Range {

		private static final long serialVersionUID = 7068295774353349575L;
		private int low;
		private int high;

		MultiElementRange(final int low, final int high)
				throws IllegalArgumentException {
			assert low < high : "low >= high";
			this.low = low;
			this.high = high;
		}

		@Override
		public int getLow() {
			return low;
		}

		@Override
		public int getHigh() {
			return high;
		}

		@Override
		public boolean contains(int element) {
			return low <= element && element <= high;
		}

		@Override
		public String toString() {
			return MessageFormat.format("{0}-{1}", low, high);
		}

		@Override
		public int compareTo(Range o) {
			return high < o.getLow() ? -1 : low > o.getHigh() ? +1 : 0;
		}

	}

	/**
	 * 
	 * @author hiam20
	 * 
	 */
	class IntRangeSetIterator extends AbstractIntIterator implements
			IntIterator {

		private final Iterator<Range> rangeIter;
		private Range currentRange = null;
		private int nextElement = 0;

		IntRangeSetIterator() {
			rangeIter = rangeList.iterator();
			if (rangeIter.hasNext()) {
				currentRange = rangeIter.next();
				nextElement = currentRange.getLow();
			}
		}

		@Override
		public boolean hasNext() {
			return currentRange != null;
		}

		@Override
		public int nextInt() {
			if (!hasNext())
				throw new NoSuchElementException();

			final int element = nextElement;
			if (element < currentRange.getHigh()) {
				nextElement = element + 1;
			} else if (rangeIter.hasNext()) {
				currentRange = rangeIter.next();
				nextElement = currentRange.getLow();
			} else {
				currentRange = null;
			}
			return element;
		}
	}

}
