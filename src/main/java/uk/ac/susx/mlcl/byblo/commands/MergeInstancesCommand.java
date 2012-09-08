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
import com.google.common.base.Objects;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.io.BybloIO;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.ObjectSource;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class MergeInstancesCommand extends AbstractMergeCommand<TokenPair> {

    @ParametersDelegate
    private DoubleEnumeratingDelegate indexDelegate = new DoubleEnumeratingDelegate();

    public MergeInstancesCommand(
            File sourceFileA, File sourceFileB, File destinationFile,
            Charset charset, DoubleEnumeratingDelegate indexDelegate) {
        super(sourceFileA, sourceFileB, destinationFile, charset, TokenPair.indexOrder());
        setIndexDelegate(indexDelegate);
    }

    public MergeInstancesCommand() {
    }

    @Override
    public void runCommand() throws Exception {
        super.runCommand();
        indexDelegate.saveEnumerator();
        indexDelegate.closeEnumerator();

    }

    public final DoubleEnumeratingDelegate getIndexDelegate() {
        return indexDelegate;
    }

    public final void setIndexDelegate(DoubleEnumeratingDelegate indexDelegate) {
        Checks.checkNotNull("indexDelegate", indexDelegate);
        this.indexDelegate = indexDelegate;
    }

    @Override
    protected ObjectSource<TokenPair> openSource(File file) throws FileNotFoundException, IOException {
        return BybloIO.openInstancesSource(file, getFileDelegate().getCharset(), indexDelegate);
    }

    @Override
    protected ObjectSink<TokenPair> openSink(File file) throws FileNotFoundException, IOException {
        return BybloIO.openInstancesSink(file, getFileDelegate().getCharset(), indexDelegate);
    }

    public static void main(String[] args) throws Exception {
        new MergeInstancesCommand().runCommand(args);
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().add("indexing", indexDelegate);
    }

}
