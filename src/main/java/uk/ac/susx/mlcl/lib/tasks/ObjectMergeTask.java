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
 * POSSIBILITY OF SUCH DAMAGE.To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.tasks;

import com.google.common.base.Objects;
import java.io.Flushable;
import java.util.Comparator;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;

/**
 *
 * @param <T>
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class ObjectMergeTask<T> extends AbstractTask {

    private Source<T> sourceA;

    private Source<T> sourceB;

    private Sink<T> sink;

    private Comparator<T> comparator;

    public ObjectMergeTask(Source<T> sourceA, Source<T> sourceB, Sink<T> sink,
                     Comparator<T> comparator) {
        setSourceA(sourceA);
        setSourceB(sourceB);
        setSink(sink);
        setComparator(comparator);
    }

    public ObjectMergeTask(Source<T> sourceA, Source<T> sourceB, Sink<T> sink) {
        setSourceA(sourceA);
        setSourceB(sourceB);
        setSink(sink);
        setComparator(Comparators.<T>naturalOrderIfPossible());
    }

    public ObjectMergeTask() {
        setComparator(Comparators.<T>naturalOrderIfPossible());
    }

    public final Comparator<T> getComparator() {
        return comparator;
    }

    public final void setComparator(Comparator<T> comparator) {
        Checks.checkNotNull(comparator);
        this.comparator = comparator;
    }

    public final Sink<T> getSink() {
        return sink;
    }

    public final void setSink(Sink<T> sink) {
        Checks.checkNotNull(sink);
        this.sink = sink;
    }

    public final Source<T> getSourceA() {
        return sourceA;
    }

    public final void setSourceA(Source<T> sourceA) {
        Checks.checkNotNull(sourceA);
        this.sourceA = sourceA;
    }

    public final Source<T> getSourceB() {
        return sourceB;
    }

    public final void setSourceB(Source<T> sourceB) {
        Checks.checkNotNull(sourceB);
        this.sourceB = sourceB;
    }

    public boolean equals(ObjectMergeTask<?> other) {
        if (!super.equals(other))
            return false;
        if (this.getSourceA() != other.getSourceA()
                && (this.getSourceA() == null || !this.getSourceA().equals(other.
                    getSourceA())))
            return false;
        if (this.getSourceB() != other.getSourceB()
                && (this.getSourceB() == null || !this.getSourceB().equals(other.
                    getSourceB())))
            return false;
        if (this.getSink() != other.getSink() && (this.getSink() == null || !this.
                                                  getSink().equals(
                                                  other.getSink())))
            return false;
        if (this.getComparator() != other.getComparator()
                && (this.getComparator() == null || !this.getComparator().equals(other.
                    getComparator())))
            return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return equals((ObjectMergeTask<?>) obj);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 71 * hash + (this.sourceA != null ? this.sourceA.hashCode() : 0);
        hash = 71 * hash + (this.sourceB != null ? this.sourceB.hashCode() : 0);
        hash = 71 * hash + (this.sink != null ? this.sink.hashCode() : 0);
        hash = 71 * hash + (this.comparator != null ? this.comparator.hashCode() : 0);
        return hash;
    }

    @Override
    protected void initialiseTask() throws Exception {
        Checks.checkNotNull(getSourceA());
        Checks.checkNotNull(getSourceB());
        Checks.checkNotNull(getSink());
        Checks.checkNotNull(getComparator());
        if (getSourceA().equals(getSourceB()))
            throw new IllegalStateException("Sources A and B are the same.");
        if (getSourceA().equals(getSink()))
            throw new IllegalStateException("Source A is the same as the sink.");
        if (getSourceB().equals(getSink()))
            throw new IllegalStateException("Source B is the same as the sink.");
    }

    @Override
    protected void runTask() throws Exception {
        T a = sourceA.hasNext() ? sourceA.read() : null;
        T b = sourceB.hasNext() ? sourceB.read() : null;
        while (a != null && b != null) {
            final int c = comparator.compare(a, b);
            if (c < 0) {
                sink.write(a);
                a = sourceA.hasNext() ? sourceA.read() : null;
            } else if (c > 0) {
                sink.write(b);
                b = sourceB.hasNext() ? sourceB.read() : null;
            } else {
                sink.write(a);
                sink.write(b);
                a = sourceA.hasNext() ? sourceA.read() : null;
                b = sourceB.hasNext() ? sourceB.read() : null;
            }
        }
        while (a != null) {
            sink.write(a);
            a = sourceA.hasNext() ? sourceA.read() : null;
        }
        while (b != null) {
            sink.write(b);
            b = sourceB.hasNext() ? sourceB.read() : null;
        }


        if (sink instanceof Flushable)
            ((Flushable) sink).flush();
    }

    @Override
    protected void finaliseTask() throws Exception {
    }

     @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("sourceA", getSourceA()).
                add("sourceB", getSourceB()).
                add("sink", getSink()).
                add("comparator", getComparator());
    }

}
