/*
 * Copyright (c) 2010-2013, University of Sussex
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
package uk.ac.susx.mlcl.byblo.tasks;

import org.junit.Test;
import uk.ac.susx.mlcl.lib.tasks.FileDeleteTask;
import uk.ac.susx.mlcl.testing.AbstractObjectTest;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class FileDeleteTaskTest extends AbstractObjectTest<FileDeleteTask> {

    @Override
    public Class<? extends FileDeleteTask> getImplementation() {
        return FileDeleteTask.class;
    }

    @Test
    public void testRun_success() throws Exception {
        File tmp = File.createTempFile(getClass().getName(), "");
        assertTrue(tmp.exists());
        FileDeleteTask instance = new FileDeleteTask(tmp);
        instance.run();
        while (instance.isExceptionTrapped())
            instance.throwTrappedException();
        assertFalse(tmp.exists());
    }

    @Test(expected = IOException.class)
    public void testRunTask_failure() throws Exception {
        File tmp = File.createTempFile(getClass().getName(), "");
        if (!tmp.delete())
            throw new IOException("Failed to delete file: " + tmp);
        assertFalse(tmp.exists());
        FileDeleteTask instance = new FileDeleteTask(tmp);
        instance.run();
        instance.throwTrappedException();
    }

    @Test(expected = IOException.class)
    public void testRun_failure() throws Exception {
        File tmp = File.createTempFile(getClass().getName(), "");
        if (!tmp.delete())
            throw new IOException("Failed to delete file: " + tmp);
        assertFalse(tmp.exists());
        FileDeleteTask instance = new FileDeleteTask(tmp);
        instance.run();
        instance.throwTrappedException();
    }

    @Test
    public void testGetSetFile() throws IOException {
        File expected = File.createTempFile(getClass().getName(), "");
        FileDeleteTask instance = new FileDeleteTask(expected);
        assertEquals(expected, instance.getFile());
        instance = new FileDeleteTask();
        instance.setFile(expected);
        assertEquals(expected, instance.getFile());
        if (!expected.delete())
            throw new IOException("Failed to delete file: " + expected);
    }
}
