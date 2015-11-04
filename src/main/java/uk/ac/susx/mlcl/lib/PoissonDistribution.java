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
package uk.ac.susx.mlcl.lib;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.AbstractIntIterator;
import it.unimi.dsi.fastutil.ints.IntIterator;

import java.util.NoSuchElementException;
import java.util.Random;

/**
 * General health warning: This class been developer relatively quickly to meet
 * some non-critical needs. It should be correct but is far from optimized. Do
 * not use it if performance or generality is important.
 *
 * @author hiam20
 */
public class PoissonDistribution {

    private final double lambda;

    private final double exp_neg_lambda;

    private Random random = new Random();

    public PoissonDistribution(final double lambda) {
        if (lambda <= 0)
            throw new IllegalArgumentException("lambda <= 0");
        this.lambda = lambda;
        exp_neg_lambda = Math.exp(-lambda);
    }

    public double getLambda() {
        return lambda;
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        Preconditions.checkNotNull(random, "random");
        this.random = random;
    }

    public double mean() {
        return lambda;
    }

    public double variance() {
        return lambda;
    }

    public int median() {
        return (int) Math.floor(lambda + (1.0 / 3.0) - 0.02 / lambda);
    }

    /**
     * Probability Mass Function (PMF)
     *
     * @param k
     * @return
     */
    public double pmf(final int k) {
        if (k < 0 || k > 170)
            throw new IllegalArgumentException(
                    "only implemented for 0 <= k < 170");
        return (Math.pow(lambda, k) / factorial(k)) * exp_neg_lambda;
    }

    /**
     * Cumulative Distribution Function (CDF)
     *
     * @return
     */
    public double cdf(final int k) {
        if (k < 0 || k > 170)
            throw new IllegalArgumentException(
                    "only implemented for 0 <= k < 170");
        double sum = 0;
        for (int i = 0; i <= k; i++)
            sum += Math.pow(lambda, i) / factorial(i);
        return exp_neg_lambda * sum;
    }

    @VisibleForTesting
    static double factorial(final int n) {
        if (n < 0)
            throw new IllegalArgumentException("n < 0");
        if (n > 170)
            throw new IllegalArgumentException(
                    "n! can not be calculated for n>170");
        else {
            double x_factorial = 1;
            for (int i = 2; i <= n; i++)
                x_factorial *= i;
            return x_factorial;
        }
    }

    /**
     * Knuth's method
     *
     * @return
     */
    public int random() {
        // Let L ← e^−λ, k ← 0 and p ← 1.
        int k = 0;
        double p = 1;
        do {
            ++k;
            // Generate uniform random number u in [0,1] and let p ← p × u.
            p *= random.nextDouble();
        } while (p > exp_neg_lambda);
        return k - 1;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PoisonDistribution[lambda=");
        builder.append(lambda);
        builder.append("]");
        return builder.toString();
    }

    public IntIterator generator(final long limit) {
        return new Generator(limit);
    }

    public IntIterator generator() {
        return new Generator();
    }

    private class Generator extends AbstractIntIterator {
        public static final long NO_LIMIT = -1;
        private final long limit;
        private long count = 0;

        private Generator(final long limit) {
            this.limit = limit;
        }

        private Generator() {
            this(NO_LIMIT);
        }

        @Override
        public boolean hasNext() {
            return limit == NO_LIMIT || count < limit;
        }

        @Override
        public int nextInt() {
            if (!hasNext())
                throw new NoSuchElementException();
            ++count;
            return PoissonDistribution.this.random();
        }

    }
}
