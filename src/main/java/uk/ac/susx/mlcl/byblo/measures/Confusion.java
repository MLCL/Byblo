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
package uk.ac.susx.mlcl.byblo.measures;

import com.google.common.annotations.Beta;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * Proximity measure computing the confusion probability between the given
 * vector pair.
 *
 * Essen, Ute and Volker Steinbiss. 1992. Co-occurrence smoothing for stochastic
 * language modeling. In Proceed- ings of ICASSP, volume 1, pages 161{164.
 *
 * Grishman, Ralph and John Sterling. 1993. Smoothing of automatically generated
 * selectional constraints. In Hu- man Language Technology, pages 254{259, San
 * Fran- cisco, California. Advanced Research Projects Agency, Software and
 * Intelligent Systems Technology Oce, Morgan Kaufmann.
 * 
 *
 * Discussed in JE Weeds (2003) "Measures and Applications of Lexical
 * Distributional Similarity", which references (Sugawara, Nishimura, Toshioka,
 * Okachi, & Kaneko, 1985; Essen & Steinbiss, 1992; Grishman & Sterling, 1993;
 * Dagan et al., 1999; Lapata et al., 2001)
 *
 * sim(q,r) = sum((P(q_i) * P(r_i)) / P(c_i))
 *
 * Expected range: [0,1] (JE Weeds, 2003)
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @deprecated replaced by v2 measures and weightings
 */
@Beta
public final class Confusion extends AbstractMIProximity {

    @Override
    public double shared(final SparseDoubleVector a,
                         final SparseDoubleVector b) {
        double total = 0.0;

        int i = 0, j = 0;
        while (i < a.size && j < b.size) {
            if (a.keys[i] < b.keys[j]) {
                i++;
            } else if (a.keys[i] > b.keys[j]) {
                j++;
            } else if (isFiltered(a.keys[i])) {
                i++;
                j++;
            } else { // Q.keys[i] == R.keys[j]
                total += prob(a, i) * prob(b, j)
                        / featurePrior(a.keys[i]);

                i++;
                j++;
            }
        }
        total *= entryPrior(a);

        assert total >= 0.0 && total <= 1.0 : "Expecting output in the range 0 to 1";
        return total;
    }

    @Override
    public double left(final SparseDoubleVector a) {
        return 0;
    }

    @Override
    public double right(final SparseDoubleVector b) {
        return 0;
    }

    @Override
    public double combine(final double shared,
                          final double left, final double right) {
        return shared;
    }

    @Override
    public boolean isSymmetric() {
        return false;
    }

    @Override
    public String toString() {
        return "Confusion{}";
    }
}
