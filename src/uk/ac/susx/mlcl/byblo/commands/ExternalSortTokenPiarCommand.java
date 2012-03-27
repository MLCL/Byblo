/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import com.beust.jcommander.ParametersDelegate;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.TokenPairSink;
import uk.ac.susx.mlcl.byblo.io.TokenPairSource;
import uk.ac.susx.mlcl.lib.io.*;

/**
 *
 * @author hiam20
 */
public class ExternalSortTokenPiarCommand extends AbstractExternalSortCommand<TokenPair> {

    @ParametersDelegate
    private final IndexDeligatePair indexDeligate = new IndexDeligatePair();

    public ExternalSortTokenPiarCommand(File sourceFile, File destinationFile, Charset charset, boolean preindexed1, boolean preindexed2) {
        super(sourceFile, destinationFile, charset);
        indexDeligate.setPreindexedTokens1(preindexed1);
        indexDeligate.setPreindexedTokens2(preindexed2);
    }

    public ExternalSortTokenPiarCommand() {
    }

    @Override
    protected Sink<TokenPair> openSink(File file) throws IOException {
        return new TokenPairSink(new TSVSink(file, getFileDeligate().getCharset()), getIndexDeligate().getEncoder1(), getIndexDeligate().getEncoder2());
    }

    @Override
    protected SeekableSource<TokenPair, Lexer.Tell> openSource(File file) throws IOException {
        return new TokenPairSource(new TSVSource(file, getFileDeligate().getCharset()), getIndexDeligate().getDecoder1(), getIndexDeligate().getDecoder2());
    }

    public IndexDeligatePair getIndexDeligate() {
        return indexDeligate;
    }
    
}
