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
package uk.ac.susx.mlcl.dttools;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import uk.ac.susx.mlcl.dttools.io.ContextEntry;
import uk.ac.susx.mlcl.dttools.io.ContextSink;
import uk.ac.susx.mlcl.dttools.io.FeatureEntry;
import uk.ac.susx.mlcl.dttools.io.FeatureSink;
import uk.ac.susx.mlcl.dttools.io.HeadEntry;
import uk.ac.susx.mlcl.dttools.io.HeadSink;
import uk.ac.susx.mlcl.dttools.io.InstanceEntry;
import uk.ac.susx.mlcl.dttools.io.InstanceSource;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.MiscUtil;
import uk.ac.susx.mlcl.lib.ObjectIndex;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.lib.tasks.AbstractTask;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Read in a raw feature instances file, to produce three frequency files:
 * heads, contexts, and features.</p>
 *
 * <p>The input instances file contains one record per line. Each line consists
 * of two tab-delimited values: a head string, and a context string. Each pair
 * represents a single observation of a relation, where head is the thing being
 * described, and context is the thing describing it.</p>
 * <pre>
 *      head1    context1
 *      head2    context2
 *      head3    context3
 *      ...      ...
 * </pre>
 * <p>The output heads file contains one record per line. Each line consists of
 * four values: a head string, a unique integer id of the head string, the width
 * of the head (i.e the number of unique contexts it is described by), and the
 * frequency (total number of occurrences).</p>
 * <pre>
 *      head1    id1    width1    freq1
 *      head2    id2    width2    freq2
 *      head3    id3    width3    freq3
 *      ...      ...    ...       ...
 * </pre>
 * <p>The output contexts file contains one record per line. Each line consists
 * of two values: a context string, and the frequency.</p>
 * <pre>
 *      context1    freq1
 *      context2    freq2
 *      context3    freq3
 *      ...         ...
 * </pre>
 * <p>The output features file contains one record per line. Each line consists
 * of three values: a head string, a context string, and the frequency of this
 * pair (i.e the total number of occurrences of these two strings together.</p>
 * <pre>
 *      head1    context1    freq1
 *      head2    context2    freq2
 *      head3    context3    freq3
 *      ...      ...         ...
 * </pre>
 *
 * @version 14nd December 2010
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Read in a raw feature instances file, to "
+ "produce three frequency files: heads, contexts, and features.",
            resourceBundle = "uk.ac.susx.mlcl.dttools.strings")
