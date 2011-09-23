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
package uk.ac.susx.mlcl.byblo;

import uk.ac.susx.mlcl.byblo.DeleteTask;
import com.beust.jcommander.JCommander;
import java.util.ResourceBundle;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Hamish Morgan (hamish.morgan@sussex.ac.uk)
 * @version 27th March 2011
 */
public class DeleteTaskTest {

    public DeleteTaskTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("\n\nTesting DeleteTask");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        System.out.flush();
        System.err.flush();
    }

    @After
    public void tearDown() {
        System.out.flush();
        System.err.flush();
    }

 
    @Test
    public void testRun_success() throws Exception {
        System.out.println("Testing run() -- expecting success");
        File tmp = File.createTempFile(getClass().getName(), "");
        assertTrue(tmp.exists());
        DeleteTask instance = new DeleteTask(tmp);
        instance.run();
        instance.throwException();
        assertFalse(tmp.exists());
    }

    @Test(expected = IOException.class)
    public void testRunTask_failure() throws Exception {
        System.out.println("Testing runTask() -- expecting failure");
        File tmp = File.createTempFile(getClass().getName(), "");
        tmp.delete();
        assertFalse(tmp.exists());
        DeleteTask instance = new DeleteTask(tmp);
        instance.run();
        instance.throwException();
    }

    @Test(expected = IOException.class)
    public void testRun_failure() throws Exception {
        System.out.println("Testing runTask() -- expecting failure");
        File tmp = File.createTempFile(getClass().getName(), "");
        tmp.delete();
        assertFalse(tmp.exists());
        DeleteTask instance = new DeleteTask(tmp);
        instance.run();
        instance.throwException();
    }

    @Test
    public void testGetSetFile() throws IOException {
        System.out.println("Testing getFile() and setFile()");
        File tmp = File.createTempFile(getClass().getName(), "");
        DeleteTask instance = new DeleteTask(tmp);
        File expResult = tmp;
        assertEquals(expResult, instance.getFile());
        instance = new DeleteTask();
        instance.setFile(tmp);
        assertEquals(tmp, instance.getFile());
        tmp.delete();
    }

    @Test
    public void testCLI() throws IOException {
        System.out.println("Testing command line usage.");
        File tmp = File.createTempFile(getClass().getName(), "");
        DeleteTask instance = new DeleteTask();
        String[] args = {"-f", tmp.toString()};
        JCommander jc = new JCommander();
        jc.addObject(instance);
        jc.setDescriptionsBundle(ResourceBundle.getBundle(
                "uk.ac.susx.mlcl.byblo.strings"));
        jc.parse(args);
        assertEquals(tmp, instance.getFile());
        tmp.delete();
        jc.usage();
    }
}
