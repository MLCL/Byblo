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

package uk.ac.susx.mlcl.lib.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.text.MessageFormat;

/**
* Created with IntelliJ IDEA.
* User: hiam20
* Date: 08/04/2013
* Time: 12:24
* To change this template use File | Settings | File Templates.
*/
public class DataFormatException extends IOException {

    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_MESSAGE = "Invalid format found when parsing file.";

    private final long offset;

    private final File file;

    private final Charset charset;

    public DataFormatException(String message, Throwable cause, long offset, File file, Charset charset) {
        super(message, cause);
        this.offset = offset;
        this.file = file;
        this.charset = charset;
    }

    public DataFormatException(Throwable cause, long offset, File file, Charset charset) {
        super(cause);
        this.offset = offset;
        this.file = file;
        this.charset = charset;
    }

    public DataFormatException(long offset, File file, Charset charset) {
        this.offset = offset;
        this.file = file;
        this.charset = charset;
    }

    public DataFormatException(String message, long offset, File file, Charset charset) {
        super(message);
        this.offset = offset;
        this.file = file;
        this.charset = charset;
    }

    public File getFile() {
        return file;
    }

    public long getOffset() {
        return offset;
    }

    @Override
    public String getMessage() {
        String context;
        try {
            context = '\'' + context() + '\'';
        } catch (final IOException ex) {
            // Yes ex is a dead store, but I don't really care about that
            // exception since we are trying to produce a report for
            // diagnosing another one.
            context = "";
        }

        return MessageFormat.format("Invalid format found when parsing file {0} near byte offset {1}:{2}: {3}",
                getFile(), getOffset(), context, super.getMessage());
    }

    private String context() throws IOException {
        final long start_offset = Math.max(offset - 32, 0);
        final int len = 64;

        RandomAccessFile in = null;
        try {
            in = new RandomAccessFile(file, "r");
            final byte[] bytes = new byte[len];
            in.seek(start_offset);
            final int nBytesRead = in.read(bytes);
            return new String(bytes, 0, nBytesRead, charset);
        } finally {
            if (in != null)
                in.close();
        }
    }
}
