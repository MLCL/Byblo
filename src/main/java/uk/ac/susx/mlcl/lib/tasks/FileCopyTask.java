/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.tasks;

import com.google.common.base.Objects;
import java.io.*;
import java.nio.channels.FileChannel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static java.text.MessageFormat.format;
import uk.ac.susx.mlcl.lib.Checks;

/**
 *
 * @author hiam20
 */
public class FileCopyTask extends AbstractTask {

    private static final Log LOG = LogFactory.getLog(FileCopyTask.class);

    private File sourceFile;

    private File destFile;

    public FileCopyTask(File sourceFile, File destinationFile) {
        setSrcFile(sourceFile);
        setDstFile(destinationFile);
    }

    public FileCopyTask() {
    }

    protected static void copy(File from, File to) throws IOException {

        if (!to.exists()) {
            to.createNewFile();
        }
        FileChannel source = null;
        FileChannel destination = null;
        try {

            source = new FileInputStream(from).getChannel();
            destination = new FileOutputStream(to).getChannel();

            long remaining = source.size();
            while (remaining > 0)
                remaining -= destination.transferFrom(source, 0, remaining);

        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    @Override
    protected void runTask() throws Exception {
        LOG.info(format("Copying file from \"{0}\" to \"{1}\".",
                        getSrcFile(), getDstFile()));

        // Check the configuration state
        if (sourceFile.equals(destFile))
            throw new IllegalStateException("sourceFile equals destinationFile");

        copy(sourceFile, destFile);

        if (LOG.isInfoEnabled())
            LOG.info("Completed copy.");
    }

    public final File getSrcFile() {
        return sourceFile;
    }

    public final File getDstFile() {
        return destFile;
    }

    public final void setSrcFile(final File sourceFile) throws NullPointerException {
        Checks.checkNotNull("sourceFile", sourceFile);
        this.sourceFile = sourceFile;
    }

    public final void setDstFile(final File destFile) throws NullPointerException {
        Checks.checkNotNull("destFile", destFile);
        this.destFile = destFile;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("in", sourceFile).
                add("out", destFile);
    }

}
