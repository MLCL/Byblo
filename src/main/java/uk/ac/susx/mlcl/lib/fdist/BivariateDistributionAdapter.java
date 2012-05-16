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
 *
 * @author hiam20
 */
public abstract class BivariateDistributionAdapter implements BivariateDistribution {

    @Override
    public abstract long getFrequency(int x, int y);

    @Override
    public abstract void setFrequency(int x, int y, long newFrequency);

    @Override
    public abstract DiscreteUnivariateDistribution marginaliseOverX();

    @Override
    public abstract DiscreteUnivariateDistribution marginaliseOverY();

    @Override
    public abstract long getFrequencyTotal();

    @Override
    public double getProbability(int x, int y) {
        return (double) getFrequency(x, y) / (double) getFrequencyTotal();
    }

    @Override
    public boolean isPresent(int x, int y) {
        return getFrequency(x, y) > 0;
    }

    @Override
    public boolean isEmpty() {
        return getFrequencyTotal() > 0;
    }

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public DiscreteUnivariateDistribution conditionOnY(final int y) {
        return new UnivariateDistributionAdapter() {

            @Override
            public long getFrequency(int x) {
                return BivariateDistributionAdapter.this.getFrequency(x, y);
            }

            @Override
            public void setFrequency(int x, long newFreq) {
                BivariateDistributionAdapter.this.setFrequency(x, y, newFreq);
            }

            @Override
            public long getFrequencyTotal() {
                return marginaliseOverX().getFrequency(y);
            }

            @Override
            public int size() {
                return marginaliseOverX().size();
            }

        };
    }

    @Override
    public DiscreteUnivariateDistribution conditionOnX(final int x) {
        return new UnivariateDistributionAdapter() {

            @Override
            public long getFrequency(int y) {
                return BivariateDistributionAdapter.this.getFrequency(x, y);
            }

            @Override
            public void setFrequency(int y, long newFreq) {
                BivariateDistributionAdapter.this.setFrequency(x, y, newFreq);
            }

            @Override
            public long getFrequencyTotal() {
                return marginaliseOverY().getFrequency(x);
            }

            @Override
            public int size() {
                return marginaliseOverY().size();
            }

        };
    }

    @Override
    public BivariateDistribution invertX() {
        return new BivariateDistributionAdapter() {

            @Override
            public long getFrequency(int x, int y) {
                return BivariateDistributionAdapter.this.marginaliseOverX().getFrequency(y)
                        - BivariateDistributionAdapter.this.getFrequency(x, y);
            }

            @Override
            public void setFrequency(int x, int y, long newFrequency) {
                throw new UnsupportedOperationException();
            }

            @Override
            public DiscreteUnivariateDistribution marginaliseOverX() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public DiscreteUnivariateDistribution marginaliseOverY() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public long getFrequencyTotal() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        };
    }

    @Override
    public BivariateDistribution invertY() {
        return new BivariateDistributionAdapter() {

            @Override
            public long getFrequency(int x, int y) {
                return BivariateDistributionAdapter.this.marginaliseOverY().getFrequency(x)
                        - BivariateDistributionAdapter.this.getFrequency(x, y);
            }

            @Override
            public void setFrequency(int x, int y, long newFrequency) {
                throw new UnsupportedOperationException();
            }

            @Override
            public DiscreteUnivariateDistribution marginaliseOverX() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public DiscreteUnivariateDistribution marginaliseOverY() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public long getFrequencyTotal() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        };
    }

}
