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
package uk.ac.susx.mlcl.byblo.tasks;

import com.google.common.base.Objects;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.Flushable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import uk.ac.susx.mlcl.byblo.io.Token;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.events.ProgressDeligate;
import uk.ac.susx.mlcl.lib.events.ProgressListener;
import uk.ac.susx.mlcl.lib.events.ProgressReporting;
import uk.ac.susx.mlcl.lib.io.ObjectIO;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.ObjectSource;
import uk.ac.susx.mlcl.lib.tasks.AbstractTask;

/**
 * <p>Read in a raw feature instances, to produce three frequency cuonts:
 * entries, features, and event pairs.</p>
 * <p/>
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class CountTask extends AbstractTask
        implements ProgressReporting {

    private static final long serialVersionUID = 1L;

    private final ProgressDeligate progress = new ProgressDeligate(this, true);

    private ObjectSource<TokenPair> source;

    private ObjectSink<Weighted<TokenPair>> eventSink;

    private ObjectSink<Weighted<Token>> entrySink;

    private ObjectSink<Weighted<Token>> featureSink;

    private Comparator<Weighted<TokenPair>> eventComparator;

    private Comparator<Weighted<Token>> entryComparator;

    private Comparator<Weighted<Token>> featureComparator;

    public CountTask(
            ObjectSource<TokenPair> source,
            ObjectSink<Weighted<TokenPair>> eventSink,
            ObjectSink<Weighted<Token>> entrySink,
            ObjectSink<Weighted<Token>> featureSink,
            Comparator<Weighted<TokenPair>> eventComparator,
            Comparator<Weighted<Token>> entryComparator,
            Comparator<Weighted<Token>> featureComparator) {
        setSource(source);
        setEventSink(eventSink);
        setEntrySink(entrySink);
        setFeatureSink(featureSink);
        setEventComparator(eventComparator);
        setEntryComparator(entryComparator);
        setFeatureComparator(featureComparator);
    }

    public final ObjectSink<Weighted<Token>> getEntrySink() {
        return entrySink;
    }

    public final void setEntrySink(ObjectSink<Weighted<Token>> entrySink) {
        Checks.checkNotNull("entrySink", entrySink);
        this.entrySink = entrySink;
    }

    public final ObjectSink<Weighted<TokenPair>> getEventSink() {
        return eventSink;
    }

    public final void setEventSink(ObjectSink<Weighted<TokenPair>> eventSink) {
        Checks.checkNotNull("eventSink", eventSink);
        this.eventSink = eventSink;
    }

    public final ObjectSink<Weighted<Token>> getFeatureSink() {
        return featureSink;
    }

    public final void setFeatureSink(ObjectSink<Weighted<Token>> featureSink) {
        Checks.checkNotNull("featureSink", featureSink);
        this.featureSink = featureSink;
    }

    public final ObjectSource<TokenPair> getSource() {
        return source;
    }

    public final void setSource(ObjectSource<TokenPair> source) {
        Checks.checkNotNull("source", source);
        this.source = source;
    }

    public final Comparator<Weighted<Token>> getEntryComparator() {
        return entryComparator;
    }

    public final void setEntryComparator(
            Comparator<Weighted<Token>> entryComparator) {
        Checks.checkNotNull("entryComparator", entryComparator);
        this.entryComparator = entryComparator;
    }

    public final Comparator<Weighted<TokenPair>> getEventComparator() {
        return eventComparator;
    }

    public final void setEventComparator(
            Comparator<Weighted<TokenPair>> eventComparator) {
        Checks.checkNotNull("eventComparator", eventComparator);
        this.eventComparator = eventComparator;
    }

    public final Comparator<Weighted<Token>> getFeatureComparator() {
        return featureComparator;
    }

    public final void setFeatureComparator(
            Comparator<Weighted<Token>> featureComparator) {
        Checks.checkNotNull("featureComparator", featureComparator);
        this.featureComparator = featureComparator;
    }

    void checkState() {
        Checks.checkNotNull("source", source);
        Checks.checkNotNull("featureSink", featureSink);
        Checks.checkNotNull("eventSink", eventSink);
        Checks.checkNotNull("entrySink", entrySink);
        Checks.checkNotNull("featureComparator", featureComparator);
        Checks.checkNotNull("eventComparator", eventComparator);
        Checks.checkNotNull("entryComparator", entryComparator);
    }

    @Override
    protected void initialiseTask() throws Exception {
        checkState();
    }

    @Override
    public void runTask() throws Exception {

        progress.setState(State.RUNNING);

        float loadFactor = Hash.FAST_LOAD_FACTOR;
        int initSize = Hash.DEFAULT_INITIAL_SIZE;// * 100;

        final Object2IntOpenHashMap<TokenPair> eventFreq =
                new Object2IntOpenHashMap<TokenPair>(initSize, loadFactor);
        eventFreq.defaultReturnValue(0);

        final Int2IntOpenHashMap featureFreq =
                new Int2IntOpenHashMap(initSize,
                                       loadFactor);
        featureFreq.defaultReturnValue(0);

        final Int2IntOpenHashMap entryFreq = new Int2IntOpenHashMap(initSize,
                                                                    loadFactor);
        entryFreq.defaultReturnValue(0);

        long instanceCount = 0;
        while (getSource().hasNext()) {
            final TokenPair instance = getSource().read();

            entryFreq.add(instance.id1(), 1);
            featureFreq.add(instance.id2(), 1);
            eventFreq.add(instance, 1);

            ++instanceCount;
            if (instanceCount % 1000000 == 0 || !getSource().hasNext()) {
                progress.setMessage(MessageFormat.format("Read {0} instances",
                                                         instanceCount));
            }
        }

        progress.startAdjusting();
        progress.
                setProgressPercent(
                (int) (100 * instanceCount
                       / (instanceCount + entryFreq.
                          size() + featureFreq.
                          size() + eventFreq.
                          size())));
        progress.setMessage("Writing entries.");
        progress.endAdjusting();


        {
            List<Weighted<Token>> entries =
                    int2IntMapToWeightedTokens(entryFreq);
            Collections.sort(entries, getEntryComparator());
            ObjectIO.copy(entries, getEntrySink());
        }
        if (getEntrySink() instanceof Flushable)
            ((Flushable) getEntrySink()).flush();
        progress.startAdjusting();
        progress.
                setProgressPercent(
                (int) (100 * (instanceCount + entryFreq.
                              size())
                       / (instanceCount + entryFreq.
                          size() + featureFreq.
                          size() + eventFreq.
                          size())));
        progress.setMessage("Writing features.");
        progress.endAdjusting();


        {
            List<Weighted<Token>> features = int2IntMapToWeightedTokens(
                    featureFreq);
            Collections.sort(features, getFeatureComparator());
            ObjectIO.copy(features, getFeatureSink());
        }
        if (getFeatureSink() instanceof Flushable)
            ((Flushable) getFeatureSink()).flush();

        progress.startAdjusting();
        progress.
                setProgressPercent(
                (int) (100 * (instanceCount + entryFreq.
                              size() + featureFreq.
                              size())
                       / (instanceCount + entryFreq.
                          size() + featureFreq.
                          size() + eventFreq.
                          size())));
        progress.setMessage("Writing events.");
        progress.endAdjusting();

        {
            List<Weighted<TokenPair>> events = obj2intmapToWeightedObjList(
                    eventFreq);
            Collections.sort(events, getEventComparator());
            ObjectIO.copy(events, getEventSink());
        }
        if (getEventSink() instanceof Flushable)
            ((Flushable) getEventSink()).flush();

        progress.startAdjusting();
        progress.setProgressPercent(100);
        progress.setState(State.COMPLETED);
        progress.endAdjusting();


    }

    @Override
    protected void finaliseTask() throws Exception {
    }

    private static List<Weighted<Token>> int2IntMapToWeightedTokens(
            final Int2IntMap map) {
        final List<Weighted<Token>> out = new ArrayList<Weighted<Token>>(map.
                size());
        for (Int2IntMap.Entry e : map.int2IntEntrySet()) {
            out.add(new Weighted<Token>(
                    new Token(e.getIntKey()), e.getIntValue()));
        }
        return out;
    }

    private static <T> List<Weighted<T>> obj2intmapToWeightedObjList(
            final Object2IntMap<T> map) {
        final List<Weighted<T>> out = new ArrayList<Weighted<T>>(map.size());
        for (Object2IntMap.Entry<T> e : map.object2IntEntrySet()) {
            out.add(new Weighted<T>(
                    e.getKey(), e.getIntValue()));
        }
        return out;
    }

    @Override
    public String getName() {
        return "count";
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
    public String getProgressReport() {
        return progress.getProgressReport();
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
    public State getState() {
        return progress.getState();
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("source", getSource()).
                add("entriesSink", getEntrySink()).
                add("featuresSink", getFeatureSink()).
                add("eventsSink", getEventSink()).
                add("entriesComparator", getEntryComparator()).
                add("featuresComparator", getFeatureComparator()).
                add("eventsComparator", getEventComparator());
    }
}
