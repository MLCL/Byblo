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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Objects;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.lib.tasks.AbstractTask;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Copy a source file to a destination.
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Copy a file.")
public class CopyTask extends AbstractTask {

    private static final Log LOG = LogFactory.getLog(CopyTask.class);

    private static final int BUFFER_SIZE = 100000;

    @Parameter(names = {"-i", "--input-file"},
               description = "Source file that will be read")
    private File sourceFile = IOUtil.STDIN_FILE;

    @Parameter(names = {"-o", "--output-file"},
               description = "Destination file that will be writen to.")
    private File destFile = IOUtil.STDOUT_FILE;

    public CopyTask(File sourceFile, File destinationFile) {
        setSrcFile(sourceFile);
        setDstFile(destinationFile);
    }

    public CopyTask() {
    }

    @Override
    protected void initialiseTask() throws Exception {
    }

    @Override
    protected void finaliseTask() throws Exception {
    }

    @Override
    protected void runTask() throws Exception {
        if (LOG.isInfoEnabled())
            LOG.info("Copying file from \"" + getSrcFile() + "\" to \""
                    + getDstFile() + "\".");

        // Check the configuration state
        if (sourceFile.equals(destFile) && !IOUtil.isStdin(sourceFile))
            throw new IllegalStateException("sourceFile equals destinationFile");

        InputStream in = null;
        OutputStream out = null;
        try {
            in = IOUtil.openInputStream(getSrcFile());
            out = IOUtil.openOutputStream(getDstFile());

            byte[] b = new byte[BUFFER_SIZE];
            int i = in.read(b);
            while (i != -1) {
                out.write(b, 0, i);
                i = in.read(b);
            }

        } finally {
            if (in != null)
                in.close();
            if (out != null) {
                out.flush();
                out.close();
            }
        }
        if (LOG.isInfoEnabled())
            LOG.info("Completed copy.");
    }

    public final File getSrcFile() {
        return sourceFile;
    }

    public final File getDstFile() {
        return destFile;
    }

    public final void setSrcFile(final File sourceFile)
            throws NullPointerException {
        if (sourceFile == null)
            throw new NullPointerException("sourceFile is null");
        this.sourceFile = sourceFile;
    }

    public final void setDstFile(final File destFile)
            throws NullPointerException {
        if (destFile == null)
            throw new NullPointerException("destinationFile is null");
        this.destFile = destFile;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("in", sourceFile).
                add("out", destFile);
    }
}
