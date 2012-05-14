/*
 * Copyright (c) 2010-2012, University of Sussex
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
 * POSSIBILITY OF SUCH DAMAGE.To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ZipfianDistTest {

    public ZipfianDistTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
//
//    /**
//     * Test of getPopulationSize method, of class ZipfianDist.
//     */
//    @Test
//    public void testGetPopulationSize() {
//        System.out.println("getPopulationSize");
//        ZipfianDist instance = null;
//        int expResult = 0;
//        int result = instance.getPopulationSize();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getExponent method, of class ZipfianDist.
//     */
//    @Test
//    public void testGetExponent() {
//        System.out.println("getExponent");
//        ZipfianDist instance = null;
//        double expResult = 0.0;
//        double result = instance.getExponent();
//        assertEquals(expResult, result, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getRandom method, of class ZipfianDist.
//     */
//    @Test
//    public void testGetRandom() {
//        System.out.println("getRandom");
//        ZipfianDist instance = null;
//        Random expResult = null;
//        Random result = instance.getRandom();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setRandom method, of class ZipfianDist.
//     */
//    @Test
//    public void testSetRandom() {
//        System.out.println("setRandom");
//        Random random = null;
//        ZipfianDist instance = null;
//        instance.setRandom(random);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of random method, of class ZipfianDist.
//     */
//    @Test
//    public void testRandom() {
//        System.out.println("random");
//        ZipfianDist instance = null;
//        int expResult = 0;
//        int result = instance.random();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of pmf method, of class ZipfianDist.
//     */
//    @Test
//    public void testPmf() {
//        System.out.println("pmf");
//        int k = 0;
//        ZipfianDist instance = null;
//        double expResult = 0.0;
//        double result = instance.pmf(k);
//        assertEquals(expResult, result, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of cdf method, of class ZipfianDist.
//     */
//    @Test
//    public void testCdf() {
//        System.out.println("cdf");
//        int k = 0;
//        ZipfianDist instance = null;
//        double expResult = 0.0;
//        double result = instance.cdf(k);
//        assertEquals(expResult, result, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    @Test
    public void testCompareH0WithH1() {
        System.out.println("testCompareH0WithH1");
        for (int n = 1; n <= 1000; n++) {
            double h0 = ZipfianDist.harm0_s1(n);
            double h1 = ZipfianDist.harm1(n, 1);
//            System.out.printf("%d %f %f %n", n, h0, h1);
            assertEquals(h0, h1, 0.0000000000001);
        }
    }
//    /**
//     * Test of toString method, of class ZipfianDist.
//     */
//    @Test
//    public void testToString() {
//        System.out.println("toString");
//        ZipfianDist instance = null;
//        String expResult = "";
//        String result = instance.toString();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of equals method, of class ZipfianDist.
//     */
//    @Test
//    public void testEquals() {
//        System.out.println("equals");
//        Object obj = null;
//        ZipfianDist instance = null;
//        boolean expResult = false;
//        boolean result = instance.equals(obj);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of hashCode method, of class ZipfianDist.
//     */
//    @Test
//    public void testHashCode() {
//        System.out.println("hashCode");
//        ZipfianDist instance = null;
//        int expResult = 0;
//        int result = instance.hashCode();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
