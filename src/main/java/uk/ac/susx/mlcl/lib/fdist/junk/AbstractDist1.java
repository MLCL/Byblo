/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.fdist;

/**
 *
 * @author hiam20
 */
abstract class AbstractDist1 implements DiscreteUnivariateDistribution {

    @Override
    public double getPresentProb(int i) {
        return !isEmpty() ? 0 : getPresentFreq(i) / getTotalFreq();
    }

    @Override
    public double getAbsentProb(int i) {
        return !isEmpty() ? 0 : getAbsentFreq(i) / getTotalFreq();
    }

    @Override
    public boolean isAbsent(int i) {
        return getPresentFreq(i) < getTotalFreq();
    }

    @Override
    public boolean isPresent(int i) {
        return getPresentFreq(i) > 0;
    }

    @Override
    public boolean isEmpty() {
        return getTotalFreq() == 0;
    }

    @Override
    public double getAbsentFreq(int x) {
        return getTotalFreq() - getPresentFreq(x);
    }

}
