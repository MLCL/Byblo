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
package uk.ac.susx.mlcl.lib.commands;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects;
import uk.ac.susx.mlcl.lib.tasks.FileMoveTask;

import javax.annotation.CheckReturnValue;
import java.io.File;

/**
 * Move source file to a destination.
 * <p/>
 * Attempts to perform a fast rename if possible. Otherwise it falls back to
 * slower copy and delete.
 * <p/>
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Parameters(commandDescription = "Move a file.")
public final class FileMoveCommand extends AbstractCommand {

    @ParametersDelegate
    private final FilePipeDelegate filesDelegate = new FilePipeDelegate();

    public FileMoveCommand(File sourceFile, File destinationFile) {
        filesDelegate.setSourceFile(sourceFile);
        filesDelegate.setDestinationFile(destinationFile);
    }

    public FileMoveCommand() {
    }

    public final void setSourceFile(File sourceFile) {
        filesDelegate.setSourceFile(sourceFile);
    }

    public final void setDestinationFile(File destinationFile) {
        filesDelegate.setDestinationFile(destinationFile);
    }

    public final File getSourceFile() {
        return filesDelegate.getSourceFile();
    }

    public final File getDestinationFile() {
        return filesDelegate.getDestinationFile();
    }

    @Override
    @CheckReturnValue
    public boolean runCommand() {

        FileMoveTask task = new FileMoveTask(
                filesDelegate.getSourceFile(),
                filesDelegate.getDestinationFile());

        task.run();

        try {
            if (task.isExceptionTrapped())
                task.throwTrappedException();
            return true;
        } catch (Exception e) {
            System.err.print(e);
            return false;
        }
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("from", filesDelegate.getSourceFile()).
                add("to", filesDelegate.getDestinationFile());
    }
}
