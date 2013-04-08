/*
 * Copyright (c) 2010-2013, University of Sussex
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
package uk.ac.susx.mlcl.lib.io;

import java.io.*;
import java.net.URI;

/**
 */
public class DataMemoryStore implements DataStore {


    private static final byte END_OF_RECORD_MARKER = 0;

    @Override
    public DataSource openDataSource() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DataSink openDataSink() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SeekableDataSource openSeekableDataSource() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSeekable() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public URI getURI() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean touch() throws IOException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean free() throws IOException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean exists() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isReadable() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isWritable() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public static class DataMemorySink implements DataSink, Closeable, Flushable {

        final ByteArrayOutputStream bytesOut;
        final DataOutputStream dataOut;
        boolean open;

        public DataMemorySink(ByteArrayOutputStream bytesOut) {
            this.bytesOut = bytesOut;
            this.dataOut = new DataOutputStream(bytesOut);
            this.open = true;
        }

        @Override
        public void endOfRecord() throws IOException {
            dataOut.writeByte(END_OF_RECORD_MARKER);
        }

        @Override
        public void writeByte(byte val) throws IOException {
            dataOut.writeByte(val);
        }

        @Override
        public void writeChar(char val) throws IOException {
            dataOut.writeChar(val);
        }

        @Override
        public void writeShort(short val) throws IOException {
            dataOut.writeShort(val);
        }

        @Override
        public void writeInt(int val) throws IOException {
            dataOut.writeInt(val);
        }

        @Override
        public void writeLong(long val) throws IOException {
            dataOut.writeLong(val);
        }

        @Override
        public void writeDouble(double val) throws IOException {
            dataOut.writeDouble(val);
        }

        @Override
        public void writeFloat(float val) throws IOException {
            dataOut.writeFloat(val);
        }

        @Override
        public void writeString(String str) throws IOException {
            dataOut.writeUTF(str);
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() throws IOException {
            dataOut.close();
            open = false;

        }

        @Override
        public void flush() throws IOException {
            dataOut.flush();
        }
    }

    public static class DataMemorySource implements DataSource, Closeable {

        private final ByteArrayInputStream bytesIn;
        private final DataInputStream dataIn;
        private boolean open;

        public DataMemorySource(ByteArrayInputStream bytesIn) {
            this.bytesIn = bytesIn;
            dataIn = new DataInputStream(bytesIn);
            open = true;
        }

        @Override
        public void endOfRecord() throws IOException {
            if (dataIn.readByte() != END_OF_RECORD_MARKER)
                throw new IOException();
        }

        @Override
        public boolean isEndOfRecordNext() throws IOException {
            try {
                dataIn.mark(1);
                return (dataIn.readByte() == END_OF_RECORD_MARKER);
            } finally {
                dataIn.reset();
            }
        }

        @Override
        public void skipRecord() throws IOException {
            while (dataIn.readByte() != END_OF_RECORD_MARKER) {
                // not a sausage
            }
        }

        @Override
        public byte readByte() throws IOException {
            return dataIn.readByte();
        }

        @Override
        public char readChar() throws IOException {
            return dataIn.readChar();
        }

        @Override
        public short readShort() throws IOException {
            return dataIn.readShort();
        }

        @Override
        public int readInt() throws IOException {
            return dataIn.readInt();
        }

        @Override
        public long readLong() throws IOException {
            return dataIn.readLong();
        }

        @Override
        public float readFloat() throws IOException {
            return dataIn.readFloat();
        }

        @Override
        public double readDouble() throws IOException {
            return dataIn.readDouble();
        }

        @Override
        public String readString() throws IOException {
            return dataIn.readUTF();
        }

        @Override
        public boolean canRead() throws IOException {
            return dataIn.available() > 0;
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() throws IOException {
            dataIn.close();
            open = false;
        }

    }
}
