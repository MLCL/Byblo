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

import com.google.common.base.CharMatcher;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.CharacterCodingException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.RandomAccess;

/**
 * <p>A {@link Lexer} performs lexical analysis of some delimited input file. It
 *  produces lexemes (lexical tokens) that can be interpreted by a parser or
 *  handler of some sort.</p>
 *
 * <p>Written as a replacement for the many other tokenisers available, it
 *  offers a unique combination of features:</p>
 *
 * <ul>
 *  <li>Extremely fast, NIO based, buffered reading.</li>
 *
 *  <li>Semi-random access allowing return to any previous lexeme position.</li>
 *
 *  <li>No unnecessary object instantiation (e.g. {@link String} fragments).
 *      This is achieved using lazy iteration with an advance/accessor control.
 *      Insures that only the absolute minimum amount of work is done at each
 *      step.</li>
 *
 *  <li>Useful debug output statistics such as line and column number.</li>
 * </ul>
 *
 * <p>{@link Lexer} is not thread safe and must be synchronized externally if
 *  concurrent access is possible.</p>
 *
 * <p>Requires Java 6</p>
 *
 * <h4>Example Usage:</h4>
 * <pre>
 * File infile = new File("/path/to/data/file");
 * FileChannel fc = new FileInputStream(infile).getChannel();
 * MappedByteBuffer bbuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
 *
 * RaspLexer lexer = new RaspLexer(bbuf, Charset.forName("ISO-8859-15"));
 * lexer.addDelimiter(')');
 * lexer.addDelimiter('(');
 *
 * while (lexer.hasNext()) {
 *     lexer.advance();
 *
 *     System.out.printf("%-6d %-12s %-6d %-6d %-15s (%d,%d)%n",
 *         lexer.number(),
 *         lexer.type(),
 *         lexer.start(), lexer.end(),
 *         lexer.value(),
 *         lexer.line(), lexer.column());
 * }
 * </pre>
 *
 * <h4>To Do</h4>
 * <ul>
 *  <li>Remove dependency on fastutil. This library is excellent, but too big to
 *      be an acceptable dependency to a tiny library like this.</li>
 *
 *  <li>Implement configurable lexeme types. Each type should be defined by a
 *      name and a character matcher. The character matcher is a function that
 *      takes a character as it's argument and returns whether or lamnda not
 *      that character is in some set. The class can provide default
 *      implementations of common character matchers, such as for whitespace,
 *      but the user can define them as required.</li>
 *
 *  <li>Extend the character matching to string sequences. This would enable
 *      more complex lexical entities, but is starting to blur the line between
 *      lexical and syntactic analysis.</li>
 *
 *  <li>Seeking is messy because line, column, start, and end fields
 *      become relative to last call to {@link Lexer#seek(Tell)}. In addition
 *      the number field is inconsistent across multiple seeks to the same
 *      lexeme. There is also a problem with efficiency because calls to
 *      {@link Lexer#tell()} are expensive - if anything
 *      {@link Lexer#seek(Tell)} should be expensive. There are three
 *      approaches to resolving this problem:
 *      <ol>
 *          <li>Perform some evaluation of the surrounding characters to
 *              calculate the line and column numbers. This would be
 *              computationally expensive and would fail to solve the issues
 *              with the other fields.</li>
 *          <li>Use the less functional, but safer mark/reset method that
 *              ByteBuffer objects implement. The Lexer would then store a copy
 *              of all required fields when mark is called, that would be
 *              reinstated on a call to reset. Downside of this is that it
 *              looses the flexibility of multiple re-entry points.</li>
 *          <li>Building on mark/reset - the fields could be returned as a state
 *              object on calls to tell(), than can be reinstated by passing the
 *              state back to seek()</li>
 *      </ol>
 * </li>
 * </ul>
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class Lexer implements RandomAccess {

    /**
     * Report events to this logger. Only major events are reported, and to
     * Level.FINE or bellow.
     */
    private static final Logger LOG = Logger.getLogger(Lexer.class.getName());

    /**
     * <p>The number of characters that can be stored in cbuf. If more space is
     * required then the buffer will grow: n' = 2 * n + 1,
     * where n is the current buffer size, and n' is new buffer size.</p>
     *
     * <p>Note that the buffer size is not, and should not be configurable
     * because it doesn't work as one would normally expect. This buffer is
     * <em>not</em> for File I/O performance enhancement, which is handled by
     * underlying MappedByteBuffer, but for character decoding. Setting it too
     * high will reduce performance, due to L2 caching and issues with
     * the implementation of {@link #tell()}.</p>
     */
    private static final int INITIAL_BUFFER_SIZE = 1 << 8;

    /**
     * Each lexeme found will be one of the those defined in this enum.
     */
    public enum Type {

        Delimiter, Whitespace, Value

    }

    // =====================================
    // Configuration fields
    // =====================================
    /**
     * Set of all characters that will be interpreted as delimiters with the
     * input.
     */
