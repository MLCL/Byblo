package uk.ac.susx.mlcl.lib.collect;

import static org.junit.Assert.assertEquals;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntOpenHashBigSet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import uk.ac.susx.mlcl.AbstractTest;
import uk.ac.susx.mlcl.lib.MemoryUsage;
import uk.ac.susx.mlcl.lib.MiscUtil;
import uk.ac.susx.mlcl.lib.UniformIntGenerator;

import com.google.common.base.Stopwatch;

public class IntSetsTest extends AbstractTest {

	@SuppressWarnings("unchecked")
	static Class<? extends IntSet>[] INTSET_IMPLS = new Class[] {
			IntRangeSet.class,//
			IntRangeSet2.class,//
			IntRangeSet4.class,//
			IntBitSet.class,//
			IntOpenHashSet.class,//
			IntArraySet.class,//
			IntRBTreeSet.class, //
			IntAVLTreeSet.class,//
			IntLinkedOpenHashSet.class,//
			IntOpenHashBigSet.class };

	@Test
	public void compareInsertPerformance() throws InstantiationException,
			IllegalAccessException {
		System.out.println("compareInsertPerformance()");
		int repeats = 10;
		int dataSize = 100000;

		int minValue = 1;
		int maxValue = 8000000;
		//
		int populationSize = Integer.MAX_VALUE / 32;
		double exponent = 2.0;
		final Stopwatch sw = new Stopwatch();

		double[][] times = new double[INTSET_IMPLS.length][repeats];
		double[][] mem = new double[INTSET_IMPLS.length][repeats];

		for (int r = 0; r < repeats; r++) {

			// IntIterator gen = new ZipfianIntGenerator(newRandom(), dataSize,
			// populationSize,
			// exponent);

			IntIterator gen = new UniformIntGenerator(newRandom(), dataSize,
					minValue, maxValue);

			final int[] data = new IntOpenHashSet(gen).toIntArray();

			IntSet[] sets = new IntSet[INTSET_IMPLS.length];

			System.out.printf("Repeat %d of %d.%n", r, repeats);
			for (int i = 0; i < INTSET_IMPLS.length; i++) {
				final IntSet intSet = INTSET_IMPLS[i].newInstance();

				sw.reset();
				sw.start();
				for (int j = 0; j < data.length; j++) {
					intSet.add(data[j]);
				}
				sw.stop();
				times[i][r] = sw.elapsedMillis();

				attemptToTrim(intSet);

				sets[i] = new IntOpenHashSet(intSet);
				// System.out.println(intSet);

				MemoryUsage mu = new MemoryUsage();
				mu.add(intSet);
				mem[i][r] = mu.getInstanceSizeBytes();

				System.out.printf("%20s %6.2f seconds    %s%n",
						INTSET_IMPLS[i].getSimpleName(),
						sw.elapsedMillis() / 1000d,
						MiscUtil.humanReadableBytes(mu.getInstanceSizeBytes()));
			}

			for (int i = 0; i < sets.length - 1; i++) {
				assertEquals(sets[i], sets[i + 1]);
			}
		}
		System.out.printf("Results:%n");
		for (int i = 0; i < INTSET_IMPLS.length; i++) {

			double avTime = ArrayMath.mean(times[i]) / 1000.0;
			double seTime = ArrayMath.sampleStddev(times[i]) / 1000.0;

			long avMem = (long) ArrayMath.mean(mem[i]);
			long seMem = (long) ArrayMath.sampleStddev(mem[i]);

			System.out.printf("%20s %6.3f ~%-6.3f seconds    %-8s ~%-8s %n",
					INTSET_IMPLS[i].getSimpleName(), avTime, seTime,
					MiscUtil.humanReadableBytes(avMem),
					MiscUtil.humanReadableBytes(seMem));

		}

	}

	static void attemptToTrim(Object o) {
		try {
			o.getClass().getMethod("trim").invoke(o);
		} catch (IllegalAccessException e) {
			// fail
		} catch (InvocationTargetException e) {
			// fail
		} catch (NoSuchMethodException e) {
			// fail
		}

	}

}
