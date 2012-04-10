/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.io;

import com.beust.jcommander.Parameter;
import com.google.common.base.Objects;
import java.io.Serializable;
import uk.ac.susx.mlcl.lib.commands.AbstractDeligate;

/**
 *
 * @author hiam20
 */
public class IndexDeligateImpl extends AbstractDeligate implements Serializable, IndexDeligate {

    private static final long serialVersionUID = 1L;

    @Parameter(names = {"-s1", "--skipindexed1"},
    description = "Whether indices will be encoded as deltas in the first column")
    private boolean skipIndexed1 = DEFAULT_SKIP_INDEXING;

    @Parameter(names = {"-s2", "--skipindexed2"},
    description = "Whether indices will be encoded as deltas in the second column")
    private boolean skipIndexed2 = DEFAULT_SKIP_INDEXING;

    public IndexDeligateImpl(boolean skipIndexed1, boolean skipIndexed2) {
        this.skipIndexed1 = skipIndexed1;
        this.skipIndexed2 = skipIndexed2;
    }

    public IndexDeligateImpl() {
        this(DEFAULT_SKIP_INDEXING, DEFAULT_SKIP_INDEXING);
    }

    @Override
    public boolean isSkipIndexed1() {
        return skipIndexed1;
    }

    @Override
    public boolean isSkipIndexed2() {
        return skipIndexed2;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("skipIndexed1", isSkipIndexed1()).
                add("skipIndexed2", isSkipIndexed2());
    }

}
