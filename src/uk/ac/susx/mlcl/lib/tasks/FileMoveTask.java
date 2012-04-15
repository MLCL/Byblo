/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.tasks;

import java.io.File;
import java.io.IOException;
import static java.text.MessageFormat.format;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author hiam20
 */
public class FileMoveTask extends FileCopyTask {

    private static final Log LOG = LogFactory.getLog(FileMoveTask.class);

    public FileMoveTask(File sourceFile, File destinationFile) {
        super(sourceFile, destinationFile);
    }

    public FileMoveTask() {
        super();
    }

    protected static void move(File from, File to) throws IOException {
        if (!from.renameTo(to)) {
            if (LOG.isDebugEnabled())
                LOG.debug("Cannot performan fast rename; falling back to copy");

            copy(from, to);

            if (!from.delete())
                throw new IOException(format("Unable to delete file {0}", from));
        }
    }

    @Override
    protected void runTask() throws Exception {
        if (LOG.isInfoEnabled())
            LOG.info(format("Moving file from \"{0}\" to \"{1}\".",
                            getSrcFile(), getDstFile()));

        // Check the configuration state
        if (getSrcFile().equals(getDstFile()))
            throw new IllegalStateException("sourceFile equals destinationFile");

        copy(getSrcFile(), getDstFile());

        if (LOG.isInfoEnabled())
            LOG.info("Completed move.");
    }
}
