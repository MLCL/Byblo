/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratingDeligates;
import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumerating;
import com.beust.jcommander.ParametersDelegate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
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
    private SingleEnumerating indexDeligate = new SingleEnumeratingDeligate(
            Enumerating.DEFAULT_TYPE, false, null, false, false);

    public UnindexWTCommand(
            File sourceFile, File destinationFile, Charset charset,
            SingleEnumerating indexDeligate) {
        super(sourceFile, destinationFile, charset);
        this.indexDeligate = indexDeligate;
    }

    public UnindexWTCommand() {
        super();
    }

    @Override
    public void runCommand() throws Exception {
        Checks.checkNotNull("indexFile", indexDeligate.getEnumeratorFile());
        super.runCommand();
        indexDeligate.closeEnumerator();
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

    public SingleEnumerating getIndexDeligate() {
        return indexDeligate;
    }

    public void setIndexDeligate(SingleEnumerating indexDeligate) {
        this.indexDeligate = indexDeligate;
    }

    protected SingleEnumerating sourceIndexDeligate() {
        return EnumeratingDeligates.decorateEnumerated(indexDeligate, true);
    }

    protected SingleEnumerating sinkIndexDeligate() {
        return EnumeratingDeligates.decorateEnumerated(indexDeligate, false);
    }

}
