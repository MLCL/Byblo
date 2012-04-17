/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
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
public class ExternalSortInstancesCommand extends AbstractExternalSortCommand<TokenPair> {

    private static final long serialVersionUID = 1L;

    @ParametersDelegate
    private DoubleEnumerating indexDeligate = new DoubleEnumeratingDeligate();

    public ExternalSortInstancesCommand(
            File sourceFile, File destinationFile, Charset charset,
            DoubleEnumerating indexDeligate) {
        super(sourceFile, destinationFile, charset);
        setIndexDeligate(indexDeligate);
    }

    public ExternalSortInstancesCommand() {
    }

    @Override
    public void runCommand() throws Exception {
        super.runCommand();
        indexDeligate.saveEnumerator();
        indexDeligate.closeEnumerator();

    }

    @Override
    protected Sink<TokenPair> openSink(File file) throws IOException {
        return BybloIO.openInstancesSink(file, getCharset(), indexDeligate);
    }

    @Override
    protected SeekableSource<TokenPair, Tell> openSource(File file) throws IOException {
        return BybloIO.openInstancesSource(file, getCharset(), indexDeligate);
    }

    public final DoubleEnumerating getIndexDeligate() {
        return indexDeligate;
    }

    public final void setIndexDeligate(DoubleEnumerating indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }

}
