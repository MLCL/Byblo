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

import java.io.File;
import java.io.IOException;
import javax.annotation.Nullable;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public enum EnumeratorType {

    Memory {
        @Override
        public Enumerator<String> open(@Nullable File file) throws IOException {
            if (file != null && file.exists())
                return MemoryBasedStringEnumerator.load(file);
            else
                return MemoryBasedStringEnumerator.newInstance(file);
        }

        @Override
        public void save(Enumerator<String> enumerator) throws IOException {
            assert enumerator instanceof MemoryBasedStringEnumerator;
            ((MemoryBasedStringEnumerator) enumerator).save();
        }

        @Override
        public void close(Enumerator<String> enumerator) throws IOException {
            assert enumerator instanceof MemoryBasedStringEnumerator;
        }
    }, JDBM {
        @Override
        public Enumerator<String> open(@Nullable File file) {
            if (file == null) {
                return JDBMStringEnumerator.newInstance(null);
            } else if (!file.exists()) {
                return JDBMStringEnumerator.newInstance(file);
            } else {
                return JDBMStringEnumerator.load(file);
            }
        }

        @Override
        public void save(Enumerator<String> enumerator) throws IOException {
            assert enumerator instanceof JDBMStringEnumerator;
            ((JDBMStringEnumerator) enumerator).save();
        }

        @Override
        public void close(Enumerator<String> enumerator) throws IOException {
            assert enumerator instanceof JDBMStringEnumerator;
            ((JDBMStringEnumerator) enumerator).close();
        }
    };

    public abstract Enumerator<String> open(File file) throws IOException;

    public abstract void save(Enumerator<String> enumerator) throws IOException;

    public abstract void close(Enumerator<String> enumerator) throws IOException;
}
