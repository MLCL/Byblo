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
package uk.ac.susx.mlcl.byblo.tasks;

import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.byblo.measures.DecomposableMeasure;
import uk.ac.susx.mlcl.byblo.measures.Measure;
import uk.ac.susx.mlcl.byblo.measures.impl.Jaccard;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import uk.ac.susx.mlcl.lib.events.ProgressDelegate;
import uk.ac.susx.mlcl.lib.events.ProgressListener;
import uk.ac.susx.mlcl.lib.events.ProgressReporting;
import uk.ac.susx.mlcl.lib.io.ObjectIO;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.SeekableObjectSource;
import uk.ac.susx.mlcl.lib.tasks.AbstractTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The most basic implementation of all-pairs similarity search. Will only
 * perform better than the inverted index approach {@link InvertedApssTask} with
 * very dense vectors.
 *
 * @param <P> The generic-type for offset positions.
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class NaiveApssTask<P> extends AbstractTask
        implements ProgressReporting {

    private static final Log LOG = LogFactory.getLog(NaiveApssTask.class);

    /**
     * Set to Jaccard because it requires no parameterization; hence can be
     * instantiated quickly and easily.
     */
    public static final Measure DEFAULT_MEASURE = new Jaccard();

    protected final ProgressDelegate progress = new ProgressDelegate(this, true);

    private SeekableObjectSource<Indexed<SparseDoubleVector>, P> sourceA;

    private SeekableObjectSource<Indexed<SparseDoubleVector>, P> sourceB;

    private Measure measure = DEFAULT_MEASURE;

    private ObjectSink<Weighted<TokenPair>> sink;

    /**
     * Filters that determine which feature vectors are considered.
     */
    private Predicate<Indexed<SparseDoubleVector>> processRecord = Predicates.
            alwaysTrue();

    /**
     * Filters that determine which resultant pairs are output
     */
    private Predicate<Weighted<TokenPair>> pruducePair = Predicates.alwaysTrue();
    // Stat collection

    private ApssStats stats = new ApssStats();
    // Component of the similarity calculation that depends only on the sourceA
    // feature vectorx - can be precalculated to save time during the
    // quadratic part of the algorithm

    private Int2DoubleMap precalcA = null;
    // Component of the similarity calculation that depends only on the sourceB
    // feature vectors - can be precalculated to save time during the
    // quadratic part of the algorithm

    private Int2DoubleMap precalcB = null;

    /**
     * Constructor of minimal parameterisation, taking arguments that must be
     * given to the algorithm for it to be in a runnable state.
     *
     * @param Q
     * @param R
     * @param sink
     */
    public NaiveApssTask(
            SeekableObjectSource<Indexed<SparseDoubleVector>, P> Q,
            SeekableObjectSource<Indexed<SparseDoubleVector>, P> R,
            ObjectSink<Weighted<TokenPair>> sink) {
        setSourceA(Q);
        setSourceB(R);
        setSink(sink);
    }

    /**
     * Null constructor used for reflection instantiation.
     */
    public NaiveApssTask() {
    }

    public Predicate<Weighted<TokenPair>> getProducatePair() {
        return pruducePair;
    }

    public void setProducatePair(Predicate<Weighted<TokenPair>> pruducePair) {
        Checks.checkNotNull("pruducePair");
        this.pruducePair = pruducePair;
    }

    public Predicate<Indexed<SparseDoubleVector>> getProcessRecord() {
        return processRecord;
    }

    public void setProcessRecord(
            Predicate<Indexed<SparseDoubleVector>> processRecord) {
        Checks.checkNotNull("processRecord");
        this.processRecord = processRecord;
    }

    public final ApssStats getStats() {
        return stats;
    }

    public final void setStats(ApssStats stats) {
        Checks.checkNotNull("stats");
        this.stats = stats;
    }

    public final void setSourceA(
            SeekableObjectSource<Indexed<SparseDoubleVector>, P> A) {
        if (A == null) {
            throw new NullPointerException("sourceA is null");
        }
        if (A == sourceB) {
            throw new IllegalArgumentException("sourceA == sourceB");
        }
        this.sourceA = A;
    }

    public final void setSourceB(
            SeekableObjectSource<Indexed<SparseDoubleVector>, P> B) {
        if (B == null) {
            throw new NullPointerException("sourceB is null");
        }
        if (sourceA == B) {
            throw new IllegalArgumentException("sourceA == sourceB");
        }
        this.sourceB = B;
    }

    protected final SeekableObjectSource<Indexed<SparseDoubleVector>, P> getSourceA() {
        return sourceA;
    }

    protected final SeekableObjectSource<Indexed<SparseDoubleVector>, P> getSourceB() {
        return sourceB;
    }

    public final Measure getMeasure() {
        return measure;
    }

    public final void setMeasure(Measure measure) {
        Checks.checkNotNull("measure", measure);
        this.measure = measure;
    }

    public final ObjectSink<Weighted<TokenPair>> getSink() {
        return sink;
    }

    public final void setSink(ObjectSink<Weighted<TokenPair>> sink) {
        if (sink == null) {
            throw new NullPointerException("handler == null");
        }
        this.sink = sink;
    }

    @Override
    protected void initialiseTask() throws Exception {
        checkState();
        buildPrecalcs();
    }

    protected final int PAIR_OUTPUT_BUFFER_SIZE = 100000;

    protected void writeOutPairs(List<Weighted<TokenPair>> pairs) throws IOException {
        if (pairs.isEmpty())
            return;
        // Sorting the pairs reduces disk space usage due to compact format and
        // skip indexing.
        Collections.sort(pairs, Weighted.recordOrder(TokenPair.indexOrder()));
        synchronized (getSink()) {
            ObjectIO.copy(pairs, getSink());
        }
        pairs.clear();
    }

    @Override
    protected void runTask() throws Exception {
        List<Weighted<TokenPair>> pairBuffer = new ArrayList<Weighted<TokenPair>>();
        final P restartB = getSourceB().position();

        progress.startAdjusting();
        progress.setState(State.RUNNING);
        progress.setMessage("Running all-pairs.");
        progress.setProgressPercent(0);
        progress.endAdjusting();

        // for every vector (a) in source A
        while (getSourceA().hasNext()) {
            Indexed<SparseDoubleVector> a = getSourceA().read();

            if (!processRecord.apply(a)) {
                continue;
            }

            // for every vector (b) in source B
            if (sourceB.position() != restartB) {
                sourceB.position(restartB);
            }
            while (getSourceB().hasNext()) {
                stats.incrementCandidatesCount();

                Indexed<SparseDoubleVector> b = sourceB.read();
                if (!processRecord.apply(b)) {
                    continue;
                }

                double sim = sim(a, b);
                Weighted<TokenPair> pair = new Weighted<TokenPair>(
                        new TokenPair(b.key(), a.key()), sim);
                if (pruducePair.apply(pair)) {
                    pairBuffer.add(pair);
                    stats.incrementProductionCount();
                    if (pairBuffer.size() > PAIR_OUTPUT_BUFFER_SIZE) {
                        writeOutPairs(pairBuffer);
                    }
                }
            }
        }

        writeOutPairs(pairBuffer);

        progress.startAdjusting();
        progress.setProgressPercent(100);
        progress.setState(State.COMPLETED);
        progress.endAdjusting();

    }

    @Override
    protected void finaliseTask() throws Exception {
        precalcA = null;
        precalcB = null;
    }

    /**
     * Confirm that the algorithm has been correctly parameterized such that it
     * is likely to run without error.
     *
     * @throws IOException if something goes wrong with input sources
     */
    protected void checkState() throws IOException {
        if (sourceA == null) {
            throw new IllegalStateException("source A is not set");
        }
        if (!sourceA.hasNext()) {
            throw new IllegalStateException("source A is exhausted");
        }
        if (sourceB == null) {
            throw new IllegalStateException("source B is not set");
        }
        if (!sourceB.hasNext()) {
            throw new IllegalStateException("source B is exhausted");
        }
        if (sourceA == sourceB) {
            throw new IllegalArgumentException("sourceA == sourceB");
        }
        if (sink == null) {
            throw new IllegalStateException("sink (destination) is not set");
        }
        if (measure == null) {
            throw new IllegalStateException("measure is not set");
        }
        if (processRecord == null) {
            throw new NullPointerException("recordFilter == null");
        }
        if (pruducePair == null) {
            throw new NullPointerException("pairFilter == null");
        }
    }

    protected void buildPrecalcs() throws IOException {
        // Calculate the left and right hand components if they have not been
        // provided.
        if (getMeasure() instanceof DecomposableMeasure) {
            if (precalcA == null) {
                precalcA = buildPrecalcA();
            }
            if (precalcB == null) {
                precalcB = buildPrecalcB();
            }
        }

    }

    protected Int2DoubleMap getPrecalcA() {
        return precalcA;
    }

    protected Int2DoubleMap getPrecalcB() {
        return precalcB;
    }

    protected Int2DoubleMap buildPrecalcA() throws IOException {
        if (!(getMeasure() instanceof DecomposableMeasure))
            return null;

        final DecomposableMeasure dm = (DecomposableMeasure) getMeasure();

        final P startA = sourceA.position();
        Int2DoubleOpenHashMap result = new Int2DoubleOpenHashMap();
        while (sourceA.hasNext()) {
            Indexed<SparseDoubleVector> p = sourceA.read();
            result.put(p.key(), dm.left(p.value()));
        }
        sourceA.position(startA);
        return result;
    }

    protected Int2DoubleMap buildPrecalcB() throws IOException {
        if (!(getMeasure() instanceof DecomposableMeasure))
            return null;

        final DecomposableMeasure dm = (DecomposableMeasure) getMeasure();

        final P startB = sourceB.position();
        Int2DoubleOpenHashMap result = new Int2DoubleOpenHashMap();
        while (sourceB.hasNext()) {
            Indexed<SparseDoubleVector> p = sourceB.read();
            result.put(p.key(), dm.right(p.value()));
        }
        sourceB.position(startB);
        return result;
    }

    protected final double sim(
            final Indexed<SparseDoubleVector> a,
            final Indexed<SparseDoubleVector> b) {
        stats.incrementComparisonCount();

        // XXX: checking instanceof everytime will be a bit slow
        if (measure instanceof DecomposableMeasure) {
            final DecomposableMeasure dm =
                    (DecomposableMeasure) getMeasure();
            return dm.combine(
                    dm.shared(a.value(), b.value()),
                    precalcA.get(a.key()),
                    precalcB.get(b.key()));
        } else {
            return measure.similarity(a.value(), b.value());
        }
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
    public String getName() {
        return "naive-allpairs";
    }

    @Override
    public void addProgressListener(ProgressListener progressListener) {
        progress.addProgressListener(progressListener);
    }

    @Override
    public State getState() {
        return progress.getState();
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("sourceA", sourceA).
                add("sourceB", sourceB).
                add("measure", measure).
                add("sink", sink).
                add("processRecord", processRecord).
                add("pruducePair", pruducePair).
                add("stats", stats);
    }
}
