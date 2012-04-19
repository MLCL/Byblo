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
 * Utility class to hold a number of static maths functions.
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class MathUtil {

    public static final double PI = Math.PI;

    public static final double TWO_PI = 2d * PI;

    public static final double HALF_PI = 0.5d * PI;

    private MathUtil() {
    }

    /**
     * Normalise the angle theta: so it is in the range: 0 &lt;= a &lt; 2*pi
     * 
     * @param theta  angle to normalise (in radians)
     * @return  angle normalised
     */
    public static double normaliseAnglePositive(final double theta) {
        if (theta < 0d)
            return TWO_PI + (theta % TWO_PI);
        else
            return (theta % TWO_PI);
    }

    /**
     * Normalise the angle theta: so it is in the range: -pi &lt;= a &lt; pi
     * 
     * @param theta  angle to normalise (in radians)
     * @return  angle normalised
     */
    public static double normaliseAngle(final double theta) {
        final double trimmedAngle = theta % TWO_PI;
        if (trimmedAngle >= PI)
            return (trimmedAngle % PI) - PI;
        else if (trimmedAngle < -PI)
            return PI + (trimmedAngle % PI);
        else
            return trimmedAngle;
    }

    /**
     * Return the angle in radians between the 2d vectors defined by the
     * parameters.
     * 
     * @param ax x-axis component of the first vector
     * @param ay y-axis component of the first vector
     * @param bx x-axis component of the second vector
     * @param by y-axis component of the second vector
     * @return angle between the vectors
     */
    public static double angleBetween(
            final double ax, final double ay,
            final double bx, final double by) {
        return normaliseAngle(Math.atan2(ay, ax) - Math.atan2(by, bx));
    }

    /**
     * Calculate whether the two scalars (<tt>a</tt> and <tt>b</tt>) are equal, 
     * given some slack value <tt>epsilon</tt>. This allows floating point 
     * values to be compared for approximate equality, such that floating point
     * errors are alleviated.
     * 
     * @param a first value to compare
     * @param b second value to compare
     * @param epsilon amount of difference allowed
     * @return true if the values with epsilon, false otherwise
     */
    public static boolean epsilonEquals(
            final double a, final double b, final double epsilon) {
        final double diff = a - b;
        return diff < epsilon && diff > 0d - epsilon;
    }

}
