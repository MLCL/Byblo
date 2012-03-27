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
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.commands.CopyCommand;
import uk.ac.susx.mlcl.lib.io.Files;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class CopyCommandTest {

    @Test(timeout=1000)
    public void testGetSrcFile() {
        System.out.println("Testing getSrcFile() and setSrcFile()");
        File x = new File("x");
        CopyCommand instance = new CopyCommand();
        instance.filesDeligate.setSourceFile(x);
        File expResult = x;
        File result = instance.filesDeligate.getSourceFile();
        assertEquals(expResult, result);
    }

    @Test(timeout=1000)
    public void testGetDstFile() {
        System.out.println("Testing getDstFile() and setDstFile()");
        File x = new File("x");
        CopyCommand instance = new CopyCommand();
        instance.filesDeligate.setDestinationFile(x);
        File expResult = x;
        File result = instance.filesDeligate.getDestinationFile();
        assertEquals(expResult, result);
    }

    @Test(timeout=1000)
    public void testRun_success() throws Exception {
        System.out.println("Testing run() -- expecting success");
        File in = File.createTempFile(getClass().getName(), "in");
        String str = "blah blah yackaty schmackaty";
        Files.writeAll(in, Files.DEFAULT_CHARSET, str);
        File out = File.createTempFile(getClass().getName(), "out");
        CopyCommand instance = new CopyCommand(in, out);
        instance.runCommand();
        assertTrue(out.exists());
        assertEquals(in.length(), out.length());

        StringBuilder sb = new StringBuilder();
        Files.readAll(out, Files.DEFAULT_CHARSET, sb);
        assertEquals(str, sb.toString());
        in.delete();
        out.delete();
    }

    @Test(expected = FileNotFoundException.class)
    public void testRun_failure_noinput() throws Exception {
        System.out.println("Testing run() -- expecting failure (no input)");
        File in = File.createTempFile(getClass().getName(), "in");
        in.delete();
        File out = File.createTempFile(getClass().getName(), "out");
        CopyCommand instance = new CopyCommand(in, out);
        instance.runCommand();
        in.delete();
        out.delete();
    }

    @Test(timeout=1000, expected=ParameterException.class)
    public void testCLI() throws IOException {
        System.out.println("Testing command line usage.");
        File x = new File("x"), y = new File("y");
        CopyCommand instance = new CopyCommand();
        String[] args = {"-i", x.toString(), "-o", y.toString()};
        JCommander jc = new JCommander();
        jc.addObject(instance);
        jc.parse(args);
        assertEquals(x, instance.filesDeligate.getSourceFile());
        assertEquals(y, instance.filesDeligate.getDestinationFile());
        jc.usage();
    }
}
