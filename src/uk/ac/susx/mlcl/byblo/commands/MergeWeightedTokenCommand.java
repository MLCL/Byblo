/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.io.IndexDeligate;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.byblo.io.WeightSumReducerSink;
import uk.ac.susx.mlcl.byblo.io.Token;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenSink;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenSource;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;
import uk.ac.susx.mlcl.lib.io.TSVSink;
import uk.ac.susx.mlcl.lib.io.TSVSource;

/**
 *
 * @author hiam20
 */
public class MergeWeightedTokenCommand extends AbstractMergeCommand<Weighted<Token>> {

    @ParametersDelegate
    protected final IndexDeligate indexDeligate = new IndexDeligate();

    public MergeWeightedTokenCommand(File sourceFileA, File sourceFileB, File destinationFile, Charset charset, boolean preindexed) {
        super(sourceFileA, sourceFileB, destinationFile, charset,
              Weighted.recordOrder(Token.indexOrder()));
        indexDeligate.setPreindexedTokens(preindexed);
    }

    public MergeWeightedTokenCommand() {
    }

    @Override
    protected Source<Weighted<Token>> openSource(File file) throws FileNotFoundException, IOException {
        return new WeightedTokenSource(
                new TSVSource(file, getFileDeligate().getCharset()),
                indexDeligate);
    }

    @Override
    protected Sink<Weighted<Token>> openSink(File file) throws FileNotFoundException, IOException {
        WeightedTokenSink s = new WeightedTokenSink(
                new TSVSink(file, getFileDeligate().getCharset()),
                indexDeligate);
        s.setCompactFormatEnabled(!getFileDeligate().isCompactFormatDisabled());
        return new WeightSumReducerSink<Token>(s);
    }

    public static void main(String[] args) throws Exception {
        new MergeWeightedTokenCommand().runCommand(args);
    }
    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().add("indexing", indexDeligate);
    }

}
