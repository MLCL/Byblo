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
 *
 * @author hiam20
 */
public class ZipfianDist {

    /**
     * The population size.
     */
    private final int populationSize;

    /**
     * Exponent characterizing the distribution.
     */
    private final double exponent;

    private Random random = null;

    private transient double[] H = null;

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
        if (H == null)
            H = generate_H_table(populationSize, exponent);
        return (1.0 / Math.pow(k, exponent)) / H[populationSize];
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
        if (H == null)
            H = generate_H_table(populationSize, exponent);
        return H[k] / H[populationSize];
    }

    /**
     * A pseudo-inverse of the cumulative distribution function.
     *
     * For a given value of uniformly distributed random variable u, calculate
     * the minimum rank that is at least a probable as u.
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

    /**
     * Pre-calculate the harmonic number table H for all ranks in the
     * distribution.
     *
     * The n-th generalized harmonic number is the sum of the reciprocals of the
     * first n natural numbers to the power m:
     *
     * H[n] = 1/(1^2) + 1/(2^m) + 1/3 + ... + 1/n
     *
     * @param N
     * @param s
     * @return
     */
    private static double[] generate_H_table(int N, double s) {
        double[] H = new double[N + 1];
        H[0] = 0;
        H[1] = 1;
        for (int k = 2; k <= N; k++)
            H[k] = H[k - 1] + 1.0 / Math.pow(k, s);
        return H;
    }

    @Override
    public String toString() {
        return "ZipfianDist{" + "populationSize=" + populationSize + ", exponent=" + exponent + '}';
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
        if (Double.doubleToLongBits(this.exponent) != Double.doubleToLongBits(other.exponent))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + this.populationSize;
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.exponent) ^ (Double.doubleToLongBits(this.exponent) >>> 32));
        return hash;
    }

}
