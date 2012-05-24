/*
 * Copyright (c) 2010-2012, MLCL, University of Sussex
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

import java.io.File;
import java.io.IOException;

/**
 * A file {@link FileFactory} implementation that creates temporary files using
 * {@link File#createTempFile(java.lang.String, java.lang.String)} to produce
 * new files.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class TempFileFactory implements FileFactory {

    public static final String DEFAULT_PREFIX = "tmp-";

    public static final String DEFAULT_SUFFIX = ".tmp";

    private String prefix;

    private String suffix;

    private File directory;

    /**
     * Construct a new instance of the TempFileFactory, where created files will
     * be stored in the given <tt>directory</tt>, and will be be named using the
     * provided <tt>prefix</tt> and <tt>suffix</tt>.
     *
     * Note that the constraints for <tt>suffix</tt> and <tt>prefix</tt> are
     * inherited from {@link File#createTempFile(java.lang.String, java.lang.String)};
     * i.e that the <tt>prefix</tt> must be non-null and at least 3 characters
     * long, but that <tt>suffix</tt> can be anything. If <tt>suffix</tt> is
     * null then <tt>.tmp</tt> is used.
     *
     * @param prefix String that will appended to the start of the name of each
     * created file
     * @param suffix String that will appended to the end of the name of each
     * created file
     * @param directory Create file will be inside this directory
     * @throws NullPointerException when prefix is null
     * @throws IllegalArgumentException when prefix length &lt; 3 characters
     */
    public TempFileFactory(String prefix, String suffix, File directory)
            throws NullPointerException, IllegalArgumentException {
        setPrefix(prefix);
        setSuffix(suffix);
        setDirectory(directory);
    }

    /**
     * Construct a new instance of the TempFileFactory, where created files will
     * be stored in the given <tt>directory</tt>, and will be be named using the
     * provided <tt>prefix</tt>; The <tt>suffix</tt> will be <tt>.tmp</tt>.
     *
     * @param prefix String that will appended to the start of the name of each
     * created file
     * @param directory Create file will be inside this directory
     * @throws NullPointerException when prefix is null
     * @throws IllegalArgumentException when prefix length &lt; 3 characters
     */
    public TempFileFactory(String prefix, File directory)
            throws NullPointerException, IllegalArgumentException {
        this(prefix, DEFAULT_SUFFIX, directory);
    }

    /**
     * Construct a new instance of the TempFileFactory, where created files will
     * be stored in the given <tt>directory</tt>; The created files will be
     * named using a <tt>prefix</tt> of <tt>tmp-</tt> and <tt>suffix</tt> of
     * <tt>.tmp</tt>.
     *
     * @param directory Create file will be inside this directory
     */
    public TempFileFactory(File directory) {
        this(DEFAULT_PREFIX, DEFAULT_SUFFIX, directory);
    }

    /**
     * Construct a new instance of the TempFileFactory, where created files will
     * be stored in the given system default temporary directory; files will be
     * named using the provided <tt>prefix</tt> and <tt>suffix</tt>.
     *
     * @param prefix String that will appended to the start of the name of each
     * created file
     * @param suffix String that will appended to the end of the name of each
     * created file
     * @throws NullPointerException when prefix is null
     * @throws IllegalArgumentException when prefix length &lt; 3 characters
     */
    public TempFileFactory(String prefix, String suffix)
            throws NullPointerException, IllegalArgumentException {
        this(prefix, suffix, getSystemDefaultTmpDir());
    }

    /**
     * Construct a new instance of the TempFileFactory, where created files will
     * be stored in the given system default temporary directory; files will be
     * named using the provided <tt>prefix</tt> and the <tt>suffix</tt> will be
     * set to <tt>.tmp</tt>.
     *
     * @param prefix String that will appended to the start of the name of each
     * created file
     * @throws NullPointerException when prefix is null
     * @throws IllegalArgumentException when prefix length &lt; 3 characters
     */
    public TempFileFactory(String prefix)
            throws NullPointerException, IllegalArgumentException {
        this(prefix, DEFAULT_SUFFIX, getSystemDefaultTmpDir());
    }

    /**
     * Construct a new instance of the TempFileFactory, where created files will
     * be stored in the given system default temporary directory; The created
     * files will be named using a <tt>prefix</tt> of <tt>tmp-</tt> and
     * <tt>suffix</tt> of <tt>.tmp</tt>.
     *
     */
    public TempFileFactory() {
        this(DEFAULT_PREFIX, DEFAULT_SUFFIX, getSystemDefaultTmpDir());
    }

    @Override
    public File createFile() throws IOException {
        return File.createTempFile(prefix, suffix, directory);
    }

    @Override
    public File createFile(String pref, String suff) throws IOException {
        return File.createTempFile(pref + prefix, suffix + suff, directory);
    }

    /**
     *
     * @return string appended to end of created file names
     */
    public final String getSuffix() {
        return suffix;
    }

    /**
     *
     * @param suffix string appended to end of created file names
     */
    public final void setSuffix(String suffix) {
        this.suffix = (suffix == null) ? DEFAULT_SUFFIX : suffix;;
    }

    /**
     *
     * @return string appended to start of created file names
     */
    public final String getPrefix() {
        return prefix;
    }

    /**
     *
     * @param prefix string appended to start of created file names
     * @throws NullPointerException when prefix is null
     * @throws IllegalArgumentException when suffix length &lt; 3 characters
     */
    public final void setPrefix(String prefix)
            throws NullPointerException, IllegalArgumentException {
        if (prefix == null)
            throw new NullPointerException();
        if (prefix.length() < 3)
            throw new IllegalArgumentException("Prefix string too short");
        this.prefix = prefix;
    }

    /**
     *
     * @return location where files are created
     */
    public final File getDirectory() {
        return directory;
    }

    /**
     *
     * @param directory location where files are created
     */
    public final void setDirectory(File directory) {
        this.directory = directory == null
                         ? getSystemDefaultTmpDir()
                         : directory;
    }

    /**
     * The default temporary directory for the current system.
     *
     * @return system default temporary file directory.
     */
    public static File getSystemDefaultTmpDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDirectory().toString());
        sb.append(File.separator);
        sb.append(getPrefix());
        sb.append("<uniqueid>");
        sb.append(getSuffix());
        return sb.toString();
    }

}
