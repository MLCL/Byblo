/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.fdist;

/**
 *
 * @author hiam20
 */
public interface DiscreteUnivariateDistribution extends Distribution {

    int getNumValues();

    double getProbability(int valueId);

    double getExpectation(int valueId);

}