//    private final CharSet delimiters;
    private CharMatcher whitespaceMatcher = CharMatcher.WHITESPACE;

    private CharMatcher delimiterMatcher = CharMatcher.NONE;

    private final CharFileChannel channel;

    private long previousChannelPosition;
//
//    /**
//     * ByteBuffer containing or wrapping the raw input data. In the case of a
//     * file it should probably be a MappedByteBuffer from a FileChannel.
//     */
//    private final ByteBuffer bbuf;

    /**
     * Target charset used to decode bytes, given in the constructor.
     */
//    private final Charset charset;
//
//    /**
//     * Decoder object created form the defined charset.
//     */
//    private final CharsetDecoder decoder;
    // =====================================
    // State fields
    // =====================================
    /**
     * Store decoded characters
     */
    private CharBuffer cbuf;

    /**
     * The offset in bbuf at the start of the previous read.
     *
     * The *character* offset of the data in cbuf from the start of bbuf. Note
     * that this may or may not be the same as the byte offset in bbuf
     * depending on the character encoding.
     */
    private int cbufOffset = 0;
//
//    /**
//     * The line number of next character to be read form cbuf.
//     */
//    private int line = 0;
//
//    /**
//     * The column number of the next character to be return from cbuf. This
//     * should not be relied upon since there are (rare) occasions when it can
//     * be wrong.
//     */
//    private int column = 0;

    /**
     * The offset of the start of the current lexeme from the beginning of cbuf.
     */
    private int start = 0;

    /**
     * The offset of the end of the current lexeme from the beginning of cbuf.
     */
    private int end = 0;

    /**
     * The type of the current lexeme
     */
    private Type type = null;
//
//    /**
//     * The line number of the start of the current lexeme.
//     */
//    private int startLine = -1;
//
//    /**
//     * The column number of the start of the current lexeme.
//     */
//    private int startColumn = -1;
//
//    /**
//     * A counter of the number of lexemes found, and thus the unique number
//     * for the current lexeme. (Unique but not identifying since the same lexeme
//     * can have multiple numbers.)
//     */
//    private long number = -1L;

    /**
     * <p>Construct a new instance of {@link Lexer} that reads from the given
     * channel.</p>
     *
     * @param channel The channel to read from
     * @throws NullPointerException if buffer or charset are null
     */
    public Lexer(CharFileChannel channel) throws NullPointerException {
//        if (buffer == null)
//            throw new NullPointerException("buffer is null");
//        if (charset == null)
//            throw new NullPointerException("charset is null");
        this.channel = channel;

//        this.bbuf = buffer;
//        this.charset = charset;
//        decoder = charset.newDecoder();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Decoding bytes to charset {0}.", channel.
                    getCharset());
            LOG.log(Level.FINE, "Initializing buffer capacity to {0} chars.",
                    INITIAL_BUFFER_SIZE);
        }
        cbuf = CharBuffer.allocate(INITIAL_BUFFER_SIZE);
        cbuf.flip();

        previousChannelPosition = 0;
