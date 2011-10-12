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
package uk.ac.susx.mlcl.lib.io;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract class that holds functionality to read a Tab Separated Values file.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @param <T>
 */
public abstract class AbstractTSVSink<T>
        implements Sink<T>, Closeable, Flushable {

    private static final Log LOG = LogFactory.getLog(AbstractTSVSink.class);

    private static final char RECORD_DELIM = '\n';

    private static final char VALUE_DELIM = '\t';

    private final Writer out;

    public AbstractTSVSink(File file, Charset charset) throws FileNotFoundException, IOException {
        if (file == null)
            throw new NullPointerException("file == null");
        if (LOG.isDebugEnabled())
            LOG.debug("Opening file \"" + file + "\" for writing.");
        out = new BufferedWriter(
                new OutputStreamWriter(
                new FileOutputStream(file), charset));

    }

    @Override
    public void writeAll(Collection<? extends T> objs) throws IOException {
        for (T o : objs) {
            write(o);
        }
    }

    protected void writeRecordDelimiter() throws IOException {
        out.append(RECORD_DELIM);
    }

    protected void writeValueDelimiter() throws IOException {
        out.append(VALUE_DELIM);
    }

    protected void writeString(String str) throws IOException {
        out.write(str);
    }

    protected void writeInt(int val) throws IOException {
        writeString(Integer.toString(val));
    }

    protected void writeDouble(double val) throws IOException {
        writeString(Double.toString(val));
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    // XXX Check if this is strictly necessary
    @Override
    protected void finalize() throws Throwable {
        flush();
        close();
        super.finalize();
    }
}
