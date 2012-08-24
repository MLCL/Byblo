package uk.ac.susx.mlcl.lib.collect;

import it.unimi.dsi.fastutil.ints.AbstractIntIterator;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;

import org.w3c.dom.ranges.Range;

/**
 * 
 * 
 * @author hiam20
 * @see RangeSet: A Low-Memory Set Data Structure for Integers by Scott McMaster
 *      http
 *      ://www.codeproject.com/Articles/10308/RangeSet-A-Low-Memory-Set-Data-
 *      Structure-for-Integ
 */
public class IntRangeSet4 extends AbstractIntSet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1230172450875537978L;

	private final ObjectSortedSet<Range> ranges;

	private int size;

	public IntRangeSet4() {
		this.ranges = new ObjectRBTreeSet<Range>();
		size = 0;
	}

	public IntRangeSet4(int... elements) {
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
		return ranges.contains(newRange(element));
	}

	@Override
	public void clear() {
		ranges.clear();
		size = 0;
	}

	@Override
	public boolean add(final int element) {
		final Range range = new SingleElementRange(element);

		final ObjectSortedSet<Range> tailSet = ranges.tailSet(range);
		if (!tailSet.isEmpty() && tailSet.first().contains(element))
			return false;

		final ObjectSortedSet<Range> headSet = ranges.headSet(range);

		final Range below = headSet.isEmpty() ? null : headSet.last();
		final Range above = tailSet.isEmpty() ? null : tailSet.first();

		if (below != null && below.getHigh() == element - 1) {
			if (above != null && above.getLow() == element + 1) {
				// ranges below and above, so merge the existing ranges.
				if (below.getClass() == MultiElementRange.class) {
					tailSet.remove(above);
					((MultiElementRange) below).high = above.getHigh();
				} else if (above.getClass() == MultiElementRange.class) {
					headSet.remove(below);
					((MultiElementRange) above).low = below.getLow();
				} else {
					headSet.remove(below);
					tailSet.remove(above);
					tailSet.add(newRange(below.getLow(), above.getHigh()));
				}

			} else {
				// ranges below only, so extends it
				if (below.getClass() == MultiElementRange.class) {
					((MultiElementRange) below).high++;
				} else {
					headSet.remove(below);
					tailSet.add(newRange(below.getLow(), below.getHigh() + 1));
				}
			}
		} else if (above != null && above.getLow() == element + 1) {
			// ranges above only, so extends it
			if (above.getClass() == MultiElementRange.class) {
				((MultiElementRange) above).low--;
			} else {
				tailSet.remove(above);
				tailSet.add(newRange(above.getLow() - 1, above.getHigh()));
			}
		} else {
			// no conjoining ranges, so add a new singleton range
			tailSet.add(range);
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
		// ranges.trim();
	}

	@Override
	public boolean remove(final int element) {

		Range newRange = new SingleElementRange(element);

		final ObjectSortedSet<Range> tailSet = ranges.tailSet(newRange);

		if (tailSet.isEmpty())
			return false;

		final Range current = tailSet.first();

		if (!current.contains(element))
			return false;
		//
		//
		// final int rangeIdx = findIndexOfRangeFor(element);
		// if (rangeIdx < 0)
		// return false;

		// OK, we have it. There are four cases.
		// final Range range = ranges.get(rangeIdx);
		if (current.getLow() == element && current.getHigh() == element) {

			// 1. It's part of a single-element range. Remove the whole thing.
			ranges.remove(current);
			// ranges.remove(rangeIdx);

		} else if (current.getLow() == element) {
			// 2. We have a range that we need to shrink from the bottom.

			((MultiElementRange) current).low++;

			// ranges.set(rangeIdx, newRange(range.getLow() + 1,
			// range.getHigh()));
		} else if (current.getHigh() == element) {
			// 3. We have a range that we need to shrink from the top.

			((MultiElementRange) current).high--;

			// ranges.set(rangeIdx, newRange(range.getLow(), range.getHigh() -
			// 1));
		} else {
			// 4. Darn, we have to split a range.

			tailSet.remove(current);
			ranges.add(newRange(current.getLow(), element - 1));
			ranges.add(newRange(element + 1, current.getHigh()));
			//
			// ranges.set(rangeIdx, newRange(range.getLow(), element - 1));
			//
			// ranges.add(rangeIdx + 1, newRange(element + 1, range.getHigh()));
		}

		// We also have to make sure to decrement the cached count.
		--size;
		return true;
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

	//
	// /**
	// * Find the index of the range containing the given element currently in
	// the
	// * list, if any.
	// *
	// * @param element
	// * what we're looking for.
	// * @return index of the containing range if found, or the appropriate
	// * insertion point (a negative number) otherwise
	// */
	// private int findIndexOfRangeFor(final int element) {
	// if (ranges.isEmpty())
	// return -1;
	//
	// int low = 0;
	// int high = ranges.size() - 1;
	// while (low <= high) {
	// final int middle = (low == high) ? low : ((low + high) / 2);
	// final Range curRange = ranges.get(middle);
	//
	// if (curRange.contains(element))
	// return middle;
	// else if (curRange.getLow() > element)
	// high = middle - 1;
	// else if (curRange.getHigh() < element)
	// low = middle + 1;
	// else
	// return -middle;
	// }
	//
	// // Standard data structures hack to indicate the appropriate insertion
	// // point as a part of this call.
	// return -(low + 1);
	// }

	/**
	 * Add a range of the appropriate type depending on whether the start and
	 * end of the range are equal.
	 */
	private void addRange(final int low, final int high) {
		assert ranges.isEmpty() || ranges.last().getHigh() < low;
		//
		// assert ranges.isEmpty()
		// || ranges.get(ranges.size() - 1).getHigh() < low;

		ranges.add(newRange(low, high));
	}

	@Override
	public String toString() {
		return ranges.toString();
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
			rangeIter = ranges.iterator();
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
