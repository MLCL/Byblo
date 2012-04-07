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
package uk.ac.susx.mlcl.lib.io;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class that holds functionality to read a Tab Separated Values file.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class TSVSink implements Closeable, Flushable, DataSink {

    private static final Log LOG = LogFactory.getLog(TSVSink.class);

    private static final char RECORD_DELIM = '\n';

    private static final char VALUE_DELIM = '\t';

    private final Appendable out;

    private long row;

    private long column;

    public TSVSink(File file, Charset charset)
            throws FileNotFoundException, IOException {
        if (file == null)
            throw new NullPointerException("file == null");
        if (LOG.isDebugEnabled())
            LOG.debug("Opening file \"" + file + "\" for writing.");
        out = new BufferedWriter(
                new OutputStreamWriter(
                new FileOutputStream(file), charset));
        row = 0;
        column = 0;

    }

    @Override
    public void endOfRecord() throws IOException {
        writeRecordDelimiter();
    }

    private void writeRecordDelimiter() throws IOException {
        out.append(RECORD_DELIM);
        ++row;
        column = 0;
    }

    private void writeValueDelimiter() throws IOException {
        out.append(VALUE_DELIM);
    }

    @Override
    public void writeString(String str) throws IOException {
        assert str.indexOf(VALUE_DELIM) == -1;
        assert str.indexOf(RECORD_DELIM) == -1;
        if (column > 0)
            writeValueDelimiter();
        out.append(str);
        ++column;
    }

    @Override
    public void writeInt(int val) throws IOException {
        writeString(Integer.toString(val));
    }

    private final DecimalFormat DOUBLE_FORMAT =
            new DecimalFormat("###0.0#####;-###0.0#####");

    @Override
    public void writeDouble(double val) throws IOException {
        if (Double.compare(Math.floor(val), val) == 0) {
            writeInt((int) val);
        } else {
            writeString(DOUBLE_FORMAT.format(val));
        }


//        writeString(Double.toString(val));
    }

    @Override
    public void writeChar(char val) throws IOException {
        writeInt(val);
    }

    @Override
    public void writeByte(byte val) throws IOException {
        writeInt(val);
    }

    @Override
    public void writeShort(short val) throws IOException {
        writeInt(val);
    }

    @Override
    public void writeLong(long val) throws IOException {
        writeString(Long.toString(val));
    }

    @Override
    public void writeFloat(float val) throws IOException {
        writeString(Float.toString(val));
    }

    @Override
    public void close() throws IOException {
        if (out instanceof Closeable)
            ((Closeable) out).close();
    }

    @Override
    public void flush() throws IOException {
        if (out instanceof Flushable)
            ((Flushable) out).flush();
    }
}
