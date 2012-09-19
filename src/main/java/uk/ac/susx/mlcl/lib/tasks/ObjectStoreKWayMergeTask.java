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
import uk.ac.susx.mlcl.lib.io.*;

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
 *
 * @param <T> Object type to be merged
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Nonnull
@CheckReturnValue
@NotThreadSafe
public abstract class ObjectStoreKWayMergeTask<T> extends AbstractTask implements ProgressReporting {

    private static final Log LOG = LogFactory.getLog(ObjectStoreKWayMergeTask.class);

    private final ObjectPipeTask pipeTaskDelegate = new ObjectPipeTask<T>();
    private final ProgressDelegate progress = new ProgressDelegate(this, true);

    @Nullable
    private final List<ObjectStore<T, ?>> inputs;

    @Nullable
    private ObjectStore<T, ?> output;

    @Nullable
    private Comparator<T> comparator;

    public ObjectStoreKWayMergeTask(final List<ObjectStore<T, ?>> inputs, final ObjectStore<T, ?> output, final Comparator<T> comparator) {
        this.inputs = new ArrayList<ObjectStore<T, ?>>(inputs.size());
        addInputs(inputs);
        setOutput(output);
        setComparator(comparator);
    }

    public ObjectStoreKWayMergeTask(final List<ObjectStore<T, ?>> inputs, final ObjectStore<T, ?> output) {
        this(inputs, output, Comparators.<T>naturalOrderIfPossible());
    }

    public ObjectStoreKWayMergeTask() {
        inputs = new ArrayList<ObjectStore<T, ?>>();
        output = null;
        setComparator(Comparators.<T>naturalOrderIfPossible());
    }

    public final Comparator<T> getComparator() {
        return comparator;
    }

    @Nullable
    public final List<ObjectStore<T, ?>> getInputs() {
        return inputs;
    }

    public final void addInputs(final List<ObjectStore<T, ?>> inputs) {
        Preconditions.checkNotNull(inputs, "inputs");
        Preconditions.checkNotNull(!inputs.isEmpty(), "inputs is empty");
        for (ObjectStore<T, ?> input : inputs) {
            Preconditions.checkArgument(input.isReadable(), "input is not readable");
            Preconditions.checkArgument(input.exists(), "input is not exist");
        }

        this.inputs.addAll(inputs);
    }

    @Nullable
    public final ObjectStore<T, ?> getOutput() {
        return output;
    }

    public final void setOutput(final ObjectStore<T, ?> output) {
        if (output != this.output) {
            Preconditions.checkNotNull(output, "output");
            Preconditions.checkArgument(output.isWritable(), "output is not writable");

            this.output = output;
        }
    }

    public final void setComparator(final Comparator<T> comparator) {
        Preconditions.checkNotNull(comparator, "comparator");
        this.comparator = comparator;
    }

    @Override
    protected void runTask() throws Exception {
        checkState();

        updateProgress(State.RUNNING, "Running merge.", 0);

        List<ObjectSource<T>> sources = new ArrayList<ObjectSource<T>>(inputs.size());
        ObjectSink<T> sink = null;

        try {
            for (ObjectStore<T, ?> input : inputs) {
                final ObjectSource<T> source = input.openObjectSource();
                sources.add(source);
            }
            ObjectSource<T> mergeSource = MergingObjectSource.merge(comparator, sources);
            sink = output.openObjectSink();

            ObjectPipeTask<T> pipeTask = new ObjectPipeTask<T>(mergeSource, sink);

            pipeTask.runTask();

        } finally {

            for (ObjectSource<T> source : sources) {
                ((Closeable) source).close();
            }
            if(sink != null)
                sink.close();
        }


        updateProgress(State.COMPLETED, "All done.", 100);
    }

    protected void checkState() {
        Preconditions.checkNotNull(getComparator(), "comparator");

        Preconditions.checkNotNull(inputs, "inputs");
        Preconditions.checkNotNull(!inputs.isEmpty(), "inputs is empty");
        for (ObjectStore<T, ?> input : inputs) {
            Preconditions.checkArgument(input.isReadable(), "input is not readable");
            Preconditions.checkArgument(input.exists(), "input is not exist");
        }

        Preconditions.checkNotNull(output, "output");
        Preconditions.checkArgument(output.isWritable(), "output is not writable");

        // Merge works fromList just one input but it's redundant so throw a warning, but run anyway
        if (inputs.size() == 1)
            LOG.warn("Running K-Way Merge only one input.");
        // If the output already exists we shall throw a warning but carry on regardless
        if (output.exists())
            LOG.warn("output already exists: " + output);

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
        return "merge";
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
    public State getState() {
        return progress.getState();
    }
}
