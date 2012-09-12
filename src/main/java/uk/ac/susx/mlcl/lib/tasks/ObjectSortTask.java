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

import com.google.common.base.Objects.ToStringHelper;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.io.ObjectIO;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.ObjectSource;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @param <T>
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class ObjectSortTask<T> extends ObjectPipeTask<T> {

    private static final long serialVersionUID = 1L;

    private Comparator<T> comparator;

    public ObjectSortTask(ObjectSource<T> source, ObjectSink<T> sink,
                          Comparator<T> comparator) {
        super(source, sink);
        setComparator(comparator);
    }

    public ObjectSortTask(ObjectSource<T> source, ObjectSink<T> sink) {
        super(source, sink);
        setComparator(Comparators.<T>naturalOrderIfPossible());
    }

    public ObjectSortTask() {
        super();
        setComparator(Comparators.<T>naturalOrderIfPossible());
    }

    public final Comparator<T> getComparator() {
        return comparator;
    }

    public final void setComparator(Comparator<T> comparator) {
        Checks.checkNotNull(comparator);
        this.comparator = comparator;
    }

    public boolean equals(ObjectSortTask<?> other) {
        if (!super.equals(this))
            return false;
        if (this.getComparator() != other.getComparator()
                && (this.getComparator() == null
                || !this.getComparator().
                equals(other.getComparator())))
            return false;
        return true;

    }

    @Override
    public boolean equals(Object obj) {
        return obj != null
                && getClass() == obj.getClass()
                && equals((ObjectSortTask<?>) obj);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 71 * hash + (this.comparator != null ? this.comparator.hashCode()
                : 0);
        return hash;
    }

    @Override
    protected void initialiseTask() throws Exception {
        super.initialiseTask();
        Checks.checkNotNull(getComparator());
    }

    @Override
    protected void runTask() throws IOException {

        progress.setState(State.RUNNING);

        final List<T> items = ObjectIO.readAll(getSource());

        if (getSource() instanceof Closeable)
            ((Closeable) getSource()).close();

        Collections.sort(items, getComparator());

        long i = ObjectIO.copy(items, getSink());
        assert i == items.size();

        if (getSink() instanceof Flushable)
            ((Flushable) getSink()).flush();


        progress.setState(State.COMPLETED);
    }

    @Override
    public String getName() {
        return "sort";
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("comparator", getComparator());
    }
}
