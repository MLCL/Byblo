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
package uk.ac.susx.mlcl.lib.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for ExitTrapper utility.
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ExitTrapperTest {

    public ExitTrapperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        System.setSecurityManager(null);
    }

    @After
    public void tearDown() {
        System.setSecurityManager(null);
    }

    /**
     * Test of enableExistTrapping method, of class ExitTrapper.
     */
    @Test(expected = uk.ac.susx.mlcl.lib.test.ExitTrapper.ExitException.class,
          timeout = 1000)
    public void testEnableExistTrapping() {
        System.out.println("enableExistTrapping");
        ExitTrapper.enableExistTrapping();
        assertTrue("Exist trapping should be enabled.",
                   ExitTrapper.isExitTrappingEnabled());
        System.exit(Integer.MIN_VALUE);
    }

    /**
     * Test of disableExitTrapping method, of class ExitTrapper.
     */
    @Test(timeout = 1000)
    public void testDisableExitTrapping() {
        System.out.println("disableExitTrapping");
        ExitTrapper.disableExitTrapping();
        assertFalse("Exist trapping should be disabled.",
                    ExitTrapper.isExitTrappingEnabled());
    }

    /**
     * Test of toggleExitTrapping method, of class ExitTrapper.
     */
    @Test(expected = uk.ac.susx.mlcl.lib.test.ExitTrapper.ExitException.class,
          timeout = 1000)
    public void testToggleExitTrapping() {
        System.out.println("toggleExitTrapping");
        ExitTrapper.toggleExitTrapping();
        System.exit(1);
    }

    /**
     * Test of isExitTrappingEnabled method, of class ExitTrapper.
     */
    @Test(timeout = 1000)
    public void testIsExitTrappingEnabled() {
        System.out.println("isExitTrappingEnabled");
        assertFalse("Exist trapping should be disabled.",
                    ExitTrapper.isExitTrappingEnabled());
        ExitTrapper.enableExistTrapping();
        assertTrue("Exist trapping should be enabled.",
                   ExitTrapper.isExitTrappingEnabled());
        try {
            System.exit(0);
            fail("System.exit should have been trapped.");
        } catch (ExitTrapper.ExitException ex) {
            assertEquals(0, ex.getStatus());
            //pass
        }

        ExitTrapper.disableExitTrapping();
        assertFalse("Exist trapping should be disabled.",
                    ExitTrapper.isExitTrappingEnabled());
        ExitTrapper.toggleExitTrapping();
        assertTrue("Exist trapping should be enabled.",
                   ExitTrapper.isExitTrappingEnabled());
        try {
            System.exit(Integer.MAX_VALUE);
            fail("System.exit should have been trapped.");
        } catch (ExitTrapper.ExitException ex) {
            assertEquals(Integer.MAX_VALUE, ex.getStatus());
            //pass
        }

        ExitTrapper.toggleExitTrapping();
        assertFalse("Exist trapping should be disabled.",
                    ExitTrapper.isExitTrappingEnabled());
    }

    /**
     * Test of ExitException subclass of ExitTrapper.
     */
    @Test(timeout = 1000)
    public void testExitException() {
        System.out.println("ExitException");
        ExitTrapper.enableExistTrapping();
        int[] codes = new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE, 0, -1, 1,
            -Integer.MAX_VALUE, Integer.MAX_VALUE - 1};
        for (int code : codes) {
            try {
                System.exit(code);
                fail("System.exit(" + code + ") should have been trapped.");
            } catch (ExitTrapper.ExitException ex) {
                assertEquals(code, ex.getStatus());
                //pass
            }
        }
    }
}
