/*
 * Copyright (c) 2010-2012, University of Sussex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of the University of Sussex nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.susx.mlcl.byblo.enumerators;

import com.beust.jcommander.Parameter;
import com.google.common.base.Objects;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.FullBuild;
import uk.ac.susx.mlcl.byblo.commands.FilterCommand;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class EnumeratingDeligate
        implements Serializable, Enumerating {

    private static final Log LOG = LogFactory.getLog(FullBuild.class);

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
            LOG.warn("Attempt made to save an enumerator that was not open.");
            return;
        }
        type.save(enumerator);
        if (enumerator.indexOf(FilterCommand.FILTERED_STRING) != FilterCommand.FILTERED_ID)
            throw new AssertionError();
    }

    protected void close(Enumerator<String> enumerator) throws IOException {
        if (enumerator == null) {
            LOG.warn("Attempt made to close an enumerator that was not open.");
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
