/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.tasks;

import java.io.Flushable;
import java.util.Comparator;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;

/**
 *
 * @param <T> 
 * @author hiam20
 */
public class MergeTask<T> extends AbstractTask {

    private Source<T> sourceA;

    private Source<T> sourceB;

    private Sink<T> sink;

    private Comparator<T> comparator;

    public MergeTask(Source<T> sourceA, Source<T> sourceB, Sink<T> sink, Comparator<T> comparator) {
        setSourceA(sourceA);
        setSourceB(sourceB);
        setSink(sink);
        setComparator(comparator);
    }

    public MergeTask(Source<T> sourceA, Source<T> sourceB, Sink<T> sink) {
        setSourceA(sourceA);
        setSourceB(sourceB);
        setSink(sink);
        setComparator(Comparators.<T>naturalOrderIfPossible());
    }

    public MergeTask() {
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

    public boolean equals(MergeTask<?> other) {
        if (this.getSourceA() != other.getSourceA()
                && (this.getSourceA() == null || !this.getSourceA().equals(other.getSourceA())))
            return false;
        if (this.getSourceB() != other.getSourceB()
                && (this.getSourceB() == null || !this.getSourceB().equals(other.getSourceB())))
            return false;
        if (this.getSink() != other.getSink() && (this.getSink() == null || !this.getSink().equals(other.getSink())))
            return false;
        if (this.getComparator() != other.getComparator()
                && (this.getComparator() == null || !this.getComparator().equals(other.getComparator())))
            return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return equals((MergeTask<?>) obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
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


        if (getSink() instanceof Flushable)
            ((Flushable) getSink()).flush();
    }
    
    @Override
    protected void finaliseTask() throws Exception {
        if (sink instanceof Flushable)
            ((Flushable) sink).flush();
    }

}
