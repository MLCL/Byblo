/*
 * Copyright (c) 2010-2011, University of Sussex
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
package uk.ac.susx.mlcl.byblo.allpairs;

import com.google.common.base.Objects;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.Seekable;
import uk.ac.susx.mlcl.lib.io.SeekableSource;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @param <T> The atomic data type
 * @param <P> Data offset type for seeking
 */
public class Chunker<T, P> implements SeekableSource<Chunk<T>, P>, Closeable {

    private static final Log LOG = LogFactory.getLog(Chunker.class);
    private static final long DEFAULT_MAX_CHUNK_SIZE = 1000;
    private long maxChunkSize = DEFAULT_MAX_CHUNK_SIZE;
    private final SeekableSource<T, P> inner;
    private final boolean seekable;

    public Chunker(SeekableSource<T, P> inner, long maxChunkSize) {
        this.inner = inner;
        this.seekable = inner instanceof Seekable;
        this.maxChunkSize = maxChunkSize;
    }

    public long getMaxChunkSize() {
        return maxChunkSize;
    }

    public void setMaxChunkSize(long maxChunkSize) {
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
        return new Chunk<T>("" + start + ":" + end, items);
    }

    @Override
    public boolean hasNext() throws IOException {
        return inner.hasNext();
    }

    @Override
    public void position(P offset) throws IOException {
        if (!seekable) {
            throw new UnsupportedOperationException(
                    "Not supported by wrapped instance.");
        }
        ((Seekable<P>) inner).position(offset);
    }

    @Override
    public P position() throws IOException {
        if (!seekable) {
            throw new UnsupportedOperationException(
                    "Not supported by wrapped instance.");
        }
        return ((Seekable<P>) inner).position();
    }

    @Override
    public void close() throws IOException {
        if (!(inner instanceof Closeable)) {
            throw new UnsupportedOperationException(
                    "Not supported by wrapped instance.");
        }
        ((Closeable) inner).close();
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
    }

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this).
                add("maxChunkSize", maxChunkSize).
                add("inner", inner).
                add("seekable", seekable);
    }
}
