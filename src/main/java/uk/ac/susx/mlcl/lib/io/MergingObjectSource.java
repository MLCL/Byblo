/*
 * Copyright (c) 2010-2013, University of Sussex
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

import com.google.common.base.Preconditions;
import uk.ac.susx.mlcl.lib.Comparators;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

/**
 * <code>MergingObjectSource</code> performs a two-way merge on the output of two child <code>ObjectSource</code> objects;
 * such that if the child sources where sorted, the result will also be sorted.
 * <p/>
 * Instances of <code>MergingObjectSource</code> can be stacked hierarchically to produce a binary-tree of merging sources
 * known as a multi-way or k-way merge. The factory method {@link #merge(java.util.Comparator, ObjectSource[])} has been
 * provided to automatically build a balanced k-way merge tree from the given array of sources.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@NotThreadSafe
@CheckReturnValue
public final class MergingObjectSource<T> implements ObjectSource<T> {

    private final Comparator<T> comparator;

    private final ObjectSource<T> left;

    private final ObjectSource<T> right;

    @Nullable
    private T leftHead;

    @Nullable
    private T rightHead;

    private boolean initialised = false;

    /**
     * Construct a new <code>MergingObjectSource</code> instance, that merges the given <code>left</code> and
     * <code>right</code> instances, using the given <code>comparator</code>.
     *
     * @param left       first source to merge
     * @param right      second source to merge
     * @param comparator method of comparison between objects in the two sources
     * @throws NullPointerException     if any parameter is null
     * @throws IllegalArgumentException if left and right are the same object
     */
    public MergingObjectSource(final ObjectSource<T> left, final ObjectSource<T> right, final Comparator<T> comparator) {
        Preconditions.checkNotNull(left, "left");
        Preconditions.checkNotNull(right, "right");
        Preconditions.checkNotNull(comparator, "comparator");
        Preconditions.checkArgument(left != right, "left and right are the same object");
        this.left = left;
        this.right = right;
        this.comparator = comparator;
    }

    /**
     * Construct a new <code>MergingObjectSource</code> instance, that merges the given <code>left</code> and
     * <code>right</code> instances, using the underlying objects natural ordering. If they objects do not have a
     * natural order (i.e they do not implement {@link Comparable}) then expect failure in form of a {@link
     * ClassCastException} when an attempt is made to retrieve the first object..
     *
     * @param left  first source to merge
     * @param right second source to merge
     * @throws NullPointerException     if any parameter is null
     * @throws IllegalArgumentException if left and right are the same object
     */
    public MergingObjectSource(final ObjectSource<T> left, final ObjectSource<T> right) {
        this(left, right, Comparators.<T>naturalOrderIfPossible());
    }

    /**
     * Factory method producing an <code>ObjectSource</code> that k-way-merges all the input sources simultaneously.
     * This is achieved by building a tree of binary mergers using <code>MergingObjectSource</code> instances.
     * <p/>
     * The alternative is to perform a two-way merge on each pair of sources, writing the resource out somewhere, then
     * repeat until all parts have been merged. The asymptotic complexity of number of comparisons is identical between
     * these two methods O(nk log k) for k partitions, of average length n. They differ in that a multi-way merge
     * performs the entire operation at once, substantially reducing the I/O overhead if writing is expensive. The trade
     * of is that multi-merge has a slightly higher constant overhead per comparison, which results in it
     * under-performing when merging small lists.
     * <p/>
     * If <code>inputs</code> is empty then an empty source is returned. If inputs contains exactly one source, then
     * only that source is returned.
     *
     * @param comparator method of comparison between objects in the two sources
     * @param inputs     any number of <code>ObjectSource</code> objects to be merged
     * @param <T>        type of object contained in the sources
     * @return An object source that will merge all the inputs as it is read.
     * @throws NullPointerException if comparator is null
     */
    public static <T> ObjectSource<T> merge(final Comparator<T> comparator, final ObjectSource<T>... inputs) {
        if (inputs.length == 0) {
            return ObjectIO.<T>nullSource();
        }

        // Start by taking a copy of the input array
        final ObjectSource<T>[] result = Arrays.copyOf(inputs, inputs.length);

        // At each iteration of the outer loop we will half the number of sources in result by replacing each adjacent
        // pair of sources with a source that merges the two together.
        int n = result.length;
        while (n > 1) {

            int i = 1;
            int j = 0;
            while (i < n) {
                result[j] = new MergingObjectSource<T>(result[i - 1], result[i], comparator);
                i += 2;
                ++j;
            }
            // Handle odd number of inputs by copying the final source to the first element. The first element is used
            // so it will be combined in the next iteration, insuring the tree does not become unbalanced.
            if (n % 2 == 1) {
                result[j] = result[0];
                result[0] = result[i - 1];
                ++i;
                ++j;
            }

            n = j;
        }
        return result[0];
    }

    public static <T> ObjectSource<T> merge(final Comparator<T> comparator, final Collection<ObjectSource<T>> inputs) {
        return merge(comparator, inputs.toArray(new ObjectSource[inputs.size()]));
    }
    /**
     * Get the comparator that was set at construction time.
     *
     * @return the comparator
     */
    public Comparator<T> getComparator() {
        return comparator;
    }

    /**
     * Get the left hand <code>ObjectSource</code> that was set at construction time.
     *
     * @return first source
     */
    public ObjectSource<T> getLeft() {
        return left;
    }

    /**
     * Get the right hand <code>ObjectSource</code> that was set at construction time.
     *
     * @return second source
     */
    public ObjectSource<T> getRight() {
        return right;
    }

    /**
     * Close and free up resources associated with this source. Also closes child sources.
     *
     * @throws IOException thrown by <code>close()</code> on child sources
     */
    @Override
    public void close() throws IOException {
        left.close();
        right.close();
        leftHead = null;
        rightHead = null;
    }

    /**
     * Get whether this source is open; which is true if either or both of the child sources are open, false if both
     * child sources are closed.
     *
     * @return true if this source is open, false otherwise
     */
    @Override
    public boolean isOpen() {
        return left.isOpen() || right.isOpen();
    }

    @Override
    public T read() throws IOException {
        if (!initialised) initialise();

        if (leftHead == null) {
            if (rightHead == null) {
                throw new IOException("Source is empty.");
            } else {
                return popRight();
            }
        } else if (rightHead == null) {
            return popLeft();
        } else if (comparator.compare(leftHead, rightHead) <= 0) {
            return popLeft();
        } else {
            return popRight();
        }
    }

    @Override
    public boolean hasNext() throws IOException {
        if (!initialised) initialise();
        return leftHead != null || rightHead != null;
    }

    /**
     * Get the head of the first source, and update the head by reading the source.
     *
     * @return current head of the first source, or <code>null</code> if the source is exhausted or uninitialised
     * @throws IOException when bad things happen
     */
    private
    @Nullable
    T popLeft() throws IOException {
        T previousHead = leftHead;
        leftHead = left.hasNext() ? left.read() : null;
        return previousHead;
    }

    /**
     * Get the head of the second source, and update the head by reading the source.
     *
     * @return current head of the second source, or <code>null</code> if the source is exhausted or uninitialised
     * @throws IOException when bad things happen
     */
    private
    @Nullable
    T popRight() throws IOException {
        T previousHead = rightHead;
        rightHead = right.hasNext() ? right.read() : null;
        return previousHead;
    }

    /**
     * The algorithm requires access to the head element from both sources. Since peeking isn't normally supported they
     * must be read before comparison. This process must occur at some point after construction, but before the first
     * called to {@link #hasNext()} or {@link #read()}. Unfortunately this initializing may throw an exception, so we
     * are faced with a choice: either the constructors perform initialization, in which case they may throw exceptions,
     * or we have to check for initialization at every access. We opted for the second option, which while slightly
     * slower results in much cleaner API.
     *
     * @throws IOException when bad things happen
     */
    private void initialise() throws IOException {
        if (!initialised) {
            if (popLeft() != null)
                throw new AssertionError("Initialization occurred more than once.");
            if (popRight() != null)
                throw new AssertionError("Initialization occurred more than once.");
            initialised = true;
        }
    }

    public String treeString() throws IOException {
        StringBuilder builder = new StringBuilder();
        treeString(builder, "");
        return builder.toString();
    }

    private void treeString(StringBuilder builder, String linePrefix) throws IOException {
        if (!initialised) initialise();

        builder.append(linePrefix);
        builder.append("*-* ").append(leftHead);
        if (left.getClass() != MergingObjectSource.class) {
            builder.append(" <--\n");
        } else {
            builder.append("\n");
            ((MergingObjectSource) left).treeString(builder, linePrefix + "|    ");
        }

        builder.append(linePrefix);
        builder.append("\\-* ");
        builder.append(rightHead);
        if (right.getClass() != MergingObjectSource.class) {
            builder.append(" <--\n");
        } else {
            builder.append('\n');
            ((MergingObjectSource) right).treeString(builder, linePrefix + "     ");
        }
    }


    public
    @Nonnegative
    int getMaxHeight() {
        return 1 + Math.max(getMaxHeight(left), getMaxHeight(right));
    }

    public
    @Nonnegative
    int getMinHeight() {
        return 1 + Math.min(getMaxHeight(left), getMaxHeight(right));
    }

    private static
    @Nonnegative
    int getMaxHeight(final ObjectSource<?> node) {
        return node.getClass() == MergingObjectSource.class ? ((MergingObjectSource<?>) node).getMaxHeight() : 0;
    }

    private static
    @Nonnegative
    int getMinHeight(final ObjectSource<?> node) {
        return node.getClass() == MergingObjectSource.class ? ((MergingObjectSource<?>) node).getMinHeight() : 0;
    }

    private static <T> PeekableObjectSource<T> ensurePeekable(ObjectSource<T> source) {
        return source instanceof PeekableObjectSource
                ? ((PeekableObjectSource<T>) source)
                : new PeekableObjectSourceAdapter<T>(source);
    }


    /**
     * Get whether or not the binary tree rooted by this <code>MergingObjectSource</code> instance is balanced. If it is
     * balanced then the number of comparison operations will be optimal.
     * <p/>
     * A binary tree is defined balanced if (1) it is a leaf node, or (2) both the left and right sub-trees are balanced
     * and their height is within 1 of each other.
     *
     * @return
     */
    public boolean isBalanced() {
        return isBalanced(left) && isBalanced(right) && Math.abs(getMaxHeight(left) - getMaxHeight(right)) <= 1;
    }

    private static boolean isBalanced(final ObjectSource<?> node) {
        return node.getClass() == MergingObjectSource.class ? ((MergingObjectSource) node).isBalanced() : true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MergingObjectSource that = (MergingObjectSource) o;

        if (initialised != that.initialised) return false;
        if (!comparator.equals(that.comparator)) return false;
        if (!left.equals(that.left)) return false;
        if (leftHead != null ? !leftHead.equals(that.leftHead) : that.leftHead != null) return false;
        if (!right.equals(that.right)) return false;
        if (rightHead != null ? !rightHead.equals(that.rightHead) : that.rightHead != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = comparator.hashCode();
        result = 31 * result + left.hashCode();
        result = 31 * result + right.hashCode();
        result = 31 * result + (leftHead != null ? leftHead.hashCode() : 0);
        result = 31 * result + (rightHead != null ? rightHead.hashCode() : 0);
        result = 31 * result + (initialised ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MergingObjectSource[" +
                "comparator=" + comparator +
                ", left=" + left +
                ", right=" + right +
                ", leftHead=" + leftHead +
                ", rightHead=" + rightHead +
                ']';
    }
}
