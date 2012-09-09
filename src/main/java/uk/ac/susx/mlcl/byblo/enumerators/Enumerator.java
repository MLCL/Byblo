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
package uk.ac.susx.mlcl.byblo.enumerators;

import java.util.Map.Entry;

/**
 * Interface defining a unique indexing of complex objects (usually strings).
 * <p/>
 * Implementations of this interface are expected to associate each unique
 * object with an integer value. When an object is queried using indexOf for the
 * first time a new unique integer is returned. On subsequent queries of of the
 * same object, the same unique integer will be returned. After an object has
 * been assigned a unique id it can be retrieved using the valueOf method.
 *
 * @param <T> type of object being indexed.
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public interface Enumerator<T> extends Iterable<Entry<Integer, T>> {

    /**
     * Index value that implies the object has not been enumerated.
     */
    int NULL_INDEX = -1;

    /**
     * Retrieve the unique index for the given object.
     *
     * @param value object to index
     * @return the index
     * @throws NullPointerException when value is null
     */
    int indexOf(final T value);

    /**
     * Retrieve the object for the given unique index.
     *
     * @param index index of object to retrieve
     * @return the object
     * @throws IllegalArgumentException when index is negative
     */
    T valueOf(final int index);

}
