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

import java.util.Comparator;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import uk.ac.susx.mlcl.lib.Checks;
import java.lang.reflect.Array;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Static utility class for symbolic manipulation of arrays.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@CheckReturnValue
@Nonnull
@SuppressWarnings("unchecked")
public final class ArrayUtil {

    private ArrayUtil() {}

    private static final class Lazy {
        private static final Random RND = new Random();
        private Lazy() {}
    }

    public static @Nonnegative int countEQ(boolean[] logical, boolean value) {
        int n = 0;
        for (boolean aLogical : logical)
            n += aLogical == value ? 1 : 0;
        return n;
    }

    public static int[] find(boolean[] logical) {
        int[] result = new int[countEQ(logical, true)];
        for (int i = 0, j = 0; i < logical.length; i++)
            if (logical[i])
                result[j++] = i;
        return result;
    }



    /*
     * ==================
     *  Type: boolean
     * ==================
     */


    /**
     * Convert an array of primitive <code>boolean</code> values to a new array of <code>Boolean</code> boxed objects.
     *
     * @param src Primitive array to convert
     * @return a new array containing the boxed equivalents of the values in the given array
     */
    public static Boolean[] box(final boolean[] src) {
        return box(src, 0, src.length);
    }

    /**
     * Convert the tail of an array of primitive <code>boolean</code values to a new array of <code>Boolean</code> boxed objects.
     *
     * @param src    Primitive array to convert
     * @param offset First index to covert (inclusive)
     * @return a new array containing the boxed equivalents of the values in the given array
     */
    public static Boolean[] box(final boolean[] src,  @Nonnegative final int offset) {
        return box(src, offset, src.length - offset);
    }

