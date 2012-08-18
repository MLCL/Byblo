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

import com.google.common.base.Predicate;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import javax.annotation.WillClose;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class Deltas {

    private Deltas() {
    }

    public static DataSink deltaInt(DataSink inner,
                                    Predicate<Integer> deltaCol) {
        return new DeltaIntSink<DataSink>(inner, deltaCol);
    }

    public static DataSource deltaInt(DataSource inner,
                                      Predicate<Integer> deltaCol) {
        return new DeltaIntSource<DataSource>(inner, deltaCol);
    }

    public static SeekableDataSource deltaInt(SeekableDataSource inner,
                                              Predicate<Integer> deltaCol) {
        return new SeekableDeltaIntSource<SeekableDataSource>(inner, deltaCol);
    }

    private static class AbstractDeltaInt<S>
            implements Closeable {

        final S inner;

        final Predicate<Integer> deltaColumn;

        int previous;

        int column;

        AbstractDeltaInt(S inner, Predicate<Integer> deltaCol) {
            this.inner = inner;
            this.deltaColumn = deltaCol;
            previous = 0;
            column = 0;
        }

        @Override
        @WillClose
        public void close() throws IOException {
            if (inner instanceof Closeable)
                ((Closeable) inner).close();
        }
    }

    private static class DeltaIntSource<S extends DataSource>
            extends AbstractDeltaInt<S>
            implements DataSource {

        DeltaIntSource(S inner, Predicate<Integer> deltaCol) {
            super(inner, deltaCol);
        }

        @Override
        public void endOfRecord() throws IOException {
            inner.endOfRecord();
            column = 0;
        }

        @Override
        public boolean isEndOfRecordNext() throws IOException {
            return inner.isEndOfRecordNext();
        }

        @Override
        public byte readByte() throws IOException {
            ++column;
            return inner.readByte();
        }

        @Override
        public char readChar() throws IOException {
            ++column;
            return inner.readChar();
        }

        @Override
        public short readShort() throws IOException {
            ++column;
            return inner.readShort();
        }

        @Override
        public int readInt() throws IOException {
            if (deltaColumn.apply(column)) {
                ++column;
                previous += inner.readInt();
                return previous;
            } else {
                ++column;
                return inner.readInt();
            }
        }

        @Override
        public long readLong() throws IOException {
            ++column;
            return inner.readLong();
        }

        @Override
        public float readFloat() throws IOException {
            ++column;
            return inner.readFloat();
        }

        @Override
        public double readDouble() throws IOException {
            ++column;
            return inner.readDouble();
        }

        @Override
        public String readString() throws IOException {
            ++column;
            return inner.readString();
        }

        @Override
        public boolean canRead() throws IOException {
            return inner.canRead();
        }
    }

    private static class DeltaIntSink<S extends DataSink>
            extends AbstractDeltaInt<S>
            implements DataSink, Flushable {

        DeltaIntSink(S inner,
                     Predicate<Integer> deltaCol) {
            super(inner, deltaCol);
        }

        @Override
        public void endOfRecord() throws IOException {
            inner.endOfRecord();
            column = 0;
        }

        @Override
        public void writeByte(byte val) throws IOException {
            inner.writeByte(val);
            ++column;
        }

        @Override
        public void writeChar(char val) throws IOException {
            inner.writeChar(val);
            ++column;

        }

        @Override
        public void writeShort(short val) throws IOException {
            inner.writeShort(val);
            ++column;

        }

        @Override
        public void writeInt(int val) throws IOException {
            if (deltaColumn.apply(column)) {
                inner.writeInt(val - previous);
                previous = val;
            } else {
                inner.writeInt(val);
            }
            ++column;

        }

        @Override
        public void writeLong(long val) throws IOException {
            inner.writeLong(val);
            ++column;

        }

        @Override
        public void writeDouble(double val) throws IOException {
            inner.writeDouble(val);
            ++column;

        }

        @Override
        public void writeFloat(float val) throws IOException {
            inner.writeFloat(val);
            ++column;

        }

        @Override
        public void writeString(String str) throws IOException {
            inner.writeString(str);
            ++column;
        }

        @Override
        public void flush() throws IOException {
            if (inner instanceof Flushable)
                ((Flushable) inner).flush();
        }
    }

    private static class SeekableDeltaIntSource<T extends SeekableDataSource>
            extends DeltaIntSource<T>
            implements SeekableDataSource {

        SeekableDeltaIntSource(T inner,
                               Predicate<Integer> deltaCol) {
            super(inner, deltaCol);
        }

        @Override
        public void position(Tell offset) throws IOException {
            previous = offset.value(Integer.class);
            offset = offset.next();
            column = offset.value(Integer.class);
            offset = offset.next();
            inner.position(offset);
        }

        @Override
        public Tell position() throws IOException {
            return inner.position().
                    push(Integer.class, column).
                    push(Integer.class, previous);
        }
    }
}
