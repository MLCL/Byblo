/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
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
public class MergeTokenPairCommand extends AbstractMergeCommand<TokenPair> {

    @ParametersDelegate
    private DoubleEnumeratingDeligate indexDeligate = new DoubleEnumeratingDeligate();

    public MergeTokenPairCommand(
            File sourceFileA, File sourceFileB, File destinationFile,
            Charset charset, DoubleEnumeratingDeligate indexDeligate) {
        super(sourceFileA, sourceFileB, destinationFile, charset, TokenPair.indexOrder());
        setIndexDeligate(indexDeligate);
    }

    public MergeTokenPairCommand() {
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
    protected Source<TokenPair> openSource(File file) throws FileNotFoundException, IOException {
        return TokenPairSource.open(
                file, getFileDeligate().getCharset(),
                indexDeligate);
    }

    @Override
    protected Sink<TokenPair> openSink(File file) throws FileNotFoundException, IOException {
        TokenPairSink s = TokenPairSink.open(
                file, getFileDeligate().getCharset(),
                indexDeligate,
                !getFileDeligate().isCompactFormatDisabled());
//        s.setCompactFormatEnabled(!getFileDeligate().isCompactFormatDisabled());
        return s;
    }

    public static void main(String[] args) throws Exception {
        new MergeTokenPairCommand().runCommand(args);
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().add("indexing", indexDeligate);
    }

}
