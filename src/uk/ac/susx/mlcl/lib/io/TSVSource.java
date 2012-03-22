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
package uk.ac.susx.mlcl.lib.io;

import com.google.common.base.CharMatcher;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import uk.ac.susx.mlcl.lib.MiscUtil;

/**
 * Class that holds functionality to read a Tab Separated Values file.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class TSVSource implements SeekableSource<Iterable<String>, Lexer.Tell> {

    private static final char RECORD_DELIM = '\n';

    private static final char VALUE_DELIM = '\t';

    private final Lexer lexer;

    private final File file;

    private long row;

    private long column;

    public TSVSource(File file, Charset charset) throws FileNotFoundException, IOException {
        if (file == null)
            throw new NullPointerException("file == null");
        if (!file.exists())
            throw new FileNotFoundException("Path " + file + " does not exist.");
        if (!file.isFile())
            throw new IllegalArgumentException(
                    "Path " + file + " is not a normal file.");
        if (!file.canRead())
            throw new IllegalArgumentException(
                    "File " + file + " is not readable.");

        lexer = new Lexer(file, charset);
        lexer.setDelimiterMatcher(CharMatcher.anyOf("\n\t"));
        lexer.setWhitespaceMatcher(CharMatcher.NONE);
        if (lexer.hasNext())
            lexer.advance();
        this.file = file;
        row = 0;
        column = 0;
    }

    public File getFile() {
        return file;
    }

    @Override
    public void position(Lexer.Tell offset) throws IOException {
        lexer.seek(offset);
        row = 0;
        column = 0;
    }

    public double percentRead() throws IOException {
        return 100d * lexer.bytesRead() / lexer.bytesTotal();
    }

    public long roughPosition() {
        return lexer.start();
    }

    @Override
    public Lexer.Tell position() {
        return lexer.tell();
    }

    @Override
    public boolean hasNext() throws CharacterCodingException, IOException {
        return lexer.hasNext();
    }

    protected void skipWhitespace() throws CharacterCodingException, IOException {
        while (lexer.type() == Lexer.Type.Whitespace && lexer.hasNext()) {
            lexer.advance();
        }
    }

    public boolean isEndOfRecordNext() throws CharacterCodingException, IOException {
        return isDelimiterNext() && lexer.charAt(0) == RECORD_DELIM;
    }

    private boolean isValueDelimiterNext() throws CharacterCodingException, IOException {
        return isDelimiterNext() && lexer.charAt(0) == VALUE_DELIM;
    }

    private boolean isDelimiterNext() throws CharacterCodingException, IOException {
        return lexer.type() == Lexer.Type.Delimiter;
    }


    public void endOfRecord() throws CharacterCodingException, IOException {
        do {
            parseDelimiter(RECORD_DELIM);
            ++row;
        } while (isEndOfRecordNext() && hasNext());
        column = 0;
    }

    private void parseValueDelimiter() throws CharacterCodingException, IOException {
        do {
            parseDelimiter(VALUE_DELIM);
        } while (isValueDelimiterNext() && hasNext());
    }

    public String readString() throws CharacterCodingException, IOException {
        if (column > 0)
            parseValueDelimiter();
        
        skipWhitespace();
        expectType(Lexer.Type.Value, lexer.type());
        final String str = lexer.value().toString();
        if (lexer.hasNext())
            lexer.advance();

        ++column;
        return str;
    }

    public double readDouble() throws CharacterCodingException, IOException {
        try {
            return Double.valueOf(readString());
        } catch (NumberFormatException nfe) {
            throw new TSVDataFormatException(this, nfe);
        }
    }

    public int readInt() throws CharacterCodingException, IOException {
        try {
            return Integer.parseInt(readString());
        } catch (NumberFormatException nfe) {
            throw new TSVDataFormatException(this, nfe);
        }
    }

    private void parseDelimiter(char delim) throws CharacterCodingException, IOException {
        skipWhitespace();
        expectType(Lexer.Type.Delimiter, lexer.type());
        expectDelim(delim, lexer.charAt(0));
        if (lexer.hasNext())
            lexer.advance();
    }

    private void expectType(Lexer.Type expected, Lexer.Type actual) throws TSVDataFormatException {
        if (expected != actual) {
            throw new TSVDataFormatException(this,
                                             "Expecting type " + expected + " but found " + actual);
        }
    }

    private void expectDelim(char expected, char actual) throws TSVDataFormatException {
        if (expected != actual)
            throw new TSVDataFormatException(this, "Expecting delimiter "
                    + MiscUtil.printableUTF8(expected) + " but found "
                    + MiscUtil.printableUTF8(actual));
    }

    @Override
    public Iterable<String> read() throws IOException {
        List<String> record = new ArrayList<String>();
        boolean first = true;
        while (!isEndOfRecordNext()) {
            if (!first)
                parseValueDelimiter();
            record.add(readString());
        }
        endOfRecord();
        return record;
    }

}
