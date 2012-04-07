/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects;
import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.IndexDeligatePair;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.TokenPairSink;
import uk.ac.susx.mlcl.byblo.io.TokenPairSource;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;
import uk.ac.susx.mlcl.lib.commands.AbstractCommand;
import uk.ac.susx.mlcl.lib.commands.FilePipeDeligate;
import uk.ac.susx.mlcl.lib.io.Source;
import uk.ac.susx.mlcl.lib.io.TSVSink;
import uk.ac.susx.mlcl.lib.io.TSVSource;

/**
 *
 * @author hiam20
 */
public class IndexEventsCommand extends AbstractCommand {

    private static final Log LOG = LogFactory.getLog(IndexEventsCommand.class);

    @ParametersDelegate
    private FilePipeDeligate fileDeligate = new FilePipeDeligate();

    @ParametersDelegate
    private IndexDeligatePair indexDeligate = new IndexDeligatePair();

//    
//    @Parameter(names = {"-i", "--input"}, required = true,
//    description = "Source events file",
//    validateWith = InputFileValidator.class)
//    private File inputEventsFile;
//
//    @Parameter(names = {"-o", "--output"}, required = true,
//    description = "Destination events file.",
//    validateWith = OutputFileValidator.class)
//    private File outputEventsFile = null;
//
//    @Parameter(names = {"-ei", "--entry-index"}, required = true,
//    description = "Entry index destination file",
//    validateWith = OutputFileValidator.class)
//    private File entryIndexFile = null;
//
//    @Parameter(names = {"-fi", "--feature-index"}, required = true,
//    description = "Feature frequencies destination file.",
//    validateWith = OutputFileValidator.class)
//    private File featureIndexFile = null;
//
//    @Parameter(names = {"-c", "--charset"},
//    description = "Character encoding to use for input and output.")
//    private Charset charset = Files.DEFAULT_CHARSET;
    public IndexEventsCommand(File inputEventsFile, Charset charset,
                              File outputEventsFile,
                              File entryIndexFile, File featureIndexFile) {
        indexDeligate.setIndexFile1(entryIndexFile);
        indexDeligate.setIndexFile2(featureIndexFile);
        fileDeligate.setSourceFile(inputEventsFile);
        fileDeligate.setDestinationFile(outputEventsFile);
        fileDeligate.setCharset(charset);
    }

    public IndexEventsCommand() {
    }

    public FilePipeDeligate getFileDeligate() {
        return fileDeligate;
    }

    public void setFileDeligate(FilePipeDeligate fileDeligate) {
        Checks.checkNotNull("fileDeligate", fileDeligate);
        this.fileDeligate = fileDeligate;
    }

    public IndexDeligatePair getIndexDeligate() {
        return indexDeligate;
    }

    public void setIndexDeligate(IndexDeligatePair indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }

    @Override
    public void runCommand() throws Exception {
        checkState();

        final Enumerator<String> index1 =
                Enumerators.newDefaultStringEnumerator();

        final Enumerator<String> index2 =
                Enumerators.newDefaultStringEnumerator();


        Source<TokenPair> src = null;

        IndexDeligatePair srcIdx = new IndexDeligatePair(false, false,
                                                         index1, index2);

        IndexDeligatePair dstIdx = new IndexDeligatePair(true, true);
        dstIdx.setSkipindexed1(getIndexDeligate().isSkipindexed1());
        dstIdx.setSkipindexed2(getIndexDeligate().isSkipindexed2());
        try {
            src =  TokenPairSource.open(
                    fileDeligate.getSourceFile(), fileDeligate.getCharset(),
                    srcIdx);

            TokenPairSink snk = null;

            try {
                snk =  TokenPairSink.open(
                        fileDeligate.getDestinationFile(), fileDeligate.getCharset(),
                        dstIdx,
                        true);
//                snk.setCompactFormatEnabled(true);

                while (src.hasNext())
                    snk.write(src.read());

                Enumerators.saveStringEnumerator(index1, indexDeligate.getIndexFile1());
                Enumerators.saveStringEnumerator(index2, indexDeligate.getIndexFile2());


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
        new IndexEventsCommand().runCommand(args);
    }

//    public final Charset getCharset() {
//        return charset;
//    }
//
//    public final void setCharset(Charset charset) {
//        Checks.checkNotNull("charset", charset);
//        this.charset = charset;
//    }
//
//    public final File getEntryIndexFile() {
//        return entryIndexFile;
//    }
//
//    public final void setEntryIndexFile(File entryIndexFile) {
//        Checks.checkNotNull("entryIndexFile", entryIndexFile);
//        this.entryIndexFile = entryIndexFile;
//    }
//
//    public final File getFeatureIndexFile() {
//        return featureIndexFile;
//    }
//
//    public final void setFeatureIndexFile(File featureIndexFile) {
//        Checks.checkNotNull("featureIndexFile", featureIndexFile);
//        this.featureIndexFile = featureIndexFile;
//    }
//
//    public final File getInputEventsFile() {
//        return inputEventsFile;
//    }
//
//    public final void setInputEventsFile(File inputEventsFile) {
//        Checks.checkNotNull("inputEventsFile", inputEventsFile);
//        this.inputEventsFile = inputEventsFile;
//    }
//
//    public final File getOutputEventsFile() {
//        return outputEventsFile;
//    }
//
//    public final void setOutputEventsFile(File outputEventsFile) {
//        Checks.checkNotNull("outputEventsFile", outputEventsFile);
//        this.outputEventsFile = outputEventsFile;
//    }
    private void checkState() throws NullPointerException, IllegalStateException {
//        Checks.checkNotNull("charset", getCharset());
//        Checks.checkNotNull("entryIndexFile", getEntryIndexFile());
//        Checks.checkNotNull("featureIndexFile", getFeatureIndexFile());
//        Checks.checkNotNull("inputEventsFile", getInputEventsFile());
//        Checks.checkNotNull("outputEventsFile", getOutputEventsFile());
//
//        // Check non of the files are the same
//        final File[] files = new File[]{
//            getEntryIndexFile(), getFeatureIndexFile(),
//            getInputEventsFile(), getOutputEventsFile()};
//        for (int i = 0; i < files.length - 1; i++)
//            for (int j = i + 1; j < files.length; j++)
//                if (files[i].equals(files[j]))
//                    throw new IllegalStateException(
//                            "Two parameters points to same file: " + files[i]);
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().add("idx", getIndexDeligate()).add("files", getFileDeligate());
//.
//                add("in", getInputEventsFile()).
//                add("out", getOutputEventsFile()).
//                add("idx1", getEntryIndexFile()).
//                add("idx2", getFeatureIndexFile()).
//                add("charset", getCharset());
    }

}
