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
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.susx.mlcl.byblo.commands.FilterCommand;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.JDBCStringEnumerator;
import uk.ac.susx.mlcl.lib.MemoryStringEnumerator;
import uk.ac.susx.mlcl.lib.commands.AbstractDeligate;

/**
 *
 * @author hiam20
 */
public abstract class EnumeratorBaringDeligate
        extends AbstractDeligate
        implements Serializable, EnumeratorBaring {

    private static final long serialVersionUID = 1L;

    @Parameter(names = {"-s1", "--skipindexed1"},
    description = "Whether indices will be encoded as deltas in the first column")
    private boolean skipIndexed1 = DEFAULT_SKIP_INDEXING;

    @Parameter(names = {"-s2", "--skipindexed2"},
    description = "Whether indices will be encoded as deltas in the second column")
    private boolean skipIndexed2 = DEFAULT_SKIP_INDEXING;

    enum Type {

        Memory,
        JDBC

    }

    private Type type = Type.JDBC;

    public EnumeratorBaringDeligate(boolean skipIndexed1, boolean skipIndexed2) {
        this.skipIndexed1 = skipIndexed1;
        this.skipIndexed2 = skipIndexed2;
    }

    public EnumeratorBaringDeligate() {
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

    public Enumerator<String> open(File file) throws IOException {
        Enumerator<String> out;
        if (type == Type.Memory) {
            if (file != null && file.exists())
                out = MemoryStringEnumerator.load(file);
            else
                out = MemoryStringEnumerator.newInstance(file);
        } else if (type == Type.JDBC) {
            if (file == null) {
                out = JDBCStringEnumerator.newInstance(file);
            } else if (!file.exists()) {
                out = JDBCStringEnumerator.newInstance(file);
            } else {
                out = JDBCStringEnumerator.load(file);
            }
//            if (file != null && file.exists())
//                else {
//                // XXX
//                File f = File.createTempFile("jdbc-", ".tmp", new File("./temp0"));
//                return JDBCStringEnumerator.newInstance(f);
//            }
        } else {
            throw new AssertionError("Unknown enumerator type " + type);
        }
        
        assert out.indexOf(FilterCommand.FILTERED_STRING) == FilterCommand.FILTERED_ID;
        return out;
    }

    public void save(Enumerator<String> enumerator) throws IOException {
        if(enumerator == null) {
            Logger.getLogger(EnumeratorBaringDeligate.class.getName()).log(Level.WARNING,
                "Attempt made to save an enumerator that was not open.");
            return;
        }
        if (type == Type.Memory) {
            ((MemoryStringEnumerator) enumerator).save();
        } else if (type == Type.JDBC) {
            ((JDBCStringEnumerator) enumerator).save();
        } else {
            throw new AssertionError("Unknown enumerator type " + type);
        }
    }

    public void close(Enumerator<String> enumerator) throws IOException {
        if(enumerator == null) {
            Logger.getLogger(EnumeratorBaringDeligate.class.getName()).log(Level.WARNING,
                "Attempt made to save an enumerator that was not open.");
            return;
        }

        if (type == Type.Memory) {
            // Nothing
        } else if (type == Type.JDBC) {
            ((JDBCStringEnumerator) enumerator).close();
        } else {
            throw new AssertionError("Unknown enumerator type " + type);
        }
    }

}
