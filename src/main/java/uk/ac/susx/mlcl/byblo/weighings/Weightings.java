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

import java.util.Arrays;
import java.util.Collection;
import uk.ac.susx.mlcl.lib.Checks;

/**
 * Factory class from some derivative {@link Weighting} implementations.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class Weightings {

    /**
     * Static utility class should not ever be instantiated.
     */
    private Weightings() {
    }

    /**
     * @param weightings weightings to compose
     * @return weighting scheme the runs the given weightings in sequence.
     */
    public static Weighting compose(Weighting... weightings) {
        Checks.checkNotNull("weightings", weightings);
        if (weightings.length == 0)
            throw new IllegalArgumentException("weightings is empty");
        if (weightings.length == 1)
            return weightings[0];
        else {
            return new CompositeWeighting(Arrays.asList(weightings));
        }
    }

    /**
     * @param weightings weightings to compose
     * @return weighting scheme the runs the given weightings in sequence.
     */
    public static Weighting compose(Collection<Weighting> weightings) {
        Checks.checkNotNull("weightings", weightings);
        if (weightings.isEmpty())
            throw new IllegalArgumentException("weightings is empty");
        if (weightings.size() == 1)
            return weightings.iterator().next();
        else {
            return new CompositeWeighting(weightings);
        }
    }

    /**
     * Constant to aid conversion to base 2 logarithms.
     *
     * Conceptually it doesn't really matter what base is used, but 2 is the
     * standard base for most information theoretic approaches.
     *
     * TODO: Move to mlcl-lib/MathUtil
     */
    public static final double LOG_2 = Math.log(2.0);

    /**
     * Return the base 2 logarithm of the parameter v.
     *
     * TODO: Move to mlcl-lib/MathUtil
     *
     * @param v some values
     * @return logarithm of the value
     */
    public static double log2(final double v) {
        return Math.log(v) / LOG_2;
    }
}