//        delimiters = new CharOpenHashSet();
    }

    public Lexer(File file, Charset charset) throws FileNotFoundException, NullPointerException, IOException {
        this(new CharFileChannel(
                new FileInputStream(file).getChannel(),
                IOUtil.DEFAULT_CHARSET));
    }

    public void setDelimiterMatcher(CharMatcher delimiterMatcher) {
        this.delimiterMatcher = delimiterMatcher;
    }

    public CharMatcher getDelimiterMatcher() {
        return delimiterMatcher;
    }

    public void setWhitespaceMatcher(CharMatcher whitespaceMatcher) {
        this.whitespaceMatcher = whitespaceMatcher;
    }

    public CharMatcher getWhitespaceMatcher() {
        return whitespaceMatcher;
    }

//
//    public Lexer1(File file, Charset charset)
//            throws FileNotFoundException, IOException {
//        this(new FileInputStream(file).getChannel().map(
//                MapMode.READ_ONLY, 0, file.length()), charset);
//    }
    public long bytesRead() {
        return channel.position();
    }

    public long bytesTotal() throws IOException {
        return channel.size();
    }
//
//    /**
//     * Set the given character to be a lexical delimiter.
//     *
//     * @param ch the character to set as a delimiter
//     * @throws IllegalArgumentException if the delimiter is already set
//     */
//    public void addDelimiter(final char ch) throws IllegalArgumentException {
//        if (isDelimiter(ch))
//            throw new IllegalArgumentException(
//                    "delimiter '" + ch + "' already added.");
//        if (LOG.isLoggable(Level.FINE)) {
//            LOG.log(Level.FINE, "Adding delimiter character ''{0}''.", ch);
//        }
//        delimiters.add(ch);
//    }
//
//    /**
//     * Unset the given character as a lexical delimiter.
//     *
//     * @param ch the character to unset as a delimiter
//     * @throws IllegalArgumentException if the delimiter has not yet been set
//     */
//    public void removeDelimiter(final char ch) {
//        if (!isDelimiter(ch))
//            throw new IllegalArgumentException(
//                    "delimiter '" + ch + "' not found.");
//        if (LOG.isLoggable(Level.FINE)) {
//            LOG.log(Level.FINE, "Removing delimiter character ''{0}''.", ch);
//        }
//        delimiters.remove(ch);
//    }
//
//    /**
//     * Return whether or not the given character is a lexical delimiter.
//     *
//     * @param ch the character to check if it is a delimiter
//     * @return true if c is set as a delimiter, false otherwise
//     */
//    public boolean isDelimiter(final char ch) {
//        return delimiters.contains(ch);
//    }

    /**
     * Return true if the RaspLexer can produce more elements of any kind.
     *
     * @return true if there are more elements, false otherwise.
     * @throws CharacterCodingException When byte to char decoding fails
     */
    public final boolean hasNext() throws CharacterCodingException,
            ClosedChannelException, IOException {
        return channel.hasBytesRemaining() || cbuf.hasRemaining();
    }

    /**
     * Move internal pointers to the next lexeme.
     *
     * @throws CharacterCodingException  When byte to char decoding fails
     */
    public final void advance() throws CharacterCodingException,
            ClosedChannelException, IOException {
        if (!hasNext())
            throw new NoSuchElementException("iteration has no more elements.");

//        number++;
        start = cbuf.position();
//        end = start;
//        startLine = line;
//        startColumn = column;
        char c = read();

        if (delimiterMatcher.matches(c)) { // isDelimiter(c)
            type = Type.Delimiter;
        } else if (whitespaceMatcher.matches(c)) { // Character.isWhitespace(c)
            type = Type.Whitespace;
        } else {
            type = Type.Value;
        }

        try {
            switch (type) {
                case Whitespace:
                    do {
                        c = read();
                    } while (whitespaceMatcher.matches(c)
                            && !delimiterMatcher.matches(c));
                    unread(1);
                    break;
                case Value:
                    do {
                        c = read();
                    } while (!whitespaceMatcher.matches(c)
                            && !delimiterMatcher.matches(c));
                    unread(1);
                    break;
            }
        } catch (BufferUnderflowException e) {
            // perfectly acceptable as far as the lexer is concerned -
            // usually denoting EOF during a lexeme sequence
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Reached EOF.", e);
            }
        }
        end = cbuf.position();
    }

