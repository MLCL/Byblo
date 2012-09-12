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
package uk.ac.susx.mlcl.byblo.io;

import uk.ac.susx.mlcl.lib.io.ForwardingObjectSink;
import uk.ac.susx.mlcl.lib.io.ObjectSink;

import javax.annotation.WillClose;
import java.io.IOException;

/**
 * @param <T>
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class WeightSumReducerObjectSink<T>
        extends ForwardingObjectSink<ObjectSink<Weighted<T>>, Weighted<T>> {

    private T currentRecord = null;

    private double weightSum = 0;

    public WeightSumReducerObjectSink(ObjectSink<Weighted<T>> inner) {
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
    @WillClose
    public void close() throws IOException {
        flush();
        super.close();
    }
}
