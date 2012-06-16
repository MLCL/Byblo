/*
 * Copyright (c) 2011-2012, University of Sussex
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
package uk.ac.susx.mlcl.lib;

import java.io.IOException;

/**
 * Static utility class for String manipulation.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class Strings {

    private Strings() {
    }

    /**
     * Return a String representation of the given character as an ASCII escape
     * sequence.
     *
     * If the given character is one of the standard non-printable characters
     * then return a string contain a single backslash, followed by the
     * characters escape. For example new-line character will return the 2
     * character string "\n". Otherwise the character is escaped using it's
     * UTF character code escape sequence.
     *
     * @param ch character to escape
     * @return escaped character string
     */
    public static String escape(final char ch) {
        final StringBuilder sb = new StringBuilder();
        try {
            escape(ch, sb);
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
        return sb.toString();
    }

    /**
     * Append to the given destination a String representation of the given
     * character as an ASCII escape sequence.
     *
     * If the given character is one of the standard non-printable characters
     * then return a string contain a single backslash, followed by the
     * characters escape. For example new-line character will return the 2
     * character string "\n". Otherwise the character is escaped using it's
     * UTF character code escape sequence.
     *
     * @param ch character to escape
     * @param dst destination to write escaped character sequence
     * @throws IOException if {@link Appendable#append(java.lang.CharSequence)}
     *                  throws an IOException.
     */
    public static void escape(final char ch, final Appendable dst)
            throws IOException {
        if (ch >= 32 && ch < 128 && ch != '\\')
            dst.append(ch);
        switch (ch) {
            case '\b':
                dst.append("\\b");
                break;
            case '\t':
                dst.append("\\t");
                break;
            case '\n':
                dst.append("\\n");
                break;
            case '\f':
                dst.append("\\f");
                break;
            case '\r':
                dst.append("\\r");
                break;
            case '\\':
                dst.append("\\\\");
                break;
            case '\0':
                dst.append("\\0");
                break;
            default:
                dst.append(String.format("\\u%04x", (int) ch));
        }
    }

    /**
     * Read input CharSequence <tt>src</tt> converting all characters with a
     * UTF-8 code &lt; 32 to and ASCII escape sequence as defined in
     * {@link #escape(char) }.
     *
     * @param src character sequence to read
     * @return escaped character string
     */
    public static String escape(final CharSequence src) {
        return escape(src, 0, src.length());
    }

    /**
     * Read input CharSequence <tt>src</tt> starting at character
     * <tt>fromIndex</tt>, converting all characters with a UTF-8 code &lt; 32
     * to and ASCII escape sequence as defined in {@link #escape(char) }.
     *
     * @param src character sequence to read
     * @param fromIndex index of first character to read (inclusive)
     * @return escaped character string
     */
    public static String escape(final CharSequence src, final int fromIndex) {
        return escape(src, fromIndex, src.length());
    }

    /**
     * Read input CharSequence <tt>src</tt> starting at character
     * <tt>fromIndex</tt>, up to but no including character <tt>toIndex</tt>,
     * converting all characters with a UTF-8 code &lt; 32 to and ASCII escape
     * sequence as defined in {@link #escape(char) }.
     *
     * @param src character sequence to read
     * @param fromIndex index of first character to read (inclusive)
     * @param toIndex  index of last character to read (exclusive)
     * @return escaped character string
     */
    public static String escape(final CharSequence src, final int fromIndex, final int toIndex) {
        final StringBuilder sb = new StringBuilder();
        try {
            escape(src, fromIndex, toIndex, sb);
        } catch (IOException ex) {
            // StringBuilder appendable will never throw an IOException
            throw new AssertionError(ex);
        }
        return sb.toString();
    }

    /**
     * Read input CharSequence <tt>src</tt> converting all characters with a
     * UTF-8 code &lt; 32 to and ASCII escape sequence as defined in
     * {@link #escape(char) }. The result is appended to the given Appendable
     * <tt>dst</tt>.
     *
     * @param src character sequence to read
     * @param dst appendable to which escaped characters are written
     * @throws IOException if {@link Appendable#append(java.lang.CharSequence)}
     *                  throws an IOException.
     */
    public static void escape(final CharSequence src, final Appendable dst)
            throws IOException {
        escape(src, 0, src.length(), dst);
    }

    /**
     * Read input CharSequence <tt>src</tt> starting at character
     * <tt>fromIndex</tt>, converting all characters with a UTF-8 code &lt; 32
     * to and ASCII escape sequence as defined in {@link #escape(char) }. The
     * result is appended to the given Appendable <tt>dst</tt>.
     *
     * @param src character sequence to read
     * @param fromIndex index of first character to read (inclusive)
     * @param dst appendable to which escaped characters are written
     * @throws IOException if {@link Appendable#append(java.lang.CharSequence)}
     *                  throws an IOException.
     */
    public static void escape(final CharSequence src, final int fromIndex, final Appendable dst)
            throws IOException {
        escape(src, fromIndex, src.length(), dst);
    }

    /**
     * Read input CharSequence <tt>src</tt> starting at character
     * <tt>fromIndex</tt>, up to but no including character <tt>toIndex</tt>,
     * converting all characters with a UTF-8 code &lt; 32 to and ASCII escape
     * sequence as defined in {@link #escape(char) }. The result is appended to
     * the given Appendable <tt>dst</tt>.
     *
     * @param src character sequence to read
     * @param fromIndex index of first character to read (inclusive)
     * @param toIndex  index of last character to read (exclusive)
     * @param dst appendable to which escaped characters are written
     * @throws IOException if {@link Appendable#append(java.lang.CharSequence)}
     *                  throws an IOException.
     */
    public static void escape(final CharSequence src, final int fromIndex,
                              final int toIndex, final Appendable dst)
            throws IOException {
        int i = fromIndex;
        while (i < toIndex && src.charAt(i) >= 32) {
            i++;
        }
        dst.append(src, 0, i);

        while (i < toIndex) {

            if (src.charAt(i) < 32 || src.charAt(i) == '\\' || src.charAt(i) > 127)
                escape(src.charAt(i), dst);
            else
                dst.append(src.charAt(i));
            i++;
        }
    }

    /**
     *
     * @param src source sequence to read characters from
     * @return unescaped string
     */
    public static String unescape(final CharSequence src) {
        return unescape(src, 0, src.length());
    }

    /**
     *
     * @param src source sequence to read characters from
     * @param fromIndex index of first character to unescape (inclusive)
     * @return unescaped string
     */
    public static String unescape(final CharSequence src, final int fromIndex) {
        return unescape(src, fromIndex, src.length());
    }

    /**
     *
     * @param src source sequence to read characters from
     * @param fromIndex index of first character to unescape (inclusive)
     * @param toIndex index of last character to unescape (exclusive)
     * @return unescaped string
     */
    public static String unescape(final CharSequence src, final int fromIndex, final int toIndex) {
        final StringBuilder sb = new StringBuilder();
        try {
            unescape(src, fromIndex, toIndex, sb);
        } catch (IOException ex) {
            // StringBuilder appendable will never throw an IOException
            throw new AssertionError(ex);
        }
        return sb.toString();
    }

    /**
     *
     * @param src source sequence to read characters from
     * @param dst destination to write unescaped character sequence
     * @throws IOException if {@link Appendable#append(java.lang.CharSequence)}
     *                  throws an IOException.
     */
    public static void unescape(final CharSequence src, final Appendable dst)
            throws IOException {
        unescape(src, 0, src.length(), dst);
    }

    /**
     *
     * @param src source sequence to read characters from
     * @param fromIndex index of first character to unescape (inclusive)
     * @param dst destination to write unescaped character sequence
     * @throws IOException if {@link Appendable#append(java.lang.CharSequence)}
     *                  throws an IOException.
     */
    public static void unescape(final CharSequence src, final int fromIndex, final Appendable dst)
            throws IOException {
        unescape(src, fromIndex, src.length(), dst);
    }

    /**
     *
     * @param src source sequence to read characters from
     * @param fromIndex index of first character to unescape (inclusive)
     * @param toIndex index of last character to unescape (exclusive)
     * @param dst destination to write unescaped character sequence
     * @throws IOException if {@link Appendable#append(java.lang.CharSequence)}
     *                  throws an IOException.
     */
    public static void unescape(final CharSequence src, final int fromIndex,
                                final int toIndex, final Appendable dst)
            throws IOException {
        int i = fromIndex;
        char c;
        while (i < toIndex) {
            if ((c = src.charAt(i++)) == '\\') {
                final char escCode = src.charAt(i);
                i++;
                if (isOctalDigit(escCode)) {
                    int octal = escCode;
                    int count = 1;
                    while (i < toIndex && count < 3 && isOctalDigit(src.charAt(i))) {
                        octal = (8 * octal) + src.charAt(i);
                        i++;
                        count++;
                    }
                    dst.append((char) octal);
                } else {
                    switch (escCode) {
                        case 'a':
                            dst.append((char) 7);
                            break;
                        case 'b':
                            dst.append('\b');
                            break;
                        case 't':
                            dst.append('\t');
                            break;
                        case 'v':
                            dst.append((char) 11);
                            break;
                        case 'n':
                            dst.append('\n');
                            break;
                        case 'f':
                            dst.append('\f');
                            break;
                        case 'r':
                            dst.append('\r');
                            break;
                        case 'u': // utf code point
                            dst.append((char) Integer.parseInt(
                                    src.subSequence(i, i + 4).toString(), 16));
                            i += 4;
                            break;
                        case 'x': // ascii hex code
                            dst.append((char) Integer.parseInt(
                                    src.subSequence(i, i + 2).toString(), 16));
                            i += 2;
                            break;
                        default:
                            dst.append(escCode);
                    }
                }
            } else {
                dst.append(c);
            }

        }
    }

    /**
     * Return true if the given character is an octal digit (i.e in the range
     * '0' to '7' inclusive.
     *
     * @param c character to check
     * @return true if character is an octal digit, false otherwise
     */
    private static boolean isOctalDigit(final char c) {
        return c >= '0' && c <= '7';
    }

}
