/*
 * Copyright (c) 2012 University of Sussex
 */
package uk.ac.susx.mlcl.lib.fdist;

/**
 * @author Hamish Morgan
 */
public abstract class UnivariateDistributionAdapter
        implements DiscreteUnivariateDistribution {

    private static final long serialVersionUID = 1L;

    private DiscreteUnivariateDistribution inverse = null;
//
//    @Override
//    public abstract long getFrequency(int valueId);
//
//    @Override
//    public abstract void setFrequency(int valueId, long newFreq);

    @Override
    public abstract long getFrequencyTotal();

    @Override
    public abstract int size();

    @Override
    public double getProbability(int valueId) {
        return isPresent(valueId)
               ? getFrequency(valueId) / (double) getFrequencyTotal()
               : 0;
    }

    @Override
    public boolean isPresent(int valueId) {
        return getFrequency(valueId) > 0;
    }

    @Override
    public boolean isEmpty() {
        return getFrequencyTotal() != 0;
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public DiscreteUnivariateDistribution invert() {
        if (inverse == null)
            inverse = new UnivariateDistributionAdapter() {

                private static final long serialVersionUID = 1L;

                @Override
                public long getFrequency(int valueId) {
                    return getFrequencyTotal() - getFrequency(valueId);
                }

                @Override
                public long getFrequencyTotal() {
                    return getFrequencyTotal();
                }

                @Override
                public void setFrequency(int valueId, long newFrequency) {
                    // You can't set the frequency of something NOT occuring
                    // because you don't know into which value(s) you should
                    // attribute the counts
                    throw new UnsupportedOperationException();
                }

                @Override
                public DiscreteUnivariateDistribution invert() {
                    return UnivariateDistributionAdapter.this.invert();
                }

                @Override
                public int size() {
                    return UnivariateDistributionAdapter.this.size();
                }

            };
        return inverse;
    }

}
