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
package uk.ac.susx.mlcl.byblo.weighings;

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
public abstract class AbstractMeasureTest<T extends Weighting> {

	static final double EPSILON = 1E-10;

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

	Weighting newConfiguredInstanceFor(SparseDoubleVector... data) {
		return newConfiguredInstanceFor(Arrays.asList(data));

	}

	Weighting newConfiguredInstanceFor(List<SparseDoubleVector> data) {
		try {
			Weighting measure = newInstance();
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
	
	
//
//	@Test
//	@Ignore
//	public void testRunFromCommandLine() throws Exception {
//		System.out.println("testRunFromCommandLine");
//
//		runFromCommandLine();
//	}
//
//	public void runFromCommandLine(String... extraArgs) throws Exception {
//		StringBuilder name = new StringBuilder();
//		name.append(getMeasureName());
//		for (String arg : extraArgs)
//			name.append(arg);
//
//		File output = new File(TestConstants.TEST_OUTPUT_DIR,
//				TestConstants.FRUIT_NAME + "-" + name.toString());
//		TestConstants.deleteIfExist(output);
//
//		try {
//			ExitTrapper.enableExistTrapping();
//
//			String[] args = new String[] { "allpairs", "--charset", "UTF-8",
//					"--measure", getMeasureName(), "--input",
//					TEST_FRUIT_EVENTS.toString(), "--input-features",
//					TEST_FRUIT_FEATURES.toString(), "--input-entries",
//					TestConstants.TEST_FRUIT_ENTRIES.toString(), "--output",
//					output.toString() };
//
//			args = cat(args, extraArgs);
//
//			Tools.main(args);
//
//		} finally {
//			ExitTrapper.disableExitTrapping();
//		}
//
//		Assert.assertTrue("Output file " + output + " does not exist.",
//				output.exists());
//		Assert.assertTrue("Output file " + output + " is empty.",
//				output.length() > 0);
//	}

	
	
	
	
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
