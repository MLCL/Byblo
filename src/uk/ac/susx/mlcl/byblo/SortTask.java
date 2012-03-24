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
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;
import uk.ac.susx.mlcl.lib.io.*;
import uk.ac.susx.mlcl.lib.tasks.ReverseComparator;

/**
 * Task that takes a single input file and sorts it according to some
 * comparator, then writes the results to an output file.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Sort a file.")
public abstract class SortTask<T> extends CopyTask {

    private static final Log LOG = LogFactory.getLog(SortTask.class);

    private Comparator<T> comparator;

    @Parameter(names = {"-c", "--charset"},
               description = "The character set encoding to use for both input and output files.")
    private Charset charset = Files.DEFAULT_CHARSET;

    @Parameter(names = {"-r", "--reverse"},
               description = "Reverse the result of comparisons.")
    private boolean reverse = false;

    public SortTask(File sourceFile, File destinationFile, Charset charset,
                    Comparator<T> comparator) {
        super(sourceFile, destinationFile);
        setCharset(charset);
        this.comparator = comparator;
    }

    public SortTask(File sourceFile, File destinationFile, Charset charset) {
        this(sourceFile, destinationFile, charset, null);
    }

    public SortTask(File sourceFile, File destinationFile) {
        this(sourceFile, destinationFile, Files.DEFAULT_CHARSET);
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

    public final Comparator<T> getComparator() {
        return isReverse() ? new ReverseComparator<T>(comparator) : comparator;
    }

    public final void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public boolean isComparatorSet() {
        return getComparator() != null;
    }

//    @Override
//    protected void runTask() throws Exception {
//        if (LOG.isInfoEnabled())
//            LOG.info("Running memory sort from \"" + getSrcFile()
//                    + "\" to \"" + getDstFile() + "\".");
//
//
//        final List<String> lines = new ArrayList<String>();
//        Files.readAllLines(getSrcFile(), getCharset(), lines);
//        Collections.sort(lines, (Comparator<String>) getComparator());
//        Files.writeAllLines(getDstFile(), getCharset(), lines);
//
//        if (LOG.isInfoEnabled())
//            LOG.info("Completed memory sort.");
//
//    }
    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("charset", charset).
                add("comparator", comparator);
    }

    public abstract static class WeightedTokenSortTask extends SortTask<Weighted<Token>> {

        private static final Log LOG = LogFactory.getLog(
                WeightedTokenSortTask.class);

        @Parameter(names = {"-p", "--preindexed"},
                   description = "Whether tokens in the input events file are indexed.")
        private boolean preindexedTokens = false;

        public WeightedTokenSortTask(
                File sourceFile, File destinationFile, Charset charset,
                boolean preindexed) {
            super(sourceFile, destinationFile, charset,
                  Weighted.recordOrder(Token.indexOrder()));
            setPreindexedTokens(preindexed);
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
        protected void runTask() throws Exception {
            if (LOG.isInfoEnabled())
                LOG.info("Running memory sort from \"" + getSrcFile()
                        + "\" to \"" + getDstFile() + "\".");

            final Function<String, Integer> decoder;
            final Function<Integer, String> encoder;

            if (!preindexedTokens) {
                decoder = Token.stringDecoder(getIndex());
                encoder = Token.stringEncoder(getIndex());
            } else {
                decoder = Token.enumeratedDecoder();
                encoder = Token.enumeratedEncoder();
            }


            WeightedTokenSource src = new WeightedTokenSource(
                    new TSVSource(getSrcFile(), getCharset()),
                    decoder);
            final List<Weighted<Token>> items = IOUtil.readAll(src);

            if (src instanceof Closeable)
                ((Closeable) src).close();

            Collections.sort(items, getComparator());

            WeightedTokenSink snk = new WeightedTokenSink(
                    new TSVSink(getDstFile(), getCharset()),
                    encoder);

            int i = IOUtil.copy(items, snk);
            assert i == items.size();

            snk.flush();
            snk.close();

            if (LOG.isInfoEnabled())
                LOG.info("Completed memory sort.");
        }
    }

    public abstract static class WeightedTokenPairSortTask extends SortTask<Weighted<TokenPair>> {

        private static final Log LOG = LogFactory.getLog(
                WeightedTokenSortTask.class);

        @Parameter(names = {"-p1", "--preindexed1"},
                   description = "Whether tokens in the first column of the input file are indexed.")
        private boolean preindexedTokens1 = false;

        @Parameter(names = {"-p2", "--preindexed2"},
                   description = "Whether entries in the second column of the input file are indexed.")
        private boolean preindexedTokens2 = false;

        public WeightedTokenPairSortTask(
                File sourceFile, File destinationFile, Charset charset,
                boolean preindexedTokens1, boolean preindexedTokens2) {
            super(sourceFile, destinationFile, charset,
                  Weighted.recordOrder(TokenPair.indexOrder()));
            setPreindexedTokens1(preindexedTokens1);
            setPreindexedTokens2(preindexedTokens2);
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
        protected void runTask() throws Exception {
            if (LOG.isInfoEnabled())
                LOG.info("Running memory sort from \"" + getSrcFile()
                        + "\" to \"" + getDstFile() + "\".");

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

            Source<Weighted<TokenPair>> src = new WeightedTokenPairSource(
                    new TSVSource(getSrcFile(), getCharset()),
                    decoder1, decoder2);

            final List<Weighted<TokenPair>> items = IOUtil.readAll(src);

            if (src instanceof Closeable)
                ((Closeable) src).close();

            Collections.sort(items, getComparator());

            WeightedTokenPairSink snk = new WeightedTokenPairSink(
                    new TSVSink(getDstFile(), getCharset()),
                    encoder1, encoder2);

            IOUtil.copy(items, snk);


            snk.flush();
            snk.close();

            if (LOG.isInfoEnabled())
                LOG.info("Completed memory sort.");
        }
    }

    public abstract static class TokenPairSortTask extends SortTask<TokenPair> {

        private static final Log LOG = LogFactory.getLog(
                WeightedTokenSortTask.class);

        @Parameter(names = {"-p1", "--preindexed1"},
                   description = "Whether tokens in the first column of the input file are indexed.")
        private boolean preindexedTokens1 = false;

        @Parameter(names = {"-p2", "--preindexed2"},
                   description = "Whether entries in the second column of the input file are indexed.")
        private boolean preindexedTokens2 = false;

        public TokenPairSortTask(
                File sourceFile, File destinationFile, Charset charset,
                boolean preindexedTokens1, boolean preindexedTokens2) {
            super(sourceFile, destinationFile, charset, TokenPair.indexOrder());
            setPreindexedTokens1(preindexedTokens1);
            setPreindexedTokens2(preindexedTokens2);
        }

        private Enumerator<String> entryIndex = null;

        private Enumerator<String> featureIndex = null;

        public Enumerator<String> getIndex1() {
            if (entryIndex == null)
                entryIndex = Enumerators.newDefaultStringEnumerator();
            return entryIndex;
        }

        public void setIndex1(Enumerator<String> entryIndex) {
            this.entryIndex = entryIndex;
        }

        public Enumerator<String> getIndex2() {
            if (featureIndex == null)
                featureIndex = Enumerators.newDefaultStringEnumerator();
            return featureIndex;
        }

        public void setIndex2(Enumerator<String> featureIndex) {
            this.featureIndex = featureIndex;
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
        protected void runTask() throws Exception {
            if (LOG.isInfoEnabled())
                LOG.info("Running memory sort from \"" + getSrcFile()
                        + "\" to \"" + getDstFile() + "\".");

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

            Source<TokenPair> src = new TokenPairSource(
                    new TSVSource(getSrcFile(), getCharset()),
                    decoder1, decoder2);
            if (src instanceof Closeable) {
                ((Closeable) src).close();
            }

            final List<TokenPair> items = IOUtil.readAll(src);

            if (src instanceof Closeable)
                ((Closeable) src).close();
            
            Collections.sort(items, getComparator());

            TokenPairSink snk = new TokenPairSink(
                    new TSVSink(getDstFile(), getCharset()),
                    encoder1, encoder2);

            IOUtil.copy(items, snk);

            snk.flush();
            snk.close();

            if (LOG.isInfoEnabled())
                LOG.info("Completed memory sort.");
        }
    }

    public static class EntryFreqsSortTask extends WeightedTokenSortTask {

        public EntryFreqsSortTask(File sourceFile, File destinationFile,
                                  Charset charset, boolean preindexed) {
            super(sourceFile, destinationFile, charset, preindexed);
        }
    }

    public static class FeatureFreqsSortTask extends WeightedTokenSortTask {

        public FeatureFreqsSortTask(File sourceFile, File destinationFile,
                                    Charset charset, boolean preindexed) {
            super(sourceFile, destinationFile, charset, preindexed);
        }
    }

    public static class EventFreqsSortTask extends WeightedTokenPairSortTask {

        public EventFreqsSortTask(File sourceFile, File destinationFile,
                                  Charset charset, boolean preindexedTokens1,
                                  boolean preindexedTokens2) {
            super(sourceFile, destinationFile, charset, preindexedTokens1,
                  preindexedTokens2);
        }
    }

    public static class EventSortTask extends TokenPairSortTask {

        public EventSortTask(File sourceFile, File destinationFile,
                             Charset charset, boolean preindexedTokens1,
                             boolean preindexedTokens2) {
            super(sourceFile, destinationFile, charset, preindexedTokens1,
                  preindexedTokens2);
        }
    }

    public static class SimsSortTask extends WeightedTokenPairSortTask {

        public SimsSortTask(File sourceFile, File destinationFile,
                            Charset charset, boolean preindexedTokens1,
                            boolean preindexedTokens2) {
            super(sourceFile, destinationFile, charset, preindexedTokens1,
                  preindexedTokens2);
        }
    }
}
