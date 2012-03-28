/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import com.beust.jcommander.Parameter;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import uk.ac.susx.mlcl.byblo.io.Token;
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
    private boolean preindexedTokens1 = false;

    @Parameter(names = {"-p2", "--preindexed2"},
    description = "Whether entries in the second column of the input file are indexed.")
    private boolean preindexedTokens2 = false;

    @Parameter(names = {"-x1", "--index-file-1"},
    description = "Index for the first token type.",
    validateWith = InputFileValidator.class)
    private File indexFile1 = null;

    @Parameter(names = {"-x2", "--index-file-2"},
    description = "Index for the second token type.",
    validateWith = InputFileValidator.class)
    private File indexFile2 = null;

    private Enumerator<String> index1 = null;

    private Enumerator<String> index2 = null;

    public IndexDeligatePair(boolean preindexedTokens1, boolean preindexedTokens2) {
        setPreindexedTokens1(preindexedTokens1);
        setPreindexedTokens2(preindexedTokens2);
    }

    public IndexDeligatePair() {
    }

    public final Enumerator<String> getIndex1() throws IOException {
        if (index1 == null) {
            // if tokens are preindexed then a file MUST be available
            // otherwise the file will be loaded if it exists
            if (isPreindexedTokens1() || (indexFile1 != null && indexFile1.exists())) {
                index1 = Enumerators.loadStringEnumerator(indexFile1);
            } else {
                index1 = Enumerators.newDefaultStringEnumerator();
            }
        }
        return index1;
    }

    public final void setIndex1(Enumerator<String> entryIndex) {
        this.index1 = entryIndex;
    }

    public final Enumerator<String> getIndex2() throws IOException {
        if (index2 == null) {
            // if tokens are preindexed then a file MUST be available
            // otherwise the file will be loaded if it exists
            if (isPreindexedTokens2() || (indexFile2 != null && indexFile2.exists())) {
                index2 = Enumerators.loadStringEnumerator(indexFile2);
            } else {
                index2 = Enumerators.newDefaultStringEnumerator();
            }
        }
        return index2;
    }

    public final void setIndex2(Enumerator<String> featureIndex) {
        this.index2 = featureIndex;
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

    public final Function<String, Integer> getDecoder1() throws IOException {
        return isPreindexedTokens1() ? Token.enumeratedDecoder() : Token.stringDecoder(getIndex1());
    }

    public final Function<Integer, String> getEncoder1() throws IOException {
        return isPreindexedTokens1() ? Token.enumeratedEncoder() : Token.stringEncoder(getIndex1());
    }

    public final Function<String, Integer> getDecoder2() throws IOException {
        return isPreindexedTokens2() ? Token.enumeratedDecoder() : Token.stringDecoder(getIndex2());
    }

    public final Function<Integer, String> getEncoder2() throws IOException {
        return isPreindexedTokens2() ? Token.enumeratedEncoder() : Token.stringEncoder(getIndex2());
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
