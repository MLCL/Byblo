/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib.io;

import java.io.IOException;

/**
 *
 * @author hamish
 */
public interface DataSource {

    void endOfRecord() throws IOException;

    boolean isEndOfRecordNext() throws IOException;

    byte readByte() throws IOException;

    char readChar() throws IOException;

    short readShort() throws IOException;

    int readInt() throws IOException;

    long readLong() throws IOException;

    float readFloat() throws IOException;

    double readDouble() throws IOException;

    String readString() throws IOException;

    public boolean canRead() throws IOException;
}
