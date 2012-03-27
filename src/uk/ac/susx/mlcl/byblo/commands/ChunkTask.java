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

import uk.ac.susx.mlcl.lib.commands.CopyCommand;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Objects;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.MiscUtil;
import uk.ac.susx.mlcl.lib.io.FileFactory;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import uk.ac.susx.mlcl.lib.AbstractCommandTask;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.io.Files;
import uk.ac.susx.mlcl.lib.commands.InputFileValidator;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 * @deprecated Should be replaced with Chunker
 */
@Deprecated
@Parameters(commandDescription = "Split a large file into a number of smaller files.")
public class ChunkTask extends AbstractCommandTask {

    private static final Log LOG = LogFactory.getLog(ChunkTask.class);

    public static final int DEFAULT_MAX_CHUNK_SIZE = 10 * (1 << 20);

    private FileFactory chunkFileFactory =
            new TempFileFactory();

    @Parameter(names = {"-C", "--max-chunk-size"},
               description = "Number of lines that will be read and sorted in RAM at one time (per thread). Larger values increase memory usage and performace.")
    private int maxChunkSize = DEFAULT_MAX_CHUNK_SIZE;

    @Parameter(names = {"-i", "--input-file"},
               description = "Source file. If this argument is not given, or if it is \"-\", then stdin will be read.",
               validateWith = InputFileValidator.class,
               required = true)
    private File sourceFile;

    private BlockingQueue<File> dstFileQueue = new LinkedBlockingDeque<File>();

    @Parameter(names = {"-c", "--charset"},
               description = "Character encoding to use.")
    private Charset charset = Files.DEFAULT_CHARSET;

    public final Charset getCharset() {
        return charset;
    }

    public final void setCharset(Charset charset) {
        Checks.checkNotNull(charset);
        this.charset = charset;
    }

    public ChunkTask() {
    }

    public ChunkTask(File srcFile, Charset charset) {
        setSrcFile(srcFile);
        setCharset(charset);
    }

    public ChunkTask(File srcFile, Charset charset, int maxChunkSize) {
        setSrcFile(srcFile);
        setMaxChunkSize(maxChunkSize);
        setCharset(charset);
    }

    @Override
    protected void initialiseTask() throws Exception {
    }

    @Override
    protected void finaliseTask() throws Exception {
    }

    public BlockingQueue<File> getDstFileQueue() {
        return dstFileQueue;
    }

    public void setDstFileQueue(BlockingQueue<File> dstFileQueue) {
        if (dstFileQueue == null)
            throw new NullPointerException("dstFileQueue is null");
        this.dstFileQueue = dstFileQueue;
    }

    @Override
    protected void runTask() throws Exception {
//        // Can't actually do this because it will be deleted later.
//        // Don't bother chunking if it's smaller than the chunk size
//        if(sourceFile.length() <= maxChunkSize) {
//            if(LOG.isDebugEnabled())
//                LOG.debug("Skipping chunking because source file is smaller than max chunk size.");
//            dstFileQueue.put(sourceFile);
//            return;            
//        }

        // So instead of the (above) fast solution to a small (or empty) input
        // file, just quickly copy the file.
        if (sourceFile.length() <= maxChunkSize) {
            if (LOG.isDebugEnabled())
                LOG.debug("Input is smaller than chunk size, copying.");
            File tmp = chunkFileFactory.createFile("ch", "");
            CopyCommand ct = new CopyCommand(sourceFile, tmp);
            ct.runCommand();
            dstFileQueue.put(tmp);
            return;
        }


        if (LOG.isInfoEnabled())
            LOG.info("Running chunker on file \"" + sourceFile + "\", max size = " + MiscUtil.
                    humanReadableBytes(getMaxChunkSize()) + ".");

        BufferedReader reader = null;
        BufferedWriter writer = null;
        File tmp = null;

        final int nlBytes = System.getProperty("line.separator").getBytes().length;

        try {
            reader = Files.openReader(sourceFile, charset);
            int chunk = 1;
            int chunkBytesWritten = 0;
            String line = reader.readLine();
            int lineBytes = line == null ? 0
                    : line.getBytes().length + nlBytes;

            while (line != null) {

                try {
                    tmp = chunkFileFactory.createFile("ch", "");
                    if (LOG.isDebugEnabled())
                        LOG.debug("Producing chunk " + chunk + " to file \"" + tmp
                                + "\".");
                    writer = Files.openWriter(tmp, charset);

                    do {
                        writer.write(line);
                        writer.newLine();
                        chunkBytesWritten += lineBytes;
                        line = reader.readLine();
                        lineBytes = line == null ? 0 : line.getBytes().length + nlBytes;
                    } while (line != null && chunkBytesWritten + lineBytes < maxChunkSize);

                } finally {

                    if (writer != null) {
                        writer.flush();
                        writer.close();
                    }
                    if (tmp != null)
                        dstFileQueue.put(tmp);
                    chunkBytesWritten = 0;
                    chunk++;
                }
            }
        } finally {
            if (reader != null)
                reader.close();
        }
        if (LOG.isInfoEnabled())
            LOG.info("Completed chunking task.");
    }

    public final void setMaxChunkSize(int maxChunkSize) {
        if (maxChunkSize <= 0)
            throw new IllegalArgumentException("maxChunkSize <= 0");
        this.maxChunkSize = maxChunkSize;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    public final void setSrcFile(File sourceFile) {
        if (sourceFile == null)
            throw new NullPointerException("sourceFile is null");
        this.sourceFile = sourceFile;
    }

    public File getSrcFile() {
        return sourceFile;
    }

    public Collection<File> getDestFiles() {
        return Collections.unmodifiableCollection(dstFileQueue);
    }

    public FileFactory getChunkFileFactory() {
        return chunkFileFactory;
    }

    public void setChunkFileFactory(FileFactory chunkFileFactory) {
        this.chunkFileFactory = chunkFileFactory;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("in", sourceFile).
                add("out", chunkFileFactory).
                add("chunkSize", maxChunkSize).
                add("charset", charset);
    }
}
