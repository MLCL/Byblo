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

import uk.ac.susx.mlcl.lib.io.IOUtil;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;
import uk.ac.susx.mlcl.lib.SimpleEnumerator;
import uk.ac.susx.mlcl.lib.io.TSVSink;
import uk.ac.susx.mlcl.lib.io.TSVSource;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class WeightedEntryPairTest {

    private void copyWEP(File a, File b, boolean compact) throws FileNotFoundException, IOException {
        Enumerator<String> idx = Enumerators.newDefaultStringEnumerator();

        WeightedTokenPairSource aSrc = new WeightedTokenPairSource(
                new TSVSource(a, DEFAULT_CHARSET), Token.stringDecoder(idx),Token.stringDecoder(idx));
        WeightedTokenPairSink bSink = new WeightedTokenPairSink(
                new TSVSink(b, DEFAULT_CHARSET),Token.stringEncoder(idx), Token.stringEncoder(idx));
        bSink.setCompactFormatEnabled(compact);

        IOUtil.copy(aSrc, bSink);
        bSink.close();
    }

    @Test
    public void testWeightedEntryFeaturesConversion() throws FileNotFoundException, IOException {
        File a = TEST_FRUIT_SIMS;
        File b = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_SIMS.getName() + ".compact");
        File c = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_SIMS.getName() + ".verbose");

        copyWEP(a, b, true);

        assertTrue("Compact copy is smaller that verbose source.",
                   b.length() <= a.length());

        copyWEP(b, c, false);


        assertTrue("Verbose copy is smaller that compact source.",
                   c.length() >= b.length());
        assertTrue("Double converted file is not equal to origion.",
                   Files.equal(a, c));
    }

    @Test
    public void testEntryPairEnumeratorConversion() throws FileNotFoundException, IOException {
        File a = TEST_FRUIT_SIMS;
        File b = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_SIMS.getName() + ".enum");
        File c = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_SIMS.getName() + ".str");

        Enumerator<String> idx = Enumerators.newDefaultStringEnumerator();

        {
            WeightedTokenPairSource aSrc = new WeightedTokenPairSource(
                    new TSVSource(a, DEFAULT_CHARSET), 
                    Token.stringDecoder(idx), 
                    Token.stringDecoder(idx));
            WeightedTokenPairSink bSink = new WeightedTokenPairSink(
                    new TSVSink(b, DEFAULT_CHARSET));
            IOUtil.copy(aSrc, bSink);
            bSink.close();
        }

        assertTrue("Compact copy is smaller that verbose source.",
                   b.length() <= a.length());

        {
            WeightedTokenPairSource bSrc = new WeightedTokenPairSource(new TSVSource(b, DEFAULT_CHARSET));
            WeightedTokenPairSink cSink = new WeightedTokenPairSink(
                    new TSVSink(c, DEFAULT_CHARSET), 
                    Token.stringEncoder(idx), Token.stringEncoder(idx));
            IOUtil.copy(bSrc, cSink);
            cSink.close();
        }

        assertTrue("Verbose copy is smaller that compact source.",
                   c.length() >= b.length());
        assertTrue("Double converted file is not equal to origion.",
                   Files.equal(a, c));
    }

}
