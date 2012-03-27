/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.tasks;

import com.google.common.base.Objects.ToStringHelper;
import java.io.Closeable;
import java.io.Flushable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;
import uk.ac.susx.mlcl.lib.tasks.AbstractTask;

/**
 *
 * @param <T>
 * @author hiam20
 */
public class SortTask<T> extends AbstractTask {

    private Source<T> source;

    private Sink<T> sink;

    private Comparator<T> comparator;

    public SortTask(Source<T> source, Sink<T> sink, Comparator<T> comparator) {
        setSource(source);
        setSink(sink);
        setComparator(comparator);
    }

    public SortTask(Source<T> sourceA, Sink<T> sink) {
        setSource(sourceA);
        setSink(sink);
        setComparator(Comparators.<T>naturalOrderIfPossible());
    }

    public SortTask() {
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

    public final Source<T> getSource() {
        return source;
    }

    public final void setSource(Source<T> sourceA) {
        Checks.checkNotNull(sourceA);
        this.source = sourceA;
    }

    public boolean equals(SortTask<?> other) {
        if (this.getSource() != other.getSource() && (this.getSource() == null
                || !this.getSource().equals(other.getSource())))
            return false;
        if (this.getSink() != other.getSink() && (this.getSink() == null
                || !this.getSink().equals(other.getSink())))
            return false;
        if (this.getComparator() != other.getComparator() && (this.getComparator() == null
                || !this.getComparator().equals(other.getComparator())))
            return false;
        return true;

    }

    @Override
    public boolean equals(Object obj) {
        return obj != null
                && getClass() == obj.getClass()
                && equals((SortTask<?>) obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.source != null ? this.source.hashCode() : 0);
        hash = 71 * hash + (this.sink != null ? this.sink.hashCode() : 0);
        hash = 71 * hash + (this.comparator != null ? this.comparator.hashCode() : 0);
        return hash;
    }

    @Override
    protected void initialiseTask() throws Exception {
        Checks.checkNotNull(getSource());
        Checks.checkNotNull(getSink());
        Checks.checkNotNull(getComparator());
    }

    @Override
    protected void runTask() throws Exception {

        final List<T> items = IOUtil.readAll(getSource());

        if (getSource() instanceof Closeable)
            ((Closeable) getSource()).close();

        Collections.sort(items, getComparator());

        int i = IOUtil.copy(items, getSink());
        assert i == items.size();

        if (getSink() instanceof Flushable)
            ((Flushable) getSink()).flush();
//        if (snk instanceof Closeable)
//            ((Closeable) snk).close();


    }

    @Override
    protected void finaliseTask() throws Exception {
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("source", getSource()).
                add("sink", getSink()).
                add("comparator", getComparator());
    }

    
}
