/*
 * Copyright (c) 2010-2013, University of Sussex
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
package uk.ac.susx.mlcl.lib.tasks;

import com.google.common.base.Objects;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.events.ProgressDelegate;
import uk.ac.susx.mlcl.lib.events.ProgressListener;
import uk.ac.susx.mlcl.lib.events.ProgressReporting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import static java.text.MessageFormat.format;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class FileCopyTask extends AbstractTask implements ProgressReporting {

    final ProgressDelegate progress = new ProgressDelegate(this, false);

    private File sourceFile;

    private File destinationFile;

    public FileCopyTask(File sourceFile, File destinationFile) {
        setSrcFile(sourceFile);
        setDstFile(destinationFile);
    }

    public FileCopyTask() {
    }

    static void copy(File from, File to) throws IOException {

        FileChannel source = null;
        FileChannel destination = null;
        try {

            source = new FileInputStream(from).getChannel();
            destination = new FileOutputStream(to).getChannel();

            long remaining = source.size();
            while (remaining > 0)
                remaining -= destination.transferFrom(source, 0, remaining);

        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    @Override
    protected void runTask() throws Exception {

        progress.startAdjusting();
        progress.setState(State.RUNNING);
        progress.setMessage(format("Copying file from \"{0}\" to \"{1}\".",
                getSrcFile(), getDstFile()));
        progress.endAdjusting();

        // Check the configuration state
        if (sourceFile.equals(destinationFile))
            throw new IllegalStateException("sourceFile equals destinationFile");

        copy(sourceFile, destinationFile);

        progress.setState(State.COMPLETED);
    }

    final File getSrcFile() {
        return sourceFile;
    }

    final File getDstFile() {
        return destinationFile;
    }

    public final void setSrcFile(final File sourceFile) throws NullPointerException {
        Checks.checkNotNull("sourceFile", sourceFile);
        this.sourceFile = sourceFile;
    }

    public final void setDstFile(final File destinationFile) throws NullPointerException {
        Checks.checkNotNull("destinationFile", destinationFile);
        this.destinationFile = destinationFile;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("in", sourceFile).
                add("out", destinationFile);
    }

    @Override
    public void removeProgressListener(ProgressListener progressListener) {
        progress.removeProgressListener(progressListener);
    }

    @Override
    public boolean isProgressPercentageSupported() {
        return progress.isProgressPercentageSupported();
    }

    @Override
    public String getProgressReport() {
        return progress.getProgressReport();
    }

    @Override
    public int getProgressPercent() {
        return progress.getProgressPercent();
    }

    @Override
    public ProgressListener[] getProgressListeners() {
        return progress.getProgressListeners();
    }

    @Override
    public String getName() {
        return "copy";
    }

    @Override
    public State getState() {
        return progress.getState();
    }

    @Override
    public void addProgressListener(ProgressListener progressListener) {
        progress.addProgressListener(progressListener);
    }
}
