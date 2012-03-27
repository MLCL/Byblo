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
public class ExternalSortWeightedTokenCommand extends AbstractExternalSortCommand<Weighted<Token>> {

    @ParametersDelegate
    private final IndexDeligateSingle indexDeligate = new IndexDeligateSingle();

    public ExternalSortWeightedTokenCommand(File sourceFile, File destinationFile, Charset charset, boolean preindexed) {
        super(sourceFile, destinationFile, charset);
        indexDeligate.setPreindexedTokens(preindexed);
    }

    public ExternalSortWeightedTokenCommand() {
    }

    @Override
    protected Sink<Weighted<Token>> openSink(File file) throws IOException {
        return new WeightSumReducerSink<Token>(new WeightedTokenSink(new TSVSink(file, getFileDeligate().getCharset()), getIndexDeligate().getEncoder()));
    }

    @Override
    protected SeekableSource<Weighted<Token>, Lexer.Tell> openSource(File file) throws IOException {
        return new WeightedTokenSource(new TSVSource(file, getFileDeligate().getCharset()), getIndexDeligate().getDecoder());
    }

    public IndexDeligateSingle getIndexDeligate() {
        return indexDeligate;
    }
    
}
