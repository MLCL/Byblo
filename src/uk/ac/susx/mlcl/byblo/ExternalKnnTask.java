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
package uk.ac.susx.mlcl.byblo;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import uk.ac.susx.mlcl.lib.tasks.Task;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Perform k-nearest-neighbours on a similarity file.")
public class ExternalKnnTask extends ExternalSortTask {

    private static final Logger LOG =
            Logger.getLogger(ExternalKnnTask.class.getName());

    public static final int DEFAULT_K = 100;

    @Parameter(names = {"-k"}, description = "The number of neighbours to produce for each base entry.")
    private int k = DEFAULT_K;

    public ExternalKnnTask(File sourceFile, File destinationFile, Charset charset,
                      Comparator<String> comparator, int maxChunkSize, int k) {
        super(sourceFile, destinationFile, charset, comparator, maxChunkSize);
        setK(k);
    }

    public ExternalKnnTask(File sourceFile, File destinationFile, Charset charset, int k) {
        super(sourceFile, destinationFile, charset);
        setK(k);
    }
    public ExternalKnnTask() {
        super();
    }

    @Override
    protected void initialiseTask() throws Exception {
        super.initialiseTask();
        if (getComparator() == null)
            throw new NullPointerException();

    }

    @Override
    protected void finaliseTask() throws Exception {
        super.initialiseTask();
    }

    public final int getK() {
        return k;
    }

    public final void setK(int k) {
        if (k < 1)
            throw new IllegalArgumentException("k < 1");
        this.k = k;
    }

    @Override
    protected void runTask() throws Exception {

        LOG.log(Level.INFO,
                "Running KNN externally: from \"{0}\" to \"{1}\". ({2})",
                new Object[]{getSrcFile(), getDestFile(),
                    Thread.currentThread().getName()});

        map();
        reduce();
        finish();
    }

    @Override
    protected void handleCompletedTask(Task task) throws Exception {
        task.throwException();
        if (task.getClass().equals(MergeTask.class)) {

            MergeTask mergeTask = (MergeTask) task;
            submitTask(new DeleteTask(mergeTask.getSourceFileA()));
            submitTask(new DeleteTask(mergeTask.getSourceFileB()));
            submitTask(new KnnTask(mergeTask.getDestFile(),
                                     mergeTask.getDestFile(),
                                     getCharset(),
                                     getComparator(), getK()));

        } else if (task.getClass().equals(SortTask.class)) {

            SortTask sortTask = (SortTask) task;
            submitTask(new KnnTask(sortTask.getDstFile(),
                                     sortTask.getDstFile(),
                                     getCharset(),
                                     getComparator(), getK()));

        } else if (task.getClass().equals(KnnTask.class)) {

            KnnTask knnTask = (KnnTask) task;
            queueMergeTask(knnTask.getDstFile());

        } else if (task.getClass().equals(DeleteTask.class)) {
            // not a sausage
        } else {
            throw new AssertionError(
                    "Task type " + task.getClass()
                    + " should not have been queued.");
        }
    }
}
