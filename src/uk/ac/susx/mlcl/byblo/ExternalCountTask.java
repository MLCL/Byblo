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

import uk.ac.susx.mlcl.lib.io.TempFileFactoryConverter;
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
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Freqency count a structured input instance file.")
public class ExternalCountTask extends AbstractParallelTask {

    private static final Logger LOG = Logger.getLogger(
            ExternalCountTask.class.getName());

    private static final int DEFAULT_MAX_CHUNK_SIZE = ChunkTask.DEFAULT_MAX_CHUNK_SIZE;

    @Parameter(names = {"-C", "--chunk-size"},
               description = "Number of lines per work unit. Lrger value increase performance and memory usage.")
    private int maxChunkSize = DEFAULT_MAX_CHUNK_SIZE;

    @Parameter(names = {"-i", "--input"},
               required = true,
               description = "Input instances file")
    private File inputFile;

    @Parameter(names = {"-oef", "--output-entry-features"},
               required = true,
               description = "Output entry-feature frequencies file")
    private File entryFeaturesFile = null;

    @Parameter(names = {"-oe", "--output-entries"},
               required = true,
               description = "Output entry frequencies file")
    private File entriesFile = null;

    @Parameter(names = {"-of", "--output-features"},
               required = true,
               description = "Output feature frequencies file")
    private File featuresFile = null;

    @Parameter(names = {"-T", "--temporary-directory"},
               description = "Directory used for holding temporary files.",
               converter = TempFileFactoryConverter.class)
    private FileFactory tempFileFactory = new TempFileFactory("temp", ".txt");

    @Parameter(names = {"-c", "--charset"},
               description = "Character encoding to use for reading and writing files.")
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

    private static final String FEATURES_FILE_SUFFIX = ".f";

    private static final String ENTRY_FEATURES_FILE_SUFFIX = ".ef";

    private static final String ENTRIES_FILE_SUFFIX = ".e";

    private Queue<File> mergeEntryQueue;

    private Queue<File> mergeFeaturesQueue;

    private Queue<File> mergeEntryFeatureQueue;

    public ExternalCountTask(final File instancesFile, final File featuresFile,
            final File entriesFile, final File contextsFile, Charset charset,
            int maxChunkSize) {
        this(instancesFile, featuresFile, entriesFile, contextsFile);
        setCharset(charset);
        setMaxChunkSize(maxChunkSize);
    }

    public ExternalCountTask(
            final File instancesFile, final File entryFeaturesFile,
            final File entriesFile, final File featuresFile) {
        setInstancesFile(instancesFile);
        setEntryFeaturesFile(entryFeaturesFile);
        setEntriesFile(entriesFile);
        setFeaturesFile(featuresFile);
    }

    public ExternalCountTask() {
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

    public final File getFeaturesFile() {
        return featuresFile;
    }

    public final void setFeaturesFile(final File contextsFile)
            throws NullPointerException {
        if (contextsFile == null)
            throw new NullPointerException("contextsFile is null");
        this.featuresFile = contextsFile;
    }

    public final File getEntryFeaturesFile() {
        return entryFeaturesFile;
    }

    public final void setEntryFeaturesFile(final File featuresFile)
            throws NullPointerException {
        if (featuresFile == null)
            throw new NullPointerException("featuresFile is null");
        this.entryFeaturesFile = featuresFile;
    }

    public final File getEntriesFile() {
        return entriesFile;
    }

    public final void setEntriesFile(final File entriesFile)
            throws NullPointerException {
        if (entriesFile == null)
            throw new NullPointerException("entriesFile is null");
        this.entriesFile = entriesFile;
    }

    public File getInputFile() {
        return inputFile;
    }

    public final void setInstancesFile(final File inputFile)
            throws NullPointerException {
        if (inputFile == null)
            throw new NullPointerException("sourceFile is null");
        this.inputFile = inputFile;
    }

    @Override
    protected void runTask() throws Exception {
        map();
        reduce();
        finish();
    }

    protected void map() throws Exception {

        mergeEntryQueue = new ArrayDeque<File>();
        mergeFeaturesQueue = new ArrayDeque<File>();
        mergeEntryFeatureQueue = new ArrayDeque<File>();

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
                File chunk_entriesFile = new File(
                        chunk.getPath() + ENTRIES_FILE_SUFFIX);
                File chunk_featuresFile = new File(
                        chunk.getPath() + FEATURES_FILE_SUFFIX);
                File chunk_entryFeaturesFile = new File(
                        chunk.getPath() + ENTRY_FEATURES_FILE_SUFFIX);
                submitTask(new CountTask(chunk, chunk_entryFeaturesFile,
                        chunk_entriesFile, chunk_featuresFile, getCharset()));
            }

            // XXX: Nasty hack to stop it tight looping when both queues are empty
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

        if (task.getClass().equals(CountTask.class)) {

            CountTask countTask = (CountTask) task;

            submitTask(new SortTask(countTask.getEntriesFile(),
                    countTask.getEntriesFile(), getCharset(), comparator));
            submitTask(new SortTask(countTask.getEntryFeaturesFile(),
                    countTask.getEntryFeaturesFile(), getCharset(), comparator));
            submitTask(new SortTask(countTask.getFeaturesFile(),
                    countTask.getFeaturesFile(), getCharset(), comparator));

            submitTask(new DeleteTask(countTask.getInputFile()));

        } else if (task.getClass().equals(SortTask.class)) {

            SortTask sortTask = (SortTask) task;
            File dst = sortTask.getDstFile();

            if (dst.getName().endsWith(ENTRIES_FILE_SUFFIX))
                queueMergeTask(dst, mergeEntryQueue);
            else if (dst.getName().endsWith(ENTRY_FEATURES_FILE_SUFFIX))
                queueMergeTask(dst, mergeEntryFeatureQueue);
            else if (dst.getName().endsWith(FEATURES_FILE_SUFFIX))
                queueMergeTask(dst, mergeFeaturesQueue);
            else
                throw new AssertionError();


        } else if (task.getClass().equals(MergeTask.class)) {

            MergeTask mergeTask = (MergeTask) task;

            File dst = mergeTask.getDestFile();

            if (dst.getName().endsWith(ENTRIES_FILE_SUFFIX))
                queueMergeTask(dst, mergeEntryQueue);
            else if (dst.getName().endsWith(ENTRY_FEATURES_FILE_SUFFIX))
                queueMergeTask(dst, mergeEntryFeatureQueue);
            else if (dst.getName().endsWith(FEATURES_FILE_SUFFIX))
                queueMergeTask(dst, mergeFeaturesQueue);

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

        File finalMerge;

        finalMerge = mergeEntryQueue.poll();
        new CopyTask(finalMerge, getEntriesFile()).runTask();
        new DeleteTask(finalMerge).runTask();

        finalMerge = mergeEntryFeatureQueue.poll();
        new CopyTask(finalMerge, getEntryFeaturesFile()).runTask();
        new DeleteTask(finalMerge).runTask();

        finalMerge = mergeFeaturesQueue.poll();
        new CopyTask(finalMerge, getFeaturesFile()).runTask();
        new DeleteTask(finalMerge).runTask();
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