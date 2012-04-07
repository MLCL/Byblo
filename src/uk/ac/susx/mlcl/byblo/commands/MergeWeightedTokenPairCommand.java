/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.io.IndexDeligatePair;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects.ToStringHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.byblo.io.WeightSumReducerSink;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSink;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSource;
import uk.ac.susx.mlcl.lib.Checks;
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
    private IndexDeligatePair indexDeligate = new IndexDeligatePair();

    public MergeWeightedTokenPairCommand(
            File sourceFileA, File sourceFileB, File destinationFile,
            Charset charset, IndexDeligatePair indexDeligate) {
        super(sourceFileA, sourceFileB, destinationFile, charset, Weighted.recordOrder(TokenPair.indexOrder()));
        setIndexDeligate(indexDeligate);
    }

    public MergeWeightedTokenPairCommand() {
    }

    public final IndexDeligatePair getIndexDeligate() {
        return indexDeligate;
    }

    public final void setIndexDeligate(IndexDeligatePair indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }

    @Override
    protected Source<Weighted<TokenPair>> openSource(File file) throws FileNotFoundException, IOException {
        return  WeightedTokenPairSource.open(
                file, getFileDeligate().getCharset(),
                indexDeligate);
    }

    @Override
    protected Sink<Weighted<TokenPair>> openSink(File file) throws FileNotFoundException, IOException {

        WeightedTokenPairSink s =  WeightedTokenPairSink.open(
                file, getFileDeligate().getCharset(),
                indexDeligate,
                !getFileDeligate().isCompactFormatDisabled());
//        s.setCompactFormatEnabled();
        return new WeightSumReducerSink<TokenPair>(s);
    }

    public static void main(String[] args) throws Exception {
        new MergeWeightedTokenCommand().runCommand(args);
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper().add("indexing", indexDeligate);
    }

}
