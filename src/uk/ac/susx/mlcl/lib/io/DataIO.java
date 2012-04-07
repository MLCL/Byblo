/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib.io;

import java.io.Closeable;
import java.io.IOException;
import uk.ac.susx.mlcl.lib.Enumerator;

/**
 *
 * @author hamish
 */
public class DataIO {

    public static DataSource enumerated(
            DataSource src, Enumerator<String>[] enumerators) {
        return new StringEnumeratingDSource<DataSource>(src, enumerators);
    }

    public static SeekableDataSource<Tell> enumerated(
            SeekableDataSource<Tell> src, Enumerator<String>[] enumerators) {
        return new StringEnumeratingSDSource<SeekableDataSource<Tell>>(src, enumerators);
    }

    public static DataSink enumerated(
            DataSink src, Enumerator<String>[] enumerators) {
        return new StringEnumeratingDSink(src, enumerators);
    }

    public static DataSource enumerated(
            DataSource src, Enumerator<String> enumerator) {
        return new SimpleStringEnumeratingDSource(src, enumerator);
    }

    public static SeekableDataSource<Tell> enumerated(
            SeekableDataSource<Tell> src, Enumerator<String> enumerator) {
        return new SimpleStringEnumeratingSDSource(src, enumerator);
    }

    public static DataSink enumerated(
            DataSink src, Enumerator<String> enumerator) {
        return new SimpleStringEnumeratingDSink(src, enumerator);
    }

    public static DataSink compact(DataSink inner) {
        return new CompactDataSink(inner);
    }

    public static DataSource compact(DataSource inner, int numColumns) {
        return new CompactDataSource<DataSource>(inner, numColumns);
    }

    public static SeekableDataSource<Tell> compact(
            SeekableDataSource<Tell> inner, int numColumns) {
        return new CompactSeekableDataSource(inner, numColumns);
    }

    private static class SimpleStringEnumeratingDSource
            extends DataSourceAdapter<DataSource>
            implements DataSource, Closeable {

        final Enumerator<String> enumerator;

        private SimpleStringEnumeratingDSource(
                DataSource inner, Enumerator<String> enumerator) {
            super(inner);
            this.enumerator = enumerator;
        }
//
//        @Override
//        public String readString() throws IOException {
//            return enumerator.value(super.readInt());
//        }

        @Override
        public int readInt() throws IOException {
            return enumerator.index(super.readString());
        }
    }

    private static class SimpleStringEnumeratingSDSource
            extends SeekableDataSourceAdapter<SeekableDataSource<Tell>, Tell>
            implements SeekableDataSource<Tell>, Closeable {

        final Enumerator<String> enumerator;

        private SimpleStringEnumeratingSDSource(
                SeekableDataSource<Tell> inner, Enumerator<String> enumerator) {
            super(inner);
            this.enumerator = enumerator;
        }
//
//        @Override
//        public String readString() throws IOException {
//            return enumerator.value(super.readInt());
//        }

        @Override
        public int readInt() throws IOException {
            return enumerator.index(super.readString());
        }
    }

    private static class SimpleStringEnumeratingDSink
            extends DataSinkAdapter<DataSink> {

        final Enumerator<String> enumerator;

        protected int column;

        private SimpleStringEnumeratingDSink(
                DataSink inner,
                Enumerator<String> enumerator) {
            super(inner);
            this.enumerator = enumerator;
        }
//
//        @Override
//        public void writeString(String str) throws IOException {
//            super.writeInt(enumerator.index(str));
//        }

        @Override
        public void writeInt(int val) throws IOException {
            super.writeString(enumerator.value(val));
        }
    }

