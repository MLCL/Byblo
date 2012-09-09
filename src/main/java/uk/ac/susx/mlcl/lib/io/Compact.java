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

import uk.ac.susx.mlcl.lib.Checks;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class Compact {

    private Compact() {
    }

    public static DataSink compact(DataSink inner, int numColumns) {
        Checks.checkNotNull("inner", inner);
        assert !(inner instanceof DSink);
        assert !(inner instanceof Enumerated.SimpleDSink);
        assert !(inner instanceof Enumerated.ComplexDSink);
        return new DSink<DataSink>(inner, numColumns);
    }

    public static DataSource compact(DataSource inner, int numColumns) {
        Checks.checkNotNull("inner", inner);
        assert !(inner instanceof DSink);
        assert !(inner instanceof Enumerated.ComplexDSource);
        assert !(inner instanceof Enumerated.ComplexSDSource);
        assert !(inner instanceof Enumerated.SimpleDSource);
        assert !(inner instanceof Enumerated.SimpleSDSource);
        return new DSource<DataSource>(inner, numColumns);
    }

    public static SeekableDataSource compact(
            SeekableDataSource inner, int numColumns) {
        Checks.checkNotNull("inner", inner);
        assert !(inner instanceof DSink);
        assert !(inner instanceof Enumerated.ComplexDSource);
        assert !(inner instanceof Enumerated.ComplexSDSource);
        assert !(inner instanceof Enumerated.SimpleDSource);
        assert !(inner instanceof Enumerated.SimpleSDSource);
        return new SeekableDSource<SeekableDataSource>(inner, numColumns);
    }

    private static abstract class AbstractCompact<I> implements Closeable {

        final int numColumns;

        final I inner;

        Object currentHead;

        int column;

        private AbstractCompact(I inner, int numColumns) {
            Checks.checkNotNull("inner", inner);
            this.inner = inner;
            this.numColumns = numColumns;
            currentHead = null;
            column = 0;
        }

        @Override
        public void close() throws IOException {
            if (inner instanceof Closeable)
                ((Closeable) inner).close();
        }
    }

    private static class DSink<S extends DataSink>
            extends AbstractCompact<S>
            implements DataSink, Flushable {

        private DSink(S inner, int numColumns) {
            super(inner, numColumns);
        }

        @Override
        public void endOfRecord() throws IOException {
            column = 0;
        }

        boolean isNewHead(Object head) throws IOException {
            assert column == 0;
            if (currentHead != null && head.equals(currentHead)) {
                return false;
            } else {
                if (currentHead != null) {
                    inner.endOfRecord();
                    currentHead = null;
                }
                currentHead = head;
                return true;
            }
        }

        @Override
        public void writeByte(byte val) throws IOException {
            if (column > 0 || isNewHead(val))
                inner.writeByte(val);
            ++column;
        }

        @Override
        public void writeChar(char val) throws IOException {
            if (column > 0 || isNewHead(val))
                inner.writeChar(val);
            ++column;
        }

        @Override
        public void writeDouble(double val) throws IOException {
            if (column > 0 || isNewHead(val))
                inner.writeDouble(val);
            ++column;
        }

        @Override
        public void writeFloat(float val) throws IOException {
            if (column > 0 || isNewHead(val))
                inner.writeFloat(val);
            ++column;
        }

        @Override
        public void writeInt(int val) throws IOException {
            if (column > 0 || isNewHead(val))
                inner.writeInt(val);
            ++column;
        }

        @Override
        public void writeLong(long val) throws IOException {
            if (column > 0 || isNewHead(val))
                inner.writeLong(val);
            ++column;
        }

        @Override
        public void writeShort(short val) throws IOException {
            if (column > 0 || isNewHead(val))
                inner.writeShort(val);
            ++column;
        }

        @Override
        public void writeString(String val) throws IOException {
            if (column > 0 || isNewHead(val))
                inner.writeString(val);
            ++column;
        }

        @Override
        public void close() throws IOException {
            if (currentHead != null) {
                inner.endOfRecord();
                currentHead = null;
                flush();
            }
            super.close();
        }

        @Override
        public void flush() throws IOException {
            if (inner instanceof Flushable)
                ((Flushable) inner).flush();
        }
    }

    private static class DSource<S extends DataSource>
            extends AbstractCompact<S>
            implements DataSource {

        private DSource(S inner, int numColumns) {
            super(inner, numColumns);
        }

        @Override
        public boolean canRead() throws IOException {
            return inner.canRead();
        }

        @Override
        public boolean isEndOfRecordNext() throws IOException {
            return column == numColumns;
        }

        @Override
        public void endOfRecord() throws IOException {
            assert this.isEndOfRecordNext();
            column = 0;
            if (inner.isEndOfRecordNext()) {
                currentHead = null;
                inner.endOfRecord();
            }
        }

        @Override
        public byte readByte() throws IOException {
            if (column == 0) {
                column++;
                if (currentHead == null)
                    currentHead = inner.readByte();
                return (Byte) currentHead;
            } else {
                column++;
                return inner.readByte();
            }
        }

        @Override
        public char readChar() throws IOException {
            if (column == 0) {
                column++;
                if (currentHead == null)
                    currentHead = inner.readChar();
                return (Character) currentHead;
            } else {
                column++;
                return inner.readChar();
            }
        }

        @Override
        public double readDouble() throws IOException {
            if (column == 0) {
                column++;
                if (currentHead == null)
                    currentHead = inner.readDouble();
                return (Double) currentHead;
            } else {
                column++;
                return inner.readDouble();
            }
        }

        @Override
        public float readFloat() throws IOException {
            if (column == 0) {
                column++;
                if (currentHead == null)
                    currentHead = inner.readFloat();
                return (Float) currentHead;
            } else {
                column++;
                return inner.readFloat();
            }
        }

        @Override
        public int readInt() throws IOException {
            if (column == 0) {
                column++;
                if (currentHead == null)
                    currentHead = inner.readInt();
                return (Integer) currentHead;
            } else {
                column++;
                return inner.readInt();
            }
        }

        @Override
        public long readLong() throws IOException {
            if (column == 0) {
                column++;
                if (currentHead == null)
                    currentHead = inner.readLong();
                return (Long) currentHead;
            } else {
                column++;
                return inner.readLong();
            }
        }

        @Override
        public short readShort() throws IOException {
            if (column == 0) {
                column++;
                if (currentHead == null)
                    currentHead = inner.readShort();
                return (Short) currentHead;
            } else {
                column++;
                return inner.readShort();
            }
        }

        @Override
        public String readString() throws IOException {
            if (column == 0) {
                column++;
                if (currentHead == null)
                    currentHead = inner.readString();
                return (String) currentHead;
            } else {
                column++;
                return inner.readString();
            }
        }
    }

    private static class SeekableDSource<S extends SeekableDataSource>
            extends DSource<S>
            implements SeekableDataSource {

        private SeekableDSource(S inner, int numColumns) {
            super(inner, numColumns);
        }

        @Override
        public void position(Tell offset) throws IOException {
            column = offset.value(Integer.class);
            offset = offset.next();
            currentHead = offset.value(Object.class);
            offset = offset.next();
            inner.position(offset);
        }

        @Override
        public Tell position() throws IOException {
            return inner.position().
                    push(Object.class, currentHead).
                    push(Integer.class, column);
        }
    }
}
