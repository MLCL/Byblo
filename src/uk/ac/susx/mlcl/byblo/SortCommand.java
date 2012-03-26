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
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.io.*;

/**
 * Task that takes a single input file and sorts it according to some
 * comparator, then writes the results to an output file.
 *
 * @param <T> 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Sort a file.")
public abstract class SortCommand<T> extends CopyCommand {

    private static final Log LOG = LogFactory.getLog(SortCommand.class);

    private Comparator<T> comparator;

    @Parameter(names = {"-c", "--charset"},
    description = "The character set encoding to use for both input and output files.")
    private Charset charset = Files.DEFAULT_CHARSET;

    @Parameter(names = {"-r", "--reverse"},
    description = "Reverse the result of comparisons.")
    private boolean reverse = false;

    public SortCommand(File sourceFile, File destinationFile, Charset charset,
                       Comparator<T> comparator) {
        super(sourceFile, destinationFile);
        setCharset(charset);
        this.comparator = comparator;
    }

    public SortCommand(File sourceFile, File destinationFile, Charset charset) {
        this(sourceFile, destinationFile, charset, null);
    }

    public SortCommand(File sourceFile, File destinationFile) {
        this(sourceFile, destinationFile, Files.DEFAULT_CHARSET);
    }

    public SortCommand() {
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
        return isReverse() ? Comparators.reverse(comparator) : comparator;
    }

    public final void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public boolean isComparatorSet() {
        return getComparator() != null;
    }

    @Override
    public void runCommand() throws Exception {
        if (LOG.isInfoEnabled())
            LOG.info("Running memory sort from \"" + filesDeligate.getSourceFile()
                    + "\" to \"" + filesDeligate.getDestinationFile() + "\".");

        Source<T> src = openSource(filesDeligate.getSourceFile());


        final List<T> items = IOUtil.readAll(src);

        if (src instanceof Closeable)
            ((Closeable) src).close();

        Collections.sort(items, getComparator());

        Sink<T> snk = openSink(filesDeligate.getDestinationFile());

        int i = IOUtil.copy(items, snk);
        assert i == items.size();

        if (snk instanceof Flushable)
            ((Flushable) snk).flush();
        if (snk instanceof Closeable)
            ((Closeable) snk).close();

        if (LOG.isInfoEnabled())
            LOG.info("Completed memory sort.");
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("charset", charset).
                add("comparator", comparator);
    }

    protected abstract Source<T> openSource(File file)
            throws FileNotFoundException, IOException;

    protected abstract Sink<T> openSink(File file)
            throws FileNotFoundException, IOException;

    public static class WeightedTokenSortCommand extends SortCommand<Weighted<Token>> {

        private static final Log LOG = LogFactory.getLog(
                WeightedTokenSortCommand.class);

        @ParametersDelegate
        protected SingleIndexDeligate indexDeligate = new SingleIndexDeligate();

        public WeightedTokenSortCommand(
                File sourceFile, File destinationFile, Charset charset,
                boolean preindexed) {
            super(sourceFile, destinationFile, charset,
                  Weighted.recordOrder(Token.indexOrder()));
            indexDeligate.setPreindexedTokens(preindexed);
        }

        public WeightedTokenSortCommand() {
        }

        @Override
        protected Source<Weighted<Token>> openSource(File file)
                throws FileNotFoundException, IOException {
            return new WeightedTokenSource(
                    new TSVSource(file, getCharset()), indexDeligate.getDecoder());
        }

        @Override
        protected Sink<Weighted<Token>> openSink(File file)
                throws FileNotFoundException, IOException {
            return new WeightSumReducerSink<Token>(
                    new WeightedTokenSink(
                    new TSVSink(file, getCharset()), indexDeligate.getEncoder()));
        }

    }

    public static class WeightedTokenPairSortCommand extends SortCommand<Weighted<TokenPair>> {

        private static final Log LOG = LogFactory.getLog(
                WeightedTokenSortCommand.class);

        @ParametersDelegate
        protected TwoIndexDeligate indexDeligate = new TwoIndexDeligate();

        public WeightedTokenPairSortCommand(
                File sourceFile, File destinationFile, Charset charset,
                boolean preindexedTokens1, boolean preindexedTokens2) {
            super(sourceFile, destinationFile, charset,
                  Weighted.recordOrder(TokenPair.indexOrder()));
            indexDeligate.setPreindexedTokens1(preindexedTokens1);
            indexDeligate.setPreindexedTokens2(preindexedTokens2);
        }

        public WeightedTokenPairSortCommand() {
        }

        @Override
        protected Source<Weighted<TokenPair>> openSource(File file)
                throws FileNotFoundException, IOException {
            return new WeightedTokenPairSource(
                    new TSVSource(file, getCharset()),
                    indexDeligate.getDecoder1(), indexDeligate.getDecoder2());
        }

        @Override
        protected Sink<Weighted<TokenPair>> openSink(File file)
                throws FileNotFoundException, IOException {
            return new WeightSumReducerSink<TokenPair>(
                    new WeightedTokenPairSink(
                    new TSVSink(file, getCharset()),
                    indexDeligate.getEncoder1(), indexDeligate.getEncoder2()));
        }

    }

    public static class TokenPairSortCommand extends SortCommand<TokenPair> {

        private static final Log LOG = LogFactory.getLog(
                WeightedTokenSortCommand.class);

        @ParametersDelegate
        protected TwoIndexDeligate indexDeligate = new TwoIndexDeligate();

        public TokenPairSortCommand(
                File sourceFile, File destinationFile, Charset charset,
                boolean preindexedTokens1, boolean preindexedTokens2) {
            super(sourceFile, destinationFile, charset, TokenPair.indexOrder());
            indexDeligate.setPreindexedTokens1(preindexedTokens1);
            indexDeligate.setPreindexedTokens2(preindexedTokens2);
        }

        public TokenPairSortCommand() {
        }

        @Override
        protected Source<TokenPair> openSource(File file)
                throws FileNotFoundException, IOException {
            return new TokenPairSource(
                    new TSVSource(file, getCharset()),
                    indexDeligate.getDecoder1(), indexDeligate.getDecoder2());
        }

        @Override
        protected Sink<TokenPair> openSink(File file)
                throws FileNotFoundException, IOException {
            return new TokenPairSink(
                    new TSVSink(file, getCharset()),
                    indexDeligate.getEncoder1(), indexDeligate.getEncoder2());
        }

    }

}
