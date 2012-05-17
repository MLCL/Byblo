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

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Flushable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.MiscUtil;
import uk.ac.susx.mlcl.lib.commands.AbstractCommand;
import uk.ac.susx.mlcl.lib.commands.FilePipeDelegate;
import uk.ac.susx.mlcl.lib.io.Files;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.ObjectSource;
import uk.ac.susx.mlcl.lib.tasks.ObjectPipeTask;

/**
 * Abstract super class for all tasks that require copying data from one file
 * to another.
 *
 * @param <T>
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Parameters(commandDescription = "Sort a file.")
public abstract class AbstractCopyCommand<T> extends AbstractCommand {

    private static final Log LOG = LogFactory.getLog(AbstractCopyCommand.class);

    @ParametersDelegate
    private FilePipeDelegate filesDeligate = new FilePipeDelegate();

    public AbstractCopyCommand(File sourceFile, File destinationFile, Charset charset) {
        filesDeligate = new FilePipeDelegate(sourceFile, destinationFile, charset);
    }

    public AbstractCopyCommand(File sourceFile, File destinationFile) {
        this(sourceFile, destinationFile, Files.DEFAULT_CHARSET);
    }

    public AbstractCopyCommand() {
    }

    public FilePipeDelegate getFilesDeligate() {
        return filesDeligate;
    }

    public final void setCharset(Charset charset) {
        filesDeligate.setCharset(charset);
    }

    public final Charset getCharset() {
        return filesDeligate.getCharset();
    }

    public final void setSourceFile(File sourceFile) throws NullPointerException {
        filesDeligate.setSourceFile(sourceFile);
    }

    public final void setDestinationFile(File destFile) throws NullPointerException {
        filesDeligate.setDestinationFile(destFile);
    }

    public final File getSourceFile() {
        return filesDeligate.getSourceFile();
    }

    public final File getDestinationFile() {
        return filesDeligate.getDestinationFile();
    }

    @Override
    public void runCommand() throws Exception {
        if (LOG.isInfoEnabled())
            LOG.info(MessageFormat.format(
                    "Running command {0} from \"{1}\" to \"{2}\".",
                    getName(),
                    getFilesDeligate().getSourceFile(),
                    getFilesDeligate().getDestinationFile()));
        LOG.debug(MiscUtil.memoryInfoString());

        ObjectSource<T> src = openSource(getFilesDeligate().getSourceFile());
        ObjectSink<T> snk = openSink(getFilesDeligate().getDestinationFile());

        ObjectPipeTask<T> task = new ObjectPipeTask<T>();
        task.setSource(src);
        task.setSink(snk);
        task.run();

        while (task.isExceptionTrapped())
            task.throwTrappedException();

        if (src instanceof Closeable)
            ((Closeable) src).close();
        if (snk instanceof Flushable)
            ((Flushable) snk).flush();
        if (snk instanceof Closeable)
            ((Closeable) snk).close();

        LOG.debug(MiscUtil.memoryInfoString());
        if (LOG.isInfoEnabled())
            LOG.info(MessageFormat.format("Completed command {0}.", getName()));
    }

    public String getName() {
        return "copy";
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("name", getName()).
                add("files", getFilesDeligate());
    }

    protected abstract ObjectSource<T> openSource(File file)
            throws FileNotFoundException, IOException;

    protected abstract ObjectSink<T> openSink(File file)
            throws FileNotFoundException, IOException;

}
