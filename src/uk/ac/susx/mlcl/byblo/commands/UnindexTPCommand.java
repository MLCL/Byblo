/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratingDeligates;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
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
public class UnindexTPCommand extends AbstractCopyCommand<TokenPair> {

    @ParametersDelegate
    private DoubleEnumerating indexDeligate = new DoubleEnumeratingDeligate(false, false);

    public UnindexTPCommand(
            File sourceFile, File destinationFile, Charset charset,
            DoubleEnumerating indexDeligate) {
        super(sourceFile, destinationFile, charset);
        this.indexDeligate = indexDeligate;
    }

    public UnindexTPCommand() {
        super();
    }

    @Override
    public void runCommand() throws Exception {
        Checks.checkNotNull("indexFile1", indexDeligate.getEntryEnumeratorFile());
        Checks.checkNotNull("indexFile2", indexDeligate.getFeatureEnumeratorFile());
        super.runCommand();
                indexDeligate.closeEnumerator();

    }

    @Override
    protected Source<TokenPair> openSource(File file)
            throws FileNotFoundException, IOException {
        return TokenPairSource.open(
                file, getFilesDeligate().getCharset(),
                sourceIndexDeligate());
    }

    @Override
    protected Sink<TokenPair> openSink(File file)
            throws FileNotFoundException, IOException {

        return TokenPairSink.open(
                file, getFilesDeligate().getCharset(),
                sinkIndexDeligate(),
                !getFilesDeligate().isCompactFormatDisabled());
    }

    public DoubleEnumerating getIndexDeligate() {
        return indexDeligate;
    }

    public void setIndexDeligate(DoubleEnumeratingDeligate indexDeligate) {
        this.indexDeligate = indexDeligate;
    }

    protected DoubleEnumerating sourceIndexDeligate() {
        return EnumeratingDeligates.decorateEnumerated(indexDeligate, true);
    }

    protected DoubleEnumerating sinkIndexDeligate() {
        return EnumeratingDeligates.decorateEnumerated(indexDeligate, false);
    }

}
