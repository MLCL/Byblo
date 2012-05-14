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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.byblo.measures.Jaccard;
import uk.ac.susx.mlcl.byblo.measures.Proximity;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.lib.io.SeekableSource;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.tasks.AbstractTask;

/**
 * The most basic implementation of all-pairs similarity search. Will only
 * perform better than the inverted index approach {@link InvertedApssTask} with
 * very dense vectors.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @param <P> The generic-type for offset positions.
 */
public class NaiveApssTask<P> extends AbstractTask {

    private static final Log LOG = LogFactory.getLog(NaiveApssTask.class);

    /**
     * Set to Jaccard because it requires no parameterization; hence can be
     * instantiated quickly and easily.
     */
    public static final Proximity DEFAULT_MEASURE = new Jaccard();

    private SeekableSource<Indexed<SparseDoubleVector>, P> sourceA;

    private SeekableSource<Indexed<SparseDoubleVector>, P> sourceB;

    private Proximity measure = DEFAULT_MEASURE;

    private Sink<Weighted<TokenPair>> sink;

    /**
     * Filters that determine which feature vectors are considered.
     */
    private Predicate<Indexed<SparseDoubleVector>> processRecord = Predicates.alwaysTrue();

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
            SeekableSource<Indexed<SparseDoubleVector>, P> Q,
            SeekableSource<Indexed<SparseDoubleVector>, P> R,
            Sink<Weighted<TokenPair>> sink) {
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
            SeekableSource<Indexed<SparseDoubleVector>, P> A) {
        if (A == null) {
            throw new NullPointerException("sourceA is null");
        }
        if (A == sourceB) {
            throw new IllegalArgumentException("sourceA == sourceB");
        }
        this.sourceA = A;
    }

    public final void setSourceB(
            SeekableSource<Indexed<SparseDoubleVector>, P> B) {
        if (B == null) {
            throw new NullPointerException("sourceB is null");
        }
        if (sourceA == B) {
            throw new IllegalArgumentException("sourceA == sourceB");
        }
        this.sourceB = B;
    }

    protected final SeekableSource<Indexed<SparseDoubleVector>, P> getSourceA() {
        return sourceA;
    }

    protected final SeekableSource<Indexed<SparseDoubleVector>, P> getSourceB() {
        return sourceB;
    }

    public final Proximity getMeasure() {
        return measure;
    }

    public final void setMeasure(Proximity measure) {
        if (measure == null) {
            throw new NullPointerException("measure == null");
        }
        this.measure = measure;
    }

    public final Sink<Weighted<TokenPair>> getSink() {
        return sink;
    }

    public final void setSink(Sink<Weighted<TokenPair>> sink) {
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

    @Override
    protected void runTask() throws Exception {
        List<Weighted<TokenPair>> pairs = new ArrayList<Weighted<TokenPair>>();
        final P restartB = getSourceB().position();

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
                    pairs.add(pair);
                    stats.incrementProductionCount();
                }
            }
        }
        Collections.sort(pairs, Weighted.recordOrder(TokenPair.indexOrder()));
        synchronized (getSink()) {
            IOUtil.copy(pairs, getSink());
        }
    }

    @Override
    protected void finaliseTask() throws Exception {
        precalcA = null;
        precalcB = null;
    }

    /**
     * Confirm that the algorithm has been correctly parameterized such that it
     * is likely to run without error.
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
        if (precalcA == null) {
            precalcA = buildPrecalcA();
        }
        if (precalcB == null) {
            precalcB = buildPrecalcB();
        }

    }

    protected Int2DoubleMap getPrecalcA() {
        return precalcA;
    }

    protected Int2DoubleMap getPrecalcB() {
        return precalcB;
    }

    protected Int2DoubleMap buildPrecalcA() throws IOException {
        final P startA = sourceA.position();
        Int2DoubleOpenHashMap result = new Int2DoubleOpenHashMap();
        while (sourceA.hasNext()) {
            Indexed<SparseDoubleVector> p = sourceA.read();
            result.put(p.key(), getMeasure().left(p.value()));
        }
        sourceA.position(startA);
        return result;
    }

    protected Int2DoubleMap buildPrecalcB() throws IOException {
        final P startB = sourceB.position();
        Int2DoubleOpenHashMap result = new Int2DoubleOpenHashMap();
        while (sourceB.hasNext()) {
            Indexed<SparseDoubleVector> p = sourceB.read();
            result.put(p.key(), getMeasure().right(p.value()));
        }
        sourceB.position(startB);
        return result;
    }

    protected final double sim(
            final Indexed<SparseDoubleVector> a,
            final Indexed<SparseDoubleVector> b) {
        stats.incrementComparisonCount();
        return measure.combine(
                measure.shared(a.value(), b.value()),
                precalcA.get(a.key()),
                precalcB.get(b.key()));
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
