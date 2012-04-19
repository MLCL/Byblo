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
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.susx.mlcl.byblo.commands.FilterCommand;

/**
 *
 * @author hiam20
 */
public abstract class EnumeratingDeligate
        implements Serializable, Enumerating {

    private static final long serialVersionUID = 1L;
//
//    @Parameter(names = {"-s1", "--skipindexed1"},
//    description = "Whether indices will be encoded as deltas in the first column")
//    private boolean skipIndexed1 = DEFAULT_SKIP_INDEXING;
//
//    @Parameter(names = {"-s2", "--skipindexed2"},
//    description = "Whether indices will be encoded as deltas in the second column")
//    private boolean skipIndexed2 = DEFAULT_SKIP_INDEXING;

    @Parameter(names = {"-et", "--enumerator-type"})
    private EnumeratorType type = DEFAULT_TYPE;

    public EnumeratingDeligate(EnumeratorType type) {

        this.type = type;
    }

    public EnumeratingDeligate() {
        this(DEFAULT_TYPE);
    }
//
//    @Override
//    public boolean isEnumeratorSkipIndexed1() {
//        return skipIndexed1;
//    }
//
//    @Override
//    public boolean isEnumeratorSkipIndexed2() {
//        return skipIndexed2;
//    }
//
//    @Override
//    public void setEnumeratorSkipIndexed1(boolean b) {
//        skipIndexed1 = b;
//    }
//
//    @Override
//    public void setEnumeratorSkipIndexed2(boolean b) {
//        skipIndexed2 = b;
//    }

    @Override
    public EnumeratorType getEnuemratorType() {
        return type;
    }

    public void setEnumeratorType(EnumeratorType type) {
        this.type = type;
    }

    protected Enumerator<String> open(File file) throws IOException {
        Enumerator<String> out = type.open(file);
        if (out.indexOf(FilterCommand.FILTERED_STRING) != FilterCommand.FILTERED_ID)
            throw new AssertionError();
        return out;
    }

    protected void save(Enumerator<String> enumerator) throws IOException {
        if (enumerator == null) {
            Logger.getLogger(EnumeratingDeligate.class.getName()).log(
                    Level.WARNING,
                    "Attempt made to save an enumerator that was not open.");
            return;
        }
        type.save(enumerator);
        if (enumerator.indexOf(FilterCommand.FILTERED_STRING) != FilterCommand.FILTERED_ID)
            throw new AssertionError();
    }

    protected void close(Enumerator<String> enumerator) throws IOException {
        if (enumerator == null) {
            Logger.getLogger(EnumeratingDeligate.class.getName()).log(
                    Level.WARNING,
                    "Attempt made to close an enumerator that was not open.");
            return;
        }
        if (enumerator.indexOf(FilterCommand.FILTERED_STRING) != FilterCommand.FILTERED_ID)
            throw new AssertionError();
        type.close(enumerator);
    }

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this);
    }

    @Override
    public final String toString() {
        return toStringHelper().toString();
    }

}
