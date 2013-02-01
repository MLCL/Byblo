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
package uk.ac.susx.mlcl.lib.io;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import uk.ac.susx.mlcl.TestConstants;
import uk.ac.susx.mlcl.testing.AbstractObjectTest;
import uk.ac.susx.mlcl.testing.SlowTestCategory;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

import static org.junit.Assert.*;

/**
 * Test cases for the CharFileChannel class.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class CharFileChannelTest extends AbstractObjectTest<CharFileChannel> {

    @Override
    protected Class<? extends CharFileChannel> getImplementation() {
        return CharFileChannel.class;
    }

    public static File makeTempFile(int size) throws IOException {
        final File file = new File(TestConstants.TEST_OUTPUT_DIR, CharFileChannelTest.class.getName() + ".small-sample");
        if (!file.exists()) {
            System.out.println(" > Creating data file: " + file);
            OutputStream out = null;
            try {
                out = new BufferedOutputStream(new FileOutputStream(file));
                byte[] data = new byte[1024];
                newRandom().nextBytes(data);
                int i = 0;
                while (i < size) {
                    out.write(data, 0, Math.min(data.length, size - i));
                    i += data.length;
                }
                out.flush();
            } finally {
                if (out != null)
                    out.close();
            }
        }
        return file;
    }

    private static int SMALL_SAMPLE_SIZE = 1024 * 1024;


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @Test
    public void testReadLargeBuffer() throws Exception {
        testReadSmallBuffer(SMALL_SAMPLE_SIZE / 3 | 1);
    }


    @Test
    public void testReadSmallBuffer() throws Exception {
        testReadSmallBuffer(15);
    }

    @Test
    public void testReadSizeOneBuffer() throws Exception {
        testReadSmallBuffer(1);
    }

    static void testReadSmallBuffer(int bufferSize) throws Exception {

        File smallSampleFile = makeTempFile(SMALL_SAMPLE_SIZE);
//        FileInputStream inputStream = null;
        RandomAccessFile raf = null;

        FileChannel fileChannel = null;
        CharFileChannel instance = null;

        try {
            raf = new RandomAccessFile(smallSampleFile, "rw");
            fileChannel = raf.getChannel();
            instance = new CharFileChannel(fileChannel, Charset.defaultCharset());

            final CharBuffer dst = CharBuffer.allocate(bufferSize);
//            final CharBuffer dst = CharBuffer.wrap(new char[bufferSize]);

            long charsRead = 0;
            long n;
            long pos = instance.position();
            while ((n = instance.read(dst)) != 0) {
//            System.out.printf(" > read=%d, pos=%d, size=%d, remaining=%d, more=%s%n",
//                    n, instance.position(), instance.size(), instance.bytesRemaining(), instance.hasBytesRemaining());

                assert instance.position() > pos;
                assert instance.position() <= instance.size();

                pos = instance.position();
                charsRead += n;
                dst.flip();
                dst.clear();
            }

            Assert.assertFalse(instance.hasBytesRemaining());
            Assert.assertEquals(0, instance.bytesRemaining());
            Assert.assertEquals(instance.size(), (long) instance.position());


            // For all file system this should hold
            assertTrue(charsRead <= smallSampleFile.length());

        } finally {
            if (instance != null)
                instance.close();
            if (fileChannel != null)
                fileChannel.close();
            if (raf != null) {
                raf.close();
            }
        }
        assertClosed(instance);
    }

    @Test
    public void testSeekable() throws Exception {

        System.out.println("Testing position() and position(long)");

        File smallSampleFile = makeTempFile(SMALL_SAMPLE_SIZE);

        CharFileChannel instance = new CharFileChannel(
                new FileInputStream(smallSampleFile).getChannel(), Charset.defaultCharset());

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
        assertTrue(charsRead <= smallSampleFile.length());

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

        instance.close();
        assertClosed(instance);
    }

    @Test
    public void testIsOpen() throws FileNotFoundException, IOException {
        System.out.println("Testing isOpen() and close()");

        File smallSampleFile = makeTempFile(SMALL_SAMPLE_SIZE);

        CharFileChannel instance = new CharFileChannel(
                new FileInputStream(smallSampleFile).getChannel(),
                Files.DEFAULT_CHARSET);

        assertEquals(true, instance.isOpen());
        instance.close();

        assertClosed(instance);
    }

    @Test
//    @Ignore(value = "Test creates a massive (over 2 GB) file to test address "
//            + "outside of 32bits. Hense it is not suitable for all users to "
//            + "run.")
    @Category(SlowTestCategory.class)
    public void testOpenVeryLargeFile() throws Exception {
        System.out.println("Testing very large file mapping.");

        File hugeSampleFile = new File(TestConstants.TEST_OUTPUT_DIR, getClass().getName() + ".huge-sample");

        if (!hugeSampleFile.exists()) {
            // Create a file that is too big for a single map
            System.out.println(" + Creating temporary file: " + hugeSampleFile);

            System.out.println(
                    " + Truncating temporary file to " + ((1L << 31L) + 1) + " bytes.");
            RandomAccessFile raf = new RandomAccessFile(hugeSampleFile, "rw");
            raf.seek(((1L << 31L) + 1));
            raf.writeUTF("THE END");
            raf.close();
        }

        System.out.println(" + Testing CharFileChannel");

        CharFileChannel instance = new CharFileChannel(new FileInputStream(hugeSampleFile).getChannel(), Files.DEFAULT_CHARSET);
        instance.setMaxMappedBytes(1000000);

        assertEquals(true, instance.isOpen());
        instance.insureMapped(10000, 0);

        instance.close();

        assertClosed(instance);
    }


    @Test
    @Ignore(value = "writing is not currently enabled or supported by CharFileChannel")
    public void testWrite() throws IOException {
        File file = new File(TestConstants.TEST_OUTPUT_DIR, "blahblahblah");

        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fileChannel = raf.getChannel();
        CharFileChannel charChannel = new CharFileChannel(fileChannel);

        CharBuffer buf = CharBuffer.wrap("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut at nunc eu " +
                "massa hendrerit porta eu in dui.");
        charChannel.write(buf);

        charChannel.close();
        fileChannel.close();
        raf.close();

    }

    static void assertClosed(CharFileChannel channel) throws IOException {
        Assert.assertFalse(channel.isOpen());
        try {
            CharBuffer dst = CharBuffer.allocate(100);
            channel.read(dst);
            fail("ClosedChannelException should have been thrown.");
        } catch (ClosedChannelException ex) {
            // Yay (this is supposed to happen)
        }
    }
}
