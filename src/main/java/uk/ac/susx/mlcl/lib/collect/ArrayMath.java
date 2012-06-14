/*
 * Copyright (c) 2011-2012, University of Sussex
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

import uk.ac.susx.mlcl.lib.Checks;
import java.util.Arrays;
import java.util.Random;
import static uk.ac.susx.mlcl.lib.collect.ArrayUtil.*;

/**
 * Static utility class for performing arithmetic operations on primitive
 * arrays.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@SuppressWarnings("unchecked")
public final class ArrayMath {

    private ArrayMath() {}

    private static final class Lazy {
        private static final Random RND = new Random();
        private Lazy() {}
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


    public static byte mean(byte[] values, int fromIndex, int toIndex) {
        return (byte)(sum(values, fromIndex, toIndex) / (toIndex - fromIndex));
    }

    public static byte mean(byte[] values, int fromIndex) {
		return (byte)mean(values, fromIndex, values.length);
    }

    public static byte mean(byte[] values) {
		return (byte)mean(values, 0, values.length);
    }

    public static byte variance(byte[] values, int fromIndex, int toIndex) {
		byte u = mean(values, fromIndex, toIndex);
		byte s2 = 0;
		for(int i = fromIndex; i < toIndex; i++) 
			s2 += (values[i] - u) * (values[i] - u);
		return (byte)(s2 / (toIndex - fromIndex));
    }

    public static byte variance(byte[] values, int fromIndex) {
		return (byte)variance(values, fromIndex, values.length);
	}
    
    public static byte variance(byte[] values) {
		return (byte)variance(values, 0, values.length);
	}
	
    public static byte stddev(byte[] values, int fromIndex, int toIndex) {
		return (byte)Math.sqrt(variance(values, fromIndex, toIndex));
    }

    public static byte stddev(byte[] values, int fromIndex) {
		return (byte)Math.sqrt(variance(values, fromIndex));
	}
    
    public static byte stddev(byte[] values) {
		return (byte)Math.sqrt(variance(values));
	}
	

    public static byte sampleVariance(byte[] values, int fromIndex, int toIndex) {
		byte u = mean(values, fromIndex, toIndex);
		byte s2 = 0;
		for(int i = fromIndex; i < toIndex; i++) 
			s2 += (values[i] - u) * (values[i] - u);
		return (byte)(s2 / ((toIndex - fromIndex) - 1));
    }

    public static byte sampleVariance(byte[] values, int fromIndex) {
		return (byte)sampleVariance(values, fromIndex, values.length);
	}
    
    public static byte sampleVariance(byte[] values) {
		return (byte)sampleVariance(values, 0, values.length);
	}
	
    public static byte sampleStddev(byte[] values, int fromIndex, int toIndex) {
		return (byte)Math.sqrt(sampleVariance(values, fromIndex, toIndex));
    }

    public static byte sampleStddev(byte[] values, int fromIndex) {
		return (byte)Math.sqrt(sampleVariance(values, fromIndex));
	}
    
    public static byte sampleStddev(byte[] values) {
		return (byte)Math.sqrt(sampleVariance(values));
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


    public static char mean(char[] values, int fromIndex, int toIndex) {
        return (char)(sum(values, fromIndex, toIndex) / (toIndex - fromIndex));
    }

    public static char mean(char[] values, int fromIndex) {
		return (char)mean(values, fromIndex, values.length);
    }

    public static char mean(char[] values) {
		return (char)mean(values, 0, values.length);
    }

    public static char variance(char[] values, int fromIndex, int toIndex) {
		char u = mean(values, fromIndex, toIndex);
		char s2 = 0;
		for(int i = fromIndex; i < toIndex; i++) 
			s2 += (values[i] - u) * (values[i] - u);
		return (char)(s2 / (toIndex - fromIndex));
    }

    public static char variance(char[] values, int fromIndex) {
		return (char)variance(values, fromIndex, values.length);
	}
    
    public static char variance(char[] values) {
		return (char)variance(values, 0, values.length);
	}
	
    public static char stddev(char[] values, int fromIndex, int toIndex) {
		return (char)Math.sqrt(variance(values, fromIndex, toIndex));
    }

    public static char stddev(char[] values, int fromIndex) {
		return (char)Math.sqrt(variance(values, fromIndex));
	}
    
    public static char stddev(char[] values) {
		return (char)Math.sqrt(variance(values));
	}
	

    public static char sampleVariance(char[] values, int fromIndex, int toIndex) {
		char u = mean(values, fromIndex, toIndex);
		char s2 = 0;
		for(int i = fromIndex; i < toIndex; i++) 
			s2 += (values[i] - u) * (values[i] - u);
		return (char)(s2 / ((toIndex - fromIndex) - 1));
    }

    public static char sampleVariance(char[] values, int fromIndex) {
		return (char)sampleVariance(values, fromIndex, values.length);
	}
    
    public static char sampleVariance(char[] values) {
		return (char)sampleVariance(values, 0, values.length);
	}
	
    public static char sampleStddev(char[] values, int fromIndex, int toIndex) {
		return (char)Math.sqrt(sampleVariance(values, fromIndex, toIndex));
    }

    public static char sampleStddev(char[] values, int fromIndex) {
		return (char)Math.sqrt(sampleVariance(values, fromIndex));
	}
    
    public static char sampleStddev(char[] values) {
		return (char)Math.sqrt(sampleVariance(values));
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


    public static short mean(short[] values, int fromIndex, int toIndex) {
        return (short)(sum(values, fromIndex, toIndex) / (toIndex - fromIndex));
    }

    public static short mean(short[] values, int fromIndex) {
		return (short)mean(values, fromIndex, values.length);
    }

    public static short mean(short[] values) {
		return (short)mean(values, 0, values.length);
    }

    public static short variance(short[] values, int fromIndex, int toIndex) {
		short u = mean(values, fromIndex, toIndex);
		short s2 = 0;
		for(int i = fromIndex; i < toIndex; i++) 
			s2 += (values[i] - u) * (values[i] - u);
		return (short)(s2 / (toIndex - fromIndex));
    }

    public static short variance(short[] values, int fromIndex) {
		return (short)variance(values, fromIndex, values.length);
	}
    
    public static short variance(short[] values) {
		return (short)variance(values, 0, values.length);
	}
	
    public static short stddev(short[] values, int fromIndex, int toIndex) {
		return (short)Math.sqrt(variance(values, fromIndex, toIndex));
    }

    public static short stddev(short[] values, int fromIndex) {
		return (short)Math.sqrt(variance(values, fromIndex));
	}
    
    public static short stddev(short[] values) {
		return (short)Math.sqrt(variance(values));
	}
	

    public static short sampleVariance(short[] values, int fromIndex, int toIndex) {
		short u = mean(values, fromIndex, toIndex);
		short s2 = 0;
		for(int i = fromIndex; i < toIndex; i++) 
			s2 += (values[i] - u) * (values[i] - u);
		return (short)(s2 / ((toIndex - fromIndex) - 1));
    }

    public static short sampleVariance(short[] values, int fromIndex) {
		return (short)sampleVariance(values, fromIndex, values.length);
	}
    
    public static short sampleVariance(short[] values) {
		return (short)sampleVariance(values, 0, values.length);
	}
	
    public static short sampleStddev(short[] values, int fromIndex, int toIndex) {
		return (short)Math.sqrt(sampleVariance(values, fromIndex, toIndex));
    }

    public static short sampleStddev(short[] values, int fromIndex) {
		return (short)Math.sqrt(sampleVariance(values, fromIndex));
	}
    
    public static short sampleStddev(short[] values) {
		return (short)Math.sqrt(sampleVariance(values));
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


    public static int mean(int[] values, int fromIndex, int toIndex) {
        return (int)(sum(values, fromIndex, toIndex) / (toIndex - fromIndex));
    }

    public static int mean(int[] values, int fromIndex) {
		return (int)mean(values, fromIndex, values.length);
    }

    public static int mean(int[] values) {
		return (int)mean(values, 0, values.length);
    }

    public static int variance(int[] values, int fromIndex, int toIndex) {
		int u = mean(values, fromIndex, toIndex);
		int s2 = 0;
		for(int i = fromIndex; i < toIndex; i++) 
			s2 += (values[i] - u) * (values[i] - u);
		return (int)(s2 / (toIndex - fromIndex));
    }

    public static int variance(int[] values, int fromIndex) {
		return (int)variance(values, fromIndex, values.length);
	}
    
    public static int variance(int[] values) {
		return (int)variance(values, 0, values.length);
	}
	
    public static int stddev(int[] values, int fromIndex, int toIndex) {
		return (int)Math.sqrt(variance(values, fromIndex, toIndex));
    }

    public static int stddev(int[] values, int fromIndex) {
		return (int)Math.sqrt(variance(values, fromIndex));
	}
    
    public static int stddev(int[] values) {
		return (int)Math.sqrt(variance(values));
	}
	

    public static int sampleVariance(int[] values, int fromIndex, int toIndex) {
		int u = mean(values, fromIndex, toIndex);
		int s2 = 0;
		for(int i = fromIndex; i < toIndex; i++) 
			s2 += (values[i] - u) * (values[i] - u);
		return (int)(s2 / ((toIndex - fromIndex) - 1));
    }

    public static int sampleVariance(int[] values, int fromIndex) {
		return (int)sampleVariance(values, fromIndex, values.length);
	}
    
    public static int sampleVariance(int[] values) {
		return (int)sampleVariance(values, 0, values.length);
	}
	
    public static int sampleStddev(int[] values, int fromIndex, int toIndex) {
		return (int)Math.sqrt(sampleVariance(values, fromIndex, toIndex));
    }

    public static int sampleStddev(int[] values, int fromIndex) {
		return (int)Math.sqrt(sampleVariance(values, fromIndex));
	}
    
    public static int sampleStddev(int[] values) {
		return (int)Math.sqrt(sampleVariance(values));
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


    public static long mean(long[] values, int fromIndex, int toIndex) {
        return (long)(sum(values, fromIndex, toIndex) / (toIndex - fromIndex));
    }

    public static long mean(long[] values, int fromIndex) {
		return (long)mean(values, fromIndex, values.length);
    }

    public static long mean(long[] values) {
		return (long)mean(values, 0, values.length);
    }

    public static long variance(long[] values, int fromIndex, int toIndex) {
		long u = mean(values, fromIndex, toIndex);
		long s2 = 0;
		for(int i = fromIndex; i < toIndex; i++) 
			s2 += (values[i] - u) * (values[i] - u);
		return (long)(s2 / (toIndex - fromIndex));
    }

    public static long variance(long[] values, int fromIndex) {
		return (long)variance(values, fromIndex, values.length);
	}
    
    public static long variance(long[] values) {
		return (long)variance(values, 0, values.length);
	}
	
    public static long stddev(long[] values, int fromIndex, int toIndex) {
		return (long)Math.sqrt(variance(values, fromIndex, toIndex));
    }

    public static long stddev(long[] values, int fromIndex) {
		return (long)Math.sqrt(variance(values, fromIndex));
	}
    
    public static long stddev(long[] values) {
		return (long)Math.sqrt(variance(values));
	}
	

    public static long sampleVariance(long[] values, int fromIndex, int toIndex) {
		long u = mean(values, fromIndex, toIndex);
		long s2 = 0;
		for(int i = fromIndex; i < toIndex; i++) 
			s2 += (values[i] - u) * (values[i] - u);
		return (long)(s2 / ((toIndex - fromIndex) - 1));
    }

    public static long sampleVariance(long[] values, int fromIndex) {
		return (long)sampleVariance(values, fromIndex, values.length);
	}
    
    public static long sampleVariance(long[] values) {
		return (long)sampleVariance(values, 0, values.length);
	}
	
    public static long sampleStddev(long[] values, int fromIndex, int toIndex) {
		return (long)Math.sqrt(sampleVariance(values, fromIndex, toIndex));
    }

    public static long sampleStddev(long[] values, int fromIndex) {
		return (long)Math.sqrt(sampleVariance(values, fromIndex));
	}
    
    public static long sampleStddev(long[] values) {
		return (long)Math.sqrt(sampleVariance(values));
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


    public static float mean(float[] values, int fromIndex, int toIndex) {
        return (float)(sum(values, fromIndex, toIndex) / (toIndex - fromIndex));
    }

    public static float mean(float[] values, int fromIndex) {
		return (float)mean(values, fromIndex, values.length);
    }

    public static float mean(float[] values) {
		return (float)mean(values, 0, values.length);
    }

    public static float variance(float[] values, int fromIndex, int toIndex) {
		float u = mean(values, fromIndex, toIndex);
		float s2 = 0;
		for(int i = fromIndex; i < toIndex; i++) 
			s2 += (values[i] - u) * (values[i] - u);
		return (float)(s2 / (toIndex - fromIndex));
    }

    public static float variance(float[] values, int fromIndex) {
		return (float)variance(values, fromIndex, values.length);
	}
    
    public static float variance(float[] values) {
		return (float)variance(values, 0, values.length);
	}
	
    public static float stddev(float[] values, int fromIndex, int toIndex) {
		return (float)Math.sqrt(variance(values, fromIndex, toIndex));
    }

    public static float stddev(float[] values, int fromIndex) {
		return (float)Math.sqrt(variance(values, fromIndex));
	}
    
    public static float stddev(float[] values) {
		return (float)Math.sqrt(variance(values));
	}
	

    public static float sampleVariance(float[] values, int fromIndex, int toIndex) {
		float u = mean(values, fromIndex, toIndex);
		float s2 = 0;
		for(int i = fromIndex; i < toIndex; i++) 
			s2 += (values[i] - u) * (values[i] - u);
		return (float)(s2 / ((toIndex - fromIndex) - 1));
    }

    public static float sampleVariance(float[] values, int fromIndex) {
		return (float)sampleVariance(values, fromIndex, values.length);
	}
    
    public static float sampleVariance(float[] values) {
		return (float)sampleVariance(values, 0, values.length);
	}
	
    public static float sampleStddev(float[] values, int fromIndex, int toIndex) {
		return (float)Math.sqrt(sampleVariance(values, fromIndex, toIndex));
    }

    public static float sampleStddev(float[] values, int fromIndex) {
		return (float)Math.sqrt(sampleVariance(values, fromIndex));
	}
    
    public static float sampleStddev(float[] values) {
		return (float)Math.sqrt(sampleVariance(values));
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


    public static double mean(double[] values, int fromIndex, int toIndex) {
        return (double)(sum(values, fromIndex, toIndex) / (toIndex - fromIndex));
    }

    public static double mean(double[] values, int fromIndex) {
		return (double)mean(values, fromIndex, values.length);
    }

    public static double mean(double[] values) {
		return (double)mean(values, 0, values.length);
    }

    public static double variance(double[] values, int fromIndex, int toIndex) {
		double u = mean(values, fromIndex, toIndex);
		double s2 = 0;
		for(int i = fromIndex; i < toIndex; i++) 
			s2 += (values[i] - u) * (values[i] - u);
		return (double)(s2 / (toIndex - fromIndex));
    }

    public static double variance(double[] values, int fromIndex) {
		return (double)variance(values, fromIndex, values.length);
	}
    
    public static double variance(double[] values) {
		return (double)variance(values, 0, values.length);
	}
	
    public static double stddev(double[] values, int fromIndex, int toIndex) {
		return (double)Math.sqrt(variance(values, fromIndex, toIndex));
    }

    public static double stddev(double[] values, int fromIndex) {
		return (double)Math.sqrt(variance(values, fromIndex));
	}
    
    public static double stddev(double[] values) {
		return (double)Math.sqrt(variance(values));
	}
	

    public static double sampleVariance(double[] values, int fromIndex, int toIndex) {
		double u = mean(values, fromIndex, toIndex);
		double s2 = 0;
		for(int i = fromIndex; i < toIndex; i++) 
			s2 += (values[i] - u) * (values[i] - u);
		return (double)(s2 / ((toIndex - fromIndex) - 1));
    }

    public static double sampleVariance(double[] values, int fromIndex) {
		return (double)sampleVariance(values, fromIndex, values.length);
	}
    
    public static double sampleVariance(double[] values) {
		return (double)sampleVariance(values, 0, values.length);
	}
	
    public static double sampleStddev(double[] values, int fromIndex, int toIndex) {
		return (double)Math.sqrt(sampleVariance(values, fromIndex, toIndex));
    }

    public static double sampleStddev(double[] values, int fromIndex) {
		return (double)Math.sqrt(sampleVariance(values, fromIndex));
	}
    
    public static double sampleStddev(double[] values) {
		return (double)Math.sqrt(sampleVariance(values));
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


}