    private static class StringEnumeratingDSource<T extends DataSource>
            extends DataSourceAdapter<T>
            implements DataSource, Closeable {

        final Enumerator<String>[] enumerators;

        protected int column;

        private StringEnumeratingDSource(
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
                    ? enumerators[column].index(getInner().readString())
                    : getInner().readInt();
            ++column;
            return val;
//            return super.readInt();
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

    private static class StringEnumeratingSDSource<T extends SeekableDataSource<Tell>>
            extends StringEnumeratingDSource<T>
            implements SeekableDataSource<Tell> {

        private StringEnumeratingSDSource(
                T inner,
                Enumerator<String>[] enumerators) {
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

    private static class StringEnumeratingDSink
            extends DataSinkAdapter<DataSink> {

        final Enumerator<String>[] enumerators;

        protected int column;

        private StringEnumeratingDSink(
                DataSink inner,
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
                getInner().writeString(enumerators[column].value(val));
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

    private static class CompactDataSink
            extends DataSinkAdapter<DataSink> {

        private Object currentHead;

        private int column;

        private CompactDataSink(DataSink inner) {
            super(inner);
            column = 0;
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
                    super.endOfRecord();
                    currentHead = null;
                }
                currentHead = head;
                return true;
            }
        }

        @Override
        public void writeByte(byte val) throws IOException {
            if (column > 0 || isNewHead(val))
                getInner().writeByte(val);
            ++column;
        }

        @Override
        public void writeChar(char val) throws IOException {
            if (column > 0 || isNewHead(val))
                getInner().writeChar(val);
            ++column;
        }

        @Override
        public void writeDouble(double val) throws IOException {
            if (column > 0 || isNewHead(val))
                getInner().writeDouble(val);
            ++column;
        }

        @Override
        public void writeFloat(float val) throws IOException {
            if (column > 0 || isNewHead(val))
                getInner().writeFloat(val);
            ++column;
        }

        @Override
        public void writeInt(int val) throws IOException {
            if (column > 0 || isNewHead(val))
                getInner().writeInt(val);
            ++column;
        }

        @Override
        public void writeLong(long val) throws IOException {
            if (column > 0 || isNewHead(val))
                getInner().writeLong(val);
            ++column;
        }

        @Override
        public void writeShort(short val) throws IOException {
            if (column > 0 || isNewHead(val))
                getInner().writeShort(val);
            ++column;
        }

        @Override
        public void writeString(String val) throws IOException {
            if (column > 0 || isNewHead(val))
                getInner().writeString(val);
            ++column;
        }

        @Override
        public void close() throws IOException {
            if (currentHead != null) {
                getInner().endOfRecord();
                currentHead = null;
            }
            super.close();
        }
    }

    private static class CompactDataSource<T extends DataSource>
            extends DataSourceAdapter<T> {

        private final int numColumns;

        private Object currentHead;

        private int column;

        private CompactDataSource(T inner, int numColumns) {
            super(inner);
            this.numColumns = numColumns;
            currentHead = null;
            column = 0;
        }

        protected int getColumn() {
            return column;
        }

        protected void setColumn(int column) {
            this.column = column;
        }

        protected Object getCurrentHead() {
            return currentHead;
        }

        protected void setCurrentHead(Object currentHead) {
            this.currentHead = currentHead;
        }

        @Override
        public boolean isEndOfRecordNext() throws IOException {
            return column == numColumns;
        }

        @Override
        public void endOfRecord() throws IOException {
            assert this.isEndOfRecordNext();
            column = 0;
            if (getInner().isEndOfRecordNext()) {
                currentHead = null;
                getInner().endOfRecord();
            }
        }

        @Override
        public byte readByte() throws IOException {
            if (column == 0) {
                column++;
                if (currentHead == null)
                    currentHead = getInner().readByte();
                return (Byte) currentHead;
            } else {
                column++;
                return getInner().readByte();
            }
        }

        @Override
        public char readChar() throws IOException {
            if (column == 0) {
                column++;
                if (currentHead == null)
                    currentHead = super.readChar();
                return (Character) currentHead;
            } else {
                column++;
                return getInner().readChar();
            }
        }

        @Override
        public double readDouble() throws IOException {
            if (column == 0) {
                column++;
                if (currentHead == null)
                    currentHead = getInner().readDouble();
                return (Double) currentHead;
            } else {
                column++;
                return getInner().readDouble();
            }
        }

        @Override
        public float readFloat() throws IOException {
            if (column == 0) {
                column++;
                if (currentHead == null)
                    currentHead = getInner().readFloat();
                return (Float) currentHead;
            } else {
                column++;
                return getInner().readFloat();
            }
        }

        @Override
        public int readInt() throws IOException {
            if (column == 0) {
                column++;
                if (currentHead == null)
                    currentHead = getInner().readInt();
                return (Integer) currentHead;
            } else {
                column++;
                return getInner().readInt();
            }
        }

        @Override
        public long readLong() throws IOException {
            if (column == 0) {
                column++;
                if (currentHead == null)
                    currentHead = getInner().readLong();
                return (Long) currentHead;
            } else {
                column++;
                return getInner().readLong();
            }
        }

        @Override
        public short readShort() throws IOException {
            if (column == 0) {
                column++;
                if (currentHead == null)
                    currentHead = getInner().readShort();
                return (Short) currentHead;
            } else {
                column++;
                return getInner().readShort();
            }
        }

        @Override
        public String readString() throws IOException {
            if (column == 0) {
                column++;
                if (currentHead == null)
                    currentHead = getInner().readString();
                return (String) currentHead;
            } else {
                column++;
                return getInner().readString();
            }
        }
    }

    private static class CompactSeekableDataSource
            extends CompactDataSource<SeekableDataSource<Tell>>
            implements SeekableDataSource<Tell> {

        private CompactSeekableDataSource(SeekableDataSource<Tell> inner,
                                          int numColumns) {
            super(inner, numColumns);
        }

        @Override
        public void position(Tell offset) throws IOException {
            setColumn(offset.value(Integer.class));
            offset = offset.next();
            setCurrentHead(offset.value(Object.class));
            offset = offset.next();
            getInner().position(offset);
        }

        @Override
        public Tell position() throws IOException {
            return getInner().position().
                    push(Object.class, getCurrentHead()).
                    push(Integer.class, getColumn());
        }
    }

    private DataIO() {
    }
}
