/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.io;

import com.beust.jcommander.Parameter;
import com.google.common.base.Objects;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.commands.InputFileValidator;

/**
 *
 * @author hiam20
 */
public class EnumeratorSingleBaringDeligate
        extends EnumeratorBaringDeligate
        implements Serializable, EnumeratorSingleBaring {

    private static final long serialVersionUID = 1L;

    @Parameter(names = {"-E", "--enumerated"},
    description = "Whether tokens in the input file are enumerated.")
    private boolean enumerated = DEFAULT_IS_ENUMERATED;

    @Parameter(names = {"-X", "--index-file"},
    description = "Index for the string tokens.",
    validateWith = InputFileValidator.class)
    private File indexFile = null;

    private Enumerator<String> enumerator = null;

    public EnumeratorSingleBaringDeligate(
            boolean enumerated, File indexFile, Enumerator<String> enumerator,
            boolean skipIndexed1, boolean skipIndexed2) {
        super(skipIndexed1, skipIndexed2);
        this.enumerated = enumerated;
        this.indexFile = indexFile;
        this.enumerator = enumerator;
    }

    public EnumeratorSingleBaringDeligate(
            boolean enumerated, File indexFile,
            boolean skipIndexed1, boolean skipIndexed2) {
        super(skipIndexed1, skipIndexed2);
        this.enumerated = enumerated;
        this.indexFile = indexFile;
        this.enumerator = null;
    }

    public EnumeratorSingleBaringDeligate(boolean enumerated, Enumerator<String> enumerator) {
        this(enumerated, null, enumerator,
             DEFAULT_SKIP_INDEXING, DEFAULT_SKIP_INDEXING);
    }

    public EnumeratorSingleBaringDeligate(boolean enumerated) {
        this(enumerated, null, null,
             DEFAULT_SKIP_INDEXING, DEFAULT_SKIP_INDEXING);
    }

    public EnumeratorSingleBaringDeligate() {
        this(DEFAULT_IS_ENUMERATED, null, null,
             DEFAULT_SKIP_INDEXING, DEFAULT_SKIP_INDEXING);
    }

    @Override
    public File getIndexFile() {
        return indexFile;
    }

    @Override
    public final boolean isEnumerated() {
        return enumerated;
    }

    @Override
    public final Enumerator<String> getEnumerator() throws IOException {
        if (enumerator == null) {
            open();
        }
        return enumerator;
    }

    @Override
    public void open() throws IOException {
        enumerator = open(indexFile);
    }

    @Override
    public void save() throws IOException {
        save(enumerator);
    }

    @Override
    public void close() throws IOException {
        close(enumerator);
        enumerator = null;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("isEnumerated", isEnumerated()).
                add("indexFile", getIndexFile());
    }

    @Override
    public EnumeratorPairBaring getEnumeratorPairCarriar() {
        return EnumeratorDeligates.toPair(this);
    }
    
    

}
