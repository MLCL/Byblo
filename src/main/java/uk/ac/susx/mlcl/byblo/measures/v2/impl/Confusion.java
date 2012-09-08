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
package uk.ac.susx.mlcl.byblo.measures.v2.impl;

/**
 * Proximity measure computing the confusion probability between the given
 * vector pair.
 *
 * Essen, Ute and Volker Steinbiss. 1992. Co-occurrence smoothing for stochastic
 * language modeling. In Proceed- ings of ICASSP, volume 1, pages 161{164.
 *
 * Sugawara, K., M. Nishimura, K. Toshioka, M. Okochi, and T. Kaneko. 1985.
 * Isolated word recognition using hid- den Markov models. In Proceedings of
 * ICASSP, pages 1{4, Tampa, Florida. IEEE.
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
 * @deprecated Not actually implemented
 */
@Deprecated
public class Confusion {
}
