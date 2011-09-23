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
package uk.ac.susx.mlcl.dttools;

import uk.ac.susx.mlcl.lib.io.FileFactory;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.util.ResourceBundle;
import java.io.File;
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
public class ChunkTaskTest {

    public ChunkTaskTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("\n\nTesting ChunkTask");
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

    /**
     * Test of runTask method, of class ChunkTask.
     */
    @Test
    public void testRunTask() throws Exception {
        System.out.println("runTask");

        String str = "blah\nblah\nyackaty\nschmackaty";
        String[] expectedResult = str.split("\n");
        int maxChunkSize = 5;
        FileFactory fileFactory = new TempFileFactory();

        File in = File.createTempFile(getClass().getName(), "in");
        IOUtil.writeAll(in, IOUtil.DEFAULT_CHARSET, str);

        ChunkTask instance = new ChunkTask();
        instance.setSrcFile(in);
        instance.setMaxChunkSize(maxChunkSize);
        instance.setChunkFileFactory(fileFactory);

        instance.run();
        instance.throwException();

        int i = 0;
        for (File out : instance.getDestFiles()) {
            StringBuilder sb = new StringBuilder();
            IOUtil.readAll(out, IOUtil.DEFAULT_CHARSET, sb);
            assertEquals(expectedResult[i] + "\n", sb.toString());
            i++;
            out.delete();
        }
    }

    @Test
    public void testGetSetMaxChunkSize() {
        System.out.println("Testing setMaxChunkSize() and getMaxChunkSize()");
        int maxChunkSize = 1000;
        ChunkTask instance = new ChunkTask();
        instance.setMaxChunkSize(maxChunkSize);
        assertEquals(maxChunkSize, instance.getMaxChunkSize());
    }

    @Test
    public void testGetSetSrcFile() {
        System.out.println("Testing setSrcFile() and getSrcFile()");
        File sourceFile = new File(".");
        ChunkTask instance = new ChunkTask();
        instance.setSrcFile(sourceFile);
        assertEquals(sourceFile, instance.getSrcFile());
    }

    @Test
    public void testGetSetChunkFileFactory() {
        System.out.println(
                "Testing setChunkFileFactory() and getChunkFileFactory()");
        FileFactory chunkFileFactory = new TempFileFactory();
        ChunkTask instance = new ChunkTask();
        instance.setChunkFileFactory(chunkFileFactory);
        assertEquals(chunkFileFactory, instance.getChunkFileFactory());
    }

    @Test
    public void testCLI() throws IOException {
        System.out.println("Testing command line usage.");
        File tmp = File.createTempFile(getClass().getName(), "");
        ChunkTask instance = new ChunkTask();
        String[] args = {"-i", tmp.toString()};
        JCommander jc = new JCommander();
        jc.addObject(instance);
        jc.setDescriptionsBundle(ResourceBundle.getBundle(
                "uk.ac.susx.mlcl.dttools.strings"));
        jc.parse(args);
        assertEquals(tmp, instance.getSrcFile());
        tmp.delete();
        jc.usage();
    }
}
