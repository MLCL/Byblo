/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.fdist;

/**
 *
 * @author hiam20
 */
public interface BivariateDistribution extends Distribution {

    double getExpectedValue(int x, int y);
//
//    long getFrequency(int x, int y);
//
//    /**
//     *
//     * Some implementations may not support mutation, in which case they will
//     * throw UnsupportedOperationException
//     *
//     * @param x
//     * @param y
//     * @param newFrequency
//     * @throws UnsupportedOperationException implementation does not support
//     * mutation
//     */
//    void setFrequency(int x, int y, long newFrequency)
//            throws UnsupportedOperationException;

    double getProbability(int x, int y);

    boolean isPresent(int x, int y);

    BivariateDistribution invertX();

    BivariateDistribution invertY();

    DiscreteUnivariateDistribution marginaliseOverX();

    DiscreteUnivariateDistribution marginaliseOverY();

    DiscreteUnivariateDistribution conditionOnY(int y);

    DiscreteUnivariateDistribution conditionOnX(int x);

}
