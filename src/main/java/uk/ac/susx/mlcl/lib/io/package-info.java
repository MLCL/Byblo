/*
 * Copyright (c) 2010-2012, University of Sussex
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
/**
 * Contains data I/O functionality.
 *
 * <h2>IO Notes</h2>
 *
 *
 *
 * There exists a conceptual hierarchy of random access capabilities for data storage resources, from pure sequential
 * access up to full random access. Each level extends the functionality of the previous. The hierarchy is as
 * follows:<p/>
 *
 * <dl>
 *
 * <dt>Sequential access</dt> <dd>Data can be read or written in order with no ability change position. A input resource
 * would be expected to implement a very simply interface such as {@link java.util.Iterator} or {@link DataSource}</dd>
 *
 * <dt>Limited mark/reset</dt> <dd>The resource contains some buffer capabilities such a single position can be marked
 * and then returned at a later point within some limit.</dd>
 *
 * <dt>Unlimited mark/reset</dt> <dd>The single marked position can be returned to after an arbitrary number of
 * access.</dd>
 *
 * <dt>Seekable</dt> <dd>The resource can be queried for it's current position, returning a value which can be used to
 * return to that position at any point. Any number of positions can be produced and stored externally, and they will
 * remain valid as long as the resource is open. The {@link Seekable} interface implements this behaviour.</dd>
 *
 * <dt>Full random access</dt> <dt>Every data access method is can be parameterised with a position that will operate on
 * the associated location. The key difference between full random access and seekable that the latter supports seeking
 * at any time (cf. seekable positions are only required to be valid after the position has been reached and the
 * position accessor has been called.)</dt>
 *
 * </dl>
 *
 * In addition a resource may support a number of other capabilities. These include:
 *
 * <dl>
 *
 * <dt>Bidirectionality</dt>
 *
 * <dt>Closeable</dt>
 *
 * <dt>Flushable</dt>
 *
 * </dl>
 *
 */
@ParametersAreNonnullByDefault package uk.ac.susx.mlcl.lib.io;

import javax.annotation.ParametersAreNonnullByDefault;
