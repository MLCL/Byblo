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
package uk.ac.susx.mlcl.lib.collect;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.susx.mlcl.lib.MemoryUsage;
import uk.ac.susx.mlcl.lib.MiscUtil;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class IntBitSetTest extends AbstractIntSortedSetTest<IntBitSet> {

	@Override
	Class<? extends IntBitSet> getImplementation() {
		return IntBitSet.class;
	}

	@Test
	public void testPeekMemoryUsage() {
		System.out.println("testPeekMemoryUsage()");

		// Test needs at least 500MB of memory. Fail if it isn't available.
		final long requiredMemory = (1L << 29);
		Assert.assertTrue(String.format(
				"Please re-run tests with at least %s of available memory.",
				MiscUtil.humanReadableBytes(requiredMemory)), Runtime
				.getRuntime().maxMemory() > requiredMemory);

		IntBitSet instance = newInstance();
		instance.add(0);
		instance.add(Integer.MAX_VALUE);

		final MemoryUsage mu = new MemoryUsage();
		mu.add(instance);
		System.out.println("> object memory: " + mu.getInfoString());
		System.out.println("> system memory: " + MiscUtil.memoryInfoString());

		// Expected memory usage is 256 MiB of 2^31 bits @ 8 bits per bytes
		final double expectedSizeMB = (Integer.MAX_VALUE + 1.0)
				/ (8 * 1024 * 1024);
		final double actualSizeMB = (double) mu.getInstanceSizeBytes()
				/ (1024 * 1024);
		final double allowedeErrorMB = 1.0 / 1024;// Allow +/- 1KiB
		Assert.assertEquals(expectedSizeMB, actualSizeMB, allowedeErrorMB);

	}

}
