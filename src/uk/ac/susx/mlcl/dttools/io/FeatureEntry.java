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
package uk.ac.susx.mlcl.dttools.io;

/**
 *
 * @author Hamish Morgan (hamish.morgan@sussex.ac.uk)
 * @version 27th March 2011
 */
public final class FeatureEntry {

//    private final String head;
//
//    private final String context;

    private final int headId;

    private final int contextId;

    private final double weight;

    public FeatureEntry(//String head, String context,
            final int head_id,
                        final int context_id, final double weight) {
//        this.head = head;
//        this.context = context;
        this.headId = head_id;
        this.contextId = context_id;
        this.weight = weight;
    }

    public int getHeadId() {
        return headId;
    }

    public int getContextId() {
        return contextId;
    }

    public double getWeight() {
        return weight;
    }
//
//    public String getHead() {
//        return head;
//    }
//
//    public String getContext() {
//        return context;
//    }

    @Override
    public String toString() {
        return "FeatureEntry{"
                + "head=" + //head
//                + "(" +
                headId
//                + ")"
                + ", context=" //+ context
//                + "("
                + contextId
//                + ")"
                + ", weight=" + weight + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final FeatureEntry other = (FeatureEntry) obj;
        if (this.headId != other.headId)
            return false;
        if (this.contextId != other.contextId)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + this.headId;
        hash = 47 * hash + this.contextId;
        return hash;
    }


}
