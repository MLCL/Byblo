/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib.io;

import java.io.Serializable;
import uk.ac.susx.mlcl.lib.Checks;

/**
 *
 * @author hamish
 */
public final class Tell implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Class<?> type;

    private final Object value;

    private final Tell next;

    private Tell(Class<?> type, Object value, Tell next) {
        Checks.checkNotNull(type);
        this.type = type;
        this.value = value;
        this.next = next;
    }

    public Tell(Class<?> type, Object value) {
        this(type, value, null);
    }

    public final Tell next() {
        return next;
    }

    public final boolean hasNext() {
        return next != null;
    }

    public final <T> T value(Class<T> type) {
        return type.cast(value);
    }

    public final <T> Tell push(Class<T> type, T value) {
        return new Tell(type, value, this);
    }

    public boolean equals(Tell other) {
        if (this.type != other.type && (this.type == null || !this.type.equals(
                                        other.type)))
            return false;
        if (this.value != other.value && (this.value == null || !this.value.
                                          equals(other.value)))
            return false;
        if (this.next != other.next && (this.next == null || !this.next.equals(
                                        other.next)))
            return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null
                && getClass() == obj.getClass()
                && equals((Tell) obj);
    }

    @Override
    public int hashCode() {
        return 97 * (97 * (97 * 7 
                + (this.type != null ? this.type.hashCode() : 0)) 
                + (this.value != null ? this.value.hashCode() : 0)) 
                + (this.next != null ? this.next.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "Tell{" + "type=" + type.getSimpleName() + ", value=" + value + ", inner=" + next + '}';
    }
}
