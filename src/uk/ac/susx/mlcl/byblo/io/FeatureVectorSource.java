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
package uk.ac.susx.mlcl.byblo.io;

import uk.ac.susx.mlcl.lib.ObjectIndex;
import uk.ac.susx.mlcl.lib.collect.Entry;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import uk.ac.susx.mlcl.lib.collect.SparseVectors;
import uk.ac.susx.mlcl.lib.io.Lexer;
import uk.ac.susx.mlcl.lib.io.SeekableSource;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;

/**
 * Wraps a {@link FeatureSource} to produce complete feature vectors instead of
 * just individual feature/context pairs.
 * 
 * @author Hamish Morgan (hamish.morgan@sussex.ac.uk)
 * @version 27th March 2011
 */
public class FeatureVectorSource
        implements SeekableSource<Entry<SparseDoubleVector>, Lexer.Tell> {

    private final FeatureSource inner;

    private FeatureEntry next;

    private Lexer.Tell tell;

    public FeatureVectorSource(FeatureSource inner) {
        this.inner = inner;
        tell = Lexer.Tell.START;
        next = null;
    }

    public ObjectIndex<String> getHeadIndex() {
        return inner.getHeadIndex();
    }

    public ObjectIndex<String> getContextIndex() {
        return inner.getContextIndex();
    }

    @Override
    public boolean hasNext() throws IOException {
        return inner.hasNext() || next != null;
    }

    @Override
    public Entry<SparseDoubleVector> read() throws IOException {
        if (next == null)
            readNext();
        Int2DoubleMap features = new Int2DoubleOpenHashMap();
        FeatureEntry start = next;
        int cardinality = 0;
        do {
            features.put(next.getContextId(), next.getWeight());
            cardinality = Math.max(cardinality, next.getContextId() + 1);
            // XXX position() should not need to be called every iteration
            tell = inner.position();
            readNext();
        } while (next != null && next.getHeadId() == start.getHeadId());


        return new Entry<SparseDoubleVector>(
                start.getHeadId(),
                SparseVectors.toDoubleVector(features, cardinality));
    }

    @Override
    public void position(Lexer.Tell offset) throws IOException {
        inner.position(offset);
        tell = offset;
        readNext();
    }

    @Override
    public Lexer.Tell position() throws IOException {
        return tell;
    }

    private void readNext() throws IOException {
        try {
            next = inner.hasNext() ? inner.read() : null;
        } catch (CharacterCodingException e) {
            next = null;
            throw e;
        }
    }
}
