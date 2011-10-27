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
import com.google.common.base.Objects;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.CharacterCodingException;
import java.util.NoSuchElementException;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.RandomAccess;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * </ul>
 *
 * <p>{@link Lexer} is not thread safe and must be synchronized externally if
 *  concurrent access is required.</p>
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
 *     System.out.printf("%-6d %-12s %-6d %-6d %-15s%n",
 *         lexer.number(),
 *         lexer.type(),
 *         lexer.start(), lexer.end(),
 *         lexer.value());
 * }
 * </pre>
 *
 * <h4>ToDo</h4>
 * <ul>
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
 * </ul>
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class Lexer implements RandomAccess {

    /**
     * Report events to this logger. Only major events are reported, and to
     * Level.FINE or bellow.
     */
    private static final Log LOG = LogFactory.getLog(Lexer.class);

    /**
     * <p>The number of characters that can be stored in cbuf. If more space is
     * required then the buffer will grow: n' = 2 * n + 1,
     * where n is the current buffer size, and n' is new buffer size.</p>
     *
     * <p>Note that the buffer size is not, and should not, be configurable
     * because it doesn't work as one would normally expect. This buffer is
     * <em>not</em> for File I/O performance enhancement, which is handled by
     * underlying MappedByteBuffer, but for character decoding. Setting it too
     * high will reduce performance, due to L2 caching and issues. The buffer 
     * should be approximately as large as the longest lexeme in the input.
     */
    private static final int INITIAL_BUFFER_SIZE = 1 << 8;

    /**
     * Each lexeme found will be one of the those defined in this enum.
     */
    public enum Type {

        Delimiter,
        Whitespace,
        Value

    }

    /**
     * Function defining the set of all whitespace characters
     */
    private CharMatcher whitespaceMatcher = CharMatcher.WHITESPACE;

    /**
     * Function defining the set of all delimiter characters
     */
    private CharMatcher delimiterMatcher = CharMatcher.NONE;

    /**
     * Source of character data
     */
    private final CharFileChannel channel;

    /**
     * Store of position in the channel that should be seeked to, such that the
     * currently advanced lexeme will be re-retrievable. This is generally the
     * previous channel position, i.e the position in the channel before the 
     * last call to read.
     */
    private long channelRestartOffset;

    /**
     * Store the position that the last read started at. This is subtracted from 
     * the actual cbuf position() to give the position if a restart was 
     * performed.
     */
    private int charBufferRestartOffset;

    /**
     * Record the offset that can be used for seeking back to the currently
     * advanced position.
     */
    private Tell tell = new Tell(0, 0);

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

    /**
     * <p>Construct a new instance of {@link Lexer} that reads from the given
     * channel.</p>
     *
     * @param channel The channel to read from
     * @throws NullPointerException if buffer or charset are null
     */
    public Lexer(CharFileChannel channel) throws NullPointerException {
        this.channel = channel;

        if (LOG.isTraceEnabled()) {
            LOG.trace("Decoding bytes to charset " + channel.getCharset());
            LOG.trace(
                    "Initializing buffer capacity to " + INITIAL_BUFFER_SIZE + " chars.");
        }
        cbuf = CharBuffer.allocate(INITIAL_BUFFER_SIZE);
        cbuf.flip();

        channelRestartOffset = 0;
    }

    public Lexer(File file, Charset charset) throws FileNotFoundException, NullPointerException, IOException {
        this(new CharFileChannel( new FileInputStream(file).getChannel(),
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

    public long bytesRead() {
        return channel.position();
    }

    public long bytesTotal() throws IOException {
        return channel.size();
    }

    /**
     * Return true if the RaspLexer can produce more elements of any kind.
     *
     * @return true if there are more elements, false otherwise.
     * @throws CharacterCodingException When byte to char decoding fails
     * @throws ClosedChannelException
     * @throws IOException  
     */
    public final boolean hasNext() throws CharacterCodingException,
            ClosedChannelException, IOException {
        return channel.hasBytesRemaining() || cbuf.hasRemaining();
    }

    /**
     * Move internal pointers to the next lexeme.
     *
     * @throws CharacterCodingException  When byte to char decoding fails
     * @throws ClosedChannelException
     * @throws IOException  
     */
    public final void advance() throws CharacterCodingException,
            ClosedChannelException, IOException {
        if (!hasNext())
            throw new NoSuchElementException("iteration has no more elements.");

        tell.channelOffset = channelRestartOffset;
        tell.bufferOffset = cbuf.position() - charBufferRestartOffset;

        start = cbuf.position();
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
            if (LOG.isTraceEnabled()) {
                LOG.trace("Reached EOF.", e);
            }
        }
        end = cbuf.position();
    }

    /**
     * Return the {@link Type} of the current lexeme.
     *
     * @return the type of the current lexeme
     * @throws IllegalStateException when any consistency check fails
     */
    public final Type type() throws IllegalStateException {
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
        return cbufOffset + end;
    }

    /**
     * Instantiate and populate a StringBuilder object with the characters that
     * constitute the current lexeme.
     *
     * @return characters constituting the current lexeme
     * @throws IllegalStateException when any consistency check fails
     */
    public final StringBuilder value() throws IllegalStateException {
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

            if (LOG.isTraceEnabled()) {
                LOG.trace("Growing character buffer from length " + cbuf.
                        capacity() + " to " + newCapacity);
            }

            final CharBuffer src = cbuf;
            cbuf = CharBuffer.allocate(newCapacity);
            cbuf.put(src);
        } else {
            // Move all the data from the start of current lexeme down to the
            // beginning of the buffer
            cbuf.compact();
        }

        charBufferRestartOffset = off;
        channelRestartOffset = channel.position();
        channel.read(cbuf);

        // Whatever the decode result, put the buffer in a defined state so
        // recovery can be attempted - then throw the exception if one occured
        cbuf.flip();
        end = (end - start);
        cbufOffset += start;
        start = 0;
        cbuf.position(off);
    }

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
    public Tell tell() {
        return tell.clone();
    }

    public static final class Tell {
        // the offset in the channel of the current buffered region

        public static final Tell START = new Tell(0, 0);

        private long channelOffset;
        // the offset in the buffer of the first character in the lexeme

        private int bufferOffset;

        private Tell(long channelOffset, int bufferOffset) {
            this.channelOffset = channelOffset;
            this.bufferOffset = bufferOffset;
        }

        private Tell() {
            this(0, 0);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final Tell other = (Tell) obj;
            if (this.channelOffset != other.channelOffset)
                return false;
            if (this.bufferOffset != other.bufferOffset)
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + (int) (this.channelOffset ^ (this.channelOffset >>> 32));
            hash = 83 * hash + this.bufferOffset;
            return hash;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this).
                    addValue(channelOffset).
                    addValue(bufferOffset).toString();
        }

        @Override
        protected Tell clone() {
            return new Tell(this.channelOffset, this.bufferOffset);
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
     * @throws IOException  
     */
    public void seek(final Tell offset) throws CharacterCodingException, IOException {

        channel.position(offset.channelOffset);
        channelRestartOffset = channel.position();
        charBufferRestartOffset = 0;

        cbuf.clear();
        channel.read(cbuf);
        cbuf.flip();

        cbuf.position(offset.bufferOffset);

        start = offset.bufferOffset;
        end = offset.bufferOffset;


        if (hasNext())
            advance();

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
        return cbuf.get();
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
    }
}
