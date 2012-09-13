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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * 
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class CosineTest extends AbstractMeasureTest<Cosine> {

	@Override
    public Class<? extends Cosine> getImplementation() {
		return Cosine.class;
	}

	@Override
    public String getMeasureName() {
		return "cosine";
	}

	@Test
	public void testAgainReferenceImplementation() {
		System.out.println("testAgainReferenceImplementation()");

		final double[] arr1 = new double[] { 0, 1, 0, 3, 0, 8, 0, 3, 0, 6, 0, 2 };
		final double[] arr2 = new double[] { 6, 2, 0, 4, 5, 1, 9, 0, 5, 2, 0, 0 };

		double shared = 0;
		double normSq1 = 0;
		double normSq2 = 0;

		for (int i = 0; i < arr1.length; i++) {
			shared += arr1[i] * arr2[i];
			normSq1 += arr1[i] * arr1[i];
			normSq2 += arr2[i] * arr2[i];
		}

		double expected = shared / (Math.sqrt(normSq1) * Math.sqrt(normSq2));

		SparseDoubleVector vec1 = new SparseDoubleVector(arr1.length);
		SparseDoubleVector vec2 = new SparseDoubleVector(arr2.length);

		for (int i = 0; i < arr1.length; i++) {
			vec1.set(i, arr1[i]);
			vec2.set(i, arr2[i]);
		}

		final double actual = similarity(newInstance(), vec1, vec2);

		assertEquals(expected, actual, EPSILON);
	}

}