//    /**
//     * Return the number of the current lexeme. This is the absolute number of
//     * lexemes iterated over, and will increment for every lexeme. If seek() is
//     * called the number will continue to increase.
//     *
//     * @return unique number for the current lexeme
//     * @throws IllegalStateException when any consistency check fails
//     */
//    public final long number() throws IllegalStateException {
////        checkAccessorState();
//        return number;
//    }
    /**
     * Return the {@link Type} of the current lexeme.
     *
     * @return the type of the current lexeme
     * @throws IllegalStateException when any consistency check fails
     */
    public final Type type() throws IllegalStateException {
//        checkAccessorState();
        return type;
    }

    /**
     * Return the character offset of the start of the current lexeme.
     *
     * This can on occasions be incorrect (for e.g. after a call to seek()) and so
     * should not be relied upon for lexeme identification.
     *
     * @return character offset of the start of the current lexeme
     * @throws IllegalStateException when any consistency check fails
     */
    public final int start() throws IllegalStateException {
//        checkAccessorState();
        return cbufOffset + start;
    }

    /**
     * Return the character offset of the end of the current lexeme.
     *
     * This can on occasions be incorrect (for e.g. after a call to seek()) and so
     * should not be relied upon for lexeme identification.
     *
     * @return character offset of the end of the current lexeme
     * @throws IllegalStateException when any consistency check fails
     */
    public final int end() throws IllegalStateException {
//        checkAccessorState();
        return cbufOffset + end;
    }

