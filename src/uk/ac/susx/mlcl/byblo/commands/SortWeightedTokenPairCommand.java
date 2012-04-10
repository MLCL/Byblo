/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.io.IndexDeligatePairImpl;
import com.beust.jcommander.ParametersDelegate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;

/**
 *
 * @author hiam20
 */
public class SortWeightedTokenPairCommand extends AbstractSortCommand<Weighted<TokenPair>> {

    private static final Log LOG = LogFactory.getLog(SortWeightedTokenCommand.class);

    @ParametersDelegate
    private IndexDeligatePair indexDeligate = new IndexDeligatePairImpl();

    public SortWeightedTokenPairCommand(
            File sourceFile, File destinationFile, Charset charset,
            IndexDeligatePair indexDeligate) {
        super(sourceFile, destinationFile, charset, Weighted.recordOrder(TokenPair.indexOrder()));
        setIndexDeligate(indexDeligate);
    }

    public SortWeightedTokenPairCommand() {
    }

    public final IndexDeligatePair getIndexDeligate() {
        return indexDeligate;
    }

    public final void setIndexDeligate(IndexDeligatePair indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }

    @Override
    protected Source<Weighted<TokenPair>> openSource(File file)
            throws FileNotFoundException, IOException {
        WeightedTokenPairSource s = WeightedTokenPairSource.open(
                file, getFilesDeligate().getCharset(),
                getIndexDeligate());
        return s;
    }

    @Override
    protected Sink<Weighted<TokenPair>> openSink(File file)
            throws FileNotFoundException, IOException {
        WeightedTokenPairSink s = WeightedTokenPairSink.open(
                file, getFilesDeligate().getCharset(),
                getIndexDeligate(),
                !getFilesDeligate().isCompactFormatDisabled());
        return new WeightSumReducerSink<TokenPair>(s);
    }

}
