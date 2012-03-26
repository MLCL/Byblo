/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.tasks;

import java.io.Closeable;
import java.io.Flushable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import uk.ac.susx.mlcl.byblo.ExternalSimsKnnTask;
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
public class KnnTask<T> extends AbstractTask {

    private Source<T> source;

    private Sink<T> sink;

    private int k = ExternalSimsKnnTask.DEFAULT_K;

    private Comparator<T> classComparator;

    private Comparator<T> nearnessComparator;

    public KnnTask(Source<T> source, Sink<T> sink, Comparator<T> classComparator, Comparator<T> nearnessComparator) {
        setSource(source);
        setSink(sink);
        setClassComparator(classComparator);
        setNearnessComparator(nearnessComparator);
    }

    public KnnTask(Source<T> sourceA, Sink<T> sink) {
        setSource(sourceA);
        setSink(sink);
    }

    public KnnTask() {
    }

    public final Comparator<T> getClassComparator() {
        return classComparator;
    }

    public final void setClassComparator(Comparator<T> classComparator) {
        Checks.checkNotNull(classComparator);
        this.classComparator = classComparator;
    }

    public final Comparator<T> getNearnessComparator() {
        return nearnessComparator;
    }

    public final void setNearnessComparator(Comparator<T> nearnessComparator) {
        this.nearnessComparator = nearnessComparator;
    }

    public final Comparator<T> getCombinedComparator() {
        return Comparators.fallback(
                getClassComparator(), getNearnessComparator());
    }

    public final int getK() {
        return k;
    }

    public final void setK(int k) {
        if (k < 1)
            throw new IllegalArgumentException("k < 1");
        this.k = k;
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

    @Override
    protected void initialiseTask() throws Exception {
        Checks.checkNotNull(getSource());
        Checks.checkNotNull(getSink());
        Checks.checkNotNull(getClassComparator());
        Checks.checkNotNull(getNearnessComparator());
    }

    @Override
    protected void runTask() throws Exception {

        Source<T> src = getSource();
        final List<T> items = IOUtil.readAll(getSource());

        if (getSource() instanceof Closeable)
            ((Closeable) getSource()).close();

        Collections.sort(items, getCombinedComparator());

        Sink<T> snk = getSink();

        T currentClass = null;
        int count = 0;
        for (T item : items) {
            if (currentClass == null || classComparator.compare(item, currentClass) != 0) {
                currentClass = item;
                count = 1;
            } else {
                count++;
            }
            if (count <= k) {
                snk.write(item);
            }
        }

        if (snk instanceof Flushable)
            ((Flushable) snk).flush();
        if (snk instanceof Closeable)
            ((Closeable) snk).close();


    }

    @Override
    protected void finaliseTask() throws Exception {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final KnnTask<T> other = (KnnTask<T>) obj;
        if (this.source != other.source && (this.source == null || !this.source.equals(other.source)))
            return false;
        if (this.sink != other.sink && (this.sink == null || !this.sink.equals(other.sink)))
            return false;
        if (this.classComparator != other.classComparator && (this.classComparator == null || !this.classComparator.equals(other.classComparator)))
            return false;
        if (this.nearnessComparator != other.nearnessComparator && (this.nearnessComparator == null || !this.nearnessComparator.equals(other.nearnessComparator)))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.source != null ? this.source.hashCode() : 0);
        hash = 71 * hash + (this.sink != null ? this.sink.hashCode() : 0);
        hash = 71 * hash + (this.classComparator != null ? this.classComparator.hashCode() : 0);
        hash = 71 * hash + (this.nearnessComparator != null ? this.nearnessComparator.hashCode() : 0);
        return hash;
    }

}
