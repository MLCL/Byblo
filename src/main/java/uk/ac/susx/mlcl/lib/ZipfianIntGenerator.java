package uk.ac.susx.mlcl.lib;

import it.unimi.dsi.fastutil.ints.AbstractIntIterator;

import java.util.NoSuchElementException;
import java.util.Random;

public class ZipfianIntGenerator extends AbstractIntIterator {
	public static final int NO_LIMIT = -1;
	private static final int DEFAILT_POPULATION_SIZE = Integer.MAX_VALUE;
	private static final double DEFAULT_EXPONENT = 2.0;
	private final ZipfianDistribution zd;
	private final int limit;
	private int count = 0;

	public ZipfianIntGenerator(Random rand, int limit, int populationSize,
			double exponent) {
		zd = new ZipfianDistribution(populationSize, exponent);
		zd.setRandom(rand);
		this.limit = limit;
	}

	public ZipfianIntGenerator(int limit) {
		this(new Random(), limit, DEFAILT_POPULATION_SIZE, DEFAULT_EXPONENT);
	}

	public ZipfianIntGenerator() {
		this(new Random(), NO_LIMIT, DEFAILT_POPULATION_SIZE, DEFAULT_EXPONENT);
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
		return zd.random();
	}

}