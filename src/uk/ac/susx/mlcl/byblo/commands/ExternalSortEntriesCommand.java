/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumerating;
import com.beust.jcommander.ParametersDelegate;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.SeekableSource;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Tell;

/**
 *
 * @author hiam20
 */
public class ExternalSortEntriesCommand extends AbstractExternalSortCommand<Weighted<Token>> {

    private static final long serialVersionUID = 1L;

    @ParametersDelegate
    private SingleEnumerating indexDeligate = new SingleEnumeratingDeligate();

    public ExternalSortEntriesCommand(
            File sourceFile, File destinationFile, Charset charset,
            SingleEnumerating indexDeligate) {
        super(sourceFile, destinationFile, charset);
        setIndexDeligate(indexDeligate);
    }

    public ExternalSortEntriesCommand() {
    }

    @Override
    public void runCommand() throws Exception {
        super.runCommand();
        indexDeligate.saveEnumerator();
        indexDeligate.closeEnumerator();

    }

    @Override
    protected Sink<Weighted<Token>> openSink(File file) throws IOException {
        return new WeightSumReducerSink<Token>(
                BybloIO.openEntriesSink(file, getCharset(), indexDeligate));
    }

    @Override
    protected SeekableSource<Weighted<Token>, Tell> openSource(File file) throws IOException {
        return BybloIO.openEntriesSource(file, getCharset(), indexDeligate);
    }

    public final SingleEnumerating getIndexDeligate() {
        return indexDeligate;
    }

    public final void setIndexDeligate(SingleEnumerating indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }

}
