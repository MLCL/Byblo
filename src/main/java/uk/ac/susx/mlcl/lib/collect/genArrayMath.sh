#!/bin/sh
#
# Copyright (c) 2011-2012, University of Sussex
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
#  * Redistributions of source code must retain the above copyright notice,
#    this list of conditions and the following disclaimer.
#
#  * Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
#
#  * Neither the name of the University of Sussex nor the names of its
#    contributors may be used to endorse or promote products derived from this
#    software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
#



class=ArrayMath
outfile=${class}.java

if [[ -e ${outfile} ]]; then
    rm ${outfile}
fi

PRIMTYP=("boolean" "byte" "char" "short" "int" "long" "float" "double" "Object" "T")
GENTYPE[9]=" <T>"
NEWINSTANCE=("new boolean" "new byte" "new char" "new short" "new int" "new long"
    "new float" "new double" "new Object" "(T[])new Object")
BOXTYPE=("Boolean" "Byte" "Character" "Short" "Integer" "Long" "Float" "Double" "Object")

ARGSIN[9]=", Comparator<T> comp"
ARGSOUT[9]=", comp"

COMP=("(\$1==\$2?1:0)" "(byte)(\$1-\$2)" "(char)(\$1-\$2)" "(short)(\$1-\$2)" "\$1-\$2" "\$1-\$2"
     "\$1-\$2" "\$1-\$2" "((Comparable)\$1).compareTo(\$2)" "comp.compare(\$1,\$2)")


PrimToStringClass=("" Integer Integer Integer Integer Long Float Double "" "")

symbolic=(1 1 1 1 1 1 1 1 1 0)
ordered=(1 1 1 1 1 1 1 1 1 1)
numeric=(0 1 1 1 1 1 1 1 0 0)
tooctal=(0 1 1 1 1 1 0 0 0 0)
floatp=(0 0 0 0 0 0 1 1 0 0)


perl -pe "s/__CLASS__/${class}/g;" << "---EOF---" >> ${outfile}
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
public final class __CLASS__ {