    /**
     * Convert a portion of an array of primitive <code>boolean</code values to a new array of <code>Boolean</code> boxed objects.
     *
     * @param src    Primitive array to convert
     * @param offset First index to covert (inclusive)
     * @param len    Last index to covert (exclusive)
     * @return a new array containing the boxed equivalents of the values in the given array.
     */
    public static Boolean[] box(final boolean[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        Boolean[] dst = new Boolean[len];
        for(int i = 0; i < len; i++)
            dst[i] = src[offset + i];
        return dst;
    }

    /**
     * Convert an array of <code>Boolean</code> boxed objects to a new array of primitive <code>boolean</code> values.
     *
     * @param src Boxed object array to convert
     * @return a new array containing the primitive equivalents of the values in the given array
     */
    public static boolean[] unbox(final Boolean[] src) {
        return unbox(src, 0, src.length);
    }

    /**
     * Convert the tail of an array of <code>Boolean</code> boxed objects to a new array of primitive <code>boolean</code values.
     *
     * @param src    Boxed object array to convert
     * @param offset First index to covert (inclusive)
     * @return a new array containing the primitive equivalents of the values in the given array
     */
    public static boolean[] unbox(final Boolean[] src,  @Nonnegative final int offset) {
        return unbox(src, offset, src.length - offset);
    }

    /**
     * Convert a portion of an array of <code>Boolean</code> boxed objects to a new array of primitive <code>boolean</code values.
     *
     * @param src    Boxed object array to convert
     * @param offset First index to covert (inclusive)
     * @param len    Last index to covert (exclusive)
     * @return a new array containing the primitive equivalents of the values in the given array.
     */
    public static boolean[] unbox(final Boolean[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        boolean[] dst = new boolean[len];
        for(int i = 0; i < len; i++)
            dst[i] = src[offset + i];
        return dst;
    }



    public static boolean[] copyOf(final boolean[] src) {
        return copyOf(src, 0, src.length);
    }

    public static boolean[] copyOf(final boolean[] src,  @Nonnegative final int offset) {
        return copyOf(src, offset, src.length - offset);
    }

    public static boolean[] copyOf(final boolean[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        boolean[] dst = new boolean[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    /**
     * Concatenate two or more arrays.
     *
     * @param arrays to be concatenated
     * @return array contain all the elements of the input arrays
     */
     public static boolean[] cat(final boolean[]... arrays) {
        int n = 0;
        for (boolean[] array : arrays)
            n += array.length;
        boolean[] result = (boolean[])Array.newInstance(arrays[0].getClass().getComponentType(), n);
        int offset = 0;
        for (boolean[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static int firstIndexOf(final boolean[] arr, final boolean val) {
        return firstIndexOf(arr, val, 0, arr.length);
    }

    public static int firstIndexOf(final boolean[] arr, final boolean val,  @Nonnegative int fromIndex) {
        return firstIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int firstIndexOf(final boolean[] arr, final boolean val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static int lastIndexOf(final boolean[] arr, final boolean val) {
        return lastIndexOf(arr, val, 0, arr.length);
    }

    public static int lastIndexOf(final boolean[] arr, final boolean val,  @Nonnegative int fromIndex) {
        return lastIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int lastIndexOf(final boolean[] arr, final boolean val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static boolean contains(final boolean[] arr, final boolean val) {
        return firstIndexOf(arr, val) != -1;
    }

    public static boolean contains(final boolean[] arr, final boolean val,  @Nonnegative int fromIndex) {
        return firstIndexOf(arr, val, fromIndex) != -1;
    }

    public static boolean contains(final boolean[] arr, final boolean val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return firstIndexOf(arr, val, fromIndex, toIndex) != -1;
    }

    public static boolean[] unique(final boolean... arr) {
        // This is way to slow O(n^2) - can be done in
        // O(n log n) by sorting in input first.
        boolean[] result = new boolean[arr.length];
        int j = 0;
        for (boolean anArr : arr) {
            if (!contains(result, anArr)) {
                result[j] = anArr;
                j++;
            }
        }
        return copyOf(result, 0, j);
    }

    public static void reverse(final boolean[] a) {
        reverse(a, 0, a.length);
    }

    public static void reverse(final boolean[] a,  @Nonnegative final int fromIndex) {
        reverse(a, fromIndex, a.length);
    }

    public static void reverse(final boolean[] a,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            swap(a, i, j);
        }
    }

    public static void swap(final boolean[] a,  @Nonnegative final int i,  @Nonnegative final int j) {
        final boolean tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static boolean[] elementsOf(boolean[] arr, int[] idx) {
        boolean[] result = new boolean[idx.length];
        for (int i = 0; i < idx.length; i++)
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
            return copyOf(arr, 0, newCap);
        } else {
            return arr;
        }
    }

    public static boolean mode(boolean[] arr) {
        Map<Boolean, Integer> map = new HashMap<Boolean, Integer>();
        for (boolean anArr : arr) {
            if (map.containsKey(anArr))
                map.put(anArr, map.get(anArr) + 1);
            else
                map.put(anArr, 1);
        }
        int maxCount = -1;
        Boolean maxValue = arr[0];
        for (Map.Entry<Boolean, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxValue = entry.getKey();
            }
        }
        return maxValue;
    }



    public static void permute(final boolean[] a) {
        permute(a, 0, a.length);
    }

    public static void permute(final boolean[] a,  @Nonnegative final int toIndex) {
        permute(a, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in
     * lexicographic order.
     *
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isn't the most efficient solution.
     *
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static void permute(final boolean[] a,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
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

    public static boolean min(boolean[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmin(values , fromIndex, toIndex)];
    }

    public static boolean max(boolean[] values ) {
        return values[argmax(values , 0, values.length - 1)];
    }

    public static boolean max(boolean[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmax(values , fromIndex, toIndex)];
    }

    public static int argmin(boolean[] values ) {
        return argmin(values , 0, values.length - 1);
    }

    public static int argmin(boolean[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if ((values[i]==values[argmin]?1:0)<0)
                argmin = i;
        return argmin;
    }

    public static int argmax(boolean[] values ) {
        return argmax(values , 0, values.length - 1);
    }

    public static int argmax(boolean[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if ((values[i]==values[argmax]?1:0)>0)
                argmax = i;
        return argmax;
    }

    public static int[] argminmax(boolean[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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

    public static boolean[] minmax(boolean[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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
        clamp(src , min, max, dst);
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
        clampMin(src , min, dst);
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
        clampMax(src , max, dst);
        return dst;
    }



    /*
     * ==================
     *  Type: byte
     * ==================
     */


    /**
     * Convert an array of primitive <code>byte</code> values to a new array of <code>Byte</code> boxed objects.
     *
     * @param src Primitive array to convert
     * @return a new array containing the boxed equivalents of the values in the given array
     */
    public static Byte[] box(final byte[] src) {
        return box(src, 0, src.length);
    }

    /**
     * Convert the tail of an array of primitive <code>byte</code values to a new array of <code>Byte</code> boxed objects.
     *
     * @param src    Primitive array to convert
     * @param offset First index to covert (inclusive)
     * @return a new array containing the boxed equivalents of the values in the given array
     */
    public static Byte[] box(final byte[] src,  @Nonnegative final int offset) {
        return box(src, offset, src.length - offset);
    }

    /**
     * Convert a portion of an array of primitive <code>byte</code values to a new array of <code>Byte</code> boxed objects.
     *
     * @param src    Primitive array to convert
     * @param offset First index to covert (inclusive)
     * @param len    Last index to covert (exclusive)
     * @return a new array containing the boxed equivalents of the values in the given array.
     */
    public static Byte[] box(final byte[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        Byte[] dst = new Byte[len];
        for(int i = 0; i < len; i++)
            dst[i] = src[offset + i];
        return dst;
    }

    /**
     * Convert an array of <code>Byte</code> boxed objects to a new array of primitive <code>byte</code> values.
     *
     * @param src Boxed object array to convert
     * @return a new array containing the primitive equivalents of the values in the given array
     */
    public static byte[] unbox(final Byte[] src) {
        return unbox(src, 0, src.length);
    }

    /**
     * Convert the tail of an array of <code>Byte</code> boxed objects to a new array of primitive <code>byte</code values.
     *
     * @param src    Boxed object array to convert
     * @param offset First index to covert (inclusive)
     * @return a new array containing the primitive equivalents of the values in the given array
     */
    public static byte[] unbox(final Byte[] src,  @Nonnegative final int offset) {
        return unbox(src, offset, src.length - offset);
    }

    /**
     * Convert a portion of an array of <code>Byte</code> boxed objects to a new array of primitive <code>byte</code values.
     *
     * @param src    Boxed object array to convert
     * @param offset First index to covert (inclusive)
     * @param len    Last index to covert (exclusive)
     * @return a new array containing the primitive equivalents of the values in the given array.
     */
    public static byte[] unbox(final Byte[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        byte[] dst = new byte[len];
        for(int i = 0; i < len; i++)
            dst[i] = src[offset + i];
        return dst;
    }



    public static byte[] copyOf(final byte[] src) {
        return copyOf(src, 0, src.length);
    }

    public static byte[] copyOf(final byte[] src,  @Nonnegative final int offset) {
        return copyOf(src, offset, src.length - offset);
    }

    public static byte[] copyOf(final byte[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        byte[] dst = new byte[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    /**
     * Concatenate two or more arrays.
     *
     * @param arrays to be concatenated
     * @return array contain all the elements of the input arrays
     */
     public static byte[] cat(final byte[]... arrays) {
        int n = 0;
        for (byte[] array : arrays)
            n += array.length;
        byte[] result = (byte[])Array.newInstance(arrays[0].getClass().getComponentType(), n);
        int offset = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static int firstIndexOf(final byte[] arr, final byte val) {
        return firstIndexOf(arr, val, 0, arr.length);
    }

    public static int firstIndexOf(final byte[] arr, final byte val,  @Nonnegative int fromIndex) {
        return firstIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int firstIndexOf(final byte[] arr, final byte val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static int lastIndexOf(final byte[] arr, final byte val) {
        return lastIndexOf(arr, val, 0, arr.length);
    }

    public static int lastIndexOf(final byte[] arr, final byte val,  @Nonnegative int fromIndex) {
        return lastIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int lastIndexOf(final byte[] arr, final byte val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static boolean contains(final byte[] arr, final byte val) {
        return firstIndexOf(arr, val) != -1;
    }

    public static boolean contains(final byte[] arr, final byte val,  @Nonnegative int fromIndex) {
        return firstIndexOf(arr, val, fromIndex) != -1;
    }

    public static boolean contains(final byte[] arr, final byte val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return firstIndexOf(arr, val, fromIndex, toIndex) != -1;
    }

    public static byte[] unique(final byte... arr) {
        // This is way to slow O(n^2) - can be done in
        // O(n log n) by sorting in input first.
        byte[] result = new byte[arr.length];
        int j = 0;
        for (byte anArr : arr) {
            if (!contains(result, anArr)) {
                result[j] = anArr;
                j++;
            }
        }
        return copyOf(result, 0, j);
    }

    public static void reverse(final byte[] a) {
        reverse(a, 0, a.length);
    }

    public static void reverse(final byte[] a,  @Nonnegative final int fromIndex) {
        reverse(a, fromIndex, a.length);
    }

    public static void reverse(final byte[] a,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            swap(a, i, j);
        }
    }

    public static void swap(final byte[] a,  @Nonnegative final int i,  @Nonnegative final int j) {
        final byte tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static byte[] elementsOf(byte[] arr, int[] idx) {
        byte[] result = new byte[idx.length];
        for (int i = 0; i < idx.length; i++)
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
            return copyOf(arr, 0, newCap);
        } else {
            return arr;
        }
    }

    public static byte mode(byte[] arr) {
        Map<Byte, Integer> map = new HashMap<Byte, Integer>();
        for (byte anArr : arr) {
            if (map.containsKey(anArr))
                map.put(anArr, map.get(anArr) + 1);
            else
                map.put(anArr, 1);
        }
        int maxCount = -1;
        Byte maxValue = arr[0];
        for (Map.Entry<Byte, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxValue = entry.getKey();
            }
        }
        return maxValue;
    }



    public static void permute(final byte[] a) {
        permute(a, 0, a.length);
    }

    public static void permute(final byte[] a,  @Nonnegative final int toIndex) {
        permute(a, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in
     * lexicographic order.
     *
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isn't the most efficient solution.
     *
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static void permute(final byte[] a,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
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

    public static byte min(byte[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmin(values , fromIndex, toIndex)];
    }

    public static byte max(byte[] values ) {
        return values[argmax(values , 0, values.length - 1)];
    }

    public static byte max(byte[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmax(values , fromIndex, toIndex)];
    }

    public static int argmin(byte[] values ) {
        return argmin(values , 0, values.length - 1);
    }

    public static int argmin(byte[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if ((byte)(values[i]-values[argmin])<0)
                argmin = i;
        return argmin;
    }

    public static int argmax(byte[] values ) {
        return argmax(values , 0, values.length - 1);
    }

    public static int argmax(byte[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if ((byte)(values[i]-values[argmax])>0)
                argmax = i;
        return argmax;
    }

    public static int[] argminmax(byte[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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

    public static byte[] minmax(byte[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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
        clamp(src , min, max, dst);
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
        clampMin(src , min, dst);
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
        clampMax(src , max, dst);
        return dst;
    }




    public static String toHexString(final byte[] arr) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (arr.length > 0)
            buffer.append(Integer.toHexString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            buffer.append(' ');
            buffer.append(Integer.toHexString(arr[i]));
        }
        buffer.append(']');
        return buffer.toString();
    }


    public static String toOctalString(final byte[] arr) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (arr.length > 0)
            buffer.append(Integer.toOctalString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            buffer.append(' ');
            buffer.append(Integer.toOctalString(arr[i]));
        }
        buffer.append(']');
        return buffer.toString();
    }

    public static String toBinaryString(final byte[] arr) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (arr.length > 0)
            buffer.append(Integer.toBinaryString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            buffer.append(' ');
            buffer.append(Integer.toBinaryString(arr[i]));
        }
        buffer.append(']');
        return buffer.toString();
    }



    /*
     * ==================
     *  Type: char
     * ==================
     */


    /**
     * Convert an array of primitive <code>char</code> values to a new array of <code>Character</code> boxed objects.
     *
     * @param src Primitive array to convert
     * @return a new array containing the boxed equivalents of the values in the given array
     */
    public static Character[] box(final char[] src) {
        return box(src, 0, src.length);
    }

    /**
     * Convert the tail of an array of primitive <code>char</code values to a new array of <code>Character</code> boxed objects.
     *
     * @param src    Primitive array to convert
     * @param offset First index to covert (inclusive)
     * @return a new array containing the boxed equivalents of the values in the given array
     */
    public static Character[] box(final char[] src,  @Nonnegative final int offset) {
        return box(src, offset, src.length - offset);
    }

    /**
     * Convert a portion of an array of primitive <code>char</code values to a new array of <code>Character</code> boxed objects.
     *
     * @param src    Primitive array to convert
     * @param offset First index to covert (inclusive)
     * @param len    Last index to covert (exclusive)
     * @return a new array containing the boxed equivalents of the values in the given array.
     */
    public static Character[] box(final char[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        Character[] dst = new Character[len];
        for(int i = 0; i < len; i++)
            dst[i] = src[offset + i];
        return dst;
    }

    /**
     * Convert an array of <code>Character</code> boxed objects to a new array of primitive <code>char</code> values.
     *
     * @param src Boxed object array to convert
     * @return a new array containing the primitive equivalents of the values in the given array
     */
    public static char[] unbox(final Character[] src) {
        return unbox(src, 0, src.length);
    }

    /**
     * Convert the tail of an array of <code>Character</code> boxed objects to a new array of primitive <code>char</code values.
     *
     * @param src    Boxed object array to convert
     * @param offset First index to covert (inclusive)
     * @return a new array containing the primitive equivalents of the values in the given array
     */
    public static char[] unbox(final Character[] src,  @Nonnegative final int offset) {
        return unbox(src, offset, src.length - offset);
    }

    /**
     * Convert a portion of an array of <code>Character</code> boxed objects to a new array of primitive <code>char</code values.
     *
     * @param src    Boxed object array to convert
     * @param offset First index to covert (inclusive)
     * @param len    Last index to covert (exclusive)
     * @return a new array containing the primitive equivalents of the values in the given array.
     */
    public static char[] unbox(final Character[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        char[] dst = new char[len];
        for(int i = 0; i < len; i++)
            dst[i] = src[offset + i];
        return dst;
    }



    public static char[] copyOf(final char[] src) {
        return copyOf(src, 0, src.length);
    }

    public static char[] copyOf(final char[] src,  @Nonnegative final int offset) {
        return copyOf(src, offset, src.length - offset);
    }

    public static char[] copyOf(final char[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        char[] dst = new char[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    /**
     * Concatenate two or more arrays.
     *
     * @param arrays to be concatenated
     * @return array contain all the elements of the input arrays
     */
     public static char[] cat(final char[]... arrays) {
        int n = 0;
        for (char[] array : arrays)
            n += array.length;
        char[] result = (char[])Array.newInstance(arrays[0].getClass().getComponentType(), n);
        int offset = 0;
        for (char[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static int firstIndexOf(final char[] arr, final char val) {
        return firstIndexOf(arr, val, 0, arr.length);
    }

    public static int firstIndexOf(final char[] arr, final char val,  @Nonnegative int fromIndex) {
        return firstIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int firstIndexOf(final char[] arr, final char val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static int lastIndexOf(final char[] arr, final char val) {
        return lastIndexOf(arr, val, 0, arr.length);
    }

    public static int lastIndexOf(final char[] arr, final char val,  @Nonnegative int fromIndex) {
        return lastIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int lastIndexOf(final char[] arr, final char val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static boolean contains(final char[] arr, final char val) {
        return firstIndexOf(arr, val) != -1;
    }

    public static boolean contains(final char[] arr, final char val,  @Nonnegative int fromIndex) {
        return firstIndexOf(arr, val, fromIndex) != -1;
    }

    public static boolean contains(final char[] arr, final char val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return firstIndexOf(arr, val, fromIndex, toIndex) != -1;
    }

    public static char[] unique(final char... arr) {
        // This is way to slow O(n^2) - can be done in
        // O(n log n) by sorting in input first.
        char[] result = new char[arr.length];
        int j = 0;
        for (char anArr : arr) {
            if (!contains(result, anArr)) {
                result[j] = anArr;
                j++;
            }
        }
        return copyOf(result, 0, j);
    }

    public static void reverse(final char[] a) {
        reverse(a, 0, a.length);
    }

    public static void reverse(final char[] a,  @Nonnegative final int fromIndex) {
        reverse(a, fromIndex, a.length);
    }

    public static void reverse(final char[] a,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            swap(a, i, j);
        }
    }

    public static void swap(final char[] a,  @Nonnegative final int i,  @Nonnegative final int j) {
        final char tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static char[] elementsOf(char[] arr, int[] idx) {
        char[] result = new char[idx.length];
        for (int i = 0; i < idx.length; i++)
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
            return copyOf(arr, 0, newCap);
        } else {
            return arr;
        }
    }

    public static char mode(char[] arr) {
        Map<Character, Integer> map = new HashMap<Character, Integer>();
        for (char anArr : arr) {
            if (map.containsKey(anArr))
                map.put(anArr, map.get(anArr) + 1);
            else
                map.put(anArr, 1);
        }
        int maxCount = -1;
        Character maxValue = arr[0];
        for (Map.Entry<Character, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxValue = entry.getKey();
            }
        }
        return maxValue;
    }



    public static void permute(final char[] a) {
        permute(a, 0, a.length);
    }

    public static void permute(final char[] a,  @Nonnegative final int toIndex) {
        permute(a, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in
     * lexicographic order.
     *
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isn't the most efficient solution.
     *
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static void permute(final char[] a,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
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

    public static char min(char[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmin(values , fromIndex, toIndex)];
    }

    public static char max(char[] values ) {
        return values[argmax(values , 0, values.length - 1)];
    }

    public static char max(char[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmax(values , fromIndex, toIndex)];
    }

    public static int argmin(char[] values ) {
        return argmin(values , 0, values.length - 1);
    }

    public static int argmin(char[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if ((char)(values[i]-values[argmin])<0)
                argmin = i;
        return argmin;
    }

    public static int argmax(char[] values ) {
        return argmax(values , 0, values.length - 1);
    }

    public static int argmax(char[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if ((char)(values[i]-values[argmax])>0)
                argmax = i;
        return argmax;
    }

    public static int[] argminmax(char[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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

    public static char[] minmax(char[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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
        clamp(src , min, max, dst);
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
        clampMin(src , min, dst);
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
        clampMax(src , max, dst);
        return dst;
    }




    public static String toHexString(final char[] arr) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (arr.length > 0)
            buffer.append(Integer.toHexString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            buffer.append(' ');
            buffer.append(Integer.toHexString(arr[i]));
        }
        buffer.append(']');
        return buffer.toString();
    }


    public static String toOctalString(final char[] arr) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (arr.length > 0)
            buffer.append(Integer.toOctalString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            buffer.append(' ');
            buffer.append(Integer.toOctalString(arr[i]));
        }
        buffer.append(']');
        return buffer.toString();
    }

    public static String toBinaryString(final char[] arr) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (arr.length > 0)
            buffer.append(Integer.toBinaryString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            buffer.append(' ');
            buffer.append(Integer.toBinaryString(arr[i]));
        }
        buffer.append(']');
        return buffer.toString();
    }



    /*
     * ==================
     *  Type: short
     * ==================
     */


    /**
     * Convert an array of primitive <code>short</code> values to a new array of <code>Short</code> boxed objects.
     *
     * @param src Primitive array to convert
     * @return a new array containing the boxed equivalents of the values in the given array
     */
    public static Short[] box(final short[] src) {
        return box(src, 0, src.length);
    }

    /**
     * Convert the tail of an array of primitive <code>short</code values to a new array of <code>Short</code> boxed objects.
     *
     * @param src    Primitive array to convert
     * @param offset First index to covert (inclusive)
     * @return a new array containing the boxed equivalents of the values in the given array
     */
    public static Short[] box(final short[] src,  @Nonnegative final int offset) {
        return box(src, offset, src.length - offset);
    }

    /**
     * Convert a portion of an array of primitive <code>short</code values to a new array of <code>Short</code> boxed objects.
     *
     * @param src    Primitive array to convert
     * @param offset First index to covert (inclusive)
     * @param len    Last index to covert (exclusive)
     * @return a new array containing the boxed equivalents of the values in the given array.
     */
    public static Short[] box(final short[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        Short[] dst = new Short[len];
        for(int i = 0; i < len; i++)
            dst[i] = src[offset + i];
        return dst;
    }

    /**
     * Convert an array of <code>Short</code> boxed objects to a new array of primitive <code>short</code> values.
     *
     * @param src Boxed object array to convert
     * @return a new array containing the primitive equivalents of the values in the given array
     */
    public static short[] unbox(final Short[] src) {
        return unbox(src, 0, src.length);
    }

    /**
     * Convert the tail of an array of <code>Short</code> boxed objects to a new array of primitive <code>short</code values.
     *
     * @param src    Boxed object array to convert
     * @param offset First index to covert (inclusive)
     * @return a new array containing the primitive equivalents of the values in the given array
     */
    public static short[] unbox(final Short[] src,  @Nonnegative final int offset) {
        return unbox(src, offset, src.length - offset);
    }

    /**
     * Convert a portion of an array of <code>Short</code> boxed objects to a new array of primitive <code>short</code values.
     *
     * @param src    Boxed object array to convert
     * @param offset First index to covert (inclusive)
     * @param len    Last index to covert (exclusive)
     * @return a new array containing the primitive equivalents of the values in the given array.
     */
    public static short[] unbox(final Short[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        short[] dst = new short[len];
        for(int i = 0; i < len; i++)
            dst[i] = src[offset + i];
        return dst;
    }



    public static short[] copyOf(final short[] src) {
        return copyOf(src, 0, src.length);
    }

    public static short[] copyOf(final short[] src,  @Nonnegative final int offset) {
        return copyOf(src, offset, src.length - offset);
    }

    public static short[] copyOf(final short[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        short[] dst = new short[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    /**
     * Concatenate two or more arrays.
     *
     * @param arrays to be concatenated
     * @return array contain all the elements of the input arrays
     */
     public static short[] cat(final short[]... arrays) {
        int n = 0;
        for (short[] array : arrays)
            n += array.length;
        short[] result = (short[])Array.newInstance(arrays[0].getClass().getComponentType(), n);
        int offset = 0;
        for (short[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static int firstIndexOf(final short[] arr, final short val) {
        return firstIndexOf(arr, val, 0, arr.length);
    }

    public static int firstIndexOf(final short[] arr, final short val,  @Nonnegative int fromIndex) {
        return firstIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int firstIndexOf(final short[] arr, final short val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static int lastIndexOf(final short[] arr, final short val) {
        return lastIndexOf(arr, val, 0, arr.length);
    }

    public static int lastIndexOf(final short[] arr, final short val,  @Nonnegative int fromIndex) {
        return lastIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int lastIndexOf(final short[] arr, final short val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static boolean contains(final short[] arr, final short val) {
        return firstIndexOf(arr, val) != -1;
    }

    public static boolean contains(final short[] arr, final short val,  @Nonnegative int fromIndex) {
        return firstIndexOf(arr, val, fromIndex) != -1;
    }

    public static boolean contains(final short[] arr, final short val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return firstIndexOf(arr, val, fromIndex, toIndex) != -1;
    }

    public static short[] unique(final short... arr) {
        // This is way to slow O(n^2) - can be done in
        // O(n log n) by sorting in input first.
        short[] result = new short[arr.length];
        int j = 0;
        for (short anArr : arr) {
            if (!contains(result, anArr)) {
                result[j] = anArr;
                j++;
            }
        }
        return copyOf(result, 0, j);
    }

    public static void reverse(final short[] a) {
        reverse(a, 0, a.length);
    }

    public static void reverse(final short[] a,  @Nonnegative final int fromIndex) {
        reverse(a, fromIndex, a.length);
    }

    public static void reverse(final short[] a,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            swap(a, i, j);
        }
    }

    public static void swap(final short[] a,  @Nonnegative final int i,  @Nonnegative final int j) {
        final short tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static short[] elementsOf(short[] arr, int[] idx) {
        short[] result = new short[idx.length];
        for (int i = 0; i < idx.length; i++)
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
            return copyOf(arr, 0, newCap);
        } else {
            return arr;
        }
    }

    public static short mode(short[] arr) {
        Map<Short, Integer> map = new HashMap<Short, Integer>();
        for (short anArr : arr) {
            if (map.containsKey(anArr))
                map.put(anArr, map.get(anArr) + 1);
            else
                map.put(anArr, 1);
        }
        int maxCount = -1;
        Short maxValue = arr[0];
        for (Map.Entry<Short, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxValue = entry.getKey();
            }
        }
        return maxValue;
    }



    public static void permute(final short[] a) {
        permute(a, 0, a.length);
    }

    public static void permute(final short[] a,  @Nonnegative final int toIndex) {
        permute(a, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in
     * lexicographic order.
     *
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isn't the most efficient solution.
     *
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static void permute(final short[] a,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
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

    public static short min(short[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmin(values , fromIndex, toIndex)];
    }

    public static short max(short[] values ) {
        return values[argmax(values , 0, values.length - 1)];
    }

    public static short max(short[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmax(values , fromIndex, toIndex)];
    }

    public static int argmin(short[] values ) {
        return argmin(values , 0, values.length - 1);
    }

    public static int argmin(short[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if ((short)(values[i]-values[argmin])<0)
                argmin = i;
        return argmin;
    }

    public static int argmax(short[] values ) {
        return argmax(values , 0, values.length - 1);
    }

    public static int argmax(short[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if ((short)(values[i]-values[argmax])>0)
                argmax = i;
        return argmax;
    }

    public static int[] argminmax(short[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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

    public static short[] minmax(short[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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
        clamp(src , min, max, dst);
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
        clampMin(src , min, dst);
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
        clampMax(src , max, dst);
        return dst;
    }




    public static String toHexString(final short[] arr) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (arr.length > 0)
            buffer.append(Integer.toHexString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            buffer.append(' ');
            buffer.append(Integer.toHexString(arr[i]));
        }
        buffer.append(']');
        return buffer.toString();
    }


    public static String toOctalString(final short[] arr) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (arr.length > 0)
            buffer.append(Integer.toOctalString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            buffer.append(' ');
            buffer.append(Integer.toOctalString(arr[i]));
        }
        buffer.append(']');
        return buffer.toString();
    }

    public static String toBinaryString(final short[] arr) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (arr.length > 0)
            buffer.append(Integer.toBinaryString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            buffer.append(' ');
            buffer.append(Integer.toBinaryString(arr[i]));
        }
        buffer.append(']');
        return buffer.toString();
    }



    /*
     * ==================
     *  Type: int
     * ==================
     */


    /**
     * Convert an array of primitive <code>int</code> values to a new array of <code>Integer</code> boxed objects.
     *
     * @param src Primitive array to convert
     * @return a new array containing the boxed equivalents of the values in the given array
     */
    public static Integer[] box(final int[] src) {
        return box(src, 0, src.length);
    }

    /**
     * Convert the tail of an array of primitive <code>int</code values to a new array of <code>Integer</code> boxed objects.
     *
     * @param src    Primitive array to convert
     * @param offset First index to covert (inclusive)
     * @return a new array containing the boxed equivalents of the values in the given array
     */
    public static Integer[] box(final int[] src,  @Nonnegative final int offset) {
        return box(src, offset, src.length - offset);
    }

    /**
     * Convert a portion of an array of primitive <code>int</code values to a new array of <code>Integer</code> boxed objects.
     *
     * @param src    Primitive array to convert
     * @param offset First index to covert (inclusive)
     * @param len    Last index to covert (exclusive)
     * @return a new array containing the boxed equivalents of the values in the given array.
     */
    public static Integer[] box(final int[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        Integer[] dst = new Integer[len];
        for(int i = 0; i < len; i++)
            dst[i] = src[offset + i];
        return dst;
    }

    /**
     * Convert an array of <code>Integer</code> boxed objects to a new array of primitive <code>int</code> values.
     *
     * @param src Boxed object array to convert
     * @return a new array containing the primitive equivalents of the values in the given array
     */
    public static int[] unbox(final Integer[] src) {
        return unbox(src, 0, src.length);
    }

    /**
     * Convert the tail of an array of <code>Integer</code> boxed objects to a new array of primitive <code>int</code values.
     *
     * @param src    Boxed object array to convert
     * @param offset First index to covert (inclusive)
     * @return a new array containing the primitive equivalents of the values in the given array
     */
    public static int[] unbox(final Integer[] src,  @Nonnegative final int offset) {
        return unbox(src, offset, src.length - offset);
    }

    /**
     * Convert a portion of an array of <code>Integer</code> boxed objects to a new array of primitive <code>int</code values.
     *
     * @param src    Boxed object array to convert
     * @param offset First index to covert (inclusive)
     * @param len    Last index to covert (exclusive)
     * @return a new array containing the primitive equivalents of the values in the given array.
     */
    public static int[] unbox(final Integer[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        int[] dst = new int[len];
        for(int i = 0; i < len; i++)
            dst[i] = src[offset + i];
        return dst;
    }



    public static int[] copyOf(final int[] src) {
        return copyOf(src, 0, src.length);
    }

    public static int[] copyOf(final int[] src,  @Nonnegative final int offset) {
        return copyOf(src, offset, src.length - offset);
    }

    public static int[] copyOf(final int[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        int[] dst = new int[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    /**
     * Concatenate two or more arrays.
     *
     * @param arrays to be concatenated
     * @return array contain all the elements of the input arrays
     */
     public static int[] cat(final int[]... arrays) {
        int n = 0;
        for (int[] array : arrays)
            n += array.length;
        int[] result = (int[])Array.newInstance(arrays[0].getClass().getComponentType(), n);
        int offset = 0;
        for (int[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static int firstIndexOf(final int[] arr, final int val) {
        return firstIndexOf(arr, val, 0, arr.length);
    }

    public static int firstIndexOf(final int[] arr, final int val,  @Nonnegative int fromIndex) {
        return firstIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int firstIndexOf(final int[] arr, final int val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static int lastIndexOf(final int[] arr, final int val) {
        return lastIndexOf(arr, val, 0, arr.length);
    }

    public static int lastIndexOf(final int[] arr, final int val,  @Nonnegative int fromIndex) {
        return lastIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int lastIndexOf(final int[] arr, final int val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static boolean contains(final int[] arr, final int val) {
        return firstIndexOf(arr, val) != -1;
    }

    public static boolean contains(final int[] arr, final int val,  @Nonnegative int fromIndex) {
        return firstIndexOf(arr, val, fromIndex) != -1;
    }

    public static boolean contains(final int[] arr, final int val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return firstIndexOf(arr, val, fromIndex, toIndex) != -1;
    }

    public static int[] unique(final int... arr) {
        // This is way to slow O(n^2) - can be done in
        // O(n log n) by sorting in input first.
        int[] result = new int[arr.length];
        int j = 0;
        for (int anArr : arr) {
            if (!contains(result, anArr)) {
                result[j] = anArr;
                j++;
            }
        }
        return copyOf(result, 0, j);
    }

    public static void reverse(final int[] a) {
        reverse(a, 0, a.length);
    }

    public static void reverse(final int[] a,  @Nonnegative final int fromIndex) {
        reverse(a, fromIndex, a.length);
    }

    public static void reverse(final int[] a,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            swap(a, i, j);
        }
    }

    public static void swap(final int[] a,  @Nonnegative final int i,  @Nonnegative final int j) {
        final int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static int[] elementsOf(int[] arr, int[] idx) {
        int[] result = new int[idx.length];
        for (int i = 0; i < idx.length; i++)
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
            return copyOf(arr, 0, newCap);
        } else {
            return arr;
        }
    }

    public static int mode(int[] arr) {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int anArr : arr) {
            if (map.containsKey(anArr))
                map.put(anArr, map.get(anArr) + 1);
            else
                map.put(anArr, 1);
        }
        int maxCount = -1;
        Integer maxValue = arr[0];
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxValue = entry.getKey();
            }
        }
        return maxValue;
    }



    public static void permute(final int[] a) {
        permute(a, 0, a.length);
    }

    public static void permute(final int[] a,  @Nonnegative final int toIndex) {
        permute(a, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in
     * lexicographic order.
     *
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isn't the most efficient solution.
     *
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static void permute(final int[] a,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
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

    public static int min(int[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmin(values , fromIndex, toIndex)];
    }

    public static int max(int[] values ) {
        return values[argmax(values , 0, values.length - 1)];
    }

    public static int max(int[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmax(values , fromIndex, toIndex)];
    }

    public static int argmin(int[] values ) {
        return argmin(values , 0, values.length - 1);
    }

    public static int argmin(int[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (values[i]-values[argmin]<0)
                argmin = i;
        return argmin;
    }

    public static int argmax(int[] values ) {
        return argmax(values , 0, values.length - 1);
    }

    public static int argmax(int[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (values[i]-values[argmax]>0)
                argmax = i;
        return argmax;
    }

    public static int[] argminmax(int[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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

    public static int[] minmax(int[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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
        clamp(src , min, max, dst);
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
        clampMin(src , min, dst);
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
        clampMax(src , max, dst);
        return dst;
    }




    public static String toHexString(final int[] arr) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (arr.length > 0)
            buffer.append(Integer.toHexString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            buffer.append(' ');
            buffer.append(Integer.toHexString(arr[i]));
        }
        buffer.append(']');
        return buffer.toString();
    }


    public static String toOctalString(final int[] arr) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (arr.length > 0)
            buffer.append(Integer.toOctalString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            buffer.append(' ');
            buffer.append(Integer.toOctalString(arr[i]));
        }
        buffer.append(']');
        return buffer.toString();
    }

    public static String toBinaryString(final int[] arr) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (arr.length > 0)
            buffer.append(Integer.toBinaryString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            buffer.append(' ');
            buffer.append(Integer.toBinaryString(arr[i]));
        }
        buffer.append(']');
        return buffer.toString();
    }



    /*
     * ==================
     *  Type: long
     * ==================
     */


    /**
     * Convert an array of primitive <code>long</code> values to a new array of <code>Long</code> boxed objects.
     *
     * @param src Primitive array to convert
     * @return a new array containing the boxed equivalents of the values in the given array
     */
    public static Long[] box(final long[] src) {
        return box(src, 0, src.length);
    }

    /**
     * Convert the tail of an array of primitive <code>long</code values to a new array of <code>Long</code> boxed objects.
     *
     * @param src    Primitive array to convert
     * @param offset First index to covert (inclusive)
     * @return a new array containing the boxed equivalents of the values in the given array
     */
    public static Long[] box(final long[] src,  @Nonnegative final int offset) {
        return box(src, offset, src.length - offset);
    }

    /**
     * Convert a portion of an array of primitive <code>long</code values to a new array of <code>Long</code> boxed objects.
     *
     * @param src    Primitive array to convert
     * @param offset First index to covert (inclusive)
     * @param len    Last index to covert (exclusive)
     * @return a new array containing the boxed equivalents of the values in the given array.
     */
    public static Long[] box(final long[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        Long[] dst = new Long[len];
        for(int i = 0; i < len; i++)
            dst[i] = src[offset + i];
        return dst;
    }

    /**
     * Convert an array of <code>Long</code> boxed objects to a new array of primitive <code>long</code> values.
     *
     * @param src Boxed object array to convert
     * @return a new array containing the primitive equivalents of the values in the given array
     */
    public static long[] unbox(final Long[] src) {
        return unbox(src, 0, src.length);
    }

    /**
     * Convert the tail of an array of <code>Long</code> boxed objects to a new array of primitive <code>long</code values.
     *
     * @param src    Boxed object array to convert
     * @param offset First index to covert (inclusive)
     * @return a new array containing the primitive equivalents of the values in the given array
     */
    public static long[] unbox(final Long[] src,  @Nonnegative final int offset) {
        return unbox(src, offset, src.length - offset);
    }

    /**
     * Convert a portion of an array of <code>Long</code> boxed objects to a new array of primitive <code>long</code values.
     *
     * @param src    Boxed object array to convert
     * @param offset First index to covert (inclusive)
     * @param len    Last index to covert (exclusive)
     * @return a new array containing the primitive equivalents of the values in the given array.
     */
    public static long[] unbox(final Long[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        long[] dst = new long[len];
        for(int i = 0; i < len; i++)
            dst[i] = src[offset + i];
        return dst;
    }



    public static long[] copyOf(final long[] src) {
        return copyOf(src, 0, src.length);
    }

    public static long[] copyOf(final long[] src,  @Nonnegative final int offset) {
        return copyOf(src, offset, src.length - offset);
    }

    public static long[] copyOf(final long[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        long[] dst = new long[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    /**
     * Concatenate two or more arrays.
     *
     * @param arrays to be concatenated
     * @return array contain all the elements of the input arrays
     */
     public static long[] cat(final long[]... arrays) {
        int n = 0;
        for (long[] array : arrays)
            n += array.length;
        long[] result = (long[])Array.newInstance(arrays[0].getClass().getComponentType(), n);
        int offset = 0;
        for (long[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static int firstIndexOf(final long[] arr, final long val) {
        return firstIndexOf(arr, val, 0, arr.length);
    }

    public static int firstIndexOf(final long[] arr, final long val,  @Nonnegative int fromIndex) {
        return firstIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int firstIndexOf(final long[] arr, final long val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static int lastIndexOf(final long[] arr, final long val) {
        return lastIndexOf(arr, val, 0, arr.length);
    }

    public static int lastIndexOf(final long[] arr, final long val,  @Nonnegative int fromIndex) {
        return lastIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int lastIndexOf(final long[] arr, final long val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static boolean contains(final long[] arr, final long val) {
        return firstIndexOf(arr, val) != -1;
    }

    public static boolean contains(final long[] arr, final long val,  @Nonnegative int fromIndex) {
        return firstIndexOf(arr, val, fromIndex) != -1;
    }

    public static boolean contains(final long[] arr, final long val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return firstIndexOf(arr, val, fromIndex, toIndex) != -1;
    }

    public static long[] unique(final long... arr) {
        // This is way to slow O(n^2) - can be done in
        // O(n log n) by sorting in input first.
        long[] result = new long[arr.length];
        int j = 0;
        for (long anArr : arr) {
            if (!contains(result, anArr)) {
                result[j] = anArr;
                j++;
            }
        }
        return copyOf(result, 0, j);
    }

    public static void reverse(final long[] a) {
        reverse(a, 0, a.length);
    }

    public static void reverse(final long[] a,  @Nonnegative final int fromIndex) {
        reverse(a, fromIndex, a.length);
    }

    public static void reverse(final long[] a,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            swap(a, i, j);
        }
    }

    public static void swap(final long[] a,  @Nonnegative final int i,  @Nonnegative final int j) {
        final long tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static long[] elementsOf(long[] arr, int[] idx) {
        long[] result = new long[idx.length];
        for (int i = 0; i < idx.length; i++)
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
            return copyOf(arr, 0, newCap);
        } else {
            return arr;
        }
    }

    public static long mode(long[] arr) {
        Map<Long, Integer> map = new HashMap<Long, Integer>();
        for (long anArr : arr) {
            if (map.containsKey(anArr))
                map.put(anArr, map.get(anArr) + 1);
            else
                map.put(anArr, 1);
        }
        int maxCount = -1;
        Long maxValue = arr[0];
        for (Map.Entry<Long, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxValue = entry.getKey();
            }
        }
        return maxValue;
    }



    public static void permute(final long[] a) {
        permute(a, 0, a.length);
    }

    public static void permute(final long[] a,  @Nonnegative final int toIndex) {
        permute(a, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in
     * lexicographic order.
     *
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isn't the most efficient solution.
     *
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static void permute(final long[] a,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
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

    public static long min(long[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmin(values , fromIndex, toIndex)];
    }

    public static long max(long[] values ) {
        return values[argmax(values , 0, values.length - 1)];
    }

    public static long max(long[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmax(values , fromIndex, toIndex)];
    }

    public static int argmin(long[] values ) {
        return argmin(values , 0, values.length - 1);
    }

    public static int argmin(long[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (values[i]-values[argmin]<0)
                argmin = i;
        return argmin;
    }

    public static int argmax(long[] values ) {
        return argmax(values , 0, values.length - 1);
    }

    public static int argmax(long[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (values[i]-values[argmax]>0)
                argmax = i;
        return argmax;
    }

    public static int[] argminmax(long[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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

    public static long[] minmax(long[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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
        clamp(src , min, max, dst);
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
        clampMin(src , min, dst);
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
        clampMax(src , max, dst);
        return dst;
    }




    public static String toHexString(final long[] arr) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (arr.length > 0)
            buffer.append(Long.toHexString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            buffer.append(' ');
            buffer.append(Long.toHexString(arr[i]));
        }
        buffer.append(']');
        return buffer.toString();
    }


    public static String toOctalString(final long[] arr) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (arr.length > 0)
            buffer.append(Long.toOctalString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            buffer.append(' ');
            buffer.append(Long.toOctalString(arr[i]));
        }
        buffer.append(']');
        return buffer.toString();
    }

    public static String toBinaryString(final long[] arr) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (arr.length > 0)
            buffer.append(Long.toBinaryString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            buffer.append(' ');
            buffer.append(Long.toBinaryString(arr[i]));
        }
        buffer.append(']');
        return buffer.toString();
    }



    /*
     * ==================
     *  Type: float
     * ==================
     */


    /**
     * Convert an array of primitive <code>float</code> values to a new array of <code>Float</code> boxed objects.
     *
     * @param src Primitive array to convert
     * @return a new array containing the boxed equivalents of the values in the given array
     */
    public static Float[] box(final float[] src) {
        return box(src, 0, src.length);
    }

    /**
     * Convert the tail of an array of primitive <code>float</code values to a new array of <code>Float</code> boxed objects.
     *
     * @param src    Primitive array to convert
     * @param offset First index to covert (inclusive)
     * @return a new array containing the boxed equivalents of the values in the given array
     */
    public static Float[] box(final float[] src,  @Nonnegative final int offset) {
        return box(src, offset, src.length - offset);
    }

    /**
     * Convert a portion of an array of primitive <code>float</code values to a new array of <code>Float</code> boxed objects.
     *
     * @param src    Primitive array to convert
     * @param offset First index to covert (inclusive)
     * @param len    Last index to covert (exclusive)
     * @return a new array containing the boxed equivalents of the values in the given array.
     */
    public static Float[] box(final float[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        Float[] dst = new Float[len];
        for(int i = 0; i < len; i++)
            dst[i] = src[offset + i];
        return dst;
    }

    /**
     * Convert an array of <code>Float</code> boxed objects to a new array of primitive <code>float</code> values.
     *
     * @param src Boxed object array to convert
     * @return a new array containing the primitive equivalents of the values in the given array
     */
    public static float[] unbox(final Float[] src) {
        return unbox(src, 0, src.length);
    }

    /**
     * Convert the tail of an array of <code>Float</code> boxed objects to a new array of primitive <code>float</code values.
     *
     * @param src    Boxed object array to convert
     * @param offset First index to covert (inclusive)
     * @return a new array containing the primitive equivalents of the values in the given array
     */
    public static float[] unbox(final Float[] src,  @Nonnegative final int offset) {
        return unbox(src, offset, src.length - offset);
    }

    /**
     * Convert a portion of an array of <code>Float</code> boxed objects to a new array of primitive <code>float</code values.
     *
     * @param src    Boxed object array to convert
     * @param offset First index to covert (inclusive)
     * @param len    Last index to covert (exclusive)
     * @return a new array containing the primitive equivalents of the values in the given array.
     */
    public static float[] unbox(final Float[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        float[] dst = new float[len];
        for(int i = 0; i < len; i++)
            dst[i] = src[offset + i];
        return dst;
    }



    public static float[] copyOf(final float[] src) {
        return copyOf(src, 0, src.length);
    }

    public static float[] copyOf(final float[] src,  @Nonnegative final int offset) {
        return copyOf(src, offset, src.length - offset);
    }

    public static float[] copyOf(final float[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        float[] dst = new float[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    /**
     * Concatenate two or more arrays.
     *
     * @param arrays to be concatenated
     * @return array contain all the elements of the input arrays
     */
     public static float[] cat(final float[]... arrays) {
        int n = 0;
        for (float[] array : arrays)
            n += array.length;
        float[] result = (float[])Array.newInstance(arrays[0].getClass().getComponentType(), n);
        int offset = 0;
        for (float[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static int firstIndexOf(final float[] arr, final float val) {
        return firstIndexOf(arr, val, 0, arr.length);
    }

    public static int firstIndexOf(final float[] arr, final float val,  @Nonnegative int fromIndex) {
        return firstIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int firstIndexOf(final float[] arr, final float val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static int lastIndexOf(final float[] arr, final float val) {
        return lastIndexOf(arr, val, 0, arr.length);
    }

    public static int lastIndexOf(final float[] arr, final float val,  @Nonnegative int fromIndex) {
        return lastIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int lastIndexOf(final float[] arr, final float val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static boolean contains(final float[] arr, final float val) {
        return firstIndexOf(arr, val) != -1;
    }

    public static boolean contains(final float[] arr, final float val,  @Nonnegative int fromIndex) {
        return firstIndexOf(arr, val, fromIndex) != -1;
    }

    public static boolean contains(final float[] arr, final float val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return firstIndexOf(arr, val, fromIndex, toIndex) != -1;
    }

    public static float[] unique(final float... arr) {
        // This is way to slow O(n^2) - can be done in
        // O(n log n) by sorting in input first.
        float[] result = new float[arr.length];
        int j = 0;
        for (float anArr : arr) {
            if (!contains(result, anArr)) {
                result[j] = anArr;
                j++;
            }
        }
        return copyOf(result, 0, j);
    }

    public static void reverse(final float[] a) {
        reverse(a, 0, a.length);
    }

    public static void reverse(final float[] a,  @Nonnegative final int fromIndex) {
        reverse(a, fromIndex, a.length);
    }

    public static void reverse(final float[] a,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            swap(a, i, j);
        }
    }

    public static void swap(final float[] a,  @Nonnegative final int i,  @Nonnegative final int j) {
        final float tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static float[] elementsOf(float[] arr, int[] idx) {
        float[] result = new float[idx.length];
        for (int i = 0; i < idx.length; i++)
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
            return copyOf(arr, 0, newCap);
        } else {
            return arr;
        }
    }

    public static float mode(float[] arr) {
        Map<Float, Integer> map = new HashMap<Float, Integer>();
        for (float anArr : arr) {
            if (map.containsKey(anArr))
                map.put(anArr, map.get(anArr) + 1);
            else
                map.put(anArr, 1);
        }
        int maxCount = -1;
        Float maxValue = arr[0];
        for (Map.Entry<Float, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxValue = entry.getKey();
            }
        }
        return maxValue;
    }



    public static void permute(final float[] a) {
        permute(a, 0, a.length);
    }

    public static void permute(final float[] a,  @Nonnegative final int toIndex) {
        permute(a, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in
     * lexicographic order.
     *
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isn't the most efficient solution.
     *
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static void permute(final float[] a,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
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

    public static float min(float[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmin(values , fromIndex, toIndex)];
    }

    public static float max(float[] values ) {
        return values[argmax(values , 0, values.length - 1)];
    }

    public static float max(float[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmax(values , fromIndex, toIndex)];
    }

    public static int argmin(float[] values ) {
        return argmin(values , 0, values.length - 1);
    }

    public static int argmin(float[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (values[i]-values[argmin]<0)
                argmin = i;
        return argmin;
    }

    public static int argmax(float[] values ) {
        return argmax(values , 0, values.length - 1);
    }

    public static int argmax(float[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (values[i]-values[argmax]>0)
                argmax = i;
        return argmax;
    }

    public static int[] argminmax(float[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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

    public static float[] minmax(float[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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
        clamp(src , min, max, dst);
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
        clampMin(src , min, dst);
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
        clampMax(src , max, dst);
        return dst;
    }







    public static String toHexString(final float[] arr) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (arr.length > 0)
            buffer.append(Float.toHexString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            buffer.append(' ');
            buffer.append(Float.toHexString(arr[i]));
        }
        buffer.append(']');
        return buffer.toString();
    }



    /*
     * ==================
     *  Type: double
     * ==================
     */


    /**
     * Convert an array of primitive <code>double</code> values to a new array of <code>Double</code> boxed objects.
     *
     * @param src Primitive array to convert
     * @return a new array containing the boxed equivalents of the values in the given array
     */
    public static Double[] box(final double[] src) {
        return box(src, 0, src.length);
    }

    /**
     * Convert the tail of an array of primitive <code>double</code values to a new array of <code>Double</code> boxed objects.
     *
     * @param src    Primitive array to convert
     * @param offset First index to covert (inclusive)
     * @return a new array containing the boxed equivalents of the values in the given array
     */
    public static Double[] box(final double[] src,  @Nonnegative final int offset) {
        return box(src, offset, src.length - offset);
    }

    /**
     * Convert a portion of an array of primitive <code>double</code values to a new array of <code>Double</code> boxed objects.
     *
     * @param src    Primitive array to convert
     * @param offset First index to covert (inclusive)
     * @param len    Last index to covert (exclusive)
     * @return a new array containing the boxed equivalents of the values in the given array.
     */
    public static Double[] box(final double[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        Double[] dst = new Double[len];
        for(int i = 0; i < len; i++)
            dst[i] = src[offset + i];
        return dst;
    }

    /**
     * Convert an array of <code>Double</code> boxed objects to a new array of primitive <code>double</code> values.
     *
     * @param src Boxed object array to convert
     * @return a new array containing the primitive equivalents of the values in the given array
     */
    public static double[] unbox(final Double[] src) {
        return unbox(src, 0, src.length);
    }

    /**
     * Convert the tail of an array of <code>Double</code> boxed objects to a new array of primitive <code>double</code values.
     *
     * @param src    Boxed object array to convert
     * @param offset First index to covert (inclusive)
     * @return a new array containing the primitive equivalents of the values in the given array
     */
    public static double[] unbox(final Double[] src,  @Nonnegative final int offset) {
        return unbox(src, offset, src.length - offset);
    }

    /**
     * Convert a portion of an array of <code>Double</code> boxed objects to a new array of primitive <code>double</code values.
     *
     * @param src    Boxed object array to convert
     * @param offset First index to covert (inclusive)
     * @param len    Last index to covert (exclusive)
     * @return a new array containing the primitive equivalents of the values in the given array.
     */
    public static double[] unbox(final Double[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        double[] dst = new double[len];
        for(int i = 0; i < len; i++)
            dst[i] = src[offset + i];
        return dst;
    }



    public static double[] copyOf(final double[] src) {
        return copyOf(src, 0, src.length);
    }

    public static double[] copyOf(final double[] src,  @Nonnegative final int offset) {
        return copyOf(src, offset, src.length - offset);
    }

    public static double[] copyOf(final double[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        double[] dst = new double[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    /**
     * Concatenate two or more arrays.
     *
     * @param arrays to be concatenated
     * @return array contain all the elements of the input arrays
     */
     public static double[] cat(final double[]... arrays) {
        int n = 0;
        for (double[] array : arrays)
            n += array.length;
        double[] result = (double[])Array.newInstance(arrays[0].getClass().getComponentType(), n);
        int offset = 0;
        for (double[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static int firstIndexOf(final double[] arr, final double val) {
        return firstIndexOf(arr, val, 0, arr.length);
    }

    public static int firstIndexOf(final double[] arr, final double val,  @Nonnegative int fromIndex) {
        return firstIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int firstIndexOf(final double[] arr, final double val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static int lastIndexOf(final double[] arr, final double val) {
        return lastIndexOf(arr, val, 0, arr.length);
    }

    public static int lastIndexOf(final double[] arr, final double val,  @Nonnegative int fromIndex) {
        return lastIndexOf(arr, val, fromIndex, arr.length);
    }

    public static int lastIndexOf(final double[] arr, final double val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static boolean contains(final double[] arr, final double val) {
        return firstIndexOf(arr, val) != -1;
    }

    public static boolean contains(final double[] arr, final double val,  @Nonnegative int fromIndex) {
        return firstIndexOf(arr, val, fromIndex) != -1;
    }

    public static boolean contains(final double[] arr, final double val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return firstIndexOf(arr, val, fromIndex, toIndex) != -1;
    }

    public static double[] unique(final double... arr) {
        // This is way to slow O(n^2) - can be done in
        // O(n log n) by sorting in input first.
        double[] result = new double[arr.length];
        int j = 0;
        for (double anArr : arr) {
            if (!contains(result, anArr)) {
                result[j] = anArr;
                j++;
            }
        }
        return copyOf(result, 0, j);
    }

    public static void reverse(final double[] a) {
        reverse(a, 0, a.length);
    }

    public static void reverse(final double[] a,  @Nonnegative final int fromIndex) {
        reverse(a, fromIndex, a.length);
    }

    public static void reverse(final double[] a,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            swap(a, i, j);
        }
    }

    public static void swap(final double[] a,  @Nonnegative final int i,  @Nonnegative final int j) {
        final double tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static double[] elementsOf(double[] arr, int[] idx) {
        double[] result = new double[idx.length];
        for (int i = 0; i < idx.length; i++)
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
            return copyOf(arr, 0, newCap);
        } else {
            return arr;
        }
    }

    public static double mode(double[] arr) {
        Map<Double, Integer> map = new HashMap<Double, Integer>();
        for (double anArr : arr) {
            if (map.containsKey(anArr))
                map.put(anArr, map.get(anArr) + 1);
            else
                map.put(anArr, 1);
        }
        int maxCount = -1;
        Double maxValue = arr[0];
        for (Map.Entry<Double, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxValue = entry.getKey();
            }
        }
        return maxValue;
    }



    public static void permute(final double[] a) {
        permute(a, 0, a.length);
    }

    public static void permute(final double[] a,  @Nonnegative final int toIndex) {
        permute(a, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in
     * lexicographic order.
     *
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isn't the most efficient solution.
     *
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static void permute(final double[] a,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
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

    public static double min(double[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmin(values , fromIndex, toIndex)];
    }

    public static double max(double[] values ) {
        return values[argmax(values , 0, values.length - 1)];
    }

    public static double max(double[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmax(values , fromIndex, toIndex)];
    }

    public static int argmin(double[] values ) {
        return argmin(values , 0, values.length - 1);
    }

    public static int argmin(double[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (values[i]-values[argmin]<0)
                argmin = i;
        return argmin;
    }

    public static int argmax(double[] values ) {
        return argmax(values , 0, values.length - 1);
    }

    public static int argmax(double[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (values[i]-values[argmax]>0)
                argmax = i;
        return argmax;
    }

    public static int[] argminmax(double[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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

    public static double[] minmax(double[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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
        clamp(src , min, max, dst);
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
        clampMin(src , min, dst);
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
        clampMax(src , max, dst);
        return dst;
    }







    public static String toHexString(final double[] arr) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (arr.length > 0)
            buffer.append(Double.toHexString(arr[0]));
        for (int i = 1; i < arr.length; i++) {
            buffer.append(' ');
            buffer.append(Double.toHexString(arr[i]));
        }
        buffer.append(']');
        return buffer.toString();
    }



    /*
     * ==================
     *  Type: Object
     * ==================
     */



    public static void permute(final Object[] a) {
        permute(a, 0, a.length);
    }

    public static void permute(final Object[] a,  @Nonnegative final int toIndex) {
        permute(a, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in
     * lexicographic order.
     *
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isn't the most efficient solution.
     *
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static void permute(final Object[] a,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
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

    public static Object min(Object[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmin(values , fromIndex, toIndex)];
    }

    public static Object max(Object[] values ) {
        return values[argmax(values , 0, values.length - 1)];
    }

    public static Object max(Object[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmax(values , fromIndex, toIndex)];
    }

    public static int argmin(Object[] values ) {
        return argmin(values , 0, values.length - 1);
    }

    public static int argmin(Object[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (((Comparable)values[i]).compareTo(values[argmin])<0)
                argmin = i;
        return argmin;
    }

    public static int argmax(Object[] values ) {
        return argmax(values , 0, values.length - 1);
    }

    public static int argmax(Object[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (((Comparable)values[i]).compareTo(values[argmax])>0)
                argmax = i;
        return argmax;
    }

    public static int[] argminmax(Object[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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

    public static Object[] minmax(Object[] values ,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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
        clamp(src , min, max, dst);
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
        clampMin(src , min, dst);
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
        clampMax(src , max, dst);
        return dst;
    }



    /*
     * ==================
     *  Type: T
     * ==================
     */



    public static <T> T[] copyOf(final T[] src) {
        return copyOf(src, 0, src.length);
    }

    public static <T> T[] copyOf(final T[] src,  @Nonnegative final int offset) {
        return copyOf(src, offset, src.length - offset);
    }

    public static <T> T[] copyOf(final T[] src,  @Nonnegative final int offset,  @Nonnegative final int len) {
        T[] dst = (T[])new Object[len];
        System.arraycopy(src, offset, dst, 0, len);
        return dst;
    }

    /**
     * Concatenate two or more arrays.
     *
     * @param arrays to be concatenated
     * @return array contain all the elements of the input arrays
     */
     public static <T> T[] cat(final T[]... arrays) {
        int n = 0;
        for (T[] array : arrays)
            n += array.length;
        T[] result = (T[])Array.newInstance(arrays[0].getClass().getComponentType(), n);
        int offset = 0;
        for (T[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static <T> int firstIndexOf(final T[] arr, final T val) {
        return firstIndexOf(arr, val, 0, arr.length);
    }

    public static <T> int firstIndexOf(final T[] arr, final T val,  @Nonnegative int fromIndex) {
        return firstIndexOf(arr, val, fromIndex, arr.length);
    }

    public static <T> int firstIndexOf(final T[] arr, final T val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static <T> int lastIndexOf(final T[] arr, final T val) {
        return lastIndexOf(arr, val, 0, arr.length);
    }

    public static <T> int lastIndexOf(final T[] arr, final T val,  @Nonnegative int fromIndex) {
        return lastIndexOf(arr, val, fromIndex, arr.length);
    }

    public static <T> int lastIndexOf(final T[] arr, final T val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        for (int i = toIndex - 1; i >= fromIndex; i--)
            if (arr[i] == val)
                return i;
        return -1;
    }

    public static <T> boolean contains(final T[] arr, final T val) {
        return firstIndexOf(arr, val) != -1;
    }

    public static <T> boolean contains(final T[] arr, final T val,  @Nonnegative int fromIndex) {
        return firstIndexOf(arr, val, fromIndex) != -1;
    }

    public static <T> boolean contains(final T[] arr, final T val,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return firstIndexOf(arr, val, fromIndex, toIndex) != -1;
    }

    public static <T> T[] unique(final T... arr) {
        // This is way to slow O(n^2) - can be done in
        // O(n log n) by sorting in input first.
        T[] result = (T[])new Object[arr.length];
        int j = 0;
        for (T anArr : arr) {
            if (!contains(result, anArr)) {
                result[j] = anArr;
                j++;
            }
        }
        return copyOf(result, 0, j);
    }

    public static <T> void reverse(final T[] a) {
        reverse(a, 0, a.length);
    }

    public static <T> void reverse(final T[] a,  @Nonnegative final int fromIndex) {
        reverse(a, fromIndex, a.length);
    }

    public static <T> void reverse(final T[] a,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
        for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
            swap(a, i, j);
        }
    }

    public static <T> void swap(final T[] a,  @Nonnegative final int i,  @Nonnegative final int j) {
        final T tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static <T> T[] elementsOf(T[] arr, int[] idx) {
        T[] result = (T[])new Object[idx.length];
        for (int i = 0; i < idx.length; i++)
            result[i] = arr[idx[i]];
        return result;
    }

    public static <T> T[] elementsOf(T[] arr, boolean[] logical) {
        return elementsOf(arr, find(logical));
    }

    public static <T> boolean[] valuesEq(T[] arr, T val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] == val;
        return result;
    }

    public static <T> boolean[] valuesNeq(T[] arr, T val) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] != val;
        return result;
    }

    public static <T> T[] ensureCapacity(T[] arr, int minCap) {
        int oldCap = arr.length;
        if (minCap > oldCap) {
            int newCap = (oldCap * 3) / 2 + 1;
            if (newCap < minCap)
                newCap = minCap;
            return copyOf(arr, 0, newCap);
        } else {
            return arr;
        }
    }

    public static <T> T mode(T[] arr) {
        Map<T, Integer> map = new HashMap<T, Integer>();
        for (T anArr : arr) {
            if (map.containsKey(anArr))
                map.put(anArr, map.get(anArr) + 1);
            else
                map.put(anArr, 1);
        }
        int maxCount = -1;
        T maxValue = arr[0];
        for (Map.Entry<T, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxValue = entry.getKey();
            }
        }
        return maxValue;
    }



    public static <T> void permute(final T[] a, Comparator<T> comp) {
        permute(a, comp, 0, a.length);
    }

    public static <T> void permute(final T[] a, Comparator<T> comp,  @Nonnegative final int toIndex) {
        permute(a, comp, toIndex, a.length);
    }

    /**
     * Takes an array a and rearranges it into the next permutation in
     * lexicographic order.
     *
     * Use an algorithm defined in
     * http://en.wikipedia.org/wiki/Permutation#Generation_in_lexicographic_order
     * which probably isn't the most efficient solution.
     *
     * @param a the array to be permuted
     * @param toIndex index of the first element (inclusive) to be permuted
     * @param fromIndex index of the last element (exclusive) to be permuted
     */
    public static <T> void permute(final T[] a, Comparator<T> comp,  @Nonnegative final int fromIndex,  @Nonnegative final int toIndex) {
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

    public static <T> T min(T[] values , Comparator<T> comp,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmin(values , comp, fromIndex, toIndex)];
    }

    public static <T> T max(T[] values , Comparator<T> comp) {
        return values[argmax(values , comp, 0, values.length - 1)];
    }

    public static <T> T max(T[] values , Comparator<T> comp,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        return values[argmax(values , comp, fromIndex, toIndex)];
    }

    public static <T> int argmin(T[] values , Comparator<T> comp) {
        return argmin(values , comp, 0, values.length - 1);
    }

    public static <T> int argmin(T[] values , Comparator<T> comp,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmin = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (comp.compare(values[i],values[argmin])<0)
                argmin = i;
        return argmin;
    }

    public static <T> int argmax(T[] values , Comparator<T> comp) {
        return argmax(values , comp, 0, values.length - 1);
    }

    public static <T> int argmax(T[] values , Comparator<T> comp,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
        int argmax = fromIndex;
        for (int i = fromIndex + 1; i <= toIndex; i++)
            if (comp.compare(values[i],values[argmax])>0)
                argmax = i;
        return argmax;
    }

    public static <T> int[] argminmax(T[] values , Comparator<T> comp,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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

    public static <T> T[] minmax(T[] values , Comparator<T> comp,  @Nonnegative int fromIndex,  @Nonnegative int toIndex) {
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
        clamp(src , comp, min, max, dst);
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
        clampMin(src , comp, min, dst);
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
        clampMax(src , comp, max, dst);
        return dst;
    }


}

