/*
 * Copyright (c) 2010-2011, MLCL, University of Sussex
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

/**
 * <p>Interface defining a data-store that can be random-accessed to some 
 * extent.</p>
 * 
 * <p>Generally this is combined with an enumeration interface such as 
 * {@link Source} to allow positions to re-visited.</p>
 *
 * <p>Random access is used by some AllPairs implementations so they can record
 * read-offsets at certain times, then return to those offsets later. It allows
 * the All-Pairs implementation to handle arbitrarily large files, within any
 * amount of RAM, at the expense of increased disk I/O.</p>
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public interface Seekable<P> {

    /**
     * <p>Move the internal data pointer to some offset.<p>
     *
     * <p>The offset should always be one returned by {@link #position() }, e
     * because this interface does not define what the value represents. It could
     * be an array index, or it could be byte offset in a file.
     *
     * @param offset location to seek to
     * @throws IOException something has gone wrong with the underlying store
     */
    void position(P offset) throws IOException;

    /**
     * <p>Return the offset of the internal data pointer to the next item that
     * to be read or written.</p>
     *
     * <p>This value can be used by passing it to {@link #position() },
     * such that the source will restart from the current position.</p>
     *
     * @return some current data store offset
     * @throws IOException something has gone wrong with the underlying store
     */
    P position() throws IOException;
}
