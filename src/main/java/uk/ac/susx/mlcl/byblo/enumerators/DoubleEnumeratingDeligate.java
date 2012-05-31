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
 * POSSIBILITY OF SUCH DAMAGE.To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.enumerators;

import com.beust.jcommander.Parameter;
import com.google.common.base.Objects;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class DoubleEnumeratingDeligate
        extends EnumeratingDeligate
        implements Serializable, DoubleEnumerating {

    private static final long serialVersionUID = 1L;

    @Parameter(names = {"-Ee", "--enumerated-entries"},
    description = "Whether tokens in the first column of the input file are indexed.")
    private boolean enumeratedEntries = DEFAULT_IS_ENUMERATED;

    @Parameter(names = {"-Ef", "--enumerated-features"},
    description = "Whether entries in the second column of the input file are indexed.")
    private boolean enumeratedFeatures = DEFAULT_IS_ENUMERATED;

    @Parameter(names = {"-Xe", "--entries-index-file"},
    description = "Index file for enumerating entries.")
    private File entryEnumeratorFile = null;

    @Parameter(names = {"-Xf", "--features-index-file"},
    description = "Index file for enumerating features.")
    private File featureEnumeratorFile = null;

    private Enumerator<String> entryEnumerator = null;

    private Enumerator<String> featureEnumerator = null;

    protected DoubleEnumeratingDeligate(
            EnumeratorType type, boolean enumeratedEntries, boolean enumeratedFeatures,
            File entryIndexFile, File featureIndexFile,
            Enumerator<String> entryEnumerator, Enumerator<String> featureEnumerator) {
        super(type);
        this.enumeratedEntries = enumeratedEntries;
        this.enumeratedFeatures = enumeratedFeatures;
        this.entryEnumeratorFile = entryIndexFile;
        this.featureEnumeratorFile = featureIndexFile;
        this.entryEnumerator = entryEnumerator;
        this.featureEnumerator = featureEnumerator;
    }

    public DoubleEnumeratingDeligate(
            EnumeratorType type, boolean enumeratedEntries, boolean enumeratedFeatures,
            File entryIndexFile, File featureIndexFile) {
        this(type, enumeratedEntries, enumeratedFeatures, entryIndexFile, featureIndexFile,
             null, null);
    }

    public DoubleEnumeratingDeligate() {
        this(DEFAULT_TYPE, DEFAULT_IS_ENUMERATED, DEFAULT_IS_ENUMERATED,
             null, null);
    }

    @Override
    public final Enumerator<String> getEntryEnumerator() throws IOException {
        if (entryEnumerator == null) {
            // if tokens are preindexed then a file MUST be available
            // otherwise the file will be loaded if it exists
            openEntriesEnumerator();
        }
        return entryEnumerator;
    }

    @Override
    public final Enumerator<String> getFeatureEnumerator() throws IOException {
        if (featureEnumerator == null) {
            openFeaturesEnumerator();
        }
        return featureEnumerator;
    }

    @Override
    public final File getEntryEnumeratorFile() {
        return entryEnumeratorFile;
    }

    @Override
    public final File getFeatureEnumeratorFile() {
        return featureEnumeratorFile;
    }

    public void setEntryEnumeratorFile(File entryEnumeratorFile) {
        this.entryEnumeratorFile = entryEnumeratorFile;
    }

    public void setFeatureEnumeratorFile(File featureEnumeratorFile) {
        this.featureEnumeratorFile = featureEnumeratorFile;
    }

    @Override
    public void openEntriesEnumerator() throws IOException {
        entryEnumerator = open(entryEnumeratorFile);;
    }

    @Override
    public void saveEntriesEnumerator() throws IOException {
        save(entryEnumerator);
    }

    @Override
    public void closeEntriesEnumerator() throws IOException {
        close(entryEnumerator);
        entryEnumerator = null;
    }

    @Override
    public void openFeaturesEnumerator() throws IOException {
        featureEnumerator = open(featureEnumeratorFile);
    }

    @Override
    public void saveFeaturesEnumerator() throws IOException {
        save(featureEnumerator);
    }

    @Override
    public void closeFeaturesEnumerator() throws IOException {
        close(featureEnumerator);
        featureEnumerator = null;
    }

    @Override
    public boolean isEntriesEnumeratorOpen() {
        return entryEnumerator != null;
    }

    @Override
    public boolean isFeaturesEnumeratorOpen() {
        return featureEnumerator != null;
    }

    @Override
    public void closeEnumerator() throws IOException {
        if (isFeaturesEnumeratorOpen())
            closeFeaturesEnumerator();
        if (isEntriesEnumeratorOpen())
            closeEntriesEnumerator();
    }

    @Override
    public void saveEnumerator() throws IOException {
        if (isEntriesEnumeratorOpen())
            saveEntriesEnumerator();
        if (isFeaturesEnumeratorOpen())
            saveFeaturesEnumerator();
    }

    @Override
    public void openEnumerator() throws IOException {
        openEntriesEnumerator();
        openFeaturesEnumerator();
    }

    @Override
    public boolean isEnumeratedEntries() {
        return enumeratedEntries;
    }

    @Override
    public void setEnumeratedEntries(boolean enumeratedEntries) {
        this.enumeratedEntries = enumeratedEntries;
    }

    @Override
    public boolean isEnumeratedFeatures() {
        return enumeratedFeatures;
    }

    @Override
    public void setEnumeratedFeatures(boolean enumeratedFeatures) {
        this.enumeratedFeatures = enumeratedFeatures;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("preindexed1", isEnumeratedEntries()).
                add("preindexed2", isEnumeratedFeatures()).
                add("index1", getEntryEnumeratorFile()).
                add("index2", getFeatureEnumeratorFile());
    }

    @Override
    public SingleEnumerating getEntriesEnumeratorCarriar() {
        return EnumeratingDeligates.toSingleEntries(this);
    }

    @Override
    public SingleEnumerating getFeaturesEnumeratorCarriar() {
        return EnumeratingDeligates.toSingleFeatures(this);
    }

    @Override
    public boolean isEnumeratorOpen() {
        return isEntriesEnumeratorOpen() || isFeaturesEnumeratorOpen();
    }

}
