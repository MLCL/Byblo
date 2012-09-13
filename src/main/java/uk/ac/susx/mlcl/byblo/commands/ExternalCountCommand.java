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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratorType;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.byblo.tasks.CountTask;
import uk.ac.susx.mlcl.lib.AbstractParallelCommandTask;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.MiscUtil;
import uk.ac.susx.mlcl.lib.commands.*;
import uk.ac.susx.mlcl.lib.events.ProgressAggregate;
import uk.ac.susx.mlcl.lib.events.ProgressListener;
import uk.ac.susx.mlcl.lib.events.ProgressReporting;
import uk.ac.susx.mlcl.lib.events.ReportLoggingProgressListener;
import uk.ac.susx.mlcl.lib.io.*;
import uk.ac.susx.mlcl.lib.tasks.FileDeleteTask;
import uk.ac.susx.mlcl.lib.tasks.ObjectMergeTask;
import uk.ac.susx.mlcl.lib.tasks.ObjectSortTask;
import uk.ac.susx.mlcl.lib.tasks.Task;

import java.io.*;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Future;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Parameters(commandDescription = "Frequency count a structured input instance file.")
public class ExternalCountCommand extends AbstractParallelCommandTask implements ProgressReporting {

    private static final Log LOG = LogFactory.getLog(ExternalCountCommand.class);

    private static final String KEY_TASK_TYPE = "KEY_TASK_TYPE";

    private static final String KEY_DATA_TYPE = "KEY_DATA_TYPE";

    private static final String KEY_SRC_FILE = "KEY_SRC_FILE";

    private static final String KEY_SRC_FILE_A = "KEY_SRC_FILE_A";

    private static final String KEY_SRC_FILE_B = "KEY_SRC_FILE_B";

    private static final String KEY_DST_FILE = "KEY_DST_FILE";

    private static final String KEY_DST_ENTRIES_FILE = "KEY_DST_ENTRIES_FILE";

    private static final String KEY_DST_FEATURES_FILE = "KEY_DST_FEATURES_FILE";

    private static final String KEY_DST_EVENTS_FILE = "KEY_DST_EVENTS_FILE";

    private static final String VALUE_TASK_TYPE_DELETE = "VALUE_TASK_TYPE_DELETE";

    private static final String VALUE_TASK_TYPE_COUNT = "VALUE_TASK_TYPE_COUNT";

    private static final String VALUE_TASK_TYPE_MERGE = "VALUE_TASK_TYPE_MERGE";

    private static final String VALUE_TASK_TYPE_SORT = "VALUE_TASK_TYPE_SORT";

    protected static final String VALUE_DATA_TYPE_INPUT = "VALUE_DATA_TYPE_INPUT";

    private static final String VALUE_DATA_TYPE_ENTRIES = "VALUE_DATA_TYPE_ENTRIES";

    private static final String VALUE_DATA_TYPE_FEATURES = "VALUE_DATA_TYPE_FEATURES";

    private static final String VALUE_DATA_TYPE_EVENTS = "VALUE_DATA_TYPE_EVENTS";

    private static final boolean DEBUG = false;

    private final ProgressAggregate progress = new ProgressAggregate(this);

    @ParametersDelegate
    private DoubleEnumerating indexDelegate = new DoubleEnumeratingDelegate();

    @ParametersDelegate
    private FileDelegate fileDelegate = new FileDelegate();

    @Parameter(names = {"-i", "--input"}, required = true,
            description = "Input instances file", validateWith = InputFileValidator.class)
    private File inputFile;

    @Parameter(names = {"-oef", "--output-entry-features"}, required = true,
            description = "Output entry-feature frequencies file", validateWith = OutputFileValidator.class)
    private File eventsFile = null;

    @Parameter(names = {"-oe", "--output-entries"}, required = true,
            description = "Output entry frequencies file", validateWith = OutputFileValidator.class)
    private File entriesFile = null;

    @Parameter(names = {"-of", "--output-features"}, required = true,
            description = "Output feature frequencies file", validateWith = OutputFileValidator.class)
    private File featuresFile = null;

    @Parameter(names = {"-T", "--temporary-directory"},
            description = "Directory used for holding temporary files.", converter = TempFileFactoryConverter.class)
    private FileFactory tempFileFactory = new TempFileFactory();

    private Queue<File> mergeEntryQueue;

    private Queue<File> mergeFeaturesQueue;

