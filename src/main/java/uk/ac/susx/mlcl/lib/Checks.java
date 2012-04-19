/*
 * Copyright (c) 2010-2012, MLCL Lab, University of Sussex
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

/**
 * Static utility class used for checking method arguments.
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class Checks {

    private Checks() {
    }

    public static void checkLE(String message, double... vals) {
        for (int i = 0; i < vals.length - 1; i++)
            if (vals[i + 1] > vals[i])
                throw new IllegalArgumentException(message);
    }

    public static void checkEqual(double a, double b) {
        if (a != b)
            throw new IllegalArgumentException("arguments not equal");
    }

    public static void checkEqual(long a, long b) {
        if (a != b)
            throw new IllegalArgumentException("arguments not equal");
    }

    public static void checkEqual(String str, double a, double b) {
        if (a != b)
            throw new IllegalArgumentException("arguments not equal: " + str);
    }

    public static void checkEquals(double... a) {
        for (int i = 0; i < a.length - 1; i++)
            if (a[i] != a[i + 1])
                throw new IllegalArgumentException("arguments not equal");
    }

    public static void checkEquals(String str, double... a) {
        for (int i = 0; i < a.length - 1; i++)
            if (a[i] != a[i + 1])
                throw new IllegalArgumentException("arguments not equal: " + str);
    }

    public static void checkArrayIndex(int index, int size) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + size);
    }

    public static void checkArrayIndex(String str, int index, int size) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(
                    str + " (index: " + index + ", size: " + size + ")");
    }

    public static void checkReal(final String name, final double num) {
        if (java.lang.Double.isNaN(num)
                || java.lang.Double.isInfinite(num))
            throw new IllegalArgumentException(
                    "Expecting argument '" + name
                    + "' to be areal number but found " + num);
    }

    public static void checkReal(final double num) {
        if (java.lang.Double.isNaN(num)
                || java.lang.Double.isInfinite(num))
            throw new IllegalArgumentException(
                    "Expecting argument to be a real number but found " + num);
    }

    public static void checkNonZero(final double num) {
        if (num == 0 || num == -0)
            throw new IllegalArgumentException(
                    "Expecting argument to be non-zero but found " + num);
    }

    public static void checkNonZero(final String name, final double num) {
        if (Double.isNaN(num) || num == 0 || num == -0)
            throw new IllegalArgumentException(
                    "Expecting argument '" + name
                    + "' to be non-zero but found " + num);
    }

    public static void checkNonZero(final int num) {
        if (num == 0 || num == -0)
            throw new IllegalArgumentException(
                    "Expecting argument to be non-zero but found " + num);
    }

    public static void checkNonZero(final String name, final int num) {
        if (Double.isNaN(num) || num == 0 || num == -0)
            throw new IllegalArgumentException(
                    "Expecting argument '" + name
                    + "' to be non-zero but found " + num);
    }

    public static void checkRangeIncl(String name, final double num, double min,
                                      double max) {
        if (Double.isNaN(num) || num < min || num > max)
            throw new IllegalArgumentException(
                    "Expecting argument '" + name + "' to be in the range ["
                    + min + ":" + max + "] inclusive, found " + num);
    }

    public static void checkRangeIncl(final double num, double min,
                                      double max) {
        if (Double.isNaN(num) || num < min || num > max)
            throw new IllegalArgumentException(
                    "Expecting argument to be in the range [" + min + ":"
                    + max + "] inclusive, found " + num);
    }

    public static void checkRangeExcl(String name, final double num, double min,
                                      double max) {
        if (Double.isNaN(num) || num <= min || num >= max)
            throw new IllegalArgumentException(
                    "Expecting argument '" + name + "' to be in the range ["
                    + min + ":" + max + "] exclusive, found " + num);
    }

    public static void checkRangeExcl(final double num, double min,
                                      double max) {
        if (Double.isNaN(num) || num <= min || num >= max)
            throw new IllegalArgumentException(
                    "Expecting argument to be in the range [" + min + ":"
                    + max + "] exclusive, found " + num);
    }

    public static void checkRangeIncl(String name, final int num, int min,
                                      int max) {
        if (Double.isNaN(num) || num < min || num > max)
            throw new IllegalArgumentException(
                    "Expecting argument '" + name + "' to be in the range ["
                    + min + ":" + max + "] inclusive, found " + num);
    }

    public static void checkRangeIncl(final int num, int min,
                                      int max) {
        if (Double.isNaN(num) || num < min || num > max)
            throw new IllegalArgumentException(
                    "Expecting argument to be in the range [" + min + ":"
                    + max + "] inclusive, found " + num);
    }

    public static void checkRangeExcl(String name, final int num, int min,
                                      int max) {
        if (Double.isNaN(num) || num <= min || num >= max)
            throw new IllegalArgumentException(
                    "Expecting argument '" + name + "' to be in the range ["
                    + min + ":" + max + "] exclusive, found " + num);
    }

    public static void checkRangeExcl(final int num, int min,
                                      int max) {
        if (Double.isNaN(num) || num <= min || num >= max)
            throw new IllegalArgumentException(
                    "Expecting argument to be in the range [" + min + ":"
                    + max + "] exclusive, found " + num);
    }

    public static void checkNotNull(final Object o)
            throws NullPointerException {
        if (o == null)
            throw new NullPointerException(
                    "Argument is null.");
    }

    public static void checkNotNull(final String name, final Object o)
            throws NullPointerException {
        if (o == null)
            throw new NullPointerException(
                    "Argument '" + name + "' is null.");
    }

}
