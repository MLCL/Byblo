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
package uk.ac.susx.mlcl.lib;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.google.common.base.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.commands.Command;
import uk.ac.susx.mlcl.lib.commands.ConverterFactory;
import uk.ac.susx.mlcl.lib.tasks.AbstractTask;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @deprecated temporary class while command and task APIs are being separated
 */
@Parameters()
@Deprecated
public abstract class AbstractCommandTask extends AbstractTask implements Command {

    private static final Log LOG = LogFactory.getLog(AbstractCommandTask.class);

    @Parameter(names = {"-h", "--help"},
            description = "Display this help message.")
    private boolean usageRequested = false;

    public AbstractCommandTask() {
    }

    public final boolean isUsageRequested() {
        return usageRequested;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this).
                add("help", isUsageRequested());
    }

    @Override
    public void runCommand() throws Exception {
        this.run();
        while (this.isExceptionTrapped())
            this.throwTrappedException();
    }

    @Override
    public void runCommand(String[] args)
            throws InstantiationException, IllegalAccessException, Exception {

        Checks.checkNotNull("args", args);

        if (LOG.isTraceEnabled())
            LOG.trace("Initialising command " + this);

        final JCommander jc = new JCommander();
        jc.setProgramName(this.getClass().getSimpleName());
        jc.addConverterFactory(new ConverterFactory());

        jc.addObject(this);

        try {
            jc.parse(args);
        } catch (ParameterException ex) {
            System.err.println(ex.getMessage());
            final StringBuilder builder = new StringBuilder();
            jc.usage(builder);
            System.err.println(builder);
            throw ex;
        }

        if (LOG.isTraceEnabled())
            LOG.trace("Running command " + this);

        if (this.isUsageRequested()) {
            jc.usage();
        } else {
            this.runCommand();
        }

        if (LOG.isTraceEnabled())
            LOG.trace("Completed command " + this);
    }
}
