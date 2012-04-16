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
 * POSSIBILITY OF SUCH DAMAGE.To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.commands.AbstractCommand;
import uk.ac.susx.mlcl.lib.io.FileFactory;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import static uk.ac.susx.mlcl.lib.Checks.*;
import static java.text.MessageFormat.format;
import uk.ac.susx.mlcl.byblo.commands.ExternalCountCommand;
import uk.ac.susx.mlcl.byblo.commands.IndexTPCommand;
import uk.ac.susx.mlcl.lib.Checks;

/**
 *
 * @author hiam20
 */
public class FullBuild extends AbstractCommand {

    private static final Log LOG = LogFactory.getLog(FullBuild.class);

    private Charset charset = Charset.defaultCharset();

    private File instancesFile;

    private File outputDir;

    private File tempBaseDir;

    public FullBuild() {
    }

    public void setInstancesFile(File instancesFile) {
        this.instancesFile = instancesFile;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public File getTempBaseDir() {
        return tempBaseDir;
    }

    public void setTempBaseDir(File tempBaseDir) {
        this.tempBaseDir = tempBaseDir;
    }

    @Override
    public void runCommand() throws Exception {

        checkValidInputFile("Instances file", instancesFile);

        // If the output dir isn't
        if (outputDir == null)
            outputDir = instancesFile.getParentFile();
        checkValidOutputDir("Output dir", outputDir);

        if (tempBaseDir == null)
            tempBaseDir = createTempSubdirDir(outputDir);

        File entryIndexFile =
                new File(outputDir, instancesFile.getName() + ".entry-index");
        File featureIndexFile =
                new File(outputDir, instancesFile.getName() + ".feature-index");
        File instancesIndexedFile =
                new File(outputDir, instancesFile.getName() + ".indexed");


        {
            checkValidOutputFile("Indexed instances file", instancesIndexedFile);
            checkValidOutputFile("Feature index file", featureIndexFile);
            checkValidOutputFile("Entry index file", entryIndexFile);

            IndexTPCommand indexCmd = new IndexTPCommand();
            indexCmd.setSourceFile(instancesFile);
            indexCmd.setDestinationFile(instancesIndexedFile);
            indexCmd.setCharset(charset);
            indexCmd.setEntryEnumeratorFile(entryIndexFile);
            indexCmd.setFeatureEnumeratorFile(featureIndexFile);
            indexCmd.runCommand();

            checkValidInputFile("Indexed instances file", instancesIndexedFile);
        }


        {
            
//            checkValidInputFile("Indexed instances file", instancesIndexedFile);
//            
//            
//            ExternalCountCommand countCmd = new ExternalCountCommand();
//            countCmd.setInstancesFile(outputDir);
//            countCmd.setEntriesFile(entryIndexFile);
//            countCmd.set
            
            
        }





    }

    private static File createTempSubdirDir(File base) throws IOException {
        checkValidOutputDir("Temporary base directory", base);
        FileFactory tmp = new TempFileFactory(base);
        File tempDir = tmp.createFile("tempdir", "");
        LOG.debug(format("Creating temporary directory {0}", tempDir));
        if (!tempDir.delete() || tempDir.mkdir())
            throw new IOException(format(
                    "Unable to create temporary directory {0}", tempDir));
        checkValidOutputDir("Temporary directory", tempDir);
        return tempDir;
    }

    private static File suffixed(File file, String suffix) {
        return new File(file.getParentFile(), file.getName() + suffix);
    }

    public static void checkValidInputFile(File file) {
        checkValidInputFile("Input file", file);
    }

    public static void checkValidInputFile(String name, File file) {
        Checks.checkNotNull(name, file);
        if (!file.exists())
            throw new IllegalArgumentException(format(
                    "{0} does not exist: {1}", name, file));
        if (!file.canRead())
            throw new IllegalArgumentException(format(
                    "{0} is not readable: {0}", name, file));
        if (!file.isFile())
            throw new IllegalArgumentException(format(
                    "{0} is not a regular file: ", name, file));

    }

    public static void checkValidOutputFile(File file) {
        checkValidOutputFile("Output file", file);
    }

    public static void checkValidOutputFile(String name, File file) {
        Checks.checkNotNull(name, file);
        if (file.exists()) {
            if (!file.isFile())
                throw new IllegalArgumentException(format(
                        "{0} already exists, but not regular: {1}", name, file));
            if (!file.canWrite())
                throw new IllegalArgumentException(format(
                        "{0} already exists, but is not writeable: {1}", name, file));
        } else {
            if (!file.getParentFile().canWrite())
                throw new IllegalArgumentException(
                        format("{0} can not be created, because the parent "
                        + "directory is not writeable: {1}", name, file));
        }
    }

    public static void checkValidOutputDir(File dir) {
        checkValidOutputDir("Output directory", dir);
    }

    public static void checkValidOutputDir(String name, File file) {
        Checks.checkNotNull(name, file);
        if (!file.exists())
            throw new IllegalArgumentException(format(
                    "{0} does not exist: {1}", name, file));
        if (!file.canWrite())
            throw new IllegalArgumentException(format(
                    "{0} is not writeable: {0}", name, file));
        if (!file.isDirectory())
            throw new IllegalArgumentException(format(
                    "{0} is not a directory: ", name, file));

    }

}
