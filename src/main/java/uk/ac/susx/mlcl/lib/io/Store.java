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

package uk.ac.susx.mlcl.lib.io;

import javax.annotation.CheckReturnValue;
import java.io.IOException;
import java.net.URI;

/**
 * A <code>Store</code> is an abstract resource used to retain data in some way.
 * <p/>
 * Usually it is a persistence layer such as files or databases, but it doesn't have to be. For example the data could
 * be stored in RAM, or it could implement dummy resource (such as notional black hole of <code>/dev/null</code>.)
 * Objects implementing this class can be though as equivalent to Java's {@link java.io.File}, from which accessors (e.g
 * {@link java.io.InputStream}) and mutators (e.g. {@link java.io.OutputStream}) can be instantiated.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@CheckReturnValue
public interface Store {

    /**
     * Get a URI description of this Store.
     * <p/>
     * Generally this will be defined by the instance creator, and is used to uniquely identify the resource in some
     * way, but this isn't strictly defined so authors may do as this wish. As such an implementation should
     * <em>not</em> rely on the URI to have any formal semantics beyond does defined by the URI format itself.
     *
     * @return a URI for this resource
     * @see URI
     */
    URI getURI();

    /**
     * Create the underlying resource if it doesn't already exist, otherwise does nothing.
     *
     * @return true if the resource did not exist and was created, false if it already existed
     * @throws IOException if the resource did not exist but could not be created for some reason
     */
    boolean touch() throws IOException;

    /**
     * Remove, delete, drop, or deallocate any resources associated with this store, assuming the resources exist.
     * <p/>
     * If the resources don't exist this method does nothing. Subsequent calls to {@link #exists()} must return false,
     * until the resource is created again.
     *
     * @return true if the resource existed before but has now been freed, false otherwise
     * @throws IOException if the resource did exist but could not be removed for some reason
     */
    boolean free() throws IOException;

    /**
     * Get whether or not there are any actual resources, such as files, database tables, or data structures, currently
     * associated with this <code>Store</code>.
     *
     * @return true if resources are allocated, false otherwise
     */
    boolean exists();

    /**
     * Get whether the data associated with this resource can be accessed in any way using this <code>Store</code>
     * instance.
     * <p/>
     * Note that the <code>Store</code> interface itself doesn't implement an method of accessing the data, this must be
     * provided by subclasses.
     *
     * @return true if resources is readable in any way, false otherwise
     */
    boolean isReadable();

    /**
     * Get whether the data associated with this resource be modified in any way using this <code>Store</code>
     * instance.
     * <p/>
     * This does not refer to the {@link #touch()} and {@link #free()} methods, which should still function even if this
     * resource is not writable. Instead it refers to the subclass implemented data mutation method.
     *
     * @return true if the underlying resource can be modified, false otherwise
     */
    boolean isWritable();


}
