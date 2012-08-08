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
package uk.ac.susx.mlcl.byblo.measures.v2;

import static java.lang.Math.min;
import java.text.MessageFormat;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.byblo.weighings.Weightings;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * Static utility class providing common functionality to various similarity
 * measures.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class Measures {

    /**
     * Static utility class should not be instantiated.
     */
    private Measures() {
    }

    /**
     * Produce the multi-set intersection of the feature vectors
     * <tt>vectorA</tt> and <tt>vectorB</tt>. Calculated as the sum of the of
     * the minimum value at each index.
     *
     * @param vectorA first feature vector
     * @param vectorB second feature vector
     * @return multi-set intersection of feature vectors
     */
    public static double intersection(final SparseDoubleVector vectorA,
                                      final SparseDoubleVector vectorB) {
        Checks.checkNotNull("vectorA", vectorA);
        Checks.checkNotNull("vectorB", vectorB);
        double shared = 0;
        int i = 0;
        int j = 0;
        while (i < vectorA.size && j < vectorB.size) {
            if (vectorA.keys[i] < vectorB.keys[j]) {
                i++;
            } else if (vectorA.keys[i] > vectorB.keys[j]) {
                j++;
            } else {
                shared += min(vectorA.values[i], vectorB.values[j]);
                i++;
                j++;
            }
        }
        return shared;
    }

    /**
     * Produce the multi-set union of the feature vectors <tt>vectorA</tt> and
     * <tt>vectorB</tt>. Calculated as the sum of the multi-set cardinality of
     * both vectors, minute the intersection.
     *
     * @param vectorA first feature vector
     * @param vectorB second feature vector
     * @return multi-set union of vectors
     */
    public static double union(final SparseDoubleVector vectorA,
                               final SparseDoubleVector vectorB) {
        Checks.checkNotNull("vectorA", vectorA);
        Checks.checkNotNull("vectorB", vectorB);
        return cardinality(vectorA) + cardinality(vectorB)
                - intersection(vectorA, vectorB);
    }

    /**
     * Return the multi-set cardinality of the vector, which is the sum.
     *
     * @param vector feature vector to calculate cardinality of
     * @return cardinality
     */
    public static double cardinality(final SparseDoubleVector vector) {
        Checks.checkNotNull("vector", vector);
        return vector.sum;
    }

    /**
     * Calculate the inner product of vectors <tt>vectorA</tt> and
     * <tt>vectorB</tt>.
     *
     * @param vectorA first feature vector
     * @param vectorB second feature vector
     * @return inner product
     */
    public static double dotProduct(final SparseDoubleVector vectorA,
                                    final SparseDoubleVector vectorB) {
        Checks.checkNotNull("vectorA", vectorA);
        Checks.checkNotNull("vectorB", vectorB);
        double numerator = 0;
        int i = 0;
        int j = 0;
        while (i < vectorA.size && j < vectorB.size) {
            if (vectorA.keys[i] < vectorB.keys[j]) {
                i++;
            } else if (vectorA.keys[i] > vectorB.keys[j]) {
                j++;
            } else {
                numerator += vectorA.values[i] * vectorB.values[j];
                i++;
                j++;
            }
        }
        return numerator;
    }

    /**
     * Calculate the squared length of the vector; i.e the inner product of the
     * vector with itself.
     *
     * @param vector vector to calculate the squared length of
     * @return squared length
     */
    public static double lengthSquared(final SparseDoubleVector vector) {
        Checks.checkNotNull("vector", vector);
        double normSquared = 0;
        for (int i = 0; i < vector.size; i++)
            normSquared += vector.values[i] * vector.values[i];
        return normSquared;
    }

    /**
     * Return the length of the vector (i.e the vector normal) calculated as the
     * square-root of the inner product of the vector with itself.
     *
     * @param vector vector to calculate the length of
     * @return length
     */
    public static double length(final SparseDoubleVector vector) {
        Checks.checkNotNull("vector", vector);
        return (vector.size == 0) ? 0
                : Math.sqrt(lengthSquared(vector));
    }

    /**
     *
     * @param <T>
     */
    private static class AutoWeightingAdapter<T extends Measure>
            implements Measure {

        private final T innerMeasure;

        private AutoWeightingAdapter(T innerMeasure) {
            Checks.checkNotNull("innerMeasure", innerMeasure);
            this.innerMeasure = innerMeasure;
        }

        public final T getInnerMeasure() {
            return innerMeasure;
        }

        @Override
        public double getHomogeneityBound() {
            return innerMeasure.getHomogeneityBound();
        }

        @Override
        public double getHeterogeneityBound() {
            return innerMeasure.getHeterogeneityBound();
        }

        @Override
        public Weighting getExpectedWeighting() {
            return Weightings.none();
        }

        @Override
        public boolean isCommutative() {
            return innerMeasure.isCommutative();
        }

        public boolean equals(AutoWeightingAdapter<?> other) {
            if (this.innerMeasure != other.innerMeasure
                    && (this.innerMeasure == null
                        || !this.innerMeasure.equals(other.innerMeasure)))
                return false;
            return true;
        }

        protected SparseDoubleVector weight(SparseDoubleVector vector) {
            return getInnerMeasure().getExpectedWeighting().apply(vector);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            return equals((AutoWeightingAdapter) obj);
        }

        @Override
        public int hashCode() {
            return 79 * 5 + (this.innerMeasure != null
                             ? this.innerMeasure.hashCode() : 0);
        }

        @Override
        public String toString() {
            return "AutoWeighting{measure=" + innerMeasure
                    + ", weighting=" + innerMeasure.getExpectedWeighting() + '}';
        }
    }

    /**
     *
     */
    private static class AutoWeightingDistance
            extends AutoWeightingAdapter<Distance>
            implements Distance {

        private AutoWeightingDistance(Distance innerMeasure) {
            super(innerMeasure);
        }

        @Override
        public double distance(SparseDoubleVector A, SparseDoubleVector B) {
            return getInnerMeasure().distance(weight(A), weight(B));
        }
    }

    /**
     *
     */
    private static class AutoWeightingProximity
            extends AutoWeightingAdapter<Proximity>
            implements Proximity {

        private AutoWeightingProximity(Proximity innerMeasure) {
            super(innerMeasure);
        }

        @Override
        public double proximity(SparseDoubleVector A, SparseDoubleVector B) {
            return getInnerMeasure().proximity(weight(A), weight(B));
        }
    }

    public static Distance autoWeighted(Distance measure) {
        Checks.checkNotNull("measure", measure);
        if (measure.getExpectedWeighting().equals(Weightings.none())) {
            return measure;
        } else {
            return new AutoWeightingDistance(measure);
        }
    }

    public static Proximity autoWeighted(Proximity measure) {
        Checks.checkNotNull("measure", measure);
        if (measure.getExpectedWeighting().equals(Weightings.none())) {
            return measure;
        } else {
            return new AutoWeightingProximity(measure);
        }
    }

    public static Measure autoWeighted(Measure measure) {
        Checks.checkNotNull("measure", measure);
        if (measure instanceof Distance) {
            return autoWeighted((Distance) measure);
        } else if (measure instanceof Proximity) {
            return autoWeighted((Proximity) measure);
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Argument 'measure' expected to be of type Proximity or "
                    + "Distance but found {0} of type {1}",
                    measure, measure.getClass()));
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
