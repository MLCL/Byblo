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
public class IndexDeligate extends AbstractDeligate implements Serializable {

    private static final long serialVersionUID = 1L;

    static final boolean DEFAULT_INDEXING = false;

    static final boolean DEFAULT_SKIP_INDEXING = false;

    @Parameter(names = {"-p", "--preindexed"},
               description = "Whether tokens in the input events file are indexed.")
    private boolean preindexedTokens = DEFAULT_INDEXING;

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

    public IndexDeligate(boolean preindexedTokens, File indexFile,
                         Enumerator<String> index,
                         boolean skipIndexed1, boolean skipIndexed2) {
        this.preindexedTokens = preindexedTokens;
        this.indexFile = indexFile;
        this.enumerator = index;
        this.skipIndexed1 = skipIndexed1;
        this.skipIndexed2 = skipIndexed2;
    }

    public IndexDeligate(boolean preindexedTokens, Enumerator<String> index) {
        this.preindexedTokens = preindexedTokens;
        this.enumerator = index;
    }

    public IndexDeligate(boolean preindexed) {
        setPreindexedTokens(preindexed);
    }

    public IndexDeligate() {
    }

    public IndexDeligatePair pair() throws IOException {
        return new IndexDeligatePair(
                isPreindexedTokens(), isPreindexedTokens(),
                getEnumerator(), getEnumerator(),
                isSkipIndexed1(), isSkipIndexed2());
    }

    public boolean isSkipIndexed1() {
        return skipIndexed1;
    }

    public void setSkipIndexed1(boolean skipIndexed1) {
        this.skipIndexed1 = skipIndexed1;
    }

    public boolean isSkipIndexed2() {
        return skipIndexed2;
    }

    public void setSkipIndexed2(boolean skipIndexed2) {
        this.skipIndexed2 = skipIndexed2;
    }

    public File getIndexFile() {
        return indexFile;
    }

    public void setIndexFile(File indexFile) {
        Checks.checkNotNull(indexFile);
        this.indexFile = indexFile;
    }

    public final Enumerator<String> getEnumerator() throws IOException {
        if (enumerator == null) {
            if (isPreindexedTokens()) {
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

    public final boolean isPreindexedTokens() {
        return preindexedTokens;
    }

    public final void setPreindexedTokens(boolean preindexedTokens) {
        this.preindexedTokens = preindexedTokens;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("preindexed", isPreindexedTokens()).
                add("index", getIndexFile());
    }
}
