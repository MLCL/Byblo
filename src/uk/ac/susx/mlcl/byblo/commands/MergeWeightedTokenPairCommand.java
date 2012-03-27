/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects.ToStringHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.byblo.WeightSumReducerSink;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSink;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSource;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;
import uk.ac.susx.mlcl.lib.io.TSVSink;
import uk.ac.susx.mlcl.lib.io.TSVSource;

/**
 *
 * @author hiam20
 */
public class MergeWeightedTokenPairCommand extends AbstractMergeCommand<Weighted<TokenPair>> {

    @ParametersDelegate
    protected final IndexDeligatePair indexDeligate = new IndexDeligatePair();

    public MergeWeightedTokenPairCommand(File sourceFileA, File sourceFileB, File destinationFile, Charset charset, boolean preindexedTokens1, boolean preindexedTokens2) {
        super(sourceFileA, sourceFileB, destinationFile, charset, Weighted.recordOrder(TokenPair.indexOrder()));
        indexDeligate.setPreindexedTokens1(preindexedTokens1);
        indexDeligate.setPreindexedTokens2(preindexedTokens2);
    }

    public MergeWeightedTokenPairCommand() {
    }

    @Override
    protected Source<Weighted<TokenPair>> openSource(File file) throws FileNotFoundException, IOException {
        return new WeightedTokenPairSource(
                new TSVSource(file, getFileDeligate().getCharset()),
                indexDeligate.getDecoder1(), indexDeligate.getDecoder2());
    }

    @Override
    protected Sink<Weighted<TokenPair>> openSink(File file) throws FileNotFoundException, IOException {
        return new WeightSumReducerSink<TokenPair>(
                new WeightedTokenPairSink(
                new TSVSink(file, getFileDeligate().getCharset()),
                indexDeligate.getEncoder1(), indexDeligate.getEncoder2()));
    }

    public static void main(String[] args) throws Exception {
        new MergeWeightedTokenCommand().runCommand(args);
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper().add("indexing", indexDeligate);
    }
    
    

}
