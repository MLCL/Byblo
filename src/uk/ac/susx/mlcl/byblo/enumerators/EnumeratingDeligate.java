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
import uk.ac.susx.mlcl.lib.commands.AbstractDeligate;

/**
 *
 * @author hiam20
 */
public abstract class EnumeratingDeligate
        extends AbstractDeligate
        implements Serializable, Enumerating {

    private static final long serialVersionUID = 1L;

    @Parameter(names = {"-s1", "--skipindexed1"},
    description = "Whether indices will be encoded as deltas in the first column")
    private boolean skipIndexed1 = DEFAULT_SKIP_INDEXING;

    @Parameter(names = {"-s2", "--skipindexed2"},
    description = "Whether indices will be encoded as deltas in the second column")
    private boolean skipIndexed2 = DEFAULT_SKIP_INDEXING;

    @Parameter(names = {"-et", "--enumerator-type"})
    private Type type = Type.JDBC;

    public EnumeratingDeligate(boolean skipIndexed1, boolean skipIndexed2) {
        this.skipIndexed1 = skipIndexed1;
        this.skipIndexed2 = skipIndexed2;
    }

    public EnumeratingDeligate() {
        this(DEFAULT_SKIP_INDEXING, DEFAULT_SKIP_INDEXING);
    }

    @Override
    public boolean isEnumeratorSkipIndexed1() {
        return skipIndexed1;
    }

    @Override
    public boolean isEnumeratorSkipIndexed2() {
        return skipIndexed2;
    }

    protected Enumerator<String> open(File file) throws IOException {
        Enumerator<String> out = type.open(file);
        assert out.indexOf(FilterCommand.FILTERED_STRING) == FilterCommand.FILTERED_ID;
        return out;
    }

    protected void save(Enumerator<String> enumerator) throws IOException {
        if (enumerator == null) {
            Logger.getLogger(EnumeratingDeligate.class.getName()).log(
                    Level.WARNING, "Attempt made to save an enumerator that was not open.");
            return;
        }
        type.save(enumerator);
    }

    protected void close(Enumerator<String> enumerator) throws IOException {
        if (enumerator == null) {
            Logger.getLogger(EnumeratingDeligate.class.getName()).log(
                    Level.WARNING, "Attempt made to save an enumerator that was not open.");
            return;
        }
        type.close(enumerator);
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("skipIndexed1", isEnumeratorSkipIndexed1()).
                add("skipIndexed2", isEnumeratorSkipIndexed2());
    }

    private enum Type {

        Memory {

            @Override
            public Enumerator<String> open(File file) throws IOException {
                if (file != null && file.exists())
                    return MemoryBasedStringEnumerator.load(file);
                else
                    return MemoryBasedStringEnumerator.newInstance(file);
            }

            @Override
            public void save(Enumerator<String> enumerator) throws IOException {
                ((MemoryBasedStringEnumerator) enumerator).save();
            }

            @Override
            public void close(Enumerator<String> enumerator) throws IOException {
            }

        },
        JDBC {

            @Override
            public Enumerator<String> open(File file) {
                if (file == null) {
                    return JDBCStringEnumerator.newInstance(file);
                } else if (!file.exists()) {
                    return JDBCStringEnumerator.newInstance(file);
                } else {
                    return JDBCStringEnumerator.load(file);
                }
            }

            @Override
            public void save(Enumerator<String> enumerator) throws IOException {
                ((JDBCStringEnumerator) enumerator).save();
            }

            @Override
            public void close(Enumerator<String> enumerator) throws IOException {
                ((JDBCStringEnumerator) enumerator).close();
            }

        };

        public abstract Enumerator<String> open(File file) throws IOException;

        public abstract void save(Enumerator<String> enumerator) throws IOException;

        public abstract void close(Enumerator<String> enumerator) throws IOException;

    }
}
