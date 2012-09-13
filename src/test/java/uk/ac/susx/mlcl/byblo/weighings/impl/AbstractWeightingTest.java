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
package uk.ac.susx.mlcl.byblo.weighings.impl;

import junit.framework.Assert;
import org.junit.Test;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import uk.ac.susx.mlcl.testing.AbstractObjectTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class AbstractWeightingTest<T extends Weighting> extends AbstractObjectTest<T> {

    static final double EPSILON = 1E-15;

    protected abstract String getWeightingName();


    //
    // ============================================================================
    // Tests
    // ============================================================================
    //

    /**
     * Measures are generally instantiated through reflect. They require a
     * publicly accessible default (no argument) constructor.
     */
    @Test
    public void testDefaultConstructor() {
        System.out.println("testDefaultConstructor()");

        try {
            Class<? extends T> clazz = getImplementation();
            Assert.assertFalse("Measure is abstract",
                    Modifier.isAbstract(clazz.getModifiers()));
            Assert.assertFalse("Measure is an interface",
                    Modifier.isInterface(clazz.getModifiers()));

            Constructor<? extends T> constructor = clazz.getConstructor();
            Assert.assertTrue("Default constructor is not public",
                    Modifier.isPublic(constructor.getModifiers()));

            final T instance = constructor.newInstance();
            Assert.assertNotNull("instance", instance);

        } catch (SecurityException e) {
            throw new AssertionError(e);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw new AssertionError(e);
        } catch (InstantiationException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Insure that the lower-bound property is actually a number
     */
    @Test
    public void testGetLowerBound() {
        final T instance = newInstance();
        final double lowerBound = instance.getLowerBound();
        Assert.assertTrue("lower bound is NaN", !Double.isNaN(lowerBound));
    }

    /**
     * Insure that the upper-bound property is actually a number
     */
    @Test
    public void testGetUpperBound() {
        final T instance = newInstance();
        final double upperBound = instance.getUpperBound();
        Assert.assertTrue("upper bound is NaN", !Double.isNaN(upperBound));
    }

    /**
     * Check the lower and upper bounds are oriented correctly
     */
    @Test
    public void testBoundOrientation() {
        final T instance = newInstance();
        final double lowerBound = instance.getLowerBound();
        final double upperBound = instance.getUpperBound();

        Assert.assertTrue(Double.NEGATIVE_INFINITY <= lowerBound);
        Assert.assertTrue("lower bound >= upper bound", lowerBound < upperBound);
        Assert.assertTrue(upperBound <= Double.POSITIVE_INFINITY);
    }

    /**
     * Test that the weighting doesn't explode when given an empty vector
     */
    @Test
    public void testEmptyVector() {
        SparseDoubleVector vector = new SparseDoubleVector(0);
        final T instance = newInstance();
        SparseDoubleVector reweightedVector = instance.apply(vector);
        Assert.assertNotNull("reweighted vector is null", reweightedVector);
        Assert.assertEquals("a reweighted empty vector should be identical to the original", vector, reweightedVector);
    }

    /**
     * Test that the weighting doesn't explode when given a vector containing exactly one element.
     */
    @Test
    public void testSizeOneVector() {
        SparseDoubleVector vector = new SparseDoubleVector(1);
        vector.set(0, 1);
        final T instance = newInstance();
        SparseDoubleVector reweightedVector = instance.apply(vector);
        Assert.assertNotNull("reweighted vector is null", reweightedVector);
        Assert.assertEquals("reweighted cardinality 1 vector should also be of cardinality 1", 1, reweightedVector.cardinality());
    }


}
