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
package uk.ac.susx.mlcl.lib.commands;

import com.beust.jcommander.Parameter;
import com.google.common.base.Objects;
import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class FilePipeDelegate extends FileDeligate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Parameter(names = {"-i", "--input"}, description = "Source file that will be read", validateWith = InputFileValidator.class, required = true)
    private File sourceFile;

    @Parameter(names = {"-o", "--output"}, description = "Destination file that will be writen to.", validateWith = OutputFileValidator.class, required = true)
    private File destFile;

    public FilePipeDelegate(File sourceFile, File destinationFile, Charset charset) {
        super(charset);
        setSourceFile(sourceFile);
        setDestinationFile(destinationFile);
    }

    public FilePipeDelegate() {
    }

    public final File getSourceFile() {
        return sourceFile;
    }

    public final File getDestinationFile() {
        return destFile;
    }

    public final void setSourceFile(final File sourceFile) {
        if (sourceFile == null)
            throw new NullPointerException("sourceFile is null");
        this.sourceFile = sourceFile;
    }

    public final void setDestinationFile(final File destFile) {
        if (destFile == null)
            throw new NullPointerException("destinationFile is null");
        this.destFile = destFile;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("in", getSourceFile()).
                add("out", getDestinationFile());
    }

}
