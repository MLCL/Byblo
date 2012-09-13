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

import junit.framework.Assert;
import org.junit.*;
import uk.ac.susx.mlcl.lib.test.ExitTrapper;
import uk.ac.susx.mlcl.testing.AbstractObjectTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertTrue;

public abstract class AbstractCommandTest<T extends Command> extends AbstractObjectTest<T> {

    boolean hasMethod(String name, Class<?>... parameterTypes) {
        try {
            getImplementation().getMethod(name, parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * If a main method exists, first check it conforms to the usual rules, then invoke it with no arguments. The
     * results will vary but we can expect that either it works fine, or that System.exit() was called with a non-zero
     * exist status.
     *
     * @throws Throwable
     */
    @Test
    public void testCommandExitStatus() throws Throwable {
        // Not all commands have a main method
        Assume.assumeTrue(hasMethod("main", String[].class));

        try {
            ExitTrapper.enableExistTrapping();

            Method main = getImplementation().getMethod("main", String[].class);

            Assert.assertTrue("main method must be static",
                    Modifier.isStatic(main.getModifiers()));
            Assert.assertTrue("main method must be public",
                    Modifier.isPublic(main.getModifiers()));
            Assert.assertTrue("main method must return void",
                    main.getReturnType() == void.class);

            try {

                main.invoke(null, (Object) new String[0]);

            } catch (InvocationTargetException ex) {
                // Wraps any exception thrown, including the the
                // ExitException we are expecting, just rethrow the cause
                throw ex.getCause();
            }

        } catch (ExitTrapper.ExitException ex) {
            assertTrue("Expecting non-zero exit status.", ex.getStatus() != 0);
        } finally {
            ExitTrapper.disableExitTrapping();
        }
    }

    @Test
    public void testHelp() throws Exception {
        Assume.assumeTrue(AbstractCommand.class
                .isAssignableFrom(getImplementation()));

        try {
            ExitTrapper.enableExistTrapping();

            T instance = newInstance();
            instance.runCommand(new String[]{"--help"});

        } catch (ExitTrapper.ExitException ex) {
            // If System.exit() is called by the command then it should be a zero exist status, though ideally it
            // shouldn't call System exit on success at all.
            assertTrue("Expecting zero exit status.", ex.getStatus() == 0);
        } finally {
            ExitTrapper.disableExitTrapping();
        }

    }

}
