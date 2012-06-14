/*
 * Copyright (c) 2011-2012, University of Sussex
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

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;
import java.text.MessageFormat;

/**
 * Static utility class that allows calls to {@link java.lang.System#exit(int)}
 * to be intercepted.  This makes direct testing of <tt>main</tt> methods 
 * possible, since all calls to {@link System#exit(int) } result in a runtime 
 * {@link ExitException}, rather than simply terminating the forked VM (passing
 * the test).
 * 
 * <p>When exit trapping is enabled, the previously installed 
 * {@link java.lang.SecurityManager} will be temporarily encapsulate, 
 * over-riding {@link java.lang.SecurityManager#checkExit(int)}. It will pass 
 * all other security related checks to the encapsulated security manager (if 
 * there was one). When trapping is disabled the previously installed manager 
 * will be re-instated.</p>
 * 
 * <p>Take care that there are guarantees in place to insure ExitTrapper is 
 * disabled when not required, otherwise it's functionality will bleed into
 * other area of VM. To insure ExitTrapper is disabled enclosed it in a
 * <tt>try/finally</tt> block:</p>
 * <pre>
 *  try {
 *      ExitTrapper.enableExistTrapping();
 * 
 *      // Do trapped stuff here...
 * 
 *  } catch(ExitException ex) {
 *      // System.exit() was called
 *      int status = ex.getStatus();
 *      // Handle exception here
 *  }finally {
 *      ExitTrapper.disableExitTrapping();
 *  }
 * </pre>
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ExitTrapper {

    /**
     * Static utility class can't be constructed
     */
    private ExitTrapper() {
    }

    /**
     * Turn on exit trapping if it's currently off
     */
    public static synchronized void enableExistTrapping() {
        if (!isExitTrappingEnabled()) {
            System.setSecurityManager(
                    new NoExitSecurityManager(System.getSecurityManager()));
        }
    }

    /**
     * Turn off exit trapping if it's currently on
     */
    public static synchronized void disableExitTrapping() {
        if (isExitTrappingEnabled()) {
            System.setSecurityManager(
                    ((NoExitSecurityManager) System.getSecurityManager()).
                    getInner());
        }
    }

    /**
     * Turn off exit trapping if it's currently on, otherwise turn it on
     */
    public static synchronized void toggleExitTrapping() {
        if (isExitTrappingEnabled())
            disableExitTrapping();
        else
            enableExistTrapping();
    }

    /**
     * Return whether or not exit trapping is enabled.
     * 
     * @return true if exit trapping is enabled, false otherwise
     */
    public static synchronized boolean isExitTrappingEnabled() {
        final SecurityManager currentSM = System.getSecurityManager();
        return currentSM != null && currentSM instanceof NoExitSecurityManager;
    }

    /**
     * <tt>ExitException</tt> is a {@link hava.lang.SecurityException } that 
     * will be thrown when {@link java.lang.System#exit(int)} is called and 
     * trapping is enabled.
     */
    public static final class ExitException extends SecurityException {

        private static final long serialVersionUID = 1L;

        private static final String MESSAGE =
                "Call to System.exit({0,number,integer}) trapped.";

        /**
         * Exit status code that was trapped.
         */
        private final int status;

        /**
         * Construct a new ExitException with the given exit status code.
         * 
         * @param status Code passed to {@link java.lang.System#exit(int)}
         */
        public ExitException(int status) {
            super(MessageFormat.format(MESSAGE, status));
            this.status = status;
        }

        /**
         * The status code that was passed to {@link java.lang.System#exit(int)}
         * 
         * @return The status code of trapped exit.
         */
        public final int getStatus() {
            return status;
        }
    }

    /**
     * Security manager instance that will throw exceptions when System.exit is
     * called.
     */
    private static final class NoExitSecurityManager
            extends SecurityManagerDecoratorAdapter {

        private NoExitSecurityManager(SecurityManager inner) {
            super(inner);
        }

        private NoExitSecurityManager() {
            super();
        }

        @Override
        public void checkExit(int status) throws ExitException {
            super.checkExit(status);
            throw new ExitException(status);
        }

        @Override
        public void checkPermission(Permission perm) {
            if (isInnerSet())
                getInner().checkPermission(perm);
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
            if (isInnerSet())
                getInner().checkPermission(perm, context);
        }
    }

    /**
     * SecurityManagerDecoratorAdapter wraps a given security manager, 
     * delegating all calls to the inner class. This class is intended to be
     * extended, with only a subset of the method overridden.
     */
    private static abstract class SecurityManagerDecoratorAdapter
            extends SecurityManager {

        private final SecurityManager inner;

        private SecurityManagerDecoratorAdapter(SecurityManager inner) {
            this.inner = inner;
        }

        private SecurityManagerDecoratorAdapter() {
            this(null);
        }

        public SecurityManager getInner() {
            return inner;
        }

        public boolean isInnerSet() {
            return inner != null;
        }

        @Override
        public void checkExit(int status) {
            if (isInnerSet())
                inner.checkExit(status);
            else
                super.checkExit(status);
        }

        @Override
        public void checkPermission(Permission perm) {
            if (isInnerSet())
                inner.checkPermission(perm);
            else
                super.checkPermission(perm);
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
            if (isInnerSet())
                inner.checkPermission(perm, context);
            else
                super.checkPermission(perm, context);
        }

        @Override
        public ThreadGroup getThreadGroup() {
            return !isInnerSet()
                    ? super.getThreadGroup()
                    : inner.getThreadGroup();
        }

        @Override
        public Object getSecurityContext() {
            return !isInnerSet()
                    ? super.getSecurityContext()
                    : inner.getSecurityContext();
        }

        @Override
        @Deprecated
        public boolean getInCheck() {
            return !isInnerSet()
                    ? super.getInCheck()
                    : inner.getInCheck();
        }

        @Override
        public void checkWrite(String file) {
            if (isInnerSet())
                inner.checkWrite(file);
            else
                super.checkWrite(file);
        }

        @Override
        public void checkWrite(FileDescriptor fd) {
            if (isInnerSet())
                inner.checkWrite(fd);
            else
                super.checkWrite(fd);
        }

        @Override
        public boolean checkTopLevelWindow(Object window) {
            return !isInnerSet()
                    ? super.checkTopLevelWindow(window)
                    : inner.checkTopLevelWindow(window);
        }

        @Override
        public void checkSystemClipboardAccess() {
            if (isInnerSet())
                inner.checkSystemClipboardAccess();
            else
                super.checkSystemClipboardAccess();
        }

        @Override
        public void checkSetFactory() {
            if (isInnerSet())
                inner.checkSetFactory();
            else
                super.checkSetFactory();
        }

        @Override
        public void checkSecurityAccess(String target) {
            if (isInnerSet())
                inner.checkSecurityAccess(target);
            else
                super.checkSecurityAccess(target);
        }

        @Override
        public void checkRead(String file, Object context) {
            if (isInnerSet())
                inner.checkRead(file, context);
            else
                super.checkRead(file, context);
        }

        @Override
        public void checkRead(String file) {
            if (isInnerSet())
                inner.checkRead(file);
            else
                super.checkRead(file);
        }

        @Override
        public void checkRead(FileDescriptor fd) {
            if (isInnerSet())
                inner.checkRead(fd);
            else
                super.checkRead(fd);
        }

        @Override
        public void checkPropertyAccess(String key) {
            if (isInnerSet())
                inner.checkPropertyAccess(key);
            else
                super.checkPropertyAccess(key);
        }

        @Override
        public void checkPropertiesAccess() {
            if (isInnerSet())
                inner.checkPropertiesAccess();
            else
                super.checkPropertiesAccess();
        }

        @Override
        public void checkPrintJobAccess() {
            if (isInnerSet())
                inner.checkPrintJobAccess();
            else
                super.checkPrintJobAccess();
        }

        @Override
        public void checkPackageDefinition(String pkg) {
            if (isInnerSet())
                inner.checkPackageDefinition(pkg);
            else
                super.checkPackageDefinition(pkg);
        }

        @Override
        public void checkPackageAccess(String pkg) {
            if (isInnerSet())
                inner.checkPackageAccess(pkg);
            else
                super.checkPackageAccess(pkg);
        }

        @Override
        @Deprecated
        public void checkMulticast(InetAddress maddr, byte ttl) {
            if (isInnerSet())
                inner.checkMulticast(maddr, ttl);
            else
                super.checkMulticast(maddr, ttl);
        }

        @Override
        public void checkMulticast(InetAddress maddr) {
            if (isInnerSet())
                inner.checkMulticast(maddr);
            else
                super.checkMulticast(maddr);
        }

        @Override
        public void checkMemberAccess(Class<?> clazz, int which) {
            if (isInnerSet())
                inner.checkMemberAccess(clazz, which);
            else
                super.checkMemberAccess(clazz, which);
        }

        @Override
        public void checkListen(int port) {
            if (isInnerSet())
                inner.checkListen(port);
            else
                super.checkListen(port);
        }

        @Override
        public void checkLink(String lib) {
            if (isInnerSet())
                inner.checkLink(lib);
            else
                super.checkLink(lib);
        }

        @Override
        public void checkExec(String cmd) {
            if (isInnerSet())
                inner.checkExec(cmd);
            else
                super.checkExec(cmd);
        }

        @Override
        public void checkDelete(String file) {
            if (isInnerSet())
                inner.checkDelete(file);
            else
                super.checkDelete(file);
        }

        @Override
        public void checkCreateClassLoader() {
            if (isInnerSet())
                inner.checkCreateClassLoader();
            else
                super.checkCreateClassLoader();
        }

        @Override
        public void checkConnect(String host, int port, Object context) {
            if (isInnerSet())
                inner.checkConnect(host, port, context);
            else
                super.checkConnect(host, port, context);
        }

        @Override
        public void checkConnect(String host, int port) {
            if (isInnerSet())
                inner.checkConnect(host, port);
            else
                super.checkConnect(host, port);
        }

        @Override
        public void checkAwtEventQueueAccess() {
            if (isInnerSet())
                inner.checkAwtEventQueueAccess();
            else
                super.checkAwtEventQueueAccess();
        }

        @Override
        public void checkAccess(ThreadGroup g) {
            if (isInnerSet())
                inner.checkAccess(g);
            else
                super.checkAccess(g);
        }

        @Override
        public void checkAccess(Thread t) {
            if (isInnerSet())
                inner.checkAccess(t);
            else
                super.checkAccess(t);
        }

        @Override
        public void checkAccept(String host, int port) {
            if (isInnerSet())
                inner.checkAccept(host, port);
            else
                super.checkAccept(host, port);
        }
    }
}
