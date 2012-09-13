/*
 * Copyright (c) 2011-2012, University of Sussex
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
package uk.ac.susx.mlcl.lib.io;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.io.File;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.HashMap;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for the CharFileChannel class.
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class CharFileChannelTest {

    public static File makeTempFile(int size) throws IOException {
        final File file = File.createTempFile(CharFileChannelTest.class.getName(),
                                              ".tmp");
        final OutputStream out = new BufferedOutputStream(
                new FileOutputStream(file));
        byte[] data = new byte[1024];
        new Random().nextBytes(data);
        int i = 0;
        while (i < size) {
            out.write(data, 0, Math.min(data.length, size - i));
            i += data.length;
        }
        out.flush();
        out.close();
        return file;
    }

    private static int SMALL_SAMPLE_SIZE = 1024 * 1024;

    private static File SMALL_SAMPLE_FILE;

    @BeforeClass
    public static void setUpClass() throws Exception {
        SMALL_SAMPLE_FILE = makeTempFile(SMALL_SAMPLE_SIZE);
        SMALL_SAMPLE_FILE.deleteOnExit();
    }

    @Test
    public void testRead() throws Exception {
        System.out.println("Testing read(CharBuffer)");

        CharFileChannel instance = new CharFileChannel(
                new FileInputStream(SMALL_SAMPLE_FILE).getChannel(), Charset.defaultCharset());

        CharBuffer dst = CharBuffer.allocate(777773);

        long charsRead = 0;
        long n;
        long pos = instance.position();
        while ((n = instance.read(dst)) != 0) {
            pos = instance.position();
            charsRead += n;
            dst.flip();
            dst.clear();
        }
        // For all file system this should hold
        assertTrue(charsRead <= SMALL_SAMPLE_FILE.length());

        // However the following is system dependant
//        assertEquals(charsRead, SMALL_SAMPLE_FILE.length());
    }

    @Test
    public void testReadSmallBuffer() throws Exception {
        System.out.println("Testing read(CharBuffer) with small buffer");

        CharFileChannel instance = new CharFileChannel(
                new FileInputStream(SMALL_SAMPLE_FILE).getChannel(), Charset.defaultCharset());

        CharBuffer dst = CharBuffer.allocate(1 << 4);

        long charsRead = 0;
        long n;
        long pos = instance.position();
        while ((n = instance.read(dst)) != 0) {
            pos = instance.position();
            charsRead += n;
            dst.clear();
        }
        System.out.println(charsRead + " chars read");

        // For all file system this should hold
        assertTrue(charsRead <= SMALL_SAMPLE_FILE.length());

        // However the following is system dependant
//        assertEquals(charsRead, SMALL_SAMPLE_FILE.length());
    }

    @Test
    public void testSeekable() throws Exception {

        System.out.println("Testing position() and position(long)");


        CharFileChannel instance = new CharFileChannel(
                new FileInputStream(SMALL_SAMPLE_FILE).getChannel(), Charset.defaultCharset());

        int charbuffersize = 1001;

        Map<Long, CharBuffer> results = new HashMap<Long, CharBuffer>();
        long charsRead = 0;
        long n = -1;
        while (n != 0) {
            long pos = instance.position();
            CharBuffer dst = CharBuffer.allocate(charbuffersize);
            n = instance.read(dst);
            dst.flip();

            results.put(pos, dst);
            charsRead += n;
        }
        // For all file system this should hold
        assertTrue(charsRead <= SMALL_SAMPLE_FILE.length());

        // However the following is system dependant
//        assertEquals(charsRead, SMALL_SAMPLE_FILE.length());

        List<Entry<Long, CharBuffer>> entries =
                new ArrayList<Entry<Long, CharBuffer>>(results.entrySet());

        Random rand = new Random(0);
        for (int i = 0; i < 100; i++) {
            int j = rand.nextInt(results.size());

            long pos = entries.get(j).getKey();
            CharBuffer expectedResult = entries.get(j).getValue();

            instance.position(pos);
            CharBuffer result = CharBuffer.allocate(charbuffersize);
            n = instance.read(result);
            result.flip();

            assertEquals(expectedResult.length(), result.length());
            assertEquals(expectedResult, result);

        }
    }

    @Test
    public void testIsOpen() throws FileNotFoundException, IOException {
        System.out.println("Testing isOpen() and close()");


        CharFileChannel instance = new CharFileChannel(
                new FileInputStream(SMALL_SAMPLE_FILE).getChannel(),
                Files.DEFAULT_CHARSET);

        assertEquals(true, instance.isOpen());
        instance.close();
        assertEquals(false, instance.isOpen());

        try {
            CharBuffer dst = CharBuffer.allocate(777773);
            instance.read(dst);
            fail("Exception should have been thrown.");
        } catch (ClosedChannelException ex) {
            // Yay (this is supposed to happen)
        }
    }

    @Test
    @Ignore(value = "Test creates a massive (over 2 GB) file to test address "
    + "outside of 32bits. Hense it is not suitable for all users to "
    + "run.")
    public void testOpenVeryLargeFile() throws Exception {
        System.out.println("Testing very large file mapping.");

        // Create a file that is too big for a single map
        File tmp = File.createTempFile(this.getClass().getName() + "-", "");
        System.out.println(" + Creating temporary file: " + tmp);
        tmp.deleteOnExit();


        System.out.println(
                " + Truncating temporary file to " + ((1L << 31L) + 1) + " bytes.");
        RandomAccessFile raf = new RandomAccessFile(tmp, "rw");
        raf.seek(((1L << 31L) + 1));
        raf.writeUTF("THE END");
        raf.close();

        System.out.println(" + Testing CharFileChannel");

        CharFileChannel instance = new CharFileChannel(
                new FileInputStream(tmp).getChannel(),
                Files.DEFAULT_CHARSET);
        instance.setMaxMappedBytes(1000000);

        assertEquals(true, instance.isOpen());
        instance.insureMapped(10000);

        instance.close();
        assertEquals(false, instance.isOpen());

        try {
            CharBuffer dst = CharBuffer.allocate(777773);
            instance.read(dst);
            fail("Exception should have been thrown.");
        } catch (ClosedChannelException ex) {
            // Yay (this is supposed to happen)
        }
    }

}
