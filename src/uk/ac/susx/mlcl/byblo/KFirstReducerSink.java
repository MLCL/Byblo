/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Comparator;
import uk.ac.susx.mlcl.lib.io.Sink;

/**
 *
 * @author hiam20
 */
public class KFirstReducerSink<T> implements Sink<T>, Flushable, Closeable {

    private final Sink<T> inner;

    private Comparator<T> comparator = null;

    private int limit = 100;

    private T currentRecord = null;

    private int count = -1;

    public KFirstReducerSink(Sink<T> inner, Comparator<T> comparator, int limit) {
        this.inner = inner;
        this.limit = limit;
        this.comparator = comparator;
    }

    @Override
    public void write(T o) throws IOException {
        if (currentRecord == null
                || comparator.compare(currentRecord, o) != 0) {
            currentRecord = o;
            count = 0;
        }
        ++count;

        if (count <= limit) {
            inner.write(o);
        }
    }

    @Override
    public void flush() throws IOException {
        if (inner instanceof Flushable)
            ((Flushable) inner).flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        if (inner instanceof Closeable)
            ((Closeable) inner).close();
    }

}
