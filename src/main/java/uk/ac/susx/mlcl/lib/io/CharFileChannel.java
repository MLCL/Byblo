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

import com.google.common.base.Optional;
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.nio.*;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.charset.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>A class that gets round the problem of very large, seekable character files (greater than 2^31-1 bytes) by
 * borrowing API from the {@link java.nio.channels.FileChannel} and from Java 7's NIO improvements.
 * <p/>
 * All the positional accessors (such as {@code size()} and {@code position()}return offsets in bytes not characters.
 * This may seem confusing but there is simply no efficient way to determine these values in characters.
 * <p/>
 * This is not a complete implementation. The following additional functions would be required to be entirely
 * compatible
 * with the {@link java.nio.channels.FileChannel} API:
 * <p/>
 * <ul>
 * <p/>
 * <li>Implementation of the WritableCharChannel interface and write(CharBuffer) method. </li>
 * <p/>
 * <li>Locking methods. These could probably be delegated to the inner FileChannel object.</li>
 * <p/>
 * <li>Other miscellaneous methods including: {@code size()}, {@code truncate()}, and {@code force(boolean)}.</li>
 * <p/>
 * </ul>
 * <p/>
 * If this code is ever ported to Java 7 then it will work for any Channel that also implements
 * java.nio.channels.SeekableByteChannel, not just {@link java.nio.channels.FileChannel}.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
public class CharFileChannel implements CharChannel, Seekable<Long> {

    private static final Logger LOG = Logger.getLogger(CharFileChannel.class.getName());

    private static final long DEFAULT_MAX_MAPPED_BYTES = Integer.MAX_VALUE;

    /**
     * Size in bytes of the mapped region of the file. Should between 1 and Integer.MAX_VALUE.
     */
    private long maxMappedBytes = DEFAULT_MAX_MAPPED_BYTES;

    /**
     * The inner file change from which bytes will be read and decoded into characters.
     */
    private final FileChannel fileChannel;

    /**
     * Character set decoding.
     */
    private final CharsetDecoder decoder;

    /**
     * Character set encoder.
     */
    private final CharsetEncoder encoder;

    /**
     * ByteBuffer over the currently mapped region of the file.
     */
    private
    @Nonnull
    Optional<ByteBuffer> _buffer = Optional.absent();


    private boolean isBufferPresent() {
        return this._buffer.isPresent();
    }

    private boolean isBufferAbsent() {
        return !this._buffer.isPresent();
    }

    private void setBuffer(@Nonnull ByteBuffer buffer) {
        freeBuffer();
        this._buffer = Optional.of(buffer);
    }

    private void setBufferAbsent() {
        freeBuffer();
        this._buffer = Optional.absent();
    }

    private void setBufferEmpty() {
        freeBuffer();
        this._buffer = Optional.of(ByteBuffer.allocateDirect(0));
    }

    private
    @Nonnull
    ByteBuffer getBuffer() {
        return this._buffer.get();
    }

    // Even if the previous mapping failed it could occupy considerable amount of memory. This memory
    // is not reclaimed until the object is garbage collected, and may obstruct a subsequent mapping.
    private void freeBuffer() {

        if (LOG.isLoggable(Level.WARNING))
            LOG.log(Level.WARNING, "Attempting to free ByteBuffer resources.");

        if (!this._buffer.isPresent()) {
            // Could still have been created but unassigned, like if a mapping failed, so
            // attempt a single GC anyway
            System.gc();
        } else {
            // Ok so we have a reference. First create a phantom to we can check when it's GC'd, then clear the
            // strong reference, finally repeatedly GC until it's gone (is on the reference queue)
            final ReferenceQueue<ByteBuffer> refQueue = new ReferenceQueue<ByteBuffer>();
            final PhantomReference<ByteBuffer> phantom = new PhantomReference<ByteBuffer>(this._buffer.get(), refQueue);
            this._buffer = Optional.absent();

            int attemptCount = 1;
            System.gc();

            if (refQueue.poll() != null) {
                if (LOG.isLoggable(Level.WARNING))
                    LOG.log(Level.WARNING, "ByteBuffer freed.");
            } else {
                // Ok so it didn't work the first time so lets keep trying with increasing long timeouts.
                try {
                    long timeout = 1;

                    while (refQueue.remove(timeout) == null) {
                        timeout = (long) ((timeout + 1) * 1.5);
                        ++attemptCount;
                        if (LOG.isLoggable(Level.WARNING))
                            LOG.log(Level.WARNING,
                                    "Attempt {0} (with timeout {1}ms) to free ByteBuffer resources.",
                                    new Object[]{attemptCount, timeout});
                        System.gc();
                    }
                    if (LOG.isLoggable(Level.FINE))
                        LOG.log(Level.FINE, "ByteBuffer freed.");

                } catch (InterruptedException e) {
                    LOG.log(Level.WARNING, "Ignoring exception caught during free ByteBuffer process.", e);
                }
            }
        }
    }


    /**
     * Offset of the mapped region from the start of the file.
     */
    private long bufferOffset = 0;

    /**
     * Cache the file size because calls to FileChannel.size() take an excessively long time.
     */
    private long fileSize = -1;

    /**
     * Construct a new {@link uk.ac.susx.mlcl.lib.io.CharFileChannel} object from the given {@link
     * java.nio.channels.FileChannel} and {@link java.nio.charset.CharsetDecoder}
     * <p/>
     * The FileChannel must have been opened from a stream with the desired read/write mode.
     *
     * @param fileChannel byte based FileChannel to encapsulate
     * @param decoder     desired character set decoder
     * @throws NullPointerException if either fileChannel or decoder are null
     */
    public CharFileChannel(FileChannel fileChannel, CharsetDecoder decoder, CharsetEncoder encoder) throws NullPointerException {
        if (fileChannel == null)
            throw new NullPointerException("byteChannel is null");
        if (!fileChannel.isOpen())
            throw new IllegalArgumentException("fileChannel is already closed");
        if (encoder == null)
            throw new NullPointerException("encoder is null");
        if (decoder == null)
            throw new NullPointerException("decoder is null");

        this.fileChannel = fileChannel;
        this.decoder = decoder;
        this.encoder = encoder;
        setBufferAbsent();
        this.bufferOffset = 0;
    }

    /**
     * Construct a new {@link uk.ac.susx.mlcl.lib.io.CharFileChannel} object from the given {@link
     * java.nio.channels.FileChannel} and {@link java.nio.charset.Charset}
     *
     * @param fileChannel byte based FileChannel to encapsulate
     * @param charset     desired character set to decode into
     * @throws NullPointerException if either fileChannel or charset are null
     */
    public CharFileChannel(FileChannel fileChannel, Charset charset) throws NullPointerException {
        this(fileChannel, Files.decoderFor(charset), Files.encoderFor(charset));
    }

    /**
     * Construct a new {@link uk.ac.susx.mlcl.lib.io.CharFileChannel} object from the given {@link
     * java.nio.channels.FileChannel}. The charset will be set to the default for this Java Virtual Machine.
     *
     * @param fileChannel byte based FileChannel to encapsulate
     * @throws NullPointerException if fileChannel is null
     */
    public CharFileChannel(FileChannel fileChannel) throws NullPointerException {
        this(fileChannel, Files.DEFAULT_CHARSET);
    }

    /**
     * Return the character set that bytes will be decoded into.
     *
     * @return destination character set
     */
    public Charset getCharset() {
        return decoder.charset();
    }

    /**
     * Return the size in bytes of the mapped region of the file. Should between 1 and Integer.MAX_VALUE.
     *
     * @return size in bytes of the mapped region of the file
     */
    public long getMaxMappedBytes() {
        return maxMappedBytes;
    }

    /**
     * Set the size in bytes of the mapped region of the file Should between 1 and Integer.MAX_VALUE.
     *
     * @param maxMappedBytes new mapped region size
     * @throws IllegalArgumentException if maxMappedBytes &lt; 1 or maxMappedBytes &gt; Integer.MAX_VALUE
     */
    public void setMaxMappedBytes(long maxMappedBytes) {
        if (maxMappedBytes < 1)
            throw new IllegalArgumentException("maxMappedBytes < 1");
        if (maxMappedBytes > Integer.MAX_VALUE)
            throw new IllegalArgumentException(
                    "maxMappedBytes > Integer.MAX_VALUE");
        this.maxMappedBytes = maxMappedBytes;
    }

    /**
     * Return the size in byte of the encapsulated {@link java.nio.channels.FileChannel}.
     *
     * @return file size in bytes
     * @throws java.nio.channels.ClosedChannelException
     *                             If this channel is closed
     * @throws java.io.IOException If some other I/O error occurs
     */
    public long size() throws IOException {
        return (fileSize == -1) ? (fileSize = fileChannel.size()) : fileSize;
    }

    /**
     * Return the offset of the next byte to be read form the {@link java.nio.channels.FileChannel}.</p>
     *
     * @return file byte offset of next read
     */
    @Override
    public final Long position() {
        return bufferOffset + (isBufferAbsent() ? 0 : getBuffer().position());
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

        if (isBufferAbsent()) {
            bufferOffset = pos;
        } else if (pos >= bufferOffset && pos < bufferOffset + getBuffer().limit()) {
            getBuffer().position((int) (pos - bufferOffset));
        } else {
            bufferOffset = pos;
            setBufferAbsent();
        }
    }

    /**
     * Return whether or not the encapsulated {@link java.nio.channels.FileChannel} is still open.
     *
     * @return true if the channel is open, false otherwise
     */
    @Override
    public boolean isOpen() {
        return fileChannel.isOpen();
    }

    /**
     * Close the encapsulated file channel, and release buffering resources to garbage collection.
     *
     * @throws java.io.IOException If an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        fileChannel.close();
        setBufferAbsent();
    }

    /**
     * Read a sequence of bytes from the encapsulated {@link java.nio.channels.FileChannel}, decode into characters and
     * put them in the destination buffer. Returns the number of characters that have been added to the destination
     * buffer.
     *
     * @param dst destination buffer
     * @return number of characters put in {@code dst}
     * @throws java.nio.channels.ClosedChannelException
     *                             If this channel is closed
     * @throws java.nio.channels.NonReadableChannelException
     *                             If the mode is READ_ONLY but this channel was not opened for reading
     * @throws java.nio.channels.NonWritableChannelException
     *                             If the mode is READ_WRITE or PRIVATE but this channel was not opened for both
     *                             reading
     *                             and writing
     * @throws java.nio.charset.UnmappableCharacterException
     *
     * @throws java.nio.charset.CharacterCodingException
     *
     * @throws java.nio.charset.MalformedInputException
     *
     * @throws java.io.IOException If some other I/O error occurs
     */
    @Override
    public final int read(final CharBuffer dst) throws
            NonReadableChannelException,
            IOException {

        // sanity check the state
        if (!isOpen())
            throw new ClosedChannelException();
        if (dst == null)
            throw new NullPointerException("dst is null");
        if (!dst.hasRemaining())
            throw new IllegalArgumentException("dst remaining is 0");

        // record the start offset so we can calculate how much has been read
        final int startChar = dst.position();

        // reference the results of each decode here
        CoderResult coderResult;
        decoder.reset();

        // Repeat reading until the destination buffer is full, or the source
        // buffer is empty
        do {
            // Attempt to insure sufficient is data available for reading in the  mapped portion of the source channel,
            // which is not guaranteed to succeed.
            insureMapped((int) Math.ceil(dst.remaining() * encoder.maxBytesPerChar()), 0);

            coderResult = decoder.decode(getBuffer(), dst, false);
            checkCoderResult(coderResult);

        } while (hasBytesRemaining() && coderResult.isUnderflow());

        checkCoderResult(decoder.decode(getBuffer(), dst, true));
        checkCoderResult(decoder.flush(dst));

        return dst.position() - startChar;
    }

    /**
     * Return the number of bytes available for reading. Calculated as the file size, minus the offset of the mapped
     * region, minus the read position within the mapped region.
     *
     * @return number of bytes remaining
     * @throws java.nio.channels.ClosedChannelException
     *                             If this channel is closed
     * @throws java.io.IOException If some other I/O error occurs
     */
    public long bytesRemaining() throws IOException {
        return size() - bufferOffset - (isBufferAbsent() ? 0 : getBuffer().position());
    }

    /**
     * @return true if there are bytes remaining
     * @throws java.nio.channels.ClosedChannelException
     *                             If this channel is closed
     * @throws java.io.IOException If some other I/O error occurs
     */
    public boolean hasBytesRemaining() throws IOException {
        return bytesRemaining() > 0;
    }

    /**
     * Attempt to insure that the the required number of bytes ({@code requiredBytes}) is available for reading in the
     * buffer. If there is sufficient bytes available on the encapsulated {@link java.nio.channels.FileChannel}, and no
     * exception occurs, then this operation is guaranteed to succeed.
     * <p/>
     * Note that this method has package protected accessibility for unit testing purposes only.
     *
     * @param requiredReadBytes minimum number of bytes to be mapped when this method returns
     * @throws java.nio.channels.NonReadableChannelException
     *                             If the mode is READ_ONLY but this channel was not opened for reading
     * @throws java.nio.channels.NonWritableChannelException
     *                             If the mode is READ_WRITE or PRIVATE but this channel was not opened for both
     *                             reading
     *                             and writing
     * @throws java.nio.channels.ClosedChannelException
     *                             If this channel is closed
     * @throws java.io.IOException If some other I/O error occurs
     */
    void insureMapped(int requiredReadBytes, int requireWriteBytes)
            throws IOException {

        final boolean mappingRequired;

        if (isBufferAbsent()) {
            mappingRequired = true;
        } else if (hasBytesRemaining() && getBuffer().remaining() < Math.max(requiredReadBytes, requireWriteBytes)) {
            bufferOffset += getBuffer().position();
            mappingRequired = true;
        } else {
            mappingRequired = false;
        }

        if (mappingRequired) {
            // Ignore the buffer amount since we will map as much as possible
            // and let the operating system sort out the efficiency
//            long length = Math.max(Math.min(size() - bufferOffset, maxMappedBytes), 0);

            // Attempt to buffer as much of the remaining file as possible, up the the maxMappedBytes limit
            long length = size() - bufferOffset;
            if (length > maxMappedBytes)
                length = maxMappedBytes;

            if (length < requireWriteBytes)
                length = requireWriteBytes;

            if (length == 0) {
                setBufferEmpty();
                return;
            }

//            try {
//                setBuffer(fileChannel.map(FileChannel.MapMode.READ_WRITE, bufferOffset, length));
//            } catch (NonWritableChannelException ex) {
            setBuffer(fileChannel.map(FileChannel.MapMode.READ_ONLY, bufferOffset, length));
//            }
        }
    }

    /**
     * @param src buffer from which bytes are to be retrieved
     * @return nothing
     * @throws UnsupportedOperationException always
     */
    @Override
    public int write(CharBuffer src) throws IOException {
        throw new UnsupportedOperationException();

//        // sanity check the state
//        if (!isOpen())
//            throw new ClosedChannelException();
//        if (src == null)
//            throw new NullPointerException("src is null");
//
//        // record the start offset so we can calculate how much has been read
//        final int startChar = src.position();
//
//        // reference the results of each decode here
//        CoderResult coderResult;
//        encoder.reset();
//
//        // Repeat reading until the destination buffer is full, or the source
//        // buffer is empty
//        do {
//
//            // Attempt to insure sufficient is data available for reading in the  mapped portion of the source channel,
//            // which is not guaranteed to succeed.
//            insureMapped(0, (int) Math.ceil(src.remaining() * encoder.maxBytesPerChar()));
//
//            coderResult = encoder.encode(src, buffer, !src.hasRemaining());
//            checkCoderResult(coderResult);
//
//        } while (src.hasRemaining());
//
//        checkCoderResult(encoder.encode(src, buffer, !src.hasRemaining()));
//        checkCoderResult(encoder.flush(buffer));
//
//        return src.position() - startChar;
    }

    private static void checkCoderResult(CoderResult coderResult) throws CharacterCodingException {
        if (coderResult.isError()) {
            try {
                coderResult.throwException();
            } catch (BufferOverflowException ex) {
                throw new AssertionError(ex);
            } catch (BufferUnderflowException ex) {
                throw new AssertionError(ex);
            }
        }
    }

}
