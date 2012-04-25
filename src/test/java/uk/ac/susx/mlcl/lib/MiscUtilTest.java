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

import java.util.Calendar;
import static org.junit.Assert.*;
import org.junit.Test;
import uk.ac.susx.mlcl.lib.collect.ArrayUtil;

/**
 *
 * @author Hamish Morgan
 */
public class MiscUtilTest {

    public MiscUtilTest() {
    }

    /**
     * Test of printableUtf8Character method, of class MiscUtil.
     */
    @Test
    public void testPrintableUTF8() {
        System.out.println("printableUTF8");

        for (int i = 0; i <= 127; i++) {
            final char ch = (char) i;
            final String result = MiscUtil.printableUTF8(ch);
            assertNotNull(result);
            if (i > 32 && i != 127) {
                // String should simply contain the character
                assertTrue("String should have length 1", result.length() == 1);
                assertEquals("Result should be the input character.", ch, result.charAt(0));
            } else {
                // String should contain some string name
                assertTrue("String should have length at least 1", result.length() >= 1);
                assertTrue("Result should not be the input character.", ch != result.charAt(0));
            }
        }
    }

    @Test
    public void testSizeof() throws Exception {

//        System.out.printf("Primitive int %d %n", MiscUtil.sizeOf((int) 1));
//        System.out.printf("Primitive long %d %n", MiscUtil.sizeOf((long) 1));
//        System.out.printf("Primitive double %d %n", MiscUtil.sizeOf((double) 1));
//        System.out.printf("Primitive float %d %n", MiscUtil.sizeOf((float) 1));
//        System.out.printf("Primitive short %d %n", MiscUtil.sizeOf((short) 1));
//        System.out.printf("Primitive char %d %n", MiscUtil.sizeOf((char) 1));
//        System.out.printf("Primitive byte %d %n", MiscUtil.sizeOf((byte) 1));
//        System.out.printf("Primitive boolean %d %n", MiscUtil.sizeOf(true));
//
//        System.out.printf("Integer %d %n", MiscUtil.sizeOf((Integer) (int) 1));
//        System.out.printf("Long %d %n", MiscUtil.sizeOf((Long) (long) 1));
//        System.out.printf("Double %d %n", MiscUtil.sizeOf((Double) (double) 1));
//        System.out.printf("Float %d %n", MiscUtil.sizeOf((Float) (float) 1.0));
//        System.out.printf("Short %d %n", MiscUtil.sizeOf((Short) (short) 1));
//        System.out.printf("Character %d %n", MiscUtil.sizeOf((Character) (char) 1));
//        System.out.printf("Byte %d %n", MiscUtil.sizeOf((Byte) (byte) 1));
//        System.out.printf("Boolean %d %n", MiscUtil.sizeOf((Boolean) (boolean) true));

        final int ARR_SIZE = 10000;
//
        System.out.printf("Primitive int array  %d %n", MiscUtil.sizeOf(new int[ARR_SIZE]));
        System.out.printf("Primitive long array  %d %n", MiscUtil.sizeOf(new long[ARR_SIZE]));
        System.out.printf("Primitive double array  %d %n", MiscUtil.sizeOf(new double[ARR_SIZE]));
        System.out.printf("Primitive float array  %d %n", MiscUtil.sizeOf(new float[ARR_SIZE]));
        System.out.printf("Primitive short array  %d %n", MiscUtil.sizeOf(new short[ARR_SIZE]));
        System.out.printf("Primitive char array  %d %n", MiscUtil.sizeOf(new char[ARR_SIZE]));
        System.out.printf("Primitive byte array  %d %n", MiscUtil.sizeOf(new byte[ARR_SIZE]));
        System.out.printf("Primitive boolean array %d %n", MiscUtil.sizeOf(new boolean[ARR_SIZE]));
//
//        System.out.printf("Empty Integer array  %d %n", MiscUtil.sizeOf(new Integer[ARR_SIZE]));
//        System.out.printf("Empty Long array  %d %n", MiscUtil.sizeOf(new Long[ARR_SIZE]));
//        System.out.printf("Empty Double array  %d %n", MiscUtil.sizeOf(new Double[ARR_SIZE]));
//        System.out.printf("Empty Float array  %d %n", MiscUtil.sizeOf(new Float[ARR_SIZE]));
//        System.out.printf("Empty Short array  %d %n", MiscUtil.sizeOf(new Short[ARR_SIZE]));
//        System.out.printf("Empty Character array  %d %n", MiscUtil.sizeOf(new Character[ARR_SIZE]));
//        System.out.printf("Empty Byte array  %d %n", MiscUtil.sizeOf(new Byte[ARR_SIZE]));
//        System.out.printf("Empty Boolean array %d %n", MiscUtil.sizeOf(new Boolean[ARR_SIZE]));

        System.out.printf("Integer array  %d %n", MiscUtil.sizeOf(ArrayUtil.box(new int[ARR_SIZE])));
        System.out.printf("Long array  %d %n", MiscUtil.sizeOf(ArrayUtil.box(new long[ARR_SIZE])));
        System.out.printf("Double array  %d %n", MiscUtil.sizeOf(ArrayUtil.box(new double[ARR_SIZE])));
        System.out.printf("Float array  %d %n", MiscUtil.sizeOf(ArrayUtil.box(new float[ARR_SIZE])));
        System.out.printf("Short array  %d %n", MiscUtil.sizeOf(ArrayUtil.box(new short[ARR_SIZE])));
        System.out.printf("Character array  %d %n", MiscUtil.sizeOf(ArrayUtil.box(new char[ARR_SIZE])));
        System.out.printf("Byte array  %d %n", MiscUtil.sizeOf(ArrayUtil.box(new byte[ARR_SIZE])));
        System.out.printf("Boolean array %d %n", MiscUtil.sizeOf(ArrayUtil.box(new boolean[ARR_SIZE])));

//
//        Calendar calendar = Calendar.getInstance();
//        System.out.printf("Calendar %d %n", MiscUtil.sizeOf(calendar));

    }

}
