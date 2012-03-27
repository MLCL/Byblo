/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.commands;

import com.beust.jcommander.Parameter;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import java.io.Serializable;
import uk.ac.susx.mlcl.byblo.io.Token;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;
import uk.ac.susx.mlcl.lib.tasks.AbstractDeligate;

/**
 *
 * @author hiam20
 */
public class IndexDeligateSingle extends AbstractDeligate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Parameter(names = {"-p", "--preindexed"}, description = "Whether tokens in the input events file are indexed.")
    private boolean preindexedTokens = false;

    private Enumerator<String> index = null;

    public IndexDeligateSingle(boolean preindexed) {
        setPreindexedTokens(preindexed);
    }

    public IndexDeligateSingle() {
    }

    public final Enumerator<String> getIndex() {
        if (index == null)
            index = Enumerators.newDefaultStringEnumerator();
        return index;
    }

    public final void setIndex(Enumerator<String> entryIndex) {
        this.index = entryIndex;
    }

    public final boolean isPreindexedTokens() {
        return preindexedTokens;
    }

    public final void setPreindexedTokens(boolean preindexedTokens) {
        this.preindexedTokens = preindexedTokens;
    }

    public final Function<String, Integer> getDecoder() {
        return isPreindexedTokens() ? Token.enumeratedDecoder() : Token.stringDecoder(getIndex());
    }

    public final Function<Integer, String> getEncoder() {
        return isPreindexedTokens() ? Token.enumeratedEncoder() : Token.stringEncoder(getIndex());
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("preindexed", isPreindexedTokens()).
                add("index", getIndex());
    }

}
