/*
 * Copyright (c) 2010-2013, University of Sussex
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
package uk.ac.susx.mlcl.byblo.enumerators;

import org.apache.jdbm.DBMaker;
import org.junit.*;
import uk.ac.susx.mlcl.lib.ZipfianDistribution;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Random;

import static uk.ac.susx.mlcl.TestConstants.TEST_OUTPUT_DIR;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class JDBMStringEnumeratorTest {

    public JDBMStringEnumeratorTest() {
    }

    private static int populationSize;

    private static double zipfExponent;

    private static int repeats;

    private static ZipfianDistribution zipfDist;

    @BeforeClass
    public static void setUpClass() throws Exception {
        populationSize = 10000;
        zipfExponent = 1;
        repeats = 10000;
        zipfDist = new ZipfianDistribution(populationSize, zipfExponent);
        new JDBMStringEnumeratorTest().performanceTest_JDBM_trans_mru100();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        zipfDist = null;
    }

    @Before
    public void setUp() {
        zipfDist.setRandom(new Random(0));
    }

    enum CacheType {

        MRU,
        Hard,
        Soft,
        Weak,
        None

    }

    @Test
    public void performanceTest_JDBM_trans_mru100() throws IOException {
        performanceTest_JDBM(false, true, CacheType.MRU, 100, false, false);
    }

    @Test
    public void performanceTest_JDBM_trans_weak() throws IOException {
        performanceTest_JDBM(false, true, CacheType.Weak, 0, false, false);
    }

    @Test
    public void performanceTest_JDBM_trans_soft() throws IOException {
        performanceTest_JDBM(false, true, CacheType.Soft, 0, false, false);
    }

    @Test
    public void performanceTest_JDBM_trans_hard() throws IOException {
        performanceTest_JDBM(false, true, CacheType.Hard, 0, false, false);
    }

    @Test
    public void performanceTest_JDBM_trans_none() throws IOException {
        performanceTest_JDBM(false, true, CacheType.None, 0, false, false);
    }

    @Test
    public void performanceTest_JDBM_noTrans_mru1() throws IOException {
        performanceTest_JDBM(false, false, CacheType.MRU, 0, false, false);
    }

    @Test
    public void performanceTest_JDBM_noTrans_mru25() throws IOException {
        performanceTest_JDBM(false, false, CacheType.MRU, 25, false, false);
    }

    @Test
    public void performanceTest_JDBM_noTrans_mru50() throws IOException {
        performanceTest_JDBM(false, false, CacheType.MRU, 50, false, false);
    }

    @Test
    public void performanceTest_JDBM_noTrans_mru100() throws IOException {
        performanceTest_JDBM(false, false, CacheType.MRU, 100, false, false);
    }

    @Test
    public void performanceTest_JDBM_noTrans_mru200() throws IOException {
        performanceTest_JDBM(false, false, CacheType.MRU, 200, false, false);
    }

    @Test
    public void performanceTest_JDBM_noTrans_mru400() throws IOException {
        performanceTest_JDBM(false, false, CacheType.MRU, 400, false, false);
    }

    @Test
    public void performanceTest_JDBM_noTrans_mru800() throws IOException {
        performanceTest_JDBM(false, false, CacheType.MRU, 800, false, false);
    }

    @Test
    public void performanceTest_JDBM_noTrans_mru1600() throws IOException {
        performanceTest_JDBM(false, false, CacheType.MRU, 1600, false, false);
    }

    @Test
    public void performanceTest_JDBM_noTrans_mru3200() throws IOException {
        performanceTest_JDBM(false, false, CacheType.MRU, 3200, false, false);
    }

    @Test
    public void performanceTest_JDBM_noTrans_weak() throws IOException {
        performanceTest_JDBM(false, false, CacheType.Weak, 0, false, false);
    }

    @Test
    public void performanceTest_JDBM_noTrans_soft() throws IOException {
        performanceTest_JDBM(false, false, CacheType.Soft, 0, false, false);
    }

    @Test
    public void performanceTest_JDBM_noTrans_hard() throws IOException {
        performanceTest_JDBM(false, false, CacheType.Hard, 0, false, false);
    }

    @Test
    public void performanceTest_JDBM_noTrans_none() throws IOException {
        performanceTest_JDBM(false, false, CacheType.None, 0, false, false);
    }

    @Test
    public void performanceTest_JDBM_noTrans_mru3200_noautoclear() throws IOException {
        performanceTest_JDBM(false, false, CacheType.MRU, 3200, true, false);
    }

    @Test
    public void performanceTest_JDBM_noTrans_weak_noautoclear() throws IOException {
        performanceTest_JDBM(false, false, CacheType.Weak, 0, true, false);
    }

    @Test
    public void performanceTest_JDBM_noTrans_soft_noautoclear() throws IOException {
        performanceTest_JDBM(false, false, CacheType.Soft, 0, true, false);
    }

    @Test
    public void performanceTest_JDBM_noTrans_hard_noautoclear() throws IOException {
        performanceTest_JDBM(false, false, CacheType.Hard, 0, true, false);
    }

    @Test
    public void performanceTest_JDBM_noTrans_mru3200_nolock() throws IOException {
        performanceTest_JDBM(false, false, CacheType.MRU, 3200, true, true);
    }

    @Test
    public void performanceTest_JDBM_noTrans_weak_nolock() throws IOException {
        performanceTest_JDBM(false, false, CacheType.Weak, 0, true, true);
    }

    @Test
    public void performanceTest_JDBM_noTrans_soft_nolock() throws IOException {
        performanceTest_JDBM(false, false, CacheType.Soft, 0, true, true);
    }

    @Test
    public void performanceTest_JDBM_noTrans_hard_nolock() throws IOException {
        performanceTest_JDBM(false, false, CacheType.Hard, 0, true, true);
    }

    @Test
    public void performanceTest_JDBM_mem_trans() throws IOException {
        performanceTest_JDBM(true, true, CacheType.None, 0, false, false);
    }

    @Test
    public void performanceTest_JDBM_mem_noTrans() throws IOException {
        performanceTest_JDBM(true, false, CacheType.None, 0, false, false);
    }

    private void performanceTest_JDBM(
            boolean memory, boolean trans, final CacheType cacheType,
            int mruSize,
            boolean disableCacheAutoClear, boolean disableLocking) throws IOException {

        final DBMaker maker;
        final File file;

        if (memory) {
            maker = DBMaker.openMemory();
            file = null;
        } else {
            file = File.createTempFile("jdbmtest", "", TEST_OUTPUT_DIR);
            if (!file.delete())
                throw new IOException("Unable to delete file " + file);
            maker = DBMaker.openFile(file.toString());
            maker.deleteFilesAfterClose();

        }
        if (!trans)
            maker.disableTransactions();

        maker.disableCache();
        switch (cacheType) {
            case Hard:
                maker.enableHardCache();
                break;
            case MRU:
                maker.enableMRUCache();
                maker.setMRUCacheSize(mruSize);
                break;
            case Soft:
                maker.enableSoftCache();
                break;
            case Weak:
                maker.enableWeakCache();
                break;
            case None:
                break;
            default:
                throw new AssertionError();

        }

        if (disableCacheAutoClear)
            maker.disableCacheAutoClear();

        if (disableLocking)
            maker.disableLocking();

        JDBMStringEnumerator idx = JDBMStringEnumerator.load(maker, file);


        performanceTest(idx);

        idx.save();
        idx.close();

    }

    @Test
    public void performanceTest_Mem() throws IOException {

        Enumerator<String> idx = MemoryBasedStringEnumerator.newInstance();


        performanceTest(idx);


    }

    private void performanceTest(Enumerator<String> idx) {

        Format fmt = new DecimalFormat("%010d");

        for (int i = 0; i < repeats; i++) {
            zipfDist.random();
            idx.indexOf(fmt.format(zipfDist.random()));
        }

        // Insure everything is indexed
        for (int i = 1; i < populationSize; i++) {
            idx.indexOf(fmt.format(zipfDist.random()));
        }


        for (int i = 0; i < repeats; i++) {
            zipfDist.random();
            idx.indexOf(fmt.format(zipfDist.random()));
        }

    }
}
