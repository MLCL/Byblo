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
package uk.ac.susx.mlcl.lib.collect;

import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.ints.*;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.susx.mlcl.lib.MemoryUsage;
import uk.ac.susx.mlcl.lib.MiscUtil;
import uk.ac.susx.mlcl.lib.UniformIntGenerator;
import uk.ac.susx.mlcl.testing.AbstractTest;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Ignore
public class IntSetsTest extends AbstractTest {

    @SuppressWarnings("unchecked")
    private static final Class<? extends IntSet>[] INT_SET_IMPLEMENTATIONS = new Class[]{
            IntBitSet.class,//
            IntOpenHashSet.class,//
            IntArraySet.class,//
            IntRBTreeSet.class, //
            IntAVLTreeSet.class,//
            IntLinkedOpenHashSet.class,//
            IntOpenHashBigSet.class};

    @Test
    public void compareInsertPerformance() throws InstantiationException,
            IllegalAccessException {
        System.out.println("compareInsertPerformance()");
        int repeats = 10;
        int dataSize = 100000;

        int minValue = 1;
        int maxValue = 8000000;
        //
//        int populationSize = Integer.MAX_VALUE / 32;
//        double exponent = 2.0;
        final Stopwatch sw = new Stopwatch();

        double[][] times = new double[INT_SET_IMPLEMENTATIONS.length][repeats];
        double[][] mem = new double[INT_SET_IMPLEMENTATIONS.length][repeats];

        for (int r = 0; r < repeats; r++) {

            // IntIterator gen = new ZipfianIntGenerator(newRandom(), dataSize,
            // populationSize,
            // exponent);

            IntIterator gen = new UniformIntGenerator(newRandom(), dataSize,
                    minValue, maxValue);

            final int[] data = new IntOpenHashSet(gen).toIntArray();

            IntSet[] sets = new IntSet[INT_SET_IMPLEMENTATIONS.length];

            System.out.printf("Repeat %d of %d.%n", r, repeats);
            for (int i = 0; i < INT_SET_IMPLEMENTATIONS.length; i++) {
                final IntSet intSet = INT_SET_IMPLEMENTATIONS[i].newInstance();

                sw.reset();
                sw.start();
                for (int aData : data) {
                    intSet.add(aData);
                }
                sw.stop();
                times[i][r] = sw.elapsedMillis();

                attemptToTrim(intSet);

                sets[i] = new IntOpenHashSet(intSet);

                MemoryUsage mu = new MemoryUsage();
                mu.add(intSet);
                mem[i][r] = mu.getInstanceSizeBytes();

                System.out.printf("%20s %6.2f seconds    %s%n",
                        INT_SET_IMPLEMENTATIONS[i].getSimpleName(),
                        sw.elapsedMillis() / 1000d,
                        MiscUtil.humanReadableBytes(mu.getInstanceSizeBytes()));
            }

            for (int i = 0; i < sets.length - 1; i++) {
                assertEquals(sets[i], sets[i + 1]);
            }
        }
        System.out.printf("Results:%n");
        for (int i = 0; i < INT_SET_IMPLEMENTATIONS.length; i++) {

            double avTime = ArrayMath.mean(times[i]) / 1000.0;
            double seTime = ArrayMath.sampleStddev(times[i]) / 1000.0;

            long avMem = (long) ArrayMath.mean(mem[i]);
            long seMem = (long) ArrayMath.sampleStddev(mem[i]);

            System.out.printf("%20s %6.3f ~%-6.3f seconds    %-8s ~%-8s %n",
                    INT_SET_IMPLEMENTATIONS[i].getSimpleName(), avTime, seTime,
                    MiscUtil.humanReadableBytes(avMem),
                    MiscUtil.humanReadableBytes(seMem));

        }

    }

    private static void attemptToTrim(Object o) {
        try {
            o.getClass().getMethod("trim").invoke(o);
        } catch (IllegalAccessException e) {
            // fail
        } catch (InvocationTargetException e) {
            // fail
        } catch (NoSuchMethodException e) {
            // fail
        }

    }

}
