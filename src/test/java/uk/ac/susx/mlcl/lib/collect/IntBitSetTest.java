package uk.ac.susx.mlcl.lib.collect;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.susx.mlcl.lib.MemoryUsage;
import uk.ac.susx.mlcl.lib.MiscUtil;

public class IntBitSetTest extends AbstractIntSortedSetTest<IntBitSet> {

	@Override
	Class<? extends IntBitSet> getImplementation() {
		return IntBitSet.class;
	}

	@Test
	public void testPeekMemoryUsage() {
		System.out.println("testPeekMemoryUsage()");

		// Test needs at least 500MB of memory. Fail if it isn't available.
		final long requiredMemory = (1L << 29);
		Assert.assertTrue(String.format(
				"Please re-run tests with at least %s of available memory.",
				MiscUtil.humanReadableBytes(requiredMemory)), Runtime
				.getRuntime().maxMemory() > requiredMemory);

		IntBitSet instance = newInstance();
		instance.add(0);
		instance.add(Integer.MAX_VALUE);

		final MemoryUsage mu = new MemoryUsage();
		mu.add(instance);
		System.out.println("> object memory: " + mu.getInfoString());
		System.out.println("> system memory: " + MiscUtil.memoryInfoString());

		// Expected memory usage is 256 MiB of 2^31 bits @ 8 bits per bytes
		final double expectedSizeMB = (Integer.MAX_VALUE + 1.0)
				/ (8 * 1024 * 1024);
		final double actualSizeMB = (double) mu.getInstanceSizeBytes()
				/ (1024 * 1024);
		final double allowedeErrorMB = 1.0 / 1024;// Allow +/- 1KiB
		Assert.assertEquals(expectedSizeMB, actualSizeMB, allowedeErrorMB);

	}

}
