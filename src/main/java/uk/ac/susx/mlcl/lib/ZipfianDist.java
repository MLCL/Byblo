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
 * POSSIBILITY OF SUCH DAMAGE.To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib;

import java.util.Random;

/**
 * Class that provides methods to calculate the PMF, CDF, and Quantile functions
 * of a Zipfian (power law) distribution.
 *
 * The Zipfian distribution relates to the frequency of words in a corpus of
 * natural language text. It states that the frequency of any given word is
 * inversely proportional to it's rank.
 *
 * The Zipfian distribution is discrete, so the quantile function is an
 * approximation, without a closed form solution.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class ZipfianDist {

    private static final double Euler_Mascheroni_constant =
            0.57721566490153286060651209008240243104215933593992;

    /**
     * The population size.
     */
    private final int populationSize;

    /**
     * Exponent characterizing the distribution.
     */
    private final double exponent;

    private Random random = null;

    /**
     * Construct a new instance of the ZipfianDist object, parameterized by the
     * size of the population (N), and the exponent characterizing the
     * distribution (s).
     *
     * @param populationSize Population size.
     * @param exponent Exponent characterizing the distribution.
     */
    public ZipfianDist(int populationSize, double exponent) {
        Checks.checkRangeIncl(populationSize, 1, Integer.MAX_VALUE);
        Checks.checkRangeExcl(exponent, 0, Double.POSITIVE_INFINITY);
        assert exponent > 0.0;
        assert populationSize >= 1;
        this.populationSize = populationSize;
        this.exponent = exponent;
    }

    /**
     * Construct a new instance of the ZipfianDist object, parameterized by the
     * size of the population (N).
     *
     * The exponent characterizing the distribution is set the classic default
     * of 1.
     *
     * @param N Population size.
     */
    public ZipfianDist(int N) {
        this(N, 1);
    }

    /**
     *
     * @return size of the population
     */
    public int getPopulationSize() {
        return populationSize;
    }

    /**
     * @return exponent characterizing the distribution
     */
    public double getExponent() {
        return exponent;
    }

    /**
     * @param random number generator object, or null if non is set.
     */
    public Random getRandom() {
        return random;
    }

    /**
     * Use the given random number generator; otherwise it falls back to
     * Math.random.()
     *
     * @param random pseudo random number generator.
     */
    public void setRandom(Random random) {
        this.random = random;
    }

    /**
     * Generate a random value in the distribution
     *
     * @return
     */
    public int random() {
        return quantile(random == null ? Math.random() : random.nextDouble());
    }

    /**
     * Probability-mass-function, returning the probability of elements of rank
     * k.
     *
     * @param k element rank
     * @return probability of element of rank k
     */
    public double pmf(int k) {
        Checks.checkRangeIncl(k, 1, populationSize);
        return (1.0 / Math.pow(k, exponent)) / harm0_s1(populationSize);
    }

    /**
     * Cumulative distribution function, returning the probability of generating
     * any elements up-to and including rank k.
     *
     * @param k element rank
     * @return probability of elements up-to and including rank k.
     */
    public double cdf(int k) {
        Checks.checkRangeIncl(k, 1, populationSize);
        return harm0_s1(k) / harm0_s1(populationSize);
    }

    /**
     * A pseudo-inverse of the cumulative distribution function.
     *
     * For a given value of uniformly distributed random variable u, calculate
     * the minimum rank that is at least a probable as u.
     *
     * Since the distribution is over discrete random variables, there is not
     * closed for solution for the quantile function. Hence it is calculate by
     * binary searching the cumulative distribution function for interval such
     * that cdf(k) &lt; u &lt; cdf(k+1).
     *
     * @param u probability value
     * @return
     */
    private int quantile(double u) {
        Checks.checkRangeIncl(u, 0, 1);
        assert u >= 0.0 && u <= 1.0;
        return quantile0(u, 1, populationSize);
    }

    /**
     * Binary search the cumulative distribution function
     *
     * @param u
     * @param min
     * @param max
     * @return
     */
    private int quantile0(double u, int min, int max) {
        assert u >= 0.0 && u <= 1.0;
        assert min >= 0;
        assert max <= populationSize;
        assert min < max;
        if (max - min == 1) {
            return max;
        } else {
            final int mid = min + ((max - min) / 2);
            if (u < cdf(mid)) {
                return quantile0(u, min, mid);
            } else {
                return quantile0(u, mid, max);
            }
        }
    }

    protected static double harm(long n, double s) {
        if (s == 1) {
            return harm0_s1(n);
        } else {
            return harm1(n, s);
        }
    }

    /**
     * Approximation of the harmonic series. Get's closer as n tends to
     * infinity, so the first 20 values are calculated with the iterative
     * method.
     *
     * @param n
     * @return
     */
    protected static double harm0_s1(long n) {
        Checks.checkRangeIncl(n, 1, Integer.MAX_VALUE);

        if (n < 20)
            return harm1(n, 1);
        return Math.log(n) + Euler_Mascheroni_constant
                + 1.0 / (2.0 * n)
                - 1.0 / (12.0 * n * n)
                + 1.0 / (120.0 * n * n * n * n)
                - 1.0 / (252.0 * n * n * n * n * n * n)
                + 1.0 / (240.0 * n * n * n * n * n * n * n * n);
    }

    protected static double harm1(long n, double s) {
        Checks.checkRangeIncl(n, 1, Integer.MAX_VALUE);

        double h = 1;
        if (s == 1) {
            for (long k = 2; k <= n; ++k)
                h += 1.0 / k;
        } else if (s == 2) {
            for (long k = 2; k <= n; ++k)
                h += 1.0 / (k * k);
        } else {
            for (long k = 2; k <= n; ++k)
                h += Math.pow(1.0 / k, s);
        }
        return h;
    }

    @Override
    public String toString() {
        return "ZipfianDist{"
                + "populationSize=" + populationSize
                + ", exponent=" + exponent + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ZipfianDist other = (ZipfianDist) obj;
        if (this.populationSize != other.populationSize)
            return false;
        // zero checks to support 0 == -0
        if ((this.exponent != 0 && other.exponent != 0)
                && Double.doubleToLongBits(this.exponent) != Double.doubleToLongBits(other.exponent))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + this.populationSize;
        // zero check to support 0 == -0
        final long exponentBits = exponent == 0 ? 0
                                  : Double.doubleToLongBits(this.exponent);
        hash = 17 * hash + (int) (exponentBits ^ (exponentBits >>> 32));
        return hash;
    }

}
