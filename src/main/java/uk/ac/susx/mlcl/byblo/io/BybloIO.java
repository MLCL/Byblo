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
package uk.ac.susx.mlcl.byblo.io;

import com.google.common.io.Closeables;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.byblo.BybloSettings;
import uk.ac.susx.mlcl.byblo.commands.AllPairsCommand;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratingDeligates;
import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumerating;
import uk.ac.susx.mlcl.byblo.weighings.MarginalDistribution;

/**
 * Static utility class that provides functions for opening the various file
 * types.
 * <p/>
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class BybloIO {

    private BybloIO() {
    }

    public static TokenPairSource openInstancesSource(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return TokenPairSource
                .open(
                file, charset, idx,
                BybloSettings.getInstance().isInstancesSkipIndexColumn1Enabled(),
                BybloSettings.getInstance().isInstancesSkipIndexColumn2Enabled());
    }

    public static TokenPairSink openInstancesSink(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return TokenPairSink
                .open(
                file, charset, idx,
                BybloSettings.getInstance().isInstancesSkipIndexColumn1Enabled(),
                BybloSettings.getInstance().isInstancesSkipIndexColumn1Enabled(),
                BybloSettings.getInstance().isInstancesCompactEnabled());
    }

    public static MarginalDistribution readMarginalDistribution(
            WeightedTokenSource src)
            throws IOException {

        final WeightedTokenSource.WTStatsSource statsSrc =
                new WeightedTokenSource.WTStatsSource(src);

        final double[] frequencies = AllPairsCommand.readAllAsArray(statsSrc);
        final double frequencySum = statsSrc.getWeightSum();
        final int nonZeroCardinality = statsSrc.getMaxId() + 1;

        final MarginalDistribution md = new MarginalDistribution(
                frequencies, frequencySum, nonZeroCardinality);
        return md;
    }

    public static MarginalDistribution readEntriesMarginalDistribution(
            final File file, final Charset charset, final SingleEnumerating idx)
            throws IOException {
        WeightedTokenSource src = null;
        try {
            src = openEntriesSource(file, charset, idx);
            return readMarginalDistribution(src);
        } finally {
            if (src != null)
                Closeables.closeQuietly(src);
        }
    }

    public static MarginalDistribution readFeaturesMarginalDistribution(
            final File file, final Charset charset, final SingleEnumerating idx)
            throws IOException {
        WeightedTokenSource src = null;
        try {
            src = openFeaturesSource(file, charset, idx);
            return readMarginalDistribution(src);
        } finally {
            if (src != null)
                Closeables.closeQuietly(src);
        }
    }

    public static WeightedTokenSource openFeaturesSource(
            File file, Charset charset, SingleEnumerating idx)
            throws IOException {
        return WeightedTokenSource.open(
                file, charset, idx,
                BybloSettings.getInstance().isFeaturesSkipIndexColumn1Enabled());

    }

    public static WeightedTokenSink openFeaturesSink(
            File file, Charset charset, SingleEnumerating idx)
            throws IOException {
        return WeightedTokenSink.open(
                file, charset, idx,
                BybloSettings.getInstance().isFeaturesSkipIndexColumn1Enabled());

    }

    public static WeightedTokenSource openFeaturesSource(
            File file, Charset charset, DoubleEnumerating idx) throws IOException {
        return openFeaturesSource(file, charset,
                                  EnumeratingDeligates.toSingleFeatures(idx));
    }

    public static WeightedTokenSink openFeaturesSink(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return openFeaturesSink(file, charset,
                                EnumeratingDeligates.toSingleFeatures(idx));
    }

    public static WeightedTokenSource openEntriesSource(
            File file, Charset charset, SingleEnumerating idx)
            throws IOException {
        return WeightedTokenSource.open(
                file, charset, idx,
                BybloSettings.getInstance().isEntriesSkipIndexColumn1Enabled());

    }

    public static WeightedTokenSink openEntriesSink(
            File file, Charset charset, SingleEnumerating idx)
            throws IOException {
        return WeightedTokenSink.open(
                file, charset, idx,
                BybloSettings.getInstance().isEntriesSkipIndexColumn1Enabled());

    }

    public static WeightedTokenSource openEntriesSource(
            File file, Charset charset, DoubleEnumerating idx) throws IOException {
        return openEntriesSource(file, charset, EnumeratingDeligates.
                toSingleEntries(idx));
    }

    public static WeightedTokenSink openEntriesSink(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return openEntriesSink(file, charset, EnumeratingDeligates.
                toSingleEntries(idx));
    }

    public static FastWeightedTokenPairVectorSource openEventsVectorSource(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return FastWeightedTokenPairVectorSource.open(
                file, charset, idx,
                BybloSettings.getInstance().isEventsSkipIndexColumn1Enabled(),
                BybloSettings.getInstance().isEventsSkipIndexColumn2Enabled());
    }

    public static FastWeightedTokenPairVectorSink openEventsVectorSink(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return FastWeightedTokenPairVectorSink.open(
                file, charset, idx,
                BybloSettings.getInstance().isEventsSkipIndexColumn1Enabled(),
                BybloSettings.getInstance().isEventsSkipIndexColumn2Enabled(),
                BybloSettings.getInstance().isEventsCompactEnabled());

    }

    public static WeightedTokenPairSource openEventsSource(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return WeightedTokenPairSource.open(
                file, charset, idx,
                BybloSettings.getInstance().isEventsSkipIndexColumn1Enabled(),
                BybloSettings.getInstance().isEventsSkipIndexColumn2Enabled());
    }

    public static WeightedTokenPairSink openEventsSink(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return WeightedTokenPairSink.open(
                file, charset, idx,
                BybloSettings.getInstance().isEventsSkipIndexColumn1Enabled(),
                BybloSettings.getInstance().isEventsSkipIndexColumn2Enabled(),
                BybloSettings.getInstance().isEventsCompactEnabled());
    }

    public static WeightedTokenPairSource openSimsSource(
            File file, Charset charset, SingleEnumerating idx)
            throws IOException {
        return WeightedTokenPairSource.open(
                file, charset, EnumeratingDeligates.toPair(idx),
                BybloSettings.getInstance().isSimsSkipIndexColumn1Enabled(),
                BybloSettings.getInstance().isSimsSkipIndexColumn2Enabled());
    }

    public static WeightedTokenPairSink openSimsSink(
            File file, Charset charset, SingleEnumerating idx)
            throws IOException {
        return WeightedTokenPairSink.open(
                file, charset, EnumeratingDeligates.toPair(idx),
                BybloSettings.getInstance().isSimsSkipIndexColumn1Enabled(),
                BybloSettings.getInstance().isSimsSkipIndexColumn2Enabled(),
                BybloSettings.getInstance().isSimsCompactEnabled());
    }

    public static WeightedTokenPairSource openSimsSource(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return openSimsSource(file, charset, EnumeratingDeligates.
                toSingleEntries(idx));
    }

    public static WeightedTokenPairSink openSimsSink(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return openSimsSink(file, charset, EnumeratingDeligates.toSingleEntries(
                idx));
    }

    public static WeightedTokenPairSource openNeighboursSource(
            File file, Charset charset, SingleEnumerating idx)
            throws IOException {
        return WeightedTokenPairSource.open(
                file, charset, EnumeratingDeligates.toPair(idx),
                BybloSettings.getInstance().
                isNeighboursSkipIndexColumn1Enabled(),
                BybloSettings.getInstance().
                isNeighboursSkipIndexColumn2Enabled());
    }

    public static WeightedTokenPairSink openNeighboursSink(
            File file, Charset charset, SingleEnumerating idx)
            throws IOException {
        return WeightedTokenPairSink.open(
                file, charset, EnumeratingDeligates.toPair(idx),
                BybloSettings.getInstance().
                isNeighboursSkipIndexColumn1Enabled(),
                BybloSettings.getInstance().
                isNeighboursSkipIndexColumn2Enabled(),
                BybloSettings.getInstance().isNeighboursCompactEnabled());
    }

    public static WeightedTokenPairSource openNeighboursSource(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return openNeighboursSource(file, charset,
                                    EnumeratingDeligates.toSingleEntries(idx));
    }

    public static WeightedTokenPairSink openNeighboursSink(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        return openNeighboursSink(file, charset,
                                  EnumeratingDeligates.toSingleEntries(idx));
    }
}
