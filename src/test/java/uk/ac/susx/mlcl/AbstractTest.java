package uk.ac.susx.mlcl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

public class AbstractTest {

	public static Random newRandom() {
		// -1489608243
		Random rand = new Random();
		final int seed = rand.nextInt();
		System.out.println(" > random seed = " + seed);
		rand = new Random(seed);
		return rand;
	}

	public static IntArrayList randomIntArrayList(Random rand, int maxValue,
			int size) {
		return IntArrayList.wrap(randomIntArray(rand, maxValue, size));

	}

	public static int[] randomIntArray(Random rand, int maxValue, int size) {
		final int[] arr = new int[size];
		for (int i = 0; i < size; i++)
			arr[i] = rand.nextInt(maxValue);
		return arr;
	}

	public static <T> T assertCloneWithSerialization(final T obj) {
		try {
			T copy = cloneWithSerialization(obj);
			assertEquals(obj, copy);
			return copy;
		} catch (IOException e) {
			throw new AssertionError(e);
		} catch (ClassNotFoundException e) {
			throw new AssertionError(e);
		}

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
	public static <T> T cloneWithSerialization(final T obj) throws IOException,
			ClassNotFoundException {

		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
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
	}

	public static <T> T clone(T obj) {
		try {
			assertTrue("doesn't implement Cloneable", obj instanceof Cloneable);
			final Method cloneMethod = obj.getClass().getMethod("clone");

			assertTrue("clone() is not public",
					Modifier.isPublic(cloneMethod.getModifiers()));
			assertFalse("clone() is abstract",
					Modifier.isAbstract(cloneMethod.getModifiers()));
			assertFalse("clone() is static",
					Modifier.isStatic(cloneMethod.getModifiers()));
			final Object result = cloneMethod.invoke(obj);
			assertEquals("cloned instance class differes", result.getClass(),
					obj.getClass());
			assertEquals("cloned object not equal to origional", obj, result);
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

	public static void assertExhausedIterator(Iterator<?> it) {
		try {
			it.next();
			fail("Expected iterator to be exhaused by next() succeeded.");
		} catch (NoSuchElementException e) {
			// this is supposed to happen
		}
	}
}
