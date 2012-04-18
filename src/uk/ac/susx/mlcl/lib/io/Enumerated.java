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
import java.io.IOException;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerator;

/**
 *
 * @author hamish
 */
public class Enumerated {

    private Enumerated() {
    }

    public static DataSource enumerated(
            DataSource inner, Enumerator<String> enumerator,
            Predicate<Integer> enumColumn) {
        Checks.checkNotNull("inner", inner);
        Checks.checkNotNull("enumerators", enumerator);
        Checks.checkNotNull("enumColumn", enumColumn);
        assert !(inner instanceof ComplexDSink);
        assert !(inner instanceof SimpleDSink);
        return new ComplexDSource2<DataSource>(inner, enumerator, enumColumn);
    }

    public static SeekableDataSource enumerated(
            SeekableDataSource inner, Enumerator<String> enumerator,
            Predicate<Integer> enumColumn) {
        Checks.checkNotNull("inner", inner);
        Checks.checkNotNull("enumerator", enumerator);
        Checks.checkNotNull("enumColumn", enumColumn);
        assert !(inner instanceof ComplexDSink);
        assert !(inner instanceof SimpleDSink);
        return new ComplexSDSource2<SeekableDataSource>(inner, enumerator,
                                                        enumColumn);
    }

    public static DataSink enumerated(
            DataSink inner, Enumerator<String> enumerator,
            Predicate<Integer> enumColumn) {
        Checks.checkNotNull("inner", inner);
        Checks.checkNotNull("enumerator", enumerator);
        Checks.checkNotNull("enumColumn", enumColumn);
        assert !(inner instanceof ComplexDSink);
        assert !(inner instanceof SimpleDSink);
        return new ComplexDSink2<DataSink>(inner, enumerator, enumColumn);
    }

    public static DataSource enumerated(
            DataSource inner, Enumerator<String>[] enumerators) {
        Checks.checkNotNull("inner", inner);
        Checks.checkNotNull("enumerators", enumerators);
        assert !(inner instanceof ComplexDSink);
        assert !(inner instanceof SimpleDSink);
        return new ComplexDSource<DataSource>(inner, enumerators);
    }

    public static SeekableDataSource enumerated(
            SeekableDataSource inner, Enumerator<String>[] enumerators) {
        Checks.checkNotNull("inner", inner);
        Checks.checkNotNull("enumerators", enumerators);
        assert !(inner instanceof ComplexDSink);
        assert !(inner instanceof SimpleDSink);
        return new ComplexSDSource<SeekableDataSource>(inner, enumerators);
    }

    public static DataSink enumerated(
            DataSink inner, Enumerator<String>[] enumerators) {
        Checks.checkNotNull("inner", inner);
        Checks.checkNotNull("enumerators", enumerators);
        assert !(inner instanceof ComplexDSink);
        assert !(inner instanceof SimpleDSink);
        return new ComplexDSink<DataSink>(inner, enumerators);
    }

    public static DataSource enumerated(
            DataSource inner, Enumerator<String> enumerator) {
        Checks.checkNotNull("inner", inner);
        Checks.checkNotNull("enumerator", enumerator);
        assert !(inner instanceof ComplexDSink);
        assert !(inner instanceof SimpleDSink);
        return new SimpleDSource(inner, enumerator);
    }

    public static SeekableDataSource enumerated(
            SeekableDataSource inner, Enumerator<String> enumerator) {
        Checks.checkNotNull("inner", inner);
        Checks.checkNotNull("enumerator", enumerator);
        assert !(inner instanceof ComplexDSink);
        assert !(inner instanceof SimpleDSink);
        return new SimpleSDSource<SeekableDataSource>(inner, enumerator);
    }

    public static DataSink enumerated(
            DataSink inner, Enumerator<String> enumerator) {
        Checks.checkNotNull("inner", inner);
        Checks.checkNotNull("enumerator", enumerator);
        assert !(inner instanceof ComplexDSink);
        assert !(inner instanceof SimpleDSink);
        return new SimpleDSink<DataSink>(inner, enumerator);
    }

