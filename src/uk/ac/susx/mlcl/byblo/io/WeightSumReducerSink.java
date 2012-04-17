/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.io;

import java.io.IOException;
import uk.ac.susx.mlcl.lib.io.Sink;

/**
 *
 * @author hiam20
 */
public class WeightSumReducerSink<T>
        extends ForwardingSink<Sink<Weighted<T>>, Weighted<T>> {

    private T currentRecord = null;

    private double weightSum = 0;

    public WeightSumReducerSink(Sink<Weighted<T>> inner) {
        super(inner);
    }

    @Override
    public void write(Weighted<T> o) throws IOException {
        if (currentRecord == null) {
            currentRecord = o.record();
            weightSum = o.weight();
        } else if (currentRecord.equals(o.record())) {
            weightSum += o.weight();
        } else {
            super.write(new Weighted<T>(currentRecord, weightSum));
            currentRecord = o.record();
            weightSum = o.weight();
        }
    }

    @Override
    public void flush() throws IOException {
        if (currentRecord != null) {
            super.write(new Weighted<T>(currentRecord, weightSum));
            currentRecord = null;
            weightSum = 0;
        }
        super.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        super.close();
    }

}
