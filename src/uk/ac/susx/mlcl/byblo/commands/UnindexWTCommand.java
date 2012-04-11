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
    private EnumeratorSingleBaring indexDeligate = new EnumeratorSingleBaringDeligate(false);

    public UnindexWTCommand(
            File sourceFile, File destinationFile, Charset charset,
            EnumeratorSingleBaring indexDeligate) {
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
        indexDeligate.close();
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

    public EnumeratorSingleBaring getIndexDeligate() {
        return indexDeligate;
    }

    public void setIndexDeligate(EnumeratorSingleBaring indexDeligate) {
        this.indexDeligate = indexDeligate;
    }

    protected EnumeratorSingleBaring sourceIndexDeligate() {
        return EnumeratorDeligates.decorateEnumerated(indexDeligate, true);
    }

    protected EnumeratorSingleBaring sinkIndexDeligate() {
        return EnumeratorDeligates.decorateEnumerated(indexDeligate, false);
    }

}
