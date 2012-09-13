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

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import uk.ac.susx.mlcl.byblo.measures.Measure;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * 
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class HindleTest extends AbstractMeasureTest<Hindle> {

	@Override
	Class<? extends Hindle> getMeasureClass() {
		return Hindle.class;
	}

	@Override
	String getMeasureName() {
		return "hindle";
	}

	/**
	 * Identity score varies bases on vector magnitude so homogeneity bound
	 * can't be reached.
	 */
	@Test
	@Override
	public void testSizeOneVectors() {
		System.out.println("testSizeOneVectors");

		final int size = 100;
		final SparseDoubleVector A = new SparseDoubleVector(size, 1);
		final SparseDoubleVector B = new SparseDoubleVector(size, 1);
		A.set(0, 1);
		B.set(0, 1);
		final double expect = 1; // newInstance().getHomogeneityBound();
		final double actual = similarity(newInstance(), A, B);
		Assert.assertEquals(expect, actual, EPSILON);
	}

	/**
	 * Identity score varies bases on vector magnitude so homogeneity bound
	 * can't be reached.
	 */
	@Test
	@Override
	public void testSizeTwoVectors() {
		System.out.println("testSizeTwoVectors");
		int size = 100;
		SparseDoubleVector A = new SparseDoubleVector(size, 2);
		SparseDoubleVector B = new SparseDoubleVector(size, 2);
		A.set(0, 1);
		A.set(1, 1);
		B.set(0, 1);
		B.set(1, 1);
		double expect = 2; // newInstance().getHomogeneityBound();
		double actual = similarity(newInstance(), A, B);

		Assert.assertEquals(expect, actual, EPSILON);
	}

	@Test
	@Override
	public void testCardinalityOneVectors() {
		System.out.println("testCardinalityOneVectors");
		SparseDoubleVector A = new SparseDoubleVector(1, 1);
		SparseDoubleVector B = new SparseDoubleVector(1, 1);
		A.set(0, 1);
		B.set(0, 1);
		Measure instance = newInstance();
		double expect = 1;
		double actual = similarity(instance, A, B);

		Assert.assertEquals(expect, actual, EPSILON);
	}

	/**
	 * Identity score varies bases on vector magnitude so homogeneity bound
	 * can't be reached.
	 */
	@Test
	@Ignore
	@Override
	public void testHomoginiety() {
		throw new UnsupportedOperationException();
	}
	/**
	 * Identity score varies bases on vector magnitude so homogeneity bound
	 * can't be reached.
	 */
	@Test
	@Ignore
	@Override
	public void testHomoginiety2() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Identity score varies bases on vector magnitude so homogeneity bound
	 * can't be reached.
	 */
	@Test
	@Ignore
	@Override
	public void testFruitIdentity() {
		throw new UnsupportedOperationException();
	}

}
