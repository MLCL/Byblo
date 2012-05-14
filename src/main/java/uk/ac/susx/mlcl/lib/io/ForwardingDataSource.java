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

import java.io.Closeable;
import java.io.IOException;
import uk.ac.susx.mlcl.lib.Checks;

/**
 *
 * @param <T>
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class ForwardingDataSource<T extends DataSource>
        implements DataSource, Closeable {

    private final T inner;

    public ForwardingDataSource(T inner) {
        Checks.checkNotNull("inner", inner);
        this.inner = inner;
    }

    public final T getInner() {
        return inner;
    }

    @Override
    public void endOfRecord() throws IOException {
        inner.endOfRecord();
    }

    @Override
    public boolean isEndOfRecordNext() throws IOException {
        return inner.isEndOfRecordNext();
    }

    @Override
    public byte readByte() throws IOException {
        return inner.readByte();
    }

    @Override
    public char readChar() throws IOException {
        return inner.readChar();
    }

    @Override
    public short readShort() throws IOException {
        return inner.readShort();
    }

    @Override
    public int readInt() throws IOException {
        return inner.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return inner.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return inner.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return inner.readDouble();
    }

    @Override
    public String readString() throws IOException {
        return inner.readString();
    }

    @Override
    public boolean canRead() throws IOException {
        return inner.canRead();
    }

    @Override
    public void close() throws IOException {
        if (inner instanceof Closeable)
            ((Closeable) inner).close();
    }

    public boolean equals(ForwardingDataSource<?> other) {
        return this.inner == other.inner
                || (this.inner != null && this.inner.equals(other.inner));
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj != null
                               && getClass() == obj.getClass()
                               && equals((ForwardingDataSource) obj));
    }

    @Override
    public int hashCode() {
        return 67 * 3 + (this.inner != null ? this.inner.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "DataSourceAdapter{" + "inner=" + inner + '}';
    }
}
