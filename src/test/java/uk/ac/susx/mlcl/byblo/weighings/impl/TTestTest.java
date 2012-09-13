package uk.ac.susx.mlcl.byblo.weighings.impl;

/**
 * Created with IntelliJ IDEA.
 * User: hamish
 * Date: 13/09/2012
 * Time: 15:38
 * To change this template use File | Settings | File Templates.
 */
public class TTestTest extends AbstractContextualWeightingTest<TTest> {
    @Override
    protected String getWeightingName() {
        return "ttest";
    }

    @Override
    protected Class<? extends TTest> getImplementation() {
        return TTest.class;
    }
}
