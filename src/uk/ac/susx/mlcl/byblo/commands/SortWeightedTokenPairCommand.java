/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.io.IndexDeligatePair;
import com.beust.jcommander.ParametersDelegate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class SortWeightedTokenPairCommand extends AbstractSortCommand<Weighted<TokenPair>> {

    private static final Log LOG = LogFactory.getLog(SortWeightedTokenCommand.class);

    @ParametersDelegate
    private IndexDeligatePair indexDeligate = new IndexDeligatePair();

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
        WeightedTokenPairSource s =  WeightedTokenPairSource.open(
                file, getFilesDeligate().getCharset(),
                getIndexDeligate()
                );
        return s;
    }

    @Override
    protected Sink<Weighted<TokenPair>> openSink(File file)
            throws FileNotFoundException, IOException {
        WeightedTokenPairSink s = WeightedTokenPairSink.open(
                file, getFilesDeligate().getCharset(),
                getIndexDeligate(),
                !getFilesDeligate().isCompactFormatDisabled());
//        s.setCompactFormatEnabled();
        return new WeightSumReducerSink<TokenPair>(s);
    }
    
    

}
