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

import com.google.common.base.Objects;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An instance of ApssStats collects various bits of information about an an
 * All-Pairs process, that can be used for debugging and performance evaluation.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class ApssStats implements Serializable {

    private static final long serialVersionUID = 4248533084667228992L;
    private final AtomicLong candidates;
    private final AtomicLong comparisons;
    private final AtomicLong productions;
    private final AtomicLong srcReads;

    /**
     * Dependency injection constructor.
     *
     * @param candidates    Count of candidate pairs
     * @param comparisons   Count of "slow" comparisons (e.g dot products).
     * @param productions   Count of unique pairs found.
     * @param srcReads      Count of records read.
     */
    protected ApssStats(AtomicLong candidates, AtomicLong comparisons,
            AtomicLong productions, AtomicLong srcReads) {
        this.candidates = candidates;
        this.comparisons = comparisons;
        this.productions = productions;
        this.srcReads = srcReads;
    }

    /**
     * Default constructor initialises everything to 0.
     */
    public ApssStats() {
        srcReads = new AtomicLong(0);
        productions = new AtomicLong(0);
        comparisons = new AtomicLong(0);
        candidates = new AtomicLong(0);
    }

    private void writeObject(final ObjectOutputStream out)
            throws IOException {
        out.writeLong(candidates.get());
        out.writeLong(comparisons.get());
        out.writeLong(productions.get());
        out.writeLong(srcReads.get());
    }

    private void readObject(final ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        candidates.set(in.readLong());
        comparisons.set(in.readLong());
        productions.set(in.readLong());
        srcReads.set(in.readLong());
    }

    public long getCandidatesCount() {
        return candidates.get();
    }

    public void incrementCandidatesCount() {
        candidates.incrementAndGet();
    }

    public void addCandidatesCount(long delta) {
        candidates.addAndGet(delta);
    }

    public long getComparisonCount() {
        return comparisons.get();
    }

    public void incrementComparisonCount() {
        comparisons.incrementAndGet();
    }

    public void addComparisonCount(long delta) {
        comparisons.addAndGet(delta);
    }

    public long getProductionCount() {
        return productions.get();
    }

    public void incrementProductionCount() {
        productions.incrementAndGet();
    }

    public void addProductionCount(long delta) {
        productions.addAndGet(delta);
    }

    public long getSourceReads() {
        return srcReads.get();
    }

    public void incrementSourceReads() {
        srcReads.incrementAndGet();
    }

    public void addSourceReads(long delta) {
        srcReads.addAndGet(delta);
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
    }

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this).
                add("candidates", candidates).
                add("comparisons", comparisons).
                add("productions", productions).
                add("srcReads", srcReads);
    }
}
