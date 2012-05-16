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
package uk.ac.susx.mlcl.lib.fdist;

/**
 * Bivariate joint distribution of two empirically estimated, dependent
 * random variables.
 *
 * @author hiam20
 */
public class BivariateDistributionImpl extends BivariateDistributionAdapter {

    private long[][] frequency;

    private DiscreteUnivariateDistribution xMarginals;

    private DiscreteUnivariateDistribution yMarginals;

    private long frequencyTotal;

    private BivariateDistributionImpl(
            long[][] frequency,
            DiscreteUnivariateDistribution xMarginals,
            DiscreteUnivariateDistribution yMarginals,
            long frequencyTotal) {
        this.frequency = frequency;
        this.xMarginals = xMarginals;
        this.yMarginals = yMarginals;
        this.frequencyTotal = frequencyTotal;
    }

    @Override
    public long getFrequency(int x, int y) {
        assert x >= 0 && y >= 0;

        return frequency.length > x && frequency[x].length > y
               ? frequency[x][y]
               : 0;
    }

    @Override
    public void setFrequency(int x, int y, long newFrequency) {
        assert x >= 0 && y >= 0;
        assert newFrequency >= 0;

        xMarginals.setFrequency(y, xMarginals.getFrequency(y)
                - frequency[x][y] + newFrequency);

        yMarginals.setFrequency(x, yMarginals.getFrequency(x)
                - frequency[x][y] + newFrequency);

        frequencyTotal += -frequency[x][y] + newFrequency;

        frequency[x][y] = newFrequency;
    }

    @Override
    public DiscreteUnivariateDistribution marginaliseOverX() {
        return xMarginals;
    }

    @Override
    public DiscreteUnivariateDistribution marginaliseOverY() {
        return yMarginals;
    }

    @Override
    public long getFrequencyTotal() {
        return frequencyTotal;
    }

}
