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
package uk.ac.susx.mlcl.byblo.commands;

import uk.ac.susx.mlcl.byblo.tasks.MergeTask;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Comparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.*;
import uk.ac.susx.mlcl.lib.tasks.AbstractCommand;

/**
 * Merges the contents of two sorted source files, line by line, into a
 * destination file.
 *
 * The source files are assumed to already be ordered according to the
 * comparator.
 *
 * Any file denoted by the name string "-" is assumed to be standard-in in the
 * case of source files, and standard out in the case of destination files..
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Merges the contents of two sorted source files, line by line, into a destination file.")
public abstract class AbstractMergeCommand<T> extends AbstractCommand {

    private static final Log LOG = LogFactory.getLog(AbstractMergeCommand.class);

    @ParametersDelegate
    private final FileMergeDeligate fileDeligate = new FileMergeDeligate();

    @Parameter(names = {"-r", "--reverse"},
    description = "Reverse the result of comparisons.")
    private boolean reverse = false;

    private Comparator<T> comparator;

    public AbstractMergeCommand(File sourceFileA, File sourceFileB, File destination,
                                Charset charset, Comparator<T> comparator) {
        fileDeligate.set(sourceFileA, sourceFileB, destination, charset);
        setComparator(comparator);
    }

    public AbstractMergeCommand() {
    }

    public FileMergeDeligate getFileDeligate() {
        return fileDeligate;
    }

    public final Comparator<T> getComparator() {
        return comparator;
    }

    public final void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public final boolean isReverse() {
        return reverse;
    }

    public final void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {

        return super.toStringHelper().
                add("files", getFileDeligate()).
                add("comparator", comparator);
    }

    @Override
    public void runCommand() throws Exception {
        if (LOG.isInfoEnabled())
            LOG.info("Running merge from \"" + getFileDeligate().getSourceFileA()
                    + "\" and \"" + getFileDeligate().getSourceFileB()
                    + "\" to \"" + getFileDeligate().getDestinationFile() + "\".");

        Source<T> srcA = openSource(getFileDeligate().getSourceFileA());
        Source<T> srcB = openSource(getFileDeligate().getSourceFileB());
        Sink<T> snk = openSink(getFileDeligate().getDestinationFile());

        MergeTask<T> task = new MergeTask<T>(
                srcA, srcB, snk, getComparator());
        task.run();
        while (task.isExceptionThrown())
            task.throwException();

        if (snk instanceof Flushable)
            ((Flushable) snk).flush();

        if (srcA instanceof Closeable)
            ((Closeable) srcA).close();
        if (srcB instanceof Closeable)
            ((Closeable) srcB).close();
        if (snk instanceof Closeable)
            ((Closeable) snk).close();

        if (LOG.isInfoEnabled())
            LOG.info("Completed merge.");
    }

    protected abstract Source<T> openSource(File file) throws FileNotFoundException, IOException;

    protected abstract Sink<T> openSink(File file) throws FileNotFoundException, IOException;

}
