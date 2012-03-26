/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo;

import com.beust.jcommander.Parameter;
import java.io.File;
import uk.ac.susx.mlcl.lib.tasks.InputFileValidator;
import uk.ac.susx.mlcl.lib.tasks.OutputFileValidator;

/**
 *
 * @author hiam20
 */
public class FilePipeDeligate {

    @Parameter(names = {"-i", "--input"}, description = "Source file that will be read", validateWith = InputFileValidator.class, required = true)
    private File sourceFile;

    @Parameter(names = {"-o", "--output"}, description = "Destination file that will be writen to.", validateWith = OutputFileValidator.class, required = true)
    private File destFile;

    public FilePipeDeligate(File sourceFile, File destinationFile) {
        setSourceFile(sourceFile);
        setDestinationFile(destinationFile);
    }

    public FilePipeDeligate() {
    }

    public final File getSourceFile() {
        return sourceFile;
    }

    public final File getDestinationFile() {
        return destFile;
    }

    public final void setSourceFile(final File sourceFile) throws NullPointerException {
        if (sourceFile == null)
            throw new NullPointerException("sourceFile is null");
        this.sourceFile = sourceFile;
    }

    public final void setDestinationFile(final File destFile) throws NullPointerException {
        if (destFile == null)
            throw new NullPointerException("destinationFile is null");
        this.destFile = destFile;
    }
    
}
