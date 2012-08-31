/*
		assertTrue(instance != clone(instance));
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
package uk.ac.susx.mlcl.testing;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class AbstractObjectTest<T> extends AbstractTest {

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		AbstractTest.setUpClass();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		AbstractTest.tearDownClass();
	}

	public T newInstance() {
		try {
			return getImplementation().newInstance();
		} catch (InstantiationException e) {
			throw new AssertionError(e);
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}

	public Constructor<? extends T> getConstructor(Class<?>... paramTypes) {
		try {
			return getImplementation().getConstructor(paramTypes);
		} catch (NoSuchMethodException e) {
			throw new AssertionError(e);
		}
	}

	public boolean hasConstructor(Class<?>... paramTypes) {
		try {
			getImplementation().getConstructor(paramTypes);
			return true;
		} catch (NoSuchMethodException e) {
			return false;
		}
	}

	public T newInstance(Class<?>[] types, Object[] params) {
		try {
			return getConstructor(types).newInstance(params);
		} catch (InstantiationException e) {
			throw new AssertionError(e);
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (InvocationTargetException e) {
			throw new AssertionError(e);
		}
	}

	public T newInstance(Class<?> type, Object param) {
		try {
			return getConstructor(type).newInstance(param);
		} catch (InstantiationException e) {
			throw new AssertionError(e);
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		} catch (InvocationTargetException e) {
			throw new AssertionError(e);
		}
	}

	public abstract Class<? extends T> getImplementation();

	/**
	 * If the object has a default (zero parameter) constructor, then check it's
	 * callable without exception.
	 */
	@Test
	public void testDefaultConstructor() {

		Assume.assumeTrue(hasConstructor());
		T instance = newInstance();

		Assert.assertNotNull(instance);
	}

	/**
	 * If the object implements {@link Cloneable} interface, the clone method
	 * should be overridden and public. All the constraints of
	 * {@link #assertCloneEquals()} should hold.
	 */
	@Test
	public void testObjectClone() {
		Assume.assumeTrue(Cloneable.class.isAssignableFrom(getImplementation()));
		Assume.assumeTrue(hasConstructor());

		final T instance = newInstance();
		final T copy = clone(instance);

		assertCloneEquals(instance, copy);
	}

	/**
	 * If the object implements {@link Serializable} interface, then it should
	 * be possible to serialize then deserialize the object to recieve an exact
	 * deep copy. All the constraints of {@link #assertCloneEquals()} should
	 * hold.
	 * 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@Test
	public void testObjectCloneWithSerialization() throws IOException,
			ClassNotFoundException {

		Assume.assumeTrue(Serializable.class
				.isAssignableFrom(getImplementation()));
		Assume.assumeTrue(hasConstructor());

		final T instance = newInstance();
		final T copy = assertCloneWithSerialization(instance);

		assertCloneEquals(instance, copy);
	}

	/**
	 * For a cloned object the following relations should hold:
	 * 
	 * <pre>
	 * 		x.clone() != x
	 *  	x.clone().getClass() == x.getClass()
	 * 		x.clone().equals(x)
	 * 		x.hashCode() == x.clone().hashCode()
	 * </pre>
	 */
	private void assertCloneEquals(Object instance, Object copy) {
		Assert.assertTrue("cloned object is the the same as the origional",
				instance != copy);
		Assert.assertTrue(MessageFormat.format(
				"Clone object class identity mismatch; "
						+ "expecting {0} but found {1}", instance.getClass(),
				copy.getClass()), copy.getClass() == instance.getClass());
		Assert.assertEquals("cloned copy is not equal", instance, copy);
		Assert.assertTrue("cloned object hash-code mismatch",
				copy.hashCode() == instance.hashCode());
	}

}
