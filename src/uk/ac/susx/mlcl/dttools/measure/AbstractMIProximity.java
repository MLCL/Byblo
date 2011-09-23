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
package uk.ac.susx.mlcl.dttools.measure;

import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AbstractMIProximity is a class that mutual information (MI) based proximity
 * measures can extend. Unlike more tradition measures (such as Jaccard) these
 * require global feature context statistical information, which is held in
 * the class fields.
 *
 * @version 18th April 2011
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
public abstract class AbstractMIProximity implements Proximity {

    private static final Logger LOG = Logger.getLogger(AbstractMIProximity.class.getName());
    /**
     * A mapping from context id to frequency, stored as a double because it
     * may have been waited in a pre-processing stage.
     */
    private double[] contextFrequencies;

    /**
     * The total number of contexts, not just the number of unique contexts.
     * This should be equal to the sum of all values in
     * {@link #contextFrequencies}.
     */
    private double contextSum;

    /**
     * The number of unique contexts in this data set, should be equals to
     * {@link #contextFrequencies.length }.
     */
    private int contextCardinality;

    /**
     * The number of contexts that are actually occurring more than once.
     * Due to pre-processing a context may have been previously filtered,
     * resulting in this value being less than the cardinality. This could
     * be described as the non-zero cardinality.
     */
    private long uniqueContextCount;

    /**
     * 
     */
    public AbstractMIProximity() {
    }

    public void setContextFreqs(double[] contextFrequencies) {
        if (contextFrequencies == null)
            throw new NullPointerException("contextFrequencies == null");
        this.contextFrequencies = contextFrequencies;
    }

    public void setContextSum(double contextSum) {
        if (Double.isNaN(contextSum))
            throw new IllegalArgumentException("grandtotal is NaN");
        if (Double.isInfinite(contextSum))
            throw new IllegalArgumentException("grandtotal is infinite");
        this.contextSum = contextSum;
    }

    public void setUniqueContextCount(long uniqueContextCount) {
        this.uniqueContextCount = uniqueContextCount;
    }

    public void setContextCardinality(int contextCardinality) {
        this.contextCardinality = contextCardinality;
    }

    /**
     * The total number of contexts, not just the number of unique contexts.
     * 
     * @return sum of all context frequencies.
     */
    public final double getContextSum() {
        return contextSum;
    }

    /**
     * The number of unique contexts.
     * @return number of unique contexts.
     */
    public final int getContextsCount() {
        return contextFrequencies.length;
    }

    public int getContextCardinality() {
        return contextCardinality;
    }

    public long getUniqueContextCount() {
        return uniqueContextCount;
    }

    /**
     * Return the frequency of a particular context, denoted by the index k.
     *
     * @param k The context id (or dimension) to access
     * @return The frequency of that context feature over the whole corpus.
     */
    protected final double contextFreq(final int k) {
        // XXX Nasty work arround for strange bug where contexts in the features
        // file don't exist in the contexts file.
        if(k >= contextFrequencies.length) {
            LOG.log(Level.WARNING, "Unable to find context freqency for key " + k);
            return 1; // must return 1 or we can get divide by zeros
        }
        return contextFrequencies[k];
    }

    /**
     * Return the independent probability of a particular context, calculated
     * as the frequency of the feature divided by the total number of features
     * observed over the corpus.
     *
     * @param k the context feature id (or dimension)
     * @return independent probability of that context occurring.
     */
    protected final double contextProb(final int k) {
        return contextFreq(k) / contextSum;
    }

    /**
     * <p>Calculate the information content of the vector V at dimension i
     * with respect to the context information held by this class.</p>
     *
     * <p>If only the positive information is required then use
     * posInf(null, contextCardinality) because it's faster.</p>
     *
     * @param V vector
     * @param i dimension (aka feature id) to calculate
     * @return information content of V at i
     */
    protected final double inf(final SparseDoubleVector V, final int i) {
        return Math.log((V.values[i] / V.sum) / contextProb(V.keys[i]));
    }

    /**
     * Calculate the positive information given by the vector V at dimension i
     * with respect to the context information held by this class. If the
     * information content is negative then 0 is returned.
     *
     * @param V vector
     * @param i dimension (aka feature id) to calculate
     * @return information content of V at i if positive, otherwise 0
     */
    protected final double posInf(final SparseDoubleVector V, final int i) {
        final double tmp = (V.values[i] / V.sum) / contextProb(V.keys[i]);
        return tmp > 1 ? Math.log(tmp) : 0;
    }

    public boolean hasPosInf(final SparseDoubleVector V, final int i) {
        return prob(V, i) > contextProb(V.keys[i]);
    }

    // Calculate if the features would both have positive
    // information content w.r.t the context data.
    public boolean hasPosInf(final SparseDoubleVector Q, final int i, final SparseDoubleVector R, final int j) {
        final double vprob = contextProb(Q.keys[i]);
        return prob(Q, i) > vprob && prob(R, j) > vprob;
    }

    protected final double prob(final SparseDoubleVector V, final int k) {
        return V.values[k] / V.sum;
    }


}
