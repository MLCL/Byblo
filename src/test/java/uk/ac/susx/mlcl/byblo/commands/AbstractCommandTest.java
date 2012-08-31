package uk.ac.susx.mlcl.byblo.commands;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.susx.mlcl.lib.commands.AbstractCommand;
import uk.ac.susx.mlcl.lib.commands.Command;
import uk.ac.susx.mlcl.lib.test.ExitTrapper;
import uk.ac.susx.mlcl.testing.AbstractObjectTest;

public abstract class AbstractCommandTest<T extends Command> extends
		AbstractObjectTest<T> {

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
		AbstractObjectTest.setUpClass();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		AbstractObjectTest.tearDownClass();
	}

	public boolean hasMethod(String name, Class<?>... parameterTypes) {
		try {
			getImplementation().getMethod(name, parameterTypes);
			return true;
		} catch (NoSuchMethodException e) {
			return false;
		}
	}

	@Test
	public void testCommandExitStatus() throws Throwable {
		// Not all commands have a main method
		Assume.assumeTrue(hasMethod("main", String[].class));

		try {
			ExitTrapper.enableExistTrapping();

			Method main = getImplementation().getMethod("main", String[].class);

			Assert.assertTrue("main method must be static",
					Modifier.isStatic(main.getModifiers()));
			Assert.assertTrue("main method must be public",
					Modifier.isPublic(main.getModifiers()));
			Assert.assertTrue("main method must return void",
					main.getReturnType() == void.class);

			try {
				main.invoke(null, (Object) new String[0]);
			} catch (InvocationTargetException ex) {
				// Wraps any exception exception thrown, including the the
				// ExitException we are expecting, just rethrow the cause
				throw ex.getCause();
			}

		} catch (ExitTrapper.ExitException ex) {
			assertTrue("Expecting non-zero exit status.", ex.getStatus() != 0);
		} finally {
			ExitTrapper.disableExitTrapping();
		}
	}

	@Test
	public void testHelp() throws Exception {
		Assume.assumeTrue(AbstractCommand.class
				.isAssignableFrom(getImplementation()));

		try {
			ExitTrapper.enableExistTrapping();

			T instance = newInstance();
			instance.runCommand(new String[] { "--help" });

		} catch (ExitTrapper.ExitException ex) {
			// assertTrue("Expecting non-zero exit status.", ex.getStatus() !=
			// 0);
		} finally {
			ExitTrapper.disableExitTrapping();
		}

	}

}
