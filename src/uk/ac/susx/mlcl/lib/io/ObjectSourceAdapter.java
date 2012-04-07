/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib.io;

import java.io.Closeable;
import java.io.IOException;

/**
 *
 * @param <S>
 * @param <T>
 * @author hamish
 */
public abstract class ObjectSourceAdapter<S extends Source<T>, T>
        implements Source<T>, Closeable {

    private final S inner;

    public ObjectSourceAdapter(S inner) {
        this.inner = inner;
    }

    public S getInner() {
        return inner;
    }

    @Override
    public T read() throws IOException {
        return inner.read();
    }

    @Override
    public boolean hasNext() throws IOException {
        return inner.hasNext();
    }

    @Override
    public void close() throws IOException {
        if (inner instanceof Closeable)
            ((Closeable) inner).close();
    }

    public boolean equals(ObjectSourceAdapter<?, ?> other) {
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
        return equals((ObjectSourceAdapter<?, ?>) obj);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.inner != null ? this.inner.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "ObjectSourceAdapter{" + "inner=" + inner + '}';
    }
}
