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
 * POSSIBILITY OF SUCH DAMAGE.To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.io;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratingDeligates;
import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumerating;

/**
 *
 * @author hiam20
 */
public class BybloIO {

    public static final boolean INSTANCES_SKIP_INDEXED_COLUMN_1 = false;

    public static final boolean INSTANCES_SKIP_INDEXED_COLUMN_2 = false;

    public static final boolean ENTRIES_SKIP_INDEXED_COLUMN_1 = true;

    public static final boolean FEATURES_SKIP_INDEXED_COLUMN_1 = true;

    public static final boolean EVENTS_SKIP_INDEXED_COLUMN_1 = true;

    public static final boolean EVENTS_SKIP_INDEXED_COLUMN_2 = true;

    public static final boolean SIMS_SKIP_INDEXED_COLUMN_1 = true;

    public static final boolean SIMS_SKIP_INDEXED_COLUMN_2 = true;

    public static final boolean NEIGHBOURS_SKIP_INDEXED_COLUMN_1 = true;

    public static final boolean NEIGHBOURS_SKIP_INDEXED_COLUMN_2 = false;

    public static final boolean INSTANCES_COMPACT = true;

    public static final boolean EVENTS_COMPACT = true;

    public static final boolean SIMS_COMPACT = true;

    public static final boolean NEIGHBOURS_COMPACT = true;

    public static TokenPairSource openInstancesSource(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return TokenPairSource.open(
                file, charset, idx,
                INSTANCES_SKIP_INDEXED_COLUMN_1,
                INSTANCES_SKIP_INDEXED_COLUMN_2);
    }

    public static TokenPairSink openInstancesSink(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return TokenPairSink.open(
                file, charset, idx,
                INSTANCES_SKIP_INDEXED_COLUMN_1,
                INSTANCES_SKIP_INDEXED_COLUMN_2,
                INSTANCES_COMPACT);
    }

    public static WeightedTokenSource openFeaturesSource(
            File file, Charset charset, SingleEnumerating idx)
            throws IOException {
        return WeightedTokenSource.open(
                file, charset, idx,
                FEATURES_SKIP_INDEXED_COLUMN_1);

    }

    public static WeightedTokenSink openFeaturesSink(
            File file, Charset charset, SingleEnumerating idx)
            throws IOException {
        return WeightedTokenSink.open(
                file, charset, idx,
                FEATURES_SKIP_INDEXED_COLUMN_1);

    }

    public static WeightedTokenSource openFeaturesSource(
            File file, Charset charset, DoubleEnumerating idx) throws IOException {
        return openFeaturesSource(file, charset, EnumeratingDeligates.toSingleFeatures(idx));
    }

    public static WeightedTokenSink openFeaturesSink(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return openFeaturesSink(file, charset, EnumeratingDeligates.toSingleFeatures(idx));
    }

    public static WeightedTokenSource openEntriesSource(
            File file, Charset charset, SingleEnumerating idx)
            throws IOException {
        return WeightedTokenSource.open(
                file, charset, idx,
                ENTRIES_SKIP_INDEXED_COLUMN_1);

    }

    public static WeightedTokenSink openEntriesSink(
            File file, Charset charset, SingleEnumerating idx)
            throws IOException {
        return WeightedTokenSink.open(
                file, charset, idx,
                ENTRIES_SKIP_INDEXED_COLUMN_1);

    }

    public static WeightedTokenSource openEntriesSource(
            File file, Charset charset, DoubleEnumerating idx) throws IOException {
        return openEntriesSource(file, charset, EnumeratingDeligates.toSingleEntries(idx));
    }

    public static WeightedTokenSink openEntriesSink(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return openEntriesSink(file, charset, EnumeratingDeligates.toSingleEntries(idx));
    }

    public static FastWeightedTokenPairVectorSource openEventsVectorSource(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
//        return new WeightedTokenPairVectorSource(openEventsSource(file, charset, idx));
        return FastWeightedTokenPairVectorSource.open(
                file, charset, idx,
                EVENTS_SKIP_INDEXED_COLUMN_1,
                EVENTS_SKIP_INDEXED_COLUMN_2);
    }

    public static FastWeightedTokenPairVectorSink openEventsVectorSink(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return FastWeightedTokenPairVectorSink.open(
                file, charset, idx,
                EVENTS_SKIP_INDEXED_COLUMN_1,
                EVENTS_SKIP_INDEXED_COLUMN_2,
                EVENTS_COMPACT);

    }

    public static WeightedTokenPairSource openEventsSource(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return WeightedTokenPairSource.open(
                file, charset, idx,
                EVENTS_SKIP_INDEXED_COLUMN_1,
                EVENTS_SKIP_INDEXED_COLUMN_2);
    }

    public static WeightedTokenPairSink openEventsSink(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return WeightedTokenPairSink.open(
                file, charset, idx,
                EVENTS_SKIP_INDEXED_COLUMN_1,
                EVENTS_SKIP_INDEXED_COLUMN_2,
                EVENTS_COMPACT);
    }

    public static WeightedTokenPairSource openSimsSource(
            File file, Charset charset, SingleEnumerating idx)
            throws IOException {
        return WeightedTokenPairSource.open(
                file, charset, EnumeratingDeligates.toPair(idx),
                SIMS_SKIP_INDEXED_COLUMN_1,
                SIMS_SKIP_INDEXED_COLUMN_2);
    }

    public static WeightedTokenPairSink openSimsSink(
            File file, Charset charset, SingleEnumerating idx)
            throws IOException {
        return WeightedTokenPairSink.open(
                file, charset, EnumeratingDeligates.toPair(idx),
                SIMS_SKIP_INDEXED_COLUMN_1,
                SIMS_SKIP_INDEXED_COLUMN_2,
                SIMS_COMPACT);
    }

    public static WeightedTokenPairSource openSimsSource(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return openSimsSource(file, charset, EnumeratingDeligates.toSingleEntries(idx));
    }

    public static WeightedTokenPairSink openSimsSink(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return openSimsSink(file, charset, EnumeratingDeligates.toSingleEntries(idx));
    }

}
