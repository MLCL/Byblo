/*
 * Copyright (c) 2010-2011, MLCL Lab, University of Sussex
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
package uk.ac.susx.mlcl.lib.test;

import java.security.Permission;

/**
 * Static utility class that allows calls to System.exit() to be intercepted. 
 * This makes direct testing of <tt>main</tt> methods possible, since all calls
 * to {@link System#exit(int) } result in a runtime {@link ExitException},
 * rather than simply terminating the forked VM (passing the test).
 * 
 * <p>Note that this class will obliterate whatever SecurityManager is installed
 * if it can. The previously install SecurityManager will however be reinstated
 * when trapping is disabled.</p>
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ExitTrapper {

    /**
     * Store an instance of the security manager.
     */
    private static SecurityManager noExitSM;

    /**
     * Store the previously installed security manager. This will be re-instated
     * when trapping is disabled.
     */
    private static SecurityManager previousSM;

    /**
     * Static utility class can't be constructed
     */
    private ExitTrapper() {
    }

    /**
     * Initialise the class - instantiate the exception object.
     */
    private static synchronized void initExitTrapper() {
        if (noExitSM == null)
            noExitSM = new NoExitSecurityManager();
    }

    /**
     * Turn on exit trapping if it's off
     */
    public static synchronized void enableExistTrapping() {
        if (!isExitTrappingEnabled()) {
            previousSM = System.getSecurityManager();
            System.setSecurityManager(noExitSM);
        }
    }

    /**
     * Turn off exit trapping if it's on
     */
    public static synchronized void disableExitTrapping() {
        if (isExitTrappingEnabled()) {
            System.setSecurityManager(previousSM);
        }
    }

    /**
     * Turn off exit trapping if it's on, otherwise turn it on
     */
    public static synchronized void toggleExitTrapping() {
        System.setSecurityManager(
                isExitTrappingEnabled()
                ? previousSM
                : noExitSM);
    }

    /**
     * Return whether or not exit trapping is enabled.
     * 
     * @return true if exit trapping is enabled, false otherwise
     */
    public static synchronized boolean isExitTrappingEnabled() {
        initExitTrapper();
        return System.getSecurityManager() == noExitSM;
    }

    /**
     * Instances of this RuntimeException will be thrown when calls to 
     * System.exit are made and trapping is enabled.
     */
    public static final class ExitException extends SecurityException {

        private static final long serialVersionUID = 1L;

        /**
         * Exit status code that was trapped.
         */
        private final int status;

        public ExitException(int status) {
            super("Call to System.exit(" + status + ") trapped.");
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

    }

    /**
     * Security manager instance that will throw exceptions when System.exit is
     * called.
     */
    private static final class NoExitSecurityManager extends SecurityManager {

        @Override
        public void checkPermission(Permission perm) {
            // allow everything.
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
            // allow everything.
        }

        @Override
        public void checkExit(int status) {
            super.checkExit(status);
            throw new ExitException(status);
        }

    }
}