//    /**
//     * Return the line on which the current lexeme starts. This will be relative
//     * to the last call to seek(), and so should not be relied upon for lexeme
//     * identification.
//     *
//     * @return line on which the current lexeme starts
//     * @throws IllegalStateException when any consistency check fails
//     */
//    public final int line() throws IllegalStateException {
////        checkAccessorState();
//        return startLine;
//    }
//    /**
//     * Return the lexeme start offset from the beginning of current line.
//     *
//     * This can on occasions be incorrect (for e.g. after a call to seek()) and so
//     * should not be relied upon for lexeme identification.
//     *
//     * @return lexeme start offset from the beginning of current line
//     * @throws IllegalStateException when any consistency check fails
//     */
//    public final int column() throws IllegalStateException {
////        checkAccessorState();
//        return startColumn;
//    }
    /**
     * Instantiate and populate a StringBuilder object with the characters that
     * constitute the current lexeme.
     *
     * @return characters constituting the current lexeme
     * @throws IllegalStateException when any consistency check fails
     */
    public final StringBuilder value() throws IllegalStateException {
//        checkAccessorState();
        final StringBuilder sb = new StringBuilder(end - start);
        value(sb);
        return sb;
    }

    /**
     * Access a single character in the current lexeme. Useful for getting a
     * delimiter character without building a string: use
     * <code>charAt(0)</code>.
     *
     * @return character at given offset from the start of the current lexeme.
     * @throws IndexOutOfBoundsException when offset >= lexeme length
     * @throws IllegalStateException when any consistency check fails
     */
    public final char charAt(final int offset)
            throws IndexOutOfBoundsException, IllegalStateException {
//        checkAccessorState();
        if (offset >= end - start)
            throw new IndexOutOfBoundsException("offset >= length");
        return cbuf.get(start + offset);
    }

    /**
     * Write a characters for the current lexeme to the StringBuilder given
     * as an argument. Very useful when syntactic analysis demands lexemes
     * to be concatenated.
     *
     * @param builder StringBuilder to write the lexeme characters to
     * @throws IllegalStateException when any consistency check fails
     */
    public final void value(final StringBuilder builder) throws IllegalStateException {
//        checkAccessorState();
        for (int i = start; i < end; i++) {
            builder.append(cbuf.get(i));
        }
    }

    /**
     * Attempt to reconstruct the whole line on which the current lexeme occurs
     * and return it as a CharSequence. This can be used to produce much more
     * helpful error messages.
     *
     * Note that this method is not guaranteed to success, since it will only
     * return whatever data is still available in the character buffer.
     *
     * @return the full line of the current lexeme if possible
     * @throws IllegalStateException when any consistency check fails
     */
    public final CharSequence debugContext() throws IllegalStateException {
//        checkAccessorState();
        int linesBefore = 0;
        int linesAfter = 0;
        int count = 0;
        int contextStart = start;
        while (contextStart > 0 && count <= linesBefore) {
            final char c = cbuf.get(contextStart);
            count += c == '\n' ? 1 : 0;
            contextStart--;
        }
        if (contextStart > 0 && cbuf.get(contextStart) == '\n') {
            contextStart++;
        }
        count = 0;
        int contextEnd = end;
        while (contextEnd < cbuf.length() && count <= linesAfter) {
            final char c = cbuf.get(contextEnd);
            count += c == '\n' ? 1 : 0;
            contextEnd++;
        }
        if (contextEnd < cbuf.length() && cbuf.get(contextEnd) == '\n') {
            contextEnd--;
        }
        cbuf.position(contextStart);
        CharSequence cs = cbuf.subSequence(0, contextEnd - contextStart);
        cbuf.position(end);
        return cs;
    }

    /**
     * Performs various consistency checks that should pass before any of the
     * entry accessors are called.
     *
     * @throws IllegalStateException when any consistency check fails
     */
//    private void checkAccessorState() throws IllegalStateException {
//        if (type == null)
//            throw new IllegalStateException("type == null");
//        if (start >= end)
//            throw new IllegalStateException(
//                    "start >= end (" + start + " >= " + end + ")");
//        if (start < 0)
//            throw new IllegalStateException("start < 0 (" + start + ")");
//        if (startColumn < 0)
//            throw new IllegalStateException(
//                    "startColumn < 0 (" + startColumn + ")");
//        if (startLine < 0)
//            throw new IllegalStateException("startLine < 0 (" + startLine + ")");
//        if (number < 0L)
//            throw new IllegalStateException("number < 0 (" + number + ")");
//    }
    /**
     * Check that the CharBuffer has required characters to be read between
     * the current position and the limit. If it doesn't then more data is
     * decode from the input ByteBuffer.
     *
     * @param required
     * @throws CharacterCodingException
     */
    private void insureRemaining(final int required) throws CharacterCodingException, ClosedChannelException, IOException {
        if (required <= cbuf.remaining()
                || !channel.hasBytesRemaining())
            return;

        final int off = cbuf.position() - start;

        // The number of characters we can potentially read into the buffer
        final int available = start + cbuf.remaining()
                + (cbuf.capacity() - cbuf.limit()); // unused buffer space

        cbuf.position(start);
        if (available < required) {
            // The buffer must grow to accomodate the token and extra data. Copy
            // the contents of the current buffer from the start of the current
            // lexeme to a new buffer.
            final int newCapacity = Math.max(cbuf.capacity() * 2 + 1,
                    cbuf.capacity() + required - available);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE,
                        "Growing character buffer from length {0} to {1}",
                        new Object[]{cbuf.capacity(), newCapacity});
            }

            final CharBuffer src = cbuf;
            cbuf = CharBuffer.allocate(newCapacity);
            cbuf.put(src);
        } else {
            // Move all the data from the start of current lexeme down to the
            // beginning of the buffer
            cbuf.compact();
        }

        previousChannelPosition = channel.position();
        channel.read(cbuf);
