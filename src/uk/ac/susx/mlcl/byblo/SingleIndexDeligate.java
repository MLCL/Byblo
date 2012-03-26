/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo;

import com.beust.jcommander.Parameter;
import com.google.common.base.Function;
import uk.ac.susx.mlcl.byblo.io.Token;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;

/**
 *
 * @author hiam20
 */
public class SingleIndexDeligate {

    @Parameter(names = {"-p", "--preindexed"}, description = "Whether tokens in the input events file are indexed.")
    private boolean preindexedTokens = false;

    private Enumerator<String> index = null;

    public SingleIndexDeligate(boolean preindexed) {
        setPreindexedTokens(preindexed);
    }

    public SingleIndexDeligate() {
    }

    public Enumerator<String> getIndex() {
        if (index == null)
            index = Enumerators.newDefaultStringEnumerator();
        return index;
    }

    public void setIndex(Enumerator<String> entryIndex) {
        this.index = entryIndex;
    }

    public final boolean isPreindexedTokens() {
        return preindexedTokens;
    }

    public final void setPreindexedTokens(boolean preindexedTokens) {
        this.preindexedTokens = preindexedTokens;
    }

    protected Function<String, Integer> getDecoder() {
        return isPreindexedTokens() ? Token.enumeratedDecoder() : Token.stringDecoder(getIndex());
    }

    protected Function<Integer, String> getEncoder() {
        return isPreindexedTokens() ? Token.enumeratedEncoder() : Token.stringEncoder(getIndex());
    }
    
}
