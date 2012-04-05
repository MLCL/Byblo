/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib.io;

import java.io.IOException;

/**
 *
 * @param <T>
 * @param <P>
 * @author hamish
 */
public class SeekableDataSourceAdapter<T extends SeekableDataSource<P>, P>
        extends DataSourceAdapter<T>
        implements SeekableDataSource<P> {

    public SeekableDataSourceAdapter(T inner) {
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
