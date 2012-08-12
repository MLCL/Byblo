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

import com.google.common.base.Predicate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSource;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairVectorSource;
import uk.ac.susx.mlcl.byblo.measures.Measure;
import uk.ac.susx.mlcl.byblo.measures.impl.Jaccard;
import uk.ac.susx.mlcl.lib.io.ObjectIO;
import uk.ac.susx.mlcl.lib.io.Tell;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class NaiveApssTaskTest {

    private static final Measure MEASURE = new Jaccard();

    private static final Predicate<Weighted<TokenPair>> PAIR_FILTER =
            Weighted.greaterThanOrEqualTo(0.1);

    /**
     * Test of runTask method, of class AbstractAPSS2.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testRunTask() throws Exception {

        NaiveApssTask<Tell> instance = new NaiveApssTask<Tell>();

        DoubleEnumeratingDeligate del = new DoubleEnumeratingDeligate(
                Enumerating.DEFAULT_TYPE, false, false, null, null);
        WeightedTokenPairSource mdbsa = WeightedTokenPairSource.open(
                TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false);
        WeightedTokenPairVectorSource vsa = mdbsa.getVectorSource();

        WeightedTokenPairSource mdbsb = WeightedTokenPairSource.open(
                TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false);
        WeightedTokenPairVectorSource vsb = mdbsb.getVectorSource();

        List<Weighted<TokenPair>> result = new ArrayList<Weighted<TokenPair>>();

        instance.setSourceA(vsa);
        instance.setSourceB(vsb);
        instance.setSink(ObjectIO.asSink(result));
        instance.setMeasure(MEASURE);
        instance.setProducatePair(PAIR_FILTER);

        instance.run();

        assertTrue(!result.isEmpty());
    }
}
