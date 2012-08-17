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
/**
 * Provides pluggable interfaces and re-usable code for implementing feature
 * re-weighting schemes.
 * <p/>
 * <h3>Weighting Implementations</h3>
 * <p/>
 * There are currently four methods for implementing the {@link Weighting}
 * interface, to produce a re-weighting function. The reason for these choices
 * is that some methods are much simpler while others are allow for greater
 * flexibility:
 * <p/>
 * <ul>
 * <p/>
 * <li>{@link uk.ac.susx.mlcl.byblo.extras.weighings.Weighting} is the most
 * general. It works vector-wise; converting an entire feature vector at a time.
 * Other weighting implementations are convert to this type internally.</li>
 * <p/>
 *
 * <li>{@link uk.ac.susx.mlcl.byblo.extras.weighings.AbstractSimpleWeighting} is
 * the simplest to implement since it defines a function that maps element-wise
 * from a single feature input weight to the output weight. This interface
 * should be implemented when the weighting is independent of everything else;
 * for example thresholding</li>
 * <p/>
 *
 * <li>{@link uk.ac.susx.mlcl.byblo.extras.weighings.AbstractElementwiseWeighting}
 * is a more complex weighting which maps element-wise but with addition
 * information provided, such as the full vector end feature id. It should be
 * used when {@link uk.ac.susx.mlcl.byblo.extras.weighings.SimpleWeighting} is
 * insufficiently flexible.</li>
 * <p/>
 *
 * <li>{@link uk.ac.susx.mlcl.byblo.extras.weighings.AbstractContextualWeighting}
 * works in a mannar similar to
 * {@link uk.ac.susx.mlcl.byblo.extras.weighings.AbstractElementwiseWeighting}
 * but with even more information available through utility methods. For example
 * these class makes the marginal distributions available.
 * <p/>
 * </ul>
 * <p/>
 * <h3>Marginal Interfaces</h3>
 * <p/>
 * The above weighting schemes are provided with a limited amount of
 * information, reserved to at most an entries feature vector. If additional
 * contextual information required such as the feature marginal distribution
 * (i.e the weighting on features irrespective of entry) then one or more of the
 * MarginalCarrying interfaces should be implemented
 * <p/>
 * <ul>
 * <p/>
 * <li>{@link uk.ac.susx.mlcl.byblo.extras.weighings.MarginalsCarrier} Super
 * interface for all marginal carriers that provided the grant total (the sum of
 * all weightings across all entries and features). When implemented the grand
 * total will be provided by software by calling
 * {@link MarginalsCarrier#setGrandTota(double) }</li>
 * <p/>
 * <li>{@link uk.ac.susx.mlcl.byblo.extras.weighings.FeatureMarginalsCarrier}
 * denotes an implementation that requires the feature marginal totals
 * independent of entries. When implemented the marginals array will be provided
 * by the software with
 * {@link uk.ac.susx.mlcl.byblo.extras.weighings.FeatureMarginalsCarrier#setFeatureMarginals(double[])}.</li>
 * <p/>
 * <li>{@link uk.ac.susx.mlcl.byblo.extras.weighings.EntryMarginalsCarrier}
 * denotes an implementation that requires the entry marginal totals independent
 * of feature. When implemented the marginals array will be provided by the
 * software with
 * {@link uk.ac.susx.mlcl.byblo.extras.weighings.EntryMarginalsCarrier#setEntryMarginals(double[])}.
 * </li>
 * <p/>
 * <ul>
 * <p/>
 */
@ParametersAreNonnullByDefault
package uk.ac.susx.mlcl.byblo.weighings;

import javax.annotation.ParametersAreNonnullByDefault;
