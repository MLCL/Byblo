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
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Flushable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratorType;
import uk.ac.susx.mlcl.byblo.io.BybloIO;
import uk.ac.susx.mlcl.byblo.io.Token;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.WeightSumReducerObjectSink;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSource;
import uk.ac.susx.mlcl.byblo.tasks.CountTask;
import uk.ac.susx.mlcl.lib.AbstractParallelCommandTask;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.commands.FileDeligate;
import uk.ac.susx.mlcl.lib.commands.FileMoveCommand;
import uk.ac.susx.mlcl.lib.commands.InputFileValidator;
import uk.ac.susx.mlcl.lib.commands.OutputFileValidator;
import uk.ac.susx.mlcl.lib.commands.TempFileFactoryConverter;
import uk.ac.susx.mlcl.lib.io.Chunk;
import uk.ac.susx.mlcl.lib.io.Chunker;
import uk.ac.susx.mlcl.lib.io.FileFactory;
import uk.ac.susx.mlcl.lib.io.SeekableObjectSource;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.ObjectSource;
import uk.ac.susx.mlcl.lib.io.Tell;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import uk.ac.susx.mlcl.lib.tasks.FileDeleteTask;
import uk.ac.susx.mlcl.lib.tasks.ObjectMergeTask;
import uk.ac.susx.mlcl.lib.tasks.ObjectSortTask;
import uk.ac.susx.mlcl.lib.tasks.Task;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Parameters(commandDescription = "Freqency count a structured input instance file.")
public class ExternalCountCommand extends AbstractParallelCommandTask {

    private static final Log LOG = LogFactory.getLog(ExternalCountCommand.class);

    protected static final String KEY_TASK_TYPE = "KEY_TASK_TYPE";

    protected static final String KEY_DATA_TYPE = "KEY_DATA_TYPE";

    protected static final String KEY_SRC_FILE = "KEY_SRC_FILE";

    protected static final String KEY_SRC_FILE_A = "KEY_SRC_FILE_A";

    protected static final String KEY_SRC_FILE_B = "KEY_SRC_FILE_B";

    protected static final String KEY_DST_FILE = "KEY_DST_FILE";

    protected static final String KEY_DST_ENTRIES_FILE = "KEY_DST_ENTRIES_FILE";

    protected static final String KEY_DST_FEATURES_FILE = "KEY_DST_FEATURES_FILE";

    protected static final String KEY_DST_EVENTS_FILE = "KEY_DST_EVENTS_FILE";

    protected static final String VALUE_TASK_TYPE_DELETE = "VALUE_TASK_TYPE_DELETE";

    protected static final String VALUE_TASK_TYPE_COUNT = "VALUE_TASK_TYPE_COUNT";

    protected static final String VALUE_TASK_TYPE_MERGE = "VALUE_TASK_TYPE_MERGE";

    protected static final String VALUE_TASK_TYPE_SORT = "VALUE_TASK_TYPE_SORT";

    protected static final String VALUE_DATA_TYPE_INPUT = "VALUE_DATA_TYPE_INPUT";

    protected static final String VALUE_DATA_TYPE_ENTRIES = "VALUE_DATA_TYPE_ENTRIES";

    protected static final String VALUE_DATA_TYPE_FEATURES = "VALUE_DATA_TYPE_FEATURES";

    protected static final String VALUE_DATA_TYPE_EVENTS = "VALUE_DATA_TYPE_EVENTS";

    private static final boolean DEBUG = false;

    @ParametersDelegate
    private DoubleEnumerating indexDeligate = new DoubleEnumeratingDeligate();

    @ParametersDelegate
    private FileDeligate fileDeligate = new FileDeligate();

    public static final int DEFAULT_MAX_CHUNK_SIE = 500000;

    @Parameter(names = {"-C", "--chunk-size"},
    description = "Number of lines per work unit. Larger value increase performance and memory usage.")
    private int maxChunkSize = DEFAULT_MAX_CHUNK_SIE;

    @Parameter(names = {"-i", "--input"},
    required = true,
    description = "Input instances file",
    validateWith = InputFileValidator.class)
    private File inputFile;

    @Parameter(names = {"-oef", "--output-entry-features"},
    required = true,
    description = "Output entry-feature frequencies file",
    validateWith = OutputFileValidator.class)
    private File eventsFile = null;