    private Queue<File> mergeEventQueue;

    public ExternalCountCommand(final File instancesFile,
                                final File eventsFile, final File entriesFile,
                                final File featuresFile, Charset charset,
                                DoubleEnumerating indexDelegate) {
        this(instancesFile, eventsFile, entriesFile, featuresFile);
        fileDelegate.setCharset(charset);
        setIndexDelegate(indexDelegate);

    }

    public ExternalCountCommand(final File instancesFile,
                                final File eventsFile, final File entriesFile,
                                final File featuresFile) {
        setInstancesFile(instancesFile);
        setEventsFile(eventsFile);
        setEntriesFile(entriesFile);
        setFeaturesFile(featuresFile);
    }

    public ExternalCountCommand() {
        super();
    }

    public FileDelegate getFileDelegate() {
        return fileDelegate;
    }

    public void setFileDelegate(FileDelegate fileDelegate) {
        this.fileDelegate = fileDelegate;
    }

    final DoubleEnumerating getIndexDelegate() {
        return indexDelegate;
    }

    public final void setIndexDelegate(DoubleEnumerating indexDelegate) {
        Checks.checkNotNull("indexDelegate", indexDelegate);
        this.indexDelegate = indexDelegate;
    }

    public FileFactory getTempFileFactory() {
        return tempFileFactory;
    }

    public void setTempFileFactory(FileFactory tempFileFactory) {
        Checks.checkNotNull("tempFileFactory", tempFileFactory);
        this.tempFileFactory = tempFileFactory;
    }

    final File getFeaturesFile() {
        return featuresFile;
    }

    public final void setFeaturesFile(final File contextsFile)
            throws NullPointerException {
        if (contextsFile == null)
            throw new NullPointerException("contextsFile is null");
        this.featuresFile = contextsFile;
    }

    final File getEventsFile() {
        return eventsFile;
    }

    public final void setEventsFile(final File featuresFile)
            throws NullPointerException {
        if (featuresFile == null)
            throw new NullPointerException("featuresFile is null");
        this.eventsFile = featuresFile;
    }

    final File getEntriesFile() {
        return entriesFile;
    }

    public final void setEntriesFile(final File entriesFile)
            throws NullPointerException {
        if (entriesFile == null)
            throw new NullPointerException("entriesFile is null");
        this.entriesFile = entriesFile;
    }

    File getInputFile() {
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

        progress.addProgressListener(new ReportLoggingProgressListener(LOG));


        progress.startAdjusting();
        progress.setState(State.RUNNING);
        progress.setMessage("Mapping to small count tasks");
        progress.endAdjusting();

        map();

        progress.setMessage("Merging and aggregating results");

        clearCompleted(true);
        finish();

        if (indexDelegate.isEnumeratorOpen()) {
            indexDelegate.saveEnumerator();
            indexDelegate.closeEnumerator();
        }

        progress.setState(State.COMPLETED);

    }

    void clearCompleted(boolean block) throws Exception {

        if (block) {
            while (!getFutureQueue().isEmpty()) {
                Task task = getFutureQueue().poll().get();
                handleCompletedTask(task);
            }
        } else {

            List<Future<? extends Task>> completed = null;
            for (Future<? extends Task> future : getFutureQueue()) {
                if (future.isDone()) {
                    if (completed == null)
                        completed = new ArrayList<Future<? extends Task>>();
                    completed.add(future);
                }
            }

            if (completed != null && !completed.isEmpty()) {
                getFutureQueue().removeAll(completed);
                for (Future<? extends Task> future : completed) {
                    handleCompletedTask(future.get());
                }
            }
        }

    }

    void map() throws Exception {

        mergeEntryQueue = new ArrayDeque<File>();
        mergeFeaturesQueue = new ArrayDeque<File>();
        mergeEventQueue = new ArrayDeque<File>();

        final SeekableObjectSource<TokenPair, Tell> src = openInstancesSource(getInputFile());

        final int maxChunkSize = estimateMaxChunkSize();
        LOG.info(MessageFormat.format("Estimated maximum chunk size: {0}", maxChunkSize));

        final ObjectSource<Chunk<TokenPair>> chunks = Chunker.newInstance(src, maxChunkSize);

        int chunkCount = 0;
        while (chunks.hasNext()) {

            clearCompleted(false);

            Chunk<TokenPair> chunk = chunks.read();

            File chunk_entriesFile = tempFileFactory.createFile("cnt.ent.", "");
            File chunk_featuresFile = tempFileFactory.createFile("cnt.feat.",
                    "");
            File chunk_eventsFile = tempFileFactory.createFile("cnt.evnt.", "");

            ++chunkCount;

            submitCountTask(chunk, chunk_entriesFile, chunk_featuresFile,
                    chunk_eventsFile);
        }

    }

