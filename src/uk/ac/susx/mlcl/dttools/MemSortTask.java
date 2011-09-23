/*
 * Copyright (c) 2010-2011, University of Sussex
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
package uk.ac.susx.mlcl.dttools;

import com.beust.jcommander.Parameter;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @version 2nd December 2010
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
public class MemSortTask extends CopyTask {

    private static final Logger LOG = Logger.getLogger(
            MemSortTask.class.getName());

    private Comparator<String> comparator;

    @Parameter(names = {"--charset"},
               descriptionKey = "USAGE_CHARSET")
    private Charset charset = IOUtil.DEFAULT_CHARSET;

    public final Charset getCharset() {
        return charset;
    }

    public final void setCharset(Charset charset) {
        Checks.checkNotNull(charset);
        this.charset = charset;
    }

    public MemSortTask(File sourceFile, File destinationFile, Charset charset,
                       Comparator<String> comparator) {
        super(sourceFile, destinationFile);
        setCharset(charset);
        this.comparator = comparator;
    }

    public final Comparator<String> getComparator() {
        return comparator;
    }

    public final void setComparator(Comparator<String> comparator) {
        if (comparator == null)
            throw new NullPointerException("comparator is null");
        this.comparator = comparator;
    }

    @Override
    protected void runTask() throws Exception {
        LOG.log(Level.INFO,
                "Sorting file in memory, from \"{0}\" to \"{1}\". ({2})",
                new Object[]{getSrcFile(), getDstFile(), Thread.currentThread().
                    getName()});
        final List<String> lines = new ArrayList<String>();
        IOUtil.readAllLines(getSrcFile(), getCharset(), lines);
        Collections.sort(lines, getComparator());
        IOUtil.writeAllLines(getDstFile(), getCharset(), lines);
    }
}