    @Parameter(names = {"-oe", "--output-entries"},
    required = true,
    description = "Output entry frequencies file",
    validateWith = OutputFileValidator.class)
    private File entriesFile = null;

    @Parameter(names = {"-of", "--output-features"},
    required = true,
    description = "Output feature frequencies file",
    validateWith = OutputFileValidator.class)
    private File featuresFile = null;

    @Parameter(names = {"-T", "--temporary-directory"},
    description = "Directory used for holding temporary files.",
    converter = TempFileFactoryConverter.class)
    private FileFactory tempFileFactory = new TempFileFactory();

    private Queue<File> mergeEntryQueue;

    private Queue<File> mergeFeaturesQueue;

    private Queue<File> mergeEventQueue;

    public ExternalCountCommand(
            final File instancesFile, final File eventsFile,
            final File entriesFile, final File featuresFile, Charset charset,
            DoubleEnumerating indexDeligate,
            int maxChunkSize) {
        this(instancesFile, eventsFile, entriesFile, featuresFile);
        fileDeligate.setCharset(charset);
        setMaxChunkSize(maxChunkSize);
        setIndexDeligate(indexDeligate);

    }

    public ExternalCountCommand(
            final File instancesFile, final File eventsFile,
            final File entriesFile, final File featuresFile,
            final Charset charset,
            DoubleEnumerating indexDeligate) {
        setInstancesFile(instancesFile);
        setEventsFile(eventsFile);
        setEntriesFile(entriesFile);
        setFeaturesFile(featuresFile);
        fileDeligate.setCharset(charset);
        setIndexDeligate(indexDeligate);
    }

    public ExternalCountCommand(
            final File instancesFile, final File eventsFile,
            final File entriesFile, final File featuresFile) {
        setInstancesFile(instancesFile);
        setEventsFile(eventsFile);
        setEntriesFile(entriesFile);
        setFeaturesFile(featuresFile);
    }

    public ExternalCountCommand() {
        super();
    }

    public FileDeligate getFileDeligate() {
        return fileDeligate;
    }

    public void setFileDeligate(FileDeligate fileDeligate) {
        this.fileDeligate = fileDeligate;
    }

    public final DoubleEnumerating getIndexDeligate() {
        return indexDeligate;
    }

    public final void setIndexDeligate(DoubleEnumerating indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }

    public FileFactory getTempFileFactory() {
        return tempFileFactory;
    }

