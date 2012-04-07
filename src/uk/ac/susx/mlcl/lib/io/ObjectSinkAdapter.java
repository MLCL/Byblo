/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib.io;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @param <S>
 * @param <T>
 * @author hamish
 */
public abstract class ObjectSinkAdapter<S extends Sink<T>, T>
        implements Sink<T>, Closeable, Flushable, Serializable {

    private static final long serialVersionUID = 1L;

    private final S inner;

    public ObjectSinkAdapter(S inner) {
        this.inner = inner;
    }

    public S getInner() {
        return inner;
    }

    @Override
    public void write(T record) throws IOException {
        inner.write(record);
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

    public boolean equals(ObjectSinkAdapter<?, ?> other) {
        if (this.inner != other.inner && (this.inner == null || !this.inner.
                                          equals(other.inner)))
            return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return equals((ObjectSinkAdapter<?, ?>) obj);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.inner != null ? this.inner.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "ObjectSinkAdapter{" + "inner=" + inner + '}';
    }
}