    void handleCompletedTask(Task task) throws Exception {
        while (task.isExceptionTrapped())
            task.throwTrappedException();

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

            submitSortEntriesTask(new File(task.getProperty(KEY_DST_ENTRIES_FILE)));
            submitSortFeaturesTask(new File(task.getProperty(KEY_DST_FEATURES_FILE)));
            submitSortEventsTask(new File(task.getProperty(KEY_DST_EVENTS_FILE)));

            File src = new File(task.getProperty(KEY_SRC_FILE));
            if (!DEBUG && !this.getInputFile().equals(src))
                submitDeleteTask(src);

        } else if (taskType.equals(VALUE_TASK_TYPE_SORT)) {

            final File src = new File(task.getProperty(KEY_SRC_FILE));
            final File dst = new File(task.getProperty(KEY_DST_FILE));

            ObjectSortTask<?> sortTask = (ObjectSortTask<?>) task;
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

            final File srcA = new File(task.getProperty(KEY_SRC_FILE_A));
            final File srcB = new File(task.getProperty(KEY_SRC_FILE_B));
            final File dst = new File(task.getProperty(KEY_DST_FILE));

            ObjectMergeTask<?> mergeTask = (ObjectMergeTask<?>) task;

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
                submitDeleteTask(srcA);
                submitDeleteTask(srcB);
            }

        } else {
            throw new AssertionError();
        }
    }

    void finish() throws Exception {
        checkState();

        File finalMerge;

        finalMerge = mergeEntryQueue.poll();
        if (finalMerge == null)
            throw new AssertionError(
                    "The entry merge queue is empty but final copy has not been completed.");

        if (!new FileMoveCommand(finalMerge, getEntriesFile()).runCommand())
            throw new RuntimeException("file move failed");

        finalMerge = mergeEventQueue.poll();
        if (finalMerge == null)
            throw new AssertionError(
                    "The entry/feature merge queue is empty but final copy has not been completed.");
        if (!new FileMoveCommand(finalMerge, getEventsFile()).runCommand())
            throw new RuntimeException("file move failed");

        finalMerge = mergeFeaturesQueue.poll();
        if (finalMerge == null)
            throw new AssertionError(
                    "The feature merge queue is empty but final copy has not been completed.");
        if (!new FileMoveCommand(finalMerge, getFeaturesFile()).runCommand())
            throw new RuntimeException("file move failed");


    }

    private Comparator<Weighted<Token>> getEntryOrder() {
        return indexDelegate.isEnumeratedEntries() ? Weighted.recordOrder(Token
                .indexOrder()) : Weighted.recordOrder(Token
                .stringOrder(indexDelegate.getEntriesEnumeratorCarrier()));
    }

    private Comparator<Weighted<Token>> getFeatureOrder()  {
        return indexDelegate.isEnumeratedFeatures() ? Weighted.recordOrder(Token.indexOrder())
                : Weighted.recordOrder(Token.stringOrder(indexDelegate.getFeaturesEnumeratorCarrier()));
    }

    private Comparator<Weighted<TokenPair>> getEventOrder() {
        return (indexDelegate.isEnumeratedEntries()
                && indexDelegate.isEnumeratedFeatures())
                ? Weighted.recordOrder(TokenPair.indexOrder())
                : Weighted.recordOrder(TokenPair.stringOrder(indexDelegate));
    }

    void submitCountTask(ObjectSource<TokenPair> instanceSource,
                         File outEntries, File outFeatures, File outEvents)
            throws IOException, InterruptedException {

        ObjectSink<Weighted<Token>> entrySink = openEntriesSink(outEntries);
        ObjectSink<Weighted<Token>> featureSink = openFeaturesSink(outFeatures);
        ObjectSink<Weighted<TokenPair>> eventsSink = openEventsSink(outEvents);

        CountTask task = new CountTask(instanceSource, eventsSink, entrySink,
                featureSink, getEventOrder(), getEntryOrder(), getFeatureOrder());

        task.setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_COUNT);

        task.setProperty(KEY_SRC_FILE, getInputFile().toString());
        task.setProperty(KEY_DST_EVENTS_FILE, outEvents.toString());
        task.setProperty(KEY_DST_ENTRIES_FILE, outEntries.toString());
        task.setProperty(KEY_DST_FEATURES_FILE, outFeatures.toString());

        progress.addChildProgressReporter(task);

        submitTask(task);
    }

    void submitDeleteTask(File file) throws InterruptedException {
        final FileDeleteTask deleteTask = new FileDeleteTask(file);
        deleteTask.setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_DELETE);
        submitTask(deleteTask);
    }

    private void submitSortEntriesTask(final File srcFile) throws IOException,
            InterruptedException {
        File dstFile = tempFileFactory.createFile("mrg.ent.", "");

        ObjectSource<Weighted<Token>> src = openEntriesSource(srcFile);
        ObjectSink<Weighted<Token>> snk = openEntriesSink(dstFile);

        ObjectSortTask<Weighted<Token>> task = new ObjectSortTask<Weighted<Token>>(src, snk);
        task.setComparator(Weighted.recordOrder(Token.indexOrder()));

        task.setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_SORT);
        task.setProperty(KEY_DATA_TYPE, VALUE_DATA_TYPE_ENTRIES);
        task.setProperty(KEY_SRC_FILE, srcFile.toString());
        task.setProperty(KEY_DST_FILE, dstFile.toString());

        progress.addChildProgressReporter(task);

        submitTask(task);
    }

    private void submitSortFeaturesTask(final File srcFile) throws IOException,
            InterruptedException {
        File dstFile = tempFileFactory.createFile("mrg.feat.", "");

        ObjectSource<Weighted<Token>> src = openFeaturesSource(srcFile);
        ObjectSink<Weighted<Token>> snk = openFeaturesSink(dstFile);

        ObjectSortTask<Weighted<Token>> task = new ObjectSortTask<Weighted<Token>>(src, snk);
        task.setComparator(Weighted.recordOrder(Token.indexOrder()));

        task.setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_SORT);
        task.setProperty(KEY_DATA_TYPE, VALUE_DATA_TYPE_FEATURES);
        task.setProperty(KEY_SRC_FILE, srcFile.toString());
        task.setProperty(KEY_DST_FILE, dstFile.toString());

        progress.addChildProgressReporter(task);

        submitTask(task);
    }

    private void submitSortEventsTask(File srcFile) throws IOException,
            InterruptedException {
        File dstFile = tempFileFactory.createFile("mrg.feat.", "");

        ObjectSource<Weighted<TokenPair>> src = openEventsSource(srcFile);
        ObjectSink<Weighted<TokenPair>> snk = openEventsSink(dstFile);

        ObjectSortTask<Weighted<TokenPair>> task = new ObjectSortTask<Weighted<TokenPair>>(src, snk);
        task.setComparator(Weighted.recordOrder(TokenPair.indexOrder()));

        task.setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_SORT);
        task.setProperty(KEY_DATA_TYPE, VALUE_DATA_TYPE_EVENTS);
        task.setProperty(KEY_SRC_FILE, srcFile.toString());
        task.setProperty(KEY_DST_FILE, dstFile.toString());

        progress.addChildProgressReporter(task);

        submitTask(task);
    }

    private void submitMergeEntriesTask(File dst) throws IOException,
            InterruptedException {
        mergeEntryQueue.add(dst);
        if (mergeEntryQueue.size() >= 2) {
            File srcFileA = mergeEntryQueue.poll();
            File srcFileB = mergeEntryQueue.poll();
            File dstFile = tempFileFactory.createFile("mrg.ent.", "");

            ObjectSource<Weighted<Token>> srcA = openEntriesSource(srcFileA);
            ObjectSource<Weighted<Token>> srcB = openEntriesSource(srcFileB);
            ObjectSink<Weighted<Token>> snk = openEntriesSink(dstFile);

            ObjectMergeTask<Weighted<Token>> task = new ObjectMergeTask<Weighted<Token>>(srcA, srcB, snk);
            task.setComparator(Weighted.recordOrder(Token.indexOrder()));

            task.setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_MERGE);
            task.setProperty(KEY_DATA_TYPE, VALUE_DATA_TYPE_ENTRIES);
            task.setProperty(KEY_SRC_FILE_A, srcFileA.toString());
            task.setProperty(KEY_SRC_FILE_B, srcFileB.toString());
            task.setProperty(KEY_DST_FILE, dstFile.toString());

            progress.addChildProgressReporter(task);

            submitTask(task);
        }
    }

    private void submitMergeFeaturesTask(File dst) throws IOException,
            InterruptedException {
        mergeFeaturesQueue.add(dst);
        if (mergeFeaturesQueue.size() >= 2) {
            File srcFileA = mergeFeaturesQueue.poll();
            File srcFileB = mergeFeaturesQueue.poll();
            File dstFile = tempFileFactory.createFile("mrg.feat.", "");

            ObjectSource<Weighted<Token>> srcA = openFeaturesSource(srcFileA);
            ObjectSource<Weighted<Token>> srcB = openFeaturesSource(srcFileB);
            ObjectSink<Weighted<Token>> snk = openFeaturesSink(dstFile);

            ObjectMergeTask<Weighted<Token>> task = new ObjectMergeTask<Weighted<Token>>(srcA, srcB, snk);
            task.setComparator(Weighted.recordOrder(Token.indexOrder()));

            task.setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_MERGE);
            task.setProperty(KEY_DATA_TYPE, VALUE_DATA_TYPE_FEATURES);
            task.setProperty(KEY_SRC_FILE_A, srcFileA.toString());
            task.setProperty(KEY_SRC_FILE_B, srcFileB.toString());
            task.setProperty(KEY_DST_FILE, dstFile.toString());

            progress.addChildProgressReporter(task);

            submitTask(task);
        }
    }

    private void submitMergeEventsTask(File dst) throws IOException,
            InterruptedException {
        mergeEventQueue.add(dst);
        if (mergeEventQueue.size() >= 2) {
            File srcFileA = mergeEventQueue.poll();
            File srcFileB = mergeEventQueue.poll();
            File dstFile = tempFileFactory.createFile("mrg.evnt.", "");

            ObjectSource<Weighted<TokenPair>> srcA = openEventsSource(srcFileA);
            ObjectSource<Weighted<TokenPair>> srcB = openEventsSource(srcFileB);
            ObjectSink<Weighted<TokenPair>> snk = openEventsSink(dstFile);

            ObjectMergeTask<Weighted<TokenPair>> task = new ObjectMergeTask<Weighted<TokenPair>>(srcA, srcB, snk);
            task.setComparator(Weighted.recordOrder(TokenPair.indexOrder()));

            task.setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_MERGE);
            task.setProperty(KEY_DATA_TYPE, VALUE_DATA_TYPE_EVENTS);
            task.setProperty(KEY_SRC_FILE_A, srcFileA.toString());
            task.setProperty(KEY_SRC_FILE_B, srcFileB.toString());
            task.setProperty(KEY_DST_FILE, dstFile.toString());

            progress.addChildProgressReporter(task);

            submitTask(task);
        }
    }

    SeekableObjectSource<Weighted<Token>, Tell> openEntriesSource(
            File file) throws IOException {
        return BybloIO.openEntriesSource(file, getCharset(), indexDelegate);
    }

    ObjectSink<Weighted<Token>> openEntriesSink(File file)
            throws IOException {
        return new WeightSumReducerObjectSink<Token>(BybloIO.openEntriesSink(
                file, getCharset(), indexDelegate));
    }

    SeekableObjectSource<Weighted<Token>, Tell> openFeaturesSource(
            File file) throws IOException {
        return BybloIO.openFeaturesSource(file, getCharset(), indexDelegate);
    }

    ObjectSink<Weighted<Token>> openFeaturesSink(File file)
            throws IOException {
        return new WeightSumReducerObjectSink<Token>(BybloIO.openFeaturesSink(
                file, getCharset(), indexDelegate));
    }

    WeightedTokenPairSource openEventsSource(File file)
            throws IOException {
        return BybloIO.openEventsSource(file, getCharset(), indexDelegate);
    }

    ObjectSink<Weighted<TokenPair>> openEventsSink(File file)
            throws IOException {
        return new WeightSumReducerObjectSink<TokenPair>(
                BybloIO.openEventsSink(file, getCharset(), indexDelegate));
    }

    SeekableObjectSource<TokenPair, Tell> openInstancesSource(
            File file) throws IOException {
        return BybloIO.openInstancesSource(file, getCharset(), indexDelegate);
    }

    protected ObjectSink<TokenPair> openInstancesSink(File file)
            throws IOException {
        return BybloIO.openInstancesSink(file, getCharset(), indexDelegate);
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
    private void checkState() throws NullPointerException,
            IllegalStateException, FileNotFoundException {
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
            throw new FileNotFoundException("instances file does not exist: " + inputFile);
        if (!inputFile.isFile())
            throw new IllegalStateException("instances file is not a normal data file: " + inputFile);
        if (!inputFile.canRead())
            throw new IllegalStateException("instances file is not readable: " + inputFile);

        // For each output file, check that either it exists and it writable,
        // or that it does not exist but is creatable
        if (entriesFile.exists()
                && (!entriesFile.isFile() || !entriesFile.canWrite()))
            throw new IllegalStateException("entries file exists but is not writable: " + entriesFile);
        if (!entriesFile.exists()
                && !entriesFile.getAbsoluteFile().getParentFile().canWrite()) {
            throw new IllegalStateException("entries file does not exists and can not be created: " + entriesFile);
        }
        if (featuresFile.exists()
                && (!featuresFile.isFile() || !featuresFile.canWrite()))
            throw new IllegalStateException("features file exists but is not writable: " + featuresFile);
        if (!featuresFile.exists()
                && !featuresFile.getAbsoluteFile().getParentFile().canWrite()) {
            throw new IllegalStateException("features file does not exists and can not be created: " + featuresFile);
        }
        if (eventsFile.exists()
                && (!eventsFile.isFile() || !eventsFile.canWrite()))
            throw new IllegalStateException("entry-features file exists but is not writable: " + eventsFile);
        if (!eventsFile.exists()
                && !eventsFile.getAbsoluteFile().getParentFile().canWrite()) {
            throw new IllegalStateException("entry-features file does not exists and can not be created: " + eventsFile);
        }
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().add("in", inputFile)
                .add("entriesOut", entriesFile)
                .add("featuresOut", featuresFile).add("eventsOut", eventsFile)
                .add("tempDir", tempFileFactory).add("fd", getFileDelegate())
                .add("id", getIndexDelegate());
    }

    public void setEnumeratedFeatures(boolean enumeratedFeatures) {
        indexDelegate.setEnumeratedFeatures(enumeratedFeatures);
    }

    public void setEnumeratedEntries(boolean enumeratedEntries) {
        indexDelegate.setEnumeratedEntries(enumeratedEntries);
    }

    public boolean isEnumeratedFeatures() {
        return indexDelegate.isEnumeratedFeatures();
    }

    public boolean isEnumeratedEntries() {
        return indexDelegate.isEnumeratedEntries();
    }

    public final void setCharset(Charset charset) {
        fileDelegate.setCharset(charset);
    }

    final Charset getCharset() {
        return fileDelegate.getCharset();
    }

    public void setEnumeratorType(EnumeratorType type) {
        indexDelegate.setEnumeratorType(type);
    }

    @Override
    public String getName() {
        return "ExternalCount";
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
    public int getProgressPercent() {
        return progress.getProgressPercent();
    }

    @Override
    public ProgressListener[] getProgressListeners() {
        return progress.getProgressListeners();
    }

    @Override
    public void addProgressListener(ProgressListener progressListener) {
        progress.addProgressListener(progressListener);
    }

    @Override
    public String getProgressReport() {
        return progress.getProgressReport();
    }

    @Override
    public State getState() {
        return progress.getState();
    }

    /**
     * Calculate a conservative guess at the maximum chunk size we can get away
     * with given the available memory, and number of simultaneous threads.
     * <p/>
     * History: In previous version the end user was expected to set this value,
     * which obviously was a total disaster. Most wouldn't both (because they
     * didn't know what it was) and result was usually either code running too
     * slowly, or java running our of heap space.
     *
     * @return maximum number of events that should be loaded per worker
     */
    private int estimateMaxChunkSize() {
        // Start by at least trying to GC whatever junk is lying around
        System.gc();
        final long bytesAvailable = MiscUtil.freeMaxMemory();
        final int numTasks = (getNumThreads() + PRELOAD_SIZE);
        int chunkSize = (int) (bytesAvailable / (CountTask.BYTES_REQUIRED_PER_EVENT * numTasks));
        int maxChunkSize = 5000000;
        // In some system we might expect a very large amount of available
        // memory. In this case we shouldn't set chunk size too large or it
        // will never parallelise.
        return Math.min(chunkSize, maxChunkSize);
    }
}
