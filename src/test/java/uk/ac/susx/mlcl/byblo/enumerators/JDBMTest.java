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

import org.apache.jdbm.DB;
import org.apache.jdbm.DBMaker;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;

/**
 * @author hiam20
 */
public class JDBMTest {

    @Test
    public void memoryLeakTest() throws IOException {
        int size = 20000;

        System.out.println(memoryUsedWithGC());

        makeTemporaryDB(size);

        System.out.println(memoryUsedWithGC());

        makeTemporaryDB(size);

        System.out.println(memoryUsedWithGC());

        makeTemporaryDB(size);

        System.out.println(memoryUsedWithGC());

    }

    void makeTemporaryDB(int size) throws IOException {
        File tmpFile = File.createTempFile("jdbm-memoryleaktest-", "-tmp");
        tmpFile.deleteOnExit();
        DBMaker maker = DBMaker.openFile(tmpFile.getPath());

        maker.disableTransactions();
        maker.disableLocking();
        maker.enableMRUCache();
        maker.setMRUCacheSize(1000000);
        maker.closeOnExit();

        DB db = maker.make();


        Map<String, String> map = db.createHashMap("hashmap");

        for (int i = 0; i < size; i++) {
            String s1 = Double.toString(Math.random());
            String s2 = Double.toString(Math.random());
            map.put(s1, s2);

            if (i % 1000 == 0) {
                db.commit();
            }
        }

        db.commit();
        db.close();


    }

    long memoryUsed() {
        return Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory();

    }

    long memoryUsedWithGC() {

        long used0 = 0;
        long used1 = memoryUsed();

        // GC until memory is stable --- "extremely dubious" is what this kind
        // of code has been described as before, but in this case it's fine I 
        // promise. It's needed or benchmarking.
        while (used0 != used1) {
            System.gc();
            used0 = used1;
            used1 = memoryUsed();

        }

        return used1;

    }

    @Test
    @Ignore
    public void fooTest() {
        final File dataPath = new File("/Volumes/LocalScratchHD/LocalHome/Projects/Byblo/data/jw-wiki-problem");
        final File entryIndexFile = new File(dataPath, "wikipedia_nounsdeps_t100.pbfiltered.entry-index");
        final File featureIndexFile = new File(dataPath, "wikipedia_nounsdeps_t100.pbfiltered.feature-index");

        foo(entryIndexFile);
//        foo(featureIndexFile);
    }

    void foo(File file) {

        final DBMaker maker = DBMaker.openFile(file.toString());
        maker.disableTransactions();
        maker.disableLocking();
        maker.enableMRUCache();
        maker.setMRUCacheSize(100000);

        final DB db = maker.make();


        System.out.printf("Stats for db %s:%n%s", file, db.calculateStatistics());

        System.out.println("COLLECTIONS:");
        for (Map.Entry<String, Object> entry : db.getCollections().entrySet()) {
            System.out.printf("  %s (%s)%n", entry.getKey(), entry.getValue().getClass().getCanonicalName());
            if (entry.getValue() instanceof Set) {
                System.out.println("    interface: set");
                System.out.printf("    size: %d%n", ((Set) entry.getValue()).size());
            } else if (entry.getValue() instanceof Map) {
                System.out.println("    interface: map");
                System.out.printf("    size: %d%n", ((Map) entry.getValue()).size());
            } else {
                System.out.println("    interface: unknown");
                System.out.printf("    size: unknown%n");
            }
        }

        System.out.println("PROPERTIES:");
        for (Map.Entry<Object, Object> entry : db.getHashMap("properties").entrySet()) {
            System.out.printf("  %s = %s%n", entry.getKey(), entry.getValue());
        }


        final Map<Integer, String> forwards = db.getHashMap("forwards");
        final Map<String, Integer> backwards = db.getHashMap("backwards");


        System.out.println("BIMAP INCONSISTENCIES:");
        for (int id : forwards.keySet()) {
            String str = forwards.get(id);
//
//            if (!backwards.containsKey(str))
//                System.out.println("  " + id + " => " + str);
//            else if (backwards.get(str) != id)
//                System.out.println("  " + id + " => " + str + " => " + backwards.get(str));

            Assert.assertTrue(MessageFormat.format("Forwards mapping contained pair {0} => {1}, but {1} is not a key " +
                    "in the backwards mapping.", id, str), backwards.containsKey(str));
            Assert.assertTrue(MessageFormat.format("Forwards mapping contained pair {0} => {1}, but backwards mapping " +
                    "contained {1}=>{2}", id, str, backwards.get(str)), backwards.get(str).equals(id));

        }

        for (String str : backwards.keySet()) {
            int id = backwards.get(str);

            Assert.assertTrue(MessageFormat.format("Backwards mapping contained pair {0} => {1}, but {1} is not a key " +
                    "in the forwards mapping.", str, id), forwards.containsKey(id));
            Assert.assertTrue(MessageFormat.format("Backwards mapping contained pair {0} => {1}, but forwards mapping " +
                    "contained {1}=>{2}", str, id, forwards.get(id)), forwards.get(id).equals(str));

            if (!forwards.containsKey(id))
                System.out.println("  " + str + " => " + id + " => ??????");
            else if (!forwards.get(id).equals(str))
                System.out.println("  " + str + " => " + id + " => " + forwards.get(id));
        }
    }
}

