/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects.ToStringHelper;
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
public class MergeEventsCommand extends AbstractMergeCommand<Weighted<TokenPair>> {

    @ParametersDelegate
    private DoubleEnumeratingDeligate indexDeligate = new DoubleEnumeratingDeligate();

    public MergeEventsCommand(
            File sourceFileA, File sourceFileB, File destinationFile,
            Charset charset, DoubleEnumeratingDeligate indexDeligate) {
        super(sourceFileA, sourceFileB, destinationFile, charset, Weighted.recordOrder(TokenPair.indexOrder()));
        setIndexDeligate(indexDeligate);
    }

    public MergeEventsCommand() {
    }

    @Override
    public void runCommand() throws Exception {
        super.runCommand();
        indexDeligate.saveEnumerator();
        indexDeligate.closeEnumerator();

    }
    
    
    public final DoubleEnumeratingDeligate getIndexDeligate() {
        return indexDeligate;
    }

    public final void setIndexDeligate(DoubleEnumeratingDeligate indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }

    @Override
    protected Source<Weighted<TokenPair>> openSource(File file) throws FileNotFoundException, IOException {
        return BybloIO.openEventsSource(file, getFileDeligate().getCharset(), indexDeligate);
    }

    @Override
    protected Sink<Weighted<TokenPair>> openSink(File file) throws FileNotFoundException, IOException {
        return new WeightSumReducerObjectSink<TokenPair>(BybloIO.openEventsSink(file, getFileDeligate().getCharset(), indexDeligate));
    }

    public static void main(String[] args) throws Exception {
        new MergeEntriesCommand().runCommand(args);
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper().add("indexing", indexDeligate);
    }

}
