/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.io.EnumeratorPairBaring;
import uk.ac.susx.mlcl.byblo.io.EnumeratorDeligates;
import uk.ac.susx.mlcl.byblo.io.EnumeratorPairBaringDeligate;
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
    private EnumeratorPairBaring indexDeligate = new EnumeratorPairBaringDeligate(false, false);

    public UnindexTPCommand(
            File sourceFile, File destinationFile, Charset charset,
            EnumeratorPairBaring indexDeligate) {
        super(sourceFile, destinationFile, charset);
        this.indexDeligate = indexDeligate;
    }

    public UnindexTPCommand() {
        super();
    }

    @Override
    public void runCommand() throws Exception {
        Checks.checkNotNull("indexFile1", indexDeligate.getEntryIndexFile());
        Checks.checkNotNull("indexFile2", indexDeligate.getFeatureIndexFile());
        super.runCommand();
                indexDeligate.close();

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

    public EnumeratorPairBaring getIndexDeligate() {
        return indexDeligate;
    }

    public void setIndexDeligate(EnumeratorPairBaringDeligate indexDeligate) {
        this.indexDeligate = indexDeligate;
    }

    protected EnumeratorPairBaring sourceIndexDeligate() {
        return EnumeratorDeligates.decorateEnumerated(indexDeligate, true);
    }

    protected EnumeratorPairBaring sinkIndexDeligate() {
        return EnumeratorDeligates.decorateEnumerated(indexDeligate, false);
    }

}
