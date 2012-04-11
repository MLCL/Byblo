/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.io.EnumeratorPairBaringDeligate;
import com.beust.jcommander.ParametersDelegate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.TokenPairSink;
import uk.ac.susx.mlcl.byblo.io.TokenPairSource;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;

/**
 *
 * @author hiam20
 */
public class SortTokenPairCommand extends AbstractSortCommand<TokenPair> {

    private static final Log LOG = LogFactory.getLog(SortWeightedTokenCommand.class);

    @ParametersDelegate
    private EnumeratorPairBaringDeligate indexDeligate = new EnumeratorPairBaringDeligate();

    public SortTokenPairCommand(File sourceFile, File destinationFile, Charset charset,
                                EnumeratorPairBaringDeligate indexDeligate) {
        super(sourceFile, destinationFile, charset, TokenPair.indexOrder());
        setIndexDeligate(indexDeligate);
    }

    public SortTokenPairCommand() {
    }

    @Override
    public void runCommand() throws Exception {
        super.runCommand();
        indexDeligate.save();
        indexDeligate.close();

    }
    
    
    @Override
    protected Source<TokenPair> openSource(File file) throws FileNotFoundException, IOException {
        return TokenPairSource.open(file, getFilesDeligate().getCharset(),
                                    indexDeligate);
    }

    @Override
    protected Sink<TokenPair> openSink(File file) throws FileNotFoundException, IOException {
        return TokenPairSink.open(
                file, getFilesDeligate().getCharset(), indexDeligate,
                !getFilesDeligate().isCompactFormatDisabled());
    }

    public final EnumeratorPairBaringDeligate getIndexDeligate() {
        return indexDeligate;
    }

    public final void setIndexDeligate(EnumeratorPairBaringDeligate indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }

}