    static class SimpleDSource
            extends ForwardingDataSource<DataSource>
            implements DataSource, Closeable {

        final Enumerator<String> enumerator;

        private SimpleDSource(
                DataSource inner, Enumerator<String> enumerator) {
            super(inner);
            this.enumerator = enumerator;
        }

        @Override
        public int readInt() throws IOException {
            return enumerator.indexOf(super.readString());
        }
    }

    static class SimpleSDSource<S extends SeekableDataSource>
            extends SeekableDataSourceAdapter<S>
            implements SeekableDataSource, Closeable {

        final Enumerator<String> enumerator;

        private SimpleSDSource(
                S inner, Enumerator<String> enumerator) {
            super(inner);
            this.enumerator = enumerator;
        }

        @Override
        public int readInt() throws IOException {
            return enumerator.indexOf(super.readString());
        }
    }

    static class SimpleDSink<S extends DataSink>
            extends ForwardingDataSink<S> {

        final Enumerator<String> enumerator;

        protected int column;

        private SimpleDSink(
                S inner,
                Enumerator<String> enumerator) {
            super(inner);
            this.enumerator = enumerator;
        }

        @Override
        public void writeInt(int val) throws IOException {
            super.writeString(enumerator.valueOf(val));
        }
    }

    static class ComplexDSource<T extends DataSource>
            extends ForwardingDataSource<T>
            implements DataSource, Closeable {

        final Enumerator<String>[] enumerators;

        protected int column;

        private ComplexDSource(
                T inner,
                Enumerator<String>[] enumerators) {
            super(inner);
            this.enumerators = enumerators;
            column = 0;
        }

        @Override
        public void endOfRecord() throws IOException {
            getInner().endOfRecord();
            column = 0;
        }

        @Override
        public byte readByte() throws IOException {
            ++column;
            return getInner().readByte();
        }

        @Override
        public char readChar() throws IOException {
            ++column;
            return getInner().readChar();
        }

        @Override
        public short readShort() throws IOException {
            ++column;
            return getInner().readShort();
        }

        @Override
        public int readInt() throws IOException {
            final int val =
                    (column < enumerators.length && enumerators[column] != null)
                    ? enumerators[column].indexOf(getInner().readString())
                    : getInner().readInt();
            ++column;
            return val;
        }

        @Override
        public long readLong() throws IOException {
            ++column;
            return getInner().readLong();
        }

        @Override
        public float readFloat() throws IOException {
            ++column;
            return getInner().readFloat();
        }

        @Override
        public double readDouble() throws IOException {
            ++column;
            return getInner().readDouble();
        }

        @Override
        public String readString() throws IOException {
            ++column;
            return getInner().readString();
        }
    }

    static class ComplexSDSource<S extends SeekableDataSource>
            extends ComplexDSource<S>
            implements SeekableDataSource {

        private ComplexSDSource(
                S inner, Enumerator<String>[] enumerators) {
            super(inner, enumerators);
        }

        @Override
        public void position(Tell offset) throws IOException {
            column = offset.value(Integer.class);
            getInner().position(offset.next());
        }

        @Override
        public Tell position() throws IOException {
            return getInner().position().push(Integer.class, column);
        }
    }

    static class ComplexDSink<S extends DataSink>
            extends ForwardingDataSink<S> {

        final Enumerator<String>[] enumerators;

        protected int column;

        private ComplexDSink(
                S inner,
                Enumerator<String>[] enumerators) {
            super(inner);
            this.enumerators = enumerators;
        }

        @Override
        public void endOfRecord() throws IOException {
            super.endOfRecord();
            column = 0;
        }

        @Override
        public void writeByte(byte val) throws IOException {
            ++column;
            getInner().writeByte(val);
        }

        @Override
        public void writeChar(char val) throws IOException {
            ++column;
            getInner().writeChar(val);
        }

        @Override
        public void writeDouble(double val) throws IOException {
            ++column;
            getInner().writeDouble(val);
        }

        @Override
        public void writeFloat(float val) throws IOException {
            ++column;
            getInner().writeFloat(val);
        }

        @Override
        public void writeInt(int val) throws IOException {
            if (column >= enumerators.length || enumerators[column] == null)
                getInner().writeInt(val);
            else
                getInner().writeString(enumerators[column].valueOf(val));
            ++column;
        }

        @Override
        public void writeLong(long val) throws IOException {
            ++column;
            getInner().writeLong(val);
        }

        @Override
        public void writeShort(short val) throws IOException {
            ++column;
            getInner().writeShort(val);
        }

        @Override
        public void writeString(String str) throws IOException {
            ++column;
            getInner().writeString(str);
        }
    }

