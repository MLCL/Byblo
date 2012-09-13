package uk.ac.susx.mlcl.byblo.weighings.impl;

/**
 * Created with IntelliJ IDEA.
 * User: hamish
 * Date: 13/09/2012
 * Time: 15:38
 * To change this template use File | Settings | File Templates.
 */
public class L2UnitVectorTest extends AbstractWeightingTest<L2UnitVector> {
    @Override
    protected String getWeightingName() {
        return "l2";
    }

    @Override
    protected Class<? extends L2UnitVector> getImplementation() {
        return L2UnitVector.class;
    }
}
