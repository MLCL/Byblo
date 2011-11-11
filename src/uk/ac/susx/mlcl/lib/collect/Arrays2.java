/*
 * Copyright (c) 2010-2011, MLCL, University of Sussex
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

import java.util.Comparator;
import java.util.Arrays;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Checks;

/**
 * Static utility class for manipulating arrays.
 * <p>
 * This class contains three distrinct types of operation, that take a different
 * view on the type of array being operated on, these being: Symbolic, partially
 * ordered, and arithmetic. Symbolic operations make no assumption about the
 * nature of the array elements, and so can operate on absolutely any data. 
 * Examples of symbolic operations include copy, reverse, and contains.
 * Partially ordered operations require the data to have some ordering, either
 * natural or via a Comparator. Examples of a partially ordered include sort,
 * min, and max. Arithmetic operation expect the data to be numeric such that
 * they can be added and multiplied.
 * </p><p>
 * TODO: Due the distinct divition in types of operation performed here, and the
 * extreme quantity of methods, it may be advisable to split this class in two:
 * One for symbolic/ordered operations, and the other for arithmetic operations.
 * </p>
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@SuppressWarnings("unchecked")
public final class Arrays2 {

    private Arrays2() {}

    private static final class Lazy {
        private static final Random RND = new Random();
        private Lazy() {}
    }

    public static int countEQ(boolean[] logical, boolean value) {
        int n = 0;
        for (int i = 0; i < logical.length; i++)
            n += logical[i] == value ? 1 : 0;
        return n;
    }

    public static int[] find(boolean[] logical) {
        int[] result = new int[countEQ(logical, true)];
        for (int i = 0, j = 0; i < logical.length; i++)
            if (logical[i])
                result[j++] = i;
        return result;
    }

    public static boolean epsilonEquals(double[] a, double[] a2, double epsilon) {
        if (a == a2)
            return true;
        if (a == null || a2 == null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i = 0; i < length; i++) {
            if (Math.abs(a[i] - a2[i]) > epsilon)
                return false;
        }

        return true;
    }

    public static int[] randPerm(int start, int end) {
        int[] arr = range(start, 1, end);
        for (int i = arr.length - 1; i > 0; i--) {
            int j = Lazy.RND.nextInt(i + 1);
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
        return arr;
    }




    /* 
     * ==================
     *  Type: boolean
     * ==================
     */

    public static boolean[] copyOf(final boolean[] src) {
        return copyOf(src, 0, src.length);
    }

    public static boolean[] copyOf(final boolean[] src, final int offset) {
        return copyOf(src, offset, src.length - offset);
    }

    public static boolean[] copyOf(final boolean[] src, final int offset, final int len) {
        boolean[] dst = new boolean[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    public static boolean[] cat(final boolean[]... arrs) {
        int n = 0;
        for (int i = 0; i < arrs.length; i++)
            n += arrs[i].length;
        boolean[] result = new boolean[n];
        int offset = 0;
        for (int i = 0; i < arrs.length; i++) {
            System.arraycopy(arrs[i], 0, result, offset, arrs[i].length);
            offset += arrs[i].length;
        }
        return result;
    }

    public static int firstIndexOf(final boolean[] arr, final boolean val) {
        return firstIndexOf(arr, val, 0, arr.length);
    }

    public static int firstIndexOf(final boolean[] arr, final boolean val, int fromIndex) {
        return firstIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int firstIndexOf(final boolean[] arr, final boolean val, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static int lastIndexOf(final boolean[] arr, final boolean val) {
        return lastIndexOf(arr, val, 0, arr.length);
    }

    public static int lastIndexOf(final boolean[] arr, final boolean val, int fromIndex) {
        return lastIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int lastIndexOf(final boolean[] arr, final boolean val, int fromIndex, int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static boolean contains(final boolean[] arr, final boolean val) {
        return firstIndexOf(arr, val) != -1;
    }

    public static boolean contains(final boolean[] arr, final boolean val, int fromIndex) {
        return firstIndexOf(arr, val, fromIndex) != -1;
    }

    public static boolean contains(final boolean[] arr, final boolean val, int fromIndex, int toIndex) {
        return firstIndexOf(arr, val, fromIndex, toIndex) != -1;
    }

    public static boolean[] unique(final boolean... arr) {
        // This is way to slow O(n^2) - can be done in 
        // O(n log n) by sorting in input first.
        boolean[] result = new boolean[arr.length];
        int j = 0;
        for (int i = 0; i < arr.length; i++) {
            if (!contains(result, arr[i])) {
                result[j] = arr[i];
                j++;
            }
        }
        return copyOf(result, 0, j);
    }

    public static void reverse(final boolean[] a) {
        reverse(a, 0, a.length);
    }

    public static void reverse(final boolean[] a, final int fromIndex) {
        reverse(a, fromIndex, a.length);
    }

    public static void reverse(final boolean[] a, final int fromIndex, final int toIndex) {
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            swap(a, i, j);
        }
    }

    public static void swap(final boolean[] a, final int i, final int j) {
        final boolean tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static boolean[] elementsOf(boolean[] arr, int[] idx) {
        boolean[] result = new boolean[idx.length];
        for (int i = 0, j = 0; i < idx.length; i++)
            result[i] = arr[idx[i]];
        return result;
    }

    public static boolean[] elementsOf(boolean[] arr, boolean[] logical) {
        return elementsOf(arr, find(logical));
    }
    
    public static boolean[] valuesEq(boolean[] arr, boolean val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] == val;
        return result;
    }

    public static boolean[] valuesNeq(boolean[] arr, boolean val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] != val;
        return result;
    }

    public static boolean[] ensureCapacity(boolean[] arr, int minCap) {
        int oldCap = arr.length;
        if (minCap > oldCap) {
            int newCap = (oldCap * 3) / 2 + 1;
            if (newCap < minCap)
                newCap = minCap;
            arr = copyOf(arr, 0, newCap);
        }
        return arr;
    }
 
    public static boolean mode(boolean[] arr) {
        Map<Boolean, Integer> map = new HashMap<Boolean, Integer>();
        for (int i = 0; i < arr.length; i++) {
            if (map.containsKey(arr[i]))
                map.put(arr[i], map.get(arr[i]) + 1);
            else
                map.put(arr[i], 1);
        }
        int maxCount = -1;
        Boolean maxValue = arr[0];
        for (Boolean key : map.keySet()) {
            int count = map.get(key);
            if (count > maxCount) {
                maxCount = count;
                maxValue = key;
            }
        }
        return maxValue;
    }
    


    public static void permute(final boolean[] a) {
        permute(a, 0, a.length);
    }

    public static void permute(final boolean[] a, final int toIndex) {
        permute(a, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in 
     * lexicographic order.
     * 
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isnt the most efficient solution.
     * 
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static void permute(final boolean[] a, final int fromIndex, final int toIndex) {
        if (toIndex - fromIndex < 2)
            return;

        // Find the largest index k such that a[k] < a[k + 1]. If no such index 
        // exists, the permutation is the last permutation.
        int k = toIndex - 2;
        while (k >= fromIndex && (a[k]==a[k + 1]?1:0)>=0)
            k--;

        if (k < fromIndex) {
            // Reached end of permutation cycle - reverse the
            // whole array and start over
            reverse(a, fromIndex, toIndex);

        } else {

            // Find the largest index l such that a[k] < a[l]. Since k + 1 is 
            // such an index, l is well defined and satisfies k < l.
            int l = toIndex - 1;
            while (l >= fromIndex && (a[k]==a[l]?1:0)>=0)
                l--;

            // Swap a[k] with a[l].
            swap(a, k, l);

            // Reverse the sequence from a[k + 1] up to and including the final 
            // element a[n].   
            reverse(a, k + 1, toIndex);
        }
    }

    public static boolean min(boolean[] values ) {
        return values[argmin(values , 0, values.length - 1)];
    }

    public static boolean min(boolean[] values , int fromIndex, int toIndex) {
        return values[argmin(values , fromIndex, toIndex)];
    }

    public static boolean max(boolean[] values ) {
        return values[argmax(values , 0, values.length - 1)];
    }

    public static boolean max(boolean[] values , int fromIndex, int toIndex) {
        return values[argmax(values , fromIndex, toIndex)];
    }

    public static int argmin(boolean[] values ) {
        return argmin(values , 0, values.length - 1);
    }

    public static int argmin(boolean[] values , int fromIndex, int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if ((values[i]==values[argmin]?1:0)<0)
                argmin = i;
        return argmin;
    }

    public static int argmax(boolean[] values ) {
        return argmax(values , 0, values.length - 1);
    }

    public static int argmax(boolean[] values , int fromIndex, int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if ((values[i]==values[argmax]?1:0)>0)
                argmax = i;
        return argmax;
    }

    public static int[] argminmax(boolean[] values , int fromIndex, int toIndex) {
        int argmax = fromIndex;
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++) {
            if ((values[i]==values[argmax]?1:0)>0)
                argmax = i;
            else if ((values[i]==values[argmin]?1:0)<0)
                argmin = i;
        }
        return new int[]{argmin, argmax};
    }

    public static boolean[] minmax(boolean[] values ) {
        return minmax(values , 0, values.length - 1);
    }

    public static boolean[] minmax(boolean[] values , int fromIndex, int toIndex) {
        int[] mm = argminmax(values , fromIndex, toIndex);
        return new boolean[]{values[mm[1]], values[mm[0]]};
    }

    public static void clamp(boolean[] src , boolean min, boolean max, boolean[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if ((src[i]== max?1:0) > 0)
                dst[i] = max;
            else if ((src[i]== min?1:0) < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static boolean[] clamp(boolean[] src , boolean min, boolean max) {
        boolean[] dst = new boolean[src.length];
        clamp(src, min, max, dst);
        return dst;
    }

    public static void clampMin(boolean[] src , boolean min, boolean[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if ((src[i]== min?1:0) < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static boolean[] clampMin(boolean[] src , boolean min) {
        boolean[] dst = new boolean[src.length];
        clampMin(src, min, dst);
        return dst;
    }

    public static void clampMax(boolean[] src , boolean max, boolean[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if ((src[i]== max?1:0) > 0)
                dst[i] = max;
            else
                dst[i] = src[i];
        }
    }

    public static boolean[] clampMax(boolean[] src , boolean max) {
        boolean[] dst = new boolean[src.length];
        clampMax(src, max, dst);
        return dst;
    }



    /* 
     * ==================
     *  Type: byte
     * ==================
     */

    public static byte[] copyOf(final byte[] src) {
        return copyOf(src, 0, src.length);
    }

    public static byte[] copyOf(final byte[] src, final int offset) {
        return copyOf(src, offset, src.length - offset);
    }

    public static byte[] copyOf(final byte[] src, final int offset, final int len) {
        byte[] dst = new byte[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    public static byte[] cat(final byte[]... arrs) {
        int n = 0;
        for (int i = 0; i < arrs.length; i++)
            n += arrs[i].length;
        byte[] result = new byte[n];
        int offset = 0;
        for (int i = 0; i < arrs.length; i++) {
            System.arraycopy(arrs[i], 0, result, offset, arrs[i].length);
            offset += arrs[i].length;
        }
        return result;
    }

    public static int firstIndexOf(final byte[] arr, final byte val) {
        return firstIndexOf(arr, val, 0, arr.length);
    }

    public static int firstIndexOf(final byte[] arr, final byte val, int fromIndex) {
        return firstIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int firstIndexOf(final byte[] arr, final byte val, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static int lastIndexOf(final byte[] arr, final byte val) {
        return lastIndexOf(arr, val, 0, arr.length);
    }

    public static int lastIndexOf(final byte[] arr, final byte val, int fromIndex) {
        return lastIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int lastIndexOf(final byte[] arr, final byte val, int fromIndex, int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static boolean contains(final byte[] arr, final byte val) {
        return firstIndexOf(arr, val) != -1;
    }

    public static boolean contains(final byte[] arr, final byte val, int fromIndex) {
        return firstIndexOf(arr, val, fromIndex) != -1;
    }

    public static boolean contains(final byte[] arr, final byte val, int fromIndex, int toIndex) {
        return firstIndexOf(arr, val, fromIndex, toIndex) != -1;
    }

    public static byte[] unique(final byte... arr) {
        // This is way to slow O(n^2) - can be done in 
        // O(n log n) by sorting in input first.
        byte[] result = new byte[arr.length];
        int j = 0;
        for (int i = 0; i < arr.length; i++) {
            if (!contains(result, arr[i])) {
                result[j] = arr[i];
                j++;
            }
        }
        return copyOf(result, 0, j);
    }

    public static void reverse(final byte[] a) {
        reverse(a, 0, a.length);
    }

    public static void reverse(final byte[] a, final int fromIndex) {
        reverse(a, fromIndex, a.length);
    }

    public static void reverse(final byte[] a, final int fromIndex, final int toIndex) {
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            swap(a, i, j);
        }
    }

    public static void swap(final byte[] a, final int i, final int j) {
        final byte tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static byte[] elementsOf(byte[] arr, int[] idx) {
        byte[] result = new byte[idx.length];
        for (int i = 0, j = 0; i < idx.length; i++)
            result[i] = arr[idx[i]];
        return result;
    }

    public static byte[] elementsOf(byte[] arr, boolean[] logical) {
        return elementsOf(arr, find(logical));
    }
    
    public static boolean[] valuesEq(byte[] arr, byte val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] == val;
        return result;
    }

    public static boolean[] valuesNeq(byte[] arr, byte val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] != val;
        return result;
    }

    public static byte[] ensureCapacity(byte[] arr, int minCap) {
        int oldCap = arr.length;
        if (minCap > oldCap) {
            int newCap = (oldCap * 3) / 2 + 1;
            if (newCap < minCap)
                newCap = minCap;
            arr = copyOf(arr, 0, newCap);
        }
        return arr;
    }
 
    public static byte mode(byte[] arr) {
        Map<Byte, Integer> map = new HashMap<Byte, Integer>();
        for (int i = 0; i < arr.length; i++) {
            if (map.containsKey(arr[i]))
                map.put(arr[i], map.get(arr[i]) + 1);
            else
                map.put(arr[i], 1);
        }
        int maxCount = -1;
        Byte maxValue = arr[0];
        for (Byte key : map.keySet()) {
            int count = map.get(key);
            if (count > maxCount) {
                maxCount = count;
                maxValue = key;
            }
        }
        return maxValue;
    }
    


    public static void permute(final byte[] a) {
        permute(a, 0, a.length);
    }

    public static void permute(final byte[] a, final int toIndex) {
        permute(a, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in 
     * lexicographic order.
     * 
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isnt the most efficient solution.
     * 
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static void permute(final byte[] a, final int fromIndex, final int toIndex) {
        if (toIndex - fromIndex < 2)
            return;

        // Find the largest index k such that a[k] < a[k + 1]. If no such index 
        // exists, the permutation is the last permutation.
        int k = toIndex - 2;
        while (k >= fromIndex && (byte)(a[k]-a[k + 1])>=0)
            k--;

        if (k < fromIndex) {
            // Reached end of permutation cycle - reverse the
            // whole array and start over
            reverse(a, fromIndex, toIndex);

        } else {

            // Find the largest index l such that a[k] < a[l]. Since k + 1 is 
            // such an index, l is well defined and satisfies k < l.
            int l = toIndex - 1;
            while (l >= fromIndex && (byte)(a[k]-a[l])>=0)
                l--;

            // Swap a[k] with a[l].
            swap(a, k, l);

            // Reverse the sequence from a[k + 1] up to and including the final 
            // element a[n].   
            reverse(a, k + 1, toIndex);
        }
    }

    public static byte min(byte[] values ) {
        return values[argmin(values , 0, values.length - 1)];
    }

    public static byte min(byte[] values , int fromIndex, int toIndex) {
        return values[argmin(values , fromIndex, toIndex)];
    }

    public static byte max(byte[] values ) {
        return values[argmax(values , 0, values.length - 1)];
    }

    public static byte max(byte[] values , int fromIndex, int toIndex) {
        return values[argmax(values , fromIndex, toIndex)];
    }

    public static int argmin(byte[] values ) {
        return argmin(values , 0, values.length - 1);
    }

    public static int argmin(byte[] values , int fromIndex, int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if ((byte)(values[i]-values[argmin])<0)
                argmin = i;
        return argmin;
    }

    public static int argmax(byte[] values ) {
        return argmax(values , 0, values.length - 1);
    }

    public static int argmax(byte[] values , int fromIndex, int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if ((byte)(values[i]-values[argmax])>0)
                argmax = i;
        return argmax;
    }

    public static int[] argminmax(byte[] values , int fromIndex, int toIndex) {
        int argmax = fromIndex;
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++) {
            if ((byte)(values[i]-values[argmax])>0)
                argmax = i;
            else if ((byte)(values[i]-values[argmin])<0)
                argmin = i;
        }
        return new int[]{argmin, argmax};
    }

    public static byte[] minmax(byte[] values ) {
        return minmax(values , 0, values.length - 1);
    }

    public static byte[] minmax(byte[] values , int fromIndex, int toIndex) {
        int[] mm = argminmax(values , fromIndex, toIndex);
        return new byte[]{values[mm[1]], values[mm[0]]};
    }

    public static void clamp(byte[] src , byte min, byte max, byte[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if ((byte)(src[i]- max) > 0)
                dst[i] = max;
            else if ((byte)(src[i]- min) < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static byte[] clamp(byte[] src , byte min, byte max) {
        byte[] dst = new byte[src.length];
        clamp(src, min, max, dst);
        return dst;
    }

    public static void clampMin(byte[] src , byte min, byte[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if ((byte)(src[i]- min) < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static byte[] clampMin(byte[] src , byte min) {
        byte[] dst = new byte[src.length];
        clampMin(src, min, dst);
        return dst;
    }

    public static void clampMax(byte[] src , byte max, byte[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if ((byte)(src[i]- max) > 0)
                dst[i] = max;
            else
                dst[i] = src[i];
        }
    }

    public static byte[] clampMax(byte[] src , byte max) {
        byte[] dst = new byte[src.length];
        clampMax(src, max, dst);
        return dst;
    }



    public static byte[] byteZeros(int n) {
        return new byte[n];
    }

    public static byte[] byteOnes(int n) {
        byte[] result = new byte[n];
        Arrays.fill(result, (byte)1);
        return result;
    }

    public static byte range(byte[] values) {
        return range(values, 0, values.length - 1);
    }

    public static byte range(byte[] values, int fromIndex) {
        return range(values, fromIndex, values.length - 1);
    }

    public static byte range(byte[] values, int fromIndex, int toIndex) {
        int[] mm = argminmax(values, fromIndex, toIndex);
        return (byte)(values[mm[1]]- values[mm[0]]);
    }


    public static byte[] range(byte start, byte step, byte end) {
        byte[] arr = new byte[(int)((end - start) / step)];
        for (int i = 0; i < arr.length; i++)
            arr[i] = (byte)(start + (i * step));
        return arr;
    }

    public static byte sum(byte[] arr) {
        return sum(arr, 0, arr.length);
    }

    public static byte sum(byte[] arr, int fromIndex) {
        return sum(arr, fromIndex, arr.length);
    }

    public static byte sum(byte[] arr, int fromIndex, int toIndex) {
        byte sum = (byte)0;
        for (int i = fromIndex; i < toIndex; i++)
            sum += arr[i];
        return sum;
    }

    public static byte product(byte[] arr) {
        return product(arr, 0, arr.length);
    }

    public static byte product(byte[] arr, int fromIndex) {
        return product(arr, fromIndex, arr.length);
    }

    public static byte product(byte[] arr, int fromIndex, int toIndex) {
        byte prod = (byte)1;
        for (int i = fromIndex; i < toIndex; i++)
            prod += arr[i];
        return prod;
    }

    public static void mul(byte[] src, byte scalar, byte[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (byte)(src[i] * scalar);
    }

    public static void mul(byte[] src1, byte[] src2, byte[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (byte)(src1[i] * src2[i]);
    }

    public static byte[] mul(byte[] src, byte scalar) {
        byte[] dst = new byte[src.length];
        mul(src, scalar, dst);
        return dst;
    }

    public static byte[] mul(byte[] src1, byte[] src2) {
        byte[] dst = new byte[src1.length];
        mul(src1, src2, dst);
        return dst;
    }

    public static void div(byte[] src, byte scalar, byte[] dst) {
        mul(src, (byte)(1.0 / scalar), dst);
    }

    public static void div(byte scalar, byte[] src, byte[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (byte)(scalar / src[i]);
    }

    public static void div(byte[] src1, byte[] src2, byte[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (byte)(src1[i] / src2[i]);
    }

    public static byte[] div(byte[] src, byte scalar) {
        return mul(src, (byte)(1.0 / scalar));
    }

    public static byte[] div(byte scalar, byte[] src) {
        byte[] dst = new byte[src.length];
        div(scalar, src, dst);
        return dst;
    }

    public static byte[] div(byte[] src1, byte[] src2) {
        byte[] dst = new byte[src1.length];
        div(src1, src2, dst);
        return dst;
    }

    public static void add(byte[] src, byte scalar, byte[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (byte)(src[i] + scalar);
    }

    public static void add(byte[] src1, byte[] src2, byte[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (byte)(src1[i] + src2[i]);
    }

    public static byte[] add(byte[] src, byte scalar) {
        byte[] dst = new byte[src.length];
        add(src, scalar, dst);
        return dst;
    }

    public static byte[] add(byte[] src1, byte[] src2) {
        byte[] dst = new byte[src1.length];
        add(src1, src2, dst);
        return dst;
    }

    public static void sub(byte[] src, byte scalar, byte[] dst) {
        add(src, (byte)(-scalar), dst);
    }

    public static void sub(byte[] src1, byte[] src2, byte[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (byte)(src1[i] - src2[i]);
    }

    public static byte[] sub(byte[] src, byte scalar) {
        return add(src, (byte)(-scalar));
    }

    public static byte[] sub(byte[] src1, byte[] src2) {
        byte[] dst = new byte[src1.length];
        sub(src1, src2, dst);
        return dst;
    }

    public static void mod(byte[] src, byte scalar, byte[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (byte)(src[i] % scalar);
    }

    public static void mod(byte scalar, byte[] src, byte[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (byte)(scalar % src[i]);
    }

    public static void mod(byte[] src1, byte[] src2, byte[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (byte)(src1[i] % src2[i]);
    }

    public static byte[] mod(byte[] src, byte scalar) {
        byte[] dst = new byte[src.length];
        mod(src, scalar, dst);
        return dst;
    }

    public static byte[] mod(byte scalar, byte[] src) {
        byte[] dst = new byte[src.length];
        mod(scalar, src, dst);
        return dst;
    }

    public static byte[] mod(byte[] src1, byte[] src2) {
        byte[] dst = new byte[src1.length];
        mod(src1, src2, dst);
        return dst;
    }

    public static void negate(byte[] src, byte[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (byte)(-src[i]);
    }

    public static byte[] negate(byte[] src) {
        byte[] dst = new byte[src.length];
        negate(src, dst);
        return dst;
    }

    public static void pow(byte[] src, byte power, byte[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (byte)Math.pow(src[i], power);
    }

    public static void pow(byte[] src1, byte[] src2, byte[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (byte)Math.pow(src1[i], src2[i]);
    }

    public static byte[] pow(byte[] src, byte scalar) {
        byte[] dst = new byte[src.length];
        pow(src, scalar, dst);
        return dst;
    }

    public static byte[] pow(byte[] src1, byte[] src2) {
        byte[] dst = new byte[src1.length];
        pow(src1, src2, dst);
        return dst;
    }

    public static void squared(byte[] src, byte[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (byte)(src[i] * src[i]);
    }

    public static byte[] squared(byte[] src) {
        byte[] dst = new byte[src.length];
        squared(src, dst);
        return dst;
    }

    public static void cubed(byte[] src, byte[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (byte)(src[i] * src[i] * src[i]);
    }

    public static byte[] cubed(byte[] src) {
        byte[] dst = new byte[src.length];
        cubed(src, dst);
        return dst;
    }

    public static void sqrt(byte[] src, byte[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (byte)(Math.sqrt(src[i]));
    }

    public static byte[] sqrt(byte[] src) {
        byte[] dst = new byte[src.length];
        sqrt(src, dst);
        return dst;
    }

    public static void abs(byte[] src, byte[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (byte)(Math.abs(src[i]));
    }

    public static byte[] abs(byte[] src) {
        byte[] dst = new byte[src.length];
        abs(src, dst);
        return dst;
    }


    public static byte mean(byte[] values) {
        return (byte)(sum(values) / values.length);
    }
    
    /**
     * Return the median average of the values passed as argument.
     *
     * @param vals 1 or more values
     * @return the media value of the values
     * @throws IllegalArgumentException if no values are passed
     * @throws NullPointerException if a null object of type {@code double[]}
     *          array is passed
     */
    public static byte median(byte[] values) {
        if (values.length == 1)
            return values[0];
        byte[] sorted = copyOf(values, values.length);
        Arrays.sort(sorted);
        final int i = sorted.length / 2;
        return (sorted.length % 2 == 0)
                ? (byte)((sorted[i - 1] + sorted[i]) / 2)
                : sorted[i];
    }

    public static void normalise(byte[] src, byte min, byte max,
                                 byte[] dst) {
        Checks.checkEqual(src.length, dst.length);
        byte smin = min(src);
        byte srange = (byte)(max(src) - smin);
        byte drange = (byte)(max - min);
        for (int i = 0; i < src.length; i++)
            dst[i] = (byte)(((src[i] - smin) / srange) * drange + min);
    }

    public static void normalise(byte[] src, byte[] dst) {
        normalise(src, (byte)0, (byte)1, dst);
    }

    public static byte[] normalise(byte[] src, byte min, byte max) {
        byte[] dst = new byte[src.length];
        normalise(src, min, max, dst);
        return dst;
    }

    public static byte[] normalise(byte[] src) {
        return normalise(src, (byte)0, (byte)1);
    }



    public static String toHexString(final byte[] arr) {
        final StringBuilder sbuf = new StringBuilder();
        sbuf.append('[');
        if (arr.length > 0)
            sbuf.append(Integer.toHexString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            sbuf.append(' ');
            sbuf.append(Integer.toHexString(arr[i]));
        }
        sbuf.append(']');
        return sbuf.toString();
    }



    /* 
     * ==================
     *  Type: char
     * ==================
     */

    public static char[] copyOf(final char[] src) {
        return copyOf(src, 0, src.length);
    }

    public static char[] copyOf(final char[] src, final int offset) {
        return copyOf(src, offset, src.length - offset);
    }

    public static char[] copyOf(final char[] src, final int offset, final int len) {
        char[] dst = new char[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    public static char[] cat(final char[]... arrs) {
        int n = 0;
        for (int i = 0; i < arrs.length; i++)
            n += arrs[i].length;
        char[] result = new char[n];
        int offset = 0;
        for (int i = 0; i < arrs.length; i++) {
            System.arraycopy(arrs[i], 0, result, offset, arrs[i].length);
            offset += arrs[i].length;
        }
        return result;
    }

    public static int firstIndexOf(final char[] arr, final char val) {
        return firstIndexOf(arr, val, 0, arr.length);
    }

    public static int firstIndexOf(final char[] arr, final char val, int fromIndex) {
        return firstIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int firstIndexOf(final char[] arr, final char val, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static int lastIndexOf(final char[] arr, final char val) {
        return lastIndexOf(arr, val, 0, arr.length);
    }

    public static int lastIndexOf(final char[] arr, final char val, int fromIndex) {
        return lastIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int lastIndexOf(final char[] arr, final char val, int fromIndex, int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static boolean contains(final char[] arr, final char val) {
        return firstIndexOf(arr, val) != -1;
    }

    public static boolean contains(final char[] arr, final char val, int fromIndex) {
        return firstIndexOf(arr, val, fromIndex) != -1;
    }

    public static boolean contains(final char[] arr, final char val, int fromIndex, int toIndex) {
        return firstIndexOf(arr, val, fromIndex, toIndex) != -1;
    }

    public static char[] unique(final char... arr) {
        // This is way to slow O(n^2) - can be done in 
        // O(n log n) by sorting in input first.
        char[] result = new char[arr.length];
        int j = 0;
        for (int i = 0; i < arr.length; i++) {
            if (!contains(result, arr[i])) {
                result[j] = arr[i];
                j++;
            }
        }
        return copyOf(result, 0, j);
    }

    public static void reverse(final char[] a) {
        reverse(a, 0, a.length);
    }

    public static void reverse(final char[] a, final int fromIndex) {
        reverse(a, fromIndex, a.length);
    }

    public static void reverse(final char[] a, final int fromIndex, final int toIndex) {
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            swap(a, i, j);
        }
    }

    public static void swap(final char[] a, final int i, final int j) {
        final char tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static char[] elementsOf(char[] arr, int[] idx) {
        char[] result = new char[idx.length];
        for (int i = 0, j = 0; i < idx.length; i++)
            result[i] = arr[idx[i]];
        return result;
    }

    public static char[] elementsOf(char[] arr, boolean[] logical) {
        return elementsOf(arr, find(logical));
    }
    
    public static boolean[] valuesEq(char[] arr, char val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] == val;
        return result;
    }

    public static boolean[] valuesNeq(char[] arr, char val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] != val;
        return result;
    }

    public static char[] ensureCapacity(char[] arr, int minCap) {
        int oldCap = arr.length;
        if (minCap > oldCap) {
            int newCap = (oldCap * 3) / 2 + 1;
            if (newCap < minCap)
                newCap = minCap;
            arr = copyOf(arr, 0, newCap);
        }
        return arr;
    }
 
    public static char mode(char[] arr) {
        Map<Character, Integer> map = new HashMap<Character, Integer>();
        for (int i = 0; i < arr.length; i++) {
            if (map.containsKey(arr[i]))
                map.put(arr[i], map.get(arr[i]) + 1);
            else
                map.put(arr[i], 1);
        }
        int maxCount = -1;
        Character maxValue = arr[0];
        for (Character key : map.keySet()) {
            int count = map.get(key);
            if (count > maxCount) {
                maxCount = count;
                maxValue = key;
            }
        }
        return maxValue;
    }
    


    public static void permute(final char[] a) {
        permute(a, 0, a.length);
    }

    public static void permute(final char[] a, final int toIndex) {
        permute(a, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in 
     * lexicographic order.
     * 
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isnt the most efficient solution.
     * 
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static void permute(final char[] a, final int fromIndex, final int toIndex) {
        if (toIndex - fromIndex < 2)
            return;

        // Find the largest index k such that a[k] < a[k + 1]. If no such index 
        // exists, the permutation is the last permutation.
        int k = toIndex - 2;
        while (k >= fromIndex && (char)(a[k]-a[k + 1])>=0)
            k--;

        if (k < fromIndex) {
            // Reached end of permutation cycle - reverse the
            // whole array and start over
            reverse(a, fromIndex, toIndex);

        } else {

            // Find the largest index l such that a[k] < a[l]. Since k + 1 is 
            // such an index, l is well defined and satisfies k < l.
            int l = toIndex - 1;
            while (l >= fromIndex && (char)(a[k]-a[l])>=0)
                l--;

            // Swap a[k] with a[l].
            swap(a, k, l);

            // Reverse the sequence from a[k + 1] up to and including the final 
            // element a[n].   
            reverse(a, k + 1, toIndex);
        }
    }

    public static char min(char[] values ) {
        return values[argmin(values , 0, values.length - 1)];
    }

    public static char min(char[] values , int fromIndex, int toIndex) {
        return values[argmin(values , fromIndex, toIndex)];
    }

    public static char max(char[] values ) {
        return values[argmax(values , 0, values.length - 1)];
    }

    public static char max(char[] values , int fromIndex, int toIndex) {
        return values[argmax(values , fromIndex, toIndex)];
    }

    public static int argmin(char[] values ) {
        return argmin(values , 0, values.length - 1);
    }

    public static int argmin(char[] values , int fromIndex, int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if ((char)(values[i]-values[argmin])<0)
                argmin = i;
        return argmin;
    }

    public static int argmax(char[] values ) {
        return argmax(values , 0, values.length - 1);
    }

    public static int argmax(char[] values , int fromIndex, int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if ((char)(values[i]-values[argmax])>0)
                argmax = i;
        return argmax;
    }

    public static int[] argminmax(char[] values , int fromIndex, int toIndex) {
        int argmax = fromIndex;
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++) {
            if ((char)(values[i]-values[argmax])>0)
                argmax = i;
            else if ((char)(values[i]-values[argmin])<0)
                argmin = i;
        }
        return new int[]{argmin, argmax};
    }

    public static char[] minmax(char[] values ) {
        return minmax(values , 0, values.length - 1);
    }

    public static char[] minmax(char[] values , int fromIndex, int toIndex) {
        int[] mm = argminmax(values , fromIndex, toIndex);
        return new char[]{values[mm[1]], values[mm[0]]};
    }

    public static void clamp(char[] src , char min, char max, char[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if ((char)(src[i]- max) > 0)
                dst[i] = max;
            else if ((char)(src[i]- min) < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static char[] clamp(char[] src , char min, char max) {
        char[] dst = new char[src.length];
        clamp(src, min, max, dst);
        return dst;
    }

    public static void clampMin(char[] src , char min, char[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if ((char)(src[i]- min) < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static char[] clampMin(char[] src , char min) {
        char[] dst = new char[src.length];
        clampMin(src, min, dst);
        return dst;
    }

    public static void clampMax(char[] src , char max, char[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if ((char)(src[i]- max) > 0)
                dst[i] = max;
            else
                dst[i] = src[i];
        }
    }

    public static char[] clampMax(char[] src , char max) {
        char[] dst = new char[src.length];
        clampMax(src, max, dst);
        return dst;
    }



    public static char[] charZeros(int n) {
        return new char[n];
    }

    public static char[] charOnes(int n) {
        char[] result = new char[n];
        Arrays.fill(result, (char)1);
        return result;
    }

    public static char range(char[] values) {
        return range(values, 0, values.length - 1);
    }

    public static char range(char[] values, int fromIndex) {
        return range(values, fromIndex, values.length - 1);
    }

    public static char range(char[] values, int fromIndex, int toIndex) {
        int[] mm = argminmax(values, fromIndex, toIndex);
        return (char)(values[mm[1]]- values[mm[0]]);
    }


    public static char[] range(char start, char step, char end) {
        char[] arr = new char[(int)((end - start) / step)];
        for (int i = 0; i < arr.length; i++)
            arr[i] = (char)(start + (i * step));
        return arr;
    }

    public static char sum(char[] arr) {
        return sum(arr, 0, arr.length);
    }

    public static char sum(char[] arr, int fromIndex) {
        return sum(arr, fromIndex, arr.length);
    }

    public static char sum(char[] arr, int fromIndex, int toIndex) {
        char sum = (char)0;
        for (int i = fromIndex; i < toIndex; i++)
            sum += arr[i];
        return sum;
    }

    public static char product(char[] arr) {
        return product(arr, 0, arr.length);
    }

    public static char product(char[] arr, int fromIndex) {
        return product(arr, fromIndex, arr.length);
    }

    public static char product(char[] arr, int fromIndex, int toIndex) {
        char prod = (char)1;
        for (int i = fromIndex; i < toIndex; i++)
            prod += arr[i];
        return prod;
    }

    public static void mul(char[] src, char scalar, char[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (char)(src[i] * scalar);
    }

    public static void mul(char[] src1, char[] src2, char[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (char)(src1[i] * src2[i]);
    }

    public static char[] mul(char[] src, char scalar) {
        char[] dst = new char[src.length];
        mul(src, scalar, dst);
        return dst;
    }

    public static char[] mul(char[] src1, char[] src2) {
        char[] dst = new char[src1.length];
        mul(src1, src2, dst);
        return dst;
    }

    public static void div(char[] src, char scalar, char[] dst) {
        mul(src, (char)(1.0 / scalar), dst);
    }

    public static void div(char scalar, char[] src, char[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (char)(scalar / src[i]);
    }

    public static void div(char[] src1, char[] src2, char[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (char)(src1[i] / src2[i]);
    }

    public static char[] div(char[] src, char scalar) {
        return mul(src, (char)(1.0 / scalar));
    }

    public static char[] div(char scalar, char[] src) {
        char[] dst = new char[src.length];
        div(scalar, src, dst);
        return dst;
    }

    public static char[] div(char[] src1, char[] src2) {
        char[] dst = new char[src1.length];
        div(src1, src2, dst);
        return dst;
    }

    public static void add(char[] src, char scalar, char[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (char)(src[i] + scalar);
    }

    public static void add(char[] src1, char[] src2, char[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (char)(src1[i] + src2[i]);
    }

    public static char[] add(char[] src, char scalar) {
        char[] dst = new char[src.length];
        add(src, scalar, dst);
        return dst;
    }

    public static char[] add(char[] src1, char[] src2) {
        char[] dst = new char[src1.length];
        add(src1, src2, dst);
        return dst;
    }

    public static void sub(char[] src, char scalar, char[] dst) {
        add(src, (char)(-scalar), dst);
    }

    public static void sub(char[] src1, char[] src2, char[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (char)(src1[i] - src2[i]);
    }

    public static char[] sub(char[] src, char scalar) {
        return add(src, (char)(-scalar));
    }

    public static char[] sub(char[] src1, char[] src2) {
        char[] dst = new char[src1.length];
        sub(src1, src2, dst);
        return dst;
    }

    public static void mod(char[] src, char scalar, char[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (char)(src[i] % scalar);
    }

    public static void mod(char scalar, char[] src, char[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (char)(scalar % src[i]);
    }

    public static void mod(char[] src1, char[] src2, char[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (char)(src1[i] % src2[i]);
    }

    public static char[] mod(char[] src, char scalar) {
        char[] dst = new char[src.length];
        mod(src, scalar, dst);
        return dst;
    }

    public static char[] mod(char scalar, char[] src) {
        char[] dst = new char[src.length];
        mod(scalar, src, dst);
        return dst;
    }

    public static char[] mod(char[] src1, char[] src2) {
        char[] dst = new char[src1.length];
        mod(src1, src2, dst);
        return dst;
    }

    public static void negate(char[] src, char[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (char)(-src[i]);
    }

    public static char[] negate(char[] src) {
        char[] dst = new char[src.length];
        negate(src, dst);
        return dst;
    }

    public static void pow(char[] src, char power, char[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (char)Math.pow(src[i], power);
    }

    public static void pow(char[] src1, char[] src2, char[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (char)Math.pow(src1[i], src2[i]);
    }

    public static char[] pow(char[] src, char scalar) {
        char[] dst = new char[src.length];
        pow(src, scalar, dst);
        return dst;
    }

    public static char[] pow(char[] src1, char[] src2) {
        char[] dst = new char[src1.length];
        pow(src1, src2, dst);
        return dst;
    }

    public static void squared(char[] src, char[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (char)(src[i] * src[i]);
    }

    public static char[] squared(char[] src) {
        char[] dst = new char[src.length];
        squared(src, dst);
        return dst;
    }

    public static void cubed(char[] src, char[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (char)(src[i] * src[i] * src[i]);
    }

    public static char[] cubed(char[] src) {
        char[] dst = new char[src.length];
        cubed(src, dst);
        return dst;
    }

    public static void sqrt(char[] src, char[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (char)(Math.sqrt(src[i]));
    }

    public static char[] sqrt(char[] src) {
        char[] dst = new char[src.length];
        sqrt(src, dst);
        return dst;
    }

    public static void abs(char[] src, char[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (char)(Math.abs(src[i]));
    }

    public static char[] abs(char[] src) {
        char[] dst = new char[src.length];
        abs(src, dst);
        return dst;
    }


    public static char mean(char[] values) {
        return (char)(sum(values) / values.length);
    }
    
    /**
     * Return the median average of the values passed as argument.
     *
     * @param vals 1 or more values
     * @return the media value of the values
     * @throws IllegalArgumentException if no values are passed
     * @throws NullPointerException if a null object of type {@code double[]}
     *          array is passed
     */
    public static char median(char[] values) {
        if (values.length == 1)
            return values[0];
        char[] sorted = copyOf(values, values.length);
        Arrays.sort(sorted);
        final int i = sorted.length / 2;
        return (sorted.length % 2 == 0)
                ? (char)((sorted[i - 1] + sorted[i]) / 2)
                : sorted[i];
    }

    public static void normalise(char[] src, char min, char max,
                                 char[] dst) {
        Checks.checkEqual(src.length, dst.length);
        char smin = min(src);
        char srange = (char)(max(src) - smin);
        char drange = (char)(max - min);
        for (int i = 0; i < src.length; i++)
            dst[i] = (char)(((src[i] - smin) / srange) * drange + min);
    }

    public static void normalise(char[] src, char[] dst) {
        normalise(src, (char)0, (char)1, dst);
    }

    public static char[] normalise(char[] src, char min, char max) {
        char[] dst = new char[src.length];
        normalise(src, min, max, dst);
        return dst;
    }

    public static char[] normalise(char[] src) {
        return normalise(src, (char)0, (char)1);
    }



    public static String toHexString(final char[] arr) {
        final StringBuilder sbuf = new StringBuilder();
        sbuf.append('[');
        if (arr.length > 0)
            sbuf.append(Integer.toHexString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            sbuf.append(' ');
            sbuf.append(Integer.toHexString(arr[i]));
        }
        sbuf.append(']');
        return sbuf.toString();
    }



    /* 
     * ==================
     *  Type: short
     * ==================
     */

    public static short[] copyOf(final short[] src) {
        return copyOf(src, 0, src.length);
    }

    public static short[] copyOf(final short[] src, final int offset) {
        return copyOf(src, offset, src.length - offset);
    }

    public static short[] copyOf(final short[] src, final int offset, final int len) {
        short[] dst = new short[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    public static short[] cat(final short[]... arrs) {
        int n = 0;
        for (int i = 0; i < arrs.length; i++)
            n += arrs[i].length;
        short[] result = new short[n];
        int offset = 0;
        for (int i = 0; i < arrs.length; i++) {
            System.arraycopy(arrs[i], 0, result, offset, arrs[i].length);
            offset += arrs[i].length;
        }
        return result;
    }

    public static int firstIndexOf(final short[] arr, final short val) {
        return firstIndexOf(arr, val, 0, arr.length);
    }

    public static int firstIndexOf(final short[] arr, final short val, int fromIndex) {
        return firstIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int firstIndexOf(final short[] arr, final short val, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static int lastIndexOf(final short[] arr, final short val) {
        return lastIndexOf(arr, val, 0, arr.length);
    }

    public static int lastIndexOf(final short[] arr, final short val, int fromIndex) {
        return lastIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int lastIndexOf(final short[] arr, final short val, int fromIndex, int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static boolean contains(final short[] arr, final short val) {
        return firstIndexOf(arr, val) != -1;
    }

    public static boolean contains(final short[] arr, final short val, int fromIndex) {
        return firstIndexOf(arr, val, fromIndex) != -1;
    }

    public static boolean contains(final short[] arr, final short val, int fromIndex, int toIndex) {
        return firstIndexOf(arr, val, fromIndex, toIndex) != -1;
    }

    public static short[] unique(final short... arr) {
        // This is way to slow O(n^2) - can be done in 
        // O(n log n) by sorting in input first.
        short[] result = new short[arr.length];
        int j = 0;
        for (int i = 0; i < arr.length; i++) {
            if (!contains(result, arr[i])) {
                result[j] = arr[i];
                j++;
            }
        }
        return copyOf(result, 0, j);
    }

    public static void reverse(final short[] a) {
        reverse(a, 0, a.length);
    }

    public static void reverse(final short[] a, final int fromIndex) {
        reverse(a, fromIndex, a.length);
    }

    public static void reverse(final short[] a, final int fromIndex, final int toIndex) {
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            swap(a, i, j);
        }
    }

    public static void swap(final short[] a, final int i, final int j) {
        final short tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static short[] elementsOf(short[] arr, int[] idx) {
        short[] result = new short[idx.length];
        for (int i = 0, j = 0; i < idx.length; i++)
            result[i] = arr[idx[i]];
        return result;
    }

    public static short[] elementsOf(short[] arr, boolean[] logical) {
        return elementsOf(arr, find(logical));
    }
    
    public static boolean[] valuesEq(short[] arr, short val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] == val;
        return result;
    }

    public static boolean[] valuesNeq(short[] arr, short val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] != val;
        return result;
    }

    public static short[] ensureCapacity(short[] arr, int minCap) {
        int oldCap = arr.length;
        if (minCap > oldCap) {
            int newCap = (oldCap * 3) / 2 + 1;
            if (newCap < minCap)
                newCap = minCap;
            arr = copyOf(arr, 0, newCap);
        }
        return arr;
    }
 
    public static short mode(short[] arr) {
        Map<Short, Integer> map = new HashMap<Short, Integer>();
        for (int i = 0; i < arr.length; i++) {
            if (map.containsKey(arr[i]))
                map.put(arr[i], map.get(arr[i]) + 1);
            else
                map.put(arr[i], 1);
        }
        int maxCount = -1;
        Short maxValue = arr[0];
        for (Short key : map.keySet()) {
            int count = map.get(key);
            if (count > maxCount) {
                maxCount = count;
                maxValue = key;
            }
        }
        return maxValue;
    }
    


    public static void permute(final short[] a) {
        permute(a, 0, a.length);
    }

    public static void permute(final short[] a, final int toIndex) {
        permute(a, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in 
     * lexicographic order.
     * 
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isnt the most efficient solution.
     * 
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static void permute(final short[] a, final int fromIndex, final int toIndex) {
        if (toIndex - fromIndex < 2)
            return;

        // Find the largest index k such that a[k] < a[k + 1]. If no such index 
        // exists, the permutation is the last permutation.
        int k = toIndex - 2;
        while (k >= fromIndex && (short)(a[k]-a[k + 1])>=0)
            k--;

        if (k < fromIndex) {
            // Reached end of permutation cycle - reverse the
            // whole array and start over
            reverse(a, fromIndex, toIndex);

        } else {

            // Find the largest index l such that a[k] < a[l]. Since k + 1 is 
            // such an index, l is well defined and satisfies k < l.
            int l = toIndex - 1;
            while (l >= fromIndex && (short)(a[k]-a[l])>=0)
                l--;

            // Swap a[k] with a[l].
            swap(a, k, l);

            // Reverse the sequence from a[k + 1] up to and including the final 
            // element a[n].   
            reverse(a, k + 1, toIndex);
        }
    }

    public static short min(short[] values ) {
        return values[argmin(values , 0, values.length - 1)];
    }

    public static short min(short[] values , int fromIndex, int toIndex) {
        return values[argmin(values , fromIndex, toIndex)];
    }

    public static short max(short[] values ) {
        return values[argmax(values , 0, values.length - 1)];
    }

    public static short max(short[] values , int fromIndex, int toIndex) {
        return values[argmax(values , fromIndex, toIndex)];
    }

    public static int argmin(short[] values ) {
        return argmin(values , 0, values.length - 1);
    }

    public static int argmin(short[] values , int fromIndex, int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if ((short)(values[i]-values[argmin])<0)
                argmin = i;
        return argmin;
    }

    public static int argmax(short[] values ) {
        return argmax(values , 0, values.length - 1);
    }

    public static int argmax(short[] values , int fromIndex, int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if ((short)(values[i]-values[argmax])>0)
                argmax = i;
        return argmax;
    }

    public static int[] argminmax(short[] values , int fromIndex, int toIndex) {
        int argmax = fromIndex;
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++) {
            if ((short)(values[i]-values[argmax])>0)
                argmax = i;
            else if ((short)(values[i]-values[argmin])<0)
                argmin = i;
        }
        return new int[]{argmin, argmax};
    }

    public static short[] minmax(short[] values ) {
        return minmax(values , 0, values.length - 1);
    }

    public static short[] minmax(short[] values , int fromIndex, int toIndex) {
        int[] mm = argminmax(values , fromIndex, toIndex);
        return new short[]{values[mm[1]], values[mm[0]]};
    }

    public static void clamp(short[] src , short min, short max, short[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if ((short)(src[i]- max) > 0)
                dst[i] = max;
            else if ((short)(src[i]- min) < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static short[] clamp(short[] src , short min, short max) {
        short[] dst = new short[src.length];
        clamp(src, min, max, dst);
        return dst;
    }

    public static void clampMin(short[] src , short min, short[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if ((short)(src[i]- min) < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static short[] clampMin(short[] src , short min) {
        short[] dst = new short[src.length];
        clampMin(src, min, dst);
        return dst;
    }

    public static void clampMax(short[] src , short max, short[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if ((short)(src[i]- max) > 0)
                dst[i] = max;
            else
                dst[i] = src[i];
        }
    }

    public static short[] clampMax(short[] src , short max) {
        short[] dst = new short[src.length];
        clampMax(src, max, dst);
        return dst;
    }



    public static short[] shortZeros(int n) {
        return new short[n];
    }

    public static short[] shortOnes(int n) {
        short[] result = new short[n];
        Arrays.fill(result, (short)1);
        return result;
    }

    public static short range(short[] values) {
        return range(values, 0, values.length - 1);
    }

    public static short range(short[] values, int fromIndex) {
        return range(values, fromIndex, values.length - 1);
    }

    public static short range(short[] values, int fromIndex, int toIndex) {
        int[] mm = argminmax(values, fromIndex, toIndex);
        return (short)(values[mm[1]]- values[mm[0]]);
    }


    public static short[] range(short start, short step, short end) {
        short[] arr = new short[(int)((end - start) / step)];
        for (int i = 0; i < arr.length; i++)
            arr[i] = (short)(start + (i * step));
        return arr;
    }

    public static short sum(short[] arr) {
        return sum(arr, 0, arr.length);
    }

    public static short sum(short[] arr, int fromIndex) {
        return sum(arr, fromIndex, arr.length);
    }

    public static short sum(short[] arr, int fromIndex, int toIndex) {
        short sum = (short)0;
        for (int i = fromIndex; i < toIndex; i++)
            sum += arr[i];
        return sum;
    }

    public static short product(short[] arr) {
        return product(arr, 0, arr.length);
    }

    public static short product(short[] arr, int fromIndex) {
        return product(arr, fromIndex, arr.length);
    }

    public static short product(short[] arr, int fromIndex, int toIndex) {
        short prod = (short)1;
        for (int i = fromIndex; i < toIndex; i++)
            prod += arr[i];
        return prod;
    }

    public static void mul(short[] src, short scalar, short[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (short)(src[i] * scalar);
    }

    public static void mul(short[] src1, short[] src2, short[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (short)(src1[i] * src2[i]);
    }

    public static short[] mul(short[] src, short scalar) {
        short[] dst = new short[src.length];
        mul(src, scalar, dst);
        return dst;
    }

    public static short[] mul(short[] src1, short[] src2) {
        short[] dst = new short[src1.length];
        mul(src1, src2, dst);
        return dst;
    }

    public static void div(short[] src, short scalar, short[] dst) {
        mul(src, (short)(1.0 / scalar), dst);
    }

    public static void div(short scalar, short[] src, short[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (short)(scalar / src[i]);
    }

    public static void div(short[] src1, short[] src2, short[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (short)(src1[i] / src2[i]);
    }

    public static short[] div(short[] src, short scalar) {
        return mul(src, (short)(1.0 / scalar));
    }

    public static short[] div(short scalar, short[] src) {
        short[] dst = new short[src.length];
        div(scalar, src, dst);
        return dst;
    }

    public static short[] div(short[] src1, short[] src2) {
        short[] dst = new short[src1.length];
        div(src1, src2, dst);
        return dst;
    }

    public static void add(short[] src, short scalar, short[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (short)(src[i] + scalar);
    }

    public static void add(short[] src1, short[] src2, short[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (short)(src1[i] + src2[i]);
    }

    public static short[] add(short[] src, short scalar) {
        short[] dst = new short[src.length];
        add(src, scalar, dst);
        return dst;
    }

    public static short[] add(short[] src1, short[] src2) {
        short[] dst = new short[src1.length];
        add(src1, src2, dst);
        return dst;
    }

    public static void sub(short[] src, short scalar, short[] dst) {
        add(src, (short)(-scalar), dst);
    }

    public static void sub(short[] src1, short[] src2, short[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (short)(src1[i] - src2[i]);
    }

    public static short[] sub(short[] src, short scalar) {
        return add(src, (short)(-scalar));
    }

    public static short[] sub(short[] src1, short[] src2) {
        short[] dst = new short[src1.length];
        sub(src1, src2, dst);
        return dst;
    }

    public static void mod(short[] src, short scalar, short[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (short)(src[i] % scalar);
    }

    public static void mod(short scalar, short[] src, short[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (short)(scalar % src[i]);
    }

    public static void mod(short[] src1, short[] src2, short[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (short)(src1[i] % src2[i]);
    }

    public static short[] mod(short[] src, short scalar) {
        short[] dst = new short[src.length];
        mod(src, scalar, dst);
        return dst;
    }

    public static short[] mod(short scalar, short[] src) {
        short[] dst = new short[src.length];
        mod(scalar, src, dst);
        return dst;
    }

    public static short[] mod(short[] src1, short[] src2) {
        short[] dst = new short[src1.length];
        mod(src1, src2, dst);
        return dst;
    }

    public static void negate(short[] src, short[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (short)(-src[i]);
    }

    public static short[] negate(short[] src) {
        short[] dst = new short[src.length];
        negate(src, dst);
        return dst;
    }

    public static void pow(short[] src, short power, short[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (short)Math.pow(src[i], power);
    }

    public static void pow(short[] src1, short[] src2, short[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (short)Math.pow(src1[i], src2[i]);
    }

    public static short[] pow(short[] src, short scalar) {
        short[] dst = new short[src.length];
        pow(src, scalar, dst);
        return dst;
    }

    public static short[] pow(short[] src1, short[] src2) {
        short[] dst = new short[src1.length];
        pow(src1, src2, dst);
        return dst;
    }

    public static void squared(short[] src, short[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (short)(src[i] * src[i]);
    }

    public static short[] squared(short[] src) {
        short[] dst = new short[src.length];
        squared(src, dst);
        return dst;
    }

    public static void cubed(short[] src, short[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (short)(src[i] * src[i] * src[i]);
    }

    public static short[] cubed(short[] src) {
        short[] dst = new short[src.length];
        cubed(src, dst);
        return dst;
    }

    public static void sqrt(short[] src, short[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (short)(Math.sqrt(src[i]));
    }

    public static short[] sqrt(short[] src) {
        short[] dst = new short[src.length];
        sqrt(src, dst);
        return dst;
    }

    public static void abs(short[] src, short[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (short)(Math.abs(src[i]));
    }

    public static short[] abs(short[] src) {
        short[] dst = new short[src.length];
        abs(src, dst);
        return dst;
    }


    public static short mean(short[] values) {
        return (short)(sum(values) / values.length);
    }
    
    /**
     * Return the median average of the values passed as argument.
     *
     * @param vals 1 or more values
     * @return the media value of the values
     * @throws IllegalArgumentException if no values are passed
     * @throws NullPointerException if a null object of type {@code double[]}
     *          array is passed
     */
    public static short median(short[] values) {
        if (values.length == 1)
            return values[0];
        short[] sorted = copyOf(values, values.length);
        Arrays.sort(sorted);
        final int i = sorted.length / 2;
        return (sorted.length % 2 == 0)
                ? (short)((sorted[i - 1] + sorted[i]) / 2)
                : sorted[i];
    }

    public static void normalise(short[] src, short min, short max,
                                 short[] dst) {
        Checks.checkEqual(src.length, dst.length);
        short smin = min(src);
        short srange = (short)(max(src) - smin);
        short drange = (short)(max - min);
        for (int i = 0; i < src.length; i++)
            dst[i] = (short)(((src[i] - smin) / srange) * drange + min);
    }

    public static void normalise(short[] src, short[] dst) {
        normalise(src, (short)0, (short)1, dst);
    }

    public static short[] normalise(short[] src, short min, short max) {
        short[] dst = new short[src.length];
        normalise(src, min, max, dst);
        return dst;
    }

    public static short[] normalise(short[] src) {
        return normalise(src, (short)0, (short)1);
    }



    public static String toHexString(final short[] arr) {
        final StringBuilder sbuf = new StringBuilder();
        sbuf.append('[');
        if (arr.length > 0)
            sbuf.append(Integer.toHexString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            sbuf.append(' ');
            sbuf.append(Integer.toHexString(arr[i]));
        }
        sbuf.append(']');
        return sbuf.toString();
    }



    /* 
     * ==================
     *  Type: int
     * ==================
     */

    public static int[] copyOf(final int[] src) {
        return copyOf(src, 0, src.length);
    }

    public static int[] copyOf(final int[] src, final int offset) {
        return copyOf(src, offset, src.length - offset);
    }

    public static int[] copyOf(final int[] src, final int offset, final int len) {
        int[] dst = new int[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    public static int[] cat(final int[]... arrs) {
        int n = 0;
        for (int i = 0; i < arrs.length; i++)
            n += arrs[i].length;
        int[] result = new int[n];
        int offset = 0;
        for (int i = 0; i < arrs.length; i++) {
            System.arraycopy(arrs[i], 0, result, offset, arrs[i].length);
            offset += arrs[i].length;
        }
        return result;
    }

    public static int firstIndexOf(final int[] arr, final int val) {
        return firstIndexOf(arr, val, 0, arr.length);
    }

    public static int firstIndexOf(final int[] arr, final int val, int fromIndex) {
        return firstIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int firstIndexOf(final int[] arr, final int val, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static int lastIndexOf(final int[] arr, final int val) {
        return lastIndexOf(arr, val, 0, arr.length);
    }

    public static int lastIndexOf(final int[] arr, final int val, int fromIndex) {
        return lastIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int lastIndexOf(final int[] arr, final int val, int fromIndex, int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static boolean contains(final int[] arr, final int val) {
        return firstIndexOf(arr, val) != -1;
    }

    public static boolean contains(final int[] arr, final int val, int fromIndex) {
        return firstIndexOf(arr, val, fromIndex) != -1;
    }

    public static boolean contains(final int[] arr, final int val, int fromIndex, int toIndex) {
        return firstIndexOf(arr, val, fromIndex, toIndex) != -1;
    }

    public static int[] unique(final int... arr) {
        // This is way to slow O(n^2) - can be done in 
        // O(n log n) by sorting in input first.
        int[] result = new int[arr.length];
        int j = 0;
        for (int i = 0; i < arr.length; i++) {
            if (!contains(result, arr[i])) {
                result[j] = arr[i];
                j++;
            }
        }
        return copyOf(result, 0, j);
    }

    public static void reverse(final int[] a) {
        reverse(a, 0, a.length);
    }

    public static void reverse(final int[] a, final int fromIndex) {
        reverse(a, fromIndex, a.length);
    }

    public static void reverse(final int[] a, final int fromIndex, final int toIndex) {
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            swap(a, i, j);
        }
    }

    public static void swap(final int[] a, final int i, final int j) {
        final int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static int[] elementsOf(int[] arr, int[] idx) {
        int[] result = new int[idx.length];
        for (int i = 0, j = 0; i < idx.length; i++)
            result[i] = arr[idx[i]];
        return result;
    }

    public static int[] elementsOf(int[] arr, boolean[] logical) {
        return elementsOf(arr, find(logical));
    }
    
    public static boolean[] valuesEq(int[] arr, int val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] == val;
        return result;
    }

    public static boolean[] valuesNeq(int[] arr, int val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] != val;
        return result;
    }

    public static int[] ensureCapacity(int[] arr, int minCap) {
        int oldCap = arr.length;
        if (minCap > oldCap) {
            int newCap = (oldCap * 3) / 2 + 1;
            if (newCap < minCap)
                newCap = minCap;
            arr = copyOf(arr, 0, newCap);
        }
        return arr;
    }
 
    public static int mode(int[] arr) {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i = 0; i < arr.length; i++) {
            if (map.containsKey(arr[i]))
                map.put(arr[i], map.get(arr[i]) + 1);
            else
                map.put(arr[i], 1);
        }
        int maxCount = -1;
        Integer maxValue = arr[0];
        for (Integer key : map.keySet()) {
            int count = map.get(key);
            if (count > maxCount) {
                maxCount = count;
                maxValue = key;
            }
        }
        return maxValue;
    }
    


    public static void permute(final int[] a) {
        permute(a, 0, a.length);
    }

    public static void permute(final int[] a, final int toIndex) {
        permute(a, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in 
     * lexicographic order.
     * 
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isnt the most efficient solution.
     * 
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static void permute(final int[] a, final int fromIndex, final int toIndex) {
        if (toIndex - fromIndex < 2)
            return;

        // Find the largest index k such that a[k] < a[k + 1]. If no such index 
        // exists, the permutation is the last permutation.
        int k = toIndex - 2;
        while (k >= fromIndex && a[k]-a[k + 1]>=0)
            k--;

        if (k < fromIndex) {
            // Reached end of permutation cycle - reverse the
            // whole array and start over
            reverse(a, fromIndex, toIndex);

        } else {

            // Find the largest index l such that a[k] < a[l]. Since k + 1 is 
            // such an index, l is well defined and satisfies k < l.
            int l = toIndex - 1;
            while (l >= fromIndex && a[k]-a[l]>=0)
                l--;

            // Swap a[k] with a[l].
            swap(a, k, l);

            // Reverse the sequence from a[k + 1] up to and including the final 
            // element a[n].   
            reverse(a, k + 1, toIndex);
        }
    }

    public static int min(int[] values ) {
        return values[argmin(values , 0, values.length - 1)];
    }

    public static int min(int[] values , int fromIndex, int toIndex) {
        return values[argmin(values , fromIndex, toIndex)];
    }

    public static int max(int[] values ) {
        return values[argmax(values , 0, values.length - 1)];
    }

    public static int max(int[] values , int fromIndex, int toIndex) {
        return values[argmax(values , fromIndex, toIndex)];
    }

    public static int argmin(int[] values ) {
        return argmin(values , 0, values.length - 1);
    }

    public static int argmin(int[] values , int fromIndex, int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (values[i]-values[argmin]<0)
                argmin = i;
        return argmin;
    }

    public static int argmax(int[] values ) {
        return argmax(values , 0, values.length - 1);
    }

    public static int argmax(int[] values , int fromIndex, int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (values[i]-values[argmax]>0)
                argmax = i;
        return argmax;
    }

    public static int[] argminmax(int[] values , int fromIndex, int toIndex) {
        int argmax = fromIndex;
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++) {
            if (values[i]-values[argmax]>0)
                argmax = i;
            else if (values[i]-values[argmin]<0)
                argmin = i;
        }
        return new int[]{argmin, argmax};
    }

    public static int[] minmax(int[] values ) {
        return minmax(values , 0, values.length - 1);
    }

    public static int[] minmax(int[] values , int fromIndex, int toIndex) {
        int[] mm = argminmax(values , fromIndex, toIndex);
        return new int[]{values[mm[1]], values[mm[0]]};
    }

    public static void clamp(int[] src , int min, int max, int[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if (src[i]- max > 0)
                dst[i] = max;
            else if (src[i]- min < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static int[] clamp(int[] src , int min, int max) {
        int[] dst = new int[src.length];
        clamp(src, min, max, dst);
        return dst;
    }

    public static void clampMin(int[] src , int min, int[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if (src[i]- min < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static int[] clampMin(int[] src , int min) {
        int[] dst = new int[src.length];
        clampMin(src, min, dst);
        return dst;
    }

    public static void clampMax(int[] src , int max, int[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if (src[i]- max > 0)
                dst[i] = max;
            else
                dst[i] = src[i];
        }
    }

    public static int[] clampMax(int[] src , int max) {
        int[] dst = new int[src.length];
        clampMax(src, max, dst);
        return dst;
    }



    public static int[] intZeros(int n) {
        return new int[n];
    }

    public static int[] intOnes(int n) {
        int[] result = new int[n];
        Arrays.fill(result, (int)1);
        return result;
    }

    public static int range(int[] values) {
        return range(values, 0, values.length - 1);
    }

    public static int range(int[] values, int fromIndex) {
        return range(values, fromIndex, values.length - 1);
    }

    public static int range(int[] values, int fromIndex, int toIndex) {
        int[] mm = argminmax(values, fromIndex, toIndex);
        return values[mm[1]]- values[mm[0]];
    }


    public static int[] range(int start, int step, int end) {
        int[] arr = new int[(int)((end - start) / step)];
        for (int i = 0; i < arr.length; i++)
            arr[i] = (int)(start + (i * step));
        return arr;
    }

    public static int sum(int[] arr) {
        return sum(arr, 0, arr.length);
    }

    public static int sum(int[] arr, int fromIndex) {
        return sum(arr, fromIndex, arr.length);
    }

    public static int sum(int[] arr, int fromIndex, int toIndex) {
        int sum = (int)0;
        for (int i = fromIndex; i < toIndex; i++)
            sum += arr[i];
        return sum;
    }

    public static int product(int[] arr) {
        return product(arr, 0, arr.length);
    }

    public static int product(int[] arr, int fromIndex) {
        return product(arr, fromIndex, arr.length);
    }

    public static int product(int[] arr, int fromIndex, int toIndex) {
        int prod = (int)1;
        for (int i = fromIndex; i < toIndex; i++)
            prod += arr[i];
        return prod;
    }

    public static void mul(int[] src, int scalar, int[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (int)(src[i] * scalar);
    }

    public static void mul(int[] src1, int[] src2, int[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (int)(src1[i] * src2[i]);
    }

    public static int[] mul(int[] src, int scalar) {
        int[] dst = new int[src.length];
        mul(src, scalar, dst);
        return dst;
    }

    public static int[] mul(int[] src1, int[] src2) {
        int[] dst = new int[src1.length];
        mul(src1, src2, dst);
        return dst;
    }

    public static void div(int[] src, int scalar, int[] dst) {
        mul(src, (int)(1.0 / scalar), dst);
    }

    public static void div(int scalar, int[] src, int[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (int)(scalar / src[i]);
    }

    public static void div(int[] src1, int[] src2, int[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (int)(src1[i] / src2[i]);
    }

    public static int[] div(int[] src, int scalar) {
        return mul(src, (int)(1.0 / scalar));
    }

    public static int[] div(int scalar, int[] src) {
        int[] dst = new int[src.length];
        div(scalar, src, dst);
        return dst;
    }

    public static int[] div(int[] src1, int[] src2) {
        int[] dst = new int[src1.length];
        div(src1, src2, dst);
        return dst;
    }

    public static void add(int[] src, int scalar, int[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (int)(src[i] + scalar);
    }

    public static void add(int[] src1, int[] src2, int[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (int)(src1[i] + src2[i]);
    }

    public static int[] add(int[] src, int scalar) {
        int[] dst = new int[src.length];
        add(src, scalar, dst);
        return dst;
    }

    public static int[] add(int[] src1, int[] src2) {
        int[] dst = new int[src1.length];
        add(src1, src2, dst);
        return dst;
    }

    public static void sub(int[] src, int scalar, int[] dst) {
        add(src, (int)(-scalar), dst);
    }

    public static void sub(int[] src1, int[] src2, int[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (int)(src1[i] - src2[i]);
    }

    public static int[] sub(int[] src, int scalar) {
        return add(src, (int)(-scalar));
    }

    public static int[] sub(int[] src1, int[] src2) {
        int[] dst = new int[src1.length];
        sub(src1, src2, dst);
        return dst;
    }

    public static void mod(int[] src, int scalar, int[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (int)(src[i] % scalar);
    }

    public static void mod(int scalar, int[] src, int[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (int)(scalar % src[i]);
    }

    public static void mod(int[] src1, int[] src2, int[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (int)(src1[i] % src2[i]);
    }

    public static int[] mod(int[] src, int scalar) {
        int[] dst = new int[src.length];
        mod(src, scalar, dst);
        return dst;
    }

    public static int[] mod(int scalar, int[] src) {
        int[] dst = new int[src.length];
        mod(scalar, src, dst);
        return dst;
    }

    public static int[] mod(int[] src1, int[] src2) {
        int[] dst = new int[src1.length];
        mod(src1, src2, dst);
        return dst;
    }

    public static void negate(int[] src, int[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (int)(-src[i]);
    }

    public static int[] negate(int[] src) {
        int[] dst = new int[src.length];
        negate(src, dst);
        return dst;
    }

    public static void pow(int[] src, int power, int[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (int)Math.pow(src[i], power);
    }

    public static void pow(int[] src1, int[] src2, int[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (int)Math.pow(src1[i], src2[i]);
    }

    public static int[] pow(int[] src, int scalar) {
        int[] dst = new int[src.length];
        pow(src, scalar, dst);
        return dst;
    }

    public static int[] pow(int[] src1, int[] src2) {
        int[] dst = new int[src1.length];
        pow(src1, src2, dst);
        return dst;
    }

    public static void squared(int[] src, int[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (int)(src[i] * src[i]);
    }

    public static int[] squared(int[] src) {
        int[] dst = new int[src.length];
        squared(src, dst);
        return dst;
    }

    public static void cubed(int[] src, int[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (int)(src[i] * src[i] * src[i]);
    }

    public static int[] cubed(int[] src) {
        int[] dst = new int[src.length];
        cubed(src, dst);
        return dst;
    }

    public static void sqrt(int[] src, int[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (int)(Math.sqrt(src[i]));
    }

    public static int[] sqrt(int[] src) {
        int[] dst = new int[src.length];
        sqrt(src, dst);
        return dst;
    }

    public static void abs(int[] src, int[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (int)(Math.abs(src[i]));
    }

    public static int[] abs(int[] src) {
        int[] dst = new int[src.length];
        abs(src, dst);
        return dst;
    }


    public static int mean(int[] values) {
        return (int)(sum(values) / values.length);
    }
    
    /**
     * Return the median average of the values passed as argument.
     *
     * @param vals 1 or more values
     * @return the media value of the values
     * @throws IllegalArgumentException if no values are passed
     * @throws NullPointerException if a null object of type {@code double[]}
     *          array is passed
     */
    public static int median(int[] values) {
        if (values.length == 1)
            return values[0];
        int[] sorted = copyOf(values, values.length);
        Arrays.sort(sorted);
        final int i = sorted.length / 2;
        return (sorted.length % 2 == 0)
                ? (int)((sorted[i - 1] + sorted[i]) / 2)
                : sorted[i];
    }

    public static void normalise(int[] src, int min, int max,
                                 int[] dst) {
        Checks.checkEqual(src.length, dst.length);
        int smin = min(src);
        int srange = (int)(max(src) - smin);
        int drange = (int)(max - min);
        for (int i = 0; i < src.length; i++)
            dst[i] = (int)(((src[i] - smin) / srange) * drange + min);
    }

    public static void normalise(int[] src, int[] dst) {
        normalise(src, (int)0, (int)1, dst);
    }

    public static int[] normalise(int[] src, int min, int max) {
        int[] dst = new int[src.length];
        normalise(src, min, max, dst);
        return dst;
    }

    public static int[] normalise(int[] src) {
        return normalise(src, (int)0, (int)1);
    }



    public static String toHexString(final int[] arr) {
        final StringBuilder sbuf = new StringBuilder();
        sbuf.append('[');
        if (arr.length > 0)
            sbuf.append(Integer.toHexString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            sbuf.append(' ');
            sbuf.append(Integer.toHexString(arr[i]));
        }
        sbuf.append(']');
        return sbuf.toString();
    }



    /* 
     * ==================
     *  Type: long
     * ==================
     */

    public static long[] copyOf(final long[] src) {
        return copyOf(src, 0, src.length);
    }

    public static long[] copyOf(final long[] src, final int offset) {
        return copyOf(src, offset, src.length - offset);
    }

    public static long[] copyOf(final long[] src, final int offset, final int len) {
        long[] dst = new long[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    public static long[] cat(final long[]... arrs) {
        int n = 0;
        for (int i = 0; i < arrs.length; i++)
            n += arrs[i].length;
        long[] result = new long[n];
        int offset = 0;
        for (int i = 0; i < arrs.length; i++) {
            System.arraycopy(arrs[i], 0, result, offset, arrs[i].length);
            offset += arrs[i].length;
        }
        return result;
    }

    public static int firstIndexOf(final long[] arr, final long val) {
        return firstIndexOf(arr, val, 0, arr.length);
    }

    public static int firstIndexOf(final long[] arr, final long val, int fromIndex) {
        return firstIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int firstIndexOf(final long[] arr, final long val, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static int lastIndexOf(final long[] arr, final long val) {
        return lastIndexOf(arr, val, 0, arr.length);
    }

    public static int lastIndexOf(final long[] arr, final long val, int fromIndex) {
        return lastIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int lastIndexOf(final long[] arr, final long val, int fromIndex, int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static boolean contains(final long[] arr, final long val) {
        return firstIndexOf(arr, val) != -1;
    }

    public static boolean contains(final long[] arr, final long val, int fromIndex) {
        return firstIndexOf(arr, val, fromIndex) != -1;
    }

    public static boolean contains(final long[] arr, final long val, int fromIndex, int toIndex) {
        return firstIndexOf(arr, val, fromIndex, toIndex) != -1;
    }

    public static long[] unique(final long... arr) {
        // This is way to slow O(n^2) - can be done in 
        // O(n log n) by sorting in input first.
        long[] result = new long[arr.length];
        int j = 0;
        for (int i = 0; i < arr.length; i++) {
            if (!contains(result, arr[i])) {
                result[j] = arr[i];
                j++;
            }
        }
        return copyOf(result, 0, j);
    }

    public static void reverse(final long[] a) {
        reverse(a, 0, a.length);
    }

    public static void reverse(final long[] a, final int fromIndex) {
        reverse(a, fromIndex, a.length);
    }

    public static void reverse(final long[] a, final int fromIndex, final int toIndex) {
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            swap(a, i, j);
        }
    }

    public static void swap(final long[] a, final int i, final int j) {
        final long tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static long[] elementsOf(long[] arr, int[] idx) {
        long[] result = new long[idx.length];
        for (int i = 0, j = 0; i < idx.length; i++)
            result[i] = arr[idx[i]];
        return result;
    }

    public static long[] elementsOf(long[] arr, boolean[] logical) {
        return elementsOf(arr, find(logical));
    }
    
    public static boolean[] valuesEq(long[] arr, long val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] == val;
        return result;
    }

    public static boolean[] valuesNeq(long[] arr, long val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] != val;
        return result;
    }

    public static long[] ensureCapacity(long[] arr, int minCap) {
        int oldCap = arr.length;
        if (minCap > oldCap) {
            int newCap = (oldCap * 3) / 2 + 1;
            if (newCap < minCap)
                newCap = minCap;
            arr = copyOf(arr, 0, newCap);
        }
        return arr;
    }
 
    public static long mode(long[] arr) {
        Map<Long, Integer> map = new HashMap<Long, Integer>();
        for (int i = 0; i < arr.length; i++) {
            if (map.containsKey(arr[i]))
                map.put(arr[i], map.get(arr[i]) + 1);
            else
                map.put(arr[i], 1);
        }
        int maxCount = -1;
        Long maxValue = arr[0];
        for (Long key : map.keySet()) {
            int count = map.get(key);
            if (count > maxCount) {
                maxCount = count;
                maxValue = key;
            }
        }
        return maxValue;
    }
    


    public static void permute(final long[] a) {
        permute(a, 0, a.length);
    }

    public static void permute(final long[] a, final int toIndex) {
        permute(a, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in 
     * lexicographic order.
     * 
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isnt the most efficient solution.
     * 
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static void permute(final long[] a, final int fromIndex, final int toIndex) {
        if (toIndex - fromIndex < 2)
            return;

        // Find the largest index k such that a[k] < a[k + 1]. If no such index 
        // exists, the permutation is the last permutation.
        int k = toIndex - 2;
        while (k >= fromIndex && a[k]-a[k + 1]>=0)
            k--;

        if (k < fromIndex) {
            // Reached end of permutation cycle - reverse the
            // whole array and start over
            reverse(a, fromIndex, toIndex);

        } else {

            // Find the largest index l such that a[k] < a[l]. Since k + 1 is 
            // such an index, l is well defined and satisfies k < l.
            int l = toIndex - 1;
            while (l >= fromIndex && a[k]-a[l]>=0)
                l--;

            // Swap a[k] with a[l].
            swap(a, k, l);

            // Reverse the sequence from a[k + 1] up to and including the final 
            // element a[n].   
            reverse(a, k + 1, toIndex);
        }
    }

    public static long min(long[] values ) {
        return values[argmin(values , 0, values.length - 1)];
    }

    public static long min(long[] values , int fromIndex, int toIndex) {
        return values[argmin(values , fromIndex, toIndex)];
    }

    public static long max(long[] values ) {
        return values[argmax(values , 0, values.length - 1)];
    }

    public static long max(long[] values , int fromIndex, int toIndex) {
        return values[argmax(values , fromIndex, toIndex)];
    }

    public static int argmin(long[] values ) {
        return argmin(values , 0, values.length - 1);
    }

    public static int argmin(long[] values , int fromIndex, int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (values[i]-values[argmin]<0)
                argmin = i;
        return argmin;
    }

    public static int argmax(long[] values ) {
        return argmax(values , 0, values.length - 1);
    }

    public static int argmax(long[] values , int fromIndex, int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (values[i]-values[argmax]>0)
                argmax = i;
        return argmax;
    }

    public static int[] argminmax(long[] values , int fromIndex, int toIndex) {
        int argmax = fromIndex;
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++) {
            if (values[i]-values[argmax]>0)
                argmax = i;
            else if (values[i]-values[argmin]<0)
                argmin = i;
        }
        return new int[]{argmin, argmax};
    }

    public static long[] minmax(long[] values ) {
        return minmax(values , 0, values.length - 1);
    }

    public static long[] minmax(long[] values , int fromIndex, int toIndex) {
        int[] mm = argminmax(values , fromIndex, toIndex);
        return new long[]{values[mm[1]], values[mm[0]]};
    }

    public static void clamp(long[] src , long min, long max, long[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if (src[i]- max > 0)
                dst[i] = max;
            else if (src[i]- min < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static long[] clamp(long[] src , long min, long max) {
        long[] dst = new long[src.length];
        clamp(src, min, max, dst);
        return dst;
    }

    public static void clampMin(long[] src , long min, long[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if (src[i]- min < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static long[] clampMin(long[] src , long min) {
        long[] dst = new long[src.length];
        clampMin(src, min, dst);
        return dst;
    }

    public static void clampMax(long[] src , long max, long[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if (src[i]- max > 0)
                dst[i] = max;
            else
                dst[i] = src[i];
        }
    }

    public static long[] clampMax(long[] src , long max) {
        long[] dst = new long[src.length];
        clampMax(src, max, dst);
        return dst;
    }



    public static long[] longZeros(int n) {
        return new long[n];
    }

    public static long[] longOnes(int n) {
        long[] result = new long[n];
        Arrays.fill(result, (long)1);
        return result;
    }

    public static long range(long[] values) {
        return range(values, 0, values.length - 1);
    }

    public static long range(long[] values, int fromIndex) {
        return range(values, fromIndex, values.length - 1);
    }

    public static long range(long[] values, int fromIndex, int toIndex) {
        int[] mm = argminmax(values, fromIndex, toIndex);
        return values[mm[1]]- values[mm[0]];
    }


    public static long[] range(long start, long step, long end) {
        long[] arr = new long[(int)((end - start) / step)];
        for (int i = 0; i < arr.length; i++)
            arr[i] = (long)(start + (i * step));
        return arr;
    }

    public static long sum(long[] arr) {
        return sum(arr, 0, arr.length);
    }

    public static long sum(long[] arr, int fromIndex) {
        return sum(arr, fromIndex, arr.length);
    }

    public static long sum(long[] arr, int fromIndex, int toIndex) {
        long sum = (long)0;
        for (int i = fromIndex; i < toIndex; i++)
            sum += arr[i];
        return sum;
    }

    public static long product(long[] arr) {
        return product(arr, 0, arr.length);
    }

    public static long product(long[] arr, int fromIndex) {
        return product(arr, fromIndex, arr.length);
    }

    public static long product(long[] arr, int fromIndex, int toIndex) {
        long prod = (long)1;
        for (int i = fromIndex; i < toIndex; i++)
            prod += arr[i];
        return prod;
    }

    public static void mul(long[] src, long scalar, long[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (long)(src[i] * scalar);
    }

    public static void mul(long[] src1, long[] src2, long[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (long)(src1[i] * src2[i]);
    }

    public static long[] mul(long[] src, long scalar) {
        long[] dst = new long[src.length];
        mul(src, scalar, dst);
        return dst;
    }

    public static long[] mul(long[] src1, long[] src2) {
        long[] dst = new long[src1.length];
        mul(src1, src2, dst);
        return dst;
    }

    public static void div(long[] src, long scalar, long[] dst) {
        mul(src, (long)(1.0 / scalar), dst);
    }

    public static void div(long scalar, long[] src, long[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (long)(scalar / src[i]);
    }

    public static void div(long[] src1, long[] src2, long[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (long)(src1[i] / src2[i]);
    }

    public static long[] div(long[] src, long scalar) {
        return mul(src, (long)(1.0 / scalar));
    }

    public static long[] div(long scalar, long[] src) {
        long[] dst = new long[src.length];
        div(scalar, src, dst);
        return dst;
    }

    public static long[] div(long[] src1, long[] src2) {
        long[] dst = new long[src1.length];
        div(src1, src2, dst);
        return dst;
    }

    public static void add(long[] src, long scalar, long[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (long)(src[i] + scalar);
    }

    public static void add(long[] src1, long[] src2, long[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (long)(src1[i] + src2[i]);
    }

    public static long[] add(long[] src, long scalar) {
        long[] dst = new long[src.length];
        add(src, scalar, dst);
        return dst;
    }

    public static long[] add(long[] src1, long[] src2) {
        long[] dst = new long[src1.length];
        add(src1, src2, dst);
        return dst;
    }

    public static void sub(long[] src, long scalar, long[] dst) {
        add(src, (long)(-scalar), dst);
    }

    public static void sub(long[] src1, long[] src2, long[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (long)(src1[i] - src2[i]);
    }

    public static long[] sub(long[] src, long scalar) {
        return add(src, (long)(-scalar));
    }

    public static long[] sub(long[] src1, long[] src2) {
        long[] dst = new long[src1.length];
        sub(src1, src2, dst);
        return dst;
    }

    public static void mod(long[] src, long scalar, long[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (long)(src[i] % scalar);
    }

    public static void mod(long scalar, long[] src, long[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (long)(scalar % src[i]);
    }

    public static void mod(long[] src1, long[] src2, long[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (long)(src1[i] % src2[i]);
    }

    public static long[] mod(long[] src, long scalar) {
        long[] dst = new long[src.length];
        mod(src, scalar, dst);
        return dst;
    }

    public static long[] mod(long scalar, long[] src) {
        long[] dst = new long[src.length];
        mod(scalar, src, dst);
        return dst;
    }

    public static long[] mod(long[] src1, long[] src2) {
        long[] dst = new long[src1.length];
        mod(src1, src2, dst);
        return dst;
    }

    public static void negate(long[] src, long[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (long)(-src[i]);
    }

    public static long[] negate(long[] src) {
        long[] dst = new long[src.length];
        negate(src, dst);
        return dst;
    }

    public static void pow(long[] src, long power, long[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (long)Math.pow(src[i], power);
    }

    public static void pow(long[] src1, long[] src2, long[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (long)Math.pow(src1[i], src2[i]);
    }

    public static long[] pow(long[] src, long scalar) {
        long[] dst = new long[src.length];
        pow(src, scalar, dst);
        return dst;
    }

    public static long[] pow(long[] src1, long[] src2) {
        long[] dst = new long[src1.length];
        pow(src1, src2, dst);
        return dst;
    }

    public static void squared(long[] src, long[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (long)(src[i] * src[i]);
    }

    public static long[] squared(long[] src) {
        long[] dst = new long[src.length];
        squared(src, dst);
        return dst;
    }

    public static void cubed(long[] src, long[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (long)(src[i] * src[i] * src[i]);
    }

    public static long[] cubed(long[] src) {
        long[] dst = new long[src.length];
        cubed(src, dst);
        return dst;
    }

    public static void sqrt(long[] src, long[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (long)(Math.sqrt(src[i]));
    }

    public static long[] sqrt(long[] src) {
        long[] dst = new long[src.length];
        sqrt(src, dst);
        return dst;
    }

    public static void abs(long[] src, long[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (long)(Math.abs(src[i]));
    }

    public static long[] abs(long[] src) {
        long[] dst = new long[src.length];
        abs(src, dst);
        return dst;
    }


    public static long mean(long[] values) {
        return (long)(sum(values) / values.length);
    }
    
    /**
     * Return the median average of the values passed as argument.
     *
     * @param vals 1 or more values
     * @return the media value of the values
     * @throws IllegalArgumentException if no values are passed
     * @throws NullPointerException if a null object of type {@code double[]}
     *          array is passed
     */
    public static long median(long[] values) {
        if (values.length == 1)
            return values[0];
        long[] sorted = copyOf(values, values.length);
        Arrays.sort(sorted);
        final int i = sorted.length / 2;
        return (sorted.length % 2 == 0)
                ? (long)((sorted[i - 1] + sorted[i]) / 2)
                : sorted[i];
    }

    public static void normalise(long[] src, long min, long max,
                                 long[] dst) {
        Checks.checkEqual(src.length, dst.length);
        long smin = min(src);
        long srange = (long)(max(src) - smin);
        long drange = (long)(max - min);
        for (int i = 0; i < src.length; i++)
            dst[i] = (long)(((src[i] - smin) / srange) * drange + min);
    }

    public static void normalise(long[] src, long[] dst) {
        normalise(src, (long)0, (long)1, dst);
    }

    public static long[] normalise(long[] src, long min, long max) {
        long[] dst = new long[src.length];
        normalise(src, min, max, dst);
        return dst;
    }

    public static long[] normalise(long[] src) {
        return normalise(src, (long)0, (long)1);
    }



    public static String toHexString(final long[] arr) {
        final StringBuilder sbuf = new StringBuilder();
        sbuf.append('[');
        if (arr.length > 0)
            sbuf.append(Long.toHexString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            sbuf.append(' ');
            sbuf.append(Long.toHexString(arr[i]));
        }
        sbuf.append(']');
        return sbuf.toString();
    }



    /* 
     * ==================
     *  Type: float
     * ==================
     */

    public static float[] copyOf(final float[] src) {
        return copyOf(src, 0, src.length);
    }

    public static float[] copyOf(final float[] src, final int offset) {
        return copyOf(src, offset, src.length - offset);
    }

    public static float[] copyOf(final float[] src, final int offset, final int len) {
        float[] dst = new float[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    public static float[] cat(final float[]... arrs) {
        int n = 0;
        for (int i = 0; i < arrs.length; i++)
            n += arrs[i].length;
        float[] result = new float[n];
        int offset = 0;
        for (int i = 0; i < arrs.length; i++) {
            System.arraycopy(arrs[i], 0, result, offset, arrs[i].length);
            offset += arrs[i].length;
        }
        return result;
    }

    public static int firstIndexOf(final float[] arr, final float val) {
        return firstIndexOf(arr, val, 0, arr.length);
    }

    public static int firstIndexOf(final float[] arr, final float val, int fromIndex) {
        return firstIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int firstIndexOf(final float[] arr, final float val, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static int lastIndexOf(final float[] arr, final float val) {
        return lastIndexOf(arr, val, 0, arr.length);
    }

    public static int lastIndexOf(final float[] arr, final float val, int fromIndex) {
        return lastIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int lastIndexOf(final float[] arr, final float val, int fromIndex, int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static boolean contains(final float[] arr, final float val) {
        return firstIndexOf(arr, val) != -1;
    }

    public static boolean contains(final float[] arr, final float val, int fromIndex) {
        return firstIndexOf(arr, val, fromIndex) != -1;
    }

    public static boolean contains(final float[] arr, final float val, int fromIndex, int toIndex) {
        return firstIndexOf(arr, val, fromIndex, toIndex) != -1;
    }

    public static float[] unique(final float... arr) {
        // This is way to slow O(n^2) - can be done in 
        // O(n log n) by sorting in input first.
        float[] result = new float[arr.length];
        int j = 0;
        for (int i = 0; i < arr.length; i++) {
            if (!contains(result, arr[i])) {
                result[j] = arr[i];
                j++;
            }
        }
        return copyOf(result, 0, j);
    }

    public static void reverse(final float[] a) {
        reverse(a, 0, a.length);
    }

    public static void reverse(final float[] a, final int fromIndex) {
        reverse(a, fromIndex, a.length);
    }

    public static void reverse(final float[] a, final int fromIndex, final int toIndex) {
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            swap(a, i, j);
        }
    }

    public static void swap(final float[] a, final int i, final int j) {
        final float tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static float[] elementsOf(float[] arr, int[] idx) {
        float[] result = new float[idx.length];
        for (int i = 0, j = 0; i < idx.length; i++)
            result[i] = arr[idx[i]];
        return result;
    }

    public static float[] elementsOf(float[] arr, boolean[] logical) {
        return elementsOf(arr, find(logical));
    }
    
    public static boolean[] valuesEq(float[] arr, float val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] == val;
        return result;
    }

    public static boolean[] valuesNeq(float[] arr, float val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] != val;
        return result;
    }

    public static float[] ensureCapacity(float[] arr, int minCap) {
        int oldCap = arr.length;
        if (minCap > oldCap) {
            int newCap = (oldCap * 3) / 2 + 1;
            if (newCap < minCap)
                newCap = minCap;
            arr = copyOf(arr, 0, newCap);
        }
        return arr;
    }
 
    public static float mode(float[] arr) {
        Map<Float, Integer> map = new HashMap<Float, Integer>();
        for (int i = 0; i < arr.length; i++) {
            if (map.containsKey(arr[i]))
                map.put(arr[i], map.get(arr[i]) + 1);
            else
                map.put(arr[i], 1);
        }
        int maxCount = -1;
        Float maxValue = arr[0];
        for (Float key : map.keySet()) {
            int count = map.get(key);
            if (count > maxCount) {
                maxCount = count;
                maxValue = key;
            }
        }
        return maxValue;
    }
    


    public static void permute(final float[] a) {
        permute(a, 0, a.length);
    }

    public static void permute(final float[] a, final int toIndex) {
        permute(a, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in 
     * lexicographic order.
     * 
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isnt the most efficient solution.
     * 
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static void permute(final float[] a, final int fromIndex, final int toIndex) {
        if (toIndex - fromIndex < 2)
            return;

        // Find the largest index k such that a[k] < a[k + 1]. If no such index 
        // exists, the permutation is the last permutation.
        int k = toIndex - 2;
        while (k >= fromIndex && a[k]-a[k + 1]>=0)
            k--;

        if (k < fromIndex) {
            // Reached end of permutation cycle - reverse the
            // whole array and start over
            reverse(a, fromIndex, toIndex);

        } else {

            // Find the largest index l such that a[k] < a[l]. Since k + 1 is 
            // such an index, l is well defined and satisfies k < l.
            int l = toIndex - 1;
            while (l >= fromIndex && a[k]-a[l]>=0)
                l--;

            // Swap a[k] with a[l].
            swap(a, k, l);

            // Reverse the sequence from a[k + 1] up to and including the final 
            // element a[n].   
            reverse(a, k + 1, toIndex);
        }
    }

    public static float min(float[] values ) {
        return values[argmin(values , 0, values.length - 1)];
    }

    public static float min(float[] values , int fromIndex, int toIndex) {
        return values[argmin(values , fromIndex, toIndex)];
    }

    public static float max(float[] values ) {
        return values[argmax(values , 0, values.length - 1)];
    }

    public static float max(float[] values , int fromIndex, int toIndex) {
        return values[argmax(values , fromIndex, toIndex)];
    }

    public static int argmin(float[] values ) {
        return argmin(values , 0, values.length - 1);
    }

    public static int argmin(float[] values , int fromIndex, int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (values[i]-values[argmin]<0)
                argmin = i;
        return argmin;
    }

    public static int argmax(float[] values ) {
        return argmax(values , 0, values.length - 1);
    }

    public static int argmax(float[] values , int fromIndex, int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (values[i]-values[argmax]>0)
                argmax = i;
        return argmax;
    }

    public static int[] argminmax(float[] values , int fromIndex, int toIndex) {
        int argmax = fromIndex;
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++) {
            if (values[i]-values[argmax]>0)
                argmax = i;
            else if (values[i]-values[argmin]<0)
                argmin = i;
        }
        return new int[]{argmin, argmax};
    }

    public static float[] minmax(float[] values ) {
        return minmax(values , 0, values.length - 1);
    }

    public static float[] minmax(float[] values , int fromIndex, int toIndex) {
        int[] mm = argminmax(values , fromIndex, toIndex);
        return new float[]{values[mm[1]], values[mm[0]]};
    }

    public static void clamp(float[] src , float min, float max, float[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if (src[i]- max > 0)
                dst[i] = max;
            else if (src[i]- min < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static float[] clamp(float[] src , float min, float max) {
        float[] dst = new float[src.length];
        clamp(src, min, max, dst);
        return dst;
    }

    public static void clampMin(float[] src , float min, float[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if (src[i]- min < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static float[] clampMin(float[] src , float min) {
        float[] dst = new float[src.length];
        clampMin(src, min, dst);
        return dst;
    }

    public static void clampMax(float[] src , float max, float[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if (src[i]- max > 0)
                dst[i] = max;
            else
                dst[i] = src[i];
        }
    }

    public static float[] clampMax(float[] src , float max) {
        float[] dst = new float[src.length];
        clampMax(src, max, dst);
        return dst;
    }



    public static float[] floatZeros(int n) {
        return new float[n];
    }

    public static float[] floatOnes(int n) {
        float[] result = new float[n];
        Arrays.fill(result, (float)1);
        return result;
    }

    public static float range(float[] values) {
        return range(values, 0, values.length - 1);
    }

    public static float range(float[] values, int fromIndex) {
        return range(values, fromIndex, values.length - 1);
    }

    public static float range(float[] values, int fromIndex, int toIndex) {
        int[] mm = argminmax(values, fromIndex, toIndex);
        return values[mm[1]]- values[mm[0]];
    }


    public static float[] range(float start, float step, float end) {
        float[] arr = new float[(int)((end - start) / step)];
        for (int i = 0; i < arr.length; i++)
            arr[i] = (float)(start + (i * step));
        return arr;
    }

    public static float sum(float[] arr) {
        return sum(arr, 0, arr.length);
    }

    public static float sum(float[] arr, int fromIndex) {
        return sum(arr, fromIndex, arr.length);
    }

    public static float sum(float[] arr, int fromIndex, int toIndex) {
        float sum = (float)0;
        for (int i = fromIndex; i < toIndex; i++)
            sum += arr[i];
        return sum;
    }

    public static float product(float[] arr) {
        return product(arr, 0, arr.length);
    }

    public static float product(float[] arr, int fromIndex) {
        return product(arr, fromIndex, arr.length);
    }

    public static float product(float[] arr, int fromIndex, int toIndex) {
        float prod = (float)1;
        for (int i = fromIndex; i < toIndex; i++)
            prod += arr[i];
        return prod;
    }

    public static void mul(float[] src, float scalar, float[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (float)(src[i] * scalar);
    }

    public static void mul(float[] src1, float[] src2, float[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (float)(src1[i] * src2[i]);
    }

    public static float[] mul(float[] src, float scalar) {
        float[] dst = new float[src.length];
        mul(src, scalar, dst);
        return dst;
    }

    public static float[] mul(float[] src1, float[] src2) {
        float[] dst = new float[src1.length];
        mul(src1, src2, dst);
        return dst;
    }

    public static void div(float[] src, float scalar, float[] dst) {
        mul(src, (float)(1.0 / scalar), dst);
    }

    public static void div(float scalar, float[] src, float[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (float)(scalar / src[i]);
    }

    public static void div(float[] src1, float[] src2, float[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (float)(src1[i] / src2[i]);
    }

    public static float[] div(float[] src, float scalar) {
        return mul(src, (float)(1.0 / scalar));
    }

    public static float[] div(float scalar, float[] src) {
        float[] dst = new float[src.length];
        div(scalar, src, dst);
        return dst;
    }

    public static float[] div(float[] src1, float[] src2) {
        float[] dst = new float[src1.length];
        div(src1, src2, dst);
        return dst;
    }

    public static void add(float[] src, float scalar, float[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (float)(src[i] + scalar);
    }

    public static void add(float[] src1, float[] src2, float[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (float)(src1[i] + src2[i]);
    }

    public static float[] add(float[] src, float scalar) {
        float[] dst = new float[src.length];
        add(src, scalar, dst);
        return dst;
    }

    public static float[] add(float[] src1, float[] src2) {
        float[] dst = new float[src1.length];
        add(src1, src2, dst);
        return dst;
    }

    public static void sub(float[] src, float scalar, float[] dst) {
        add(src, (float)(-scalar), dst);
    }

    public static void sub(float[] src1, float[] src2, float[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (float)(src1[i] - src2[i]);
    }

    public static float[] sub(float[] src, float scalar) {
        return add(src, (float)(-scalar));
    }

    public static float[] sub(float[] src1, float[] src2) {
        float[] dst = new float[src1.length];
        sub(src1, src2, dst);
        return dst;
    }

    public static void mod(float[] src, float scalar, float[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (float)(src[i] % scalar);
    }

    public static void mod(float scalar, float[] src, float[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (float)(scalar % src[i]);
    }

    public static void mod(float[] src1, float[] src2, float[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (float)(src1[i] % src2[i]);
    }

    public static float[] mod(float[] src, float scalar) {
        float[] dst = new float[src.length];
        mod(src, scalar, dst);
        return dst;
    }

    public static float[] mod(float scalar, float[] src) {
        float[] dst = new float[src.length];
        mod(scalar, src, dst);
        return dst;
    }

    public static float[] mod(float[] src1, float[] src2) {
        float[] dst = new float[src1.length];
        mod(src1, src2, dst);
        return dst;
    }

    public static void negate(float[] src, float[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (float)(-src[i]);
    }

    public static float[] negate(float[] src) {
        float[] dst = new float[src.length];
        negate(src, dst);
        return dst;
    }

    public static void pow(float[] src, float power, float[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (float)Math.pow(src[i], power);
    }

    public static void pow(float[] src1, float[] src2, float[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (float)Math.pow(src1[i], src2[i]);
    }

    public static float[] pow(float[] src, float scalar) {
        float[] dst = new float[src.length];
        pow(src, scalar, dst);
        return dst;
    }

    public static float[] pow(float[] src1, float[] src2) {
        float[] dst = new float[src1.length];
        pow(src1, src2, dst);
        return dst;
    }

    public static void squared(float[] src, float[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (float)(src[i] * src[i]);
    }

    public static float[] squared(float[] src) {
        float[] dst = new float[src.length];
        squared(src, dst);
        return dst;
    }

    public static void cubed(float[] src, float[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (float)(src[i] * src[i] * src[i]);
    }

    public static float[] cubed(float[] src) {
        float[] dst = new float[src.length];
        cubed(src, dst);
        return dst;
    }

    public static void sqrt(float[] src, float[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (float)(Math.sqrt(src[i]));
    }

    public static float[] sqrt(float[] src) {
        float[] dst = new float[src.length];
        sqrt(src, dst);
        return dst;
    }

    public static void abs(float[] src, float[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (float)(Math.abs(src[i]));
    }

    public static float[] abs(float[] src) {
        float[] dst = new float[src.length];
        abs(src, dst);
        return dst;
    }


    public static float mean(float[] values) {
        return (float)(sum(values) / values.length);
    }
    
    /**
     * Return the median average of the values passed as argument.
     *
     * @param vals 1 or more values
     * @return the media value of the values
     * @throws IllegalArgumentException if no values are passed
     * @throws NullPointerException if a null object of type {@code double[]}
     *          array is passed
     */
    public static float median(float[] values) {
        if (values.length == 1)
            return values[0];
        float[] sorted = copyOf(values, values.length);
        Arrays.sort(sorted);
        final int i = sorted.length / 2;
        return (sorted.length % 2 == 0)
                ? (float)((sorted[i - 1] + sorted[i]) / 2)
                : sorted[i];
    }

    public static void normalise(float[] src, float min, float max,
                                 float[] dst) {
        Checks.checkEqual(src.length, dst.length);
        float smin = min(src);
        float srange = (float)(max(src) - smin);
        float drange = (float)(max - min);
        for (int i = 0; i < src.length; i++)
            dst[i] = (float)(((src[i] - smin) / srange) * drange + min);
    }

    public static void normalise(float[] src, float[] dst) {
        normalise(src, (float)0, (float)1, dst);
    }

    public static float[] normalise(float[] src, float min, float max) {
        float[] dst = new float[src.length];
        normalise(src, min, max, dst);
        return dst;
    }

    public static float[] normalise(float[] src) {
        return normalise(src, (float)0, (float)1);
    }




    public static float[] round(final float[] arr) {
        final float[] result = new float[arr.length];
        for (int i = 0; i < arr.length; i++) 
            result[i] = (float)Math.round(arr[i]);
        return result;
    }

    public static float[] floor(final float[] arr) {
        final float[] result = new float[arr.length];
        for (int i = 0; i < arr.length; i++) 
            result[i] = (float)Math.floor(arr[i]);
        return result;
    }

    public static float[] ceil(final float[] arr) {
        final float[] result = new float[arr.length];
        for (int i = 0; i < arr.length; i++) 
            result[i] = (float)Math.ceil(arr[i]);
        return result;
    }


    public static String toHexString(final float[] arr) {
        final StringBuilder sbuf = new StringBuilder();
        sbuf.append('[');
        if (arr.length > 0)
            sbuf.append(Float.toHexString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            sbuf.append(' ');
            sbuf.append(Float.toHexString(arr[i]));
        }
        sbuf.append(']');
        return sbuf.toString();
    }



    /* 
     * ==================
     *  Type: double
     * ==================
     */

    public static double[] copyOf(final double[] src) {
        return copyOf(src, 0, src.length);
    }

    public static double[] copyOf(final double[] src, final int offset) {
        return copyOf(src, offset, src.length - offset);
    }

    public static double[] copyOf(final double[] src, final int offset, final int len) {
        double[] dst = new double[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    public static double[] cat(final double[]... arrs) {
        int n = 0;
        for (int i = 0; i < arrs.length; i++)
            n += arrs[i].length;
        double[] result = new double[n];
        int offset = 0;
        for (int i = 0; i < arrs.length; i++) {
            System.arraycopy(arrs[i], 0, result, offset, arrs[i].length);
            offset += arrs[i].length;
        }
        return result;
    }

    public static int firstIndexOf(final double[] arr, final double val) {
        return firstIndexOf(arr, val, 0, arr.length);
    }

    public static int firstIndexOf(final double[] arr, final double val, int fromIndex) {
        return firstIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int firstIndexOf(final double[] arr, final double val, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static int lastIndexOf(final double[] arr, final double val) {
        return lastIndexOf(arr, val, 0, arr.length);
    }

    public static int lastIndexOf(final double[] arr, final double val, int fromIndex) {
        return lastIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int lastIndexOf(final double[] arr, final double val, int fromIndex, int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static boolean contains(final double[] arr, final double val) {
        return firstIndexOf(arr, val) != -1;
    }

    public static boolean contains(final double[] arr, final double val, int fromIndex) {
        return firstIndexOf(arr, val, fromIndex) != -1;
    }

    public static boolean contains(final double[] arr, final double val, int fromIndex, int toIndex) {
        return firstIndexOf(arr, val, fromIndex, toIndex) != -1;
    }

    public static double[] unique(final double... arr) {
        // This is way to slow O(n^2) - can be done in 
        // O(n log n) by sorting in input first.
        double[] result = new double[arr.length];
        int j = 0;
        for (int i = 0; i < arr.length; i++) {
            if (!contains(result, arr[i])) {
                result[j] = arr[i];
                j++;
            }
        }
        return copyOf(result, 0, j);
    }

    public static void reverse(final double[] a) {
        reverse(a, 0, a.length);
    }

    public static void reverse(final double[] a, final int fromIndex) {
        reverse(a, fromIndex, a.length);
    }

    public static void reverse(final double[] a, final int fromIndex, final int toIndex) {
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            swap(a, i, j);
        }
    }

    public static void swap(final double[] a, final int i, final int j) {
        final double tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static double[] elementsOf(double[] arr, int[] idx) {
        double[] result = new double[idx.length];
        for (int i = 0, j = 0; i < idx.length; i++)
            result[i] = arr[idx[i]];
        return result;
    }

    public static double[] elementsOf(double[] arr, boolean[] logical) {
        return elementsOf(arr, find(logical));
    }
    
    public static boolean[] valuesEq(double[] arr, double val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] == val;
        return result;
    }

    public static boolean[] valuesNeq(double[] arr, double val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] != val;
        return result;
    }

    public static double[] ensureCapacity(double[] arr, int minCap) {
        int oldCap = arr.length;
        if (minCap > oldCap) {
            int newCap = (oldCap * 3) / 2 + 1;
            if (newCap < minCap)
                newCap = minCap;
            arr = copyOf(arr, 0, newCap);
        }
        return arr;
    }
 
    public static double mode(double[] arr) {
        Map<Double, Integer> map = new HashMap<Double, Integer>();
        for (int i = 0; i < arr.length; i++) {
            if (map.containsKey(arr[i]))
                map.put(arr[i], map.get(arr[i]) + 1);
            else
                map.put(arr[i], 1);
        }
        int maxCount = -1;
        Double maxValue = arr[0];
        for (Double key : map.keySet()) {
            int count = map.get(key);
            if (count > maxCount) {
                maxCount = count;
                maxValue = key;
            }
        }
        return maxValue;
    }
    


    public static void permute(final double[] a) {
        permute(a, 0, a.length);
    }

    public static void permute(final double[] a, final int toIndex) {
        permute(a, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in 
     * lexicographic order.
     * 
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isnt the most efficient solution.
     * 
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static void permute(final double[] a, final int fromIndex, final int toIndex) {
        if (toIndex - fromIndex < 2)
            return;

        // Find the largest index k such that a[k] < a[k + 1]. If no such index 
        // exists, the permutation is the last permutation.
        int k = toIndex - 2;
        while (k >= fromIndex && a[k]-a[k + 1]>=0)
            k--;

        if (k < fromIndex) {
            // Reached end of permutation cycle - reverse the
            // whole array and start over
            reverse(a, fromIndex, toIndex);

        } else {

            // Find the largest index l such that a[k] < a[l]. Since k + 1 is 
            // such an index, l is well defined and satisfies k < l.
            int l = toIndex - 1;
            while (l >= fromIndex && a[k]-a[l]>=0)
                l--;

            // Swap a[k] with a[l].
            swap(a, k, l);

            // Reverse the sequence from a[k + 1] up to and including the final 
            // element a[n].   
            reverse(a, k + 1, toIndex);
        }
    }

    public static double min(double[] values ) {
        return values[argmin(values , 0, values.length - 1)];
    }

    public static double min(double[] values , int fromIndex, int toIndex) {
        return values[argmin(values , fromIndex, toIndex)];
    }

    public static double max(double[] values ) {
        return values[argmax(values , 0, values.length - 1)];
    }

    public static double max(double[] values , int fromIndex, int toIndex) {
        return values[argmax(values , fromIndex, toIndex)];
    }

    public static int argmin(double[] values ) {
        return argmin(values , 0, values.length - 1);
    }

    public static int argmin(double[] values , int fromIndex, int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (values[i]-values[argmin]<0)
                argmin = i;
        return argmin;
    }

    public static int argmax(double[] values ) {
        return argmax(values , 0, values.length - 1);
    }

    public static int argmax(double[] values , int fromIndex, int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (values[i]-values[argmax]>0)
                argmax = i;
        return argmax;
    }

    public static int[] argminmax(double[] values , int fromIndex, int toIndex) {
        int argmax = fromIndex;
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++) {
            if (values[i]-values[argmax]>0)
                argmax = i;
            else if (values[i]-values[argmin]<0)
                argmin = i;
        }
        return new int[]{argmin, argmax};
    }

    public static double[] minmax(double[] values ) {
        return minmax(values , 0, values.length - 1);
    }

    public static double[] minmax(double[] values , int fromIndex, int toIndex) {
        int[] mm = argminmax(values , fromIndex, toIndex);
        return new double[]{values[mm[1]], values[mm[0]]};
    }

    public static void clamp(double[] src , double min, double max, double[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if (src[i]- max > 0)
                dst[i] = max;
            else if (src[i]- min < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static double[] clamp(double[] src , double min, double max) {
        double[] dst = new double[src.length];
        clamp(src, min, max, dst);
        return dst;
    }

    public static void clampMin(double[] src , double min, double[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if (src[i]- min < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static double[] clampMin(double[] src , double min) {
        double[] dst = new double[src.length];
        clampMin(src, min, dst);
        return dst;
    }

    public static void clampMax(double[] src , double max, double[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if (src[i]- max > 0)
                dst[i] = max;
            else
                dst[i] = src[i];
        }
    }

    public static double[] clampMax(double[] src , double max) {
        double[] dst = new double[src.length];
        clampMax(src, max, dst);
        return dst;
    }



    public static double[] doubleZeros(int n) {
        return new double[n];
    }

    public static double[] doubleOnes(int n) {
        double[] result = new double[n];
        Arrays.fill(result, (double)1);
        return result;
    }

    public static double range(double[] values) {
        return range(values, 0, values.length - 1);
    }

    public static double range(double[] values, int fromIndex) {
        return range(values, fromIndex, values.length - 1);
    }

    public static double range(double[] values, int fromIndex, int toIndex) {
        int[] mm = argminmax(values, fromIndex, toIndex);
        return values[mm[1]]- values[mm[0]];
    }


    public static double[] range(double start, double step, double end) {
        double[] arr = new double[(int)((end - start) / step)];
        for (int i = 0; i < arr.length; i++)
            arr[i] = (double)(start + (i * step));
        return arr;
    }

    public static double sum(double[] arr) {
        return sum(arr, 0, arr.length);
    }

    public static double sum(double[] arr, int fromIndex) {
        return sum(arr, fromIndex, arr.length);
    }

    public static double sum(double[] arr, int fromIndex, int toIndex) {
        double sum = (double)0;
        for (int i = fromIndex; i < toIndex; i++)
            sum += arr[i];
        return sum;
    }

    public static double product(double[] arr) {
        return product(arr, 0, arr.length);
    }

    public static double product(double[] arr, int fromIndex) {
        return product(arr, fromIndex, arr.length);
    }

    public static double product(double[] arr, int fromIndex, int toIndex) {
        double prod = (double)1;
        for (int i = fromIndex; i < toIndex; i++)
            prod += arr[i];
        return prod;
    }

    public static void mul(double[] src, double scalar, double[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (double)(src[i] * scalar);
    }

    public static void mul(double[] src1, double[] src2, double[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (double)(src1[i] * src2[i]);
    }

    public static double[] mul(double[] src, double scalar) {
        double[] dst = new double[src.length];
        mul(src, scalar, dst);
        return dst;
    }

    public static double[] mul(double[] src1, double[] src2) {
        double[] dst = new double[src1.length];
        mul(src1, src2, dst);
        return dst;
    }

    public static void div(double[] src, double scalar, double[] dst) {
        mul(src, (double)(1.0 / scalar), dst);
    }

    public static void div(double scalar, double[] src, double[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (double)(scalar / src[i]);
    }

    public static void div(double[] src1, double[] src2, double[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (double)(src1[i] / src2[i]);
    }

    public static double[] div(double[] src, double scalar) {
        return mul(src, (double)(1.0 / scalar));
    }

    public static double[] div(double scalar, double[] src) {
        double[] dst = new double[src.length];
        div(scalar, src, dst);
        return dst;
    }

    public static double[] div(double[] src1, double[] src2) {
        double[] dst = new double[src1.length];
        div(src1, src2, dst);
        return dst;
    }

    public static void add(double[] src, double scalar, double[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (double)(src[i] + scalar);
    }

    public static void add(double[] src1, double[] src2, double[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (double)(src1[i] + src2[i]);
    }

    public static double[] add(double[] src, double scalar) {
        double[] dst = new double[src.length];
        add(src, scalar, dst);
        return dst;
    }

    public static double[] add(double[] src1, double[] src2) {
        double[] dst = new double[src1.length];
        add(src1, src2, dst);
        return dst;
    }

    public static void sub(double[] src, double scalar, double[] dst) {
        add(src, (double)(-scalar), dst);
    }

    public static void sub(double[] src1, double[] src2, double[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (double)(src1[i] - src2[i]);
    }

    public static double[] sub(double[] src, double scalar) {
        return add(src, (double)(-scalar));
    }

    public static double[] sub(double[] src1, double[] src2) {
        double[] dst = new double[src1.length];
        sub(src1, src2, dst);
        return dst;
    }

    public static void mod(double[] src, double scalar, double[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (double)(src[i] % scalar);
    }

    public static void mod(double scalar, double[] src, double[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (double)(scalar % src[i]);
    }

    public static void mod(double[] src1, double[] src2, double[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (double)(src1[i] % src2[i]);
    }

    public static double[] mod(double[] src, double scalar) {
        double[] dst = new double[src.length];
        mod(src, scalar, dst);
        return dst;
    }

    public static double[] mod(double scalar, double[] src) {
        double[] dst = new double[src.length];
        mod(scalar, src, dst);
        return dst;
    }

    public static double[] mod(double[] src1, double[] src2) {
        double[] dst = new double[src1.length];
        mod(src1, src2, dst);
        return dst;
    }

    public static void negate(double[] src, double[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (double)(-src[i]);
    }

    public static double[] negate(double[] src) {
        double[] dst = new double[src.length];
        negate(src, dst);
        return dst;
    }

    public static void pow(double[] src, double power, double[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (double)Math.pow(src[i], power);
    }

    public static void pow(double[] src1, double[] src2, double[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (double)Math.pow(src1[i], src2[i]);
    }

    public static double[] pow(double[] src, double scalar) {
        double[] dst = new double[src.length];
        pow(src, scalar, dst);
        return dst;
    }

    public static double[] pow(double[] src1, double[] src2) {
        double[] dst = new double[src1.length];
        pow(src1, src2, dst);
        return dst;
    }

    public static void squared(double[] src, double[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (double)(src[i] * src[i]);
    }

    public static double[] squared(double[] src) {
        double[] dst = new double[src.length];
        squared(src, dst);
        return dst;
    }

    public static void cubed(double[] src, double[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (double)(src[i] * src[i] * src[i]);
    }

    public static double[] cubed(double[] src) {
        double[] dst = new double[src.length];
        cubed(src, dst);
        return dst;
    }

    public static void sqrt(double[] src, double[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (double)(Math.sqrt(src[i]));
    }

    public static double[] sqrt(double[] src) {
        double[] dst = new double[src.length];
        sqrt(src, dst);
        return dst;
    }

    public static void abs(double[] src, double[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (double)(Math.abs(src[i]));
    }

    public static double[] abs(double[] src) {
        double[] dst = new double[src.length];
        abs(src, dst);
        return dst;
    }


    public static double mean(double[] values) {
        return (double)(sum(values) / values.length);
    }
    
    /**
     * Return the median average of the values passed as argument.
     *
     * @param vals 1 or more values
     * @return the media value of the values
     * @throws IllegalArgumentException if no values are passed
     * @throws NullPointerException if a null object of type {@code double[]}
     *          array is passed
     */
    public static double median(double[] values) {
        if (values.length == 1)
            return values[0];
        double[] sorted = copyOf(values, values.length);
        Arrays.sort(sorted);
        final int i = sorted.length / 2;
        return (sorted.length % 2 == 0)
                ? (double)((sorted[i - 1] + sorted[i]) / 2)
                : sorted[i];
    }

    public static void normalise(double[] src, double min, double max,
                                 double[] dst) {
        Checks.checkEqual(src.length, dst.length);
        double smin = min(src);
        double srange = (double)(max(src) - smin);
        double drange = (double)(max - min);
        for (int i = 0; i < src.length; i++)
            dst[i] = (double)(((src[i] - smin) / srange) * drange + min);
    }

    public static void normalise(double[] src, double[] dst) {
        normalise(src, (double)0, (double)1, dst);
    }

    public static double[] normalise(double[] src, double min, double max) {
        double[] dst = new double[src.length];
        normalise(src, min, max, dst);
        return dst;
    }

    public static double[] normalise(double[] src) {
        return normalise(src, (double)0, (double)1);
    }




    public static double[] round(final double[] arr) {
        final double[] result = new double[arr.length];
        for (int i = 0; i < arr.length; i++) 
            result[i] = (double)Math.round(arr[i]);
        return result;
    }

    public static double[] floor(final double[] arr) {
        final double[] result = new double[arr.length];
        for (int i = 0; i < arr.length; i++) 
            result[i] = (double)Math.floor(arr[i]);
        return result;
    }

    public static double[] ceil(final double[] arr) {
        final double[] result = new double[arr.length];
        for (int i = 0; i < arr.length; i++) 
            result[i] = (double)Math.ceil(arr[i]);
        return result;
    }


    public static String toHexString(final double[] arr) {
        final StringBuilder sbuf = new StringBuilder();
        sbuf.append('[');
        if (arr.length > 0)
            sbuf.append(Double.toHexString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            sbuf.append(' ');
            sbuf.append(Double.toHexString(arr[i]));
        }
        sbuf.append(']');
        return sbuf.toString();
    }



    /* 
     * ==================
     *  Type: Object
     * ==================
     */

    public static Object[] copyOf(final Object[] src) {
        return copyOf(src, 0, src.length);
    }

    public static Object[] copyOf(final Object[] src, final int offset) {
        return copyOf(src, offset, src.length - offset);
    }

    public static Object[] copyOf(final Object[] src, final int offset, final int len) {
        Object[] dst = new Object[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    public static Object[] cat(final Object[]... arrs) {
        int n = 0;
        for (int i = 0; i < arrs.length; i++)
            n += arrs[i].length;
        Object[] result = new Object[n];
        int offset = 0;
        for (int i = 0; i < arrs.length; i++) {
            System.arraycopy(arrs[i], 0, result, offset, arrs[i].length);
            offset += arrs[i].length;
        }
        return result;
    }

    public static int firstIndexOf(final Object[] arr, final Object val) {
        return firstIndexOf(arr, val, 0, arr.length);
    }

    public static int firstIndexOf(final Object[] arr, final Object val, int fromIndex) {
        return firstIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int firstIndexOf(final Object[] arr, final Object val, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static int lastIndexOf(final Object[] arr, final Object val) {
        return lastIndexOf(arr, val, 0, arr.length);
    }

    public static int lastIndexOf(final Object[] arr, final Object val, int fromIndex) {
        return lastIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int lastIndexOf(final Object[] arr, final Object val, int fromIndex, int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static boolean contains(final Object[] arr, final Object val) {
        return firstIndexOf(arr, val) != -1;
    }

    public static boolean contains(final Object[] arr, final Object val, int fromIndex) {
        return firstIndexOf(arr, val, fromIndex) != -1;
    }

    public static boolean contains(final Object[] arr, final Object val, int fromIndex, int toIndex) {
        return firstIndexOf(arr, val, fromIndex, toIndex) != -1;
    }

    public static Object[] unique(final Object... arr) {
        // This is way to slow O(n^2) - can be done in 
        // O(n log n) by sorting in input first.
        Object[] result = new Object[arr.length];
        int j = 0;
        for (int i = 0; i < arr.length; i++) {
            if (!contains(result, arr[i])) {
                result[j] = arr[i];
                j++;
            }
        }
        return copyOf(result, 0, j);
    }

    public static void reverse(final Object[] a) {
        reverse(a, 0, a.length);
    }

    public static void reverse(final Object[] a, final int fromIndex) {
        reverse(a, fromIndex, a.length);
    }

    public static void reverse(final Object[] a, final int fromIndex, final int toIndex) {
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            swap(a, i, j);
        }
    }

    public static void swap(final Object[] a, final int i, final int j) {
        final Object tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static Object[] elementsOf(Object[] arr, int[] idx) {
        Object[] result = new Object[idx.length];
        for (int i = 0, j = 0; i < idx.length; i++)
            result[i] = arr[idx[i]];
        return result;
    }

    public static Object[] elementsOf(Object[] arr, boolean[] logical) {
        return elementsOf(arr, find(logical));
    }
    
    public static boolean[] valuesEq(Object[] arr, Object val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] == val;
        return result;
    }

    public static boolean[] valuesNeq(Object[] arr, Object val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] != val;
        return result;
    }

    public static Object[] ensureCapacity(Object[] arr, int minCap) {
        int oldCap = arr.length;
        if (minCap > oldCap) {
            int newCap = (oldCap * 3) / 2 + 1;
            if (newCap < minCap)
                newCap = minCap;
            arr = copyOf(arr, 0, newCap);
        }
        return arr;
    }
 
    public static Object mode(Object[] arr) {
        Map<Object, Integer> map = new HashMap<Object, Integer>();
        for (int i = 0; i < arr.length; i++) {
            if (map.containsKey(arr[i]))
                map.put(arr[i], map.get(arr[i]) + 1);
            else
                map.put(arr[i], 1);
        }
        int maxCount = -1;
        Object maxValue = arr[0];
        for (Object key : map.keySet()) {
            int count = map.get(key);
            if (count > maxCount) {
                maxCount = count;
                maxValue = key;
            }
        }
        return maxValue;
    }
    


    public static void permute(final Object[] a) {
        permute(a, 0, a.length);
    }

    public static void permute(final Object[] a, final int toIndex) {
        permute(a, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in 
     * lexicographic order.
     * 
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isnt the most efficient solution.
     * 
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static void permute(final Object[] a, final int fromIndex, final int toIndex) {
        if (toIndex - fromIndex < 2)
            return;

        // Find the largest index k such that a[k] < a[k + 1]. If no such index 
        // exists, the permutation is the last permutation.
        int k = toIndex - 2;
        while (k >= fromIndex && ((Comparable)a[k]).compareTo(a[k + 1])>=0)
            k--;

        if (k < fromIndex) {
            // Reached end of permutation cycle - reverse the
            // whole array and start over
            reverse(a, fromIndex, toIndex);

        } else {

            // Find the largest index l such that a[k] < a[l]. Since k + 1 is 
            // such an index, l is well defined and satisfies k < l.
            int l = toIndex - 1;
            while (l >= fromIndex && ((Comparable)a[k]).compareTo(a[l])>=0)
                l--;

            // Swap a[k] with a[l].
            swap(a, k, l);

            // Reverse the sequence from a[k + 1] up to and including the final 
            // element a[n].   
            reverse(a, k + 1, toIndex);
        }
    }

    public static Object min(Object[] values ) {
        return values[argmin(values , 0, values.length - 1)];
    }

    public static Object min(Object[] values , int fromIndex, int toIndex) {
        return values[argmin(values , fromIndex, toIndex)];
    }

    public static Object max(Object[] values ) {
        return values[argmax(values , 0, values.length - 1)];
    }

    public static Object max(Object[] values , int fromIndex, int toIndex) {
        return values[argmax(values , fromIndex, toIndex)];
    }

    public static int argmin(Object[] values ) {
        return argmin(values , 0, values.length - 1);
    }

    public static int argmin(Object[] values , int fromIndex, int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (((Comparable)values[i]).compareTo(values[argmin])<0)
                argmin = i;
        return argmin;
    }

    public static int argmax(Object[] values ) {
        return argmax(values , 0, values.length - 1);
    }

    public static int argmax(Object[] values , int fromIndex, int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (((Comparable)values[i]).compareTo(values[argmax])>0)
                argmax = i;
        return argmax;
    }

    public static int[] argminmax(Object[] values , int fromIndex, int toIndex) {
        int argmax = fromIndex;
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++) {
            if (((Comparable)values[i]).compareTo(values[argmax])>0)
                argmax = i;
            else if (((Comparable)values[i]).compareTo(values[argmin])<0)
                argmin = i;
        }
        return new int[]{argmin, argmax};
    }

    public static Object[] minmax(Object[] values ) {
        return minmax(values , 0, values.length - 1);
    }

    public static Object[] minmax(Object[] values , int fromIndex, int toIndex) {
        int[] mm = argminmax(values , fromIndex, toIndex);
        return new Object[]{values[mm[1]], values[mm[0]]};
    }

    public static void clamp(Object[] src , Object min, Object max, Object[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if (((Comparable)src[i]).compareTo( max) > 0)
                dst[i] = max;
            else if (((Comparable)src[i]).compareTo( min) < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static Object[] clamp(Object[] src , Object min, Object max) {
        Object[] dst = new Object[src.length];
        clamp(src, min, max, dst);
        return dst;
    }

    public static void clampMin(Object[] src , Object min, Object[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if (((Comparable)src[i]).compareTo( min) < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static Object[] clampMin(Object[] src , Object min) {
        Object[] dst = new Object[src.length];
        clampMin(src, min, dst);
        return dst;
    }

    public static void clampMax(Object[] src , Object max, Object[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if (((Comparable)src[i]).compareTo( max) > 0)
                dst[i] = max;
            else
                dst[i] = src[i];
        }
    }

    public static Object[] clampMax(Object[] src , Object max) {
        Object[] dst = new Object[src.length];
        clampMax(src, max, dst);
        return dst;
    }



    public static <T> void permute(final T[] a, Comparator<T> comp) {
        permute(a, comp, 0, a.length);
    }

    public static <T> void permute(final T[] a, Comparator<T> comp, final int toIndex) {
        permute(a, comp, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in 
     * lexicographic order.
     * 
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isnt the most efficient solution.
     * 
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static <T> void permute(final T[] a, Comparator<T> comp, final int fromIndex, final int toIndex) {
        if (toIndex - fromIndex < 2)
            return;

        // Find the largest index k such that a[k] < a[k + 1]. If no such index 
        // exists, the permutation is the last permutation.
        int k = toIndex - 2;
        while (k >= fromIndex && comp.compare(a[k],a[k + 1])>=0)
            k--;

        if (k < fromIndex) {
            // Reached end of permutation cycle - reverse the
            // whole array and start over
            reverse(a, fromIndex, toIndex);

        } else {

            // Find the largest index l such that a[k] < a[l]. Since k + 1 is 
            // such an index, l is well defined and satisfies k < l.
            int l = toIndex - 1;
            while (l >= fromIndex && comp.compare(a[k],a[l])>=0)
                l--;

            // Swap a[k] with a[l].
            swap(a, k, l);

            // Reverse the sequence from a[k + 1] up to and including the final 
            // element a[n].   
            reverse(a, k + 1, toIndex);
        }
    }

    public static <T> T min(T[] values , Comparator<T> comp) {
        return values[argmin(values , comp, 0, values.length - 1)];
    }

    public static <T> T min(T[] values , Comparator<T> comp, int fromIndex, int toIndex) {
        return values[argmin(values , comp, fromIndex, toIndex)];
    }

    public static <T> T max(T[] values , Comparator<T> comp) {
        return values[argmax(values , comp, 0, values.length - 1)];
    }

    public static <T> T max(T[] values , Comparator<T> comp, int fromIndex, int toIndex) {
        return values[argmax(values , comp, fromIndex, toIndex)];
    }

    public static <T> int argmin(T[] values , Comparator<T> comp) {
        return argmin(values , comp, 0, values.length - 1);
    }

    public static <T> int argmin(T[] values , Comparator<T> comp, int fromIndex, int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (comp.compare(values[i],values[argmin])<0)
                argmin = i;
        return argmin;
    }

    public static <T> int argmax(T[] values , Comparator<T> comp) {
        return argmax(values , comp, 0, values.length - 1);
    }

    public static <T> int argmax(T[] values , Comparator<T> comp, int fromIndex, int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (comp.compare(values[i],values[argmax])>0)
                argmax = i;
        return argmax;
    }

    public static <T> int[] argminmax(T[] values , Comparator<T> comp, int fromIndex, int toIndex) {
        int argmax = fromIndex;
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++) {
            if (comp.compare(values[i],values[argmax])>0)
                argmax = i;
            else if (comp.compare(values[i],values[argmin])<0)
                argmin = i;
        }
        return new int[]{argmin, argmax};
    }

    public static <T> T[] minmax(T[] values , Comparator<T> comp) {
        return minmax(values , comp, 0, values.length - 1);
    }

    public static <T> T[] minmax(T[] values , Comparator<T> comp, int fromIndex, int toIndex) {
        int[] mm = argminmax(values , comp, fromIndex, toIndex);
        return (T[])new Object[]{values[mm[1]], values[mm[0]]};
    }

    public static <T> void clamp(T[] src , Comparator<T> comp, T min, T max, T[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if (comp.compare(src[i], max) > 0)
                dst[i] = max;
            else if (comp.compare(src[i], min) < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static <T> T[] clamp(T[] src , Comparator<T> comp, T min, T max) {
        T[] dst = (T[])new Object[src.length];
        clamp(src, min, max, dst);
        return dst;
    }

    public static <T> void clampMin(T[] src , Comparator<T> comp, T min, T[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if (comp.compare(src[i], min) < 0)
                dst[i] = min;
            else
                dst[i] = src[i];
        }
    }

    public static <T> T[] clampMin(T[] src , Comparator<T> comp, T min) {
        T[] dst = (T[])new Object[src.length];
        clampMin(src, min, dst);
        return dst;
    }

    public static <T> void clampMax(T[] src , Comparator<T> comp, T max, T[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++) {
            if (comp.compare(src[i], max) > 0)
                dst[i] = max;
            else
                dst[i] = src[i];
        }
    }

    public static <T> T[] clampMax(T[] src , Comparator<T> comp, T max) {
        T[] dst = (T[])new Object[src.length];
        clampMax(src, max, dst);
        return dst;
    }


}

