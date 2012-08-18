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

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * <code>Command</code> defines an interface for an object that can be run from
 * the command line.
 * <p/>
 * Note that both {@link #runCommand() } and {@link #runCommand(java.lang.String[])
 * } will return
 * <code>false</code> when a problem occurred, which was handled internally. For
 * example when a invalid command line parameters are found, the method should
 * have already displayed a message to std-err. In this case the application is
 * expected to exit with non-zero status, but that's up to the caller. When the
 * command encounters an exception it is unable to handle it will throw a
 * {@link RuntimeException}. If the command completed successfully those methods
 * will return
 * <code>true</code>
 * <p/>
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public interface Command {

    /**
     * Run the command with no arguments, either because it requires none, or
     * because they have been injected elsewhere.
     * <p/>
     * Note:
     * <code>runCommand</code> will return false if a problem occurred that was
     * handled internally. For example when a
     * <p/>
     * @return true if the command complete successfully, false otherwise
     */
    @CheckReturnValue
    boolean runCommand();

    /**
     * Run the command with the given command line arguments.
     * <p/>
     * @param args tokenised command line arguments
     * @return true if the command complete successfully, false otherwise
     */
    @CheckReturnValue
    boolean runCommand(@Nonnull String[] args);
}
