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

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.Channel;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;

/**
 * A channel that can write chars.
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public interface WritableCharChannel extends Channel {

    /**
     * Writes a sequence of chars to this channel from the given buffer.
     * 
     * @param src buffer from which bytes are to be retrieved
     * @return number of bytes written, possibly zero
     * @throws java.nio.channels.NonWritableChannelException - If this channel was not opened for writing
     * @throws java.nio.channels.ClosedChannelException - If this channel is closed
     * @throws java.nio.channels.AsynchronousCloseException - If another thread closes this
     *                  channel while the write operation is in progress
     * @throws java.nio.channels.ClosedByInterruptException - If another thread interrupts the
     *     current thread while the write operation is in progress, thereby 
     *     closing the channel and setting the current thread's interrupt status
     * @throws java.io.IOException - If some other I/O error occurs
     */
    int write(CharBuffer src) throws NonWritableChannelException,
            ClosedChannelException, AsynchronousCloseException,
            ClosedByInterruptException, IOException;

}
