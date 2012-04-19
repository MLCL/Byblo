/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib.io;

import com.google.common.base.Predicate;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 *
 * @author hamish
 */
public class Deltas {

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
        public void close() throws IOException {
            if (inner instanceof Closeable)
                ((Closeable) inner).close();
        }
    }

    private static class DeltaIntSource<S extends DataSource>
            extends AbstractDeltaInt<S>
            implements DataSource, Closeable {

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
            implements DataSink, Closeable, Flushable {

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
