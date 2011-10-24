/*
 * Copyright (c) 2010-2011, University of Sussex
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
package uk.ac.susx.mlcl.byblo.allpairs;

import com.google.common.base.Predicate;
import static com.google.common.base.Predicates.*;
import uk.ac.susx.mlcl.byblo.measure.Jaccard;
import java.io.IOException;
import uk.ac.susx.mlcl.byblo.measure.Proximity;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.MiscUtil;
import uk.ac.susx.mlcl.lib.collect.Entry;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import uk.ac.susx.mlcl.byblo.io.WeightedEntryPairRecord;
import uk.ac.susx.mlcl.lib.io.SeekableSource;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.tasks.AbstractTask;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.io.Closeable;
import java.io.Flushable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.io.IOUtil;

/**
 * The most basic implementation of all-pairs similarity search. Will only 
 * perform better than the inverted index approach {@link InvertedApssTask} with
 * very dense vectors.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @param <P> The generic-type for offset positions.
 */
public class NaiveApssTask<P> extends AbstractTask {

    private static final Log LOG = LogFactory.getLog(NaiveApssTask.class);

    /**
     * Set to Jaccard because it requires no parameterization; hence can be
     * instantiated quickly and easily.
     */
    public static final Proximity DEFAULT_MEASURE = new Jaccard();

    private SeekableSource<Entry<SparseDoubleVector>, P> sourceA;

    private SeekableSource<Entry<SparseDoubleVector>, P> sourceB;

    private Proximity measure = DEFAULT_MEASURE;

    private Sink<WeightedEntryPairRecord> sink;

    /**
     * Filters that determine which feature vectors are considered.
     */
    private Predicate<Entry<SparseDoubleVector>> processRecord = alwaysTrue();

