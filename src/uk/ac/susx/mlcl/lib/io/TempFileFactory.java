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
 * A file {@link FileFactory} implementation that creates temporary files
 * using {@link File#createTempFile(java.lang.String, java.lang.String) to 
 * produce new files.
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class TempFileFactory implements FileFactory {

    public static final String DEFAULT_PREFIX =  "tmp";

    public static final String DEFAULT_SUFFIX = "-tmp";

    public static final File DEFAULT_DIRECTORY =
            new File(System.getProperty("java.io.tmpdir"));

    private String prefix;

    private String suffix;

    private File directory;

    public TempFileFactory(String prefix, String suffix, File directory) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.directory = directory;
    }

    public TempFileFactory(String prefix, File directory) {
        this(prefix, DEFAULT_SUFFIX, directory);
    }

    public TempFileFactory(File directory) {
        this(DEFAULT_PREFIX, DEFAULT_SUFFIX, directory);
    }

    public TempFileFactory(String prefix, String suffix) {
        this(prefix, suffix, DEFAULT_DIRECTORY);
    }

    public TempFileFactory(String prefix) {
        this(prefix, DEFAULT_SUFFIX, DEFAULT_DIRECTORY);
    }

    public TempFileFactory() {
        this(DEFAULT_PREFIX, DEFAULT_SUFFIX, DEFAULT_DIRECTORY);
    }

    @Override
    public File createFile() throws IOException {
        return File.createTempFile(/*callingClass() +*/ prefix, suffix, directory);
    }

    private String callingClass() {
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        int i = 0;
        String className = this.getClass().getName();
        while (i < ste.length && !ste[i].getClassName().equals(this.getClass().
                getName())) {
            className = ste[i].getClassName();
            i++;
        }
        while (i < ste.length && ste[i].getClassName().equals(this.getClass().
                getName())) {
            className = ste[i].getClassName();
            i++;
        }
        if (i < ste.length)
            className = ste[i].getClassName();

        
        if(className.length() > 10)
            className = className.substring(0, 10);
        
        return className;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    @Override
    public String toString() {
        return DEFAULT_DIRECTORY.toString();
    }


}
