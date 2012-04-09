/*
 * Copyright (c) 2010-2012, University of Sussex
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
package uk.ac.susx.mlcl.byblo.tasks;

import com.google.common.base.Objects;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.SeekableSource;
import java.io.Closeable;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @param <T> The atomic data type
 * @param <P> Data offset type for seeking
 */
public class Chunker<T, P> implements SeekableSource<Chunk<T>, P>, Closeable {

    private static final int DEFAULT_MAX_CHUNK_SIZE = 1000;

    private int maxChunkSize = DEFAULT_MAX_CHUNK_SIZE;

    private final SeekableSource<T, P> inner;

    public Chunker(SeekableSource<T, P> inner, int maxChunkSize) {
        this.inner = inner;
        this.maxChunkSize = maxChunkSize;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    public void setMaxChunkSize(int maxChunkSize) {
        Checks.checkRangeExcl(maxChunkSize, 0, Integer.MAX_VALUE);
        this.maxChunkSize = maxChunkSize;
    }

    @Override
    public Chunk<T> read() throws IOException {
        P start = inner.position();
        final List<T> items = new ArrayList<T>();
        int k = 0;
        while (k < maxChunkSize && inner.hasNext()) {
            items.add(inner.read());
            ++k;
        }
        P end = inner.position();
        return new Chunk<T>(
                MessageFormat.format("{0} to {1}", new Object[]{start, end}),
                items);
    }

    @Override
    public boolean hasNext() throws IOException {
        return inner.hasNext();
    }

    @Override
    public void position(P offset) throws IOException {
        inner.position(offset);
    }

    @Override
    public P position() throws IOException {
        return inner.position();
    }

    @Override
    public void close() throws IOException {
        if (inner instanceof Closeable)
            ((Closeable) inner).close();
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
    }

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this).
                add("maxChunkSize", maxChunkSize).
                add("inner", inner);
    }
}
