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

package uk.ac.susx.mlcl.lib.tasks;

import com.google.common.base.Preconditions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.events.ProgressDelegate;
import uk.ac.susx.mlcl.lib.events.ProgressListener;
import uk.ac.susx.mlcl.lib.events.ProgressReporting;
import uk.ac.susx.mlcl.lib.io.ObjectIO;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.ObjectSource;
import uk.ac.susx.mlcl.lib.io.ObjectStore;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An <code>ObjectStoreSortTask</code> takes reads all the data fromList an <code>ObjectStore</code>, sorts it, then
 * writes the results out to a second <code>ObjectStore</code>. Ordering is defined either by setting a comparator, or
 * alternatively the natural ordering of the object will be used if possible. The <code>fromList</code> and
 * <code>to</code> stores may safely reference the same store, using it supported both reading and writing.
 *
 * @param <T> Object type to be sorted
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Nonnull
@CheckReturnValue
@NotThreadSafe
public abstract class ObjectStoreKWayMergeTask<T> extends AbstractTask implements ProgressReporting {

//    private static final Log LOG = LogFactory.getLog(ObjectStoreKWayMergeTask.class);
//
//    private final ProgressDelegate progress = new ProgressDelegate(this, true);
//
//    @Nullable
//    private final List<ObjectStore<T, ?>> inputs;
//
//    @Nullable
//    private ObjectStore<T, ?> output;
//
//    @Nullable
//    private Comparator<T> comparator;
//
//    public ObjectStoreKWayMergeTask(final List<ObjectStore<T, ?>> inputs, final ObjectStore<T, ?> output, final Comparator<T> comparator) {
//        addInputs(inputs);
//        setOutput(output);
//        setComparator(comparator);
//    }
//
//    public ObjectStoreKWayMergeTask(final List<ObjectStore<T, ?>> inputs, final ObjectStore<T, ?> output) {
//        this(inputs, output, Comparators.<T>naturalOrderIfPossible());
//    }
//
//    public ObjectStoreKWayMergeTask() {
//        inputs = null;
//        output = null;
//        setComparator(Comparators.<T>naturalOrderIfPossible());
//    }
//
//    public final Comparator<T> getComparator() {
//        return comparator;
//    }
//
//    @Nullable
//    public final List<ObjectStore<T, ?>> getInputs() {
//        return inputs;
//    }
//
//    public final void addInputs(final List<ObjectStore<T, ?>> inputs) {
//        Preconditions.checkNotNull(inputs, "inputs");
//        Preconditions.checkNotNull(!inputs.isEmpty(), "inputs is empty");
//
//        for (ObjectStore<T, ?> input : inputs) {
//            Preconditions.checkArgument(input.isReadable(), "input is not readable");
//            Preconditions.checkArgument(input.exists(), "input is not exist");
//        }
//        this.inputs.addAll(inputs);
//    }
//
//    @Nullable
//    public final ObjectStore<T, ?> getOutput() {
//        return output;
//    }
//
//    public final void setOutput(final ObjectStore<T, ?> output) {
//        if (output != this.output) {
//            Preconditions.checkNotNull(output, "output");
//            Preconditions.checkArgument(output.isWritable(), "output is not writable");
//
//            this.output = output;
//        }
//    }
//
//    public final void setComparator(final Comparator<T> comparator) {
//        Preconditions.checkNotNull(comparator, "comparator");
//        this.comparator = comparator;
//    }
////
////    abstract class Node implements ObjectSource<T> {
////
////        T head;
////
////        Node(T head) {
////            this.head = head;
////        }
////
////        abstract T peek();
////
////        abstract boolean isLeaf();
////
////
////        T read(Node left, Node right) throws IOException {
////            if (!left.hasNext()) {
////                if (!right.hasNext()) {
////                    return null;
////                } else {
////                    return right.read();
////                }
////            } else if (!right.hasNext()) {
////                return left.read();
////            } else {
////                if (getComparator().compare(left.peek(), right.peek()) <= 0) {
////                    return left.read();
////                } else {
////                    return right.read();
////                }
////            }
////        }
////
////
////
////        public Node mergeNodes(Node left, Node right) throws IOException {
////            return new Branch(read(left, right), left, right);
////        }
////
////    }
////
////    class Branch extends Node implements ObjectSource<T> {
////
////        Node left;
////        Node right;
////
////        Branch(T value, Node left, Node right) {
////            super(value);
////            this.left = left;
////            this.right = right;
////        }
////
////        @Override
////        T peek() {
////            return head;
////        }
////
////        @Override
////        boolean isLeaf() {
////            return false;
////        }
////
////        @Override
////        public boolean hasNext() throws IOException {
////            return head != null && (left.hasNext() || left.hasNext());
////        }
////
////        @Override
////        public T read() throws IOException {
////            final T previousHead = head;
////            head = read(left, right);
////            return previousHead;
////        }
////
////
////    }
////
////    class Leaf extends Node implements ObjectSource<T> {
////
////        ObjectSource<T> source;
////
////        Leaf(T head, ObjectSource<T> source) {
////            super(head);
////            this.source = source;
////        }
////
////        @Override
////        T peek() {
////            return head;
////        }
////
////        @Override
////        boolean isLeaf() {
////            return true;
////        }
////
////        @Override
////        public T read() throws IOException {
////            final T previousHead = head;
////            head = source.hasNext() ? source.read() : null;
////            return previousHead;
////        }
////
////        @Override
////        public boolean hasNext() throws IOException {
////            return head != null && source.hasNext();
////        }
////
////
////    }
////
//
//
//
//
//
//    @Override
//    protected void runTask() throws IOException {
//        checkState();
//
//        // Inititalise the tree
//
//        List<ObjectSource<T>> sources = new ArrayList<ObjectSource<T>>(inputs.size());
//
//        try {
//            Node tree = null;
//
//            for (ObjectStore<T, ?> input : inputs) {
//                final ObjectSource<T> source = input.openObjectSource();
//                final T head = source.hasNext() ? source.read() : null;
//                sources.add(source);
//
//                if (tree == null) {
//                    tree = new Leaf(head, source);
//                } else {
//
//                }
//
//
//            }
//
//        } finally {
//
//            for (ObjectSource<T> source : sources) {
//                ((Closeable) source).close();
//            }
//        }
//
//        updateProgress(State.RUNNING, "Reading source.", 0);
//
//        List<T> items = null;
//
//        ObjectSource<T> sourceA = null;
//        ObjectSource<T> sourceB = null;
//        try {
//            sourceA = getFromA().openObjectSource();
//            sourceB = getFromB().openObjectSource();
//            items = ObjectIO.readAll(sourceA);
//
//            final double logN = Math.log(items.size()) / Math.log(2);
//            updateProgress(State.RUNNING, "Sorting data.", (int) (1. / (2. + logN)));
//
//            Collections.sort(items, getComparator());
//
//            updateProgress(State.RUNNING, "Writing to sink.", (int) ((1 + logN) / (2. + logN)));
//
//        } finally {
//            ((Closeable) source).close();
//        }
//
//        ObjectSink<T> sink = null;
//        try {
//            sink = getTo().openObjectSink();
//            long i = ObjectIO.copy(items, sink);
//            assert i == items.size();
//        } finally {
//            ((Flushable) sink).flush();
//            ((Closeable) sink).close();
//        }
//
//        updateProgress(State.COMPLETED, "All done.", 100);
//    }
//
//    protected void checkState() {
//        Preconditions.checkNotNull(getComparator(), "comparator");
//        Preconditions.checkNotNull(getFrom(), "fromList");
//        Preconditions.checkArgument(getFrom().isReadable(), "fromList is not readable");
//        Preconditions.checkArgument(getFrom().exists(), "fromList does not exist");
//        Preconditions.checkNotNull(getTo(), "to");
//        Preconditions.checkArgument(getTo().isWritable(), "to is not writable");
//
//
//        // Merge works fromList just one input but it's redundant so throw a warning, but run anyway
//        if (inputs.size() == 1)
//            LOG.warn("Running K-Way Merge only one input.");
//        // If the output already exists we shall throw a warning but carry on regardless
//        if (output.exists())
//            LOG.warn("output already exists: " + output);
//
//    }
//
//    protected void updateProgress(final State state, final String message, final int percentComplete) {
//        progress.startAdjusting();
//        progress.setState(state);
//        progress.setProgressPercent(percentComplete);
//        progress.setMessage(message);
//        progress.endAdjusting();
//    }
//
//    @Override
//    public String getName() {
//        return "sort";
//    }
//
//    @Override
//    public void removeProgressListener(ProgressListener progressListener) {
//        progress.removeProgressListener(progressListener);
//    }
//
//    @Override
//    public boolean isProgressPercentageSupported() {
//        return progress.isProgressPercentageSupported();
//    }
//
//    @Override
//    public String getProgressReport() {
//        return progress.getProgressReport();
//    }
//
//    @Override
//    public int getProgressPercent() {
//        return progress.getProgressPercent();
//    }
//
//    @Override
//    public ProgressListener[] getProgressListeners() {
//        return progress.getProgressListeners();
//    }
//
//    @Override
//    public void addProgressListener(ProgressListener progressListener) {
//        progress.addProgressListener(progressListener);
//    }
//
//    @Override
//    public State getState() {
//        return progress.getState();
//    }
}
