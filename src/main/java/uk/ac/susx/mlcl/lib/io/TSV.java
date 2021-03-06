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

import com.google.common.base.CharMatcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.MiscUtil;

import javax.annotation.WillClose;
import java.io.*;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.MessageFormat;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class TSV {

    private static final char RECORD_DELIMITER = '\n';

    private static final char VALUE_DELIMITER = '\t';

    private final File file;

    private final Charset charset;

    long column;

    TSV(File file, Charset charset) {
        Checks.checkNotNull("file", file);
        Checks.checkNotNull("charset", charset);
        this.file = file;
        this.charset = charset;
        column = 0;
    }

    Charset getCharset() {
        return charset;
    }

    public long getColumn() {
        return column;
    }

    File getFile() {
        return file;
    }

    /**
     * Class that holds functionality to read a Tab Separated Values file.
     *
     * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
     */
    public static final class Sink extends TSV implements Flushable, DataSink {

        private static final Log LOG = LogFactory.getLog(Sink.class);

        private final BufferedWriter out;

        private boolean open;

        @SuppressWarnings("DuplicateThrows")
        public Sink(File file, Charset charset)
                throws FileNotFoundException, IOException {
            super(file, charset);
            if (LOG.isDebugEnabled())
                LOG.debug("Opening file \"" + file + "\" for writing.");
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
            open = true;
        }

        @Override
        public void endOfRecord() throws IOException {
            writeRecordDelimiter();
        }

        private void writeRecordDelimiter() throws IOException {
            out.append(RECORD_DELIMITER);
            column = 0;
        }

        private void writeValueDelimiter() throws IOException {
            out.append(VALUE_DELIMITER);
        }

        @Override
        public void writeString(String str) throws IOException {
            Checks.checkNotNull("str", str);

            assert str.indexOf(VALUE_DELIMITER) == -1;
            assert str.indexOf(RECORD_DELIMITER) == -1;
            if (column > 0)
                writeValueDelimiter();
            out.append(str);
            ++column;
        }

        @Override
        public void writeInt(int val) throws IOException {
            writeString(Integer.toString(val));
        }

        private final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("###0.0#####;-###0.0#####");

        @Override
        public void writeDouble(double val) throws IOException {
            if (Double.compare(Math.floor(val), val) == 0) {
                writeInt((int) val);
            } else {
                writeString(DOUBLE_FORMAT.format(val));
            }
        }

        @Override
        public void writeChar(char val) throws IOException {
            writeInt(val);
        }

        @Override
        public void writeByte(byte val) throws IOException {
            writeInt(val);
        }

        @Override
        public void writeShort(short val) throws IOException {
            writeInt(val);
        }

        @Override
        public void writeLong(long val) throws IOException {
            writeString(Long.toString(val));
        }

        @Override
        public void writeFloat(float val) throws IOException {
            writeString(Float.toString(val));
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        @WillClose
        public void close() throws IOException {
            out.close();
            open = false;
        }

        @Override
        public void flush() throws IOException {
            out.flush();
        }
    }

    /**
     * Class that holds functionality to read a Tab Separated Values file.
     *
     * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
     */
    public static final class Source extends TSV implements SeekableDataSource {

        private final Lexer lexer;

        public Source(File file, Charset charset) throws IOException {
            super(file, charset);

            if (!file.exists())
                throw new FileNotFoundException("Path does not exist: " + file);
            if (!file.isFile())
                throw new IllegalArgumentException("Path is not a normal file: " + file);
            if (!file.canRead())
                throw new IllegalArgumentException("File is not readable: " + file);

            lexer = new Lexer(file, charset);
            lexer.setDelimiterMatcher(CharMatcher.anyOf("\n\t"));
            lexer.setWhitespaceMatcher(CharMatcher.NONE);
            if (lexer.hasNext())
                lexer.advance();
        }

        @Override
        public void position(Tell offset) throws IOException {
            column = offset.value(Long.class);
            lexer.position(offset.next());
        }

        public double percentRead() throws IOException {
            return 100d * lexer.bytesRead() / lexer.bytesTotal();
        }

        public long roughPosition() {
            return lexer.start();
        }

        @Override
        public Tell position() {
            return lexer.position().push(Long.class, column);
        }

        @Override
        public boolean canRead() throws IOException {
            return lexer.hasNext();
        }

        @Override
        public void skipRecord() throws IOException {
            while(lexer.hasNext() && !isEndOfRecordNext()) {
                lexer.advance();
            }
            if(lexer.hasNext() && isEndOfRecordNext())
                lexer.advance();
        }

        @Override
        public boolean isEndOfRecordNext() throws IOException {
            return isDelimiterNext() && lexer.charAt(0) == RECORD_DELIMITER;
        }

        private boolean isDelimiterNext() {
            return lexer.type() == Lexer.Type.Delimiter;
        }

        @Override
        public void endOfRecord() throws IOException {
            parseDelimiter(RECORD_DELIMITER);
            column = 0;
        }

        @Override
        public String readString() throws IOException {
            if (column > 0)
                parseDelimiter(VALUE_DELIMITER);

            expectType(Lexer.Type.Value, lexer.type());
            final String str = lexer.value().toString();
            lexer.advanceIfPossible();
            ++column;
            return str;
        }

        @Override
        public double readDouble() throws IOException {
            final String str = readString();
            try {
                return Double.valueOf(str);
            } catch (NumberFormatException nfe) {
                throw new TSVDataFormatException(this, MessageFormat.format(
                        "Caused by NumberFormatException parsing string \"{0}\"", str), nfe);
            }
        }

        @Override
        public int readInt() throws IOException {
            final String str = readString();
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException nfe) {
                throw new TSVDataFormatException(this, MessageFormat.format(
                        "Caused by NumberFormatException parsing string \"{0}\"", str), nfe);
            }
        }

        private void parseDelimiter(char delimiter) throws IOException {
            expectType(Lexer.Type.Delimiter, lexer.type());
            expectDelimiter(delimiter, lexer.charAt(0));
            lexer.advanceIfPossible();
        }

        private void expectType(Lexer.Type expected, Lexer.Type actual) throws TSVDataFormatException {
            if (expected != actual) {
                throw new TSVDataFormatException(this,
                        "Expecting type " + expected + " but found " + actual);
            }
        }

        private void expectDelimiter(char expected, char actual) throws TSVDataFormatException {
            if (expected != actual)
                throw new TSVDataFormatException(this, MessageFormat.format("Expecting delimiter {0} but found {1}.",
                        MiscUtil.printableUTF8(expected), MiscUtil.printableUTF8(actual)));
        }

        @Override
        public boolean isOpen() {
            return lexer.isOpen();
        }

        @Override
        @WillClose
        public void close() throws IOException {
            lexer.close();
        }

        @Override
        public byte readByte() throws IOException {
            return (byte) readInt();
        }

        @Override
        public char readChar() throws IOException {
            return (char) readInt();
        }

        @Override
        public short readShort() throws IOException {
            final String str = readString();
            try {
                return Short.parseShort(str);
            } catch (NumberFormatException nfe) {
                throw new TSVDataFormatException(this, MessageFormat.format(
                        "Caused by NumberFormatException parsing string \"{0}\"", str), nfe);
            }
        }

        @Override
        public long readLong() throws IOException {
            final String str = readString();
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException nfe) {
                throw new TSVDataFormatException(this, MessageFormat.
                        format("Caused by NumberFormatException parsing string \"{0}\"", str), nfe);
            }
        }

        @Override
        public float readFloat() throws IOException {
            final String str = readString();
            try {
                return Float.parseFloat(str);
            } catch (NumberFormatException nfe) {
                throw new TSVDataFormatException(this, MessageFormat.
                        format("Caused by NumberFormatException parsing string \"{0}\"", str), nfe);
            }
        }
    }

    /**
     * @author Hamish Morgan &lg;hamish.morgan@sussex.ac.uk&gt;
     */
    public static class TSVDataFormatException extends DataFormatException {

        private static final long serialVersionUID = 1L;

        private static final String DEFAULT_MESSAGE = "Invalid format found when parsing TSV file.";

        public TSVDataFormatException(Source src, final String message) {
            super(message, src.roughPosition(),src.getFile(),  src.getCharset());
        }

        public TSVDataFormatException(Source src, String message, Throwable cause) {
            super(message, cause,src.roughPosition(),src.getFile(),  src.getCharset());
        }

        public TSVDataFormatException(Source src) {
            super(DEFAULT_MESSAGE, src.roughPosition(), src.getFile(), src.getCharset());
        }

        public TSVDataFormatException(Source src, Throwable cause) {
            super(DEFAULT_MESSAGE, cause, src.roughPosition(), src.getFile(), src.getCharset());
        }
    }


}
