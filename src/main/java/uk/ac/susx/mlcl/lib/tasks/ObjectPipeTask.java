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
package uk.ac.susx.mlcl.lib.tasks;

import com.google.common.base.Objects;
import java.io.Flushable;
import java.io.IOException;
import java.io.Serializable;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.ObjectSource;

/**
 * @param <T>
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ObjectPipeTask<T> extends AbstractTask implements Serializable {

    private static final long serialVersionUID = 1L;

    private ObjectSource<T> source;

    private ObjectSink<T> sink;

    public ObjectPipeTask(ObjectSource<T> source, ObjectSink<T> sink) {
        setSource(source);
        setSink(sink);
    }

    public ObjectPipeTask() {
    }

    public final ObjectSink<T> getSink() {
        return sink;
    }

    public final void setSink(ObjectSink<T> sink) {
        Checks.checkNotNull("sink", sink);
        this.sink = sink;
    }

    public final ObjectSource<T> getSource() {
        return source;
    }

    public final void setSource(ObjectSource<T> source) {
        Checks.checkNotNull("source", source);
        this.source = source;
    }

    @Override
    protected void initialiseTask() throws Exception {
        Checks.checkNotNull("sink", sink);
        Checks.checkNotNull("source", source);
        if (getSource().equals(getSink()))
            throw new IllegalStateException("Source is the same as the sink.");
    }

    @Override
    protected void runTask() throws IOException {

        while (getSource().hasNext()) {
            getSink().write(getSource().read());
        }

        if (getSink() instanceof Flushable)
            ((Flushable) getSink()).flush();

    }

    @Override
    protected void finaliseTask() throws Exception {
        // nothing to do
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("source", getSource()).
                add("sink", getSink());
    }

    public boolean equals(ObjectPipeTask<?> that) {
        if (!super.equals((AbstractTask) that))
            return false;
        if (this.getSource() != that.getSource()
                && (this.getSource() == null || !this.getSource().equals(that.
                    getSource())))
            return false;
        if (this.getSink() != that.getSink()
                && (this.getSink() == null || !this.getSink().equals(that.
                    getSink())))
            return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return equals((ObjectPipeTask<?>) obj);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 37 * hash + (this.getSource() != null ? this.getSource().hashCode() : 0);
        hash = 37 * hash + (this.getSink() != null ? this.getSink().hashCode() : 0);
        return hash;
    }
}
