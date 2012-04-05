/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib.io;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 *
 * @param <T> 
 * @author hamish
 */
public abstract class DataSinkAdapter<T extends DataSink>
    implements DataSink, Closeable, Flushable {

    private final T inner;

    public DataSinkAdapter(T inner) {
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
    public void writeByte(byte val) throws IOException {
        inner.writeByte(val);
    }

    @Override
    public void writeChar(char val) throws IOException {
        inner.writeChar(val);
    }

    @Override
    public void writeShort(short val) throws IOException {
        inner.writeShort(val);
    }

    @Override
    public void writeInt(int val) throws IOException {
        inner.writeInt(val);
    }

    @Override
    public void writeLong(long val) throws IOException {
        inner.writeLong(val);
    }

    @Override
    public void writeDouble(double val) throws IOException {
        inner.writeDouble(val);
    }

    @Override
    public void writeFloat(float val) throws IOException {
        inner.writeFloat(val);
    }

    @Override
    public void writeString(String str) throws IOException {
        inner.writeString(str);
    }

    @Override
    public void close() throws IOException {
        if (inner instanceof Closeable)
            ((Closeable) inner).close();
    }

    @Override
    public void flush() throws IOException {
        if (inner instanceof Flushable)
            ((Flushable) inner).flush();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final DataSinkAdapter<?> other = (DataSinkAdapter) obj;
        if (this.inner != other.inner && (this.inner == null || !this.inner.equals(other.inner)))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.inner != null ? this.inner.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "DataSinkAdapter{" + "inner=" + inner + '}';
    }
    
}
