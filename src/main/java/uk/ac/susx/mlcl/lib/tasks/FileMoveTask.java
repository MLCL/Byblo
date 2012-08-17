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
package uk.ac.susx.mlcl.lib.tasks;

import java.io.File;
import java.io.IOException;
import static java.text.MessageFormat.format;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class FileMoveTask extends FileCopyTask {

    private static final Log LOG = LogFactory.getLog(FileMoveTask.class);

    public FileMoveTask(File sourceFile, File destinationFile) {
        super(sourceFile, destinationFile);
    }

    public FileMoveTask() {
        super();
    }

    protected static void move(File from, File to) throws IOException {


        if (!from.renameTo(to)) {
            if (LOG.isDebugEnabled())
                LOG.debug("Cannot performan fast rename; falling back to copy");

            copy(from, to);

            if (!from.delete())
                throw new IOException(format("Unable to delete file {0}", from));
        }
    }

    @Override
    protected void runTask() throws Exception {
        progress.startAdjusting();
        progress.setState(State.RUNNING);
        progress.setMessage(format("Copying file from \"{0}\" to \"{1}\".",
                                   getSrcFile(), getDstFile()));
        progress.endAdjusting();


        // Check the configuration state
        if (getSrcFile().equals(getDstFile()))
            throw new IllegalStateException("sourceFile equals destinationFile");

        move(getSrcFile(), getDstFile());


        progress.setState(State.COMPLETED);

    }
}
