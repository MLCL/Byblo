/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.tasks;

import com.google.common.base.Objects;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.io.Files;
import uk.ac.susx.mlcl.lib.tasks.AbstractTask;

/**
 *
 * @author hiam20
 * @deprecated Should be replaced
 */
@Deprecated
public class CopyFileTask extends AbstractTask {

    private static final Log LOG = LogFactory.getLog(CopyFileTask.class);

    private static final int BUFFER_SIZE = 100000;

    private File sourceFile;

    private File destFile;

    public CopyFileTask(File sourceFile, File destinationFile) {
        setSrcFile(sourceFile);
        setDstFile(destinationFile);
    }

    public CopyFileTask() {
    }

    @Override
    protected void initialiseTask() throws Exception {
    }

    @Override
    protected void finaliseTask() throws Exception {
    }

    @Override
    protected void runTask() throws Exception {
        if (LOG.isInfoEnabled())
            LOG.info("Copying file from \"" + getSrcFile() + "\" to \"" + getDstFile() + "\".");
        // Check the configuration state
        // Check the configuration state
        if (sourceFile.equals(destFile) && !Files.isStdin(sourceFile))
            throw new IllegalStateException("sourceFile equals destinationFile");
        InputStream in = null;
        OutputStream out = null;
        try {
            in = Files.openInputStream(getSrcFile());
            out = Files.openOutputStream(getDstFile());
            byte[] b = new byte[BUFFER_SIZE];
            int i = in.read(b);
            while (i != -1) {
                out.write(b, 0, i);
                i = in.read(b);
            }
        } finally {
            if (in != null)
                in.close();
            if (out != null) {
                out.flush();
                out.close();
            }
        }
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
        if (sourceFile == null)
            throw new NullPointerException("sourceFile is null");
        this.sourceFile = sourceFile;
    }

    public final void setDstFile(final File destFile) throws NullPointerException {
        if (destFile == null)
            throw new NullPointerException("destinationFile is null");
        this.destFile = destFile;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().add("in", sourceFile).add("out", destFile);
    }
    
}
