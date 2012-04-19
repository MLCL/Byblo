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
import uk.ac.susx.mlcl.lib.commands.FileDeligate;
import uk.ac.susx.mlcl.lib.commands.InputFileValidator;
import uk.ac.susx.mlcl.lib.commands.OutputFileValidator;

/**
 *
 * @author hiam20
 */
public class FileMergeDeligate extends FileDeligate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Parameter(names = {"-ifa", "--input-file-a"},
    required = true,
    description = "The first file to merge.",
    validateWith = InputFileValidator.class)
    private File sourceFileA;

    @Parameter(names = {"-ifb", "--input-file-b"},
    required = true,
    description = "The second file to merge.",
    validateWith = InputFileValidator.class)
    private File sourceFileB;

    @Parameter(names = {"-of", "--output-file"},
    required = true,
    description = "The output file to which both input will be merged.",
    validateWith = OutputFileValidator.class)
    private File destinationFile;

    public FileMergeDeligate(File sourceFileA, File sourceFileB, File destination, Charset charset) {
        super(charset);
        setSourceFileA(sourceFileA);
        setSourceFileB(sourceFileB);
        setDestinationFile(destination);
    }

    public FileMergeDeligate() {
    }

    public File getSourceFileA() {
        return sourceFileA;
    }

    public File getSourceFileB() {
        return sourceFileB;
    }

    public File getDestinationFile() {
        return destinationFile;
    }

    public final void setSourceFileB(File sourceFileB) {
        if (sourceFileB == null)
            throw new NullPointerException("sourceFileB = null");
        if (sourceFileB == sourceFileA)
            throw new IllegalArgumentException("sourceFileB == sourceFileA");
        if (destinationFile == sourceFileB)
            throw new IllegalArgumentException("destination == sourceFileB");
        this.sourceFileB = sourceFileB;
    }

    public final void setSourceFileA(File sourceFileA) {
        if (sourceFileA == null)
            throw new NullPointerException("sourceFileA = null");
        if (sourceFileA == sourceFileB)
            throw new IllegalArgumentException("sourceFileA == sourceFileB");
        if (destinationFile == sourceFileA)
            throw new IllegalArgumentException("destination == sourceFileA");
        this.sourceFileA = sourceFileA;
    }

    public final void setDestinationFile(File destination) {
        if (destination == null)
            throw new NullPointerException("destination = null");
        if (destination == sourceFileB)
            throw new IllegalArgumentException("destination == sourceFileB");
        if (destination == sourceFileA)
            throw new IllegalArgumentException("destination == sourceFileA");
        this.destinationFile = destination;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("in1", getSourceFileA()).
                add("in2", getSourceFileB()).
                add("out", getDestinationFile());
    }

}