public class MemCountTask extends AbstractTask implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog(MemCountTask.class);

    /**
     * Number of records to read or write between progress updates.
     */
    private static final int PROGRESS_INTERVAL = 1000000;

    @Parameter(names = {"-ii", "--input-instances"},
               required = true,
               descriptionKey = "uk.ac.susx.mlcl.dttools.MemCountTask.INSTANCES_DESCRIPTION")
    private File instancesFile;

    @Parameter(names = {"-of", "--output-features"},
               required = true,
               descriptionKey = "uk.ac.susx.mlcl.dttools.MemCountTask.FEATURES_DESCRIPTION")
    private File featuresFile = null;

    @Parameter(names = {"-oh", "--output-heads"},
               required = true,
               descriptionKey = "uk.ac.susx.mlcl.dttools.MemCountTask.HEADS_DESCRIPTION")
    private File headsFile = null;

    @Parameter(names = {"-oc", "--output-contexts"},
               required = true,
               descriptionKey = "uk.ac.susx.mlcl.dttools.MemCountTask.CONTEXTS_DESCRIPTION")
    private File contextsFile = null;

    @Parameter(names = {"-c", "--charset"},
               descriptionKey = "uk.ac.susx.mlcl.dttools.MemCountTask.CHARSET_DESCRIPTION")
    private Charset charset = IOUtil.DEFAULT_CHARSET;

    /**
     * Dependency injection constructor with all fields parameterised.
     *
     * @param instancesFile input file containing head/context instances
     * @param featuresFile  output file for head/context/frequency triples
     * @param headsFile     output file for head/frequency pairs
     * @param contextsFile  output file for context/frequency pairs
     * @param charset       character set to use for all file I/O
     * @throws NullPointerException if any argument is null
     */
    public MemCountTask(final File instancesFile, final File featuresFile,
            final File headsFile, final File contextsFile,
            final Charset charset) throws NullPointerException {
        this(instancesFile, featuresFile, headsFile, contextsFile);
        setCharset(charset);
    }

    /**
     * Minimal parameterisation constructor, with all fields that must be set
     * for the task to be functional. Character set will be set to software
     * default from {@link IOUtil#DEFAULT_CHARSET}.
     *
     * @param instancesFile input file containing head/context instances
     * @param featuresFile  output file for head/context/frequency triples
     * @param headsFile     output file for head/frequency pairs
     * @param contextsFile  output file for context/frequency pairs
     * @throws NullPointerException if any argument is null
     */
    public MemCountTask(
            final File instancesFile, final File featuresFile,
            final File headsFile, final File contextsFile)
            throws NullPointerException {
        setInstancesFile(instancesFile);
        setFeaturesFile(featuresFile);
        setHeadsFile(headsFile);
        setContextsFile(contextsFile);
    }

    /**
     * Default constructor used by serialisation and JCommander instantiation.
     * All files will initially be set to null. Character set will be set to
     * software default from {@link IOUtil#DEFAULT_CHARSET}.
     */
    public MemCountTask() {
    }

    @Override
    protected void initialiseTask() throws Exception {
        checkState();
    }

    @Override
    protected void runTask() throws Exception {
        if (LOG.isInfoEnabled())
            LOG.info("Running " + this + ". (thread:" + Thread.currentThread().
                    getName() + ")");

        {
            final ObjectIndex<String> headIndex;
            headIndex = new ObjectIndex<String>();

            final ObjectIndex<String> contextIndex = new ObjectIndex<String>();

            final Object2IntMap<InstanceEntry> featureFreq =
                    new Object2IntOpenHashMap<InstanceEntry>();
            featureFreq.defaultReturnValue(0);

            {
                final Int2IntMap contextFreq = new Int2IntOpenHashMap();
                contextFreq.defaultReturnValue(0);

                {
                    final Int2IntMap headFreq = new Int2IntOpenHashMap();
                    headFreq.defaultReturnValue(0);
//
//                    final Int2ObjectMap<IntSet> headUniqueContexts =
//                            new Int2ObjectOpenHashMap<IntSet>();


                    countInstances(headFreq, 
//                            headUniqueContexts,
                            contextFreq,
                            featureFreq, headIndex, contextIndex);

                    writeHeads(headFreq, 
//                            headUniqueContexts,
                            headIndex);
                }

                writeContexts(contextFreq, contextIndex);
            }


            writeFeatures(featureFreq, headIndex, contextIndex);

        }
                    
        if (LOG.isInfoEnabled())
            LOG.info("Completed " + this + ". (thread:" + Thread.currentThread().
                    getName() + ")");
    }

    @Override
    protected void finaliseTask() throws Exception {
    }

    private void countInstances(
            final Int2IntMap headFreq,
//            final Int2ObjectMap<IntSet> headUniqueContexts,
            final Int2IntMap contextFreq,
            final Object2IntMap<? super InstanceEntry> featureFreq,
            final ObjectIndex<String> headIndex,
            final ObjectIndex<String> contextIndex)
            throws IOException {

        if (LOG.isInfoEnabled())
            LOG.info(
                    "Reading head/context instances from " + instancesFile + ".");
        final InstanceSource instanceSource =
                new InstanceSource(instancesFile, charset, headIndex,
                contextIndex);

        if (!instanceSource.hasNext() && LOG.isWarnEnabled())
            LOG.warn("Instances file is empty.");

        long i = 0;
        while (instanceSource.hasNext()) {
            final InstanceEntry instance = instanceSource.read();
            final int head_id = instance.getHeadId();
            final int context_id = instance.getContextId();

            headFreq.put(head_id, headFreq.get(head_id) + 1);
            contextFreq.put(context_id, contextFreq.get(context_id) + 1);
            featureFreq.put(instance, featureFreq.getInt(instance) + 1);
//
//            if (!headUniqueContexts.containsKey(head_id)) {
//                headUniqueContexts.put(head_id, new IntOpenHashSet(
//                        new int[]{context_id}));
//            } else {
//                headUniqueContexts.get(head_id).add(context_id);
//            }

            if ((++i % PROGRESS_INTERVAL == 0 || !instanceSource.hasNext())
                    && LOG.isInfoEnabled()) {
                LOG.info("Read " + i + " instances. Found "
                        + headFreq.size() + " heads, " + contextFreq.size()
                        + " contexts, and " + featureFreq.size()
                        + " features. (" + (int) instanceSource.percentRead()
                        + "% complete)");
                LOG.debug(MiscUtil.memoryInfoString());
            }
        }

        // InstanceSource doesn't need closing because it uses NIO mapped
        // regions, so just allow it to be garbage collected
    }

    private void writeHeads(
            final Int2IntMap headFreq,
//            final Int2ObjectMap<IntSet> headUniqueContexts,
            final ObjectIndex<String> headIndex)
            throws IOException {

        LOG.info("Sorting heads frequency data.");
        LOG.debug(MiscUtil.memoryInfoString());

        List<Int2IntMap.Entry> headFreqList =
                new ArrayList<Int2IntMap.Entry>(headFreq.int2IntEntrySet());
        Collections.sort(headFreqList, new Comparator<Int2IntMap.Entry>() {

            @Override
            public int compare(Entry o1, Entry o2) {
                return headIndex.get(o1.getIntKey()).compareTo(
                        headIndex.get(o2.getIntKey()));
            }
        });


        LOG.info("Writing heads frequency data to file " + headsFile + ".");
        LOG.debug(MiscUtil.memoryInfoString());

        if (headsFile.exists() && LOG.isWarnEnabled())
            LOG.warn("The heads file already exists and will be overwritten.");

        HeadSink headSink = null;
        final int n = headFreqList.size();
        try {
            headSink = new HeadSink(headsFile, charset, headIndex);
            int i = 0;
            for (final Int2IntMap.Entry head : headFreqList) {
                headSink.write(new HeadEntry(
                        head.getIntKey(),
//                        head.getIntKey(),
//                        headUniqueContexts.get(head.getIntKey()).size(),
                        head.getIntValue())
                        );
                if ((++i % PROGRESS_INTERVAL == 0 || i == n) && LOG.
                        isInfoEnabled())
                    LOG.info("Wrote " + i + "/" + n + " heads. ("
                            + (int) (i * 100d / n) + "% complete)");
            }
        } finally {
            if (headSink != null) {
                headSink.flush();
                headSink.close();
            }
        }
    }

    private void writeContexts(
            final Int2IntMap contextFreq,
            final ObjectIndex<String> contextIndex)
            throws IOException {

        LOG.info("Sorting context frequency data.");
        LOG.debug(MiscUtil.memoryInfoString());

        List<Int2IntMap.Entry> contextFreqList =
                new ArrayList<Int2IntMap.Entry>(contextFreq.int2IntEntrySet());
        Collections.sort(contextFreqList, new Comparator<Int2IntMap.Entry>() {

            @Override
            public int compare(Entry o1, Entry o2) {
                return contextIndex.get(o1.getIntKey()).compareTo(
                        contextIndex.get(o2.getIntKey()));
            }
        });

        if (LOG.isInfoEnabled())
            LOG.info("Writing context frequency data to " + contextsFile + ".");
        LOG.debug(MiscUtil.memoryInfoString());

        if (contextsFile.exists() && LOG.isWarnEnabled())
            LOG.warn("The contexts file already exists and will be overwritten.");

        ContextSink contextSink = null;
        final int n = contextFreqList.size();
        try {
            contextSink = new ContextSink(contextsFile, charset,
                    contextIndex);
            int i = 0;
            for (final Int2IntMap.Entry context : contextFreqList) {
                contextSink.write(new ContextEntry(
                        context.getIntKey(),
                        context.getIntValue()));
                if ((++i % PROGRESS_INTERVAL == 0 || i == n) && LOG.
                        isInfoEnabled())
                    LOG.info("Wrote " + i + "/" + n + " contexts. ("
                            + (int) (i * 100d / n) + "% complete)");
            }
        } finally {
            if (contextSink != null) {
                contextSink.flush();
                contextSink.close();
            }
        }
    }

    private void writeFeatures(
            final Object2IntMap<? extends InstanceEntry> featureFreq,
            final ObjectIndex<String> headIndex,
            final ObjectIndex<String> contextIndex)
            throws FileNotFoundException, IOException {

        LOG.info("Sorting feature pairs frequency data.");

        List<Object2IntMap.Entry<? extends InstanceEntry>> contextFreqList =
                new ArrayList<Object2IntMap.Entry<? extends InstanceEntry>>(featureFreq.
                object2IntEntrySet());
        Collections.sort(contextFreqList, new Comparator<Object2IntMap.Entry<? extends InstanceEntry>>() {

            @Override
            public int compare(
                    Object2IntMap.Entry<? extends InstanceEntry> o1,
                    Object2IntMap.Entry<? extends InstanceEntry> o2) {
                int v = headIndex.get(o1.getKey().getHeadId()).compareTo(
                        headIndex.get(o2.getKey().getHeadId()));
                if (v == 0) {
                    v = contextIndex.get(o1.getKey().getContextId()).compareTo(
                            contextIndex.get(o2.getKey().getContextId()));
                }
                return v;
            }
        });

        if (LOG.isInfoEnabled())
            LOG.info(
                    "Writing feature pairs frequency data to file  " + featuresFile + ".");
        LOG.debug(MiscUtil.memoryInfoString());

        if (featuresFile.exists() && LOG.isWarnEnabled())
            LOG.warn("The features file already exists and will be overwritten.");

        FeatureSink featureSink = null;
        final int n = contextFreqList.size();
        try {
            featureSink = new FeatureSink(featuresFile, charset, headIndex,
                    contextIndex);
            int i = 0;
            for (final Object2IntMap.Entry<? extends InstanceEntry> feature :
                    contextFreqList) {
                featureSink.write(new FeatureEntry(
                        feature.getKey().getHeadId(),
                        feature.getKey().getContextId(),
                        feature.getIntValue()));
                if ((++i % PROGRESS_INTERVAL == 0 || i == n) && LOG.
                        isInfoEnabled())
                    LOG.info("Wrote " + i + "/" + n + " features. ("
                            + (int) (i * 100d / n) + "% complete)");
            }
        } finally {
            if (featureSink != null) {
                featureSink.flush();
                featureSink.close();
            }
        }
    }

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

    public final Charset getCharset() {
        return charset;
    }

    public final void setCharset(Charset charset) {
        Checks.checkNotNull(charset);
        this.charset = charset;
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
        if (instancesFile == null)
            throw new NullPointerException("inputFile is null");
        if (featuresFile == null)
            throw new NullPointerException("featuresFile is null");
        if (contextsFile == null)
            throw new NullPointerException("contextsFile is null");
        if (headsFile == null)
            throw new NullPointerException("headsFile is null");
        if (charset == null)
            throw new NullPointerException("charset is null");

        // Check that no two files are the same
        if (instancesFile.equals(featuresFile))
            throw new IllegalStateException("inputFile == featuresFile");
        if (instancesFile.equals(contextsFile))
            throw new IllegalStateException("inputFile == contextsFile");
        if (instancesFile.equals(headsFile))
            throw new IllegalStateException("inputFile == headsFile");
        if (featuresFile.equals(contextsFile))
            throw new IllegalStateException("featuresFile == contextsFile");
        if (featuresFile.equals(headsFile))
            throw new IllegalStateException("featuresFile == headsFile");
        if (contextsFile.equals(headsFile))
            throw new IllegalStateException("contextsFile == headsFile");


        // Check that the instances file exists and is readable
        if (!instancesFile.exists())
            throw new FileNotFoundException(
                    "instances file does not exist: " + instancesFile);
        if (!instancesFile.isFile())
            throw new IllegalStateException(
                    "instances file is not a normal data file: " + instancesFile);
        if (!instancesFile.canRead())
            throw new IllegalStateException(
                    "instances file is not readable: " + instancesFile);

        // For each output file, check that either it exists and it writeable,
        // or that it does not exist but is creatable
        if (headsFile.exists() && (!headsFile.isFile() || !headsFile.canWrite()))
            throw new IllegalStateException(
                    "heads file exists but is not writable: " + headsFile);
        if (!headsFile.exists() && !headsFile.getAbsoluteFile().getParentFile().
                canWrite()) {
            throw new IllegalStateException(
                    "heads file does not exists and can not be reated: " + headsFile);
        }
        if (contextsFile.exists() && (!contextsFile.isFile() || !contextsFile.
                canWrite()))
            throw new IllegalStateException(
                    "contexts file exists but is not writable: " + contextsFile);
        if (!contextsFile.exists() && !contextsFile.getAbsoluteFile().
                getParentFile().
                canWrite()) {
            throw new IllegalStateException(
                    "contexts file does not exists and can not be reated: " + contextsFile);
        }
        if (featuresFile.exists() && (!featuresFile.isFile() || !featuresFile.
                canWrite()))
            throw new IllegalStateException(
                    "features file exists but is not writable: " + featuresFile);
        if (!featuresFile.exists() && !featuresFile.getAbsoluteFile().
                getParentFile().
                canWrite()) {
            throw new IllegalStateException(
                    "features file does not exists and can not be reated: " + featuresFile);
        }
    }

    @Override
    public String toString() {
        return "CountTask{"
                + "instancesFile=" + instancesFile
                + ", featuresFile=" + featuresFile
                + ", headsFile=" + headsFile
                + ", contextsFile=" + contextsFile
                + ", charset=" + charset
                + '}';
    }
}
