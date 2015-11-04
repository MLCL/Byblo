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

import java.io.IOException;

/**
 * A DataSource adapter that forwards all method invocations to some encapsulated DataSource,
 *
 * @param <T> type of the encapsulated DataSource
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class ForwardingDataSource<T extends DataSource> extends ForwardingChannel<T> implements DataSource {

    public ForwardingDataSource(T inner) {
        super(inner);
    }

    @Override
    public void skipRecord() throws IOException {
        getInner().skipRecord();
    }

    @Override
    public void endOfRecord() throws IOException {
        getInner().endOfRecord();
    }

    @Override
    public boolean isEndOfRecordNext() throws IOException {
        return getInner().isEndOfRecordNext();
    }

    @Override
    public byte readByte() throws IOException {
        return getInner().readByte();
    }

    @Override
    public char readChar() throws IOException {
        return getInner().readChar();
    }

    @Override
    public short readShort() throws IOException {
        return getInner().readShort();
    }

    @Override
    public int readInt() throws IOException {
        return getInner().readInt();
    }

    @Override
    public long readLong() throws IOException {
        return getInner().readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return getInner().readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return getInner().readDouble();
    }

    @Override
    public String readString() throws IOException {
        return getInner().readString();
    }

    @Override
    public boolean canRead() throws IOException {
        return getInner().canRead();
    }

}
