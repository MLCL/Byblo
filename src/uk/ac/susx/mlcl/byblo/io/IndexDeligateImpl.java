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
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;
import uk.ac.susx.mlcl.lib.commands.AbstractDeligate;
import uk.ac.susx.mlcl.lib.commands.InputFileValidator;

/**
 *
 * @author hiam20
 */
public class IndexDeligateImpl extends AbstractDeligate implements Serializable, IndexDeligate {

    private static final long serialVersionUID = 1L;

    @Parameter(names = {"-p", "--preindexed"},
    description = "Whether tokens in the input file are enumerated.")
    private boolean preindexedTokens = DEFAULT_ENUMERATED;

    @Parameter(names = {"-x", "--index-file"},
    description = "Index for the string tokens.",
    validateWith = InputFileValidator.class)
    private File indexFile = null;

    @Parameter(names = {"-s1", "--skipindexed1"},
    description = "")
    private boolean skipIndexed1 = DEFAULT_SKIP_INDEXING;

    @Parameter(names = {"-s2", "--skipindexed2"},
    description = "")
    private boolean skipIndexed2 = DEFAULT_SKIP_INDEXING;

    private Enumerator<String> enumerator = null;

    public IndexDeligateImpl(boolean preindexedTokens, File indexFile,
                             Enumerator<String> index,
                             boolean skipIndexed1, boolean skipIndexed2) {
        this.preindexedTokens = preindexedTokens;
        this.indexFile = indexFile;
        this.enumerator = index;
        this.skipIndexed1 = skipIndexed1;
        this.skipIndexed2 = skipIndexed2;
    }

    public IndexDeligateImpl(boolean preindexedTokens, Enumerator<String> index) {
        this.preindexedTokens = preindexedTokens;
        this.enumerator = index;
    }

    public IndexDeligateImpl(boolean preindexed) {
        setPreindexedTokens(preindexed);
    }

    public IndexDeligateImpl() {
    }

    @Override
    public boolean isSkipIndexed1() {
        return skipIndexed1;
    }

    public void setSkipIndexed1(boolean skipIndexed1) {
        this.skipIndexed1 = skipIndexed1;
    }

    @Override
    public boolean isSkipIndexed2() {
        return skipIndexed2;
    }

    public void setSkipIndexed2(boolean skipIndexed2) {
        this.skipIndexed2 = skipIndexed2;
    }

    @Override
    public File getIndexFile() {
        return indexFile;
    }

    public void setIndexFile(File indexFile) {
        Checks.checkNotNull(indexFile);
        this.indexFile = indexFile;
    }

    @Override
    public final Enumerator<String> getEnumerator() throws IOException {
        if (enumerator == null) {
            if (isEnumerated()) {
                if (indexFile != null && indexFile.exists())
                    enumerator = Enumerators.loadStringEnumerator(indexFile);
                else
                    enumerator = Enumerators.nullEnumerator();
            } else {
                if (indexFile != null && indexFile.exists()) {
                    enumerator = Enumerators.loadStringEnumerator(indexFile);
                } else {
                    enumerator = Enumerators.newDefaultStringEnumerator();
                }
            }
        }
        return enumerator;
    }

    public final void setEnumerator(Enumerator<String> enumerator) {
        Checks.checkNotNull("enumerator", enumerator);
        this.enumerator = enumerator;
    }

    @Override
    public final boolean isEnumerated() {
        return preindexedTokens;
    }

    public final void setPreindexedTokens(boolean preindexedTokens) {
        this.preindexedTokens = preindexedTokens;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("preindexed", isEnumerated()).
                add("index", getIndexFile());
    }

}
