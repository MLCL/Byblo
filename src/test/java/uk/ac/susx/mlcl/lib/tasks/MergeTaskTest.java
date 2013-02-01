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
package uk.ac.susx.mlcl.lib.tasks;

import org.junit.Test;
import uk.ac.susx.mlcl.lib.io.ObjectIO;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.ObjectSource;

import java.io.Serializable;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class MergeTaskTest {


    @Test
    public void testSortTask() {

        int n = 1000;
        Random rand = new Random(0);
        List<Integer> in1 = new ArrayList<Integer>();
        List<Integer> in2 = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            in1.add(rand.nextInt(100));
            in2.add(rand.nextInt(100));
        }

        Collections.sort(in1);
        Collections.sort(in2);

        List<Integer> out = new ArrayList<Integer>();

        ObjectSource<Integer> src1 = ObjectIO.asSource(in1);
        ObjectSource<Integer> src2 = ObjectIO.asSource(in2);
        ObjectSink<Integer> sink = ObjectIO.asSink(out);

        Comparator<Integer> comparator = new IntegerNaturalOrderComparator();

        ObjectMergeTask<Integer> instance = new ObjectMergeTask<Integer>();
        instance.setSourceA(src1);
        instance.setSourceB(src2);
        instance.setSink(sink);
        instance.setComparator(comparator);

        instance.run();

        assertEquals(n * 2L, out.size());
        for (int i = 1; i < out.size(); i++)
            assertTrue(out.get(i - 1) <= out.get(i));
    }

    protected static final class IntegerNaturalOrderComparator
            implements Comparator<Integer>, Serializable {

        private static final long serialVersionUID = 1L;

        IntegerNaturalOrderComparator() {
        }

        @Override
        public int compare(final Integer o1, final Integer o2) {
            return o1 - o2;
        }
    }
}
