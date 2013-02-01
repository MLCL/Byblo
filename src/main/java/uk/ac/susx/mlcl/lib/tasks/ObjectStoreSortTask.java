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
package uk.ac.susx.mlcl.lib.tasks;

import com.google.common.base.Preconditions;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An <code>ObjectStoreSortTask</code> takes reads all the data from an <code>ObjectStore</code>, sorts it, then writes
 * the results out to a second <code>ObjectStore</code>. Ordering is defined either by setting a comparator, or
 * alternatively the natural ordering of the object will be used if possible. The <code>from</code> and <code>to</code>
 * stores may safely reference the same store, using it supported both reading and writing.
 *
 * @param <T> Object type to be sorted
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Nonnull
@CheckReturnValue
@NotThreadSafe
public class ObjectStoreSortTask<T> extends AbstractTask implements ProgressReporting {

    private final ProgressDelegate progress = new ProgressDelegate(this, true);

    @Nullable
    private ObjectStore<T, ?> from;

    @Nullable
    private ObjectStore<T, ?> to;

    @Nullable
    private Comparator<T> comparator;

    public ObjectStoreSortTask(final ObjectStore<T, ?> from, final ObjectStore<T, ?> to, final Comparator<T> comparator) {
        setFrom(from);
        setTo(to);
        setComparator(comparator);
    }

    public ObjectStoreSortTask(final ObjectStore<T, ?> from, final ObjectStore<T, ?> to) {
        this(from, to, Comparators.<T>naturalOrderIfPossible());
    }

    public ObjectStoreSortTask() {
        from = null;
        to = null;
        setComparator(Comparators.<T>naturalOrderIfPossible());
    }

    public final Comparator<T> getComparator() {
        return comparator;
    }

    @Nullable
    public final ObjectStore<T, ?> getFrom() {
        return from;
    }

    public final void setFrom(final ObjectStore<T, ?> from) {
        Preconditions.checkNotNull(from, "from");
        Preconditions.checkArgument(from.isReadable(), "from is not readable");
        this.from = from;
    }

    @Nullable
    public final ObjectStore<T, ?> getTo() {
        return to;
    }

    public final void setTo(final ObjectStore<T, ?> to) {
        Preconditions.checkNotNull(to, "to");
        Preconditions.checkArgument(to.isWritable(), "to is not writable");
        this.to = to;
    }

    public final void setComparator(final Comparator<T> comparator) {
        Preconditions.checkNotNull(comparator, "comparator");
        this.comparator = comparator;
    }

    @Override
    protected void runTask() throws IOException {
        checkState();

        updateProgress(State.RUNNING, "Reading source.", 0);

        List<T> items = null;

        ObjectSource<T> source = null;
        try {
            source = getFrom().openObjectSource();
            items = ObjectIO.readAll(source);

            final double logN = Math.log(items.size()) / Math.log(2);
            updateProgress(State.RUNNING, "Sorting data.", (int) (1. / (2. + logN)));

            Collections.sort(items, getComparator());

            updateProgress(State.RUNNING, "Writing to sink.", (int) ((1 + logN) / (2. + logN)));

        } finally {
            ((Closeable) source).close();
        }

        ObjectSink<T> sink = null;
        try {
            sink = getTo().openObjectSink();
            long i = ObjectIO.copy(items, sink);
            assert i == items.size();
        } finally {
            ((Flushable) sink).flush();
            ((Closeable) sink).close();
        }

        updateProgress(State.COMPLETED, "All done.", 100);
    }

    protected void checkState() {
        Preconditions.checkNotNull(getComparator(), "comparator");
        Preconditions.checkNotNull(getFrom(), "from");
        Preconditions.checkArgument(getFrom().isReadable(), "from is not readable");
        Preconditions.checkArgument(getFrom().exists(), "from does not exist");
        Preconditions.checkNotNull(getTo(), "to");
        Preconditions.checkArgument(getTo().isWritable(), "to is not writable");
    }

    protected void updateProgress(final State state, final String message, final int percentComplete) {
        progress.startAdjusting();
        progress.setState(state);
        progress.setProgressPercent(percentComplete);
        progress.setMessage(message);
        progress.endAdjusting();
    }

    @Override
    public String getName() {
        return "sort";
    }

    @Override
    public void removeProgressListener(ProgressListener progressListener) {
        progress.removeProgressListener(progressListener);
    }

    @Override
    public boolean isProgressPercentageSupported() {
        return progress.isProgressPercentageSupported();
    }

    @Override
    public String getProgressReport() {
        return progress.getProgressReport();
    }

    @Override
    public int getProgressPercent() {
        return progress.getProgressPercent();
    }

    @Override
    public ProgressListener[] getProgressListeners() {
        return progress.getProgressListeners();
    }

    @Override
    public void addProgressListener(ProgressListener progressListener) {
        progress.addProgressListener(progressListener);
    }

    @Override
    public ProgressReporting.State getState() {
        return progress.getState();
    }
}
