package uk.ac.susx.mlcl.lib.io;

import java.io.*;
import java.net.URI;

/**
 */
public class DataMemoryStore implements DataStore {


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

    private static final byte END_OF_RECORD_MARKER = 0;

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
