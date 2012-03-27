/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo;

import com.beust.jcommander.Parameter;
import com.google.common.base.Function;
import com.google.common.base.Objects;
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
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;
import uk.ac.susx.mlcl.lib.io.Files;
import uk.ac.susx.mlcl.lib.io.Source;
import uk.ac.susx.mlcl.lib.io.TSVSink;
import uk.ac.susx.mlcl.lib.io.TSVSource;
import uk.ac.susx.mlcl.lib.AbstractCommandTask;
import uk.ac.susx.mlcl.lib.command.InputFileValidator;
import uk.ac.susx.mlcl.lib.command.OutputFileValidator;

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

    @Parameter(names = {"-c", "--charset"},
    description = "Character encoding to use for input and output.")
    private Charset charset = Files.DEFAULT_CHARSET;

    public IndexEventsTask(File inputEventsFile, Charset charset,
                           File outputEventsFile,
                           File entryIndexFile, File featureIndexFile) {
        setCharset(charset);
        setEntryIndexFile(entryIndexFile);
        setFeatureIndexFile(featureIndexFile);
        setInputEventsFile(inputEventsFile);
        setOutputEventsFile(outputEventsFile);
    }

    public IndexEventsTask() {
    }

    @Override
    protected void initialiseTask() throws Exception {
        checkState();
    }

    @Override
    protected void finaliseTask() throws Exception {
    }

    @Override
    protected void runTask() throws Exception {

        final Enumerator<String> token1Index =
                Enumerators.newDefaultStringEnumerator();

        final Enumerator<String> token2Index =
                Enumerators.newDefaultStringEnumerator();

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

    public final Charset getCharset() {
        return charset;
    }

    public final void setCharset(Charset charset) {
        Checks.checkNotNull("charset", charset);
        this.charset = charset;
    }

    public final File getEntryIndexFile() {
        return entryIndexFile;
    }

    public final void setEntryIndexFile(File entryIndexFile) {
        Checks.checkNotNull("entryIndexFile", entryIndexFile);
        this.entryIndexFile = entryIndexFile;
    }

    public final File getFeatureIndexFile() {
        return featureIndexFile;
    }

    public final void setFeatureIndexFile(File featureIndexFile) {
        Checks.checkNotNull("featureIndexFile", featureIndexFile);
        this.featureIndexFile = featureIndexFile;
    }

    public final File getInputEventsFile() {
        return inputEventsFile;
    }

    public final void setInputEventsFile(File inputEventsFile) {
        Checks.checkNotNull("inputEventsFile", inputEventsFile);
        this.inputEventsFile = inputEventsFile;
    }

    public final File getOutputEventsFile() {
        return outputEventsFile;
    }

    public final void setOutputEventsFile(File outputEventsFile) {
        Checks.checkNotNull("outputEventsFile", outputEventsFile);
        this.outputEventsFile = outputEventsFile;
    }

    private void checkState() throws NullPointerException, IllegalStateException {
        Checks.checkNotNull("charset", getCharset());
        Checks.checkNotNull("entryIndexFile", getEntryIndexFile());
        Checks.checkNotNull("featureIndexFile", getFeatureIndexFile());
        Checks.checkNotNull("inputEventsFile", getInputEventsFile());
        Checks.checkNotNull("outputEventsFile", getOutputEventsFile());

        // Check non of the files are the same
        final File[] files = new File[]{
            getEntryIndexFile(), getFeatureIndexFile(),
            getInputEventsFile(), getOutputEventsFile()};
        for (int i = 0; i < files.length - 1; i++)
            for (int j = i + 1; j < files.length; j++)
                if (files[i].equals(files[j]))
                    throw new IllegalStateException(
                            "Two parameters points to same file: " + files[i]);

    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("in", getInputEventsFile()).
                add("out", getOutputEventsFile()).
                add("idx1", getEntryIndexFile()).
                add("idx2", getFeatureIndexFile()).
                add("charset", getCharset());
    }

}
