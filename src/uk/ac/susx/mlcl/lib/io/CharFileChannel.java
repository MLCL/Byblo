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

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>A class that gets round the problem of very large, seekable character files
 * (greater than 2^31-1 bytes) by borrowing API from the 
 * {@link java.nio.channels.FileChannel} and from Java 7's NIO improvements.</p>
 *
 * <p>All the positional accessors (such as {@code size()} and 
 * {@code position()}return offsets in bytes not characters. This may seem
 * confusing but there is simply no efficient way to determine these values in
 * characters.</p>
 *
 * <p>This is not a complete implementation. The following additional functions
 * would be required to be entirely compatible with the
 * {@link java.nio.channels.FileChannel} API:</p>
 * 
 * <ul>
 *  <li>Implementation of the WriteableCharChannel interface and
 *      write(CharBuffer) method. </li>
 *  <li>Locking methods. These could probably be simply encapsulated from the
 *      inner FileChannel object.</li>
 *  <li>Other miscellaneous methods including: {@code size()},
 *      {@code truncate()}, and {@code force(boolean)}.</li>
 * </ul>
 *
 * <p>If this code is ever ported to Java 7 then it will work for any Channel
 * that also implements java.nio.channels.SeekableByteChannel, not just
 * {@link java.nio.channels.FileChannel}.</p>
 *
 * @author Hamish I A Morgan (hamish.morgan@sussex.ac.uk)
 * @since 13 March 2011
 * @version 13 March 2011
 */
public class CharFileChannel
        implements ReadableCharChannel, WritableCharChannel, Seekable<Long> {

    private static final Logger LOG = Logger.getLogger(CharFileChannel.class.
            getName());

    private static final long DEFAULT_MAX_MAPPED_BYTES = Integer.MAX_VALUE;

    private long maxMappedBytes = DEFAULT_MAX_MAPPED_BYTES;

    private final FileChannel fileChannel;

    private final CharsetDecoder decoder;

    private ByteBuffer buffer;

    private long bufferOffset = 0;

    /**
     * Construct a new {@link CharFileChannel} object from the given 
     * {@link java.nio.channels.FileChannel} and 
     * {@link java.nio.charset.CharsetDecoder}
     *
     * The FileChannel must have been opened from a stream with the desired
     * read/write mode.
     *
     * @param fileChannel   byte based FileChannel to encapsulate
     * @param decoder       desired character set decoder
     * @throws NullPointerException if either fileChannel or decoder are null
     */
    public CharFileChannel(FileChannel fileChannel, CharsetDecoder decoder)
            throws NullPointerException, IOException {
        if (!fileChannel.isOpen())
            throw new IllegalArgumentException("fileChannel is already closed");
        if (fileChannel == null)
            throw new NullPointerException("byteChannel is null");
        if (decoder == null)
            throw new NullPointerException("decoder is null");

        LOG.log(Level.FINE, "Instantiating new CharFileChannel on FileChannel "
                + "{0}, decoding into charset {1}.",
                new Object[]{fileChannel, decoder.charset()});


        this.fileChannel = fileChannel;
        this.decoder = decoder;
        this.buffer = null;
        this.bufferOffset = 0;
        size = fileChannel.size();
    }

    final long size;

    /**
     * Construct a new {@link CharFileChannel} object from the given 
     * {@link java.nio.channels.FileChannel} and {@link java.nio.charset.Charset}
     *
     * @param fileChannel   byte based FileChannel to encapsulate
     * @param charset       desired character set to decode into
     * @throws NullPointerException if either fileChannel or charset are null
     */
    public CharFileChannel(FileChannel fileChannel, Charset charset)
            throws NullPointerException, IOException {
        this(fileChannel, IOUtil.decoderFor(charset));
    }

    /**
     * Construct a new {@link CharFileChannel} object from the given
     * {@link java.nio.channels.FileChannel}. The charset will be set to the 
     * default for this Java Virtual Machine.
     *
     * @param fileChannel byte based FileChannel to encapsulate
     * @throws NullPointerException if fileChannel is null
     */
    public CharFileChannel(FileChannel fileChannel)
            throws NullPointerException, IOException {
        this(fileChannel, IOUtil.DEFAULT_CHARSET);
    }

    /**
     * Return the character set that bytes will be decoded into.
     *
     * @return destination character set
     */
    public Charset getCharset() {
        return decoder.charset();
    }

    public long getMaxMappedBytes() {
        return maxMappedBytes;
    }

    public void setMaxMappedBytes(long maxMappedBytes) {
        if (maxMappedBytes < 1)
            throw new IllegalArgumentException("maxMappedBytes < 1");
        this.maxMappedBytes = maxMappedBytes;
    }

    /**
     * Return the size in byte of the encapsulated
     * {@link java.nio.channels.FileChannel}.
     *
     * @return file size in bytes
     * @throws ClosedChannelException If this channel is closed
     * @throws IOException If some other I/O error occurs
     */
    public long size() throws ClosedChannelException, IOException {
        return size;
    }

    /**
     * <p>Return the offset of the next byte to be read form the
     * {@link java.nio.channels.FileChannel}.</p>
     *
     * @return file byte offset of next read
     */
    @Override
    public final Long position() {
        return bufferOffset + (buffer == null ? 0 : buffer.position());
    }

    /**
     * Set the byte offset for the next read.
     *
     * @param pos the file byte offset to move to
     * @throws IllegalArgumentException if pos &lt; 0 
     */
    @Override
    public void position(Long pos) {
        if (pos < 0)
            throw new IllegalArgumentException("pos < 0");

        if (buffer == null) {
            bufferOffset = pos;
        } else if (pos >= bufferOffset && pos < bufferOffset + buffer.limit()) {
            buffer.position((int) (pos - bufferOffset));
        } else {
            bufferOffset = pos;
            buffer = null;
        }
    }

    /**
     * Return whether or not the encapsulated {@link java.nio.channels.FileChannel}
     * is still open.
     *
     * @return  true if the channel is open, false otherwise
     */
    @Override
    public boolean isOpen() {
        return fileChannel.isOpen();
    }

    /**
     * Close the encapsulated file channel, and release buffering resources to
     * garbage collection.
     *
     * @throws IOException If an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        fileChannel.close();
        buffer = null;
    }

    /**
     * <p>Read a sequence of bytes from the encapsulated
     * {@link java.nio.channels.FileChannel}, decode into characters and put
     * them in the destination buffer. Returns the number of characters that
     * have been added to the destination buffer.</p>
     * 
     * @param dst destination buffer
     * @return number of characters put in {@code dst}
     * @throws ClosedChannelException If this channel is closed
     * @throws NonReadableChannelException If the mode is READ_ONLY but this
     *          channel was not opened for reading
     * @throws NonWritableChannelException If the mode is READ_WRITE or PRIVATE
     *          but this channel was not opened for both reading and writing
     * @throws UnmappableCharacterException
     * @throws CharacterCodingException
     * @throws MalformedInputException
     * @throws IOException If some other I/O error occurs
     */
    @Override
    public final int read(final CharBuffer dst) throws MalformedInputException,
            UnmappableCharacterException, ClosedChannelException,
            CharacterCodingException, NonReadableChannelException,
            NonWritableChannelException, IOException {

        // sanity check the channel state
        if (!isOpen())
            throw new ClosedChannelException();
        if (dst == null)
            throw new NullPointerException("dst is null");
        if (!dst.hasRemaining())
            throw new IllegalArgumentException("dst remaining is 0");

        // record the start offset so we can return how much has been read
        final int startChar = dst.position();

        // reference the results of each decode here
        CoderResult coderResult;

        // Repeat reading until the destination buffer is full, or the source
        // buffer is empty
        do {
            // it would be nice if java had the method decoder.maxPerByteChars()
            // but since it doesn't - guess we will need up to 4 byes per char.
//            final int requiredBytes = (int) Math.ceil(dst.remaining() * 4);

            // Attempt to insure sufficient are available for reading in the
            // mapped portion of the source channel (Not guaranteed to succeed)
            insureMapped((int) Math.ceil(dst.remaining() * 4));

            coderResult = decoder.decode(buffer, dst, false);

            if (coderResult.isError())
                try {
                    coderResult.throwException();
                } catch (BufferOverflowException ex) {
                    throw new AssertionError(ex);
                } catch (BufferUnderflowException ex) {
                    throw new AssertionError(ex);
                }


        } while (hasBytesRemaining()
//                bytesRemaining() > 0
                && coderResult.isUnderflow());

        return dst.position() - startChar;
    }

    /**
     *
     * @return number of bytes remaining
     * @throws ClosedChannelException If this channel is closed
     * @throws IOException If some other I/O error occurs
     */
    public long bytesRemaining() throws IOException, ClosedChannelException {
        // total file size, minus the offset of the mapped region, minus the
        // read position within the mapped region, minus 1
        return size
                - bufferOffset
                - (buffer == null ? 0 : buffer.position())
                ;//- 1;
    }

    /**
     *
     * @return true if there are bytes remaining
     * @throws ClosedChannelException If this channel is closed
     * @throws IOException If some other I/O error occurs
     */
    public boolean hasBytesRemaining() throws IOException, ClosedChannelException {
        return bytesRemaining() > 0;
    }

    /**
     * <p>Attempt to insure that the the required number of bytes
     * ({@code requiredBytes}) is available for reading in the buffer. If there
     * is sufficient bytes available on the encapsulated
     * {@link java.nio.channels.FileChannel}, and no exception occurs, then this
     * operation is guaranteed to succeed.</p>
     *
     * <p>Note that this method has package protected accessibility for unit
     * testing purposes only.</p>
     *
     * @param requiredBytes
     * @throws NonReadableChannelException If the mode is READ_ONLY but this
     *          channel was not opened for reading
     * @throws NonWritableChannelException If the mode is READ_WRITE or PRIVATE
     *          but this channel was not opened for both reading and writing
     * @throws ClosedChannelException If this channel is closed
     * @throws IOException If some other I/O error occurs
     */
    void insureMapped(int requiredBytes)
            throws ClosedChannelException, NonReadableChannelException,
            NonWritableChannelException, IOException {

        final boolean readRequired;

        if (buffer == null) {
            readRequired = true;
        } else if (hasBytesRemaining() && buffer.remaining() < requiredBytes) {
            bufferOffset += buffer.position();
            readRequired = true;
        } else {
            readRequired = false;
        }
        if (readRequired) {
            // Ignore the buffer ammount since we will map as much as possible
            // and let the operating system sort out the efficiency
            long length = Math.max(Math.min(
                    size - bufferOffset,
                    maxMappedBytes), 0);
            if (length == 0) {
                buffer = ByteBuffer.allocateDirect(0);
                return;
            }

            LOG.log(Level.FINE, "Mapping file region ({0} - {1}) to buffer.",
                    new Object[]{bufferOffset, bufferOffset + length});
            buffer = fileChannel.map(
                    FileChannel.MapMode.READ_ONLY, bufferOffset, length);
        }
    }

    /**
     * <p><strong>Not yet implemented.</strong></p>
     *
     * @param src
     * @return nothing
     * @throws UnsupportedOperationException always
     */
    @Override
    public int write(CharBuffer src) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
