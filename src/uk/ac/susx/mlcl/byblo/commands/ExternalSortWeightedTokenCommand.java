/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

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
public class ExternalSortWeightedTokenCommand extends AbstractExternalSortCommand<Weighted<Token>> {

    private static final long serialVersionUID = 1L;

    @ParametersDelegate
    private EnumeratorSingleBaring indexDeligate = new EnumeratorSingleBaringDeligate();

    public ExternalSortWeightedTokenCommand(
            File sourceFile, File destinationFile, Charset charset,
            EnumeratorSingleBaring indexDeligate) {
        super(sourceFile, destinationFile, charset);
        setIndexDeligate(indexDeligate);
    }

    public ExternalSortWeightedTokenCommand() {
    }

    @Override
    public void runCommand() throws Exception {
        super.runCommand();
        indexDeligate.save();
        indexDeligate.close();

    }
    @Override
    protected Sink<Weighted<Token>> openSink(File file) throws IOException {
        WeightedTokenSink s = WeightedTokenSink.open(
                file, getFileDeligate().getCharset(), getIndexDeligate());
        s.setCompactFormatEnabled(!getFileDeligate().isCompactFormatDisabled());
        return new WeightSumReducerSink<Token>(s);
    }

    @Override
    protected SeekableSource<Weighted<Token>, Tell> openSource(File file) throws IOException {
        return WeightedTokenSource.open(file, getFileDeligate().getCharset(),
                                        getIndexDeligate());
    }

    public final EnumeratorSingleBaring getIndexDeligate() {
        return indexDeligate;
    }

    public final void setIndexDeligate(EnumeratorSingleBaring indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }

}
