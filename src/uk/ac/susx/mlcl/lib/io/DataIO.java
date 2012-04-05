/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib.io;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import uk.ac.susx.mlcl.lib.Enumerator;

/**
 *
 * @author hamish
 */
public class DataIO {

    public static DataSource enumerated(
            DataSource src, Enumerator<String>[] enumerators) {
        return new StringEnumeratingDataSource(src, enumerators);
    }

    public static <P> SeekableDataSource<Tell<P>> enumerated(
            SeekableDataSource<P> src, Enumerator<String>[] enumerators) {
        return new StringEnumeratingSeekableDataSource<P>(src, enumerators);
    }

    public static DataSink enumerated(
            DataSink src, Enumerator<String>[] enumerators) {
        return new StringEnumeratingDataSink(src, enumerators);
    }

    public static class SimpleStringEnumeratingDataSource
            extends DataSourceAdapter<DataSource>
            implements DataSource, Closeable, Flushable {

        final Enumerator<String> enumerator;

        public SimpleStringEnumeratingDataSource(
                DataSource inner, Enumerator<String> enumerator) {
            super(inner);
            this.enumerator = enumerator;
        }

        @Override
        public String readString() throws IOException {
            return enumerator.value(super.readInt());
        }
    }

    public static class SimpleStringEnumeratingSeekableDataSource<P>
            extends SeekableDataSourceAdapter<SeekableDataSource<P>, P>
            implements SeekableDataSource<P>, Closeable, Flushable {

        final Enumerator<String> enumerator;

        public SimpleStringEnumeratingSeekableDataSource(
                SeekableDataSource<P> inner, Enumerator<String> enumerator) {
            super(inner);
            this.enumerator = enumerator;
        }

        @Override
        public String readString() throws IOException {
            return enumerator.value(super.readInt());
        }
    }

    public static class SimpleStringEnumeratingDataSink
            extends DataSinkAdapter {

        final Enumerator<String> enumerator;

        protected int column;

        public SimpleStringEnumeratingDataSink(
                DataSink inner,
                Enumerator<String> enumerator) {
            super(inner);
            this.enumerator = enumerator;
        }

        @Override
        public void writeString(String str) throws IOException {
            super.writeInt(enumerator.index(str));
        }
    }

    public static class StringEnumeratingDataSource
            extends DataSourceAdapter<DataSource>
            implements DataSource, Closeable, Flushable {

        final Enumerator<String>[] enumerators;

        protected int column;

        public StringEnumeratingDataSource(
                DataSource inner,
                Enumerator<String>[] enumerators) {
            super(inner);
            this.enumerators = enumerators;
            column = 0;
        }

        @Override
        public void endOfRecord() throws IOException {
            super.endOfRecord();
            column = 0;
        }

        @Override
        public byte readByte() throws IOException {
            ++column;
            return super.readByte();
        }

        @Override
        public char readChar() throws IOException {
            ++column;
            return super.readChar();
        }

        @Override
        public short readShort() throws IOException {
            ++column;
            return super.readShort();
        }

        @Override
        public int readInt() throws IOException {
            ++column;
            return super.readInt();
        }

        @Override
        public long readLong() throws IOException {
            ++column;
            return super.readLong();
        }

        @Override
        public float readFloat() throws IOException {
            ++column;
            return super.readFloat();
        }

        @Override
        public double readDouble() throws IOException {
            ++column;
            return super.readDouble();
        }

        @Override
        public String readString() throws IOException {
            final String str = (enumerators[column] != null)
                    ? enumerators[column].value(super.readInt())
                    : super.readString();
            ++column;
            return str;
        }
    }

    public static class Tell<P> {

        private P inner;

        private int column;

        public Tell(P inner, int column) {
            this.inner = inner;
            this.column = column;
        }
    }

    public static class StringEnumeratingSeekableDataSource<P>
            extends StringEnumeratingDataSource implements SeekableDataSource<Tell<P>> {

        private SeekableDataSource<P> inner;

        public StringEnumeratingSeekableDataSource(
                SeekableDataSource<P> inner,
                Enumerator<String>[] enumerators) {
            super(inner, enumerators);
        }

        @Override
        public void position(Tell<P> offset) throws IOException {
            this.column = offset.column;
            inner.position(offset.inner);
        }

        @Override
        public Tell<P> position() throws IOException {
            return new Tell<P>(inner.position(), column);
        }
    }

    public static class StringEnumeratingDataSink extends DataSinkAdapter {

        final Enumerator<String>[] enumerators;

        protected int column;

        public StringEnumeratingDataSink(
                DataSink inner,
                Enumerator<String>[] enumerators) {
            super(inner);
            this.enumerators = enumerators;
        }

        @Override
        public void writeByte(byte val) throws IOException {
            ++column;
            super.writeByte(val);
        }

        @Override
        public void writeChar(char val) throws IOException {
            ++column;
            super.writeChar(val);
        }

        @Override
        public void writeDouble(double val) throws IOException {
            ++column;
            super.writeDouble(val);
        }

        @Override
        public void writeFloat(float val) throws IOException {
            ++column;
            super.writeFloat(val);
        }

        @Override
        public void writeInt(int val) throws IOException {
            ++column;
            super.writeInt(val);
        }

        @Override
        public void writeLong(long val) throws IOException {
            ++column;
            super.writeLong(val);
        }

        @Override
        public void writeShort(short val) throws IOException {
            ++column;
            super.writeShort(val);
        }

        @Override
        public void writeString(String str) throws IOException {
            if (enumerators[column] == null)
                super.writeString(str);
            else
                super.writeInt(enumerators[column].index(str));
            ++column;
        }
    }

    private DataIO() {
    }
}
