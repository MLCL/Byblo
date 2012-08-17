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
package uk.ac.susx.mlcl.byblo.enumerators;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.jdbm.DB;
import org.apache.jdbm.DBMaker;
import org.junit.Test;

/**
 *
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


        Map<String, String> map = db.<String, String>createHashMap("hashmap");

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

        // GC until memory is stable
        while (used0 != used1) {
            System.gc();
            used0 = used1;
            used1 = memoryUsed();

        }

        return used1;

    }
}