    private __CLASS__() {}

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


---EOF---


for ((i=0; i<${#PRIMTYP[@]}; i++))
do

if (( ${numeric[i]} == 1 ))
then
cat << "---EOF---" \
    | perl -pe "s/PRIMTYP/${PRIMTYP[i]}/g;" \
    | perl -pe "s/COMP\(([^,]*),([^)]*)\)/${COMP[i]}/g"


    public static PRIMTYP[] PRIMTYPZeros(int n) {
        return new PRIMTYP[n];
    }

    public static PRIMTYP[] PRIMTYPOnes(int n) {
        PRIMTYP[] result = new PRIMTYP[n];
        Arrays.fill(result, (PRIMTYP)1);
        return result;
    }

    public static PRIMTYP range(PRIMTYP[] values) {
        return range(values, 0, values.length - 1);
    }

    public static PRIMTYP range(PRIMTYP[] values, int fromIndex) {
        return range(values, fromIndex, values.length - 1);
    }

    public static PRIMTYP range(PRIMTYP[] values, int fromIndex, int toIndex) {
        int[] mm = argminmax(values, fromIndex, toIndex);
        return COMP(values[mm[1]], values[mm[0]]);
    }


    public static PRIMTYP[] range(PRIMTYP start, PRIMTYP step, PRIMTYP end) {
        PRIMTYP[] arr = new PRIMTYP[(int)((end - start) / step)];
        for (int i = 0; i < arr.length; i++)
            arr[i] = (PRIMTYP)(start + (i * step));
        return arr;
    }

    public static PRIMTYP sum(PRIMTYP[] arr) {
        return sum(arr, 0, arr.length);
    }

    public static PRIMTYP sum(PRIMTYP[] arr, int fromIndex) {
        return sum(arr, fromIndex, arr.length);
    }

    public static PRIMTYP sum(PRIMTYP[] arr, int fromIndex, int toIndex) {
        PRIMTYP sum = (PRIMTYP)0;
        for (int i = fromIndex; i < toIndex; i++)
            sum += arr[i];
        return sum;
    }

    public static PRIMTYP product(PRIMTYP[] arr) {
        return product(arr, 0, arr.length);
    }

    public static PRIMTYP product(PRIMTYP[] arr, int fromIndex) {
        return product(arr, fromIndex, arr.length);
    }

    public static PRIMTYP product(PRIMTYP[] arr, int fromIndex, int toIndex) {
        PRIMTYP prod = (PRIMTYP)1;
        for (int i = fromIndex; i < toIndex; i++)
            prod += arr[i];
        return prod;
    }

    public static void mul(PRIMTYP[] src, PRIMTYP scalar, PRIMTYP[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (PRIMTYP)(src[i] * scalar);
    }

    public static void mul(PRIMTYP[] src1, PRIMTYP[] src2, PRIMTYP[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (PRIMTYP)(src1[i] * src2[i]);
    }

    public static PRIMTYP[] mul(PRIMTYP[] src, PRIMTYP scalar) {
        PRIMTYP[] dst = new PRIMTYP[src.length];
        mul(src, scalar, dst);
        return dst;
    }

    public static PRIMTYP[] mul(PRIMTYP[] src1, PRIMTYP[] src2) {
        PRIMTYP[] dst = new PRIMTYP[src1.length];
        mul(src1, src2, dst);
        return dst;
    }

    public static void div(PRIMTYP[] src, PRIMTYP scalar, PRIMTYP[] dst) {
        mul(src, (PRIMTYP)(1.0 / scalar), dst);
    }

    public static void div(PRIMTYP scalar, PRIMTYP[] src, PRIMTYP[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (PRIMTYP)(scalar / src[i]);
    }

    public static void div(PRIMTYP[] src1, PRIMTYP[] src2, PRIMTYP[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (PRIMTYP)(src1[i] / src2[i]);
    }

    public static PRIMTYP[] div(PRIMTYP[] src, PRIMTYP scalar) {
        return mul(src, (PRIMTYP)(1.0 / scalar));
    }

    public static PRIMTYP[] div(PRIMTYP scalar, PRIMTYP[] src) {
        PRIMTYP[] dst = new PRIMTYP[src.length];
        div(scalar, src, dst);
        return dst;
    }

    public static PRIMTYP[] div(PRIMTYP[] src1, PRIMTYP[] src2) {
        PRIMTYP[] dst = new PRIMTYP[src1.length];
        div(src1, src2, dst);
        return dst;
    }

    public static void add(PRIMTYP[] src, PRIMTYP scalar, PRIMTYP[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (PRIMTYP)(src[i] + scalar);
    }

    public static void add(PRIMTYP[] src1, PRIMTYP[] src2, PRIMTYP[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (PRIMTYP)(src1[i] + src2[i]);
    }

    public static PRIMTYP[] add(PRIMTYP[] src, PRIMTYP scalar) {
        PRIMTYP[] dst = new PRIMTYP[src.length];
        add(src, scalar, dst);
        return dst;
    }

    public static PRIMTYP[] add(PRIMTYP[] src1, PRIMTYP[] src2) {
        PRIMTYP[] dst = new PRIMTYP[src1.length];
        add(src1, src2, dst);
        return dst;
    }

    public static void sub(PRIMTYP[] src, PRIMTYP scalar, PRIMTYP[] dst) {
        add(src, (PRIMTYP)(-scalar), dst);
    }

    public static void sub(PRIMTYP[] src1, PRIMTYP[] src2, PRIMTYP[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (PRIMTYP)(src1[i] - src2[i]);
    }

    public static PRIMTYP[] sub(PRIMTYP[] src, PRIMTYP scalar) {
        return add(src, (PRIMTYP)(-scalar));
    }

    public static PRIMTYP[] sub(PRIMTYP[] src1, PRIMTYP[] src2) {
        PRIMTYP[] dst = new PRIMTYP[src1.length];
        sub(src1, src2, dst);
        return dst;
    }

    public static void mod(PRIMTYP[] src, PRIMTYP scalar, PRIMTYP[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (PRIMTYP)(src[i] % scalar);
    }

    public static void mod(PRIMTYP scalar, PRIMTYP[] src, PRIMTYP[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (PRIMTYP)(scalar % src[i]);
    }

    public static void mod(PRIMTYP[] src1, PRIMTYP[] src2, PRIMTYP[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (PRIMTYP)(src1[i] % src2[i]);
    }

    public static PRIMTYP[] mod(PRIMTYP[] src, PRIMTYP scalar) {
        PRIMTYP[] dst = new PRIMTYP[src.length];
        mod(src, scalar, dst);
        return dst;
    }

    public static PRIMTYP[] mod(PRIMTYP scalar, PRIMTYP[] src) {
        PRIMTYP[] dst = new PRIMTYP[src.length];
        mod(scalar, src, dst);
        return dst;
    }

    public static PRIMTYP[] mod(PRIMTYP[] src1, PRIMTYP[] src2) {
        PRIMTYP[] dst = new PRIMTYP[src1.length];
        mod(src1, src2, dst);
        return dst;
    }

    public static void negate(PRIMTYP[] src, PRIMTYP[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (PRIMTYP)(-src[i]);
    }

    public static PRIMTYP[] negate(PRIMTYP[] src) {
        PRIMTYP[] dst = new PRIMTYP[src.length];
        negate(src, dst);
        return dst;
    }

    public static void pow(PRIMTYP[] src, PRIMTYP power, PRIMTYP[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (PRIMTYP)Math.pow(src[i], power);
    }

    public static void pow(PRIMTYP[] src1, PRIMTYP[] src2, PRIMTYP[] dst) {
        Checks.checkEquals(src1.length, src2.length, dst.length);
        for (int i = 0; i < src1.length; i++)
            dst[i] = (PRIMTYP)Math.pow(src1[i], src2[i]);
    }

    public static PRIMTYP[] pow(PRIMTYP[] src, PRIMTYP scalar) {
        PRIMTYP[] dst = new PRIMTYP[src.length];
        pow(src, scalar, dst);
        return dst;
    }

    public static PRIMTYP[] pow(PRIMTYP[] src1, PRIMTYP[] src2) {
        PRIMTYP[] dst = new PRIMTYP[src1.length];
        pow(src1, src2, dst);
        return dst;
    }

    public static void squared(PRIMTYP[] src, PRIMTYP[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (PRIMTYP)(src[i] * src[i]);
    }

    public static PRIMTYP[] squared(PRIMTYP[] src) {
        PRIMTYP[] dst = new PRIMTYP[src.length];
        squared(src, dst);
        return dst;
    }

    public static void cubed(PRIMTYP[] src, PRIMTYP[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (PRIMTYP)(src[i] * src[i] * src[i]);
    }

    public static PRIMTYP[] cubed(PRIMTYP[] src) {
        PRIMTYP[] dst = new PRIMTYP[src.length];
        cubed(src, dst);
        return dst;
    }

    public static void sqrt(PRIMTYP[] src, PRIMTYP[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (PRIMTYP)(Math.sqrt(src[i]));
    }

    public static PRIMTYP[] sqrt(PRIMTYP[] src) {
        PRIMTYP[] dst = new PRIMTYP[src.length];
        sqrt(src, dst);
        return dst;
    }

    public static void abs(PRIMTYP[] src, PRIMTYP[] dst) {
        Checks.checkEqual(src.length, dst.length);
        for (int i = 0; i < src.length; i++)
            dst[i] = (PRIMTYP)(Math.abs(src[i]));
    }

    public static PRIMTYP[] abs(PRIMTYP[] src) {
        PRIMTYP[] dst = new PRIMTYP[src.length];
        abs(src, dst);
        return dst;
    }


    public static PRIMTYP mean(PRIMTYP[] values, int fromIndex, int toIndex) {
        return (PRIMTYP)(sum(values, fromIndex, toIndex) / (toIndex - fromIndex));
    }

    public static PRIMTYP mean(PRIMTYP[] values, int fromIndex) {
		return (PRIMTYP)mean(values, fromIndex, values.length);
    }

    public static PRIMTYP mean(PRIMTYP[] values) {
		return (PRIMTYP)mean(values, 0, values.length);
    }

    public static PRIMTYP variance(PRIMTYP[] values, int fromIndex, int toIndex) {
		PRIMTYP u = mean(values, fromIndex, toIndex);
		PRIMTYP s2 = 0;
		for(int i = fromIndex; i < toIndex; i++)
			s2 += (values[i] - u) * (values[i] - u);
		return (PRIMTYP)(s2 / (toIndex - fromIndex));
    }

    public static PRIMTYP variance(PRIMTYP[] values, int fromIndex) {
		return (PRIMTYP)variance(values, fromIndex, values.length);
	}

    public static PRIMTYP variance(PRIMTYP[] values) {
		return (PRIMTYP)variance(values, 0, values.length);
	}

    public static PRIMTYP stddev(PRIMTYP[] values, int fromIndex, int toIndex) {
		return (PRIMTYP)Math.sqrt(variance(values, fromIndex, toIndex));
    }

    public static PRIMTYP stddev(PRIMTYP[] values, int fromIndex) {
		return (PRIMTYP)Math.sqrt(variance(values, fromIndex));
	}

    public static PRIMTYP stddev(PRIMTYP[] values) {
		return (PRIMTYP)Math.sqrt(variance(values));
	}


    public static PRIMTYP sampleVariance(PRIMTYP[] values, int fromIndex, int toIndex) {
		PRIMTYP u = mean(values, fromIndex, toIndex);
		PRIMTYP s2 = 0;
		for(int i = fromIndex; i < toIndex; i++)
			s2 += (values[i] - u) * (values[i] - u);
		return (PRIMTYP)(s2 / ((toIndex - fromIndex) - 1));
    }

    public static PRIMTYP sampleVariance(PRIMTYP[] values, int fromIndex) {
		return (PRIMTYP)sampleVariance(values, fromIndex, values.length);
	}

    public static PRIMTYP sampleVariance(PRIMTYP[] values) {
		return (PRIMTYP)sampleVariance(values, 0, values.length);
	}

    public static PRIMTYP sampleStddev(PRIMTYP[] values, int fromIndex, int toIndex) {
		return (PRIMTYP)Math.sqrt(sampleVariance(values, fromIndex, toIndex));
    }

    public static PRIMTYP sampleStddev(PRIMTYP[] values, int fromIndex) {
		return (PRIMTYP)Math.sqrt(sampleVariance(values, fromIndex));
	}

    public static PRIMTYP sampleStddev(PRIMTYP[] values) {
		return (PRIMTYP)Math.sqrt(sampleVariance(values));
	}



    /**
     * Return the median average of the values passed as argument.
     *
     * @param values 1 or more values
     * @return the media value of the values
     * @throws IllegalArgumentException if no values are passed
     * @throws NullPointerException if a null object of type {@code double[]}
     *          array is passed
     */
    public static PRIMTYP median(PRIMTYP[] values) {
        if (values.length == 1)
            return values[0];
        PRIMTYP[] sorted = copyOf(values, values.length);
        Arrays.sort(sorted);
        final int i = sorted.length / 2;
        return (sorted.length % 2 == 0)
                ? (PRIMTYP)( (sorted[i - 1] + sorted[i]) / 2)
                : sorted[i];
    }

    public static void normalise(PRIMTYP[] src, PRIMTYP min, PRIMTYP max,
                                 PRIMTYP[] dst) {
        Checks.checkEqual(src.length, dst.length);
        PRIMTYP smin = min(src);
        PRIMTYP srange = (PRIMTYP)(max(src) - smin);
        PRIMTYP drange = (PRIMTYP)(max - min);
        for (int i = 0; i < src.length; i++)
            dst[i] = (PRIMTYP)( ( (src[i] - smin) / srange) * drange + min);
    }

    public static void normalise(PRIMTYP[] src, PRIMTYP[] dst) {
        normalise(src, (PRIMTYP)0, (PRIMTYP)1, dst);
    }

    public static PRIMTYP[] normalise(PRIMTYP[] src, PRIMTYP min, PRIMTYP max) {
        PRIMTYP[] dst = new PRIMTYP[src.length];
        normalise(src, min, max, dst);
        return dst;
    }

    public static PRIMTYP[] normalise(PRIMTYP[] src) {
        return normalise(src, (PRIMTYP)0, (PRIMTYP)1);
    }


---EOF---

if (( ${floatp[i]} == 1 ))
then
cat << "---EOF---" \
    | perl -pe "s/PRIMTYP/${PRIMTYP[i]}/g;" \
    | perl -pe "s/COMP\(([^,]*),([^)]*)\)/${COMP[i]}/g"


    public static PRIMTYP[] round(final PRIMTYP[] arr) {
        final PRIMTYP[] result = new PRIMTYP[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = (PRIMTYP)Math.round(arr[i]);
        return result;
    }

    public static PRIMTYP[] floor(final PRIMTYP[] arr) {
        final PRIMTYP[] result = new PRIMTYP[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = (PRIMTYP)Math.floor(arr[i]);
        return result;
    }

    public static PRIMTYP[] ceil(final PRIMTYP[] arr) {
        final PRIMTYP[] result = new PRIMTYP[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = (PRIMTYP)Math.ceil(arr[i]);
        return result;
    }

---EOF---
fi


fi
done  >> ${outfile}

cat << "---EOF---" >> ${outfile}

}

---EOF---


