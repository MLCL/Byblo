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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratingDeligates;
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
import uk.ac.susx.mlcl.lib.collect.ArrayUtil;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import uk.ac.susx.mlcl.lib.test.ExitTrapper;

/**
 * 
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class AbstractMeasureTest<T extends Measure> {

	static final double EPSILON = 1E-10;

	static List<Indexed<SparseDoubleVector>> FRUIT_EVENTS;

	static MarginalDistribution FRUIT_FEATURE_MARGINALS;

	@BeforeClass
	public static void setUpClass() throws Exception {

		final DoubleEnumerating indexDeligate = new DoubleEnumeratingDeligate();

		// Load events
		final FastWeightedTokenPairVectorSource eventSrc = BybloIO
				.openEventsVectorSource(TEST_FRUIT_EVENTS,
						TestConstants.DEFAULT_CHARSET, indexDeligate);
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
				EnumeratingDeligates.toSingleFeatures(indexDeligate));

	}

	AbstractMeasureTest() {
	}

	T newInstance() {
		try {
			T instance = getMeasureClass().newInstance();
			return instance;
		} catch (InstantiationException e) {
			throw new AssertionError(e);
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		}
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

	abstract Class<? extends T> getMeasureClass();

	abstract String getMeasureName();

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
			Class<? extends T> clazz = getMeasureClass();
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

	/**
	 * It's not mandated that measures are Serializable, but if it's declared
	 * then it should work. So pass if Serializable isn't implemented, otherwise
	 * run the test.
	 */
	@Test
	public void testSerialization() {
		System.out.println("testSerialization()");
		final T instance = newInstance();

		// Skip if the class doens't implement Serializable.
		Assume.assumeTrue(instance instanceof Serializable);

		final T copy = cloneWithSerialization(instance);

		// objects must be equal but no the same instance
		Assert.assertEquals(instance, copy);
		Assert.assertTrue(instance != copy);
	}

	/**
	 * It's not mandated that measures are Cloneable, but if it's declared then
	 * it should work. So pass if Cloneable isn't implemented, otherwise run the
	 * test.
	 */
	@Test
	public void testClone() {
		System.out.println("testClone()");
		final T instance = newInstance();

		// Skip if the class doens't implement Cloneable.
		Assume.assumeTrue(instance instanceof Cloneable);

		final T copy = clone(instance);

		// objects must be equal but no the same instance
		Assert.assertEquals(instance, copy);
		Assert.assertTrue(instance != copy);
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

	public void runFromCommandLine(String... extraArgs) throws Exception {
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
	public void testHomoginiety() {
		System.out.println("testHomoginiety");
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
	public void testHeteroginiety() {
		System.out.println("testHeteroginiety");
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
	public void testHomoginiety2() {
		System.out.println("testHomoginiety");
		int size = 1000;
		int count = 100;
		List<SparseDoubleVector> vecs = randomVectors(newRandom(), size, count);
		Measure instance = newConfiguredInstanceFor(vecs);
		double expect = newInstance().getHomogeneityBound();
		double actual = similarity(instance, vecs.get(0), vecs.get(0));

		Assert.assertEquals(expect, actual, 0.01);
	}
//
//	@Test
//	public void testHeteroginiety2() {
//		System.out.println("testHeteroginiety");
//		int size = 1000;
//		int count = 100;
//		List<SparseDoubleVector> vecs = randomVectors(newRandom(), size, count);
//		Measure instance = newConfiguredInstanceFor(vecs);
//		double expect = newInstance().getHeterogeneityBound();
//		double actual = similarity(instance, vecs.get(0), vecs.get(1));
//
//		Assert.assertEquals(expect, actual, 0.01);
//
//		//
//		// int size = 100;
//		// SparseDoubleVector A = new SparseDoubleVector(size, size);
//		// SparseDoubleVector B = new SparseDoubleVector(size, size);
//		// for (int i = 0; i < size / 2; i++) {
//		// A.set(i * 2, i);
//		// B.set(i * 2 + 1, i);
//		// }
//		//
//		// double expect = newInstance().getHeterogeneityBound();
//		// double actual = similarity(newInstance(), A, B);
//		//
//		// Assert.assertEquals(expect, actual, EPSILON);
//	}

	// /**
	// * Independence should correspond roughly to to IID random vectors.
	// *
	// * @throws IllegalAccessException
	// * @throws InstantiationException
	// */
	// @Test
	// public void testIndependence() throws InstantiationException,
	// IllegalAccessException {
	// System.out.println("testIndependence");
	// int size = 1000;
	// Random RANDOM = newRandom();
	// List<SparseDoubleVector> vecs = randomVectors(RANDOM, size);
	// SparseDoubleVector A = vecs.get(0);
	// SparseDoubleVector B = vecs.get(1);
	//
	// Measure instance = newConfiguredInstanceFor(vecs);
	//
	// double expect = instance.getIndependenceBound();
	// double actual = similarity(instance, A, B);
	// Assert.assertEquals(expect, actual, 0.1);
	// }

	static List<SparseDoubleVector> randomVectors(Random RANDOM, int size,
			int count) {
		List<SparseDoubleVector> vecs = new ArrayList<SparseDoubleVector>();
		for (int i = 0; i < count; i++) {
			vecs.add(randomVector(RANDOM, size));
		}
		return vecs;
	}

	static boolean isFeatureMarginalsRequire(Measure measure) {
		return measure instanceof FeatureMarginalsCarrier
				|| (measure instanceof ForwardingMeasure<?> && ((ForwardingMeasure<?>) measure)
						.getDelegate() instanceof FeatureMarginalsCarrier)
				|| (measure.getExpectedWeighting()
						.isInstance(FeatureMarginalsCarrier.class));
	}

	static void setMarginals(Measure measure) {

	}

	@Test
	public void testFruitData() throws IOException {
		System.out.println("testFruitData");
		int limit = 5;

		List<SparseDoubleVector> vecs = TestConstants.loadFruitVectors();

		limit = Math.min(limit, vecs.size());

		Measure instance = newInstance();
		if (instance instanceof FeatureMarginalsCarrier)
			((FeatureMarginalsCarrier) instance)
					.setFeatureMarginals(featureMarginals(vecs));

		final double[][] results = new double[limit][limit];
		for (int i = 0; i < limit; i++) {
			for (int j = 0; j < limit; j++) {
				SparseDoubleVector A = vecs.get(i);
				SparseDoubleVector B = vecs.get(j);
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

		List<SparseDoubleVector> vecs = TestConstants.loadFruitVectors();

		limit = Math.min(limit, vecs.size());

		T instance = newInstance();
		for (SparseDoubleVector vec : vecs) {
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
		Assume.assumeTrue(getMeasureClass().isInstance(
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

	static SparseDoubleVector randomVector(Random rand, int card) {
		final SparseDoubleVector vector = new SparseDoubleVector(card, card);
		for (int i = 0; i < card; i++) {
			if (rand.nextDouble() > 0.75)
				vector.set(i, rand.nextInt(Integer.MAX_VALUE));
		}
		return vector;
	}

	/**
	 * TODO: Probably useful enough to move to a general purpose library
	 * 
	 * @param obj
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cloneWithSerialization(final T obj) {

		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			try {
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				oos = new ObjectOutputStream(baos);

				oos.writeObject(obj);
				oos.flush();

				final byte[] bytes = baos.toByteArray();

				ois = new ObjectInputStream(new ByteArrayInputStream(bytes));

				return (T) ois.readObject();
			} finally {
				if (oos != null)
					oos.close();
				if (ois != null)
					ois.close();
			}
		} catch (ClassNotFoundException ex) {
			throw new AssertionError(ex);
		} catch (IOException ex) {
			throw new AssertionError(ex);
		}
	}

	public static <T> T clone(T obj) {
		try {
			Assert.assertTrue("doesn't implement Cloneable",
					obj instanceof Cloneable);
			final Method cloneMethod = obj.getClass().getMethod("clone");

			Assert.assertTrue("clone() is not public",
					Modifier.isPublic(cloneMethod.getModifiers()));
			Assert.assertFalse("clone() is abstract",
					Modifier.isAbstract(cloneMethod.getModifiers()));
			Assert.assertFalse("clone() is static",
					Modifier.isStatic(cloneMethod.getModifiers()));
			final Object result = cloneMethod.invoke(obj);
			Assert.assertEquals("cloned instance class differes",
					result.getClass(), obj.getClass());
			Assert.assertEquals("cloned object not equal to origional", obj,
					result);
			@SuppressWarnings("unchecked")
			final T castResult = (T) result;
			return castResult;
		} catch (NoSuchMethodException e) {
			throw new AssertionError(e);
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (InvocationTargetException e) {
			throw new AssertionError(e);
		}

	}

	static SparseDoubleVector ones(int cardinality, int size) {
		SparseDoubleVector vec = new SparseDoubleVector(cardinality, size);
		for (int i = 0; i < size; i++)
			vec.set(i, 1);
		return vec;
	}

	static SparseDoubleVector zeros(int cardinality) {
		return new SparseDoubleVector(cardinality, 0);
	}

	static MarginalDistribution featureMarginals(SparseDoubleVector... vecs) {
		return featureMarginals(Arrays.asList(vecs));
	}

	static MarginalDistribution featureMarginals(List<SparseDoubleVector> vecs) {
		double[] marginals = new double[vecs.get(0).cardinality];
		int nonZeroCardinality = 0;
		double total = 0;
		for (SparseDoubleVector vec : vecs) {
			for (int i = 0; i < vec.keys.length; i++) {
				if (marginals[vec.keys[i]] == 0)
					++nonZeroCardinality;
				marginals[vec.keys[i]] += vec.values[i];
			}
			total += vec.sum;
		}
		return new MarginalDistribution(marginals, total, nonZeroCardinality);
	}

	public static Random newRandom() {
		Random rand = new Random();
		final int seed = rand.nextInt();
		System.out.println(" > random seed = " + seed);
		rand = new Random(seed);
		return rand;
	}

	public static <T> T[] cat(final T[]... arrs) {
		int n = 0;
		for (int i = 0; i < arrs.length; i++)
			n += arrs[i].length;
		@SuppressWarnings("unchecked")
		T[] result = (T[]) Array.newInstance(arrs.getClass().getComponentType()
				.getComponentType(), n);
		int offset = 0;
		for (int i = 0; i < arrs.length; i++) {
			System.arraycopy(arrs[i], 0, result, offset, arrs[i].length);
			offset += arrs[i].length;
		}
		return result;
	}
}
