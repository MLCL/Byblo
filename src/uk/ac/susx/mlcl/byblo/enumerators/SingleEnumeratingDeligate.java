/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.enumerators;

import com.beust.jcommander.Parameter;
import com.google.common.base.Objects;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import uk.ac.susx.mlcl.lib.commands.InputFileValidator;

/**
 *
 * @author hiam20
 */
public class SingleEnumeratingDeligate
        extends EnumeratingDeligate
        implements Serializable, SingleEnumerating {

    private static final long serialVersionUID = 1L;

    @Parameter(names = {"-E", "--enumerated"},
    description = "Whether tokens in the input file are enumerated.")
    private boolean enumerated = DEFAULT_IS_ENUMERATED;

    @Parameter(names = {"-X", "--index-file"},
    description = "Index for the string tokens.",
    validateWith = InputFileValidator.class)
    private File indexFile = null;

    private Enumerator<String> enumerator = null;

    public SingleEnumeratingDeligate(
            boolean enumerated, File indexFile, Enumerator<String> enumerator,
            boolean skipIndexed1, boolean skipIndexed2) {
        super(skipIndexed1, skipIndexed2);
        this.enumerated = enumerated;
        this.indexFile = indexFile;
        this.enumerator = enumerator;
    }

    public SingleEnumeratingDeligate(
            boolean enumerated, File indexFile,
            boolean skipIndexed1, boolean skipIndexed2) {
        super(skipIndexed1, skipIndexed2);
        this.enumerated = enumerated;
        this.indexFile = indexFile;
        this.enumerator = null;
    }

    public SingleEnumeratingDeligate(boolean enumerated, Enumerator<String> enumerator) {
        this(enumerated, null, enumerator,
             DEFAULT_SKIP_INDEXING, DEFAULT_SKIP_INDEXING);
    }

    public SingleEnumeratingDeligate(boolean enumerated) {
        this(enumerated, null, null,
             DEFAULT_SKIP_INDEXING, DEFAULT_SKIP_INDEXING);
    }

    public SingleEnumeratingDeligate() {
        this(DEFAULT_IS_ENUMERATED, null, null,
             DEFAULT_SKIP_INDEXING, DEFAULT_SKIP_INDEXING);
    }

    @Override
    public File getEnumeratorFile() {
        return indexFile;
    }

    @Override
    public final boolean isEnumerationEnabled() {
        return enumerated;
    }

    @Override
    public final Enumerator<String> getEnumerator() throws IOException {
        if (enumerator == null) {
            openEnumerator();
        }
        return enumerator;
    }

    @Override
    public void openEnumerator() throws IOException {
        enumerator = open(indexFile);
    }

    @Override
    public void saveEnumerator() throws IOException {
        save(enumerator);
    }

    @Override
    public void closeEnumerator() throws IOException {
        close(enumerator);
        enumerator = null;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("isEnumerated", isEnumerationEnabled()).
                add("indexFile", getEnumeratorFile());
    }

    @Override
    public DoubleEnumerating getEnumeratorPairCarriar() {
        return EnumeratingDeligates.toPair(this);
    }
    
    

}
