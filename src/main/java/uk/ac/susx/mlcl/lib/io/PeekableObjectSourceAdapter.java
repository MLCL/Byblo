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


import javax.annotation.Nullable;
import java.io.IOException;

/**
 * An interface that defines
 *
 * @param <T>
 */
public final class PeekableObjectSourceAdapter<T>
        extends ForwardingObjectSource<ObjectSource<T>, T>
        implements PeekableObjectSource<T> {

    @Nullable
    private T head;

    public PeekableObjectSourceAdapter(final ObjectSource<T> inner) {
        super(inner);
        head = null;
    }

    public T peek() throws IOException {
        if (!hasNext())
            throw new IOException("Source exhausted.");
        return head;
    }

    @Override
    public T read() throws IOException {
        if (!hasNext())
            throw new IOException("Source exhausted.");

        final T previousHead = head;
        head = null;
        return previousHead;
    }

    @Override
    public boolean hasNext() throws IOException {
        if (head == null && getInner().hasNext()) {
            head = getInner().read();
        }
        return head != null;
    }

    @Override
    public void close() throws IOException {
        head = null;
        getInner().close();
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PeekableObjectSourceAdapter that = (PeekableObjectSourceAdapter) o;

        if (head != null ? !head.equals(that.head) : that.head != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (head != null ? head.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PeekableObjectSourceAdapter[" +
                "head=" + head +
                ", tail=" + getInner().toString() +
                ']';
    }
}
