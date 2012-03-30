/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.io.IndexDeligatePair;
import com.beust.jcommander.Parameter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;
import uk.ac.susx.mlcl.lib.commands.InputFileValidator;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;
import uk.ac.susx.mlcl.lib.io.TSVSink;
import uk.ac.susx.mlcl.lib.io.TSVSource;

/**
 *
 * @author hiam20
 */
public class UnindexSimsCommand extends AbstractCopyCommand<Weighted<TokenPair>> {

    private static final Log LOG = LogFactory.getLog(UnindexSimsCommand.class);

    @Parameter(names = {"-x", "--index-file"},
    required = true,
    description = "Index for the first token type.",
    validateWith = InputFileValidator.class)
    private File indexFile = null;

    private Enumerator<String> index = null;

    public UnindexSimsCommand(
            File sourceFile, File destinationFile, Charset charset, File indexFile) {
        super(sourceFile, destinationFile, charset);
        setIndexFile(indexFile);
    }
//
//    public UnindexSimsCommand(File sourceFile, File destinationFile) {
//        super(sourceFile, destinationFile);
//    }

    public UnindexSimsCommand() {
        super();
    }

    @Override
    public void runCommand() throws Exception {
        Checks.checkNotNull("indexFile", indexFile);
        index = Enumerators.loadStringEnumerator(indexFile);
        Checks.checkNotNull("index", index);
        super.runCommand();
    }

    @Override
    protected Source<Weighted<TokenPair>> openSource(File file)
            throws FileNotFoundException, IOException {
        return new WeightedTokenPairSource(
                new TSVSource(file, getFilesDeligate().getCharset()),
                new IndexDeligatePair(true, true) //                Token.enumeratedDecoder(), Token.enumeratedDecoder()
                );
    }

    @Override
    protected Sink<Weighted<TokenPair>> openSink(File file)
            throws FileNotFoundException, IOException {
        return new WeightedTokenPairSink(
                new TSVSink(file, getFilesDeligate().getCharset()),
                new IndexDeligatePair(false, false, index, index) //                Token.stringEncoder(index), Token.stringEncoder(index)
                );
    }

    public final File getIndexFile() {
        return indexFile;
    }

    public final void setIndexFile(File indexFile) {
        Checks.checkNotNull("indexFile", indexFile);
        this.indexFile = indexFile;
    }

}
