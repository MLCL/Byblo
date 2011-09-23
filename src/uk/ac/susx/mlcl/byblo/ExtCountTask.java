/*
 * Copyright (c) 2010-2011, University of Sussex
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
package uk.ac.susx.mlcl.byblo;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import uk.ac.susx.mlcl.byblo.MergeTask.Formatter;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.FileFactory;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import uk.ac.susx.mlcl.lib.tasks.AbstractParallelTask;
import uk.ac.susx.mlcl.lib.tasks.Task;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 *
 * @version 2nd December 2010
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "USAGE_COUNT_COMMAND")
public class ExtCountTask extends AbstractParallelTask {

    private static final Logger LOG = Logger.getLogger(
            ExtCountTask.class.getName());

    private static final int DEFAULT_MAX_CHUNK_SIZE = ChunkTask.DEFAULT_MAX_CHUNK_SIZE;

    @Parameter(names = {"-c", "--chunk-size"},
               descriptionKey = "USAGE_MAX_CHUNK_SIZE")
    private int maxChunkSize = DEFAULT_MAX_CHUNK_SIZE;
//
//    @Parameter(names = {"-i", "--input"},
//               descriptionKey = "USAGE_INPUT_FILE")
//    private File sourceFile = IOUtil.STDIN_FILE;
//
//    @Parameter(names = {"-o", "--output"},
//               descriptionKey = "USAGE_OUTPUT_FILE")
//    private File destFile = IOUtil.STDOUT_FILE;
////      --compress-program=PROG  compress temporaries with PROG;
////                              decompress them with PROG -d

    @Parameter(names = {"-ii", "--input-instances"},
               required = true,
               descriptionKey = "uk.ac.susx.mlcl.byblo.ExtCountTask.INSTANCES_DESCRIPTION")
    private File instancesFile;

    @Parameter(names = {"-of", "--output-features"},
               required = true,
               descriptionKey = "uk.ac.susx.mlcl.byblo.ExtCountTask.FEATURES_DESCRIPTION")
    private File featuresFile = null;

    @Parameter(names = {"-oh", "--output-heads"},
               required = true,
               descriptionKey = "uk.ac.susx.mlcl.byblo.ExtCountTask.HEADS_DESCRIPTION")
    private File headsFile = null;

    @Parameter(names = {"-oc", "--output-contexts"},
               required = true,
               descriptionKey = "uk.ac.susx.mlcl.byblo.ExtCountTask.CONTEXTS_DESCRIPTION")
    private File contextsFile = null;

    @Parameter(names = {"-T", "--temporary-directory"},
               descriptionKey = "USAGE_TEMP_DIR",
               converter = TempFileFactoryConverter.class)
    private FileFactory tempFileFactory = new TempFileFactory("temp", ".txt");

    @Parameter(names = {"--charset"},
               descriptionKey = "USAGE_CHARSET")
    private Charset charset = IOUtil.DEFAULT_CHARSET;

    private Comparator<String> comparator = new Comparator<String>() {

        @Override
        public int compare(String o1, String o2) {
            return o1.substring(0, o1.lastIndexOf('\t')).compareTo(
                    o2.substring(0, o2.lastIndexOf('\t')));
        }
    };

    private Formatter formatter = new MergeTask.Formatter() {

        @Override
        public void write(BufferedWriter writer, String... strings) throws IOException {
            if (strings.length == 0) {
                throw new IllegalArgumentException(
                        "Expecting one or more string arguments");
            } else if (strings.length == 1) {
                writer.write(strings[0]);
                writer.newLine();
            } else {

                String key = null;
                int total = 0;
                for (int i = 0; i < strings.length; i++) {
                    final int split = strings[i].lastIndexOf('\t');
                    String newKey = strings[i].substring(0, split);
                    if (key != null && !key.equals(newKey))
                        throw new AssertionError(
                                "String keys must match during merge format.");
                    key = newKey;
                    total += Integer.parseInt(strings[i].substring(split + 1));
                }
                writer.write(key);
                writer.write('\t');
                writer.write(Integer.toString(total));
                writer.newLine();
            }
        }
    };

    private Queue<File> mergeHeadQueue;

    private Queue<File> mergeContextQueue;

    private Queue<File> mergeFeatureQueue;

    public ExtCountTask(final File instancesFile, final File featuresFile,
            final File headsFile, final File contextsFile, Charset charset,
            int maxChunkSize) {
        this(instancesFile, featuresFile, headsFile, contextsFile);
        setCharset(charset);
        setMaxChunkSize(maxChunkSize);
    }

    public ExtCountTask(
            final File instancesFile, final File featuresFile,
            final File headsFile, final File contextsFile) {
        setInstancesFile(instancesFile);
        setFeaturesFile(featuresFile);
        setHeadsFile(headsFile);
        setContextsFile(contextsFile);
//        setSourceFile(src);
//        setDestinationFile(dst);
//        setCharset(charset);
    }

    public ExtCountTask() {
        super();
    }

    public final Charset getCharset() {
        return charset;
    }

    public final void setCharset(Charset charset) {
        Checks.checkNotNull(charset);
        this.charset = charset;
    }

    public final int getMaxChunkSize() {
        return maxChunkSize;
    }

    public final void setMaxChunkSize(int maxChunkSize) {
        if (maxChunkSize < 1)
            throw new IllegalArgumentException("maxChunkSize < 1");
        this.maxChunkSize = maxChunkSize;
    }
//
//    public final File getSrcFile() {
//        return sourceFile;
//    }
//
//    public final File getDestFile() {
//        return destFile;
//    }
//
//    public final void setSourceFile(final File sourceFile)
//            throws NullPointerException {
//        if (sourceFile == null)
//            throw new NullPointerException("sourceFile is null");
//        this.sourceFile = sourceFile;
//    }
//
//    public final void setDestinationFile(final File destFile)
//            throws NullPointerException {
//        if (destFile == null)
//            throw new NullPointerException("destinationFile is null");
//        this.destFile = destFile;
//    }

    public final File getContextsFile() {
        return contextsFile;
    }

    public final void setContextsFile(final File contextsFile)
            throws NullPointerException {
        if (contextsFile == null)
            throw new NullPointerException("contextsFile is null");
        this.contextsFile = contextsFile;
    }

    public final File getFeaturesFile() {
        return featuresFile;
    }

    public final void setFeaturesFile(final File featuresFile)
            throws NullPointerException {
        if (featuresFile == null)
            throw new NullPointerException("featuresFile is null");
        this.featuresFile = featuresFile;
    }

    public final File getHeadsFile() {
        return headsFile;
    }

    public final void setHeadsFile(final File headsFile)
            throws NullPointerException {
        if (headsFile == null)
            throw new NullPointerException("headsFile is null");
        this.headsFile = headsFile;
    }

    public File getInputFile() {
        return instancesFile;
    }

    public final void setInstancesFile(final File inputFile)
            throws NullPointerException {
        if (inputFile == null)
            throw new NullPointerException("sourceFile is null");
        this.instancesFile = inputFile;
    }

    @Override
    protected void runTask() throws Exception {

//        LOG.log(Level.INFO,
//                "Sorting file externally: from \"{0}\" to \"{1}\". ({2})",
//                new Object[]{getSrcFile(), getDestFile(),


        map();
        reduce();
        finish();
    }

    protected void map() throws Exception {

        mergeHeadQueue = new ArrayDeque<File>();
        mergeContextQueue = new ArrayDeque<File>();
        mergeFeatureQueue = new ArrayDeque<File>();

        BlockingQueue<File> chunkQueue = new ArrayBlockingQueue<File>(2);

        ChunkTask chunkTask = new ChunkTask(getInputFile(), getCharset(),
                getMaxChunkSize());
        chunkTask.setDstFileQueue(chunkQueue);
        chunkTask.setChunkFileFactory(tempFileFactory);
        Future<ChunkTask> chunkFuture = submitTask(chunkTask);

        // Immidiately poll the chunk task so we can start handling other
        // completed tasks
        if (!getFutureQueue().poll().equals(chunkFuture))
            throw new AssertionError("Expecting ChunkTask on future queue.");

        while (!chunkFuture.isDone() || !chunkQueue.isEmpty()) {
            if (!getFutureQueue().isEmpty() && getFutureQueue().peek().isDone()) {

                handleCompletedTask(getFutureQueue().poll().get());

            } else if (!chunkQueue.isEmpty()) {

                File chunk = chunkQueue.take();
                File chunk_h = new File(chunk.getPath() + ".h");
                File chunk_c = new File(chunk.getPath() + ".c");
                File chunk_f = new File(chunk.getPath() + ".f");
                submitTask(new MemCountTask(
                        chunk, chunk_f, chunk_h, chunk_c, getCharset()));

            }

            // Nasty hack to stop it tight looping when both queues are empty
            Thread.sleep(1);
        }
        chunkTask.throwException();
    }

    protected void reduce() throws Exception {
        while (!getFutureQueue().isEmpty()) {
            Task task = getFutureQueue().poll().get();
            handleCompletedTask(task);
        }
    }

    protected void handleCompletedTask(Task task) throws Exception {
        task.throwException();

        if (task.getClass().equals(MemCountTask.class)) {

            MemCountTask countTask = (MemCountTask) task;

            submitTask(new MemSortTask(countTask.getHeadsFile(),
                    countTask.getHeadsFile(), getCharset(), comparator));
            submitTask(new MemSortTask(countTask.getFeaturesFile(),
                    countTask.getFeaturesFile(), getCharset(), comparator));
            submitTask(new MemSortTask(countTask.getContextsFile(),
                    countTask.getContextsFile(), getCharset(), comparator));

            submitTask(new DeleteTask(countTask.getInputFile()));

        } else if (task.getClass().equals(MemSortTask.class)) {

            MemSortTask sortTask = (MemSortTask) task;
            File dst = sortTask.getDstFile();

            if (dst.getName().endsWith(".h"))
                queueMergeTask(dst, mergeHeadQueue);
            else if (dst.getName().endsWith(".f"))
                queueMergeTask(dst, mergeFeatureQueue);
            else if (dst.getName().endsWith(".c"))
                queueMergeTask(dst, mergeContextQueue);
            else
                throw new AssertionError();


        } else if (task.getClass().equals(MergeTask.class)) {

            MergeTask mergeTask = (MergeTask) task;

            File dst = mergeTask.getDestFile();

            if (dst.getName().endsWith(".h"))
                queueMergeTask(dst, mergeHeadQueue);
            else if (dst.getName().endsWith(".f"))
                queueMergeTask(dst, mergeFeatureQueue);
            else if (dst.getName().endsWith(".c"))
                queueMergeTask(dst, mergeContextQueue);

            submitTask(new DeleteTask(mergeTask.getSourceFileA()));
            submitTask(new DeleteTask(mergeTask.getSourceFileB()));

        } else if (task.getClass().equals(DeleteTask.class)) {
            // not a sausage
        } else {
            throw new AssertionError(
                    "Task type " + task.getClass()
                    + " should not have been queued.");
        }
    }

    protected void finish() throws Exception {

        {
            File finalMerge = mergeHeadQueue.poll();
            new CopyTask(finalMerge, getHeadsFile()).runTask();
            new DeleteTask(finalMerge).runTask();
        }
        {
            File finalMerge = mergeFeatureQueue.poll();
            new CopyTask(finalMerge, getFeaturesFile()).runTask();
            new DeleteTask(finalMerge).runTask();
        }
        {
            File finalMerge = mergeContextQueue.poll();
            new CopyTask(finalMerge, getContextsFile()).runTask();
            new DeleteTask(finalMerge).runTask();
        }
    }

    protected Future<MergeTask> queueMergeTask(File file, Queue<File> q) throws IOException {
        q.add(file);

        if (q.size() >= 2) {

            File result_x = tempFileFactory.createFile(); 

            File result = new File(result_x.getPath() + file.getName().substring(
                    file.getName().length() - 2));
            result_x.delete();
            
            MergeTask mergeTask = new MergeTask(
                    q.poll(), q.poll(), result,
                    getCharset());
            mergeTask.setComparator(comparator);
            mergeTask.setFormatter(formatter);
            return submitTask(mergeTask);
        } else {
            return null;
        }
    }
}
