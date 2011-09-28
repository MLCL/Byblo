/*
 * Copyright (c) 2010-2011, University of Sussex
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

import uk.ac.susx.mlcl.lib.ObjectIndex;
import uk.ac.susx.mlcl.lib.collect.Pair;
import uk.ac.susx.mlcl.lib.io.AbstractTSVSink;
import uk.ac.susx.mlcl.lib.io.Sink;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;

/**
 *
 * @author Hamish Morgan (hamish.morgan@sussex.ac.uk)
 */
public class WeightedPairSink
        extends AbstractTSVSink<Pair>
        implements Sink<Pair> {

    private final DecimalFormat f = new DecimalFormat("###0.0#####;-###0.0#####");

    private final ObjectIndex<String> strIndexA;

    private final ObjectIndex<String> strIndexB;

    private final Object sync = new Object();

    public WeightedPairSink(File file, Charset charset,
            ObjectIndex<String> strIndexA,
            ObjectIndex<String> strIndexB) throws IOException {
        super(file, charset);
        this.strIndexA = strIndexA;
        this.strIndexB = strIndexB;
    }

    @Override
    public void write(Pair pair) throws IOException {
        super.writeString(strIndexA.get(pair.getXId()));
        super.writeValueDelimiter();
        super.writeString(strIndexB.get(pair.getYId()));
        super.writeValueDelimiter();
        if (Double.compare((int) pair.getWeight(), pair.getWeight()) == 0)
            super.writeInt((int) pair.getWeight());
        else
        super.writeString(f.format(pair.getWeight()));
        super.writeRecordDelimiter();
    }
}
