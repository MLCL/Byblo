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
package uk.ac.susx.mlcl.byblo.commands;

import com.beust.jcommander.ParametersDelegate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratorType;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.MemoryUsage;
import uk.ac.susx.mlcl.lib.events.ProgressEvent;
import uk.ac.susx.mlcl.lib.events.ProgressListener;
import uk.ac.susx.mlcl.lib.io.ObjectSink;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ExternalSortEventsCommand extends AbstractExternalSortCommand<Weighted<TokenPair>> {

    private static final Log LOG = LogFactory.getLog(ExternalSortEventsCommand.class);

    @ParametersDelegate
    private DoubleEnumerating indexDelegate = new DoubleEnumeratingDelegate();

    public ExternalSortEventsCommand(
            File sourceFile, File destinationFile, Charset charset,
            DoubleEnumerating indexDelegate) {
        super(sourceFile, destinationFile, charset);
        setIndexDelegate(indexDelegate);
    }

    public ExternalSortEventsCommand() {
    }

    @Override
    public void runCommand() throws Exception {
        this.addProgressListener(new ProgressListener() {
            @Override
            public void progressChanged(ProgressEvent progressEvent) {
                LOG.info(getProgressReport());
            }
        });

        super.runCommand();

        if (indexDelegate.isEnumeratorOpen()) {
            indexDelegate.saveEnumerator();
            indexDelegate.closeEnumerator();
        }

    }

    @Override
    protected ObjectSink<Weighted<TokenPair>> openSink(File file) throws IOException {
        return new WeightSumReducerObjectSink<TokenPair>(BybloIO.openEventsSink(file, getCharset(), indexDelegate));
    }

    @Override
    protected long getBytesPerObject() {
        return new MemoryUsage().add(new Weighted<TokenPair>(new TokenPair(1, 1), 1)).getInstanceSizeBytes();
    }

    @Override
    protected WeightedTokenPairSource openSource(File file) throws IOException {
        return BybloIO.openEventsSource(file, getCharset(), indexDelegate);
    }

    public final DoubleEnumerating getIndexDelegate() {
        return indexDelegate;
    }

    public final void setIndexDelegate(DoubleEnumerating indexDelegate) {
        Checks.checkNotNull("indexDelegate", indexDelegate);
        this.indexDelegate = indexDelegate;
    }

    public void setEnumeratorType(EnumeratorType type) {
        indexDelegate.setEnumeratorType(type);
    }

    public EnumeratorType getEnumeratorType() {
        return indexDelegate.getEnumeratorType();
    }

    public void setEnumeratedFeatures(boolean enumeratedFeatures) {
        indexDelegate.setEnumeratedFeatures(enumeratedFeatures);
    }

    public void setEnumeratedEntries(boolean enumeratedEntries) {
        indexDelegate.setEnumeratedEntries(enumeratedEntries);
    }

    public boolean isEnumeratedFeatures() {
        return indexDelegate.isEnumeratedFeatures();
    }

    public boolean isEnumeratedEntries() {
        return indexDelegate.isEnumeratedEntries();
    }
}
