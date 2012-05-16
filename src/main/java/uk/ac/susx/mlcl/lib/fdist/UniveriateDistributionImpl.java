/*
 * Copyright (c) 2012 University of Sussex
 */
package uk.ac.susx.mlcl.lib.fdist;

import java.io.Serializable;
import java.util.Arrays;
import uk.ac.susx.mlcl.lib.collect.ArrayMath;

/**
 * @author Hamish Morgan
 */
public class UniveriateDistributionImpl
        extends UnivariateDistributionAdapter
        implements Serializable, DiscreteUnivariateDistribution {

    private static final long serialVersionUID = 1L;

    private long[] frequency;

    private long frequencyTotal;

    private DiscreteUnivariateDistribution histogram = null;

    public UniveriateDistributionImpl() {
        this(new long[0]);
    }

    protected UniveriateDistributionImpl(long[] frequency) {
        this(frequency, ArrayMath.sum(frequency));
    }

    protected UniveriateDistributionImpl(long[] frequency, long frequencyTotal) {
        this.frequency = frequency;
        this.frequencyTotal = frequencyTotal;
    }

    @Override
    public long getFrequency(int valueId) {
        assert valueId >= 0;

        return frequency.length > valueId ? frequency[valueId] : 0;
    }

    @Override
    public void setFrequency(int valueId, long newFrequency) {
        assert valueId >= 0;
        assert newFrequency >= 0;

        // TODO: If newFrequency is zero then we may not need to grow

        insureCapacity(valueId);

        final long oldFrequency = frequency[valueId];
        final long delta = newFrequency - oldFrequency;


        frequencyTotal += delta;

        if (histogram != null) {
            assert oldFrequency < Integer.MAX_VALUE;
            assert newFrequency < Integer.MAX_VALUE;

            if (frequency[valueId] > 0)
                histogram.setFrequency(
                        (int) oldFrequency,
                        histogram.getFrequency((int) oldFrequency) - 1);
            if ((frequency[valueId] + delta) > 0)
                histogram.setFrequency(
                        (int) newFrequency,
                        histogram.getFrequency((int) newFrequency) + 1);
        }

        frequency[valueId] += delta;
    }

    @Override
    public long getFrequencyTotal() {
        return frequencyTotal;
    }

    @Override
    public int size() {
        return frequency.length;
    }

    private void insureCapacity(int valueId) {
        if (frequency.length <= valueId) {
            final int newCapacity = (int) (frequency.length * 1.5) + 1;
            frequency = Arrays.copyOf(frequency, newCapacity);
        }
    }

    public DiscreteUnivariateDistribution getFrequencyHistogram() {
        if (histogram == null) {
            UniveriateDistributionImpl hist = new UniveriateDistributionImpl();
            for (int i = 0; i < size(); i++) {
                assert this.getFrequency(i) < Integer.MAX_VALUE;
                hist.setFrequency(
                        (int) this.getFrequency(i),
                        hist.getFrequency((int) this.getFrequency(i)) + 1);
            }
            this.histogram = hist;
        }
        return histogram;
    }

}
