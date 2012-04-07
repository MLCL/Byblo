/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.io.IndexDeligatePair;
import com.beust.jcommander.ParametersDelegate;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.TokenPairSink;
import uk.ac.susx.mlcl.byblo.io.TokenPairSource;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.*;

/**
 *
 * @author hiam20
 */
public class ExternalSortTokenPiarCommand extends AbstractExternalSortCommand<TokenPair> {

    private static final long serialVersionUID = 1L;

    @ParametersDelegate
    private IndexDeligatePair indexDeligate = new IndexDeligatePair();

    public ExternalSortTokenPiarCommand(
            File sourceFile, File destinationFile, Charset charset,
            IndexDeligatePair indexDeligate) {
        super(sourceFile, destinationFile, charset);
        setIndexDeligate(indexDeligate);
    }

    public ExternalSortTokenPiarCommand() {
    }

    @Override
    protected Sink<TokenPair> openSink(File file) throws IOException {
        TokenPairSink s =  TokenPairSink.open(
                file, getFileDeligate().getCharset(),
                getIndexDeligate(),
                !getFileDeligate().isCompactFormatDisabled());
//        s.setCompactFormatEnabled(!getFileDeligate().isCompactFormatDisabled());
        return s;
    }

    @Override
    protected SeekableSource<TokenPair, Tell> openSource(File file) throws IOException {
        return  TokenPairSource.open(
                file, getFileDeligate().getCharset(),
                getIndexDeligate());
    }

    public final IndexDeligatePair getIndexDeligate() {
        return indexDeligate;
    }

    public final void setIndexDeligate(IndexDeligatePair indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }

}
