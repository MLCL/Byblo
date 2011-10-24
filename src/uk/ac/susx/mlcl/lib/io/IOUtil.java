/*
 * Copyright (c) 2010-2011, University of Sussex
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.Checks;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class IOUtil {

    private static final Log LOG = LogFactory.getLog(IOUtil.class);

    public static final File STDIN_FILE = new File("-");

    public static final File STDOUT_FILE = new File("-");

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");// Charset.defaultCharset();

    /*
     * XXX: The following fields are not final because they are changed to 
     * "REPORT" during unit tests. Obviously this is not ideal.
     */
    public static CodingErrorAction MALFORMED_INPUT_ACTION = CodingErrorAction.REPLACE;

    public static CodingErrorAction UNMAPPABLE_CHARACTER_ACTION = CodingErrorAction.REPLACE;

    private IOUtil() {
    }

    public static boolean isStdin(File file) {
        return file.getName().equals(STDIN_FILE.getName());
    }

    public static boolean isStdout(File file) {
        return file.getName().equals(STDOUT_FILE.getName());
    }

    public static boolean isGzip(File file) {
        return file.getName().toLowerCase().endsWith(".gz");
    }

    public static CharsetDecoder decoderFor(Charset charset) {
        return charset.newDecoder().
                onMalformedInput(MALFORMED_INPUT_ACTION).
                onUnmappableCharacter(UNMAPPABLE_CHARACTER_ACTION);
    }

    public static CharsetEncoder encoderFor(Charset charset) {
        return charset.newEncoder().
                onMalformedInput(MALFORMED_INPUT_ACTION).
                onUnmappableCharacter(UNMAPPABLE_CHARACTER_ACTION);
    }

    public static BufferedReader openReader(
            final File file, final Charset charset)
            throws FileNotFoundException, IOException {
        return new BufferedReader(new InputStreamReader(
                openInputStream(file), decoderFor(charset)));
    }

    public static BufferedWriter openWriter(
            final File file, final Charset charset)
            throws FileNotFoundException, IOException {
        return new BufferedWriter(
                new OutputStreamWriter(openOutputStream(file), encoderFor(
                charset)));
    }

    public static ReadableByteChannel openReadableByteChannel(final File file)
            throws FileNotFoundException, IOException, NullPointerException {
        if (file == null) {

            throw new NullPointerException("file is null");

        } else if (isStdin(file)) {

            if (LOG.isTraceEnabled())
                LOG.trace("Opening stdin channel.");
            return Channels.newChannel(System.in);

        } else if (isGzip(file)) {

            if (LOG.isTraceEnabled())
                LOG.trace(
                        "Opening gzip compressed readable file: \"" + file + "\".");
            return Channels.newChannel(
                    new GZIPInputStream(
                    new FileInputStream(file)));

        } else {

            if (LOG.isTraceEnabled())
                LOG.trace("Opening readable file channel: \"" + file + "\".");
            return new FileInputStream(file).getChannel();

        }
    }

    public static WritableByteChannel openWritableByteChannel(File file)
            throws FileNotFoundException, IOException, NullPointerException {
        if (file == null) {

            throw new NullPointerException("file is null");

        } else if (isStdout(file)) {

            if (LOG.isTraceEnabled())
                LOG.trace("Opening stdout channel.");
            return Channels.newChannel(System.out);

        } else if (isGzip(file)) {

            if (LOG.isTraceEnabled())
                LOG.trace(
                        "Opening writable gzip compressed file channel: \"" + file + "\".");
            return Channels.newChannel(
                    new GZIPOutputStream(
                    new FileOutputStream(file)));

        } else {

            if (LOG.isTraceEnabled())
                LOG.trace("Opening writable file channe;: \"" + file + "\".");
            return new FileOutputStream(file).getChannel();

        }
    }

    public static InputStream openInputStream(final File file)
            throws FileNotFoundException, IOException {
        if (file == null)
            throw new NullPointerException();
        if (isStdin(file)) {
            if (LOG.isTraceEnabled())
                LOG.trace("Opening stdin input stream.");
            return System.in;
        } else if (isGzip(file)) {
            if (LOG.isTraceEnabled())
                LOG.trace(
                        "Opening gzip compressed file input stream: \"" + file + "\".");
            return new GZIPInputStream(
                    new FileInputStream(file));
        } else {
            if (LOG.isTraceEnabled())
                LOG.trace("Opening file input stream: \"" + file + "\".");
            return new BufferedInputStream(new FileInputStream(file));
        }
    }

    public static OutputStream openOutputStream(File file)
            throws FileNotFoundException, IOException {
        if (file == null)
            throw new NullPointerException();
        if (isStdout(file)) {
            if (LOG.isTraceEnabled())
                LOG.trace("Opening stdout stream.");
            return System.out;
        } else if (isGzip(file)) {
            if (LOG.isTraceEnabled())
                LOG.trace("Opening gzip compressed file output stream: \""
                        + file + "\".");
            return new GZIPOutputStream(new FileOutputStream(file));
        } else {
            if (LOG.isTraceEnabled())
                LOG.trace("Opening file output stream: \"" + file + "\".");
            return new BufferedOutputStream(new FileOutputStream(file));
        }
    }

    public static String fileName(final File file, Object thing) {
        if (file == null || file.getName().equals("-")) {
            return (thing instanceof Readable || thing instanceof InputStream) ? "stdin"
                    : (thing instanceof Appendable || thing instanceof OutputStream) ? "stdout"
                    : "file";
        } else {
            return file.getName();
        }
    }

    public static void readAllLines(File file, Charset charset,
            Collection<? super String> lines)
            throws FileNotFoundException, IOException {
        BufferedReader reader = null;
        try {
            reader = IOUtil.openReader(file, charset);
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    public static void writeAllLines(File file, Charset charset,
            Collection<? extends String> lines)
            throws FileNotFoundException, IOException {

        BufferedWriter writer = null;
        try {
            writer = IOUtil.openWriter(file, charset);
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

    public static void readAll(File file, Charset charset, StringBuilder dst)
            throws FileNotFoundException, IOException {
        BufferedReader reader = null;
        try {
            reader = IOUtil.openReader(file, charset);
            char[] buf = new char[10000];
            int len = 0;
            while ((len = reader.read(buf)) != -1) {
                dst.append(buf, 0, len);
            }
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    public static void writeAll(File file, Charset charset, CharSequence src)
            throws FileNotFoundException, IOException {
        BufferedWriter writer = null;
        try {
            writer = IOUtil.openWriter(file, charset);
            writer.append(src);
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

    public static <T> Sink<T> asSink(final Collection<T> collection) {
        return new Sink<T>() {

            @Override
            public void write(T record) throws IOException {
                collection.add(record);
            }
        };
    }

    public static <T> Source<T> asSource(final Iterable<T> iterable) {
        return new Source<T>() {

            private final Iterator<? extends T> it = iterable.iterator();

            @Override
            public T read() {
                return it.next();
            }

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }
        };
    }

    public static <T> SeekableSource<T, Integer> asSource(
            final List<? extends T> list) {
        return new SeekableSource<T, Integer>() {

            private ListIterator<? extends T> it = list.listIterator();

            @Override
            public T read() {
                return it.next();
            }

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public void position(Integer offset) {
                it = list.listIterator(offset);
            }

            @Override
            public Integer position() {
                return it.nextIndex();
            }
        };
    }

    public static File createTempDir(String prefix, String suffix,
            File directory) throws IOException {
        final File temp = File.createTempFile(prefix, suffix, directory);
        if (!temp.delete() || !temp.mkdir() || !temp.isDirectory())
            throw new IOException("Failed to create temporary directory " + temp);
        return temp;
    }

    public static File createTempDir(String prefix, String suffix) throws IOException {
        return createTempDir(prefix, suffix, null);
    }

    public static <T> int copy(final Iterable<? extends T> source,
            final Sink<? super T> sink, final int limit) throws IOException {
        Checks.checkNotNull("source", source);
        Checks.checkNotNull("sink", sink);
        Checks.checkRangeIncl(limit, 0, Integer.MAX_VALUE);
        int count = 0;
        final Iterator<? extends T> it = source.iterator();
        while (it.hasNext() && count < limit) {
            sink.write(it.next());
            ++count;
        }
        return count;
    }

    public static <T> int copy(final Iterable<? extends T> source,
            final Sink<? super T> sink) throws IOException {
        return copy(source, sink, Integer.MAX_VALUE);
    }

    public static <T> int copy(final Source<? extends T> source,
            final Collection<? super T> sink, final int limit) throws IOException {
        Checks.checkNotNull("source", source);
        Checks.checkNotNull("sink", sink);
        Checks.checkRangeIncl(limit, 0, Integer.MAX_VALUE);
        int count = 0;
        while (source.hasNext() && count < limit) {
            sink.add(source.read());
            ++count;
        }
        return count;
    }

    public static <T> int copy(final Source<? extends T> source,
            final Collection<? super T> sink) throws IOException {
        return copy(source, sink, Integer.MAX_VALUE);
    }
}
