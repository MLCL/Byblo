package uk.ac.susx.mlcl.byblo.weighings.impl;

import junit.framework.Assert;
import org.junit.Test;
import uk.ac.susx.mlcl.byblo.weighings.AbstractContextualWeighting;
import uk.ac.susx.mlcl.byblo.weighings.MarginalDistribution;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * Created with IntelliJ IDEA.
 * User: hamish
 * Date: 13/09/2012
 * Time: 16:17
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractContextualWeightingTest<T extends AbstractContextualWeighting>
        extends AbstractWeightingTest<T> {


    @Test
    @Override
    public void testSizeOneVector() {

        final SparseDoubleVector vector = new SparseDoubleVector(1);
        final MarginalDistribution marginals = new MarginalDistribution(new double[]{1});

        vector.set(0, 1);
        final T instance = newInstance();
        instance.setFeatureMarginals(marginals);

        SparseDoubleVector reweightedVector = instance.apply(vector);

        Assert.assertNotNull("reweighted vector is null", reweightedVector);
        Assert.assertEquals("reweighted cardinality 1 vector should also be of cardinality 1", 1, reweightedVector.cardinality());

    }
}
