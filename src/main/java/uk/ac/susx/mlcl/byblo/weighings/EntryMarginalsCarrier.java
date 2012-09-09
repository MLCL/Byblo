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
package uk.ac.susx.mlcl.byblo.weighings;

/**
 * {@link EntryMarginalsCarrier} denotes an implementation that requires the
 * entry marginal totals independent of feature.
 * <p/>
 * When implemented the marginals array will be provided by the software with
 * {@link EntryMarginalsCarrier#setEntryMarginals(double[])}.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public interface EntryMarginalsCarrier extends MarginalsCarrier {

    /**
     * Accessor to an array containing a mapping from entry id to weighting.
     * <p/>
     * Stored as a double because it may have been waited in a pre-processing
     * stage, and as an array because it's usually dense.
     *
     * @return marginal scores for each entry
     */
    double[] getEntryMarginals();

    /**
     * Accessor to the number of entries that are actually occurring at least
     * once. Due to pre-processing an entry may have been previously filtered,
     * resulting in this value being less than t
     *
     * @return number of actually occurring entry types
     */
    long getEntryCardinality();

    /**
     * Mutator to an array containing a mapping from entry id to weighting.
     *
     * @param marginals marginal scores for each entry
     */
    void setEntryMarginals(double[] marginals);

    /**
     * Mutator to the number of entries that are actually occurring at least
     * once.
     *
     * @param cardinality number of actually occurring entry types
     */
    void setEntryCardinality(long cardinality);
}