    public void setTempFileFactory(FileFactory tempFileFactory) {
        Checks.checkNotNull("tempFileFactory", tempFileFactory);
        this.tempFileFactory = tempFileFactory;
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

    public final File getEventsFile() {
        return eventsFile;
    }

    public final void setEventsFile(final File featuresFile)
            throws NullPointerException {
        if (featuresFile == null)
            throw new NullPointerException("featuresFile is null");
        this.eventsFile = featuresFile;
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
    protected void initialiseTask() throws Exception {
        super.initialiseTask();
        checkState();
    }

    @Override
    protected void runTask() throws Exception {
        if (LOG.isInfoEnabled())
            LOG.info("Running external count on \"" + inputFile + "\".");

        map();
        reduce();
        finish();

        indexDeligate.saveEnumerator();
        indexDeligate.closeEnumerator();


        if (LOG.isInfoEnabled())
            LOG.info("Completed external count");

    }

    protected void map() throws Exception {

        mergeEntryQueue = new ArrayDeque<File>();
        mergeFeaturesQueue = new ArrayDeque<File>();
        mergeEventQueue = new ArrayDeque<File>();

        BlockingQueue<File> chunkQueue = new ArrayBlockingQueue<File>(2);


        final SeekableObjectSource<TokenPair, Tell> src =
                openInstancesSource(getInputFile());

        final ObjectSource<Chunk<TokenPair>> chunks =
                Chunker.newInstance(src, getMaxChunkSize());

        while (chunks.hasNext()) {
            while (!getFutureQueue().isEmpty() && getFutureQueue().peek().isDone()) {
                handleCompletedTask(getFutureQueue().poll().get());
            }

            Chunk<TokenPair> chunk = chunks.read();
//            submitCountTask(chunk, inputFile, eventsFile, inputFile);

            File chunk_entriesFile = tempFileFactory.createFile("cnt.ent.",
                                                                "");
            File chunk_featuresFile = tempFileFactory.createFile("cnt.feat.",
                                                                 "");
            File chunk_eventsFile = tempFileFactory.createFile(
                    "cnt.evnt.", "");

            submitCountTask(chunk, chunk_entriesFile, chunk_featuresFile,
                            chunk_eventsFile);
        }

    }

    protected void reduce() throws Exception {
        while (!getFutureQueue().isEmpty()) {
            Task task = getFutureQueue().poll().get();
            handleCompletedTask(task);
        }
    }

    protected void handleCompletedTask(Task task) throws Exception {
        while (task.isExceptionTrapped())
            task.throwTrappedException();

//        final Properties p = task.getProperties();
        final String taskType = task.getProperty(KEY_TASK_TYPE);
        final String dataType = task.getProperty(KEY_DATA_TYPE);

        if (task.isExceptionTrapped())
            task.throwTrappedException();

        if (taskType.equals(VALUE_TASK_TYPE_DELETE)) {
            // not a sausage
        } else if (taskType.equals(VALUE_TASK_TYPE_COUNT)) {

            CountTask countTask = (CountTask) task;
            if (countTask.getEntrySink() instanceof Flushable)
                ((Flushable) countTask.getEntrySink()).flush();
            if (countTask.getEntrySink() instanceof Closeable)
                ((Closeable) countTask.getEntrySink()).close();
            if (countTask.getFeatureSink() instanceof Flushable)
                ((Flushable) countTask.getFeatureSink()).flush();
            if (countTask.getFeatureSink() instanceof Closeable)
                ((Closeable) countTask.getFeatureSink()).close();
            if (countTask.getEventSink() instanceof Flushable)
                ((Flushable) countTask.getEventSink()).flush();
            if (countTask.getEventSink() instanceof Closeable)
                ((Closeable) countTask.getEventSink()).close();
            if (countTask.getSource() instanceof Closeable)
                ((Closeable) countTask.getSource()).close();

            submitSortEntriesTask(new File(
                    task.getProperty(KEY_DST_ENTRIES_FILE)));
            submitSortFeaturesTask(
                    new File(task.getProperty(KEY_DST_FEATURES_FILE)));
            submitSortEventsTask(new File(task.getProperty(KEY_DST_EVENTS_FILE)));

            File src = new File(task.getProperty(KEY_SRC_FILE));
            if (!DEBUG && !this.getInputFile().equals(src))
                submitDeleteTask(src);

        } else if (taskType.equals(VALUE_TASK_TYPE_SORT)) {

            final File src = new File(task.getProperty(KEY_SRC_FILE));
            final File dst = new File(task.getProperty(KEY_DST_FILE));

            ObjectSortTask<?> sortTask = (ObjectSortTask) task;
            if (sortTask.getSink() instanceof Flushable)
                ((Flushable) sortTask.getSink()).flush();
            if (sortTask.getSink() instanceof Closeable)
                ((Closeable) sortTask.getSink()).close();
            if (sortTask.getSource() instanceof Closeable)
                ((Closeable) sortTask.getSource()).close();

            if (dataType.equals(VALUE_DATA_TYPE_ENTRIES))
                submitMergeEntriesTask(dst);
            else if (dataType.equals(VALUE_DATA_TYPE_FEATURES))
                submitMergeFeaturesTask(dst);
            else if (dataType.equals(VALUE_DATA_TYPE_EVENTS))
                submitMergeEventsTask(dst);
            else
                throw new AssertionError();

            if (!DEBUG && !src.equals(dst))
                submitDeleteTask(src);

        } else if (taskType.equals(VALUE_TASK_TYPE_MERGE)) {

            final File srca = new File(task.getProperty(KEY_SRC_FILE_A));
            final File srcb = new File(task.getProperty(KEY_SRC_FILE_B));
            final File dst = new File(task.getProperty(KEY_DST_FILE));

            ObjectMergeTask<?> mergeTask = (ObjectMergeTask) task;
            if (mergeTask.getSink() instanceof Flushable)
                ((Flushable) mergeTask.getSink()).flush();
            if (mergeTask.getSink() instanceof Closeable)
                ((Closeable) mergeTask.getSink()).close();
            if (mergeTask.getSourceA() instanceof Closeable)
                ((Closeable) mergeTask.getSourceA()).close();
            if (mergeTask.getSourceB() instanceof Closeable)
                ((Closeable) mergeTask.getSourceB()).close();


            if (dataType.equals(VALUE_DATA_TYPE_ENTRIES))
                submitMergeEntriesTask(dst);
            else if (dataType.equals(VALUE_DATA_TYPE_FEATURES))
                submitMergeFeaturesTask(dst);
            else if (dataType.equals(VALUE_DATA_TYPE_EVENTS))
                submitMergeEventsTask(dst);
            else
                throw new AssertionError();

            if (!DEBUG) {
                submitDeleteTask(srca);
                submitDeleteTask(srcb);
            }

        } else {
            throw new AssertionError();
        }
    }

    protected void finish() throws Exception {
        checkState();

        File finalMerge;

        finalMerge = mergeEntryQueue.poll();
        if (finalMerge == null)
            throw new AssertionError(
                    "The entry merge queue is empty but final copy has not been completed.");
        new FileMoveCommand(finalMerge, getEntriesFile()).runCommand();

        finalMerge = mergeEventQueue.poll();
        if (finalMerge == null)
            throw new AssertionError(
                    "The entry/feature merge queue is empty but final copy has not been completed.");
        new FileMoveCommand(finalMerge, getEventsFile()).runCommand();

        finalMerge = mergeFeaturesQueue.poll();
        if (finalMerge == null)
            throw new AssertionError(
                    "The feature merge queue is empty but final copy has not been completed.");
        new FileMoveCommand(finalMerge, getFeaturesFile()).runCommand();

    }

    private Comparator<Weighted<Token>> getEntryOrder() throws IOException {
        return indexDeligate.isEnumeratedEntries()
               ? Weighted.recordOrder(Token.indexOrder())
               : Weighted.recordOrder(Token.stringOrder(indexDeligate.getEntriesEnumeratorCarriar()));
    }

    private Comparator<Weighted<Token>> getFeatureOrder() throws IOException {
        return indexDeligate.isEnumeratedFeatures()
               ? Weighted.recordOrder(Token.indexOrder())
               : Weighted.recordOrder(Token.stringOrder(indexDeligate.getFeaturesEnumeratorCarriar()));
    }

    private Comparator<Weighted<TokenPair>> getEventOrder() throws IOException {
        return (indexDeligate.isEnumeratedEntries() && indexDeligate.isEnumeratedFeatures())
               ? Weighted.recordOrder(TokenPair.indexOrder())
               : Weighted.recordOrder(TokenPair.stringOrder(indexDeligate));
    }

    protected void submitCountTask(ObjectSource<TokenPair> instanceSource,
                                   File outEntries, File outFeatures,
                                   File outEvents) throws IOException, InterruptedException {


//        Source<TokenPair> instanceSource = openInstancesSource(in);
        ObjectSink<Weighted<Token>> entrySink = openEntriesSink(outEntries);
        ObjectSink<Weighted<Token>> featureSink = openFeaturesSink(outFeatures);
        ObjectSink<Weighted<TokenPair>> eventsSink = openEventsSink(outEvents);

        CountTask task = new CountTask(
                instanceSource, eventsSink, entrySink, featureSink,
                getEventOrder(), getEntryOrder(), getFeatureOrder());

        task.setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_COUNT);

        task.setProperty(KEY_SRC_FILE, getInputFile().toString());
        task.setProperty(KEY_DST_EVENTS_FILE,
                         outEvents.toString());
        task.setProperty(KEY_DST_ENTRIES_FILE, outEntries.toString());
        task.setProperty(KEY_DST_FEATURES_FILE, outFeatures.toString());

        submitTask(task);
    }

    protected void submitDeleteTask(File file) throws InterruptedException {
        final FileDeleteTask deleteTask = new FileDeleteTask(file);
        deleteTask.setProperty(KEY_TASK_TYPE,
                               VALUE_TASK_TYPE_DELETE);
        submitTask(deleteTask);
    }

    private void submitSortEntriesTask(File file) throws IOException, InterruptedException {
        File srcFile = file;
        File dstFile = tempFileFactory.createFile("mrg.ent.", "");

        ObjectSource<Weighted<Token>> src = openEntriesSource(srcFile);
        ObjectSink<Weighted<Token>> snk = openEntriesSink(dstFile);

        ObjectSortTask<Weighted<Token>> task =
                new ObjectSortTask<Weighted<Token>>(src, snk);
        task.setComparator(Weighted.recordOrder(Token.indexOrder()));

        task.setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_SORT);
        task.setProperty(KEY_DATA_TYPE, VALUE_DATA_TYPE_ENTRIES);
        task.setProperty(KEY_SRC_FILE, srcFile.toString());
        task.setProperty(KEY_DST_FILE, dstFile.toString());
        submitTask(task);
    }

