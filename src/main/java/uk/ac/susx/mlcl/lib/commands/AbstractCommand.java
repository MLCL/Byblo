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
package uk.ac.susx.mlcl.lib.commands;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.google.common.base.Objects;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.Checks;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Parameters()
public abstract class AbstractCommand implements Command {

    private static final Log LOG = LogFactory.getLog(AbstractCommand.class);

    @Parameter(names = {"-h", "--help"},
               description = "Display this help message.")
    private boolean usageRequested = false;

    private final Map<String, Class<? extends Command>> subCommands;

    public AbstractCommand(Map<String, Class<? extends Command>> subCommands) {
        this.subCommands = subCommands;
    }

    public AbstractCommand() {
        this.subCommands = Collections.emptyMap();
    }

    public final boolean isUsageRequested() {
        return usageRequested;
    }

    @Override
    public abstract void runCommand() throws Exception;

    @Override
    public void runCommand(final String[] args) throws Exception {
        Checks.checkNotNull("args", args);

        if (LOG.isTraceEnabled())
            LOG.trace("Initialising command: " + this);

        final JCommander jc = new JCommander();
        jc.setProgramName(this.getClass().getSimpleName());
        jc.addConverterFactory(new ConverterFactory());
        jc.addObject(this);

        // If sub-commands have been set then instantiate all the objects
        // and pass them to JCommander
        final Map<String, Command> subCommandInstances;
        if (!subCommands.isEmpty()) {

            if (LOG.isTraceEnabled())
                LOG.trace("Initialising subcommands: " + subCommands);

            subCommandInstances = new HashMap<String, Command>();
            for (String command : subCommands.keySet()) {
                final Command instance = subCommands.get(command).newInstance();
                jc.addCommand(command, instance);
                subCommandInstances.put(command, instance);
            }
        } else {
            subCommandInstances = Collections.emptyMap();
        }


        try {

            if (LOG.isTraceEnabled())
                LOG.trace("Parsing command line options.");
            jc.parse(args);


        } catch (ParameterException ex) {
            if (LOG.isTraceEnabled())
                LOG.trace("Parsing exceoption", ex);

            System.err.println(ex.getMessage());
            StringBuilder sb = new StringBuilder();


            if (jc.getParsedCommand() == null) {
                jc.usage(sb);
            } else {
                jc.usage(jc.getParsedCommand(), sb);
            }

            System.err.println(sb);
            throw ex;
        }


        if (isUsageRequested()) {
            if (LOG.isTraceEnabled())
                LOG.trace("Printing usage.");

            if (jc.getParsedCommand() == null) {
                jc.usage();
            } else {
                jc.usage(jc.getParsedCommand());
            }

        } else if (!subCommands.isEmpty() && jc.getParsedCommand() == null) {

            if (LOG.isTraceEnabled())
                LOG.trace("Command reguired but not given.");

            System.err.println("Command reguired but not given.");
            StringBuilder sb = new StringBuilder();
            jc.usage(sb);
            System.err.println(sb);

            throw new ParameterException("Command reguired but not given.");

        } else {

            if (jc.getParsedCommand() != null) {
                Command instance = subCommandInstances.
                        get(jc.getParsedCommand());
                if (LOG.isTraceEnabled())
                    LOG.
                            trace(
                            "Running subcommand " + jc.getParsedCommand() + ": "
                            + instance);
                instance.runCommand();
            } else {
                LOG.trace("Running command: " + this);
                this.runCommand();
            }
        }
        LOG.trace("Completed command: " + this);
    }

    @Override
    public final String toString() {
        return toStringHelper().toString();
    }

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this).
                add("help", isUsageRequested()).
                add("subCommands", subCommands);
    }
}
