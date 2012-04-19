/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.tasks;

import com.google.common.base.Objects.ToStringHelper;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;

/**
 *
 * @param <T>
 * @author hiam20
 */
public class ObjectSortTask<T> extends ObjectPipeTask<T> {

    private static final long serialVersionUID = 1L;

    private Comparator<T> comparator;

    public ObjectSortTask(Source<T> source, Sink<T> sink, Comparator<T> comparator) {
        super(source, sink);
        setComparator(comparator);
    }

    public ObjectSortTask(Source<T> source, Sink<T> sink) {
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
        hash = 71 * hash + (this.comparator != null ? this.comparator.hashCode() : 0);
        return hash;
    }

    @Override
    protected void initialiseTask() throws Exception {
        super.initialiseTask();
        Checks.checkNotNull(getComparator());
    }

    @Override
    protected void runTask() throws IOException {

        final List<T> items = IOUtil.readAll(getSource());

        if (getSource() instanceof Closeable)
            ((Closeable) getSource()).close();

        Collections.sort(items, getComparator());

        int i = IOUtil.copy(items, getSink());
        assert i == items.size();

        if (getSink() instanceof Flushable)
            ((Flushable) getSink()).flush();
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("comparator", getComparator());
    }
    
    
}
