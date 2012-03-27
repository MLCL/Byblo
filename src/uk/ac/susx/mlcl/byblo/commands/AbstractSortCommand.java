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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.tasks.SortTask;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.io.*;
import uk.ac.susx.mlcl.lib.command.AbstractCommand;

/**
 * Task that takes a single input file and sorts it according to some
 * comparator, then writes the results to an output file.
 *
 * @param <T>
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Sort a file.")
public abstract class AbstractSortCommand<T> extends AbstractCommand {

    private static final Log LOG = LogFactory.getLog(AbstractSortCommand.class);

    @ParametersDelegate
    private final FilePipeDeligate filesDeligate = new FilePipeDeligate();

    @Parameter(names = {"-r", "--reverse"},
    description = "Reverse the result of comparisons.")
    private boolean reverse = false;

    private Comparator<T> comparator;

    public AbstractSortCommand(File sourceFile, File destinationFile, Charset charset,
                               Comparator<T> comparator) {
        filesDeligate.setSourceFile(sourceFile);
        filesDeligate.setDestinationFile(destinationFile);
        filesDeligate.setCharset(charset);
        this.comparator = comparator;
    }

    public AbstractSortCommand(File sourceFile, File destinationFile, Charset charset) {
        this(sourceFile, destinationFile, charset, null);
    }

    public AbstractSortCommand(File sourceFile, File destinationFile) {
        this(sourceFile, destinationFile, Files.DEFAULT_CHARSET);
    }

    public AbstractSortCommand() {
    }

    public FilePipeDeligate getFilesDeligate() {
        return filesDeligate;
    }

    public final boolean isReverse() {
        return reverse;
    }

    public final void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

   
    public final Comparator<T> getComparator() {
        return isReverse() ? Comparators.reverse(comparator) : comparator;
    }

    public final void setComparator(Comparator<T> comparator) {
        Checks.checkNotNull("comparator", comparator);
        this.comparator = comparator;
    }

    public boolean isComparatorSet() {
        return getComparator() != null;
    }

    @Override
    public void runCommand() throws Exception {
        if (LOG.isInfoEnabled())
            LOG.info("Running memory sort from \"" + getFilesDeligate().getSourceFile()
                    + "\" to \"" + getFilesDeligate().getDestinationFile() + "\".");

        Source<T> src = openSource(getFilesDeligate().getSourceFile());
        Sink<T> snk = openSink(getFilesDeligate().getDestinationFile());
        
        SortTask<T> task = new SortTask<T>();
        task.setComparator(getComparator());
        task.setSource(src);
        task.setSink(snk);
        task.run();
        
        while(task.isExceptionThrown())
            task.throwException();

        if (snk instanceof Flushable)
            ((Flushable) snk).flush();
        if (snk instanceof Closeable)
            ((Closeable) snk).close();

        if (LOG.isInfoEnabled())
            LOG.info("Completed memory sort.");
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("comparator", comparator);
    }

    protected abstract Source<T> openSource(File file)
            throws FileNotFoundException, IOException;

    protected abstract Sink<T> openSink(File file)
            throws FileNotFoundException, IOException;

}
