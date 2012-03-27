/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import com.beust.jcommander.Parameter;
import com.google.common.base.Objects;
import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.Files;
import uk.ac.susx.mlcl.lib.tasks.AbstractDeligate;
import uk.ac.susx.mlcl.lib.tasks.InputFileValidator;
import uk.ac.susx.mlcl.lib.tasks.OutputFileValidator;

/**
 *
 * @author hiam20
 */
public class FilePipeDeligate extends AbstractDeligate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Parameter(names = {"-i", "--input"}, description = "Source file that will be read", validateWith = InputFileValidator.class, required = true)
    private File sourceFile;

    @Parameter(names = {"-o", "--output"}, description = "Destination file that will be writen to.", validateWith = OutputFileValidator.class, required = true)
    private File destFile;

    @Parameter(names = {"-c", "--charset"},
    description = "The character set encoding to use for both reading input and writing output files.")
    private Charset charset = Files.DEFAULT_CHARSET;

    public FilePipeDeligate(File sourceFile, File destinationFile, Charset charset) {
        setSourceFile(sourceFile);
        setDestinationFile(destinationFile);
        setCharset(charset);
    }

    public FilePipeDeligate() {
    }

    public final Charset getCharset() {
        return charset;
    }

    public final void setCharset(Charset charset) {
        Checks.checkNotNull(charset);
        this.charset = charset;
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

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("in", getSourceFile()).
                add("out", getDestinationFile()).
                add("charset", getCharset());
    }

}
