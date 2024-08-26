package com.wynd.vop.framework.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class HashGeneratorTest {

	private static final String TEST_INPUT_STRING = "TestInputString";

	private static final String INVALID_ALGORITHM = "InvalidAlgoritm";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetMd5ForString() {
		String encryptStr = HashGenerator.getMd5ForString(TEST_INPUT_STRING);
		assertNotNull(encryptStr);
		assertFalse(encryptStr.equals(TEST_INPUT_STRING));
	}

	@Test
	public void testGetGivenHashForString_CatchBlockCode() {
		try {
			Method method = HashGenerator.class.getDeclaredMethod("getGivenHashForString", String.class, String.class);
			method.setAccessible(true);
			assertNull(method.invoke(null, TEST_INPUT_STRING, INVALID_ALGORITHM));
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			fail("Exceptions not expected");
		}
	}

	@Test
	public void testConstructor() {
		try {
			Constructor<?> constructor = HashGenerator.class.getDeclaredConstructors()[0];
			constructor.setAccessible(true);
			constructor.newInstance();
			fail("Should have thrown IllegalAccessError");
		} catch (IllegalAccessError | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			assertTrue(InvocationTargetException.class.equals(e.getClass()));
			assertTrue(IllegalAccessError.class.equals(e.getCause().getClass()));
		}
	}

}
