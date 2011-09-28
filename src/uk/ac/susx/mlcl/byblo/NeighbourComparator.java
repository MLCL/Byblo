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

import java.io.Serializable;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A string comparator that takes strings composed of three tab delimited 
 * fields. The first two are strings and the third is a double-precision 
 * floating point number. The comparator orders by first field ASCII order 
 * ascending, then by third field numeric order descending.
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
public class NeighbourComparator implements Comparator<String>, Serializable {

    private static final Logger LOG = Logger.getLogger(NeighbourComparator.class.
            getName());

    public static final long serialVersionUID = 1L;

    public NeighbourComparator() {
    }

    @Override
    public int compare(String string1, String string2) {
        try {

            final int n = string1.length();
            final int m = string2.length();
            int i = 0;
            int j = 0;
            while (i < n && j < m) {
                char c1 = string1.charAt(i);
                char c2 = string2.charAt(j);


                if (c1 != c2) {
                    return c1 - c2;
                }
                if (c1 == '\t' || c2 == '\t')
                    break;
                i++;
                j++;
            }


            // find the ends of the Entries
            while (i < n && string1.charAt(i) != '\t') {
                i++;
            }
            while (j < m && string2.charAt(j) != '\t') {
                j++;
            }

            i++;
            j++;

            // find the ends of the nieghbour words
            while (i < n && string1.charAt(i) != '\t') {
                i++;
            }
            while (j < m && string2.charAt(j) != '\t') {
                j++;
            }
            i++;
            j++;

            double num1 = Double.parseDouble(string1.substring(i));
            double num2 = Double.parseDouble(string2.substring(j));

            return -Double.compare(num1, num2);
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE,
                    "Caught exception when attempting to compare "
                    + "strings \"" + string1 + "\" and \"" + string2 + "\": " + ex,
                    ex);
            throw ex;
        }
    }
}
