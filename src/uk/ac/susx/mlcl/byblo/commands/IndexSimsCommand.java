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
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;

/**
 *
 * @author hiam20
 */
public class IndexSimsCommand extends AbstractCopyCommand<Weighted<TokenPair>> {

    @ParametersDelegate
    private IndexDeligate indexDeligate = new IndexDeligateImpl(false);

    public IndexSimsCommand(
            File sourceFile, File destinationFile, Charset charset,
            IndexDeligate indexDeligate) {
        super(sourceFile, destinationFile, charset);
        this.indexDeligate = indexDeligate;
    }

    public IndexSimsCommand() {
        super();
    }

    @Override
    public void runCommand() throws Exception {
        Checks.checkNotNull("indexFile", indexDeligate.getIndexFile());

        super.runCommand();

        Enumerators.saveStringEnumerator(indexDeligate.getEnumerator(),
                                         indexDeligate.getIndexFile());
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
                sinkIndexDeligate(),
                !getFilesDeligate().isCompactFormatDisabled());
    }

    public IndexDeligate getIndexDeligate() {
        return indexDeligate;
    }

    public void setIndexDeligate(IndexDeligate indexDeligate) {
        this.indexDeligate = indexDeligate;
    }

    protected IndexDeligatePair sourceIndexDeligate() {
        return new IndexDeligates.SingleToPairAdapter(
                IndexDeligates.decorateEnumerated(indexDeligate, false));
    }

    protected IndexDeligatePair sinkIndexDeligate() {
        return new IndexDeligates.SingleToPairAdapter(
                IndexDeligates.decorateEnumerated(indexDeligate, true));
    }

}
