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

import uk.ac.susx.mlcl.byblo.measures.Measure;
import uk.ac.susx.mlcl.byblo.measures.Measures;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.byblo.weighings.impl.NullWeighting;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

import java.io.Serializable;

import static java.lang.Math.*;

/**
 * <h4>Notes</h4>
 * <p/>
 * <ul>
 * <p/>
 * <li>All results are inverted to produce proximities rather than distances.
 * I.e Values are between 0 and 1 (inclusive) where 0 indicates infinite
 * distance, and 1 indicates no distance at all.</li>
 * <p/>
 * <li>L0 is defined as the L0 "norm" (with quotes) by David Donoho. This is
 * effectively the non-zero cardinality of the absolute difference between
 * vectors.</li>
 * <p/>
 * </ul>
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class LpSpaceDistance implements Measure, Serializable {

    public static final double DEFAULT_POWER = 2;

    private static final long serialVersionUID = 1L;

    private double power = Double.NaN;

    private Measure delegate = null;

    public LpSpaceDistance() {
        setPower(DEFAULT_POWER);
    }

    public LpSpaceDistance(double power) {
        setPower(power);
    }

    public final double getPower() {
        return power;
    }

    public final void setPower(final double newPower) {
        if (Double.isNaN(newPower))
            throw new IllegalArgumentException("newPower is NaN");
        if (power != newPower) {
            power = newPower;
            if (Measures.epsilonEquals(power, 0, 0)) {
                delegate = new HammingDistance();
            } else if (Measures.epsilonEquals(power, 1, 0)) {
                delegate = new ManhattanDistance();
            } else if (Measures.epsilonEquals(power, 2, 0)) {
                delegate = new EuclideanDistance();
            } else if (Measures.
                    epsilonEquals(power, Double.POSITIVE_INFINITY, 0)) {
                delegate = new MaxSpaceDistance();
            } else if (Measures.
                    epsilonEquals(power, Double.NEGATIVE_INFINITY, 0)) {
                delegate = new MinSpaceDistance();
            } else {
                if (delegate == null || delegate.getClass() != MinkowskiDistance.class)
                    delegate = new MinkowskiDistance();
            }
        }
    }

    @Override
    public double similarity(SparseDoubleVector A, SparseDoubleVector B) {
        return delegate.similarity(A, B);
    }

    @Override
    public double getHomogeneityBound() {
        return delegate.getHomogeneityBound();
    }

    @Override
    public double getHeterogeneityBound() {
        return delegate.getHeterogeneityBound();
    }

    @Override
    public Class<? extends Weighting> getExpectedWeighting() {
        return NullWeighting.class;
    }

    @Override
    public String toString() {
        return "Lp{" + "p=" + power + '}';
    }

    @Override
    public boolean isCommutative() {
        return true;
    }

    /**
     * Abstract super class to the various Lp Space metric implementations.
     */
    private static abstract class LpSpaceDelegate implements Measure {

        @Override
        public Class<? extends Weighting> getExpectedWeighting() {
            return NullWeighting.class;
        }

        @Override
        public final boolean isCommutative() {
            return true;
        }

        @Override
        public final double getHomogeneityBound() {
            return 0.0;
        }

        @Override
        public final double getHeterogeneityBound() {
            return Double.POSITIVE_INFINITY;
        }
    }

    /**
     * Fallback implementation for arbitrary p-spaces. Not though that it will
     * not produce the correct results of p = -inf, 0, or +inf.
     */
    private final class MinkowskiDistance extends LpSpaceDelegate {

        private static final long serialVersionUID = 1L;

        @Override
        public double similarity(SparseDoubleVector A, SparseDoubleVector B) {
            double distance = 0;

            int i = 0, j = 0;
            while (i < A.size && j < B.size) {
                if (A.keys[i] < B.keys[j]) {
                    distance += pow(A.values[i], power);
                    i++;
                } else if (A.keys[i] > B.keys[j]) {
                    distance += pow(B.values[j], power);
                    j++;
                } else {
                    distance += pow(abs(A.values[i] - B.values[j]), power);
                    i++;
                    j++;
                }

            }
            while (i < A.size) {
                distance += pow(A.values[i], power);
                i++;
            }
            while (j < B.size) {
                distance += pow(B.values[j], power);
                j++;
            }

            return Math.pow(distance, 1.0 / power);
        }
    }

    /**
     * Implementation of the power-2 space; i.e standard Euclidean space that we
     * are all used to.
     */
    private static final class EuclideanDistance extends LpSpaceDelegate {

        private static final long serialVersionUID = 1L;

        @Override
        public double similarity(SparseDoubleVector A, SparseDoubleVector B) {
            double distance = 0;

            int i = 0, j = 0;
            while (i < A.size && j < B.size) {
                if (A.keys[i] < B.keys[j]) {
                    distance += A.values[i] * A.values[i];
                    i++;
                } else if (A.keys[i] > B.keys[j]) {
                    distance += B.values[j] * B.values[j];
                    j++;
                } else {
                    distance += (A.values[i] - B.values[j])
                            * (A.values[i] - B.values[j]);
                    i++;
                    j++;
                }

            }
            while (i < A.size) {
                distance += A.values[i] * A.values[i];
                i++;
            }
            while (j < B.size) {
                distance += B.values[j] * B.values[j];
                j++;
            }

            return Math.sqrt(distance);
        }
    }

    /**
     * Implementation of the power-1 space; known as Manhattan or taxicab
     * distance.
     */
    private static final class ManhattanDistance extends LpSpaceDelegate {

        private static final long serialVersionUID = 1L;

        @Override
        public double similarity(SparseDoubleVector A, SparseDoubleVector B) {
            double distance = 0;

            int i = 0, j = 0;
            while (i < A.size && j < B.size) {
                if (A.keys[i] < B.keys[j]) {
                    distance += abs(A.values[i]);
                    i++;
                } else if (A.keys[i] > B.keys[j]) {
                    distance += abs(B.values[j]);
                    j++;
                } else {
                    distance += abs(A.values[i] - B.values[j]);
                    i++;
                    j++;
                }

            }
            while (i < A.size) {
                distance += abs(A.values[i]);
                i++;
            }
            while (j < B.size) {
                distance += abs(B.values[j]);
                j++;
            }

            return distance;
        }
    }

    /**
     * Implementation of power-zero L space.
     */
    private static final class HammingDistance extends LpSpaceDelegate {

        private static final long serialVersionUID = 1L;

        @Override
        public double similarity(SparseDoubleVector A, SparseDoubleVector B) {
            double distance = 0;

            int i = 0, j = 0;
            while (i < A.size && j < B.size) {
                if (A.keys[i] < B.keys[j]) {
                    distance += signum(abs(A.values[i]));
                    i++;
                } else if (A.keys[i] > B.keys[j]) {
                    distance += signum(abs(B.values[j]));
                    j++;
                } else {
                    distance += signum(abs(A.values[i] - B.values[j]));
                    i++;
                    j++;
                }

            }
            while (i < A.size) {
                distance += signum(abs(A.values[i]));
                i++;
            }
            while (j < B.size) {
                distance += signum(abs(B.values[j]));
                j++;
            }

            return distance;
        }
    }

    /**
     * Implementation of power +infinity L-space metric.
     */
    private static final class MaxSpaceDistance extends LpSpaceDelegate {

        private static final long serialVersionUID = 1L;

        @Override
        public double similarity(SparseDoubleVector A, SparseDoubleVector B) {
            double distance = 0;

            int i = 0, j = 0;
            while (i < A.size && j < B.size) {
                if (A.keys[i] < B.keys[j]) {
                    distance = max(distance, abs(A.values[i]));
                    i++;
                } else if (A.keys[i] > B.keys[j]) {
                    distance = max(distance, abs(B.values[j]));
                    j++;
                } else {
                    distance = max(distance, abs(A.values[i] - B.values[j]));
                    i++;
                    j++;
                }
            }
            while (i < A.size) {
                distance = max(distance, abs(A.values[i]));
                i++;
            }
            while (j < B.size) {
                distance = max(distance, abs(B.values[j]));
                j++;
            }
            return distance;
        }
    }

    /**
     * Implementation of power -infinity L-space metric.
     */
    private static final class MinSpaceDistance extends LpSpaceDelegate {

        private static final long serialVersionUID = 1L;

        @Override
        public double similarity(SparseDoubleVector A, SparseDoubleVector B) {
            double distance = 0;

            int i = 0, j = 0;
            while (i < A.size && j < B.size) {
                if (A.keys[i] < B.keys[j]) {
                    distance = min(distance, abs(A.values[i]));
                    i++;
                } else if (A.keys[i] > B.keys[j]) {
                    distance = min(distance, abs(B.values[j]));
                    j++;
                } else {
                    distance = min(distance, abs(A.values[i] - B.values[j]));
                    i++;
                    j++;
                }
            }
            while (i < A.size) {
                distance = min(distance, abs(A.values[i]));
                i++;
            }
            while (j < B.size) {
                distance = min(distance, abs(B.values[j]));
                j++;
            }

            return distance;
        }
    }
}
