/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.commands;

import com.beust.jcommander.Parameter;
import com.google.common.base.Objects;
import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;

/**
 *
 * @author hiam20
 */
public class FilePipeDelegate extends FileDeligate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Parameter(names = {"-i", "--input"}, description = "Source file that will be read", validateWith = InputFileValidator.class, required = true)
    private File sourceFile;

    @Parameter(names = {"-o", "--output"}, description = "Destination file that will be writen to.", validateWith = OutputFileValidator.class, required = true)
    private File destFile;

    public FilePipeDelegate(File sourceFile, File destinationFile, Charset charset) {
        super(charset);
        setSourceFile(sourceFile);
        setDestinationFile(destinationFile);
    }

    public FilePipeDelegate() {
    }

    public final File getSourceFile() {
        return sourceFile;
    }

    public final File getDestinationFile() {
        return destFile;
    }

    public final void setSourceFile(final File sourceFile) {
        if (sourceFile == null)
            throw new NullPointerException("sourceFile is null");
        this.sourceFile = sourceFile;
    }

    public final void setDestinationFile(final File destFile) {
        if (destFile == null)
            throw new NullPointerException("destinationFile is null");
        this.destFile = destFile;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("in", getSourceFile()).
                add("out", getDestinationFile());
    }

}
