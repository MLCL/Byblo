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
import javax.annotation.Nullable;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class SingleEnumeratingDeligate
        extends EnumeratingDeligate
        implements SingleEnumerating {

    private static final long serialVersionUID = 1L;

    @Parameter(names = {"-E", "--enumerated"},
               description = "Whether tokens in the input file are enumerated.")
    private boolean enumerationEnabled = DEFAULT_IS_ENUMERATED;

    @Parameter(names = {"-X", "--index-file"},
               description = "Index for the string tokens.")
    private File enumeratorFile = null;

    private Enumerator<String> enumerator = null;

    public SingleEnumeratingDeligate(
            EnumeratorType type, boolean enumerated, @Nullable File indexFile) {
        super(type);
        this.enumerationEnabled = enumerated;
        this.enumeratorFile = indexFile;
        this.enumerator = null;
    }

    public SingleEnumeratingDeligate() {
        this(DEFAULT_TYPE, DEFAULT_IS_ENUMERATED, null);
    }

    @Override
    public File getEnumeratorFile() {
        return enumeratorFile;
    }

    @Override
    public void setEnumeratorFile(File enumeratorFile) {
        this.enumeratorFile = enumeratorFile;
    }

    @Override
    public final boolean isEnumerationEnabled() {
        return enumerationEnabled;
    }

    @Override
    public void setEnumerationEnabled(boolean enumerationEnabled) {
        this.enumerationEnabled = enumerationEnabled;
    }

    @Override
    public final Enumerator<String> getEnumerator() throws IOException {
        if (enumerator == null) {
            openEnumerator();
        }
        return enumerator;
    }

    @Override
    public void openEnumerator() throws IOException {
        enumerator = open(enumeratorFile);
    }

    @Override
    public void saveEnumerator() throws IOException {
        save(enumerator);
    }

    @Override
    public boolean isEnumeratorOpen() {
        return enumerator != null;
    }

    @Override
    public void closeEnumerator() throws IOException {
        close(enumerator);
        enumerator = null;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("isEnumerated", isEnumerationEnabled()).
                add("indexFile", getEnumeratorFile());
    }

    @Override
    public DoubleEnumerating getEnumeratorPairCarriar() {
        return EnumeratingDeligates.toPair(this);
    }
}
