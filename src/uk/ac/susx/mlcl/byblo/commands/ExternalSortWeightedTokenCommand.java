/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.io.IndexDeligate;
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
public class ExternalSortWeightedTokenCommand extends AbstractExternalSortCommand<Weighted<Token>> {

    private static final long serialVersionUID = 1L;

    @ParametersDelegate
    private IndexDeligate indexDeligate = new IndexDeligate();

    public ExternalSortWeightedTokenCommand(
            File sourceFile, File destinationFile, Charset charset,
            IndexDeligate indexDeligate) {
        super(sourceFile, destinationFile, charset);
        setIndexDeligate(indexDeligate);
    }

    public ExternalSortWeightedTokenCommand() {
    }

    @Override
    protected Sink<Weighted<Token>> openSink(File file) throws IOException {
        WeightedTokenSink s = WeightedTokenSink.open(
                file, getFileDeligate().getCharset(), getIndexDeligate());
//                new WeightedTokenSink(
//                new TSVSink(file, getFileDeligate().getCharset()),
//                getIndexDeligate());
        s.setCompactFormatEnabled(!getFileDeligate().isCompactFormatDisabled());
        return new WeightSumReducerSink<Token>(s);
    }

    @Override
    protected SeekableSource<Weighted<Token>, Tell> openSource(File file) throws IOException {
        return WeightedTokenSource.open(file, getFileDeligate().getCharset(),
                                        getIndexDeligate());
//        return new WeightedTokenSource(
//                new TSVSource(file, getFileDeligate().getCharset()),
//                getIndexDeligate());
    }

    public final IndexDeligate getIndexDeligate() {
        return indexDeligate;
    }

    public final void setIndexDeligate(IndexDeligate indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }

}
