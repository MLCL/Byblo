package uk.ac.susx.mlcl.lib;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

import uk.ac.susx.mlcl.testing.AbstractTest;

public class PoissonDistributionTest extends AbstractTest {


	private static final long[] KNOWN_FACTORIALS = new long[] { 0L, 1L, 2L, 6L,
			24L, 120L, 720L, 5040L, 40320L, 362880L, 3628800L, 39916800L,
			479001600L, 6227020800L, 87178291200L, 1307674368000L,
			20922789888000L, 355687428096000L, 6402373705728000L,
			121645100408832000L, 2432902008176640000L };

	@Test
	public void testFactorial() {
		System.out.println("testFactorial()");

		// Negative values should throw illegalArgument
		for (int i = -5; i < 0; i++) {
			try {
				double i_factorial = PoissonDistribution.factorial(i);
				Assert.fail("factorial of negative numbers is undefined.");
			} catch (IllegalArgumentException ex) {
				// Pass
			}
		}

		// 0! = 1
		Assert.assertEquals("0! == 1", 1, PoissonDistribution.factorial(0), 0);

		// In the range 0 to 20 (inclusive) we should achieve exactly correct
		// answers
		for (int i = 1; i <= 20; i++) {
			double actual = PoissonDistribution.factorial(i);
			Assert.assertTrue(i + "! is Infinite", !Double.isInfinite(actual));
			Assert.assertTrue(i + "! is NaN", !Double.isNaN(actual));
			Assert.assertEquals(0.0, actual % 1.0, 0.0);
			Assert.assertEquals(KNOWN_FACTORIALS[i], actual, 0.0);
			Assert.assertEquals(KNOWN_FACTORIALS[i], (long) Math.floor(actual));
			Assert.assertEquals(KNOWN_FACTORIALS[i], (long) Math.ceil(actual));
		}

		// Beyond 20 we achieve increasing inaccurate reasonable approximations
		for (int i = 21; i <= 170; i++) {
			double actual = PoissonDistribution.factorial(i);
			Assert.assertTrue(i + "! is Infinite", !Double.isInfinite(actual));
			Assert.assertTrue(i + "! is NaN", !Double.isNaN(actual));
			Assert.assertTrue("(n-1)! >= n! for n=" + i,
					PoissonDistribution.factorial(i - 1) < actual);
			if (i < 170)
				Assert.assertTrue("(n+1)! <= n! for n=" + i,
						PoissonDistribution.factorial(i + 1) > actual);
		}

		for (long i = 171; i < Integer.MAX_VALUE; i = (long) ((i * 1.5) + 1)) {
			try {
				double actual = PoissonDistribution.factorial((int) i);
				Assert.fail("factorial can not be calcualted for i > 170");
			} catch (IllegalArgumentException ex) {
				// Pass
			}
		}
	}

	@Test
	public void testRandom() {
		System.out.println("testRandom()");

		final int parameters_to_try = 20;
		final int iterations_per_parameter = 10000;
		final double maxLambda = 100;

		final Random random = newRandom();
		for (int r = 0; r < parameters_to_try; r++) {
			final double lambda = random.nextDouble() * maxLambda;
			final PoissonDistribution instance = new PoissonDistribution(lambda);
			instance.setRandom(random);

			Int2IntOpenHashMap counts = new Int2IntOpenHashMap();
			for (int i = 0; i < iterations_per_parameter; i++) {
				int actual = instance.random();
				counts.add(actual, 1);

				Assert.assertTrue("samples should be positive but found "
						+ actual, actual >= 0);

			}

			Int2IntMap.Entry[] countArray = new ObjectArrayList<Int2IntMap.Entry>(
					counts.int2IntEntrySet())
					.toArray(new Int2IntMap.Entry[counts.size()]);
			Arrays.sort(countArray, new Comparator<Int2IntMap.Entry>() {

				@Override
				public int compare(Int2IntMap.Entry o1, Int2IntMap.Entry o2) {
					return o1.getIntKey() - o2.getIntKey();
				}

			});
			// System.out.println(Arrays.toString(countArray));

			double mean = 0;
			int n = 0;
			for (Int2IntMap.Entry e : countArray) {
				mean += e.getIntKey() * e.getIntValue();
				n += e.getIntValue();
			}
			mean /= n;

			double variance = 0;
			for (Int2IntMap.Entry e : countArray) {
				variance += (mean - e.getIntKey()) * (mean - e.getIntKey())
						* e.getIntValue();
			}
			variance /= n;
			//
			// System.out.println("lambda = " + lambda + ", mean = " + mean
			// + ", variance = " + variance);
			//

			Assert.assertEquals(MessageFormat.format(
					"Mean and variance should be approximately equal, but "
							+ "found mean = {0} and variance = {1}", mean,
					variance), mean / variance, 1.0, 0.05);

			Assert.assertEquals(
					MessageFormat.format(
							"Mean and lambda should be approximately equal, but "
									+ "found mean = {0} and lambda = {1}",
							mean, lambda), mean / lambda, 1.0, 0.05);
		}
	}

}
