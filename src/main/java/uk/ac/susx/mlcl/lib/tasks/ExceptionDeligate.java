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

import com.google.common.base.Objects;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Queue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple implementation of the ExcetionCarrying functionality.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ExceptionDeligate
        implements ExceptionCarrying, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog(
            ExceptionDeligate.class);

    private Queue<Exception> exceptionQueue = null;

    public ExceptionDeligate() {
    }

    public synchronized Queue<Exception> getExceptionQueue() {
        if (exceptionQueue == null)
            exceptionQueue = new ArrayDeque<Exception>();
        return exceptionQueue;
    }

    public final void catchException(Exception exception) {
        if (LOG.isWarnEnabled())
            LOG.warn("Exception caught and queued.", exception);
        getExceptionQueue().offer(exception);
    }

    @Override
    public final boolean isExceptionCaught() {
        return exceptionQueue != null && !getExceptionQueue().isEmpty();
    }

    @Override
    public final Exception getException() {
        return isExceptionCaught() ? getExceptionQueue().poll() : null;
    }

    @Override
    public final void throwException() throws Exception {
        if (isExceptionCaught())
            throw getException();
    }

    @Override
    public final String toString() {
        return toStringHelper().toString();
    }

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this).
                add("exceptions", isExceptionCaught());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ExceptionDeligate other = (ExceptionDeligate) obj;
        if (this.exceptionQueue != other.exceptionQueue
                && (this.exceptionQueue == null || !this.exceptionQueue.equals(
                    other.exceptionQueue)))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.exceptionQueue != null ? this.exceptionQueue.
                            hashCode() : 0);
        return hash;
    }
}
