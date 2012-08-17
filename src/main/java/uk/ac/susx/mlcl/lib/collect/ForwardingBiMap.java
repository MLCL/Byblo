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
package uk.ac.susx.mlcl.lib.collect;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ForwardingMap;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import uk.ac.susx.mlcl.lib.Checks;

/**
 * @param <K>
 * @param <V>
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class ForwardingBiMap<K, V>
        extends ForwardingMap<K, V>
        implements BiMap<K, V>, Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<K, V> deligate;

    private transient ForwardingBiMap<V, K> inverse;

    protected ForwardingBiMap(
            Map<K, V> forwards,
            @Nullable ForwardingBiMap<V, K> inverse) {
        this.deligate = forwards;
        this.inverse = inverse;
    }

    public static <K, V> ForwardingBiMap<K, V> create(
            Map<K, V> forwards, Map<V, K> backwards) {
        ForwardingBiMap<K, V> x = new ForwardingBiMap<K, V>(forwards, null);
        ForwardingBiMap<V, K> y = new ForwardingBiMap<V, K>(backwards, x);
        x.inverse = y;
        return x;
    }

    @Override
    protected Map<K, V> delegate() {
        return deligate;
    }

    @Override
    public V put(@Nonnull K k, @Nonnull V v) {
        Preconditions.checkNotNull(k, "key");
        Preconditions.checkNotNull(v, "value");

        if (containsValue(v)) {
            if (get(k).equals(v))
                return v;
            throw new IllegalArgumentException(
                    "given value is already bound to a different key in this bimap");
        }
        inverse.deligate.put(v, k);
        return deligate.put(k, v);
    }

    @Override
    public V forcePut(@Nonnull K k, @Nonnull V v) {
        Preconditions.checkNotNull(k, "key");
        Preconditions.checkNotNull(v, "value");

        inverse.deligate.put(v, k);
        return deligate.put(k, v);
    }

    /**
     *
     * @param value
     * @return
     * @throws ClassCastException if value is an inappropriate type for this
     *                            maps values
     */
    @Override
    public boolean containsValue(final Object value)
            throws ClassCastException {
        @SuppressWarnings("unchecked")
        final V key = (V) value;
        return inverse.deligate.containsKey(key);
    }

    @Override
    public V remove(Object key) {
        final V r = deligate.remove(key);
        inverse.deligate.remove(r);
        return r;
    }

    @Override
    public void clear() {
        deligate.clear();
        inverse.deligate.clear();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> e : map.entrySet())
            this.put(e.getKey(), e.getValue());
    }

    @Override
    public Set<V> values() {
        return inverse.deligate.keySet();
    }

    @Override
    public BiMap<V, K> inverse() {
        return inverse;
    }

    final Object writeReplace() {
        return new Serializer<K, V>(this);
    }

    public static final class Serializer<K, V> implements Externalizable {

        private static final long serialVersionUID = 1;

        private ForwardingBiMap<K, V> instance;

        public Serializer() {
        }

        Serializer(final ForwardingBiMap<K, V> se) {
            if (se == null) {
                throw new NullPointerException("se == null");
            }
            this.instance = se;
        }

        @Override
        public final void writeExternal(final ObjectOutput out)
                throws IOException {
            out.writeObject(instance.deligate);
            out.writeObject(instance.inverse.deligate);
        }

        @Override
        public final void readExternal(final ObjectInput in)
                throws IOException, ClassNotFoundException {
            @SuppressWarnings("unchecked")
            Map<K, V> forwards = (Map<K, V>) in.readObject();
            @SuppressWarnings("unchecked")
            Map<V, K> backwards = (Map<V, K>) in.readObject();
            this.instance = ForwardingBiMap.<K, V>create(forwards, backwards);
        }

        final Object readResolve() {
            return instance;
        }
    }
}
