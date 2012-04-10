/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.io.IndexDeligateImpl;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;

/**
 *
 * @author hiam20
 */
public class MergeWeightedTokenCommand extends AbstractMergeCommand<Weighted<Token>> {

    @ParametersDelegate
    private IndexDeligate indexDeligate = new IndexDeligateImpl();

    public MergeWeightedTokenCommand(
            File sourceFileA, File sourceFileB, File destinationFile, 
            Charset charset, IndexDeligate indexDeligate) {
        super(sourceFileA, sourceFileB, destinationFile, charset,
              Weighted.recordOrder(Token.indexOrder()));
        setIndexDeligate(indexDeligate);
    }

    public MergeWeightedTokenCommand() {
    }

    public final IndexDeligate getIndexDeligate() {
        return indexDeligate;
    }

    public final void setIndexDeligate(IndexDeligate indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }

    
    @Override
    protected Source<Weighted<Token>> openSource(File file) throws FileNotFoundException, IOException {
        return  WeightedTokenSource.open(file, getFileDeligate().getCharset(),
                                        indexDeligate);
//
//                new WeightedTokenSource(
//                new TSVSource(file, getFileDeligate().getCharset()),
//                indexDeligate);
    }

    @Override
    protected Sink<Weighted<Token>> openSink(File file) throws FileNotFoundException, IOException {
        WeightedTokenSink s = WeightedTokenSink.open(
                file, getFileDeligate().getCharset(), indexDeligate);
//        new WeightedTokenSink(
//                new TSVSink(file, getFileDeligate().getCharset()),
//                indexDeligate);
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
