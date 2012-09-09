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

public class UniformIntGenerator extends AbstractIntIterator {
    public static final int NO_LIMIT = -1;
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = Integer.MAX_VALUE;
    private final Random rand;
    private final int limit;
    private final int minValue;
    private final int maxValue;
    private int count = 0;

    public UniformIntGenerator(Random rand, int limit, int minValue,
                               int maxValue) {
        this.rand = rand;
        this.limit = limit;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public UniformIntGenerator(int limit, int maxValue) {
        this(new Random(), limit, DEFAULT_MIN_VALUE, maxValue);
    }

    public UniformIntGenerator(int limit) {
        this(new Random(), limit, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    public UniformIntGenerator() {
        this(new Random(), NO_LIMIT, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
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
        return minValue + rand.nextInt(maxValue - minValue);
    }

}