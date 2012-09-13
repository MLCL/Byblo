/*
 * Copyright (c) 2011-2012, University of Sussex
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

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import uk.ac.susx.mlcl.lib.Checks;

/**
 * Chunker is an ObjectSource adapter that buffers objects into fixed size
 * chunks.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @param <T> The atomic data type
 * @param <S> Type of encapsulated ObjectSource
 */
public class Chunker<T, S extends ObjectSource<T>>
        implements ObjectSource<Chunk<T>>, Closeable {

    private static final int DEFAULT_MAX_CHUNK_SIZE = 1000;

    private int maxChunkSize = DEFAULT_MAX_CHUNK_SIZE;

    private final S inner;

    private Chunker(S inner, int maxChunkSize) {
        this.inner = inner;
        this.maxChunkSize = maxChunkSize;
    }

    public static <T> ObjectSource<Chunk<T>> newInstance(
            ObjectSource<T> source, int maxChunkSize) {
        return new Chunker<T, ObjectSource<T>>(source, maxChunkSize);
    }

    public static <T, P> SeekableObjectSource<Chunk<T>, P> newSeekableInstance(
            SeekableObjectSource<T, P> source, int maxChunkSize) {
        return new SeekableChunker<T, P, SeekableObjectSource<T, P>>(
                source, maxChunkSize);
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    public void setMaxChunkSize(int maxChunkSize) {
        Checks.checkRangeExcl(maxChunkSize, 0, Integer.MAX_VALUE);
        this.maxChunkSize = maxChunkSize;
    }

    public S getInner() {
        return inner;
    }

    @Override
    public Chunk<T> read() throws IOException {
        final List<T> items = new ArrayList<T>(maxChunkSize);
        int k = 0;
        while (k < maxChunkSize && inner.hasNext()) {
            items.add(inner.read());
            ++k;
        }
        return new Chunk<T>("", items);
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

    @Override
    public String toString() {
        return "Chunker{" + "maxChunkSize=" + maxChunkSize + ", inner=" + inner + '}';
    }

    private static class SeekableChunker<T, P, S extends SeekableObjectSource<T, P>>
            extends Chunker<T, S>
            implements SeekableObjectSource<Chunk<T>, P> {

        private SeekableChunker(S inner, int maxChunkSize) {
            super(inner, maxChunkSize);
        }

        @Override
        public void position(P offset) throws IOException {
            getInner().position(offset);
        }

        @Override
        public P position() throws IOException {
            return getInner().position();
        }

    }
}
