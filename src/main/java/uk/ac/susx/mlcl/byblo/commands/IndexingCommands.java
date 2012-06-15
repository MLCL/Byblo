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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratingDeligates;
import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumerating;
import uk.ac.susx.mlcl.byblo.io.BybloIO;
import uk.ac.susx.mlcl.byblo.io.Token;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.ObjectSource;

/**
 *
 * @param <T>
 * @author hiam20
 */
public abstract class IndexingCommands<T>
    extends AbstractCopyCommand<T> {

    @ParametersDelegate
    private DoubleEnumerating indexDeligate = new DoubleEnumeratingDeligate(
            Enumerating.DEFAULT_TYPE, false, false, null, null);

    public IndexingCommands() {
        super();
    }

    public IndexingCommands(File sourceFile, File destinationFile,
                            Charset charset, DoubleEnumerating indexDeligate) {
        super(sourceFile, destinationFile, charset);
        this.indexDeligate = indexDeligate;
    }

    @Override
    public void runCommand() throws Exception {
        checkState();
        super.runCommand();

        indexDeligate.saveEnumerator();
        indexDeligate.closeEnumerator();
    }

    @Override
    public String getName() {
        return "enumeration";
    }

    protected abstract void checkState() throws Exception;

    public DoubleEnumerating getIndexDeligate() {
        return indexDeligate;
    }

    public void setIndexDeligate(DoubleEnumerating indexDeligate) {
        this.indexDeligate = indexDeligate;
    }

    protected DoubleEnumerating getSourceIndexDeligate() {
        return EnumeratingDeligates.decorateEnumerated(indexDeligate, false);
    }

    protected DoubleEnumerating getSinkIndexDeligate() {
        return EnumeratingDeligates.decorateEnumerated(indexDeligate, true);
    }

    public static class IndexEntries extends IndexingCommands<Weighted<Token>> {

        public IndexEntries(
                File sourceFile, File destinationFile, Charset charset,
                SingleEnumerating indexDeligate) {
            super(sourceFile, destinationFile, charset, EnumeratingDeligates.toPair(indexDeligate));
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
            Checks.checkNotNull("indexFile", EnumeratingDeligates.toSingleEntries(getIndexDeligate()).getEnumeratorFile());
        }

        @Override
        protected ObjectSource<Weighted<Token>> openSource(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openEntriesSource(file, getCharset(), getSourceIndexDeligate());
        }

        @Override
        protected ObjectSink<Weighted<Token>> openSink(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openEntriesSink(file, getCharset(), getSinkIndexDeligate());
        }

    }

    public static class IndexFeatures extends IndexingCommands<Weighted<Token>> {

        public IndexFeatures(
                File sourceFile, File destinationFile, Charset charset,
                SingleEnumerating indexDeligate) {
            super(sourceFile, destinationFile, charset, EnumeratingDeligates.toPair(indexDeligate));
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
            Checks.checkNotNull("indexFile", EnumeratingDeligates.toSingleFeatures(getIndexDeligate()).getEnumeratorFile());
        }

        @Override
        protected ObjectSource<Weighted<Token>> openSource(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openFeaturesSource(file, getCharset(), getSourceIndexDeligate());
        }

        @Override
        protected ObjectSink<Weighted<Token>> openSink(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openFeaturesSink(file, getCharset(), getSinkIndexDeligate());
        }

    }

    public static class IndexEvents extends IndexingCommands<Weighted<TokenPair>> {

        public IndexEvents(
                File sourceFile, File destinationFile, Charset charset,
                DoubleEnumerating indexDeligate) {
            super(sourceFile, destinationFile, charset, indexDeligate);
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
            Checks.checkNotNull("indexFile1", getIndexDeligate().getEntryEnumeratorFile());
            Checks.checkNotNull("indexFile2", getIndexDeligate().getFeatureEnumeratorFile());
        }

        @Override
        protected ObjectSource<Weighted<TokenPair>> openSource(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openEventsSource(file, getCharset(), getSourceIndexDeligate());
        }

        @Override
        protected ObjectSink<Weighted<TokenPair>> openSink(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openEventsSink(file, getCharset(), getSinkIndexDeligate());
        }

    }

    public static class IndexInstances extends IndexingCommands<TokenPair> {

        public IndexInstances(
                File sourceFile, File destinationFile, Charset charset,
                DoubleEnumerating indexDeligate) {
            super(sourceFile, destinationFile, charset, indexDeligate);
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
            Checks.checkNotNull("indexFile1", getIndexDeligate().getEntryEnumeratorFile());
            Checks.checkNotNull("indexFile2", getIndexDeligate().getFeatureEnumeratorFile());
        }

        @Override
        protected ObjectSource<TokenPair> openSource(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openInstancesSource(file, getCharset(), getSourceIndexDeligate());
        }

        @Override
        protected ObjectSink<TokenPair> openSink(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openInstancesSink(file, getCharset(), getSinkIndexDeligate());
        }

    }

    public static class IndexNeighbours extends IndexingCommands<Weighted<TokenPair>> {

        public IndexNeighbours(
                File sourceFile, File destinationFile, Charset charset,
                SingleEnumerating indexDeligate) {
            super(sourceFile, destinationFile, charset, EnumeratingDeligates.toPair(indexDeligate));
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
            Checks.checkNotNull("indexFile", EnumeratingDeligates.toSingleEntries(getIndexDeligate()).getEnumeratorFile());
        }

        @Override
        protected ObjectSource<Weighted<TokenPair>> openSource(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openNeighboursSource(file, getCharset(), getSourceIndexDeligate());
        }

        @Override
        protected ObjectSink<Weighted<TokenPair>> openSink(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openNeighboursSink(file, getCharset(), getSinkIndexDeligate());
        }

    }

    public static class IndexSims extends IndexingCommands<Weighted<TokenPair>> {

        public IndexSims(
                File sourceFile, File destinationFile, Charset charset,
                SingleEnumerating indexDeligate) {
            super(sourceFile, destinationFile, charset, EnumeratingDeligates.toPair(indexDeligate));
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
            Checks.checkNotNull("indexFile", EnumeratingDeligates.toSingleEntries(getIndexDeligate()).getEnumeratorFile());
        }

        @Override
        protected ObjectSource<Weighted<TokenPair>> openSource(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openSimsSource(file, getCharset(), getSourceIndexDeligate());
        }

        @Override
        protected ObjectSink<Weighted<TokenPair>> openSink(File file)
                throws FileNotFoundException, IOException {
            return BybloIO.openSimsSink(file, getCharset(), getSinkIndexDeligate());
        }

    }

    public static class UnindexEntries extends IndexEntries {

        public UnindexEntries() {
        }

        public UnindexEntries(File sourceFile, File destinationFile, Charset charset, SingleEnumerating indexDeligate) {
            super(sourceFile, destinationFile, charset, indexDeligate);
        }

        @Override
        public String getName() {
            return "entries unenumeration";
        }

        @Override
        protected DoubleEnumerating getSinkIndexDeligate() {
            return super.getSourceIndexDeligate();
        }

        @Override
        protected DoubleEnumerating getSourceIndexDeligate() {
            return super.getSinkIndexDeligate();
        }

    }

    public static class UnindexFeatures extends IndexFeatures {

        public UnindexFeatures() {
        }

        public UnindexFeatures(File sourceFile, File destinationFile, Charset charset, SingleEnumerating indexDeligate) {
            super(sourceFile, destinationFile, charset, indexDeligate);
        }

        @Override
        public String getName() {
            return "features unenumeration";
        }

        @Override
        protected DoubleEnumerating getSinkIndexDeligate() {
            return super.getSourceIndexDeligate();
        }

        @Override
        protected DoubleEnumerating getSourceIndexDeligate() {
            return super.getSinkIndexDeligate();
        }

    }

    public static class UnindexInstances extends IndexInstances {

        public UnindexInstances() {
        }

        public UnindexInstances(File sourceFile, File destinationFile, Charset charset, DoubleEnumerating indexDeligate) {
            super(sourceFile, destinationFile, charset, indexDeligate);
        }

        @Override
        public String getName() {
            return "instnaces unenumeration";
        }

        @Override
        protected DoubleEnumerating getSinkIndexDeligate() {
            return super.getSourceIndexDeligate();
        }

        @Override
        protected DoubleEnumerating getSourceIndexDeligate() {
            return super.getSinkIndexDeligate();
        }

    }

    public static class UnindexEvents extends IndexEvents {

        public UnindexEvents() {
        }

        public UnindexEvents(File sourceFile, File destinationFile, Charset charset, DoubleEnumerating indexDeligate) {
            super(sourceFile, destinationFile, charset, indexDeligate);
        }

        @Override
        public String getName() {
            return "events unenumeration";
        }

        @Override
        protected DoubleEnumerating getSinkIndexDeligate() {
            return super.getSourceIndexDeligate();
        }

        @Override
        protected DoubleEnumerating getSourceIndexDeligate() {
            return super.getSinkIndexDeligate();
        }

    }

    public static class UnindexNeighbours extends IndexNeighbours {

        public UnindexNeighbours() {
        }

        public UnindexNeighbours(File sourceFile, File destinationFile, Charset charset, SingleEnumerating indexDeligate) {
            super(sourceFile, destinationFile, charset, indexDeligate);
        }

        @Override
        public String getName() {
            return "neighbours unenumeration";
        }

        @Override
        protected DoubleEnumerating getSinkIndexDeligate() {
            return super.getSourceIndexDeligate();
        }

        @Override
        protected DoubleEnumerating getSourceIndexDeligate() {
            return super.getSinkIndexDeligate();
        }

    }

    public static class UnindexSims extends IndexSims {

        public UnindexSims() {
        }

        public UnindexSims(File sourceFile, File destinationFile, Charset charset, SingleEnumerating indexDeligate) {
            super(sourceFile, destinationFile, charset, indexDeligate);
        }

        @Override
        public String getName() {
            return "sims unenumeration";
        }

        @Override
        protected DoubleEnumerating getSinkIndexDeligate() {
            return super.getSourceIndexDeligate();
        }

        @Override
        protected DoubleEnumerating getSourceIndexDeligate() {
            return super.getSinkIndexDeligate();
        }

    }
}
