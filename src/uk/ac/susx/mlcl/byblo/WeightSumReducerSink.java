/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.lib.io.Sink;

/**
 *
 * @author hiam20
 */
public class WeightSumReducerSink<T> implements Sink<Weighted<T>>, Flushable, Closeable {

    private final Sink<Weighted<T>> inner;

    private T currentRecord = null;

    private double weightSum = 0;

    public WeightSumReducerSink(Sink<Weighted<T>> inner) {
        this.inner = inner;
    }

    @Override
    public void write(Weighted<T> o) throws IOException {
        if (currentRecord == null) {
            currentRecord = o.record();
            weightSum = o.weight();
        } else if (currentRecord.equals(o.record())) {
            weightSum += o.weight();
        } else {
            inner.write(new Weighted<T>(currentRecord, weightSum));
            currentRecord = o.record();
            weightSum = o.weight();
        }
    }

    @Override
    public void flush() throws IOException {
        if (currentRecord != null) {
            inner.write(new Weighted<T>(currentRecord, weightSum));
            currentRecord = null;
            weightSum = 0;
        }
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
