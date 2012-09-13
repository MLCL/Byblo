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

import junit.framework.Assert;

import org.junit.Test;

import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * 
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class LambdaDivergenceTest extends AbstractMeasureTest<LambdaDivergence> {

	@Override
    public Class<? extends LambdaDivergence> getImplementation() {
		return LambdaDivergence.class;
	}

	@Override
    public String getMeasureName() {
		return "lambda";
	}

	@Test
	@Override
	public void testOneEmptyVector() {
		System.out.println("testOneEmptyVector");
		int size = 100;
		Random RANDOM = newRandom();
		SparseDoubleVector A = new SparseDoubleVector(size, 0);
		SparseDoubleVector B = new SparseDoubleVector(size, size);
		for (int i = 0; i < size; i++)
			B.set(i, RANDOM.nextDouble());

		double expect = 0.5; // 0;
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
		double expect = 0; // 1
		double actual = similarity(newInstance(), A, B);

		Assert.assertEquals(expect, actual, EPSILON);
	}

	@Test
	public void testCLI_Lambda001() throws Exception {
		runFromCommandLine(0.01);
	}

	@Test
	public void testCLI_Lambda010() throws Exception {
		runFromCommandLine(0.10);
	}

	@Test
	public void testCLI_Lambda050() throws Exception {
		runFromCommandLine(0.50);
	}

	@Test
	public void testCLI_Lambda090() throws Exception {
		runFromCommandLine(0.90);
	}

	@Test
	public void testCLI_Lambda099() throws Exception {
		runFromCommandLine(0.99);
	}

	void runFromCommandLine(final double lambda) throws Exception {
		System.out.printf("testRunFromCommandLine(lambda=%.2f)%n", lambda);

		String[] extraArgs = new String[] { "--lambda-lambda",
				Double.toString(lambda) };
		runFromCommandLine(extraArgs);
	}

}
