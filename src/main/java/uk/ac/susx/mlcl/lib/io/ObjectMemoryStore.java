/*
 * Copyright (c) 2010-2013, University of Sussex
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

import com.google.common.base.Preconditions;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Simple implementation of an ObjectStore that backs off to a Collection data structure, presumably storing everything
 * in memory. ("Presumably" because there's nothing stopping an enterprising developer using a collection interface for
 * a data structure that is not stored on disk; the JDBM library for example.)
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Nonnull
@NotThreadSafe
public class ObjectMemoryStore<T> implements ObjectStore<T, Integer> {

    private static final String SCHEME = ObjectMemoryStore.class.getSimpleName();

    private static final String DEFAULT_NAME = "unnamed";

    private static final Class<? extends Collection> DEFAULT_IMPLEMENTATION = ArrayList.class;

    /**
     * An descriptor for the store.
     */
    private final URI name;

    /**
     * The Collection implementation to be used.
     */
    private final Class<? extends Collection> implementation;

    /**
     * Whether or not this store supports Seekable
     */
    private final boolean seekable;

    /**
     * The Collection instance to be used; either given in the constructor or instantiated on {@link #touch()}.
     */
    @Nullable
    private Collection<T> data;


    /**
     * Construct a new <code>ObjectStore</code> that will use the given <code>implementation</code> class when it is
     * created.
     *
     * @param name           arbitrary string description of the store
     * @param implementation class used to implemented backing storage
     */
    public ObjectMemoryStore(final String name, final Class<? extends Collection> implementation) {
        Preconditions.checkNotNull(name, "name");
        Preconditions.checkNotNull(implementation, "implementation");

        try {
            // Check there is a default constructor
            implementation.getConstructor();

            this.name = new URI(SCHEME, name, "");
            this.implementation = implementation;
            this.seekable = List.class.isAssignableFrom(implementation);
            this.data = null;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Implementation class must have a default (no argument) constructor.");
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Constructor creates a new instance, with the given <code>name</code>, which backs-off to an {@link ArrayList}
     * collection.
     *
     * @param name arbitrary string description of the store
     */
    public ObjectMemoryStore(final String name) {
        this(name, DEFAULT_IMPLEMENTATION);
    }

    /**
     * Default constructor creates a new unnamed instance that backs-off to an {@link ArrayList} collection.
     */
    public ObjectMemoryStore() {
        this(DEFAULT_NAME);
    }

    /**
     * Constructor creates a new instance, with the given <code>name</code>, which backs-off to the given
     * <code>data</code> collection.
     *
     * @param name arbitrary string description of the store
     * @param data backing storage
     */
    public <S extends Collection<T>> ObjectMemoryStore(final String name, final S data) {
        Preconditions.checkNotNull(name, "name");
        Preconditions.checkNotNull(data, "data");
        try {
            this.name = new URI(SCHEME, name, "");
            this.implementation = data.getClass();
            this.seekable = List.class.isAssignableFrom(implementation);
            this.data = data;
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Constructor creates a new unnamed instance, which backs-off to the given <code>data</code> collection.
     *
     * @param data backing storage
     */
    public ObjectMemoryStore(final Collection<T> data) {
        this(DEFAULT_NAME, data);
    }


    @Override
    public ObjectSource<T> openObjectSource() throws IOException {
        if (!exists())
            throw new IOException("Store does not exist.");
        return new Source();
    }

    @Override
    public SeekableObjectSource<T, Integer> openSeekableObjectSource() throws IOException {
        if (!exists())
            throw new IOException("Store does not exist.");
        if (!isSeekable())
            throw new IOException("Store does not support random access.");
        return new SeekableSource();
    }

    @Override
    public ObjectSink<T> openObjectSink() throws IOException {
        if (!exists() && !touch())
            throw new AssertionError();
        return new Sink();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean touch() throws IOException {
        try {
            if (data == null) {
                data = (Collection<T>) implementation.newInstance();
                return true;
            } else {
                return false;
            }
        } catch (InstantiationException e) {
            throw new IOException(e);
        } catch (IllegalAccessException e) {
            throw new IOException(e);
        }
    }

    @Override
    @CheckReturnValue
    public boolean free() {
        if (data != null) {
            data.clear();
            data = null;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean exists() {
        return data != null;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    public boolean isSeekable() {
        return seekable;
    }

    @Override
    public URI getURI() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ObjectMemoryStore)) return false;
        ObjectMemoryStore that = (ObjectMemoryStore) o;
        if (seekable != that.seekable) return false;
        if (data != null ? !data.equals(that.data) : that.data != null) return false;
        if (!implementation.equals(that.implementation)) return false;
        if (!name.equals(that.name)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + implementation.hashCode();
        result = 31 * result + (seekable ? 1 : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + "name='" + name + '\'' + '}';
    }

    private class Source implements ObjectSource<T> {

        @Nullable
        private Iterator<? extends T> it = data.iterator();

        @Override
        public T read() throws IOException {
            if (!isOpen())
                throw new IOException("Source is closed.");
            return it.next();
        }

        @Override
        public boolean hasNext() throws IOException {
            if (it == null)
                throw new IOException("Source is closed.");
            return it.hasNext();
        }

        @Override
        public boolean isOpen() {
            return it != null;
        }

        @Override
        public void close() {
            it = null;
        }
    }

    private class SeekableSource implements SeekableObjectSource<T, Integer> {

        @Nullable
        private ListIterator<? extends T> it = ((List<T>) data).listIterator();

        @Override
        public T read() throws IOException {
            if (it == null)
                throw new IOException("Source is closed.");
            return it.next();
        }

        @Override
        public boolean hasNext() throws IOException {
            if (!isOpen())
                throw new IOException("Source is closed.");
            return it.hasNext();
        }

        @Override
        public void position(Integer offset) throws IOException {
            if (!isOpen())
                throw new IOException("Source is closed.");
            it = ((List<T>) data).listIterator();
        }

        @Override
        public Integer position() throws IOException {
            if (!isOpen())
                throw new IOException("Source is closed.");
            return it.nextIndex();
        }

        @Override
        public boolean isOpen() {
            return it != null;
        }

        @Override
        public void close() {
            it = null;
        }
    }

    private class Sink implements ObjectSink<T> {

        private boolean open = true;

        @Override
        public void write(T record) throws IOException {
            if (!isOpen())
                throw new IOException("Sink is closed.");
            data.add(record);
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() {
            open = false;
        }
    }

}
