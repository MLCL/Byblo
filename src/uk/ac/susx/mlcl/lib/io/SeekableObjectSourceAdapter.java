/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib.io;

import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @param <S>
 * @param <T>
 * @author hamish
 */
public abstract class SeekableObjectSourceAdapter<S extends SeekableSource<T, P>, T, P>
        extends ObjectSourceAdapter<S, T>
        implements SeekableSource<T, P>, Serializable {

    private static final long serialVersionUID = 1L;

    public SeekableObjectSourceAdapter(S inner) {
        super(inner);
    }

    @Override
    public void position(P offset) throws IOException {
        getInner().position(offset);
    }

    @Override
    public P position() throws IOException {
        return getInner().position();
    }
}
