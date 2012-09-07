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
package uk.ac.susx.mlcl.lib;

import it.unimi.dsi.fastutil.ints.AbstractIntIterator;

import java.util.NoSuchElementException;
import java.util.Random;

public class ZipfianIntGenerator extends AbstractIntIterator {
	public static final int NO_LIMIT = -1;
	private static final int DEFAULT_POPULATION_SIZE = Integer.MAX_VALUE;
	private static final double DEFAULT_EXPONENT = 2.0;
	private final ZipfianDistribution zd;
	private final int limit;
	private int count = 0;

	public ZipfianIntGenerator(Random rand, int limit, int populationSize,
			double exponent) {
		zd = new ZipfianDistribution(populationSize, exponent);
		zd.setRandom(rand);
		this.limit = limit;
	}

	public ZipfianIntGenerator(int limit) {
		this(new Random(), limit, DEFAULT_POPULATION_SIZE, DEFAULT_EXPONENT);
	}

	public ZipfianIntGenerator() {
		this(new Random(), NO_LIMIT, DEFAULT_POPULATION_SIZE, DEFAULT_EXPONENT);
	}

	@Override
	public boolean hasNext() {
		return limit == NO_LIMIT || count < limit;
	}

	@Override
	public int nextInt() {
		if (!hasNext())
			throw new NoSuchElementException();
		++count;
		return zd.random();
	}

}