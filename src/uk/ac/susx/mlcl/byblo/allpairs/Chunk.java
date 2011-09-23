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

import uk.ac.susx.mlcl.lib.io.SeekableSource;
import java.io.IOException;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Hamish Morgan (hamish.morgan@sussex.ac.uk)
 * @version 27th March 2011
 * @param <T>
 */
public class Chunk<T> extends AbstractList<T>
        implements SeekableSource<T, Integer>, Cloneable {

    private final List<T> items;

    private Integer nextIndex;

    public Chunk(List<T> items) {
        this.items = items;
        nextIndex = 0;
    }

    protected Chunk(Chunk<T> other) {
        this.items = other.items;
        nextIndex = other.nextIndex;
    }

    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public T get(int index) {
        return items.get(index);
    }

    public T read() throws IOException {
        return items.get(nextIndex++);
    }

    public boolean hasNext() throws IOException {
        return nextIndex < items.size();
    }

    public void position(Integer offset) throws IOException {
        nextIndex = offset;
    }

    public Integer position() throws IOException {
        return nextIndex;
    }

    @Override
    public Chunk<T> clone() {
        return new Chunk<T>(this);
    }
}
