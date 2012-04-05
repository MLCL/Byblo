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
public interface DataSink {

    void endOfRecord() throws IOException;

    void writeByte(byte val) throws IOException;

    void writeChar(char val) throws IOException;

    void writeShort(short val) throws IOException;

    void writeInt(int val) throws IOException;

    void writeLong(long val) throws IOException;

    void writeDouble(double val) throws IOException;

    void writeFloat(float val) throws IOException;

    void writeString(String str) throws IOException;
}
