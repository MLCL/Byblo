/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

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
    private final IndexDeligatePair indexDeligate = new IndexDeligatePair();

    public SortWeightedTokenPairCommand(
            File sourceFile, File destinationFile, Charset charset,
            boolean preindexedTokens1, boolean preindexedTokens2) {
        super(sourceFile, destinationFile, charset, Weighted.recordOrder(TokenPair.indexOrder()));
        indexDeligate.setPreindexedTokens1(preindexedTokens1);
        indexDeligate.setPreindexedTokens2(preindexedTokens2);
    }

    public SortWeightedTokenPairCommand() {
    }

    public IndexDeligatePair getIndexDeligate() {
        return indexDeligate;
    }

    @Override
    protected Source<Weighted<TokenPair>> openSource(File file)
            throws FileNotFoundException, IOException {
        return new WeightedTokenPairSource(
                new TSVSource(file, getFilesDeligate().getCharset()),
                getIndexDeligate().getDecoder1(), getIndexDeligate().getDecoder2());
    }

    @Override
    protected Sink<Weighted<TokenPair>> openSink(File file)
            throws FileNotFoundException, IOException {
        return new WeightSumReducerSink<TokenPair>(
                new WeightedTokenPairSink(
                new TSVSink(file, getFilesDeligate().getCharset()),
                getIndexDeligate().getEncoder1(), getIndexDeligate().getEncoder2()));
    }

}
