/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.fdist;

/**
 *
 * @author hiam20
 */
public interface MultivariateDistribution extends Distribution {

    MultivariateDistribution invert(int variableId);

    MultivariateDistribution marginalise(int variableId);

    MultivariateDistribution conditionOn(int variableId);

    long getFrequency(int[] variableIds);

    double getProbability(int[] variableIds);

    boolean isPresent(int[] variableIds);

    int size(int variabeleId);

    MultivariateDistribution histogram();

}
