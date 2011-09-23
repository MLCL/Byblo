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
package uk.ac.susx.mlcl.dttools.allpairs;

import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.Seekable;
import uk.ac.susx.mlcl.lib.io.SeekableSource;
import uk.ac.susx.mlcl.lib.io.Source;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Hamish Morgan (hamish.morgan@sussex.ac.uk)
 * @version 27th March 2011
 * @param <T>
 */
public class Chunker<T, S> implements SeekableSource<Chunk<T>, S>, Closeable {

//    private static final Logger LOG = Logger.getLogger(
//            Chunker.class.getName());

    private long maxChunkSize = 1000;

    private final SeekableSource<T, S> inner;

    private final boolean seekable;

    public Chunker(SeekableSource<T, S> inner, long maxChunkSize) {
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
        final List<T> items = new ArrayList<T>();
        int k = 0;
        while (k < maxChunkSize && inner.hasNext()) {
            items.add(inner.read());
            ++k;
        }
        return new Chunk<T>(items);
    }

    @Override
    public boolean hasNext() throws IOException {
        return inner.hasNext();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void position(S offset) throws IOException {
        if (!seekable)
            throw new UnsupportedOperationException(
                    "Not supported by wrapped instance.");
        ((Seekable) inner).position(offset);
    }

    @SuppressWarnings("unchecked")
    @Override
    public S position() throws IOException {
        if (!seekable)
            throw new UnsupportedOperationException(
                    "Not supported by wrapped instance.");
        return (S)((Seekable) inner).position();
    }

    @Override
    public void close() throws IOException {
        if (!(inner instanceof Closeable))
            throw new UnsupportedOperationException(
                    "Not supported by wrapped instance.");
        ((Closeable) inner).close();
    }
//
//    public void initialiseTask() throws IOException {
//        // Initialise source B
//
//        if (restartChunk > 0) {
//            long restartRecords = restartChunk * maxChunkSize;
//            if (LOG.isLoggable(Level.INFO))
//                LOG.log(Level.INFO,
//                        "Skipping {0} chunks ({1} records).",
//                        new Object[]{restartChunk, restartRecords});
//            currentRecord += skip(restartRecords);
//            currentChunk += restartChunk;
//        }
//    }
//
//    public void reset() throws IOException {
//        inner.position(startPosition);
//        currentChunk = 0;
//        currentRecord = startRecord;
//    }
//
//    private long skip(long count) throws IOException {
//        int i = 0;
//        while (i < count && inner.hasNext()) {
//            inner.read();
//            //                stats.incrementSourceReads();
//            ++i;
//        }
//        return i;
//    }
//
//    public boolean hasNextChunk() throws IOException {
//        return currentRecord < endRecord && inner.hasNext();
//    }
//
//    public Chunk<T> readChunk() throws IOException {
//        int count = Math.min(maxChunkSize, endRecord - currentRecord);
//        final List<T> items = new ArrayList<T>(
//                count);
//        final int start = currentRecord;
//        final int id = currentChunk;
//        int k = 0;
//        while (k < count && inner.hasNext()) {
//            T next = inner.read();
////            if (validHeads.length > next.key() && validHeads[next.key()] > 0)
//            items.add(next);
//            ++k;
//        }
//        currentRecord += items.size();
//        if (!items.isEmpty())
//            ++currentChunk;
//        return new Chunk<T>(items, start, id);
//    }
}
