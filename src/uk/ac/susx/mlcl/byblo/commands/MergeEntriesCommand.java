/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumeratingDeligate;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects;
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
public class MergeEntriesCommand extends AbstractMergeCommand<Weighted<Token>> {

    @ParametersDelegate
    private SingleEnumerating indexDeligate = new SingleEnumeratingDeligate();

    public MergeEntriesCommand(
            File sourceFileA, File sourceFileB, File destinationFile,
            Charset charset, SingleEnumerating indexDeligate) {
        super(sourceFileA, sourceFileB, destinationFile, charset,
              Weighted.recordOrder(Token.indexOrder()));
        setIndexDeligate(indexDeligate);
    }

    public MergeEntriesCommand() {
    }

    @Override
    public void runCommand() throws Exception {
        super.runCommand();
        indexDeligate.saveEnumerator();
        indexDeligate.closeEnumerator();

    }

    public final SingleEnumerating getIndexDeligate() {
        return indexDeligate;
    }

    public final void setIndexDeligate(SingleEnumerating indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }

    @Override
    protected Source<Weighted<Token>> openSource(File file) throws FileNotFoundException, IOException {
        return BybloIO.openEntriesSource(file, getFileDeligate().getCharset(), indexDeligate);
    }

    @Override
    protected Sink<Weighted<Token>> openSink(File file) throws FileNotFoundException, IOException {
        return new WeightSumReducerSink<Token>(
                BybloIO.openEntriesSink(file, getFileDeligate().getCharset(), indexDeligate));
    }

    public static void main(String[] args) throws Exception {
        new MergeEntriesCommand().runCommand(args);
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().add("indexing", indexDeligate);
    }

}
