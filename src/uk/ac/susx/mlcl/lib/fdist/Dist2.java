/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.fdist;

/**
 *
 * @author hiam20
 */
public interface Dist2 extends Dist0 {

    double getXAbsentWithYAbsentFreq(int x, int y);

    double getXAbsentWithYAbsentProb(int x, int y);

    double getXAbsentWithYPresentFreq(int x, int y);

    double getXAbsentWithYPresentProb(int x, int y);

    EmpDist getXTotalsDist();

    double getXPresentWithYAbsentFreq(int x, int y);

    double getXPresentWithYAbsentProb(int x, int y);

    double getXPresentWithYPresentFreq(int x, int y);

    double getXPresentWithYPresentProb(int x, int y);

    EmpDist getYTotalsDist();

    boolean isXAbsentWithYAbsent(int x, int y);

    boolean isXAbsentWithYPresent(int x, int y);

    boolean isXPresentWithYAbsent(int x, int y);

    boolean isXPresentWithYPresent(int x, int y);
    
    
}
