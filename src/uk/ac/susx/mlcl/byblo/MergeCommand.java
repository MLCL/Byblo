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

import uk.ac.susx.mlcl.byblo.tasks.MergeTask;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Comparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;
import uk.ac.susx.mlcl.lib.io.*;
import uk.ac.susx.mlcl.lib.tasks.AbstractCommand;
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
public abstract class MergeCommand<T> extends AbstractCommand {

    private static final Log LOG = LogFactory.getLog(MergeCommand.class);

    private Comparator<T> comparator;

//    private Reducer<T> reducer = new DefaultReducer<T>();
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

    public MergeCommand(File sourceFileA, File sourceFileB, File destination,
                        Charset charset, Comparator<T> comparator) {
        setSourceFileA(sourceFileA);
        setSourceFileB(sourceFileB);
        setDestinationFile(destination);
        setCharset(charset);
        setComparator(comparator);
    }

    public MergeCommand() {
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

    protected abstract Source<T> openSource(File file) throws FileNotFoundException, IOException;

    protected abstract Sink<T> openSink(File file) throws FileNotFoundException, IOException;

    @Override
    public void runCommand() throws Exception {
        if (LOG.isInfoEnabled())
            LOG.info("Running merge from \"" + getSourceFileA()
                    + "\" and \"" + getSourceFileB()
                    + "\" to \"" + getDestFile() + "\".");

        Source<T> srcA = openSource(getSourceFileA());
        Source<T> srcB = openSource(getSourceFileB());
        Sink<T> snk = openSink(getDestFile());

        MergeTask<T> task = new MergeTask<T>(
                srcA, srcB, snk, getComparator());
        task.run();
        while (task.isExceptionThrown())
            task.throwException();

        if (snk instanceof Flushable)
            ((Flushable) snk).flush();

        if (srcA instanceof Closeable)
            ((Closeable) srcA).close();
        if (srcB instanceof Closeable)
            ((Closeable) srcB).close();
        if (snk instanceof Closeable)
            ((Closeable) snk).close();

        if (LOG.isInfoEnabled())
            LOG.info("Completed merge.");
    }

    public static class WeightedTokenMergeCommand extends MergeCommand<Weighted<Token>> {

        @ParametersDelegate
        protected SingleIndexDeligate indexDeligate = new SingleIndexDeligate();

        public WeightedTokenMergeCommand(
                File sourceFileA, File sourceFileB, File destinationFile,
                Charset charset, boolean preindexed) {
            super(sourceFileA, sourceFileB, destinationFile, charset,
                  Weighted.recordOrder(Token.indexOrder()));
            indexDeligate.setPreindexedTokens(preindexed);
        }

        public WeightedTokenMergeCommand() {
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

        public static void main(String[] args) throws Exception {
            new WeightedTokenMergeCommand().runCommand(args);
        }

    }

    public static class TokenPairMergeCommand extends MergeCommand<TokenPair> {

        @ParametersDelegate
        protected TwoIndexDeligate indexDeligate = new TwoIndexDeligate();

        public TokenPairMergeCommand(
                File sourceFileA, File sourceFileB, File destinationFile,
                Charset charset,
                boolean preindexedTokens1, boolean preindexedTokens2) {
            super(sourceFileA, sourceFileB, destinationFile, charset,
                  TokenPair.indexOrder());
            indexDeligate.setPreindexedTokens1(preindexedTokens1);
            indexDeligate.setPreindexedTokens2(preindexedTokens2);
        }

        public TokenPairMergeCommand() {
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

        public static void main(String[] args) throws Exception {
            new TokenPairMergeCommand().runCommand(args);
        }

    }

    public static class WeightedTokenPairMergeCommand
            extends MergeCommand<Weighted<TokenPair>> {

        @ParametersDelegate
        protected TwoIndexDeligate indexDeligate = new TwoIndexDeligate();

        public WeightedTokenPairMergeCommand(
                File sourceFileA, File sourceFileB, File destinationFile,
                Charset charset,
                boolean preindexedTokens1, boolean preindexedTokens2) {
            super(sourceFileA, sourceFileB, destinationFile, charset,
                  Weighted.recordOrder(TokenPair.indexOrder()));
            indexDeligate.setPreindexedTokens1(preindexedTokens1);
            indexDeligate.setPreindexedTokens2(preindexedTokens2);
        }

        public WeightedTokenPairMergeCommand() {
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

        public static void main(String[] args) throws Exception {
            new WeightedTokenMergeCommand().runCommand(args);
        }

    }
}
