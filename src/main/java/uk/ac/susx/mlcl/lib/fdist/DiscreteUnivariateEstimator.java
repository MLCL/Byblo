/*
 * Copyright (c) 2012 University of Sussex
 */
package uk.ac.susx.mlcl.lib.fdist;

import uk.ac.susx.mlcl.lib.collect.ArrayMath;

/**
 * @author Hamish Morgan
 */
public class DiscreteUnivariateEstimator {

    private long[] r;

    private long rTotal;

    private int[] Nr;

    private int NrTotal;

    public DiscreteUnivariateEstimator() {
        r = new long[0];
        rTotal = 0;
        Nr = new int[0];
        NrTotal = 0;
    }

    public long getFrequency(int valueId) {
        assert valueId >= 0;

        return r.length > valueId ? r[valueId] : 0;
    }

    public void setFrequency(int valueId, long newFrequency) {
        assert valueId >= 0;
        assert newFrequency >= 0;


        final long oldFrequency = r[valueId];
        final long delta = newFrequency - oldFrequency;


        rTotal += delta;

        if (Nr != null) {
            assert oldFrequency < Integer.MAX_VALUE;
            assert newFrequency < Integer.MAX_VALUE;

            if (r[valueId] > 0)
                Nr[(int) oldFrequency] -= 1;
            if ((r[valueId] + delta) > 0)
                Nr[(int) newFrequency] += 1;
        }

        r[valueId] += delta;
    }

    public DiscreteUnivariateDistribution getDistribution() {
        return new DiscreteUnivariateDistribution() {

            @Override
            public int getNumValues() {
                return NrTotal;
            }

            @Override
            public double getProbability(int valueId) {
                return (double) r[valueId] / (double) rTotal;
            }

            @Override
            public double getExpectation(int valueId) {
                return r[valueId];
            }

            @Override
            public double getTotal() {
                return rTotal;
            }

        };
    }

    public DiscreteUnivariateDistribution getHistogram() {
        return new DiscreteUnivariateDistribution() {

            @Override
            public int getNumValues() {
                return Nr.length;
            }

            @Override
            public double getProbability(int valueId) {
                return (double) Nr[valueId] / (double) NrTotal;
            }

            @Override
            public double getExpectation(int valueId) {
                return Nr[valueId];
            }

            @Override
            public double getTotal() {
                return NrTotal;
            }

        };
    }

}
