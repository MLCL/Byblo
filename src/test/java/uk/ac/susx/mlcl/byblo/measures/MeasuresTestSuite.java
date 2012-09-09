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
package uk.ac.susx.mlcl.byblo.measures;

/*
 * Copyright (c) 2010, Hamish Morgan. All Rights Reserved.
 */

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author hamish
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        uk.ac.susx.mlcl.byblo.measures.MeasuresTest.class,
        uk.ac.susx.mlcl.byblo.measures.impl.WeedsTest.class,
        uk.ac.susx.mlcl.byblo.measures.impl.ConfusionTest.class,
        uk.ac.susx.mlcl.byblo.measures.impl.DiceTest.class,
        uk.ac.susx.mlcl.byblo.measures.impl.HindleTest.class,
        uk.ac.susx.mlcl.byblo.measures.impl.KullbackLeiblerDivergenceTest.class,
        uk.ac.susx.mlcl.byblo.measures.impl.JensenShannonDivergenceTest.class,
        uk.ac.susx.mlcl.byblo.measures.impl.DotProductTest.class,
        uk.ac.susx.mlcl.byblo.measures.impl.LpSpaceDistanceTest.class,
        uk.ac.susx.mlcl.byblo.measures.impl.LinTest.class,
        uk.ac.susx.mlcl.byblo.measures.impl.PrecisionTest.class,
        uk.ac.susx.mlcl.byblo.measures.impl.JaccardTest.class,
        uk.ac.susx.mlcl.byblo.measures.impl.RecallTest.class,
        uk.ac.susx.mlcl.byblo.measures.impl.CosineTest.class,
        uk.ac.susx.mlcl.byblo.measures.impl.LeeSkewDivergenceTest.class,
        uk.ac.susx.mlcl.byblo.measures.impl.OverlapTest.class,
        uk.ac.susx.mlcl.byblo.measures.impl.KendallsTauTest.class,
        uk.ac.susx.mlcl.byblo.measures.impl.LambdaDivergenceTest.class})
public class MeasuresTestSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
}