//
//        final CoderResult decodeResult = decoder.decode(bbuf, cbuf, false);

        // Whatever the decode result, put the buffer in a defined state so
        // recovery can be attempted - then throw the exception if one occured
        cbuf.flip();
        end = (end - start);
        cbufOffset += start;
        start = 0;
        cbuf.position(off);

//        if (decodeResult.isError()) {
//            decodeResult.throwException();
//        }
    }
//
//    Charset cset = null;
//
//    CharsetEncoder cencoder = null;

    /**
     * <p>Calculate the position, in the underlying ByteBuffer, of the currently
     * advanced lexeme. Return a long integer denoting this position. Passing
     * this integer as an argument to seek() will return Lexer to the state it
     * was at when tell was called.</p>
     *
     * <p>Note that there is some overhead to calling tell, since the
     * remaining decoded character must be re-encoded to determine byte
     * position. Try to call this method as infrequently as possible.</p>
     *
     * @return byte offset of the current lexeme.
     */
    public Tell tell() //            throws CharacterCodingException
    {

        return new Tell(previousChannelPosition, start);

//
//        if (cset == null) {
//            cset = channel.getCharset();
//            cencoder = cset.newEncoder();
//        }
////        else {
////            cencoder.reset();
////        }
//        cbuf.position(start);
//        final int bufferedBytesRemaining = cencoder.encode(cbuf).remaining();
//        cbuf.position(end);
//        return channel.position() - bufferedBytesRemaining;
    }

    public static final class Tell {
        // the offset in the channel of the current buffered region

        public static final Tell START = new Tell(0, 0);

        private long channelOffset;
        // the offset in the buffer of the first character in the lexeme

        private int bufferOffset;

        public Tell(long channelOffset, int bufferOffset) {
            this.channelOffset = channelOffset;
            this.bufferOffset = bufferOffset;
        }
    }

    /**
     * <p>Resets the lexer to start iterating from the given byte <code>offset</code>. If the
     * <code>offset</code> is not at a valid lexical boundary, behavior will be undefined. Correct
     * behavior is only guaranteed when the given <code>offset</code> is a value previously
     * return by a call to {@link #tell()}.</p>
     *
     * <p>Note: After performing a {@link #seek(Tell)}, the line and column number may be
     * incorrect. The column number will correct itself after the next new-line,
     * but the line number will always be wrong, unless a seek(0) is
     * performed. After a call to seek the following fields may be inconsistent:</p>
     * <dl>
     * <dt><code>number</code></dt><dd>will continue from the current value</dd>
     * <dt><code>line</code></dt><dd>will restart from 0</dd>
     * <dt><code>column</code></dt><dd>will restart from 0</dd>
     * </dl>
     * @param offset position in the underlying byte buffer to jump to
     * @throws CharacterCodingException
     */
    public void seek(final Tell offset) throws CharacterCodingException, IOException {

        channel.position(offset.channelOffset);

        cbuf.clear();
        channel.read(cbuf);
        cbuf.flip();

        cbuf.position(offset.bufferOffset);

        start = offset.bufferOffset;
        end = offset.bufferOffset;


        if (hasNext())
            advance();


//        channel.position(offset);
//        start = 0;
//
//        cbuf.clear();
//        channel.read(cbuf);
//        cbuf.flip();
//
//        cbuf.position(0);
//        end = start;
////        line = 0;
////        column = 0;
////        startLine = 0;
////        startColumn = 0;
//
//        if (hasNext())
//            advance();
//
////        if (decodeResult.isError())
////            decodeResult.throwException();
    }

    /**
     * Gets the next character from the buffer, refilling it if necessary.
     *
     * @return The next character in the buffer.
     * @throws CharacterCodingException
     */
    private char read()
            throws CharacterCodingException, IOException {
        insureRemaining(1);
        return  cbuf.get();
//        final char c = cbuf.get();
//        if (c == '\n') {
//            line++;
//            column = 0;
//        } else {
//            column++;
//        }
//        return c;
    }

    /**
     * Rather than keep a separate look-ahead character field, just push-back
     * any character that isn't wanted. It doesn't actually change anything
     * except the internal data pointers.
     *
     * @param num The number of characters to push-back.
     */
    private void unread(final int num) {
        cbuf.position(cbuf.position() - num);
//        if (column == 0) {
//            line--;
//            column = startColumn + (cbuf.position() - start);
//        } else {
//            column--;
//        }
    }
