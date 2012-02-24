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
 * POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.susx.mlcl.byblo;

import uk.ac.susx.mlcl.lib.io.Files;
import uk.ac.susx.mlcl.lib.io.FileFactory;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;
import static uk.ac.susx.mlcl.TestConstants.*;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ChunkTaskTest {

    private static final String subject = ChunkTask.class.getName();

    /**
     * Test of runTask method, of class ChunkTask.
     */
    @Test(timeout=1000)
    public void testRunTask() throws Exception {
        System.out.println("runTask");

        String str = "blah\nblah\nyackaty\nschmackaty";
        String[] expectedResult = str.split("\n");
        int maxChunkSize = 5;
        FileFactory fileFactory = new TempFileFactory();

        File in = File.createTempFile(getClass().getName(), "in");
        Files.writeAll(in, DEFAULT_CHARSET, str);

        ChunkTask instance = new ChunkTask();
        instance.setSrcFile(in);
        instance.setMaxChunkSize(maxChunkSize);
        instance.setChunkFileFactory(fileFactory);

        instance.run();
        instance.throwException();

        int i = 0;
        for (File out : instance.getDestFiles()) {
            StringBuilder sb = new StringBuilder();
            Files.readAll(out, DEFAULT_CHARSET, sb);
            assertEquals(expectedResult[i] + "\n", sb.toString());
            i++;
            out.delete();
        }
    }

    @Test(timeout=1000)
    public void testGetSetMaxChunkSize() {
        System.out.println("Testing setMaxChunkSize() and getMaxChunkSize()");
        int maxChunkSize = 1000;
        ChunkTask instance = new ChunkTask();
        instance.setMaxChunkSize(maxChunkSize);
        assertEquals(maxChunkSize, instance.getMaxChunkSize());
    }

    @Test(timeout=1000)
    public void testGetSetSrcFile() {
        System.out.println("Testing setSrcFile() and getSrcFile()");
        File sourceFile = new File(".");
        ChunkTask instance = new ChunkTask();
        instance.setSrcFile(sourceFile);
        assertEquals(sourceFile, instance.getSrcFile());
    }

    @Test(timeout=1000)
    public void testGetSetChunkFileFactory() {
        System.out.println(
                "Testing setChunkFileFactory() and getChunkFileFactory()");
        FileFactory chunkFileFactory = new TempFileFactory();
        ChunkTask instance = new ChunkTask();
        instance.setChunkFileFactory(chunkFileFactory);
        assertEquals(chunkFileFactory, instance.getChunkFileFactory());
    }

    @Test(timeout=1000)
    public void testCLI() throws IOException {
        System.out.println("Testing command line usage.");
        File tmp = File.createTempFile(getClass().getName(), "");
        ChunkTask instance = new ChunkTask();
        String[] args = {"-i", tmp.toString()};
        JCommander jc = new JCommander();
        jc.addObject(instance);
        jc.parse(args);
        assertEquals(tmp, instance.getSrcFile());
        tmp.delete();
        jc.usage();
    }
}
