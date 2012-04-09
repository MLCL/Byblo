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
import uk.ac.susx.mlcl.lib.io.SeekableSource;
import java.io.IOException;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @param <T> The atomic type of items in this chunk
 */
public class Chunk<T> extends AbstractList<T>
        implements SeekableSource<T, Integer>, Cloneable {

    private String name;

    private final List<T> items;

    private Integer nextIndex;

    public Chunk(String name, List<T> items) {
        this.name = name;
        this.items = items;
        nextIndex = 0;
    }

    /**
     * Protected constructor for cloning only.
     *
     * @param other Chunk to clone (using a shallow copy).
     */
    protected Chunk(Chunk<T> other) {
        this.name = other.name;
        this.items = other.items;
        nextIndex = other.nextIndex;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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

    @Override
    public T read() throws IOException {
        return items.get(nextIndex++);
    }

    @Override
    public boolean hasNext() throws IOException {
        return nextIndex < items.size();
    }

    @Override
    public void position(Integer offset) throws IOException {
        nextIndex = offset;
    }

    @Override
    public Integer position() throws IOException {
        return nextIndex;
    }

    @Override
    public Chunk<T> clone() {
        return new Chunk<T>(this);
    }

    @Override
    public String toString() {
        return toStringHelper().toString();
    }

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this).addValue(name);
    }
}
