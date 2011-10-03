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
package uk.ac.susx.mlcl.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Static utility methods that don't fit anywhere else.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class MiscUtil {

    /**
     * SI unit multiples for Byte - increases by a factor of 10^3. Only required
     * up to max long 2^61
     */
    private static final String[] BYTE_UNITS_SUFFIX_SI = {
        "", "k", "M", "G", "T", "P"};

    /**
     * IEC unit multiples for Byte - increases by a factor of 2^10. Only required
     * up to max long 2^61
     */
    private static final String[] BYTE_UNITS_SUFFIX_IEC = {
        "", "Ki", "Mi", "Gi", "Ti", "Pi"};

    /**
     * Private constructor for static utility class.
     */
    private MiscUtil() {
    }

    /**
     * Format the provided integer parameter in units of bytes as a
     * human-readable string. For example 2048 produces become "2KiB". Uses
     * non-SI (1024 base) EIC units.
     *
     * @param bytes The number to make a human readable units string from.
     * @return human readable string representation of the number of bytes
     */
    public static String humanReadableBytes(long bytes) {
        return humanReadableBytes(bytes, false, true);
    }

    /**
     * Format the provided integer parameter in units of bytes as a
     * human-readable string. For example 2048 produces become "2KiB".
     * <p>
     * The output can be in either SI base units (multiples of 1000) or the
     * more traditional multiples of 1024 (2<sup>10</sup>). In the case that
     * multiples of 1024 are used, the correct IEC units infix an i character.
     * For example using KiB for kilobyte instead of kB.
     * </p>
     *
     * @param bytes The number to make a human readable units string from.
     * @param si    whether to not to use SI (1000) units, use 1024 otherwise
     * @param iec   whether or not use IEC units (e.g KiB)
     * @return human readable string representation of the number of bytes
     */
    public static String humanReadableBytes(long bytes, boolean si, boolean iec) {
        // Record and strip the sign
        final int signum = Long.signum(bytes);
        bytes *= Long.signum(bytes);

        // Calculate the power

        final double byteFrac;
        final String unit;

        if (si) {

            long i = 0;
            long lim = 1000L;
            while (i < BYTE_UNITS_SUFFIX_SI.length
                    && bytes >= lim) {
                lim *= 1000L;
                i++;
            }
            byteFrac = bytes / (double) (lim / 1000L);
            unit = BYTE_UNITS_SUFFIX_SI[(int) i];

        } else {

            long i = 0;
            final String[] units =
                    iec ? BYTE_UNITS_SUFFIX_IEC : BYTE_UNITS_SUFFIX_SI;
            while (i < units.length
                    && bytes >= 1L << 10L * (i + 1L)) {
                i++;
            }
            byteFrac = bytes / (double) (1L << (10L * i));
            unit = units[(int) i];
        }

        return String.format("%s%.1f%sB",
                signum == -1 ? "-" : "",
                byteFrac,
                unit);
    }

    /**
     * Returns the maximum amount of memory that the Java virtual machine will
     * attempt to use. If there is no inherent limit then the value
     * Long.MAX_VALUE will be returned.
     * <p/>
     * Note that this value has no bearing on the actual physical memory
     * availability of the underlying machine, only the upper limit on what the
     * Java Virtual Machine will attempt to allocate.
     *
     * @return the maximum amount of memory that the virtual machine will
     *      attempt to use, measured in bytes
     */
    public static long maxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * Returns the total amount of memory in the Java virtual machine. The
     * value returned by this method may vary over time, depending on the host
     * environment.
     *
     * @return total amount of memory currently available for current and
     *      future objects, measured in bytes.
     */
    public static long allocatedMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    /**
     * Returns the amount of free memory in the Java Virtual Machine. Calling
     * the {@link Runtime#gc()} method may result in increasing the value
     * returned by freeMemory.
     *
     * @return an approximation to the total amount of memory currently
     *      available for future allocated objects, measured in bytes.
     */
    public static long freeAllocatedMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    /**
     * Returns the amount of memory currently used by the Java Virtual Machine.
     * The value returned by this method may vary over time. Calling the
     * {@link Runtime#gc()} method may result in a decreasing the value return
     * by usedMemory.
     *
     * @return an approximation to the amount of memory currently used by
     *      allocated objects, measured in bytes.
     */
    public static long usedMemory() {
        return allocatedMemory() - freeAllocatedMemory();
    }

    /**
     * Returns the amount of memory available for use by the Java program. This
     * includes memory that has not yet been allocated to the Java Virtual
     * Machine.
     * <p />
     *
     * @return an approximation to the amount of memory that is available for
     *      use by the Java program.
     */
    public static long freeMaxMemory() {
        return maxMemory() - usedMemory();
    }

    /**
     * Return a human readable string representation of the current memory
     * allocation within the Java Virtual Machine.
     * <pre>
     * Memory[max=1015.7MiB, total=444.4MiB, used=272.7MiB, free=171.7MiB]
     * </pre>
     *
     * @return a string containing memory info.
     */
    public static String memoryInfoString() {
        final StringBuilder sb = new StringBuilder(72);
        sb.append("Memory[");
        sb.append(humanReadableBytes(usedMemory()));
        sb.append(" used, ");
        sb.append(humanReadableBytes(allocatedMemory()));
        sb.append(" allocated, ");
        sb.append(humanReadableBytes(maxMemory()));
        sb.append(" available");
        sb.append("]");
        return sb.toString();
    }

    /**
     * Print to stdout the results of calling {@link #memoryInfoString() }
     */
    public static void printMemoryInfo() {
        System.out.println(memoryInfoString());
    }

    private static final File[] paths = {new File("/bin"),
        new File("/usr/bin"),
        new File("/usr/local/bin"),
        new File("/opt/bin"),
        new File("/opt/local/bin")};

    public static String which(String command)
            throws IOException, InterruptedException {
        Process proc = Runtime.getRuntime().exec("which " + command);
        if (proc.waitFor() != 0) {

            System.out.println(System.getProperties());
//            if (new File("/usr/bin/truncate").exists()) {
//            }

            System.out.println(System.getProperty("java.ext.dirs"));
            return null;
        } else {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(proc.getInputStream()));
            String which = in.readLine();
            return which;
        }
    }
}
