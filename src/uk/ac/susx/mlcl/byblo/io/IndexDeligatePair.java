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
public class IndexDeligatePair extends AbstractDeligate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Parameter(names = {"-p1", "--preindexed1"},
    description = "Whether tokens in the first column of the input file are indexed.")
    private boolean preindexedTokens1 = IndexDeligate.DEFAULT_INDEXING;

    @Parameter(names = {"-p2", "--preindexed2"},
    description = "Whether entries in the second column of the input file are indexed.")
    private boolean preindexedTokens2 = IndexDeligate.DEFAULT_INDEXING;

    @Parameter(names = {"-x1", "--index-file-1"},
    description = "Index for the first token type.",
    validateWith = InputFileValidator.class)
    private File indexFile1 = null;

    @Parameter(names = {"-x2", "--index-file-2"},
    description = "Index for the second token type.",
    validateWith = InputFileValidator.class)
    private File indexFile2 = null;

    @Parameter(names = {"-s1", "--skipindexed1"},
    description = "")
    private boolean skipindexed1 = IndexDeligate.DEFAULT_SKIP_INDEXING;

    @Parameter(names = {"-s2", "--skipindexed2"},
    description = "")
    private boolean skipindexed2 = IndexDeligate.DEFAULT_SKIP_INDEXING;

    private Enumerator<String> enumerator1 = null;

    private Enumerator<String> enumerator2 = null;

    public IndexDeligatePair(boolean preindexedTokens1, boolean preindexedTokens2) {
        setPreindexedTokens1(preindexedTokens1);
        setPreindexedTokens2(preindexedTokens2);
    }

    public IndexDeligatePair(boolean preindexedTokens1, boolean preindexedTokens2,
                             Enumerator<String> index1, Enumerator<String> index2) {
        setPreindexedTokens1(preindexedTokens1);
        setPreindexedTokens2(preindexedTokens2);
        setEnumerator1(index1);
        setEnumerator2(index2);
    }

    public IndexDeligatePair(boolean preindexedTokens1, boolean preindexedTokens2,
                             Enumerator<String> index1, Enumerator<String> index2,
                             boolean skipindexed1, boolean skipindexed2) {
        setPreindexedTokens1(preindexedTokens1);
        setPreindexedTokens2(preindexedTokens2);
        setEnumerator1(index1);
        setEnumerator2(index2);
        setSkipindexed1(skipindexed1);
        setSkipindexed2(skipindexed2);
    }

    public IndexDeligatePair() {
    }

    public final boolean isSkipindexed1() {
        return skipindexed1;
    }

    public final void setSkipindexed1(boolean skipindexed1) {
        this.skipindexed1 = skipindexed1;
    }

    public final boolean isSkipindexed2() {
        return skipindexed2;
    }

    public final void setSkipindexed2(boolean skipindexed2) {
        this.skipindexed2 = skipindexed2;
    }

    public IndexDeligate single1() throws IOException {
        return new IndexDeligate(isPreindexedTokens1(), getIndexFile1(), getEnumerator1(), isSkipindexed1());
    }

    public IndexDeligate single2() throws IOException {
        return new IndexDeligate(isPreindexedTokens2(), getIndexFile2(), getEnumerator2(), isSkipindexed2());
    }

    public final Enumerator<String> getEnumerator1() throws IOException {
        if (enumerator1 == null) {
            // if tokens are preindexed then a file MUST be available
            // otherwise the file will be loaded if it exists
            if (isPreindexedTokens1()) {
                if (indexFile1 != null && indexFile1.exists())
                    enumerator1 = Enumerators.loadStringEnumerator(indexFile1);
                else
                    enumerator1 = Enumerators.nullEnumerator();
            } else {
                if (indexFile1 != null && indexFile1.exists()) {
                    enumerator1 = Enumerators.loadStringEnumerator(indexFile1);
                } else {
                    enumerator1 = Enumerators.newDefaultStringEnumerator();
                }
            }
        }
        return enumerator1;
    }

    public final void setEnumerator1(Enumerator<String> enumerator1) {
        Checks.checkNotNull("enumerator1", enumerator1);
        this.enumerator1 = enumerator1;
    }

    public final Enumerator<String> getEnumerator2() throws IOException {
        if (enumerator2 == null) {
            if (isPreindexedTokens2()) {
                if (indexFile2 != null && indexFile2.exists())
                    enumerator2 = Enumerators.loadStringEnumerator(indexFile2);
                else
                    enumerator2 = Enumerators.nullEnumerator();
            } else {
                if (indexFile2 != null && indexFile2.exists()) {
                    enumerator2 = Enumerators.loadStringEnumerator(indexFile2);
                } else {
                    enumerator2 = Enumerators.newDefaultStringEnumerator();
                }
            }
        }
        return enumerator2;
    }

    public final void setEnumerator2(Enumerator<String> enumerator2) {
        Checks.checkNotNull("enumerator2", enumerator2);
        this.enumerator2 = enumerator2;
    }

    public final boolean isPreindexedTokens1() {
        return preindexedTokens1;
    }

    public final void setPreindexedTokens1(boolean preindexedTokens1) {
        this.preindexedTokens1 = preindexedTokens1;
    }

    public final boolean isPreindexedTokens2() {
        return preindexedTokens2;
    }

    public final void setPreindexedTokens2(boolean preindexedTokens2) {
        this.preindexedTokens2 = preindexedTokens2;
    }

    public File getIndexFile1() {
        return indexFile1;
    }

    public void setIndexFile1(File indexFile1) {
        this.indexFile1 = indexFile1;
    }

    public File getIndexFile2() {
        return indexFile2;
    }

    public void setIndexFile2(File indexFile2) {
        this.indexFile2 = indexFile2;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("preindexed1", isPreindexedTokens1()).
                add("preindexed2", isPreindexedTokens2()).
                add("index1", getIndexFile1()).
                add("index2", getIndexFile2());
    }

}