    static class ComplexDSource2<T extends DataSource>
            extends ForwardingDataSource<T>
            implements DataSource, Closeable {

        final Enumerator<String> enumerator;

        final Predicate<Integer> enumColumn;

        protected int column;

        private ComplexDSource2(
                T inner,
                Enumerator<String> enumerator,
                Predicate<Integer> enumColumn) {
            super(inner);
            this.enumerator = enumerator;
            this.enumColumn = enumColumn;
            column = 0;
        }

        @Override
        public void endOfRecord() throws IOException {
            getInner().endOfRecord();
            column = 0;
        }

        @Override
        public byte readByte() throws IOException {
            ++column;
            return getInner().readByte();
        }

        @Override
        public char readChar() throws IOException {
            ++column;
            return getInner().readChar();
        }

        @Override
        public short readShort() throws IOException {
            ++column;
            return getInner().readShort();
        }

        @Override
        public int readInt() throws IOException {
            final int val =
                    enumColumn.apply(column)
                    ? enumerator.indexOf(getInner().readString())
                    : getInner().readInt();
            ++column;
            return val;
        }

        @Override
        public long readLong() throws IOException {
            ++column;
            return getInner().readLong();
        }

        @Override
        public float readFloat() throws IOException {
            ++column;
            return getInner().readFloat();
        }

        @Override
        public double readDouble() throws IOException {
            ++column;
            return getInner().readDouble();
        }

        @Override
        public String readString() throws IOException {
            ++column;
            return getInner().readString();
        }
    }

    static class ComplexSDSource2<S extends SeekableDataSource>
            extends ComplexDSource2<S>
            implements SeekableDataSource {

        private ComplexSDSource2(
                S inner, Enumerator<String> enumerator,
                Predicate<Integer> enumColumn) {
            super(inner, enumerator, enumColumn);
        }

        @Override
        public void position(Tell offset) throws IOException {
            column = offset.value(Integer.class);
            getInner().position(offset.next());
        }

        @Override
        public Tell position() throws IOException {
            return getInner().position().push(Integer.class, column);
        }
    }

    static class ComplexDSink2<S extends DataSink>
            extends ForwardingDataSink<S> {

        final Enumerator<String> enumerator;

        final Predicate<Integer> enumColumn;

        protected int column;

        private ComplexDSink2(
                S inner,
                Enumerator<String> enumerators,
                Predicate<Integer> enumColumn) {
            super(inner);
            this.enumerator = enumerators;
            this.enumColumn = enumColumn;
        }

        @Override
        public void endOfRecord() throws IOException {
            super.endOfRecord();
            column = 0;
        }

        @Override
        public void writeByte(byte val) throws IOException {
            ++column;
            getInner().writeByte(val);
        }

        @Override
        public void writeChar(char val) throws IOException {
            ++column;
            getInner().writeChar(val);
        }

        @Override
        public void writeDouble(double val) throws IOException {
            ++column;
            getInner().writeDouble(val);
        }

        @Override
        public void writeFloat(float val) throws IOException {
            ++column;
            getInner().writeFloat(val);
        }

        @Override
        public void writeInt(int val) throws IOException {
            if (enumColumn.apply(column))
                getInner().writeString(enumerator.valueOf(val));
            else
                getInner().writeInt(val);
            ++column;
        }

        @Override
        public void writeLong(long val) throws IOException {
            getInner().writeLong(val);
            ++column;
        }

        @Override
        public void writeShort(short val) throws IOException {
            getInner().writeShort(val);
            ++column;
        }

        @Override
        public void writeString(String str) throws IOException {
            getInner().writeString(str);
            ++column;
        }
    }
}
