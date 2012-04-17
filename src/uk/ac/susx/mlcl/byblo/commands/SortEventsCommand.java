/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import com.beust.jcommander.ParametersDelegate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratorType;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;

/**
 *
 * @author hiam20
 */
public class SortEventsCommand extends AbstractSortCommand<Weighted<TokenPair>> {

    private static final Log LOG = LogFactory.getLog(
            SortEntriesCommand.class);

    @ParametersDelegate
    private DoubleEnumerating indexDeligate = new DoubleEnumeratingDeligate();

    public SortEventsCommand(
            File sourceFile, File destinationFile, Charset charset,
            DoubleEnumerating indexDeligate) {
        super(sourceFile, destinationFile, charset,
              Weighted.recordOrder(TokenPair.indexOrder()));
        setIndexDeligate(indexDeligate);
    }

    public SortEventsCommand() {
    }

    @Override
    public void runCommand() throws Exception {
        super.runCommand();
        indexDeligate.saveEnumerator();
        indexDeligate.closeEnumerator();

    }

    public final DoubleEnumerating getIndexDeligate() {
        return indexDeligate;
    }

    public final void setIndexDeligate(DoubleEnumerating indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }

    @Override
    protected Source<Weighted<TokenPair>> openSource(File file)
            throws FileNotFoundException, IOException {
        return BybloIO.openEventsSource(file, getCharset(), indexDeligate);
    }

    @Override
    protected Sink<Weighted<TokenPair>> openSink(File file)
            throws FileNotFoundException, IOException {
        return new WeightSumReducerSink<TokenPair>(BybloIO.openEventsSink(file, getCharset(), indexDeligate));
    }

    public EnumeratorType getEnuemratorType() {
        return indexDeligate.getEnuemratorType();
    }

    public void setEnumeratedFeatures(boolean enumeratedFeatures) {
        indexDeligate.setEnumeratedFeatures(enumeratedFeatures);
    }

    public void setEnumeratedEntries(boolean enumeratedEntries) {
        indexDeligate.setEnumeratedEntries(enumeratedEntries);
    }

    public boolean isEnumeratedFeatures() {
        return indexDeligate.isEnumeratedFeatures();
    }

    public boolean isEnumeratedEntries() {
        return indexDeligate.isEnumeratedEntries();
    }

    public void setEnumeratorType(EnumeratorType type) {
        indexDeligate.setEnumeratorType(type);
    }

}
