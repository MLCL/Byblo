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
package uk.ac.susx.mlcl.byblo;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.jdbm.DB;
import org.apache.jdbm.DBMaker;
import static org.junit.Assert.*;
import org.junit.Test;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.lib.MiscUtil;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class JDBMTest {

    @Test
    public void basicExample() throws IOException {
        /**
         * create (or open existing) database using builder pattern
         */
        final String fileName = "jdbc-basicExample";
        File file = new File(TEST_OUTPUT_DIR, fileName);


        {
            deleteIfExist(file.getParentFile().listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(fileName);
                }

            }));

            assertValidOutputFiles(file);

            DB db = DBMaker.openFile(file.getPath()).make();


            /**
             * Creates TreeMap which stores data in database.
             */
            SortedMap<Integer, String> actual = db.createTreeMap("mapName");

            assertEquals(actual, Collections.emptyMap());

            /**
             * add some stuff to map
             */
            actual.put(1, "One");
            actual.put(2, "Two");
            actual.put(3, "Three");


            SortedMap<Integer, String> expected = new TreeMap<Integer, String>();
            expected.put(1, "One");
            expected.put(2, "Two");
            expected.put(3, "Three");

            assertEquals(actual, expected);

            db.commit();

            assertEquals(actual, expected);

            /**
             * Delete one record. Changes are not commited yet, but are visible.
             */
            actual.remove(2);

            expected.remove(2);
            assertEquals(actual, expected);

            db.rollback();

            expected = new TreeMap<Integer, String>();
            expected.put(1, "One");
            expected.put(2, "Two");
            expected.put(3, "Three");

            assertArrayEquals(actual.values().toArray(), expected.values().toArray());
            assertArrayEquals(actual.keySet().toArray(), expected.keySet().toArray());


            db.close();
        }

        {
            DB db = DBMaker.openFile(file.getPath()).make();
            SortedMap<Integer, String> actual = db.getTreeMap("mapName");

            SortedMap<Integer, String> expected = new TreeMap<Integer, String>();
            expected.put(1, "One");
            expected.put(2, "Two");
            expected.put(3, "Three");

            assertEquals(actual, expected);
        }

    }

    @Test(expected=OutOfMemoryError.class)
    public void fib() {


        SortedMap<Integer, BigInteger> map = new TreeMap<Integer, BigInteger>();
        map.put(0, BigInteger.ONE);
        map.put(1, BigInteger.ONE);
        map.put(2, BigInteger.valueOf(2));


        for (int i = 3; i < Integer.MAX_VALUE; i++) {
            map.put(i, map.get(i - 1).add(map.get(i - 2)));
            if (i % 10000 == 0) {
                System.out.println("Fib(" + i + ") = " + map.get(i));
                System.out.println(MiscUtil.memoryInfoString());
            }
        }

    }

    @Test
    public void fib2() throws IOException {

        final String fileName = "jdbc-fib";
        File file = new File(TEST_OUTPUT_DIR, fileName);
        deleteIfExist(file.getParentFile().listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(fileName);
            }

        }));

        assertValidOutputFiles(file);

        DBMaker maker = DBMaker.openFile(file.getPath());
        DB db = maker.make();

        SortedMap<Integer, BigInteger> map = db.createTreeMap("fib");

        map.put(0, BigInteger.ONE);
        map.put(1, BigInteger.ONE);
        map.put(2, BigInteger.valueOf(2));


        long bits = 3;
        for (int i = 3; i < 50000; i++) {
            BigInteger x = map.get(i - 1).add(map.get(i - 2));
            map.put(i, x);
            bits += x.bitLength();
            if (i % 5000 == 0) {
                System.out.println(MiscUtil.memoryInfoString());
                System.out.println("bits: " + bits);
                db.commit();
            }
        }

        System.out.println(db.calculateStatistics());
        db.defrag(false);
        System.out.println(db.calculateStatistics());
        db.clearCache();
        System.out.println(db.calculateStatistics());

    }

}
