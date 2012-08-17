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

import com.google.common.collect.BiMap;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import uk.ac.susx.mlcl.byblo.commands.FilterCommand;
import uk.ac.susx.mlcl.lib.collect.ForwardingBiMap;
import uk.ac.susx.mlcl.lib.io.Files;
import uk.ac.susx.mlcl.lib.io.TSV;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class MemoryBasedStringEnumerator extends BiMapEnumerator<String> {

    private static final long serialVersionUID = 1L;

    private final @Nullable File file;

    public MemoryBasedStringEnumerator(File file) {
        super();
        this.file = null;
    }

    public MemoryBasedStringEnumerator(
           @Nullable File file, BiMap<Integer, String> map, AtomicInteger nextId) {
        super(map, nextId);
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public boolean isFileSet() {
        return file != null;
    }

    public static MemoryBasedStringEnumerator newInstance() {
        return newInstance(null);
    }

    public static MemoryBasedStringEnumerator newInstance(@Nullable File file) {
        ForwardingBiMap<Integer, String> map = ForwardingBiMap.
                <Integer, String>create(
                new HashMap<Integer, String>(),
                new HashMap<String, Integer>());

        MemoryBasedStringEnumerator instance = new MemoryBasedStringEnumerator(
                file, map, new AtomicInteger(0));

        instance.put(FilterCommand.FILTERED_ID, FilterCommand.FILTERED_STRING);
        return instance;
    }

    public static MemoryBasedStringEnumerator load(File file) throws IOException {
        MemoryBasedStringEnumerator instance = newInstance(file);
        TSV.Source in = new TSV.Source(file, Files.DEFAULT_CHARSET);
        while (in.canRead()) {
            int id = in.readInt();
            String s = in.readString();
            in.endOfRecord();
            instance.put(id, s);
        }
        assert instance.indexOf(FilterCommand.FILTERED_STRING) == FilterCommand.FILTERED_ID;
        return instance;
    }

    public void save()
            throws IOException {
        if (file == null) {
            Logger.getLogger(this.getClass().getName()).log(
                    Level.WARNING,
                    "Attempt made to save an enumerator with no attached file.");
            return;
        }

        TSV.Sink out = new TSV.Sink(file, Files.DEFAULT_CHARSET);

        for (Map.Entry<Integer, String> e : this) {
            out.writeInt(e.getKey());
            out.writeString(e.getValue());
            out.endOfRecord();
        }
        out.flush();
        out.close();
    }

    public boolean equals(MemoryBasedStringEnumerator other) {
        if (this.file != other.file && (this.file == null || !this.file.
                                        equals(other.file)))
            return false;
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 29 * hash + (this.file != null ? this.file.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return this.equals((MemoryBasedStringEnumerator) obj);
    }
}
