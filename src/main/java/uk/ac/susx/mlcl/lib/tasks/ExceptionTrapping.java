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
package uk.ac.susx.mlcl.lib.tasks;

/**
 * Defines an interface for an object when stores exceptions, rather than
 * throwing them directly. This is useful for certain functionality, such as
 * when code is executing inside another thread: i.e implementing the Runnable
 * interface which does not support checked exceptions.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public interface ExceptionTrapping {

    /**
     * Whether or not at least 1 exception is currently caught by this object.
     *
     * @return True if an exception has been caught, false otherwise
     */
    boolean isExceptionTrapped();

    /**
     * Return the oldest exception that was previously caught, clearing it
     * internally.
     * <p/>
     * Repeated calls to this method may return addition exceptions; in a
     * first-in/first-out order. When no exception remain this method returns
     * null.
     *
     * @return the exception, or null if no exception is caught
     */
    Exception getTrappedException();

    /**
     * Throws the oldest exception that was previously caught.
     * <p/>
     * Repeated calls to this method may throw addition exceptions; in a
     * first-in/first-out order. When no exception remain this method does
     * nothing.
     *
     * @throws Exception the exception
     */
    void throwTrappedException() throws Exception;

}
