/*
 * Copyright (c) 2010-2012, University of Sussex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of the University of Sussex nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.susx.mlcl.byblo.measures.impl;

import static uk.ac.susx.mlcl.TestConstants.TEST_FRUIT_EVENTS;
import static uk.ac.susx.mlcl.TestConstants.TEST_FRUIT_FEATURES;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.susx.mlcl.TestConstants;
import uk.ac.susx.mlcl.byblo.Tools;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratingDelegates;
import uk.ac.susx.mlcl.byblo.io.BybloIO;
import uk.ac.susx.mlcl.byblo.io.FastWeightedTokenPairVectorSource;
import uk.ac.susx.mlcl.byblo.measures.AutoWeightingMeasure;
import uk.ac.susx.mlcl.byblo.measures.DecomposableMeasure;
import uk.ac.susx.mlcl.byblo.measures.ForwardingMeasure;
import uk.ac.susx.mlcl.byblo.measures.Measure;
import uk.ac.susx.mlcl.byblo.weighings.FeatureMarginalsCarrier;
import uk.ac.susx.mlcl.byblo.weighings.MarginalDistribution;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.byblo.weighings.impl.NullWeighting;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import uk.ac.susx.mlcl.lib.test.ExitTrapper;
import uk.ac.susx.mlcl.testing.AbstractObjectTest;

/**
 * 
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class AbstractMeasureTest<T extends Measure> extends AbstractObjectTest<T> {

	static final double EPSILON = 1E-10;

	private static List<Indexed<SparseDoubleVector>> FRUIT_EVENTS;

	private static MarginalDistribution FRUIT_FEATURE_MARGINALS;

    @BeforeClass
	public static void setUpClass() throws Exception {

		final DoubleEnumerating indexDelegate = new DoubleEnumeratingDelegate();

		// Load events
		final FastWeightedTokenPairVectorSource eventSrc = BybloIO
				.openEventsVectorSource(TEST_FRUIT_EVENTS,
						TestConstants.DEFAULT_CHARSET, indexDelegate);
		FRUIT_EVENTS = new ArrayList<Indexed<SparseDoubleVector>>();
		int card = 0;
		while (eventSrc.hasNext()) {
			Indexed<SparseDoubleVector> v = eventSrc.read();
			FRUIT_EVENTS.add(v);
			card = Math.max(card, v.value().cardinality);
		}

		// Add a completely empty feature vector to test that works
		FRUIT_EVENTS.add(new Indexed<SparseDoubleVector>(Integer.MAX_VALUE,
				new SparseDoubleVector(card, 0)));

		eventSrc.close();

		for (Indexed<SparseDoubleVector> v : FRUIT_EVENTS) {
			v.value().cardinality = card;
		}

		FRUIT_FEATURE_MARGINALS = BybloIO.readFeaturesMarginalDistribution(
				TEST_FRUIT_FEATURES, TestConstants.DEFAULT_CHARSET,
				EnumeratingDelegates.toSingleFeatures(indexDelegate));

	}


	Measure newConfiguredInstanceFor(SparseDoubleVector... data) {
		return newConfiguredInstanceFor(Arrays.asList(data));

	}

	Measure newConfiguredInstanceFor(List<SparseDoubleVector> data) {
		try {
			Measure measure = newInstance();
			MarginalDistribution fmd = null;
			// Inject feature marginals in the measure where necessary
			Measure m = measure;
			do {
				if (m instanceof FeatureMarginalsCarrier) {
					if (fmd == null)
						fmd = featureMarginals(data);
					((FeatureMarginalsCarrier) m).setFeatureMarginals(fmd);
				}
				if (m instanceof ForwardingMeasure<?>)
					m = ((ForwardingMeasure<?>) m).getDelegate();
				else
					m = null;
			} while (m != null);

			// First we need to wrap the measure so vectors are weighted
			// properly.
			if (measure.getExpectedWeighting() != NullWeighting.class) {
				Weighting weighting = measure.getExpectedWeighting()
						.newInstance();
				if (weighting instanceof FeatureMarginalsCarrier) {
					if (fmd == null)
						fmd = featureMarginals(data);
					((FeatureMarginalsCarrier) weighting)
							.setFeatureMarginals(fmd);
				}
				measure = new AutoWeightingMeasure(measure, weighting);
			}

			return measure;
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	protected abstract String getMeasureName();

	//
	// ============================================================================
	// Tests
	// ============================================================================
	//

	/**
	 * Measures are generally instantiated through reflect. They require a
	 * publicly accessible default (no argument) constructor.
	 */
	@Test
	public void testDefaultConstructor() {
		System.out.println("testDefaultConstructor()");

		try {
			Class<? extends T> clazz = getImplementation();
			Assert.assertFalse("Measure is abstract",
					Modifier.isAbstract(clazz.getModifiers()));
			Assert.assertFalse("Measure is an interface",
					Modifier.isInterface(clazz.getModifiers()));

			Constructor<? extends T> constructor = clazz.getConstructor();
			Assert.assertTrue("Default constructor is not public",
					Modifier.isPublic(constructor.getModifiers()));

			final T instance = constructor.newInstance();
			Assert.assertNotNull("instance", instance);

		} catch (SecurityException e) {
			throw new AssertionError(e);
		} catch (NoSuchMethodException e) {
			throw new AssertionError(e);
		} catch (InvocationTargetException e) {
			throw new AssertionError(e);
		} catch (InstantiationException e) {
			throw new AssertionError(e);
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}

	@Test
	public void testGetExpectedWeighting() {
		System.out.println("testGetExpectedWeighting()");
		final T instance = newInstance();

		Class<? extends Weighting> wgtClass = instance.getExpectedWeighting();
		Assert.assertNotNull(wgtClass);

		try {
			Weighting wgt = wgtClass.newInstance();
		} catch (InstantiationException e) {
			throw new AssertionError(e);
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}

	@Test
	public void testGetBounds() {
		System.out.println("testGetBounds()");
		final T instance = newInstance();

		final double hetBound = instance.getHeterogeneityBound();
		final double homBound = instance.getHomogeneityBound();

		Assert.assertTrue(!Double.isNaN(hetBound));
		Assert.assertTrue(!Double.isNaN(homBound));
		Assert.assertTrue(hetBound != homBound);
	}

	@Test
	public void testIsCommutative() {
		System.out.println("testIsCommutative()");
		final T instance = newInstance();
		final boolean isCom = instance.isCommutative();

		if (isCom) {
			int size = 100;
			Random RANDOM = newRandom();
			SparseDoubleVector A = randomVector(RANDOM, size);
			SparseDoubleVector B = randomVector(RANDOM, size);

			double expect = instance.similarity(A, B);
			double actual = instance.similarity(B, A);
			Assert.assertEquals(expect, actual, EPSILON);
		}
	}

	@Test
	@Ignore
	public void testRunFromCommandLine() throws Exception {
		System.out.println("testRunFromCommandLine");

		runFromCommandLine();
	}

	void runFromCommandLine(String... extraArgs) throws Exception {
		StringBuilder name = new StringBuilder();
		name.append(getMeasureName());
		for (String arg : extraArgs)
			name.append(arg);

		File output = new File(TestConstants.TEST_OUTPUT_DIR,
				TestConstants.FRUIT_NAME + "-" + name.toString());
		TestConstants.deleteIfExist(output);

		try {
			ExitTrapper.enableExistTrapping();

			String[] args = new String[] { "allpairs", "--charset", "UTF-8",
					"--measure", getMeasureName(), "--input",
					TEST_FRUIT_EVENTS.toString(), "--input-features",
					TEST_FRUIT_FEATURES.toString(), "--input-entries",
					TestConstants.TEST_FRUIT_ENTRIES.toString(), "--output",
					output.toString() };

			args = cat(args, extraArgs);

			Tools.main(args);

		} finally {
			ExitTrapper.disableExitTrapping();
		}

		Assert.assertTrue("Output file " + output + " does not exist.",
				output.exists());
		Assert.assertTrue("Output file " + output + " is empty.",
				output.length() > 0);
	}

	@Test
	public void testBothEmptyVectors() {
		System.out.println("testBothEmptyVectors");
		int size = 100;
		SparseDoubleVector A = new SparseDoubleVector(size, 0);
		SparseDoubleVector B = new SparseDoubleVector(size, 0);
		double expect = 0;
		double actual = similarity(newInstance(), A, B);

		Assert.assertEquals(expect, actual, EPSILON);
	}

	@Test
	public void testOneEmptyVector() {
		System.out.println("testOneEmptyVector");
		int size = 100;
		Random RANDOM = newRandom();
		SparseDoubleVector A = new SparseDoubleVector(size, 0);
		SparseDoubleVector B = new SparseDoubleVector(size, size);
		for (int i = 0; i < size; i++)
			B.set(i, RANDOM.nextDouble());

		double expect = 0;
		double actual = similarity(newInstance(), A, B);

		Assert.assertEquals(expect, actual, EPSILON);
	}

	@Test
	public void testSizeOneVectors() {
		System.out.println("testSizeOneVectors");
		int size = 100;
		SparseDoubleVector A = new SparseDoubleVector(size, 1);
		SparseDoubleVector B = new SparseDoubleVector(size, 1);
		A.set(0, 1);
		B.set(0, 1);
		double expect = newInstance().getHomogeneityBound();
		double actual = similarity(newInstance(), A, B);

		Assert.assertEquals(expect, actual, EPSILON);
	}

	@Test
	public void testCardinalityOneVectors() {
		System.out.println("testCardinalityOneVectors");
		SparseDoubleVector A = new SparseDoubleVector(1, 1);
		SparseDoubleVector B = new SparseDoubleVector(1, 1);
		A.set(0, 1);
		B.set(0, 1);
		Measure instance = newInstance();
		double expect = instance.getHomogeneityBound();
		double actual = similarity(instance, A, B);

		Assert.assertEquals(expect, actual, EPSILON);
	}

	@Test
	public void testSizeTwoVectors() {
		System.out.println("testSizeTwoVectors");
		int size = 100;
		SparseDoubleVector A = new SparseDoubleVector(size, 2);
		SparseDoubleVector B = new SparseDoubleVector(size, 2);
		A.set(0, 1);
		A.set(1, 1);
		B.set(0, 1);
		B.set(1, 1);
		double expect = newInstance().getHomogeneityBound();
		double actual = similarity(newInstance(), A, B);

		Assert.assertEquals(expect, actual, EPSILON);
	}

	//

	@Test
	public void testHomogeneity() {
		int size = 1000;
		int count = 100;
		SparseDoubleVector vec = randomVector(newRandom(), size);
		SparseDoubleVector another = randomVector(newRandom(), size);
		Measure instance = newConfiguredInstanceFor(vec, another);
		double expect = newInstance().getHomogeneityBound();
		double actual = similarity(instance, vec, vec);

		Assert.assertEquals(expect, actual, 0.01);
	}

	@Test
	public void testHeterogeneity() {
		int size = 1000;
		SparseDoubleVector A = ones(size, size);
		SparseDoubleVector B = ones(size, size);

		for (int i = 0; i < size; i++) {
			if (i % 2 == 0)
				A.set(i, 0);
			else
				B.set(i, 0);
		}

		Measure instance = newConfiguredInstanceFor(A, B);
		double expect = newInstance().getHeterogeneityBound();
		double actual = similarity(instance, A, B);

		Assert.assertEquals(expect, actual, 0.01);
	}

	@Test
	public void testHomogeneity2() {
		int size = 1000;
		int count = 100;
		List<SparseDoubleVector> vectors = randomVectors(newRandom(), size, count);
		Measure instance = newConfiguredInstanceFor(vectors);
		double expect = newInstance().getHomogeneityBound();
		double actual = similarity(instance, vectors.get(0), vectors.get(0));

		Assert.assertEquals(expect, actual, 0.01);
	}

	private static List<SparseDoubleVector> randomVectors(Random RANDOM, int size,
                                                          int count) {
		List<SparseDoubleVector> vectors = new ArrayList<SparseDoubleVector>();
		for (int i = 0; i < count; i++) {
			vectors.add(randomVector(RANDOM, size));
		}
		return vectors;
	}

	static boolean isFeatureMarginalsRequire(Measure measure) {
		return measure instanceof FeatureMarginalsCarrier
				|| (measure instanceof ForwardingMeasure<?> && ((ForwardingMeasure<?>) measure)
						.getDelegate() instanceof FeatureMarginalsCarrier)
				|| (measure.getExpectedWeighting()
						.isInstance(FeatureMarginalsCarrier.class));
	}

	@Test
	public void testFruitData() throws IOException {
		System.out.println("testFruitData");
		int limit = 5;

		List<SparseDoubleVector> vectors = TestConstants.loadFruitVectors();

		limit = Math.min(limit, vectors.size());

		Measure instance = newInstance();
		if (instance instanceof FeatureMarginalsCarrier)
			((FeatureMarginalsCarrier) instance)
					.setFeatureMarginals(featureMarginals(vectors));

		final double[][] results = new double[limit][limit];
		for (int i = 0; i < limit; i++) {
			for (int j = 0; j < limit; j++) {
				SparseDoubleVector A = vectors.get(i);
				SparseDoubleVector B = vectors.get(j);
				results[i][j] = similarity(instance, A, B);
			}
		}

		if (instance.isCommutative()) {
			// triangular mirrors should be equal
			for (int i = 0; i < limit; i++) {
				for (int j = 0; j < limit; j++) {
					Assert.assertEquals(results[i][j], results[j][i], EPSILON);
				}
			}
		}
	}

	@Test
	public void testFruitIdentity() throws IOException {
		System.out.println("testFruitIdentity");
		int limit = 5;

		List<SparseDoubleVector> vectors = TestConstants.loadFruitVectors();

		limit = Math.min(limit, vectors.size());

		T instance = newInstance();
		for (SparseDoubleVector vec : vectors) {
			final double result = similarity(instance, vec, vec);
			Assert.assertEquals(newInstance().getHomogeneityBound(), result,
					EPSILON);
		}
	}

	@Test
	public void testLargeCardinality() {
		System.out.println("testLargeCardinality");
		final int size = 100;
		final SparseDoubleVector A = new SparseDoubleVector(Integer.MAX_VALUE,
				size);
		final SparseDoubleVector B = new SparseDoubleVector(Integer.MAX_VALUE,
				size);
		Random RANDOM = newRandom();
		for (int i = 0; i < size; i++) {
			A.set(RANDOM.nextInt(size * 2), RANDOM.nextDouble());
			B.set(RANDOM.nextInt(size * 2), RANDOM.nextDouble());
		}

		similarity(newInstance(), A, B);
	}

	@Test
	public void testDecomposableMeasure() {
		System.out.println("testDecomposableMeasure()");

		// Skip test if the measure isn't decomposable
		Assume.assumeTrue(getImplementation().isInstance(
                DecomposableMeasure.class));

		final DecomposableMeasure instance = (DecomposableMeasure) newInstance();

		int size = 100;
		Random RANDOM = newRandom();
		SparseDoubleVector A = randomVector(RANDOM, size);
		SparseDoubleVector B = randomVector(RANDOM, size);

		final double sim = instance.similarity(A, B);

		final double left = instance.left(A);
		Assert.assertTrue(!Double.isNaN(left));
		Assert.assertTrue(!Double.isInfinite(left));

		final double right = instance.right(B);
		Assert.assertTrue(!Double.isNaN(right));
		Assert.assertTrue(!Double.isInfinite(right));

		if (instance.isCommutative()) {
			Assert.assertEquals(left, right, EPSILON);
		}

		final double shared = instance.shared(A, B);
		Assert.assertTrue(!Double.isNaN(shared));
		Assert.assertTrue(!Double.isInfinite(shared));

		final double combined = instance.combine(shared, left, right);
		Assert.assertTrue(!Double.isNaN(combined));
		Assert.assertTrue(!Double.isInfinite(combined));

		Assert.assertEquals(sim, combined, EPSILON);
	}

	//
	// ============================================================================
	// Static utility methods
	// ============================================================================
	//

	static double similarity(Measure instance, SparseDoubleVector A,
			SparseDoubleVector B) {

		final double val = instance.similarity(A, B);

		Assert.assertFalse("Similarity is NaN with measure " + instance,
				Double.isNaN(val));

		Assert.assertFalse("Non-finite similarity found (" + val
				+ ") with measure " + instance, Double.isInfinite(val));

		final double minSim, maxSim;
		if (instance.getHeterogeneityBound() < instance.getHomogeneityBound()) {
			minSim = instance.getHeterogeneityBound();
			maxSim = instance.getHomogeneityBound();
		} else {
			minSim = instance.getHomogeneityBound();
			maxSim = instance.getHeterogeneityBound();
		}
		Assert.assertTrue("expected similarity >= " + minSim + " but found "
				+ val, val >= minSim);
		Assert.assertTrue("expected similarity <= " + maxSim + " but found "
				+ val, val <= maxSim);

		if (instance.isCommutative()) {
			final double rev = instance.similarity(B, A);
			Assert.assertEquals(
					"Measure is declared commutative, but reversing "
							+ "operands results in a different score.", rev,
					val, EPSILON);
		}

		return val;
	}

	private static SparseDoubleVector randomVector(Random rand, int card) {
		final SparseDoubleVector vector = new SparseDoubleVector(card, card);
		for (int i = 0; i < card; i++) {
			if (rand.nextDouble() > 0.75)
				vector.set(i, rand.nextInt(Integer.MAX_VALUE));
		}
		return vector;
	}



	private static SparseDoubleVector ones(int cardinality, int size) {
		SparseDoubleVector vec = new SparseDoubleVector(cardinality, size);
		for (int i = 0; i < size; i++)
			vec.set(i, 1);
		return vec;
	}

	static SparseDoubleVector zeros(int cardinality) {
		return new SparseDoubleVector(cardinality, 0);
	}

	static MarginalDistribution featureMarginals(SparseDoubleVector... vectors) {
		return featureMarginals(Arrays.asList(vectors));
	}

	private static MarginalDistribution featureMarginals(List<SparseDoubleVector> vectors) {
		double[] marginals = new double[vectors.get(0).cardinality];
		int nonZeroCardinality = 0;
		double total = 0;
		for (SparseDoubleVector vec : vectors) {
			for (int i = 0; i < vec.keys.length; i++) {
				if (marginals[vec.keys[i]] == 0)
					++nonZeroCardinality;
				marginals[vec.keys[i]] += vec.values[i];
			}
			total += vec.sum;
		}
		return new MarginalDistribution(marginals, total, nonZeroCardinality);
	}

}
