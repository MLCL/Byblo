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
package uk.ac.susx.mlcl.byblo;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Comparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;
import uk.ac.susx.mlcl.lib.SimpleEnumerator;
import uk.ac.susx.mlcl.lib.io.*;
import uk.ac.susx.mlcl.lib.tasks.AbstractCommandTask;
import uk.ac.susx.mlcl.lib.tasks.InputFileValidator;
import uk.ac.susx.mlcl.lib.tasks.OutputFileValidator;

/**
 * Merges the contents of two sorted source files, line by line, into a
 * destination file.
 *
 * The source files are assumed to already be ordered according to the
 * comparator.
 *
 * Any file denoted by the name string "-" is assumed to be standard-in in the
 * case of source files, and standard out in the case of destination files..
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Merges the contents of two sorted source files, line by line, into a destination file.")
public abstract class MergeTask<T> extends AbstractCommandTask {

    private static final Log LOG = LogFactory.getLog(MergeTask.class);

    private Comparator<T> comparator;

    private Reducer<T> reducer = new DefaultReducer<T>();

    @Parameter(names = {"-ifa", "--input-file-a"}, required = true,
               description = "The first file to merge.",
               validateWith = InputFileValidator.class)
    private File sourceFileA;

    @Parameter(names = {"-ifb", "--input-file-b"}, required = true,
               description = "The second file to merge.",
               validateWith = InputFileValidator.class)
    private File sourceFileB;

    @Parameter(names = {"-of", "--output-file"},
               description = "The output file to which both input will be merged.",
               validateWith = OutputFileValidator.class)
    private File destinationFile;

    @Parameter(names = {"-c", "--charset"},
               description = "The character set encoding to use for both input and output files.")
    private Charset charset = Files.DEFAULT_CHARSET;

    @Parameter(names = {"-r", "--reverse"},
               description = "Reverse the result of comparisons.")
    private boolean reverse = false;

    public MergeTask(File sourceFileA, File sourceFileB, File destination,
                     Charset charset, Comparator<T> comparator) {
        setSourceFileA(sourceFileA);
        setSourceFileB(sourceFileB);
        setDestinationFile(destination);
        setCharset(charset);
        setComparator(comparator);
    }

    public MergeTask() {
    }

    public Reducer<T> getReducer() {
        return reducer;
    }

    public void setReducer(Reducer<T> reducer) {
        this.reducer = reducer;
    }

    public final Comparator<T> getComparator() {
        return comparator;
    }

    public final void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public final boolean isReverse() {
        return reverse;
    }

    public final void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public final Charset getCharset() {
        return charset;
    }

    public final void setCharset(Charset charset) {
        Checks.checkNotNull(charset);
        this.charset = charset;
    }

    @Override
    protected void initialiseTask() throws Exception {
    }

    @Override
    protected void finaliseTask() throws Exception {
    }

    public interface Reducer<T> {

        void reduce(Sink<T> dst, T a, T b) throws IOException;

        void reduce(Sink<T> dst, T item) throws IOException;
    }

    public static class DefaultReducer<T> implements Reducer<T> {

        @Override
        public void reduce(Sink<T> dst, T a, T b) throws IOException {
            dst.write(a);
            dst.write(b);
        }

        @Override
        public void reduce(Sink<T> dst, T item) throws IOException {
            dst.write(item);
        }
    }

    public static class WeightSumReducer<T> implements Reducer<Weighted<T>> {

        @Override
        public void reduce(Sink<Weighted<T>> dst, Weighted<T> a, Weighted<T> b)
                throws IOException {
            dst.write(new Weighted<T>(a.record(), a.weight() + b.weight()));
        }

        @Override
        public void reduce(Sink<Weighted<T>> dst,
                           Weighted<T> item) throws IOException {
            dst.write(item);
        }
    }

    protected void merge(Source<T> srcA, Source<T> srcB, Sink<T> dst) throws IOException {
        final Comparator<T> comp = getComparator();
        T a = srcA.hasNext() ? srcA.read() : null;
        T b = srcB.hasNext() ? srcB.read() : null;

        while (a != null && b != null) {
            final int c = comp.compare(a, b);
            if (c > 0) {
                reducer.reduce(dst, a);
                a = srcA.hasNext() ? srcA.read() : null;
            } else if (c < 0) {
                reducer.reduce(dst, b);
                b = srcB.hasNext() ? srcB.read() : null;
            } else {
                reducer.reduce(dst, a, b);
                a = srcA.hasNext() ? srcA.read() : null;
                b = srcB.hasNext() ? srcB.read() : null;
            }
        }
        while (a != null) {
            dst.write(a);
            a = srcA.hasNext() ? srcA.read() : null;
        }
        while (b != null) {
            dst.write(b);
            b = srcB.hasNext() ? srcB.read() : null;
        }

    }

    public File getSourceFileA() {
        return sourceFileA;
    }

    public File getSourceFileB() {
        return sourceFileB;
    }

    public File getDestFile() {
        return destinationFile;
    }

    public final void setSourceFileB(File sourceFileB) {
        if (sourceFileB == null)
            throw new NullPointerException("sourceFileB = null");
        if (sourceFileB == sourceFileA)
            throw new IllegalArgumentException("sourceFileB == sourceFileA");
        if (destinationFile == sourceFileB)
            throw new IllegalArgumentException("destination == sourceFileB");
        this.sourceFileB = sourceFileB;
    }

    public final void setSourceFileA(File sourceFileA) {
        if (sourceFileA == null)
            throw new NullPointerException("sourceFileA = null");
        if (sourceFileA == sourceFileB)
            throw new IllegalArgumentException("sourceFileA == sourceFileB");
        if (destinationFile == sourceFileA)
            throw new IllegalArgumentException("destination == sourceFileA");
        this.sourceFileA = sourceFileA;
    }

    public final void setDestinationFile(File destination) {
        if (destination == null)
            throw new NullPointerException("destination = null");
        if (destination == sourceFileB)
            throw new IllegalArgumentException("destination == sourceFileB");
        if (destination == sourceFileA)
            throw new IllegalArgumentException("destination == sourceFileA");
        this.destinationFile = destination;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("charset", charset).
                add("in1", sourceFileA).
                add("in2", sourceFileB).
                add("out", destinationFile).
                add("comparator", comparator);
    }

    public abstract static class WeightedTokenMergeTask extends MergeTask<Weighted<Token>> {

        private static final Log LOG = LogFactory.getLog(
                WeightedTokenMergeTask.class);

        @Parameter(names = {"-p", "--preindexed"},
                   description = "Whether tokens in the input events file are indexed.")
        private boolean preindexedTokens = false;

        public WeightedTokenMergeTask(
                File sourceFileA, File sourceFileB, File destinationFile,
                Charset charset, boolean preindexed) {
            super(sourceFileA, sourceFileB, destinationFile, charset,
                  Weighted.recordOrder(Token.indexOrder()));
            setPreindexedTokens(preindexed);
            checkState();
        }

        public WeightedTokenMergeTask() {
        }

        private Enumerator<String> index = null;


        public Enumerator<String> getIndex() {
            if (index == null)
                index = Enumerators.newDefaultStringEnumerator();
            return index;
        }

        public void setIndex(Enumerator<String> entryIndex) {
            this.index = entryIndex;
        }

        public final boolean isPreindexedTokens() {
            return preindexedTokens;
        }

        public final void setPreindexedTokens(boolean preindexedTokens) {
            this.preindexedTokens = preindexedTokens;
        }

        @Override
        protected void initialiseTask() throws Exception {
            super.initialiseTask();
            checkState();
        }

        private void checkState() {
//            try {
//                if (!Files.readLines(getSourceFileA(), getCharset(), 1).get(0).
//                        matches("^[^\t]+\t[^\t]+$")) {
//                    throw new IllegalArgumentException(
//                            "The source file A does not appear to be composed of tripples: " + getSourceFileA());
//
//                }
//                if (!Files.readLines(getSourceFileB(), getCharset(), 1).get(0).
//                        matches("^[^\t]+\t[^\t]+$")) {
//                    throw new IllegalArgumentException(
//                            "The source file B does not appear to be composed of tripples: " + getSourceFileB());
//
//                }
//            } catch (FileNotFoundException ex) {
//                throw new RuntimeException(ex);
//            } catch (IOException ex) {
//                throw new RuntimeException(ex);
//            }
        }

        @Override
        protected void runTask() throws Exception {
            if (LOG.isInfoEnabled())
                LOG.info("Running merge from \"" + getSourceFileA()
                        + "\" and \"" + getSourceFileB()
                        + "\" to \"" + getDestFile() + "\".");

            final Function<String, Integer> decoder;
            final Function<Integer, String> encoder;

            if (!preindexedTokens) {
                decoder = Token.stringDecoder(getIndex());
                encoder = Token.stringEncoder(getIndex());
            } else {
                decoder = Token.enumeratedDecoder();
                encoder = Token.enumeratedEncoder();
            }

            Source<Weighted<Token>> srcA = new WeightedTokenSource(
                    new TSVSource(getSourceFileA(), getCharset()), decoder);
            Source<Weighted<Token>> srcB = new WeightedTokenSource(
                    new TSVSource(getSourceFileB(), getCharset()), decoder);

            Sink<Weighted<Token>> snk = new WeightedTokenSink(
                    new TSVSink(getDestFile(), getCharset()),
                    encoder);

            merge(srcA, srcB, snk);

            if (snk instanceof Flushable)
                ((Flushable) snk).flush();


            if(srcA instanceof Closeable) 
                ((Closeable)srcA).close();
            if(srcB instanceof Closeable) 
                ((Closeable)srcB).close();
            if(snk instanceof Closeable) 
                ((Closeable)snk).close();

            if (LOG.isInfoEnabled())
                LOG.info("Completed merge.");
        }
    }

    public abstract static class TokenPairMergeTask extends MergeTask<TokenPair> {

        private static final Log LOG = LogFactory.getLog(
                TokenPairMergeTask.class);

        @Parameter(names = {"-p1", "--preindexed1"},
                   description = "Whether tokens in the first column of the input file are indexed.")
        private boolean preindexedTokens1 = false;

        @Parameter(names = {"-p2", "--preindexed2"},
                   description = "Whether entries in the second column of the input file are indexed.")
        private boolean preindexedTokens2 = false;

        public TokenPairMergeTask(
                File sourceFileA, File sourceFileB, File destinationFile,
                Charset charset,
                boolean preindexedTokens1, boolean preindexedTokens2) {
            super(sourceFileA, sourceFileB, destinationFile, charset,
                  TokenPair.indexOrder());
            setPreindexedTokens1(preindexedTokens1);
            setPreindexedTokens2(preindexedTokens2);
            checkState();
        }

        public TokenPairMergeTask() {
        }

        private Enumerator<String> index1 = null;

        private Enumerator<String> index2 = null;

        public Enumerator<String> getIndex1() {
            if (index1 == null)
                index1 = Enumerators.newDefaultStringEnumerator();
            return index1;
        }

        public void setIndex1(Enumerator<String> entryIndex) {
            this.index1 = entryIndex;
        }

        public Enumerator<String> getIndex2() {
            if (index2 == null)
                index2 = Enumerators.newDefaultStringEnumerator();
            return index2;
        }

        public void setIndex2(Enumerator<String> featureIndex) {
            this.index2 = featureIndex;
        }

        public final boolean isPreindexedTokens1() {
            return preindexedTokens1;
        }

        public final void setPreindexedTokens1(boolean preindexedTokens1) {
            this.preindexedTokens1 = preindexedTokens1;
        }

        public final boolean isPreindexedTokens2() {
            return preindexedTokens2;
        }

        public final void setPreindexedTokens2(boolean preindexedTokens2) {
            this.preindexedTokens2 = preindexedTokens2;
        }

        @Override
        protected void initialiseTask() throws Exception {
            super.initialiseTask();
            checkState();
        }

        private void checkState() {
//            try {
//                if (!Files.readLines(getSourceFileA(), getCharset(), 1).get(0).
//                        matches("^[^\t]+\t[^\t]+$")) {
//                    throw new IllegalArgumentException(
//                            "The source file A does not appear to be composed of tripples: " + getSourceFileA());
//
//                }
//                if (!Files.readLines(getSourceFileB(), getCharset(), 1).get(0).
//                        matches("^[^\t]+\t[^\t]+$")) {
//                    throw new IllegalArgumentException(
//                            "The source file B does not appear to be composed of tripples: " + getSourceFileB());
//
//                }
//            } catch (FileNotFoundException ex) {
//                throw new RuntimeException(ex);
//            } catch (IOException ex) {
//                throw new RuntimeException(ex);
//            }
        }

        @Override
        protected void runTask() throws Exception {
            if (LOG.isInfoEnabled())
                LOG.info("Running merge from \"" + getSourceFileA()
                        + "\" and \"" + getSourceFileB()
                        + "\" to \"" + getDestFile() + "\".");

            final Function<String, Integer> decoder1;
            final Function<String, Integer> decoder2;
            final Function<Integer, String> encoder1;
            final Function<Integer, String> encoder2;

            if (!preindexedTokens1) {
                decoder1 = Token.stringDecoder(getIndex1());
                encoder1 = Token.stringEncoder(getIndex1());
            } else {
                decoder1 = Token.enumeratedDecoder();
                encoder1 = Token.enumeratedEncoder();
            }

            if (!preindexedTokens2) {
                final Enumerator<String> featureIndex = Enumerators.
                        newDefaultStringEnumerator();
                decoder2 = Token.stringDecoder(getIndex2());
                encoder2 = Token.stringEncoder(getIndex2());

            } else {
                decoder2 = Token.enumeratedDecoder();
                encoder2 = Token.enumeratedEncoder();
            }

            Source<TokenPair> srcA = new TokenPairSource(
                    new TSVSource(getSourceFileA(), getCharset()),
                    decoder1, decoder2);

            Source<TokenPair> srcB = new TokenPairSource(
                    new TSVSource(getSourceFileB(), getCharset()),
                    decoder1, decoder2);

            Sink<TokenPair> snk = new TokenPairSink(
                    new TSVSink(getDestFile(), getCharset()),
                    encoder1, encoder2);

            merge(srcA, srcB, snk);

            if (snk instanceof Flushable)
                ((Flushable) snk).flush();

            if(srcA instanceof Closeable) 
                ((Closeable)srcA).close();
            if(srcB instanceof Closeable) 
                ((Closeable)srcB).close();
            if(snk instanceof Closeable) 
                ((Closeable)snk).close();
            

            if (LOG.isInfoEnabled())
                LOG.info("Completed merge.");
        }
    }

    public abstract static class WeightedTokenPairMergeTask extends MergeTask<Weighted<TokenPair>> {

        private static final Log LOG = LogFactory.getLog(
                WeightedTokenPairMergeTask.class);

        @Parameter(names = {"-p1", "--preindexed1"},
                   description = "Whether tokens in the first column of the input file are indexed.")
        private boolean preindexedTokens1 = false;

        @Parameter(names = {"-p2", "--preindexed2"},
                   description = "Whether entries in the second column of the input file are indexed.")
        private boolean preindexedTokens2 = false;

        public WeightedTokenPairMergeTask(
                File sourceFileA, File sourceFileB, File destinationFile,
                Charset charset,
                boolean preindexedTokens1, boolean preindexedTokens2) {
            super(sourceFileA, sourceFileB, destinationFile, charset,
                  Weighted.recordOrder(TokenPair.indexOrder()));
            setPreindexedTokens1(preindexedTokens1);
            setPreindexedTokens2(preindexedTokens2);
            checkState();
        }

        public WeightedTokenPairMergeTask() {
        }

        private Enumerator<String> index1 = null;

        private Enumerator<String> index2 = null;

        public Enumerator<String> getIndex1() {
            if (index1 == null)
                index1 = Enumerators.newDefaultStringEnumerator();
            return index1;
        }

        public void setIndex1(Enumerator<String> entryIndex) {
            this.index1 = entryIndex;
        }

        public Enumerator<String> getIndex2() {
            if (index2 == null)
                index2 = Enumerators.newDefaultStringEnumerator();
            return index2;
        }

        public void setIndex2(Enumerator<String> featureIndex) {
            this.index2 = featureIndex;
        }

        public final boolean isPreindexedTokens1() {
            return preindexedTokens1;
        }

        public final void setPreindexedTokens1(boolean preindexedTokens1) {
            this.preindexedTokens1 = preindexedTokens1;
        }

        public final boolean isPreindexedTokens2() {
            return preindexedTokens2;
        }

        public final void setPreindexedTokens2(boolean preindexedTokens2) {
            this.preindexedTokens2 = preindexedTokens2;
        }

        @Override
        protected void initialiseTask() throws Exception {
            super.initialiseTask();
            checkState();
        }

        private void checkState() {
//            try {
//                if (!Files.readLines(getSourceFileA(), getCharset(), 1).get(0).
//                        matches("^[^\t]+\t[^\t]+\t[^\t]+$")) {
//                    throw new IllegalArgumentException(
//                            "The source file A does not appear to be composed of tripples: " + getSourceFileA());
//
//                }
//                if (!Files.readLines(getSourceFileB(), getCharset(), 1).get(0).
//                        matches("^[^\t]+\t[^\t]+\t[^\t]+$")) {
//                    throw new IllegalArgumentException(
//                            "The source file B does not appear to be composed of tripples: " + getSourceFileB());
//
//                }
//            } catch (FileNotFoundException ex) {
//                throw new RuntimeException(ex);
//            } catch (IOException ex) {
//                throw new RuntimeException(ex);
//            }
        }

        @Override
        protected void runTask() throws Exception {
            if (LOG.isInfoEnabled())
                LOG.info("Running merge from \"" + getSourceFileA()
                        + "\" and \"" + getSourceFileB()
                        + "\" to \"" + getDestFile() + "\".");

            final Function<String, Integer> decoder1;
            final Function<String, Integer> decoder2;
            final Function<Integer, String> encoder1;
            final Function<Integer, String> encoder2;

            if (!preindexedTokens1) {
                decoder1 = Token.stringDecoder(getIndex1());
                encoder1 = Token.stringEncoder(getIndex1());
            } else {
                decoder1 = Token.enumeratedDecoder();
                encoder1 = Token.enumeratedEncoder();
            }

            if (!preindexedTokens2) {
                decoder2 = Token.stringDecoder(getIndex2());
                encoder2 = Token.stringEncoder(getIndex2());

            } else {
                decoder2 = Token.enumeratedDecoder();
                encoder2 = Token.enumeratedEncoder();
            }

            Source<Weighted<TokenPair>> srcA = new WeightedTokenPairSource(
                    new TSVSource(getSourceFileA(), getCharset()),
                    decoder1, decoder2);

            Source<Weighted<TokenPair>> srcB = new WeightedTokenPairSource(
                    new TSVSource(getSourceFileB(), getCharset()),
                    decoder1, decoder2);

            Sink<Weighted<TokenPair>> snk = new WeightedTokenPairSink(
                    new TSVSink(getDestFile(), getCharset()),
                    encoder1, encoder2);

            merge(srcA, srcB, snk);

            if (snk instanceof Flushable)
                ((Flushable) snk).flush();

            if(srcA instanceof Closeable) 
                ((Closeable)srcA).close();
            if(srcB instanceof Closeable) 
                ((Closeable)srcB).close();
            if(snk instanceof Closeable) 
                ((Closeable)snk).close();

            if (LOG.isInfoEnabled())
                LOG.info("Completed merge.");
        }
    }

    public static class EntryFreqsMergeTask extends MergeTask.WeightedTokenMergeTask {

        public EntryFreqsMergeTask(File sourceFileA, File sourceFileB,
                                   File destinationFile, Charset charset,
                                   boolean preindexed) {
            super(sourceFileA, sourceFileB, destinationFile, charset, preindexed);
        }

        public EntryFreqsMergeTask() {
        }
    }

    public static class FeatureFreqsMergeTask extends MergeTask.WeightedTokenMergeTask {

        public FeatureFreqsMergeTask(File sourceFileA, File sourceFileB,
                                     File destinationFile, Charset charset,
                                     boolean preindexed) {
            super(sourceFileA, sourceFileB, destinationFile, charset, preindexed);
        }

        public FeatureFreqsMergeTask() {
        }
    }

    public static class EventFreqsMergeTask extends MergeTask.WeightedTokenPairMergeTask {

        public EventFreqsMergeTask(File sourceFileA, File sourceFileB,
                                   File destinationFile, Charset charset,
                                   boolean preindexedTokens1,
                                   boolean preindexedTokens2) {
            super(sourceFileA, sourceFileB, destinationFile, charset,
                  preindexedTokens1, preindexedTokens2);
        }

        public EventFreqsMergeTask() {
        }
    }

    public static class EventMergeTask extends MergeTask.TokenPairMergeTask {

        public EventMergeTask(File sourceFileA, File sourceFileB,
                              File destinationFile, Charset charset,
                              boolean preindexedTokens1,
                              boolean preindexedTokens2) {
            super(sourceFileA, sourceFileB, destinationFile, charset,
                  preindexedTokens1, preindexedTokens2);
        }

        public EventMergeTask() {
        }
    }

    public static class SimsMergeTask extends MergeTask.WeightedTokenPairMergeTask {

        public SimsMergeTask(File sourceFileA, File sourceFileB,
                             File destinationFile, Charset charset,
                             boolean preindexedTokens1,
                             boolean preindexedTokens2) {
            super(sourceFileA, sourceFileB, destinationFile, charset,
                  preindexedTokens1, preindexedTokens2);
        }

        public SimsMergeTask() {
        }
    }
}
