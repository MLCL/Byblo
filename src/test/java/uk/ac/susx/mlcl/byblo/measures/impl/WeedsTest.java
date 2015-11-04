/*
 * Copyright (c) 2010-2013, University of Sussex
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

import org.junit.Test;

/**
 * 
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class WeedsTest extends AbstractMeasureTest<Weeds> {

	@Override
    public Class<? extends Weeds> getImplementation() {
		return Weeds.class;
	}

	@Override
    public String getMeasureName() {
		return "weeds";
	}

	@Test
	public void testCLI_000_100() throws Exception {
		testCLI(0, 1);
	}

	@Test
	public void testCLI_050_050() throws Exception {
		testCLI(0.5, 0.5);
	}

	@Test
	public void testCLI_100_000() throws Exception {
		testCLI(1, 0);
	}

	@Test
	public void testCLI_075_000() throws Exception {
		testCLI(0.75, 0);
	}

	@Test
	public void testCLI_050_000() throws Exception {
		testCLI(0.5, 0);
	}

	@Test
	public void testCLI_025_000() throws Exception {
		testCLI(0.25, 0);
	}

	@Test
	public void testCLI_000_000() throws Exception {
		testCLI(0, 0);
	}

	void testCLI(double beta, double gamma) throws Exception {
		System.out.println(String.format("testCLI(beta=%.2f, gamma=%.2f)",
				beta, gamma));

		runFromCommandLine("--crmi-beta", Double.toHexString(beta),
				"--crmi-gamma", Double.toHexString(gamma));
	}

}
