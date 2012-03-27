/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import com.beust.jcommander.ParametersDelegate;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.io.*;

/**
 *
 * @author hiam20
 */
public class ExternalSortWeightedTokenPiarCommand extends AbstractExternalSortCommand<Weighted<TokenPair>> {

    @ParametersDelegate
    private final IndexDeligatePair indexDeligate = new IndexDeligatePair();

    public ExternalSortWeightedTokenPiarCommand(File sourceFile, File destinationFile, Charset charset, boolean preindexed1, boolean preindexed2) {
        super(sourceFile, destinationFile, charset);
        indexDeligate.setPreindexedTokens1(preindexed1);
        indexDeligate.setPreindexedTokens2(preindexed2);
    }

    public ExternalSortWeightedTokenPiarCommand() {
    }

    @Override
    protected Sink<Weighted<TokenPair>> openSink(File file) throws IOException {
        return new WeightSumReducerSink<TokenPair>(new WeightedTokenPairSink(new TSVSink(file, getFileDeligate().getCharset()), getIndexDeligate().getEncoder1(), getIndexDeligate().getEncoder2()));
    }

    @Override
    protected SeekableSource<Weighted<TokenPair>, Lexer.Tell> openSource(File file) throws IOException {
        return new WeightedTokenPairSource(new TSVSource(file, getFileDeligate().getCharset()), getIndexDeligate().getDecoder1(), getIndexDeligate().getDecoder2());
    }

    public IndexDeligatePair getIndexDeligate() {
        return indexDeligate;
    }
    
}
