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

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * A DataSink adapter that forwards all events to an encapsulated inner instance.
 *
 * @param <T> the type of DataSink being encapsulated
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class ForwardingDataSink<T extends DataSink>
        extends ForwardingChannel<T> implements DataSink, Closeable, Flushable {

    public ForwardingDataSink(T inner) {
        super(inner);
    }

    @Override
    public void endOfRecord() throws IOException {
        getInner().endOfRecord();
    }

    @Override
    public void writeByte(byte val) throws IOException {
        getInner().writeByte(val);
    }

    @Override
    public void writeChar(char val) throws IOException {
        getInner().writeChar(val);
    }

    @Override
    public void writeShort(short val) throws IOException {
        getInner().writeShort(val);
    }

    @Override
    public void writeInt(int val) throws IOException {
        getInner().writeInt(val);
    }

    @Override
    public void writeLong(long val) throws IOException {
        getInner().writeLong(val);
    }

    @Override
    public void writeDouble(double val) throws IOException {
        getInner().writeDouble(val);
    }

    @Override
    public void writeFloat(float val) throws IOException {
        getInner().writeFloat(val);
    }

    @Override
    public void writeString(String str) throws IOException {
        getInner().writeString(str);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return equals((ForwardingDataSink) obj);
    }

}
