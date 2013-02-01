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

import javax.annotation.WillClose;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.CharBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.NoSuchElementException;

/**
 * <p>A {@link Lexer} performs lexical analysis of some delimited input file. It produces lexemes (lexical tokens) that
 * can be interpreted by a parser or handler of some sort.</p> <p/> <p>Written as a replacement for the many other
 * tokenisers available, it offers a unique combination of features:</p> <p/> <ul> <li>Extremely fast, NIO based,
 * buffered reading.</li> <p/> <li>Semi-random access allowing return to any previous lexeme position.</li> <p/> <li>No
 * unnecessary object instantiation (e.g. {@link String} fragments). This is achieved using lazy iteration with an
 * advance/accessor control. Insures that only the absolute minimum amount of work is done at each step.</li> <p/> </ul>
 * <p/> <p>{@link Lexer} is not thread safe and must be synchronized externally if concurrent access is required.</p>
 * <p/> <p>Requires Java 6</p>
 * <p/>
 * <h4>Example Usage:</h4>
 * <pre>
 * File infile = new File("/path/to/data/file");
 * FileChannel fc = new FileInputStream(infile).getChannel();
 * MappedByteBuffer buf = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
 *
 * RaspLexer lexer = new RaspLexer(buf, Charset.forName("ISO-8859-15"));
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
 * <p/>
 * <h4>ToDo</h4> <ul> <li>Implement configurable lexeme types. Each type should be defined by a name and a character
 * matcher. The character matcher is a function that takes a character as it's argument and returns whether or lambda
 * not that character is in some set. The class can provide default implementations of common character matchers, such
 * as for whitespace, but the user can define them as required.</li>
 * <p/>
 * <li>Extend the character matching to string sequences. This would enable more complex lexical entities, but is
 * starting to blur the line between lexical and syntactic analysis.</li>
 * <p/>
 * </ul>
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class Lexer implements Seekable<Tell>, Channel {

    /**
     * Report events to this logger. Only major events are reported, and to Level.FINE or bellow.
     */
    private static final Log LOG = LogFactory.getLog(Lexer.class);

    /**
     * <p>The number of characters that can be stored in <code>charBuffer</code>. If more space is required then the
     * buffer will grow: n' = 2 * n + 1, where n is the current buffer size, and n' is new buffer size.</p>
     * <p/>
     * <p>Note that the buffer size is not, and should not, be configurable because it doesn't work as one would
     * normally expect. This buffer is <em>not</em> for File I/O performance enhancement, which is handled by underlying
     * MappedByteBuffer, but for character decoding. Setting it too high will reduce performance, due to L2 caching and
     * issues. The buffer should be approximately as large as the longest lexeme in the input.
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
     * Store of position in the channel that it can seek to, such that the currently advanced lexeme will be
     * re-retrievable. This is generally the previous channel position, i.e the position in the channel before the last
     * call to read.
     */
    private long channelRestartOffset;

    /**
     * Store the position that the last read started at. This is subtracted from the actual charBuffer position() to
     * give the position if a restart was performed.
     */
    private int charBufferRestartOffset;

    /**
     * Record the offset that can be used for seeking back to the currently advanced position.
     */
    private Position pos = new Position();

    /**
     * Store decoded characters
     */
    private CharBuffer charBuffer;

    /**
     * The offset in char buffer at the start of the previous read.
     * <p/>
     * The *character* offset of the data in charBuffer from the start of  char buffer . Note that this may or may not
     * be the same as the byte offset in  char buffer  depending on the character encoding.
     */
    private int charBufferOffset = 0;

    /**
     * The offset of the start of the current lexeme from the beginning of charBuffer.
     */
    private int start = 0;

    /**
     * The offset of the end of the current lexeme from the beginning of charBuffer.
     */
    private int end = 0;

    /**
     * The type of the current lexeme
     */
    private Type type = null;

    /**
     * <p>Construct a new instance of {@link Lexer} that reads from the given channel.</p>
     *
     * @param channel The channel to read from
     * @throws NullPointerException if buffer or charset are null
     */
    private Lexer(CharFileChannel channel) throws NullPointerException {
        this.channel = channel;

        if (LOG.isTraceEnabled()) {
            LOG.trace("Decoding bytes to charset " + channel.getCharset());
            LOG.
                    trace(
                            "Initializing buffer capacity to " + INITIAL_BUFFER_SIZE + " chars.");
        }
        charBuffer = CharBuffer.allocate(INITIAL_BUFFER_SIZE);
        charBuffer.flip();

        channelRestartOffset = 0;
    }

    public Lexer(File file, Charset charset) throws NullPointerException, IOException {
        this(new CharFileChannel(new FileInputStream(file).getChannel(), charset));
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
     * Return true if the Lexer can produce more elements of any kind.
     *
     * @return true if there are more elements, false otherwise.
     * @throws CharacterCodingException When byte to char decoding fails
     * @throws ClosedChannelException
     * @throws IOException
     */
    public final boolean hasNext() throws
            IOException {
        return charBuffer.hasRemaining() || channel.hasBytesRemaining();
    }

    /**
     * Move internal pointers to the next lexeme.
     *
     * @throws CharacterCodingException When byte to char decoding fails
     * @throws ClosedChannelException
     * @throws IOException
     */
    public final void advance() throws
            IOException {
        if (!hasNext())
            throw new NoSuchElementException("iteration has no more elements.");
        advance0();
    }

    public final void advanceIfPossible() throws
            IOException {
        if (hasNext())
            advance0();
    }

    private void advance0() throws
            IOException {
        pos.channelOffset = channelRestartOffset;
        pos.bufferOffset = charBuffer.position() - charBufferRestartOffset;

        start = charBuffer.position();
        insureRemaining(1);
        char c = charBuffer.get();

        try {


            if (delimiterMatcher.matches(c)) {
                type = Type.Delimiter;
            } else if (whitespaceMatcher.matches(c)) {
                type = Type.Whitespace;

                do {
                    insureRemaining(1);
                    c = charBuffer.get();
                } while (whitespaceMatcher.matches(c)
                        && !delimiterMatcher.matches(c));
                charBuffer.position(charBuffer.position() - 1);

            } else {
                type = Type.Value;

                do {
                    insureRemaining(1);
                    c = charBuffer.get();
                } while (!whitespaceMatcher.matches(c)
                        && !delimiterMatcher.matches(c));
                charBuffer.position(charBuffer.position() - 1);
            }

        } catch (BufferUnderflowException e) {
            // perfectly acceptable as far as the lexer is concerned -
            // usually denoting EOF during a lexeme sequence
            if (LOG.isTraceEnabled()) {
                LOG.trace("Reached EOF.", e);
            }
        }
        end = charBuffer.position();
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
     * <p/>
     * This can on occasions be incorrect (for e.g. after a call to seek()) and so should not be relied upon for lexeme
     * identification.
     *
     * @return character offset of the start of the current lexeme
     * @throws IllegalStateException when any consistency check fails
     */
    public final int start() throws IllegalStateException {
        return charBufferOffset + start;
    }

    /**
     * Return the character offset of the end of the current lexeme.
     * <p/>
     * This can on occasions be incorrect (for e.g. after a call to seek()) and so should not be relied upon for lexeme
     * identification.
     *
     * @return character offset of the end of the current lexeme
     * @throws IllegalStateException when any consistency check fails
     */
    public final int end() throws IllegalStateException {
        return charBufferOffset + end;
    }

    /**
     * Instantiate and populate a StringBuilder object with the characters that constitute the current lexeme.
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
     * Get a single character in the current lexeme. Useful for getting a delimiter character without building a string:
     * use <code>charAt(0)</code>.
     *
     * @param offset the index of the character to get
     * @return character at given offset from the start of the current lexeme.
     * @throws IndexOutOfBoundsException when offset >= lexeme length
     * @throws IllegalStateException     when any consistency check fails
     */
    public final char charAt(final int offset)
            throws IndexOutOfBoundsException, IllegalStateException {
        if (offset >= end - start)
            throw new IndexOutOfBoundsException("offset >= length");
        return charBuffer.get(start + offset);
    }

    /**
     * Write a characters for the current lexeme to the StringBuilder given as an argument. Very useful when syntactic
     * analysis demands lexemes to be concatenated.
     *
     * @param builder StringBuilder to write the lexeme characters to
     * @throws IllegalStateException when any consistency check fails
     */
    final void value(final StringBuilder builder) throws IllegalStateException {
        for (int i = start; i < end; i++) {
            builder.append(charBuffer.get(i));
        }
    }

    /**
     * Attempt to reconstruct the whole line on which the current lexeme occurs and return it as a CharSequence. This
     * can be used to produce much more helpful error messages.
     * <p/>
     * Note that this method is not guaranteed to success, since it will only return whatever data is still available in
     * the character buffer.
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
            final char c = charBuffer.get(contextStart);
            count += c == '\n' ? 1 : 0;
            contextStart--;
        }
        if (contextStart > 0 && charBuffer.get(contextStart) == '\n') {
            contextStart++;
        }
        count = 0;
        int contextEnd = end;
        while (contextEnd < charBuffer.length() && count <= linesAfter) {
            final char c = charBuffer.get(contextEnd);
            count += c == '\n' ? 1 : 0;
            contextEnd++;
        }
        if (contextEnd < charBuffer.length() && charBuffer.get(contextEnd) == '\n') {
            contextEnd--;
        }
        charBuffer.position(contextStart);
        CharSequence cs = charBuffer.subSequence(0, contextEnd - contextStart);
        charBuffer.position(end);
        return cs;
    }

    /**
     * Check that the CharBuffer has required characters to be read between the current position and the limit. If it
     * doesn't then more data is decode from the input ByteBuffer.
     *
     * @param required minimum number of characters available to read
     * @throws CharacterCodingException
     */
    private void insureRemaining(final int required) throws IOException {
        if (required <= charBuffer.remaining()
                || !channel.hasBytesRemaining())
            return;

        final int off = charBuffer.position() - start;

        // The number of characters we can potentially read into the buffer
        final int available = start + charBuffer.remaining()
                + (charBuffer.capacity() - charBuffer.limit()); // unused buffer space

        charBuffer.position(start);
        if (available < required) {
            // The buffer must grow to accommodate the token and extra data. Copy
            // the contents of the current buffer from the start of the current
            // lexeme to a new buffer.
            final int newCapacity = Math.max(charBuffer.capacity() * 2 + 1,
                    charBuffer.capacity() + required - available);

            if (LOG.isTraceEnabled()) {
                LOG.trace("Growing character buffer from length " + charBuffer.
                        capacity() + " to " + newCapacity);
            }

            final CharBuffer src = charBuffer;
            charBuffer = CharBuffer.allocate(newCapacity);
            charBuffer.put(src);
        } else {
            // Move all the data from the start of current lexeme down to the
            // beginning of the buffer
            charBuffer.compact();
        }

        charBufferRestartOffset = off;
        channelRestartOffset = channel.position();
        channel.read(charBuffer);

        // Whatever the decode result, put the buffer in a defined state so
        // recovery can be attempted - then throw the exception if one occurred
        charBuffer.flip();
        end = (end - start);
        charBufferOffset += start;
        start = 0;
        charBuffer.position(off);
    }

    /**
     * <p>Calculate the position, in the underlying ByteBuffer, of the currently advanced lexeme. Return a long integer
     * denoting this position. Passing this integer as an argument to seek() will return Lexer to the state it was at
     * when tell was called.</p> <p/> <p>Note that there is some overhead to calling tell, since the remaining decoded
     * character must be re-encoded to determine byte position. Try to call this method as infrequently as
     * possible.</p>
     *
     * @return byte offset of the current lexeme.
     */
    @Override
    public Tell position() {
        return new Tell(Position.class, new Position(pos));
    }

    /**
     * <p>Resets the lexer to start iterating from the given byte <code>offset</code>. If the <code>offset</code> is not
     * at a valid lexical boundary, behavior will be undefined. Correct behavior is only guaranteed when the given
     * <code>offset</code> is a value previously return by a call to {@link #position()}.</p> <p/> <p>Note: After
     * calling this method, the line and column number may be incorrect. The column number will correct itself after the
     * next new-line, but the line number will always be wrong, unless a seek(0) is performed. After a call to seek the
     * following fields may be inconsistent:</p> <dl> <dt><code>number</code></dt><dd>will continue from the current
     * value</dd> <dt><code>line</code></dt><dd>will restart from 0</dd> <dt><code>column</code></dt><dd>will restart
     * from 0</dd> </dl>
     *
     * @param offset position in the underlying byte buffer to jump to
     * @throws CharacterCodingException
     * @throws IOException
     */
    @Override
    public void position(final Tell offset)
            throws IOException {
        this.pos = new Position(offset.value(Position.class));

        channel.position(pos.channelOffset);
        channelRestartOffset = channel.position();
        charBufferRestartOffset = 0;

        charBuffer.clear();
        channel.read(charBuffer);
        charBuffer.flip();

        charBuffer.position(pos.bufferOffset);

        start = pos.bufferOffset;
        end = pos.bufferOffset;

        advanceIfPossible();
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    @WillClose
    public void close() throws IOException {
        channel.close();
    }

    /**
     * Record the offset that can be used for seeking back to the currently advanced position.
     */
    private final class Position {

        long channelOffset;

        int bufferOffset;

        private Position(long channelOffset, int bufferOffset) {
            this.channelOffset = channelOffset;
            this.bufferOffset = bufferOffset;
        }

        private Position() {
            this(0, 0);
        }

        /**
         * Cloning constructor.
         *
         * @param other object to clone
         */
        private Position(Position other) {
            this(other.channelOffset, other.bufferOffset);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass())
                return false;
            final Position other = (Position) obj;
            return this.channelOffset == other.channelOffset
                    && this.bufferOffset == other.bufferOffset;
        }

        @Override
        public int hashCode() {
            return 43 * (43 * 3 + this.bufferOffset)
                    + (int) (this.channelOffset ^ (this.channelOffset >>> 32));
        }

        @Override
        public String toString() {
            return "Position{" + "channelOffset=" + channelOffset + ", bufferOffset=" + bufferOffset + '}';
        }
    }
}
