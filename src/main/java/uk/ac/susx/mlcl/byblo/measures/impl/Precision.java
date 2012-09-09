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
package uk.ac.susx.mlcl.byblo.measures.impl;

import javax.annotation.concurrent.Immutable;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * This measure attempts to encapsulate, for each pair of feature vectors A and
 * B, how strongly A is described by B. A high similarity is returned when all
 * the features of A, also occur in B. Conversely a low similarity is returned
 * when fews of A's features occur in B. The resultant similarity will be in the
 * range 0 (completed different) to 1 (exactly the same).
 * <p/>
 * An intuitive analogy is that of the words "orange" and and "fig". "orange" is
 * a very common word and can have several difference senses, e.g: colour,
 * fruit, company name. On the other hand "fig" is relatively infrequent and is
 * nearly always used in the sense of fruit. Hence we can say that "fig" is
 * wholly described by "apple", "apple" recalls fully the senses of "fig", and
 * so recall("fig", "apple") will be high. Conversely "apple" is only partially
 * described by "fig", "fig" does not recall fully the senses of "apple", and
 * recall("apple","fig") will be low.
 * <p/>
 * One complication is that the measure does not just use raw frequencies, but
 * the positive information content of each feature. Intuitively, a feature is
 * set to occur if it's occurrence is significant w.r.t to some base-line
 * probabilities. This is calculated as the positive point-wise mutual
 * information content SI(x,y), where x is the feature vector and y is the
 * context frequencies taken from the whole corpus.
 * <p/>
 * Note that recall(x,y) = precision(y,x) so the precision can be calculated by
 * wrapping an instance of RecallMi in the ReversedProximity decorator.
 * <p/>
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Immutable
public class Precision extends Recall {

    private static final long serialVersionUID = 1L;

    @Override
    public double shared(SparseDoubleVector A, SparseDoubleVector B) {
        return super.shared(B, A);
    }

    @Override
    public double left(SparseDoubleVector A) {
        return super.right(A);
    }

    @Override
    public double right(SparseDoubleVector B) {
        return super.left(B);
    }

    @Override
    public double combine(double shared, double left, double right) {
        return shared == 0 ? 0 : shared / right;
    }

    @Override
    public String toString() {
        return "Precision";
    }
}
