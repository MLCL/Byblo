/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.TokenPairSink;
import uk.ac.susx.mlcl.byblo.io.TokenPairSource;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;
import uk.ac.susx.mlcl.lib.io.TSVSink;
import uk.ac.susx.mlcl.lib.io.TSVSource;

/**
 *
 * @author hiam20
 */
public class MergeTokenPairCommand extends AbstractMergeCommand<TokenPair> {

    @ParametersDelegate
    protected final IndexDeligatePair indexDeligate = new IndexDeligatePair();

    public MergeTokenPairCommand(File sourceFileA, File sourceFileB, File destinationFile, Charset charset, boolean preindexedTokens1, boolean preindexedTokens2) {
        super(sourceFileA, sourceFileB, destinationFile, charset, TokenPair.indexOrder());
        indexDeligate.setPreindexedTokens1(preindexedTokens1);
        indexDeligate.setPreindexedTokens2(preindexedTokens2);
    }

    public MergeTokenPairCommand() {
    }

    @Override
    protected Source<TokenPair> openSource(File file) throws FileNotFoundException, IOException {
        return new TokenPairSource(
                new TSVSource(file, getFileDeligate().getCharset()),
                indexDeligate.getDecoder1(), indexDeligate.getDecoder2());
    }

    @Override
    protected Sink<TokenPair> openSink(File file) throws FileNotFoundException, IOException {
        return new TokenPairSink(
                new TSVSink(file, getFileDeligate().getCharset()),
                indexDeligate.getEncoder1(), indexDeligate.getEncoder2());
    }

    public static void main(String[] args) throws Exception {
        new MergeTokenPairCommand().runCommand(args);
    }
    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().add("indexing", indexDeligate);
    }

}
