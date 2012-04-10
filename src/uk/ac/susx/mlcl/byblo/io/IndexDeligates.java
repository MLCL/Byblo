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
 * POSSIBILITY OF SUCH DAMAGE.To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo.io;

import java.io.File;
import java.io.IOException;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Enumerator;

/**
 *
 * @author hiam20
 */
public class IndexDeligates {

    private IndexDeligates() {
    }

    public static IndexDeligate toSingle1(final IndexDeligatePair outer) {
        return new PairToSingle2Adapter(outer);
    }

    public static IndexDeligate toSingle2(final IndexDeligatePair outer) {
        return new PairToSingle1Adapter(outer);
    }

    public static IndexDeligatePair toPair(final IndexDeligate inner) {
        return new SingleToPairAdapter(inner);
    }

    public static IndexDeligatePair decorateEnumerated(
            final IndexDeligatePair inner, final boolean enumerated) {
        return new IndexDeligates.PairToPairAdapter(inner) {

            @Override
            public boolean isEnumerated1() {
                return enumerated;
            }

            @Override
            public boolean isEnumerated2() {
                return enumerated;
            }

        };
    }

    public static IndexDeligate decorateEnumerated(
            final IndexDeligate inner, final boolean enumerated) {
        return new IndexDeligates.SingleToSingleAdapter(inner) {

            @Override
            public boolean isEnumerated() {
                return enumerated;
            }

        };
    }

    public abstract static class AdapterBase<T> {

        private final T inner;

        public AdapterBase(final T inner) {
            Checks.checkNotNull("inner", inner);
            this.inner = inner;
        }

        protected final T getInner() {
            return inner;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final AdapterBase<T> other = (AdapterBase<T>) obj;
            if (this.inner != other.inner && (this.inner == null || !this.inner.equals(other.inner)))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            return 59 * 5 + (this.inner != null ? this.inner.hashCode() : 0);
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{" + "inner=" + inner + '}';
        }

    }

    public static class SingleToPairAdapter
            extends AdapterBase<IndexDeligate>
            implements IndexDeligatePair {

        public SingleToPairAdapter(IndexDeligate inner) {
            super(inner);
        }

        @Override
        public Enumerator<String> getEnumerator1() throws IOException {
            return getInner().getEnumerator();
        }

        @Override
        public Enumerator<String> getEnumerator2() throws IOException {
            return getInner().getEnumerator();
        }

        @Override
        public File getIndexFile1() {
            return getInner().getIndexFile();
        }

        @Override
        public File getIndexFile2() {
            return getInner().getIndexFile();
        }

        @Override
        public boolean isEnumerated1() {
            return getInner().isEnumerated();
        }

        @Override
        public boolean isEnumerated2() {
            return getInner().isEnumerated();
        }

        @Override
        public boolean isSkipIndexed1() {
            return getInner().isSkipIndexed1();
        }

        @Override
        public boolean isSkipIndexed2() {
            return getInner().isSkipIndexed2();
        }

    }

    public static class PairToSingle1Adapter
            extends AdapterBase<IndexDeligatePair>
            implements IndexDeligate {

        public PairToSingle1Adapter(IndexDeligatePair inner) {
            super(inner);
        }

        @Override
        public Enumerator<String> getEnumerator() throws IOException {
            return getInner().getEnumerator2();
        }

        @Override
        public File getIndexFile() {
            return getInner().getIndexFile2();
        }

        @Override
        public boolean isEnumerated() {
            return getInner().isEnumerated2();
        }

        @Override
        public boolean isSkipIndexed1() {
            return getInner().isSkipIndexed1();
        }

        @Override
        public boolean isSkipIndexed2() {
            return getInner().isSkipIndexed2();
        }

    }

    public static class PairToSingle2Adapter
            extends AdapterBase<IndexDeligatePair>
            implements IndexDeligate {

        public PairToSingle2Adapter(final IndexDeligatePair inner) {
            super(inner);
        }

        @Override
        public Enumerator<String> getEnumerator() throws IOException {
            return getInner().getEnumerator1();
        }

        @Override
        public File getIndexFile() {
            return getInner().getIndexFile1();
        }

        @Override
        public boolean isEnumerated() {
            return getInner().isEnumerated1();
        }

        @Override
        public boolean isSkipIndexed1() {
            return getInner().isSkipIndexed1();
        }

        @Override
        public boolean isSkipIndexed2() {
            return getInner().isSkipIndexed2();
        }

    }

    public abstract static class SingleToSingleAdapter
            extends AdapterBase<IndexDeligate>
            implements IndexDeligate {

        public SingleToSingleAdapter(final IndexDeligate inner) {
            super(inner);
        }

        @Override
        public Enumerator<String> getEnumerator() throws IOException {
            return getInner().getEnumerator();
        }

        @Override
        public File getIndexFile() {
            return getInner().getIndexFile();
        }

        @Override
        public boolean isEnumerated() {
            return getInner().isEnumerated();
        }

        @Override
        public boolean isSkipIndexed1() {
            return getInner().isSkipIndexed1();
        }

        @Override
        public boolean isSkipIndexed2() {
            return getInner().isSkipIndexed2();
        }

    }

    public abstract static class PairToPairAdapter
            extends AdapterBase<IndexDeligatePair>
            implements IndexDeligatePair {

        public PairToPairAdapter(final IndexDeligatePair inner) {
            super(inner);
        }

        @Override
        public Enumerator<String> getEnumerator1() throws IOException {
            return getInner().getEnumerator1();
        }

        @Override
        public Enumerator<String> getEnumerator2() throws IOException {
            return getInner().getEnumerator2();
        }

        @Override
        public File getIndexFile1() {
            return getInner().getIndexFile1();
        }

        @Override
        public File getIndexFile2() {
            return getInner().getIndexFile2();
        }

        @Override
        public boolean isEnumerated1() {
            return getInner().isEnumerated1();
        }

        @Override
        public boolean isEnumerated2() {
            return getInner().isEnumerated2();
        }

        @Override
        public boolean isSkipIndexed1() {
            return getInner().isSkipIndexed1();
        }

        @Override
        public boolean isSkipIndexed2() {
            return getInner().isSkipIndexed2();
        }

    }
}
