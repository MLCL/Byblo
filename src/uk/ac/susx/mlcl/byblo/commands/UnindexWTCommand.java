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
public class UnindexWTCommand extends AbstractCopyCommand<Weighted<Token>> {

    @ParametersDelegate
    private IndexDeligate indexDeligate = new IndexDeligateImpl(false);

    public UnindexWTCommand(
            File sourceFile, File destinationFile, Charset charset,
            IndexDeligate indexDeligate) {
        super(sourceFile, destinationFile, charset);
        this.indexDeligate = indexDeligate;
    }

    public UnindexWTCommand() {
        super();
    }

    @Override
    public void runCommand() throws Exception {
        Checks.checkNotNull("indexFile", indexDeligate.getIndexFile());
        super.runCommand();
    }

    @Override
    protected Source<Weighted<Token>> openSource(File file)
            throws FileNotFoundException, IOException {
        return WeightedTokenSource.open(
                file, getFilesDeligate().getCharset(),
                sourceIndexDeligate());
    }

    @Override
    protected Sink<Weighted<Token>> openSink(File file)
            throws FileNotFoundException, IOException {
        return WeightedTokenSink.open(
                file, getFilesDeligate().getCharset(),
                sinkIndexDeligate());
    }

    public IndexDeligate getIndexDeligate() {
        return indexDeligate;
    }

    public void setIndexDeligate(IndexDeligate indexDeligate) {
        this.indexDeligate = indexDeligate;
    }

    protected IndexDeligate sourceIndexDeligate() {
        return IndexDeligates.decorateEnumerated(indexDeligate, true);
    }

    protected IndexDeligate sinkIndexDeligate() {
        return IndexDeligates.decorateEnumerated(indexDeligate, false);
    }

}
