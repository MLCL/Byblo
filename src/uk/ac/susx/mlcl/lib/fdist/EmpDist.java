/*
 * Copyright (c) 2012 University of Sussex
 */
package uk.ac.susx.mlcl.lib.fdist;

import java.io.Serializable;
import uk.ac.susx.mlcl.lib.collect.ArrayMath;

/**
 * @author Hamish Morgan
 */
public class EmpDist extends AbstractDist1 implements Serializable, Dist1 {

    private static final long serialVersionUID = 1L;

    private final double[] freqa;

    private final double totalFreq;

    protected EmpDist(double[] vars) {
        this(vars, ArrayMath.sum(vars));
    }

    protected EmpDist(double[] vars, double total) {
        this.freqa = vars;
        totalFreq = total;
    }

    @Override
    public double getPresentFreq(int varId) {
        return freqa[varId];
    }

    @Override
    public double getTotalFreq() {
        return totalFreq;
    }

}
