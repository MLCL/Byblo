/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.io.IndexDeligatePairImpl;
import com.beust.jcommander.ParametersDelegate;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.*;

/**
 *
 * @author hiam20
 */
public class ExternalSortWeightedTokenPiarCommand extends AbstractExternalSortCommand<Weighted<TokenPair>> {

    @ParametersDelegate
    private IndexDeligatePair indexDeligate = new IndexDeligatePairImpl();

    public ExternalSortWeightedTokenPiarCommand(
            File sourceFile, File destinationFile, Charset charset,
            IndexDeligatePair indexDeligate) {
        super(sourceFile, destinationFile, charset);
        setIndexDeligate(indexDeligate);
    }

    public ExternalSortWeightedTokenPiarCommand() {
    }

    @Override
    protected Sink<Weighted<TokenPair>> openSink(File file) throws IOException {
        WeightedTokenPairSink s = WeightedTokenPairSink.open(
                file, getFileDeligate().getCharset(),
                getIndexDeligate(),
                !getFileDeligate().isCompactFormatDisabled());
        return new WeightSumReducerSink<TokenPair>(s);
    }

    @Override
    protected WeightedTokenPairSource openSource(File file) throws IOException {
        return WeightedTokenPairSource.open(
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
