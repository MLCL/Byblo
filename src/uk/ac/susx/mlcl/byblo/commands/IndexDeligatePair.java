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
import uk.ac.susx.mlcl.lib.commands.AbstractDeligate;

/**
 *
 * @author hiam20
 */
public class IndexDeligatePair extends AbstractDeligate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Parameter(names = {"-p1", "--preindexed1"}, description = "Whether tokens in the first column of the input file are indexed.")
    private boolean preindexedTokens1 = false;

    @Parameter(names = {"-p2", "--preindexed2"}, description = "Whether entries in the second column of the input file are indexed.")
    private boolean preindexedTokens2 = false;

    private Enumerator<String> index1 = null;

    private Enumerator<String> index2 = null;

    public IndexDeligatePair(boolean preindexedTokens1, boolean preindexedTokens2) {
        setPreindexedTokens1(preindexedTokens1);
        setPreindexedTokens2(preindexedTokens2);
    }

    public IndexDeligatePair() {
    }

    public final Enumerator<String> getIndex1() {
        if (index1 == null)
            index1 = Enumerators.newDefaultStringEnumerator();
        return index1;
    }

    public final void setIndex1(Enumerator<String> entryIndex) {
        this.index1 = entryIndex;
    }

    public final Enumerator<String> getIndex2() {
        if (index2 == null)
            index2 = Enumerators.newDefaultStringEnumerator();
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

    public final Function<String, Integer> getDecoder1() {
        return isPreindexedTokens1() ? Token.enumeratedDecoder() : Token.stringDecoder(getIndex1());
    }

    public final Function<Integer, String> getEncoder1() {
        return isPreindexedTokens1() ? Token.enumeratedEncoder() : Token.stringEncoder(getIndex1());
    }

    public final Function<String, Integer> getDecoder2() {
        return isPreindexedTokens2() ? Token.enumeratedDecoder() : Token.stringDecoder(getIndex2());
    }

    public final Function<Integer, String> getEncoder2() {
        return isPreindexedTokens2() ? Token.enumeratedEncoder() : Token.stringEncoder(getIndex2());
    }
    
     @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("preindexed1", isPreindexedTokens1()).
                add("preindexed2", isPreindexedTokens2()).
                add("index1", getIndex1()).
                add("index2", getIndex2());
    }
}
