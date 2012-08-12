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

import static java.lang.Math.min;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.byblo.weighings.impl.NullWeighting;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * Static utility class providing common functionality to various similarity
 * measures.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class Measures {

    private static final Log LOG = LogFactory.getLog(Measures.class);

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

    public static Measure autoWeighted(Measure measure, Weighting weighting) {
        Checks.checkNotNull("measure", measure);
        if (weighting.getClass().equals(NullWeighting.class)) {
            return measure;
        } else {
            return new AutoWeightingMeasure(measure, weighting);
        }
    }

    public static Measure reverse(Measure measure) {
        Checks.checkNotNull("measure", measure);
        if (measure.isCommutative()) {
            if (LOG.isWarnEnabled())
                LOG.warn("Attempting to reverse a commutative measure.");
            return measure;
        } else {
            return new ReversedMeasure(measure);
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

    public static Map<String, Class<? extends Measure>> loadMeasureAliasTable()
            throws ClassNotFoundException {

        // Map that will store measure aliases to class
        final Map<String, Class<? extends Measure>> classLookup =
                new HashMap<String, Class<? extends Measure>>();

        final ResourceBundle res = ResourceBundle.getBundle(
                uk.ac.susx.mlcl.byblo.measures.Measure.class.getPackage().
                getName() + ".measures");
        final String[] measures = res.getString("measures").split(",");

        for (int i = 0; i < measures.length; i++) {
            final String measure = measures[i].trim();
            final String className = res.getString(
                    "measure." + measure + ".class");
            @SuppressWarnings("unchecked")
            final Class<? extends Measure> clazz =
                    (Class<? extends Measure>) Class.forName(className);
            classLookup.put(measure.toLowerCase(), clazz);
            if (res.containsKey("measure." + measure + ".aliases")) {
                final String[] aliases = res.getString(
                        "measure." + measure + ".aliases").split(",");
                for (String alias : aliases) {
                    classLookup.put(alias.toLowerCase().trim(), clazz);
                }
            }
        }
        return classLookup;
    }
}