////
//    public static void main(String[] args) throws FileNotFoundException, IOException {
//        final String root = "/research/nlp/data4/hiam20/coref/test09/data";
//        final String inputFile = root + "/bnc.gramrels-lcase.00-maindb";
//
//        File file = new File(inputFile);
//        FileInputStream fis = new FileInputStream(file);
//        MappedByteBuffer mbb = fis.getChannel().map(
//                MapMode.READ_ONLY, 0, file.length());
//        Charset charset = IOUtil.DEFAULT_CHARSET;
//
//        Lexer lexer = new Lexer(mbb, charset);
//        lexer.addDelimiter(':');
//        lexer.addDelimiter('(');
//        lexer.addDelimiter(')');
//        lexer.addDelimiter('|');
//        lexer.addDelimiter('_');
//        lexer.addDelimiter('+');
//        lexer.addDelimiter(';');
//        lexer.addDelimiter('\n');
//
//        System.out.printf("%-6s %-12s %-6s %-6s %-15s (%s,%s)%n",
//                "num", "type",
//                "start", "end", "value", "line", "column");
//        while (lexer.hasNext()) {
//            final long x = lexer.tell();
//            lexer.advance();
//
//            System.out.printf("        %-6d %-12s %-6d %-6d %-15s (%d,%d)%n",
//                    lexer.number(),
//                    lexer.type(),
//                    lexer.start(),
//                    lexer.end(),
//                    lexer.value(),
//                    lexer.line(),
//                    lexer.column());
//
//            lexer.seek(x);
//
//
//            System.out.printf("        %-6d %-12s %-6d %-6d %-15s (%d,%d)%n",
//                    lexer.number(),
//                    lexer.type(),
//                    lexer.start(),
//                    lexer.end(),
//                    lexer.value(),
//                    lexer.line(),
//                    lexer.column());
//        }
//    }
//
//    public static void main(String[] args) throws FileNotFoundException, IOException {
//        String str = "come friendly bombs and fall on slough.";
//        Charset charset = IOUtil.DEFAULT_CHARSET;//forName("UTF-32");
//
//        ByteBuffer bb = ByteBuffer.wrap(str.getBytes());
//
//        Lexer lexer = new Lexer(bb, charset);
//        lexer.addDelimiter(' ');
//        lexer.addDelimiter('.');
//
//        System.out.printf("%-6s %-12s %-6s %-6s %-15s (%s,%s)%n",
//                "num", "type",
//                "start", "end", "value", "line", "column");
//        while (lexer.hasNext()) {
//            lexer.advance();
//            final long x = lexer.tell();
//
//            System.out.printf("%-6d %-12s %-6d %-6d %-15s (%d,%d)%n",
//                    lexer.number(),
//                    lexer.type(),
//                    lexer.start(),
//                    lexer.end(),
//                    lexer.value(),
//                    lexer.line(),
//                    lexer.column());
//
//            lexer.seek(x);
//            System.out.printf("%-6d %-12s %-6d %-6d %-15s (%d,%d)%n",
//                    lexer.number(),
//                    lexer.type(),
//                    lexer.start(),
//                    lexer.end(),
//                    lexer.value(),
//                    lexer.line(),
//                    lexer.column());
//        }
//    }
}
