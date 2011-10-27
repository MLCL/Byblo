/*
 * Copyright (c) 2010-2011, University of Sussex
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

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A collection of static utility functions to convert between sparse vectors
 * and other formats.
 *
 * TODO: Should be replaced by a mixture of IOFactory methods, and alternative
 *          constructors to Sparse*Vector classes. (Issue #41)
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class SparseVectors {

    private SparseVectors() {
    }

    public static SparseDoubleVector toDoubleVector(double[] arr) {
        if (arr == null) {
            throw new NullPointerException();
        }

        SparseDoubleVector vec = new SparseDoubleVector(arr.length);
        for (int i = 0; i < arr.length; i++) {
            vec.set(i, arr[i]);
        }
        vec.compact();
        return vec;
    }

    public static SparseDoubleVector toDoubleVector(Int2DoubleMap map, int cardinality) {
        if (map == null) {
            throw new NullPointerException();
        }
        if (cardinality < 0) {
            throw new IllegalArgumentException();
        }

        List<Int2DoubleMap.Entry> entries = new ArrayList<Int2DoubleMap.Entry>(map.int2DoubleEntrySet());
        Collections.sort(entries, new Comparator<Int2DoubleMap.Entry>() {

            @Override
            public final int compare(final Entry t, final Entry t1) {
                return t.getIntKey() - t1.getIntKey();
            }
        });

        int[] keys = new int[entries.size()];
        double[] values = new double[entries.size()];

        for (int i = 0; i < entries.size(); i++) {
            keys[i] = entries.get(i).getIntKey();
            values[i] = entries.get(i).getDoubleValue();
        }

        SparseDoubleVector vec = new SparseDoubleVector(keys, values, cardinality, keys.length);
        vec.compact();
        return vec;
    }
//

//    public static SparseDoubleVector toDoubleVector(TIntDoubleMap map, int cardinality) {
//        if (map == null) {
//            throw new NullPointerException();
//        }
//        if (cardinality < 0) {
//            throw new IllegalArgumentException();
//        }
//
//        SparseDoubleVector vec = new SparseDoubleVector(cardinality);
//        TIntDoubleIterator it = map.iterator();
//        while (it.hasNext()) {
//            it.advance();
//            vec.set(it.key(), it.value());
//        }
//        vec.compact();
//        return vec;
//    }
//
//    public static SparseDoubleVector toDoubleVector(TIntIntMap map, int size) {
//        if (map == null) {
//            throw new NullPointerException();
//        }
//        if (size < 0) {
//            throw new IllegalArgumentException();
//        }
//
//        SparseDoubleVector vec = new SparseDoubleVector(size);
//        TIntIntIterator it = map.iterator();
//        while (it.hasNext()) {
//            it.advance();
//            vec.set(it.key(), it.value());
//        }
//        vec.compact();
//        return vec;
//    }
    public static SparseDoubleVector toDoubleVector(String chars, int offset,
            int length, int vecsize) {
        SparseDoubleVector vec = new SparseDoubleVector(vecsize);
        int i = offset;
        final int end = offset + length;
        while (i < end) {
            int j = i;
            while (j < end && chars.charAt(j) != ':') {
                j++;
            }
            int key = 0;
            try {
                key = Integer.parseInt(chars.substring(i, j));
            } catch (NumberFormatException e) {
                System.out.println("x");
            }
            i = j;

            if (chars.charAt(i) != ':') {
                throw new AssertionError(chars.charAt(i));
            }
            i++;

            j = i;
            while (j < end && chars.charAt(j) != ',') {
                j++;
            }
            double value = Double.parseDouble(chars.substring(i, j));
            i = j;

            vec.set(key, value);

            if (i < end && chars.charAt(i) == ',') {
                i++;
            }
        }
        vec.compact();
        return vec;
    }
}
