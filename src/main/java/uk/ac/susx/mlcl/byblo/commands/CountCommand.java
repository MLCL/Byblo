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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Comparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.io.BybloIO;
import uk.ac.susx.mlcl.byblo.io.Token;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.TokenPairSource;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSink;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenSink;
import uk.ac.susx.mlcl.byblo.tasks.CountTask;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.commands.AbstractCommand;
import uk.ac.susx.mlcl.lib.commands.InputFileValidator;
import uk.ac.susx.mlcl.lib.commands.OutputFileValidator;
import uk.ac.susx.mlcl.lib.io.Files;
import uk.ac.susx.mlcl.lib.io.ObjectIO;
import uk.ac.susx.mlcl.lib.events.ProgressEvent;
import uk.ac.susx.mlcl.lib.events.ProgressListener;

/**
 * <p>Read in a raw feature instances file, to produce three frequency files:
 * entries, features, and entry-feature pairs.</p>
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Parameters(commandDescription = "Read in a raw feature instances file, to produce three "
+ "frequency files: entries, contexts, and features.")
public class CountCommand extends AbstractCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog(CountCommand.class);

    @Parameter(names = {"-i", "--input"},
    required = true,
    description = "Source instances file",
    validateWith = InputFileValidator.class)
    private File inputFile;

    @Parameter(names = {"-oef", "--output-entry-features"},
    required = true,
    description = "Entry-feature-pair frequencies destination file",
    validateWith = OutputFileValidator.class)
    private File eventsFile = null;

    @Parameter(names = {"-oe", "--output-entries"},
    required = true,
    description = "Entry frequencies destination file",
    validateWith = OutputFileValidator.class)
    private File entriesFile = null;

    @Parameter(names = {"-of", "--output-features"},
    required = true,
    description = "Feature frequencies destination file.",
    validateWith = OutputFileValidator.class)
    private File featuresFile = null;

    @Parameter(names = {"-c", "--charset"},
    description = "Character encoding to use for input and output.")
    private Charset charset = Files.DEFAULT_CHARSET;

    @ParametersDelegate
    private DoubleEnumerating indexDeligate = new DoubleEnumeratingDeligate();

    /**
     * Dependency injection constructor with all fields parameterised.
     *
     * @param instancesFile input file containing entry/context instances
     * @param eventsFile output file for entry/context/frequency triples
     * @param entriesFile output file for entry/frequency pairs
     * @param featuresFile output file for context/frequency pairs
     * @param indexDeligate
     * @param charset character set to use for all file I/O
     * @throws NullPointerException if any argument is null
     */
    public CountCommand(final File instancesFile, final File eventsFile,
                        final File entriesFile, final File featuresFile,
                        DoubleEnumerating indexDeligate,
                        final Charset charset) throws NullPointerException {
        this(instancesFile, eventsFile, entriesFile, featuresFile);
        setCharset(charset);
        setIndexDeligate(indexDeligate);
    }

    public CountCommand(final File instancesFile, final File eventsFile,
                        final File entriesFile, final File featuresFile,
                        final Charset charset) throws NullPointerException {
        this(instancesFile, eventsFile, entriesFile, featuresFile);
        setCharset(charset);
    }

    /**
     * Minimal parameterisation constructor, with all fields that must be set
     * for the task to be functional. Character set will be set to software
     * default from {@link IOUtil#DEFAULT_CHARSET}.
     *
     * @param instancesFile input file containing entry/context instances
     * @param eventsFile output file for entry/context/frequency triples
     * @param entriesFile output file for entry/frequency pairs
     * @param featuresFile output file for context/frequency pairs
     * @throws NullPointerException if any argument is null
     */
    public CountCommand(
            final File instancesFile, final File eventsFile,
            final File entriesFile, final File featuresFile)
            throws NullPointerException {
        setInstancesFile(instancesFile);
        setEventsFile(eventsFile);
        setEntriesFile(entriesFile);
        setFeaturesFile(featuresFile);
    }

    /**
     * Default constructor used by serialisation and JCommander instantiation.
     * All files will initially be set to null. Character set will be set to
     * software default from {@link IOUtil#DEFAULT_CHARSET}.
     */
    public CountCommand() {
    }

    public DoubleEnumerating getIndexDeligate() {
        return indexDeligate;
    }

    public final void setIndexDeligate(DoubleEnumerating indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }

    public final File getFeaturesFile() {
        return featuresFile;
    }

    public final void setFeaturesFile(final File featuresFile)
            throws NullPointerException {
        Checks.checkNotNull("featuresFile", featuresFile);
        this.featuresFile = featuresFile;
    }

    public final File getEventsFile() {
        return eventsFile;
    }

    public final void setEventsFile(final File eventsFile)
            throws NullPointerException {
        Checks.checkNotNull("eventsFile", eventsFile);
        this.eventsFile = eventsFile;
    }

    public final File getEntriesFile() {
        return entriesFile;
    }

    public final void setEntriesFile(final File entriesFile)
            throws NullPointerException {
        Checks.checkNotNull("entriesFile", entriesFile);
        this.entriesFile = entriesFile;
    }

    public File getInputFile() {
        return inputFile;
    }

    public final void setInstancesFile(final File inputFile)
            throws NullPointerException {
        Checks.checkNotNull("inputFile", inputFile);
        this.inputFile = inputFile;
    }

    public final Charset getCharset() {
        return charset;
    }

    public final void setCharset(Charset charset) {
        Checks.checkNotNull("charset", charset);
        this.charset = charset;
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
               : Weighted.recordOrder(TokenPair.stringOrder(
                indexDeligate));
    }

    @Override
    public void runCommand() throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("Running memory count on \"" + inputFile + "\".");
        }

        checkState();

        final TokenPairSource instanceSource = BybloIO.openInstancesSource(inputFile, charset, indexDeligate);

        WeightedTokenSink entrySink = BybloIO.openEntriesSink(entriesFile, charset, indexDeligate);

        WeightedTokenSink featureSink = BybloIO.openFeaturesSink(featuresFile, charset, indexDeligate);


        WeightedTokenPairSink eventsSink = BybloIO.openEventsSink(eventsFile, charset, indexDeligate);

        CountTask task = new CountTask(
                instanceSource, eventsSink, entrySink, featureSink,
                getEventOrder(), getEntryOrder(), getFeatureOrder());


        task.addProgressListener(new ProgressListener() {

            @Override
            public void progressChanged(ProgressEvent progressEvent) {
                System.out.println(progressEvent.getSource().getProgressReport());
            }

        });

        task.run();


        while (task.isExceptionTrapped())
            task.throwTrappedException();


        instanceSource.close();
        entrySink.flush();
        entrySink.close();
        featureSink.flush();
        featureSink.close();
        eventsSink.flush();
        eventsSink.close();

        if (indexDeligate.isEnumeratorOpen()) {
            indexDeligate.saveEnumerator();
            indexDeligate.closeEnumerator();
        }


        if (LOG.isInfoEnabled()) {
            LOG.info("Completed memory count.");
        }
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
        Checks.checkNotNull("eventsFile", eventsFile);
        Checks.checkNotNull("featuresFile", featuresFile);
        Checks.checkNotNull("entriesFile", entriesFile);
        Checks.checkNotNull("entriesFile", entriesFile);
        Checks.checkNotNull("inputFile", inputFile);
        Checks.checkNotNull("charset", charset);

        // Check that no two files are the same
        if (inputFile.equals(eventsFile)) {
            throw new IllegalStateException("inputFile == featuresFile");
        }
        if (inputFile.equals(featuresFile)) {
            throw new IllegalStateException("inputFile == contextsFile");
        }
        if (inputFile.equals(entriesFile)) {
            throw new IllegalStateException("inputFile == entriesFile");
        }
        if (eventsFile.equals(featuresFile)) {
            throw new IllegalStateException("eventsFile == featuresFile");
        }
        if (eventsFile.equals(entriesFile)) {
            throw new IllegalStateException("eventsFile == entriesFile");
        }
        if (featuresFile.equals(entriesFile)) {
            throw new IllegalStateException("featuresFile == entriesFile");
        }


        // Check that the instances file exists and is readable
        if (!inputFile.exists()) {
            throw new FileNotFoundException(
                    "instances file does not exist: " + inputFile);
        }
        if (!inputFile.isFile()) {
            throw new IllegalStateException(
                    "instances file is not a normal data file: " + inputFile);
        }
        if (!inputFile.canRead()) {
            throw new IllegalStateException(
                    "instances file is not readable: " + inputFile);
        }

        // For each output file, check that either it exists and it writeable,
        // or that it does not exist but is creatable
        if (entriesFile.exists() && (!entriesFile.isFile() || !entriesFile.canWrite())) {
            throw new IllegalStateException(
                    "entries file exists but is not writable: " + entriesFile);
        }
        if (!entriesFile.exists() && !entriesFile.getAbsoluteFile().
                getParentFile().
                canWrite()) {
            throw new IllegalStateException(
                    "entries file does not exists and can not be reated: " + entriesFile);
        }
        if (featuresFile.exists() && (!featuresFile.isFile() || !featuresFile.canWrite())) {
            throw new IllegalStateException(
                    "features file exists but is not writable: " + featuresFile);
        }
        if (!featuresFile.exists() && !featuresFile.getAbsoluteFile().
                getParentFile().
                canWrite()) {
            throw new IllegalStateException(
                    "features file does not exists and can not be reated: " + featuresFile);
        }
        if (eventsFile.exists() && (!eventsFile.isFile() || !eventsFile.canWrite())) {
            throw new IllegalStateException(
                    "entry-features file exists but is not writable: " + eventsFile);
        }
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
                add("charset", charset);
    }

    public static void main(String[] args) throws Exception {
        new CountCommand().runCommand(args);
    }

}
