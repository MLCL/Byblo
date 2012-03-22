/*
 * Copyright (c) 2010-2012, MLCL, University of Sussex
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

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import uk.ac.susx.mlcl.lib.Checks;

/**
 * Static utility class that provides methods for handling the pipeline API,
 * such as {@link Source}, {@link Sink} and related interfaces.
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class IOUtil {

    private IOUtil() {
    }

    public static <T> Sink<T> asSink(final Collection<T> collection) {
        return new Sink<T>() {

            @Override
            public void write(T record) throws IOException {
                collection.add(record);
            }

        };
    }

    public static <T> Source<T> asSource(final Iterable<T> iterable) {
        return new Source<T>() {

            private final Iterator<? extends T> it = iterable.iterator();

            @Override
            public T read() {
                return it.next();
            }

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

        };
    }

    public static <T> SeekableSource<T, Integer> asSource(
            final List<? extends T> list) {
        return new SeekableSource<T, Integer>() {

            private ListIterator<? extends T> it = list.listIterator();

            @Override
            public T read() {
                return it.next();
            }

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public void position(Integer offset) {
                it = list.listIterator(offset);
            }

            @Override
            public Integer position() {
                return it.nextIndex();
            }

        };
    }

    public static <T> int copy(final Iterable<? extends T> source,
                               final Sink<? super T> sink, final int limit) throws IOException {
        Checks.checkNotNull("source", source);
        Checks.checkNotNull("sink", sink);
        Checks.checkRangeIncl(limit, 0, Integer.MAX_VALUE);
        int count = 0;
        final Iterator<? extends T> it = source.iterator();
        while (it.hasNext() && count < limit) {
            sink.write(it.next());
            ++count;
        }
        return count;
    }

    public static <T> int copy(final Iterable<? extends T> source,
                               final Sink<? super T> sink) throws IOException {
        return copy(source, sink, Integer.MAX_VALUE);
    }

    public static <T> int copy(final Source<? extends T> source,
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

    public static <T> int copy(final Source<? extends T> source,
                               final Collection<? super T> sink) throws IOException {
        return copy(source, sink, Integer.MAX_VALUE);
    }

    public static <T> void copy(Source<T> src, Sink<T> sink) throws IOException {
        while (src.hasNext()) {
            sink.write(src.read());
        }
        if (sink instanceof Flushable)
            ((Flushable) sink).flush();
    }

    public static <T> List<T> readAll(Source<T> src) throws IOException {
        @SuppressWarnings("unchecked")
        List<T> result = (List<T>) new ArrayList<Object>();
        copy(src, result);
        return result;
    }

  
}
