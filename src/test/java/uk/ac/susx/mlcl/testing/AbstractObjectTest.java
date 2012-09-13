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

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class AbstractObjectTest<T> extends AbstractTest {


    protected T newInstance() {
        try {
            return getImplementation().newInstance();
        } catch (InstantiationException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    Constructor<? extends T> getConstructor(Class<?>... paramTypes) {
        try {
            return getImplementation().getConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    boolean hasConstructor(Class<?>... paramTypes) {
        try {
            getImplementation().getConstructor(paramTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    protected T newInstance(Class<?>[] types, Object[] params) {
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

    protected T newInstance(Class<?> type, Object param) {
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

    protected abstract Class<? extends T> getImplementation();

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
     * {@link #assertCloneEquals(Object, Object)} should hold.
     */
    @Test
    public void testObjectClone() {
        assumeTrue(Cloneable.class.isAssignableFrom(getImplementation()));
        assumeTrue(hasConstructor());

        final T instance = newInstance();
        final T copy = clone(instance);

        assertCloneEquals(instance, copy);
    }

    /**
     * If the object implements {@link Serializable} interface, then it should
     * be possible to serialize then de-serialize the object to receive an exact
     * deep copy. All the constraints of {@link #assertCloneEquals(Object, Object)} } should
     * hold.
     *
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @Test
    public void testCloneWithSerialization() throws IOException, ClassNotFoundException {

        assumeTrue(Serializable.class.isAssignableFrom(getImplementation()));
        assumeTrue(hasConstructor());

        final T instance = newInstance();
        final T copy = cloneWithSerialization(instance);

        assertCloneEquals(instance, copy);
    }

    /**
     * For a cloned object the following relations should hold:
     * <p/>
     * <pre>
     * 		x.clone() != x
     *  	x.clone().getClass() == x.getClass()
     * 		x.clone().equals(x)
     * 		x.hashCode() == x.clone().hashCode()
     * </pre>
     */
    protected static void assertCloneEquals(Object instance, Object copy) {
        assertTrue("cloned object is the the same as the original", instance != copy);
        assertTrue(MessageFormat.format("Clone object class identity mismatch; expecting {0} but found {1}", instance.getClass(),
                copy.getClass()), copy.getClass() == instance.getClass());
        assertEquals("cloned copy is not equal", instance, copy);
        assertTrue("cloned object hash-code mismatch", copy.hashCode() == instance.hashCode());
    }

    /**
     * Concatenate two or more arrays.
     * <p/>
     * This should really be in ArrayUtil
     *
     * @param arrays
     * @param <T>
     * @return
     */
    protected static <T> T[] cat(final T[]... arrays) {
        int n = 0;
        for (T[] arr : arrays) n += arr.length;
        @SuppressWarnings("unchecked")
        T[] result = (T[]) Array.newInstance(arrays.getClass().getComponentType()
                .getComponentType(), n);
        int offset = 0;
        for (T[] arr : arrays) {
            System.arraycopy(arr, 0, result, offset, arr.length);
            offset += arr.length;
        }
        return result;
    }
}
