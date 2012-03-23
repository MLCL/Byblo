/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo;

import com.beust.jcommander.Parameter;
import com.google.common.base.Function;
import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.Token;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.TokenPairSink;
import uk.ac.susx.mlcl.byblo.io.TokenPairSource;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;
import uk.ac.susx.mlcl.lib.io.*;
import uk.ac.susx.mlcl.lib.tasks.*;

/**
 *
 * @author hiam20
 */
public class IndexEventsTask extends AbstractCommandTask {

    private static final Log LOG = LogFactory.getLog(IndexEventsTask.class);

    @Parameter(names = {"-i", "--input"}, required = true,
    description = "Source events file",
    validateWith = InputFileValidator.class)
    private File inputEventsFile;

    @Parameter(names = {"-o", "--output"}, required = true,
    description = "Destination events file.",
    validateWith = OutputFileValidator.class)
    private File outputEventsFile = null;

    @Parameter(names = {"-ei", "--entry-index"}, required = true,
    description = "Entry index destination file",
    validateWith = OutputFileValidator.class)
    private File entryIndexFile = null;

    @Parameter(names = {"-fi", "--feature-index"}, required = true,
    description = "Feature frequencies destination file.",
    validateWith = OutputFileValidator.class)
    private File featureIndexFile = null;

    @Parameter(names = {"-c", "--charset"}, description = "Character encoding to use for input and output.")
    private Charset charset = Files.DEFAULT_CHARSET;

    public IndexEventsTask(File srcFile, Charset charset, File dstFile, File indexFile1, File indexFile2) {
        this.inputEventsFile = srcFile;
        this.charset = charset;
        this.outputEventsFile = dstFile;
        this.entryIndexFile = indexFile1;
        this.featureIndexFile = indexFile2;
    }

    public IndexEventsTask() {
    }

    @Override
    protected void initialiseTask() throws Exception {
    }

    @Override
    protected void finaliseTask() throws Exception {
    }

    @Override
    protected void runTask() throws Exception {

        final Enumerator<String> token1Index =
                Enumerators.newSimpleStringEnumerator();

        final Enumerator<String> token2Index =
                Enumerators.newSimpleStringEnumerator();

        final Function<String, Integer> srcToken1Decoder =
                Token.stringDecoder(token1Index);

        final Function<String, Integer> srcToken2Decoder =
                Token.stringDecoder(token2Index);

        final Function<Integer, String> dstToken1Encoder =
                Token.enumeratedEncoder();

        final Function<Integer, String> dstToken2Encoder =
                Token.enumeratedEncoder();

        Source<TokenPair> src = null;

        try {
            src = new TokenPairSource(
                    new TSVSource(inputEventsFile, charset),
                    srcToken1Decoder, srcToken2Decoder);

            TokenPairSink snk = null;

            try {
                snk = new TokenPairSink(
                        new TSVSink(outputEventsFile, charset),
                        dstToken1Encoder, dstToken2Encoder);
                snk.setCompactFormatEnabled(true);

                while (src.hasNext())
                    snk.write(src.read());


                Enumerators.saveStringEnumerator(token1Index, entryIndexFile);
                Enumerators.saveStringEnumerator(token2Index, featureIndexFile);


            } finally {
                try {
                    if (snk != null && snk instanceof Flushable)
                        ((Flushable) snk).flush();
                } finally {
                    if (snk != null && snk instanceof Closeable)
                        ((Closeable) snk).close();
                }
            }

        } finally {
            if (src != null && src instanceof Closeable)
                ((Closeable) src).close();
        }
    }

    public static void main(String[] args) throws Exception {
        new IndexEventsTask().runCommand(args);
    }

}
