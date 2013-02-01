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
package uk.ac.susx.mlcl.byblo.tasks;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import uk.ac.susx.mlcl.lib.io.SeekableObjectSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * An all-pairs similarity search implementation that improves efficiency by
 * building a reverse index of one of the input sources. This allows candidate
 * pairs to be found relatively quickly given sufficient sparsity
 * <p/>
 *
 * @param <S>
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class InvertedApssTask<S> extends NaiveApssTask<S> {

    private static final Log LOG = LogFactory.getLog(InvertedApssTask.class);

    private Int2ObjectMap<Set<Indexed<SparseDoubleVector>>> index;

    public InvertedApssTask() {
        index = null;
    }

    @Override
    protected void initialiseTask() throws Exception {
        super.initialiseTask();
        if (index == null) {
            index = buildIndex();
        }
    }

    @Override
    protected void runTask()
            throws IOException {

        progress.startAdjusting();
        progress.setState(State.RUNNING);
        progress.setMessage("Running inverted all-pairs.");
        progress.setProgressPercent(0);
        progress.endAdjusting();

        final S startB = getSourceB().position();
        List<Weighted<TokenPair>> pairs = new ArrayList<Weighted<TokenPair>>();

        while (getSourceB().hasNext()) {
            Indexed<SparseDoubleVector> b = getSourceB().read();
            if (!getProcessRecord().apply(b))
                continue;

            Set<Indexed<SparseDoubleVector>> candidates = findCandidates(b);

            for (Indexed<SparseDoubleVector> a : candidates) {
                if (!getProcessRecord().apply(a))
                    continue;
                getStats().incrementCandidatesCount();

                double sim = sim(a, b);
                Weighted<TokenPair> pair = new Weighted<TokenPair>(
                        new TokenPair(b.key(), a.key()), sim);
                if (getProducePair().apply(pair)) {
                    pairs.add(pair);
                    getStats().incrementProductionCount();

                    if (pairs.size() > PAIR_OUTPUT_BUFFER_SIZE) {
                        writeOutPairs(pairs);
                    }


                }
            }
        }

        writeOutPairs(pairs);


        getSourceB().position(startB);

        progress.startAdjusting();
        progress.setState(State.COMPLETED);
        progress.setProgressPercent(100);
        progress.endAdjusting();

    }

    Set<Indexed<SparseDoubleVector>> findCandidates(
            Indexed<SparseDoubleVector> b) {

        final Set<Indexed<SparseDoubleVector>> candidates =
                new ObjectOpenHashSet<Indexed<SparseDoubleVector>>();

        for (int k : b.value().keys) {
            if (index.containsKey(k)) {
                candidates.addAll(index.get(k));
            }
        }
        return candidates;
    }

    Int2ObjectMap<Set<Indexed<SparseDoubleVector>>> buildIndex()
            throws IOException {
        SeekableObjectSource<? extends Indexed<SparseDoubleVector>, S> src =
                getSourceA();
        final Int2ObjectMap<Set<Indexed<SparseDoubleVector>>> result =
                new Int2ObjectOpenHashMap<Set<Indexed<SparseDoubleVector>>>();
        final S startA = src.position();
        while (src.hasNext()) {
            final Indexed<SparseDoubleVector> a = src.read();
            for (int k : a.value().keys) {
                if (!result.containsKey(k)) {
                    result.put(k,
                            new ObjectOpenHashSet<Indexed<SparseDoubleVector>>());
                }
                result.get(k).add(a);
            }
        }
        src.position(startA);
        return result;
    }

    void setIndex(Int2ObjectMap<Set<Indexed<SparseDoubleVector>>> index) {
        Checks.checkNotNull("index is null", index);
        this.index = index;
    }

    Int2ObjectMap<Set<Indexed<SparseDoubleVector>>> getIndex() {
        return index;
    }

    @Override
    public String getName() {
        return "inverted-allpairs";
    }
}