    private void submitSortFeaturesTask(File file) throws IOException, InterruptedException {
        File srcFile = file;
        File dstFile = tempFileFactory.createFile("mrg.feat.", "");

        ObjectSource<Weighted<Token>> src = openFeaturesSource(srcFile);
        ObjectSink<Weighted<Token>> snk = openFeaturesSink(dstFile);

        ObjectSortTask<Weighted<Token>> task =
                new ObjectSortTask<Weighted<Token>>(src, snk);
        task.setComparator(Weighted.recordOrder(Token.indexOrder()));

        task.setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_SORT);
        task.setProperty(KEY_DATA_TYPE, VALUE_DATA_TYPE_FEATURES);
        task.setProperty(KEY_SRC_FILE, srcFile.toString());
        task.setProperty(KEY_DST_FILE, dstFile.toString());
        submitTask(task);
    }

    private void submitSortEventsTask(File file) throws IOException, InterruptedException {
        File srcFile = file;
        File dstFile = tempFileFactory.createFile("mrg.feat.", "");

        ObjectSource<Weighted<TokenPair>> src = openEventsSource(srcFile);
        ObjectSink<Weighted<TokenPair>> snk = openEventsSink(dstFile);

        ObjectSortTask<Weighted<TokenPair>> task =
                new ObjectSortTask<Weighted<TokenPair>>(src, snk);
        task.setComparator(Weighted.recordOrder(TokenPair.indexOrder()));

        task.setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_SORT);
        task.setProperty(KEY_DATA_TYPE, VALUE_DATA_TYPE_EVENTS);
        task.setProperty(KEY_SRC_FILE, srcFile.toString());
        task.setProperty(KEY_DST_FILE, dstFile.toString());
        submitTask(task);
    }

    private void submitMergeEntriesTask(File dst) throws IOException, InterruptedException {
        mergeEntryQueue.add(dst);
        if (mergeEntryQueue.size() >= 2) {
            File srcFileA = mergeEntryQueue.poll();
            File srcFileB = mergeEntryQueue.poll();
            File dstFile = tempFileFactory.createFile("mrg.ent.", "");

            ObjectSource<Weighted<Token>> srcA = openEntriesSource(srcFileA);
            ObjectSource<Weighted<Token>> srcB = openEntriesSource(srcFileB);
            ObjectSink<Weighted<Token>> snk = openEntriesSink(dstFile);

            ObjectMergeTask<Weighted<Token>> task =
                    new ObjectMergeTask<Weighted<Token>>(srcA, srcB, snk);
            task.setComparator(Weighted.recordOrder(Token.indexOrder()));

            task.setProperty(KEY_TASK_TYPE,
                             VALUE_TASK_TYPE_MERGE);
            task.setProperty(KEY_DATA_TYPE,
                             VALUE_DATA_TYPE_ENTRIES);
            task.setProperty(KEY_SRC_FILE_A, srcFileA.toString());
            task.setProperty(KEY_SRC_FILE_B, srcFileB.toString());
            task.setProperty(KEY_DST_FILE, dstFile.toString());


            submitTask(task);
        }
    }

    private void submitMergeFeaturesTask(File dst) throws IOException, InterruptedException {
        mergeFeaturesQueue.add(dst);
        if (mergeFeaturesQueue.size() >= 2) {
            File srcFileA = mergeFeaturesQueue.poll();
            File srcFileB = mergeFeaturesQueue.poll();
            File dstFile = tempFileFactory.createFile("mrg.feat.", "");

            ObjectSource<Weighted<Token>> srcA = openFeaturesSource(srcFileA);
            ObjectSource<Weighted<Token>> srcB = openFeaturesSource(srcFileB);
            ObjectSink<Weighted<Token>> snk = openFeaturesSink(dstFile);

            ObjectMergeTask<Weighted<Token>> task =
                    new ObjectMergeTask<Weighted<Token>>(srcA, srcB, snk);
            task.setComparator(Weighted.recordOrder(Token.indexOrder()));

            task.setProperty(KEY_TASK_TYPE,
                             VALUE_TASK_TYPE_MERGE);
            task.setProperty(KEY_DATA_TYPE,
                             VALUE_DATA_TYPE_FEATURES);
            task.setProperty(KEY_SRC_FILE_A, srcFileA.toString());
            task.setProperty(KEY_SRC_FILE_B, srcFileB.toString());
            task.setProperty(KEY_DST_FILE, dstFile.toString());

            submitTask(task);
        }
    }

    private void submitMergeEventsTask(File dst) throws IOException, InterruptedException {
        mergeEventQueue.add(dst);
        if (mergeEventQueue.size() >= 2) {
            File srcFileA = mergeEventQueue.poll();
            File srcFileB = mergeEventQueue.poll();
            File dstFile = tempFileFactory.createFile("mrg.evnt.", "");

            ObjectSource<Weighted<TokenPair>> srcA = openEventsSource(srcFileA);
            ObjectSource<Weighted<TokenPair>> srcB = openEventsSource(srcFileB);
            ObjectSink<Weighted<TokenPair>> snk = openEventsSink(dstFile);

            ObjectMergeTask<Weighted<TokenPair>> task =
                    new ObjectMergeTask<Weighted<TokenPair>>(srcA, srcB, snk);
            task.setComparator(Weighted.recordOrder(TokenPair.indexOrder()));


            task.setProperty(KEY_TASK_TYPE,
                             VALUE_TASK_TYPE_MERGE);
            task.setProperty(KEY_DATA_TYPE,
                             VALUE_DATA_TYPE_EVENTS);
            task.setProperty(KEY_SRC_FILE_A, srcFileA.toString());
            task.setProperty(KEY_SRC_FILE_B, srcFileB.toString());
            task.setProperty(KEY_DST_FILE, dstFile.toString());


            submitTask(task);
        }
    }

    protected SeekableObjectSource<Weighted<Token>, Tell> openEntriesSource(File file)
            throws FileNotFoundException, IOException {
        return BybloIO.openEntriesSource(file, getCharset(), indexDeligate);
    }

    protected ObjectSink<Weighted<Token>> openEntriesSink(File file) throws FileNotFoundException, IOException {
        return new WeightSumReducerObjectSink<Token>(
                BybloIO.openEntriesSink(file, getCharset(), indexDeligate));
    }

    protected SeekableObjectSource<Weighted<Token>, Tell> openFeaturesSource(File file)
            throws FileNotFoundException, IOException {
        return BybloIO.openFeaturesSource(file, getCharset(), indexDeligate);
    }

    protected ObjectSink<Weighted<Token>> openFeaturesSink(File file) throws FileNotFoundException, IOException {
        return new WeightSumReducerObjectSink<Token>(
                BybloIO.openFeaturesSink(file, getCharset(), indexDeligate));
    }

    protected WeightedTokenPairSource openEventsSource(File file)
            throws FileNotFoundException, IOException {
        return BybloIO.openEventsSource(file, getCharset(), indexDeligate);
    }

    protected ObjectSink<Weighted<TokenPair>> openEventsSink(File file)
            throws FileNotFoundException, IOException {
        return new WeightSumReducerObjectSink<TokenPair>(
                BybloIO.openEventsSink(file, getCharset(), indexDeligate));
    }

    protected SeekableObjectSource<TokenPair, Tell> openInstancesSource(File file) throws FileNotFoundException, IOException {
        return BybloIO.openInstancesSource(file, getCharset(), indexDeligate);
    }

    protected ObjectSink<TokenPair> openInstancesSink(File file) throws FileNotFoundException, IOException {
        return BybloIO.openInstancesSink(file, getCharset(), indexDeligate);
    }

    /**
     * Method that performance a number of sanity checks on the parameterisation
     * of this class. It is necessary to do this because the the class can be
     * instantiated via a null constructor when run from the command line.
     *
     * @throws NullPointerException
     * @throws IllegalStateException
     * @throws FileNotFoundException
     */
    private void checkState() throws NullPointerException, IllegalStateException, FileNotFoundException {
        // Check non of the parameters are null
        if (inputFile == null)
            throw new NullPointerException("inputFile is null");
        if (eventsFile == null)
            throw new NullPointerException("eventsFile is null");
        if (featuresFile == null)
            throw new NullPointerException("featuresFile is null");
        if (entriesFile == null)
            throw new NullPointerException("entriesFile is null");

        // Check that no two files are the same
        if (inputFile.equals(eventsFile))
            throw new IllegalStateException("inputFile == featuresFile");
        if (inputFile.equals(featuresFile))
            throw new IllegalStateException("inputFile == contextsFile");
        if (inputFile.equals(entriesFile))
            throw new IllegalStateException("inputFile == entriesFile");
        if (eventsFile.equals(featuresFile))
            throw new IllegalStateException("eventsFile == featuresFile");
        if (eventsFile.equals(entriesFile))
            throw new IllegalStateException("eventsFile == entriesFile");
        if (featuresFile.equals(entriesFile))
            throw new IllegalStateException("featuresFile == entriesFile");


        // Check that the instances file exists and is readable
        if (!inputFile.exists())
            throw new FileNotFoundException(
                    "instances file does not exist: " + inputFile);
        if (!inputFile.isFile())
            throw new IllegalStateException(
                    "instances file is not a normal data file: " + inputFile);
        if (!inputFile.canRead())
            throw new IllegalStateException(
                    "instances file is not readable: " + inputFile);

        // For each output file, check that either it exists and it writeable,
        // or that it does not exist but is creatable
        if (entriesFile.exists() && (!entriesFile.isFile() || !entriesFile.canWrite()))
            throw new IllegalStateException(
                    "entries file exists but is not writable: " + entriesFile);
        if (!entriesFile.exists() && !entriesFile.getAbsoluteFile().
                getParentFile().
                canWrite()) {
            throw new IllegalStateException(
                    "entries file does not exists and can not be reated: " + entriesFile);
        }
        if (featuresFile.exists() && (!featuresFile.isFile() || !featuresFile.canWrite()))
            throw new IllegalStateException(
                    "features file exists but is not writable: " + featuresFile);
        if (!featuresFile.exists() && !featuresFile.getAbsoluteFile().
                getParentFile().
                canWrite()) {
            throw new IllegalStateException(
                    "features file does not exists and can not be reated: " + featuresFile);
        }
        if (eventsFile.exists() && (!eventsFile.isFile() || !eventsFile.canWrite()))
            throw new IllegalStateException(
                    "entry-features file exists but is not writable: " + eventsFile);
        if (!eventsFile.exists() && !eventsFile.getAbsoluteFile().
                getParentFile().
                canWrite()) {
            throw new IllegalStateException(
                    "entry-features file does not exists and can not be reated: " + eventsFile);
        }
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("in", inputFile).
                add("entriesOut", entriesFile).
                add("featuresOut", featuresFile).
                add("eventsOut", eventsFile).
                add("tempDir", tempFileFactory).
                add("fd", getFileDeligate()).
                add("id", getIndexDeligate());
    }

    public void setEnumeratedFeatures(boolean enumeratedFeatures) {
        indexDeligate.setEnumeratedFeatures(enumeratedFeatures);
    }

    public void setEnumeratedEntries(boolean enumeratedEntries) {
        indexDeligate.setEnumeratedEntries(enumeratedEntries);
    }

    public boolean isEnumeratedFeatures() {
        return indexDeligate.isEnumeratedFeatures();
    }

    public boolean isEnumeratedEntries() {
        return indexDeligate.isEnumeratedEntries();
    }

    public final void setCharset(Charset charset) {
        fileDeligate.setCharset(charset);
    }

    public final Charset getCharset() {
        return fileDeligate.getCharset();
    }

    public void setEnumeratorType(EnumeratorType type) {
        indexDeligate.setEnumeratorType(type);
    }

}
