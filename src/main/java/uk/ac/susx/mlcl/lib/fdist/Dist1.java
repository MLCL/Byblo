/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.fdist;

/**
 *
 * @author hiam20
 */
public interface Dist1 extends Dist0 {

    double getAbsentFreq(int varId);

    double getAbsentProb(int varId);

    double getPresentFreq(int varId);

    double getPresentProb(int varId);

    boolean isAbsent(int varId);

    boolean isPresent(int varId);

}
