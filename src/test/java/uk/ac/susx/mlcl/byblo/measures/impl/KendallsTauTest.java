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

import java.util.Random;

import javax.annotation.CheckReturnValue;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import uk.ac.susx.mlcl.byblo.measures.Measure;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * Unit tests for {@link KendallsTau } proximity measure.
 * <p/>
 * 
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class KendallsTauTest extends AbstractMeasureTest<KendallsTau> {

	@Override
    public Class<? extends KendallsTau> getImplementation() {
		return KendallsTau.class;
	}

	@Override
    public String getMeasureName() {
		return "tau";
	}

	@Test
	@Override
	public void testCardinalityOneVectors() {
		System.out.println("testCardinalityOneVectors");
		SparseDoubleVector A = new SparseDoubleVector(1, 1);
		SparseDoubleVector B = new SparseDoubleVector(1, 1);
		A.set(0, 1);
		B.set(0, 1);
		double expect = 0;
		double actual = similarity(newInstance(), A, B);

		Assert.assertEquals(expect, actual, EPSILON);
	}

	@Test
	@Ignore
	@Override
	public void testHeterogeneity() {
		throw new UnsupportedOperationException();
	}

	@Test
	public void testCompareToNaiveImpl() throws Exception {
		System.out.println("testCompareToNaiveImpl");
		int size = 100;
		int repeats = 10;
		Random RANDOM = newRandom();
		KendallsTau instance = newInstance();
		Measure referenceInstance = new KendallsTauBNaiveImpl1();

		for (int i = 0; i < repeats; i++) {
			SparseDoubleVector A = new SparseDoubleVector(size * 2, size);
			SparseDoubleVector B = new SparseDoubleVector(size * 2, size);
			for (int j = 0; j < size; j++) {
				A.set(RANDOM.nextInt(size * 2), RANDOM.nextDouble());
				B.set(RANDOM.nextInt(size * 2), RANDOM.nextDouble());
			}
			double expect = referenceInstance.similarity(A, B);
			double actual = instance.similarity(A, B);

			Assert.assertEquals(expect, actual, EPSILON);
		}
	}

	/**
	 * A relatively simple implementation of Kendall's Tau-B for use as a
	 * reference when testing more complex implementations.
	 */
	static class KendallsTauBNaiveImpl1 implements Measure {

		@Override
		@CheckReturnValue
		public double similarity(SparseDoubleVector A, SparseDoubleVector B) {
			assert A.cardinality == B.cardinality : "Cardinalities not equal "
					+ A.cardinality + " != " + B.cardinality;

			final int n = A.cardinality;
			long cordance = 0;
			long ties_a = 0;
			long ties_b = 0;
			for (int i = 1; i < n; i++) {
				for (int j = 0; j < i; j++) {
					final double dA = A.get(i) - A.get(j);
					final double dB = B.get(i) - B.get(j);
					if (dA == 0)
						++ties_a;
					if (dB == 0)
						++ties_b;
					cordance += Math.signum(dA) * Math.signum(dB);

				}
			}

			// n choose 2 combinations:
			long combinations = (n * (n - 1)) >> 1;
			double denom = Math.sqrt((combinations - ties_a)
					* (combinations - ties_b));
			return cordance / denom;
		}

		@Override
		@CheckReturnValue
		public double getHomogeneityBound() {
			throw new UnsupportedOperationException();
		}

		@Override
		@CheckReturnValue
		public double getHeterogeneityBound() {
			throw new UnsupportedOperationException();
		}

		@Override
		@CheckReturnValue
		public Class<? extends Weighting> getExpectedWeighting() {
			throw new UnsupportedOperationException();
		}

		@Override
		@CheckReturnValue
		public boolean isCommutative() {
			throw new UnsupportedOperationException();
		}

	}

}
