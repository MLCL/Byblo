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
import uk.ac.susx.mlcl.byblo.enumerators.*;
import uk.ac.susx.mlcl.byblo.io.BybloIO;
import uk.ac.susx.mlcl.byblo.io.Token;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.ObjectSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @param <T>
 * @author hiam20
 */
public abstract class IndexingCommands<T>
        extends AbstractCopyCommand<T> {

    @ParametersDelegate
    private DoubleEnumerating indexDelegate = new DoubleEnumeratingDelegate(
            Enumerating.DEFAULT_TYPE, false, false, null, null);

    public IndexingCommands() {
        super();
    }

    public IndexingCommands(File sourceFile, File destinationFile,
                            Charset charset, DoubleEnumerating indexDelegate) {
        super(sourceFile, destinationFile, charset);
        this.indexDelegate = indexDelegate;
    }

    @Override
    public void runCommand() throws Exception {
        checkState();
        super.runCommand();

        indexDelegate.saveEnumerator();
        indexDelegate.closeEnumerator();
    }

    @Override
    public String getName() {
        return "enumeration";
    }

    protected abstract void checkState() throws Exception;

    public DoubleEnumerating getIndexDelegate() {
        return indexDelegate;
    }

    public void setIndexDelegate(DoubleEnumerating indexDelegate) {
        this.indexDelegate = indexDelegate;
    }

    protected DoubleEnumerating getSourceIndexDelegate() {
        return EnumeratingDelegates.decorateEnumerated(indexDelegate, false);
    }

    protected DoubleEnumerating getSinkIndexDelegate() {
        return EnumeratingDelegates.decorateEnumerated(indexDelegate, true);
    }

    public static class IndexEntries extends IndexingCommands<Weighted<Token>> {

        public IndexEntries(
                File sourceFile, File destinationFile, Charset charset,
                SingleEnumerating indexDelegate) {
            super(sourceFile, destinationFile, charset, EnumeratingDelegates.toPair(indexDelegate));
        }

        public IndexEntries() {
            super();
        }

        @Override
        public String getName() {
            return "entry enumeration";
        }

        @Override
        protected void checkState() throws Exception {
            Checks.checkNotNull("indexFile", EnumeratingDelegates.toSingleEntries(getIndexDelegate()).getEnumeratorFile());
        }

        @Override
        protected ObjectSource<Weighted<Token>> openSource(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openEntriesSource(file, getCharset(), getSourceIndexDelegate());
        }

        @Override
        protected ObjectSink<Weighted<Token>> openSink(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openEntriesSink(file, getCharset(), getSinkIndexDelegate());
        }

    }

    public static class IndexFeatures extends IndexingCommands<Weighted<Token>> {

        public IndexFeatures(
                File sourceFile, File destinationFile, Charset charset,
                SingleEnumerating indexDelegate) {
            super(sourceFile, destinationFile, charset, EnumeratingDelegates.toPair(indexDelegate));
        }

        @Override
        public String getName() {
            return "feature enumeration";
        }

        public IndexFeatures() {
            super();
        }

        @Override
        public void checkState() throws Exception {
            Checks.checkNotNull("indexFile", EnumeratingDelegates.toSingleFeatures(getIndexDelegate()).getEnumeratorFile());
        }

        @Override
        protected ObjectSource<Weighted<Token>> openSource(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openFeaturesSource(file, getCharset(), getSourceIndexDelegate());
        }

        @Override
        protected ObjectSink<Weighted<Token>> openSink(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openFeaturesSink(file, getCharset(), getSinkIndexDelegate());
        }

    }

    public static class IndexEvents extends IndexingCommands<Weighted<TokenPair>> {

        public IndexEvents(
                File sourceFile, File destinationFile, Charset charset,
                DoubleEnumerating indexDelegate) {
            super(sourceFile, destinationFile, charset, indexDelegate);
        }

        public IndexEvents() {
            super();
        }

        @Override
        public String getName() {
            return "event enumeration";
        }

        @Override
        public void checkState() throws Exception {
            Checks.checkNotNull("indexFile1", getIndexDelegate().getEntryEnumeratorFile());
            Checks.checkNotNull("indexFile2", getIndexDelegate().getFeatureEnumeratorFile());
        }

        @Override
        protected ObjectSource<Weighted<TokenPair>> openSource(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openEventsSource(file, getCharset(), getSourceIndexDelegate());
        }

        @Override
        protected ObjectSink<Weighted<TokenPair>> openSink(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openEventsSink(file, getCharset(), getSinkIndexDelegate());
        }

    }

    public static class IndexInstances extends IndexingCommands<TokenPair> {

        public IndexInstances(
                File sourceFile, File destinationFile, Charset charset,
                DoubleEnumerating indexDelegate) {
            super(sourceFile, destinationFile, charset, indexDelegate);
        }

        public IndexInstances() {
            super();
        }

        @Override
        public String getName() {
            return "instance-enumeration";
        }

        @Override
        public void checkState() throws Exception {
            Checks.checkNotNull("indexFile1", getIndexDelegate().getEntryEnumeratorFile());
            Checks.checkNotNull("indexFile2", getIndexDelegate().getFeatureEnumeratorFile());
        }

        @Override
        protected ObjectSource<TokenPair> openSource(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openInstancesSource(file, getCharset(), getSourceIndexDelegate());
        }

        @Override
        protected ObjectSink<TokenPair> openSink(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openInstancesSink(file, getCharset(), getSinkIndexDelegate());
        }

    }

    public static class IndexNeighbours extends IndexingCommands<Weighted<TokenPair>> {

        public IndexNeighbours(
                File sourceFile, File destinationFile, Charset charset,
                SingleEnumerating indexDelegate) {
            super(sourceFile, destinationFile, charset, EnumeratingDelegates.toPair(indexDelegate));
        }

        public IndexNeighbours() {
            super();
        }

        @Override
        public String getName() {
            return "neighbours enumeration";
        }

        @Override
        public void checkState() throws Exception {
            Checks.checkNotNull("indexFile", EnumeratingDelegates.toSingleEntries(getIndexDelegate()).getEnumeratorFile());
        }

        @Override
        protected ObjectSource<Weighted<TokenPair>> openSource(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openNeighboursSource(file, getCharset(), getSourceIndexDelegate());
        }

        @Override
        protected ObjectSink<Weighted<TokenPair>> openSink(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openNeighboursSink(file, getCharset(), getSinkIndexDelegate());
        }

    }

    public static class IndexSims extends IndexingCommands<Weighted<TokenPair>> {

        public IndexSims(
                File sourceFile, File destinationFile, Charset charset,
                SingleEnumerating indexDelegate) {
            super(sourceFile, destinationFile, charset, EnumeratingDelegates.toPair(indexDelegate));
        }

        public IndexSims() {
            super();
        }

        @Override
        public String getName() {
            return "sims enumeration";
        }

        @Override
        public void checkState() throws Exception {
            Checks.checkNotNull("indexFile", EnumeratingDelegates.toSingleEntries(getIndexDelegate()).getEnumeratorFile());
        }

        @Override
        protected ObjectSource<Weighted<TokenPair>> openSource(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openSimsSource(file, getCharset(), getSourceIndexDelegate());
        }

        @Override
        protected ObjectSink<Weighted<TokenPair>> openSink(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openSimsSink(file, getCharset(), getSinkIndexDelegate());
        }

    }

    public static class UnindexEntries extends IndexEntries {

        public UnindexEntries() {
        }

        public UnindexEntries(File sourceFile, File destinationFile, Charset charset, SingleEnumerating indexDelegate) {
            super(sourceFile, destinationFile, charset, indexDelegate);
        }

        @Override
        public String getName() {
            return "entries unenumeration";
        }

        @Override
        protected DoubleEnumerating getSinkIndexDelegate() {
            return super.getSourceIndexDelegate();
        }

        @Override
        protected DoubleEnumerating getSourceIndexDelegate() {
            return super.getSinkIndexDelegate();
        }

    }

    public static class UnindexFeatures extends IndexFeatures {

        public UnindexFeatures() {
        }

        public UnindexFeatures(File sourceFile, File destinationFile, Charset charset, SingleEnumerating indexDelegate) {
            super(sourceFile, destinationFile, charset, indexDelegate);
        }

        @Override
        public String getName() {
            return "features unenumeration";
        }

        @Override
        protected DoubleEnumerating getSinkIndexDelegate() {
            return super.getSourceIndexDelegate();
        }

        @Override
        protected DoubleEnumerating getSourceIndexDelegate() {
            return super.getSinkIndexDelegate();
        }

    }

    public static class UnindexInstances extends IndexInstances {

        public UnindexInstances() {
        }

        public UnindexInstances(File sourceFile, File destinationFile, Charset charset, DoubleEnumerating indexDelegate) {
            super(sourceFile, destinationFile, charset, indexDelegate);
        }

        @Override
        public String getName() {
            return "instnaces unenumeration";
        }

        @Override
        protected DoubleEnumerating getSinkIndexDelegate() {
            return super.getSourceIndexDelegate();
        }

        @Override
        protected DoubleEnumerating getSourceIndexDelegate() {
            return super.getSinkIndexDelegate();
        }

    }

    public static class UnindexEvents extends IndexEvents {

        public UnindexEvents() {
        }

        public UnindexEvents(File sourceFile, File destinationFile, Charset charset, DoubleEnumerating indexDelegate) {
            super(sourceFile, destinationFile, charset, indexDelegate);
        }

        @Override
        public String getName() {
            return "events unenumeration";
        }

        @Override
        protected DoubleEnumerating getSinkIndexDelegate() {
            return super.getSourceIndexDelegate();
        }

        @Override
        protected DoubleEnumerating getSourceIndexDelegate() {
            return super.getSinkIndexDelegate();
        }

    }

    public static class UnindexNeighbours extends IndexNeighbours {

        public UnindexNeighbours() {
        }

        public UnindexNeighbours(File sourceFile, File destinationFile, Charset charset, SingleEnumerating indexDelegate) {
            super(sourceFile, destinationFile, charset, indexDelegate);
        }

        @Override
        public String getName() {
            return "neighbours unenumeration";
        }

        @Override
        protected DoubleEnumerating getSinkIndexDelegate() {
            return super.getSourceIndexDelegate();
        }

        @Override
        protected DoubleEnumerating getSourceIndexDelegate() {
            return super.getSinkIndexDelegate();
        }

    }

    public static class UnindexSims extends IndexSims {

        public UnindexSims() {
        }

        public UnindexSims(File sourceFile, File destinationFile, Charset charset, SingleEnumerating indexDelegate) {
            super(sourceFile, destinationFile, charset, indexDelegate);
        }

        @Override
        public String getName() {
            return "sims unenumeration";
        }

        @Override
        protected DoubleEnumerating getSinkIndexDelegate() {
            return super.getSourceIndexDelegate();
        }

        @Override
        protected DoubleEnumerating getSourceIndexDelegate() {
            return super.getSinkIndexDelegate();
        }

    }
}
