package com.wynd.vop.framework.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;

public class HttpHeadersUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void tesBuildHttpHeadersForError() {
		HttpHeaders httpResponseHeaders = HttpHeadersUtil.buildHttpHeadersForError();
		assertNotNull(httpResponseHeaders);
	}

	@Test
	public void testConstructor() {
		try {
			Constructor<?> constructor = HttpHeadersUtil.class.getDeclaredConstructors()[0];
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
