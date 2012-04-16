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
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratorType;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.*;

/**
 *
 * @author hiam20
 */
public class ExternalSortWeightedTokenPiarCommand extends AbstractExternalSortCommand<Weighted<TokenPair>> {

    @ParametersDelegate
    private DoubleEnumerating indexDeligate = new DoubleEnumeratingDeligate();

    public ExternalSortWeightedTokenPiarCommand(
            File sourceFile, File destinationFile, Charset charset,
            DoubleEnumerating indexDeligate) {
        super(sourceFile, destinationFile, charset);
        setIndexDeligate(indexDeligate);
    }

    public ExternalSortWeightedTokenPiarCommand() {
    }

    @Override
    public void runCommand() throws Exception {
        super.runCommand();
        indexDeligate.saveEnumerator();
        indexDeligate.closeEnumerator();

    }
    
    
    @Override
    protected Sink<Weighted<TokenPair>> openSink(File file) throws IOException {
        WeightedTokenPairSink s = WeightedTokenPairSink.open(
                file, getFileDeligate().getCharset(),
                getIndexDeligate(),
                !getFileDeligate().isCompactFormatDisabled());
        return new WeightSumReducerSink<TokenPair>(s);
    }

    @Override
    protected WeightedTokenPairSource openSource(File file) throws IOException {
        return WeightedTokenPairSource.open(
                file, getFileDeligate().getCharset(),
                getIndexDeligate());
    }

    public final DoubleEnumerating getIndexDeligate() {
        return indexDeligate;
    }

    public final void setIndexDeligate(DoubleEnumerating indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }

    public void setEnumeratorType(EnumeratorType type) {
        indexDeligate.setEnumeratorType(type);
    }

    public void setEnumeratorSkipIndexed2(boolean b) {
        indexDeligate.setEnumeratorSkipIndexed2(b);
    }

    public void setEnumeratorSkipIndexed1(boolean b) {
        indexDeligate.setEnumeratorSkipIndexed1(b);
    }

    public boolean isEnumeratorSkipIndexed2() {
        return indexDeligate.isEnumeratorSkipIndexed2();
    }

    public boolean isEnumeratorSkipIndexed1() {
        return indexDeligate.isEnumeratorSkipIndexed1();
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
    
    
}
