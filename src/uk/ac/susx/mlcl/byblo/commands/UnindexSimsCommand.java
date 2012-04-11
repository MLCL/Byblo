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
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;

/**
 *
 * @author hiam20
 */
public class UnindexSimsCommand extends AbstractCopyCommand<Weighted<TokenPair>> {

    @ParametersDelegate
    private EnumeratorSingleBaring indexDeligate = new EnumeratorSingleBaringDeligate(false);

    public UnindexSimsCommand(
            File sourceFile, File destinationFile, Charset charset,
            EnumeratorSingleBaring indexDeligate) {
        super(sourceFile, destinationFile, charset);
        this.indexDeligate = indexDeligate;
    }

    public UnindexSimsCommand() {
        super();
    }

    @Override
    public void runCommand() throws Exception {
        Checks.checkNotNull("indexFile", indexDeligate.getIndexFile());
        super.runCommand();

        indexDeligate.close();

    
    }

    @Override
    protected Source<Weighted<TokenPair>> openSource(File file)
            throws FileNotFoundException, IOException {
        return WeightedTokenPairSource.open(
                file, getFilesDeligate().getCharset(),
                sourceIndexDeligate());
    }

    @Override
    protected Sink<Weighted<TokenPair>> openSink(File file)
            throws FileNotFoundException, IOException {
        return WeightedTokenPairSink.open(
                file, getFilesDeligate().getCharset(),
                sinkIndexDeligate(), !getFilesDeligate().isCompactFormatDisabled());
    }

    public EnumeratorSingleBaring getIndexDeligate() {
        return indexDeligate;
    }

    public void setIndexDeligate(EnumeratorSingleBaring indexDeligate) {
        this.indexDeligate = indexDeligate;
    }

    protected EnumeratorPairBaring sourceIndexDeligate() {
        return new EnumeratorDeligates.SingleToPairAdapter(
                EnumeratorDeligates.decorateEnumerated(indexDeligate, true));
    }

    protected EnumeratorPairBaring sinkIndexDeligate() {
        return new EnumeratorDeligates.SingleToPairAdapter(
                EnumeratorDeligates.decorateEnumerated(indexDeligate, false));
    }

}
