/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib.io;

import java.io.Closeable;
import java.io.IOException;

/**
 *
 * @param <T>
 * @author hamish
 */
public abstract class DataSourceAdapter<T extends DataSource>
        implements DataSource, Closeable {

    private final T inner;

    public DataSourceAdapter(T inner) {
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


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final DataSourceAdapter<?> other = (DataSourceAdapter) obj;
        if (this.inner != other.inner && (this.inner == null || !this.inner.
                                          equals(other.inner)))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.inner != null ? this.inner.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "DataSourceAdapter{" + "inner=" + inner + '}';
    }
}
