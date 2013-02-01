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

import uk.ac.susx.mlcl.lib.Checks;

import java.io.Flushable;
import java.io.IOException;
import java.util.*;

/**
 * Static utility class for use with the ObjectSink and ObjectSource interfaces.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class ObjectIO {

    private ObjectIO() {
    }

    public static <T> ObjectSink<T> nullSink() {
        return new ObjectSink<T>() {

            @Override
            public void write(T record) throws IOException {
                // nada
            }

            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public void close() {
                // cannot be closed
            }
        };
    }

    public static <T> ObjectSource<T> nullSource() {
        return new ObjectSource<T>() {

            @Override
            public T read() throws IOException {
                throw new UnsupportedOperationException("Null source cannot be read.");
            }

            @Override
            public boolean hasNext() throws IOException {
                return false;
            }

            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public void close() {
                // cannot be closed
            }
        };
    }

    public static <T> SeekableObjectSource<T, Void> nullSeekableSource() {
        return new SeekableObjectSource<T, Void>() {

            @Override
            public T read() throws IOException {
                throw new UnsupportedOperationException("Null source cannot be read.");
            }

            @Override
            public boolean hasNext() throws IOException {
                return false;
            }

            @Override
            public void position(Void offset) throws IOException {
                throw new UnsupportedOperationException("Null source has no position.");
            }

            @Override
            public Void position() throws IOException {
                return null;
            }

            @Override
            public boolean isOpen() {
                return true;
            }

            @Override
            public void close() {
                // cannot be closed
            }
        };
    }

    public static <T> ObjectSink<T> asSink(final Collection<T> collection) {
        try {
            return new ObjectMemoryStore<T>(collection).openObjectSink();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ObjectSource<T> asSource(final Collection<T> data) {
        try {
            return new ObjectMemoryStore<T>((Collection<T>) data).openObjectSource();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> SeekableObjectSource<T, Integer> asSource(final List<T> list) {
        try {
            return new ObjectMemoryStore<T>((List<T>) list).openSeekableObjectSource();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static <T> long copy(final Iterable<? extends T> source,
                                final ObjectSink<? super T> sink, final int limit) throws IOException {
        Checks.checkNotNull("source", source);
        Checks.checkNotNull("sink", sink);
        Checks.checkRangeIncl(limit, 0, Integer.MAX_VALUE);
        long count = 0;
        final Iterator<? extends T> it = source.iterator();
        while (it.hasNext() && count < limit) {
            sink.write(it.next());
            ++count;
        }
        return count;
    }

    public static <T> long copy(final Iterable<? extends T> source,
                                final ObjectSink<? super T> sink) throws IOException {
        return copy(source, sink, Integer.MAX_VALUE);
    }

    public static <T> int copy(final ObjectSource<? extends T> source,
                               final Collection<? super T> sink, final int limit) throws IOException {
        Checks.checkNotNull("source", source);
        Checks.checkNotNull("sink", sink);
        Checks.checkRangeIncl(limit, 0, Integer.MAX_VALUE);
        int count = 0;
        while (source.hasNext() && count < limit) {
            sink.add(source.read());
            ++count;
        }
        return count;
    }

    public static <T> long copy(final ObjectSource<? extends T> source,
                                final Collection<? super T> sink) throws IOException {
        return copy(source, sink, Integer.MAX_VALUE);
    }

    public static <T> void copy(ObjectSource<? extends T> src, ObjectSink<? super T> sink) throws IOException {
        while (src.hasNext()) {
            sink.write(src.read());
        }
        if (sink instanceof Flushable)
            ((Flushable) sink).flush();
    }

    public static long flush(ObjectSource<?> src) throws IOException {
        long count = 0;
        while (src.hasNext()) {
            src.read();
            ++count;
        }
        return count;
    }

    public static <T> List<T> readAll(ObjectSource<T> src) throws IOException {
        List<T> result = new ArrayList<T>();
        copy(src, result);
        return result;
    }

    public static <T> int compare(ObjectSource<T> a, ObjectSource<T> b, Comparator<T> comparator) throws IOException {
        while (a.hasNext() && b.hasNext()) {
            int c = comparator.compare(a.read(), b.read());
            if (c != 0)
                return c;
        }
        if (a.hasNext())
            return 1;
        if (b.hasNext())
            return -1;
        return 0;
    }

    public static <T extends Comparable<T>> int compare(ObjectSource<T> a, ObjectSource<T> b) throws IOException {
        while (a.hasNext() && b.hasNext()) {
            int c = a.read().compareTo(b.read());
            if (c != 0)
                return c;
        }
        if (a.hasNext())
            return 1;
        if (b.hasNext())
            return -1;
        return 0;
    }

    public static <T> boolean equals(ObjectSource<T> a, ObjectSource<T> b) throws IOException {
        while (a.hasNext() && b.hasNext()) {
            if (!a.read().equals(b.read()))
                return false;
        }
        return !(a.hasNext() || b.hasNext());
    }

}