    /**
     * Filters that determine which resultant pairs are output
     */
    private Predicate<WeightedEntryPairRecord> pruducePair = alwaysTrue();

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
            SeekableSource<Entry<SparseDoubleVector>, P> Q,
            SeekableSource<Entry<SparseDoubleVector>, P> R,
            Sink<WeightedEntryPairRecord> sink) {
        setSourceA(Q);
        setSourceB(R);
        setSink(sink);
    }

    /**
     * Null constructor used for reflection instantiation.
     */
    public NaiveApssTask() {
    }

    public Predicate<WeightedEntryPairRecord> getProducatePair() {
        return pruducePair;
    }

    public void setProducatePair(Predicate<WeightedEntryPairRecord> pruducePair) {
        Checks.checkNotNull("pruducePair");
        this.pruducePair = pruducePair;
    }

    public Predicate<Entry<SparseDoubleVector>> getProcessRecord() {
        return processRecord;
    }

    public void setProcessRecord(
            Predicate<Entry<SparseDoubleVector>> processRecord) {
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
            SeekableSource<Entry<SparseDoubleVector>, P> A) {
        if (A == null)
            throw new NullPointerException("sourceA is null");
        if (A == sourceB)
            throw new IllegalArgumentException("sourceA == sourceB");
        this.sourceA = A;
    }

    public final void setSourceB(
            SeekableSource<Entry<SparseDoubleVector>, P> B) {
        if (B == null)
            throw new NullPointerException("sourceB is null");
        if (sourceA == B)
            throw new IllegalArgumentException("sourceA == sourceB");
        this.sourceB = B;
    }

    protected final SeekableSource<Entry<SparseDoubleVector>, P> getSourceA() {
        return sourceA;
    }

    protected final SeekableSource<Entry<SparseDoubleVector>, P> getSourceB() {
        return sourceB;
    }

    public final Proximity getMeasure() {
        return measure;
    }

    public final void setMeasure(Proximity measure) {
        if (measure == null)
            throw new NullPointerException("measure == null");
        this.measure = measure;
    }

    public final Sink<WeightedEntryPairRecord> getSink() {
        return sink;
    }

    public final void setSink(Sink<WeightedEntryPairRecord> sink) {
        if (sink == null)
            throw new NullPointerException("handler == null");
        this.sink = sink;
    }

    @Override
    protected void initialiseTask() throws Exception {
        checkState();
        buildPrecalcs();
    }

    @Override
    protected void runTask() throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("Running all-pairs similarity search: " + this.getClass());
            if (LOG.isDebugEnabled())
                LOG.debug(MiscUtil.memoryInfoString());
        }

        computeAllPairs();
    }

    @Override
    protected void finaliseTask() throws Exception {
        if (sourceA instanceof Closeable)
            ((Closeable) sourceA).close();
        if (sourceB instanceof Closeable)
            ((Closeable) sourceB).close();
        precalcA = null;
        precalcB = null;
    }

    /**
     * Confirm that the algorithm has been correctly parameterized such that it
     * is likely to run without error.
     */
    protected void checkState() throws IOException {
        if (sourceA == null)
            throw new IllegalStateException("source A is not set");
        if (!sourceA.hasNext())
            throw new IllegalStateException("source A is exhausted");
        if (sourceB == null)
            throw new IllegalStateException("source B is not set");
        if (!sourceB.hasNext())
            throw new IllegalStateException("source B is exhausted");
        if (sourceA == sourceB)
            throw new IllegalArgumentException("sourceA == sourceB");
        if (sink == null)
            throw new IllegalStateException("sink (destination) is not set");
        if (measure == null)
            throw new IllegalStateException("measure is not set");
        if (processRecord == null)
            throw new NullPointerException("recordFilter == null");
        if (pruducePair == null)
            throw new NullPointerException("pairFilter == null");
    }

    protected void buildPrecalcs() throws IOException {
        // Calculate the left and right hand components if they have not been
        // provided.
        if (precalcA == null)
            precalcA = buildPrecalcA();
        if (precalcB == null)
            precalcB = buildPrecalcB();

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
            Entry<SparseDoubleVector> p = sourceA.read();
            result.put(p.key(), getMeasure().left(p.value()));
        }
        sourceA.position(startA);
        return result;
    }

    protected Int2DoubleMap buildPrecalcB() throws IOException {
        final P startB = sourceB.position();
        Int2DoubleOpenHashMap result = new Int2DoubleOpenHashMap();
        while (sourceB.hasNext()) {
            Entry<SparseDoubleVector> p = sourceB.read();
            result.put(p.key(), getMeasure().right(p.value()));
        }
        sourceB.position(startB);
        return result;
    }

    protected void computeAllPairs() throws IOException {
        List<WeightedEntryPairRecord> pairs = new ArrayList<WeightedEntryPairRecord>();
        final P restartB = getSourceB().position();

        // for every vector (a) in source A
        while (getSourceA().hasNext()) {
            Entry<SparseDoubleVector> a = getSourceA().read();

            if (!processRecord.apply(a))
                continue;

            // for every vector (b) in source B
            if (sourceB.position() != restartB)
                sourceB.position(restartB);
            while (getSourceB().hasNext()) {
                stats.incrementCandidatesCount();

                Entry<SparseDoubleVector> b = sourceB.read();
                if (!processRecord.apply(b))
                    continue;

                double sim = sim(a, b);
                WeightedEntryPairRecord pair = new WeightedEntryPairRecord(
                        a.key(), b.key(), sim);
                if (pruducePair.apply(pair)) {
                    pairs.add(pair);
                    stats.incrementProductionCount();
                }
            }
        }
        synchronized (getSink()) {
            IOUtil.copy(pairs, getSink());
            if (getSink() instanceof Flushable)
                ((Flushable) getSink()).flush();
        }
    }

    protected final double sim(
            final Entry<SparseDoubleVector> a,
            final Entry<SparseDoubleVector> b) {
        stats.incrementComparisonCount();
        return measure.combine(
                measure.shared(a.value(), b.value()),
                precalcA.get(a.key()),
                precalcB.get(b.key()));
    }

    @Override
    public String toString() {
        return "NaiveApssTask{"
                + "sourceA=" + getSourceA()
                + ", sourceB=" + getSourceB()
                + ", measure=" + getMeasure()
                + ", sink=" + getSink()
                + ", pairFilter=" + getProducatePair()
                + ", recordFilter=" + getProcessRecord()
                + ", stats=" + getStats()
                + '}';
    }
}
