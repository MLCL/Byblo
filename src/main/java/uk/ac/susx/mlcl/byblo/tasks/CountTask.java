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
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.Flushable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import uk.ac.susx.mlcl.byblo.io.Token;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Source;
import uk.ac.susx.mlcl.lib.tasks.AbstractTask;

/**
 * <p>Read in a raw feature instances, to produce three frequency cuonts:
 * entries, features, and event pairs.</p>
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
public class CountTask extends AbstractTask implements Serializable {

    private static final long serialVersionUID = 1L;

    private Source<TokenPair> source;

    private Sink<Weighted<TokenPair>> eventSink;

    private Sink<Weighted<Token>> entrySink;

    private Sink<Weighted<Token>> featureSink;

    private Comparator<Weighted<TokenPair>> eventComparator;

    private Comparator<Weighted<Token>> entryComparator;

    private Comparator<Weighted<Token>> featureComparator;

    public CountTask(
            Source<TokenPair> source,
            Sink<Weighted<TokenPair>> eventSink,
            Sink<Weighted<Token>> entrySink,
            Sink<Weighted<Token>> featureSink,
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

    public final Sink<Weighted<Token>> getEntrySink() {
        return entrySink;
    }

    public final void setEntrySink(Sink<Weighted<Token>> entrySink) {
        Checks.checkNotNull("entrySink", entrySink);
        this.entrySink = entrySink;
    }

    public final Sink<Weighted<TokenPair>> getEventSink() {
        return eventSink;
    }

    public final void setEventSink(Sink<Weighted<TokenPair>> eventSink) {
        Checks.checkNotNull("eventSink", eventSink);
        this.eventSink = eventSink;
    }

    public final Sink<Weighted<Token>> getFeatureSink() {
        return featureSink;
    }

    public final void setFeatureSink(Sink<Weighted<Token>> featureSink) {
        Checks.checkNotNull("featureSink", featureSink);
        this.featureSink = featureSink;
    }

    public final Source<TokenPair> getSource() {
        return source;
    }

    public final void setSource(Source<TokenPair> source) {
        Checks.checkNotNull("source", source);
        this.source = source;
    }

    public final Comparator<Weighted<Token>> getEntryComparator() {
        return entryComparator;
    }

    public final void setEntryComparator(Comparator<Weighted<Token>> entryComparator) {
        Checks.checkNotNull("entryComparator", entryComparator);
        this.entryComparator = entryComparator;
    }

    public final Comparator<Weighted<TokenPair>> getEventComparator() {
        return eventComparator;
    }

    public final void setEventComparator(Comparator<Weighted<TokenPair>> eventComparator) {
        Checks.checkNotNull("eventComparator", eventComparator);
        this.eventComparator = eventComparator;
    }

    public final Comparator<Weighted<Token>> getFeatureComparator() {
        return featureComparator;
    }

    public final void setFeatureComparator(Comparator<Weighted<Token>> featureComparator) {
        Checks.checkNotNull("featureComparator", featureComparator);
        this.featureComparator = featureComparator;
    }

    protected void checkState() {
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

        final Object2IntMap<TokenPair> entryFeatureFreq =
                new Object2IntOpenHashMap<TokenPair>();
        entryFeatureFreq.defaultReturnValue(0);

        final Int2IntMap featureFreq = new Int2IntOpenHashMap();
        featureFreq.defaultReturnValue(0);

        final Int2IntMap entryFreq = new Int2IntOpenHashMap();
        entryFreq.defaultReturnValue(0);

        while (getSource().hasNext()) {
            final TokenPair instance;
            instance = getSource().read();

            final int entry_id = instance.id1();
            final int feature_id = instance.id2();

            entryFreq.put(entry_id, entryFreq.get(entry_id) + 1);
            featureFreq.put(feature_id, featureFreq.get(feature_id) + 1);
            entryFeatureFreq.put(instance, entryFeatureFreq.getInt(instance) + 1);
        }

        List<Weighted<Token>> entries = int2IntMapToWeightedTokens(entryFreq);
        Collections.sort(entries, getEntryComparator());
        IOUtil.copy(entries, getEntrySink());
        if (getEntrySink() instanceof Flushable)
            ((Flushable) getEntrySink()).flush();


        List<Weighted<Token>> features = int2IntMapToWeightedTokens(featureFreq);
        Collections.sort(features, getFeatureComparator());
        IOUtil.copy(features, getFeatureSink());
        if (getFeatureSink() instanceof Flushable)
            ((Flushable) getFeatureSink()).flush();



        List<Weighted<TokenPair>> events = obj2intmapToWeightedObjList(entryFeatureFreq);
        Collections.sort(events, getEventComparator());
        IOUtil.copy(events, getEventSink());
        if (getEventSink() instanceof Flushable)
            ((Flushable) getEventSink()).flush();


    }

    @Override
    protected void finaliseTask() throws Exception {
    }

    private static List<Weighted<Token>> int2IntMapToWeightedTokens(Int2IntMap map) {
        List<Weighted<Token>> out = new ArrayList<Weighted<Token>>(map.size());
        for (Int2IntMap.Entry e : map.int2IntEntrySet()) {
            out.add(new Weighted<Token>(
                    new Token(e.getIntKey()), e.getIntValue()));
        }
        return out;
    }

    private static <T> List<Weighted<T>> obj2intmapToWeightedObjList(
            Object2IntMap<T> map) {
        List<Weighted<T>> out = new ArrayList<Weighted<T>>(map.size());
        for (Object2IntMap.Entry<T> e : map.object2IntEntrySet()) {
            out.add(new Weighted<T>(
                    e.getKey(), e.getIntValue()));
        }
        return out;
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
