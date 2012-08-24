package uk.ac.susx.mlcl.lib;

import it.unimi.dsi.fastutil.ints.AbstractIntIterator;

import java.util.NoSuchElementException;
import java.util.Random;

public class UniformIntGenerator extends AbstractIntIterator {
	public static final int NO_LIMIT = -1;
	private static final int DEFAULT_MIN_VALUE = 0;
	private static final int DEFAULT_MAX_VALUE = Integer.MAX_VALUE;
	private final Random rand;
	private final int limit;
	private final int minValue;
	private final int maxValue;
	private int count = 0;

	public UniformIntGenerator(Random rand, int limit, int minValue,
			int maxValue) {
		this.rand = rand;
		this.limit = limit;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	public UniformIntGenerator(int limit, int maxValue) {
		this(new Random(), limit, DEFAULT_MIN_VALUE, maxValue);
	}

	public UniformIntGenerator(int limit) {
		this(new Random(), limit, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
	}

	public UniformIntGenerator() {
		this(new Random(), NO_LIMIT, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
	}

	@Override
	public boolean hasNext() {
		return limit == NO_LIMIT || count < limit;
	}

	@Override
	public int nextInt() {
		if (!hasNext())
			throw new NoSuchElementException();
		++count;
		return minValue + rand.nextInt(maxValue - minValue);
	}

}