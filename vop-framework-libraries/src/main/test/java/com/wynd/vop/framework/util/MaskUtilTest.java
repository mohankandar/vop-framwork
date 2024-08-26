package com.wynd.vop.framework.util;

import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;

public class MaskUtilTest {

	@Test
	public final void testMaskUtil() throws NoSuchMethodException {
		Constructor<MaskUtil> constructor = ReflectionUtils.accessibleConstructor(MaskUtil.class);
		constructor.setAccessible(true);
		try {
			constructor.newInstance();
			fail("Should have thrown exception");
		} catch (Exception e) {
			assertEquals(InvocationTargetException.class, e.getClass());
			assertEquals(IllegalAccessError.class, e.getCause().getClass());
			assertTrue(e.getCause().getMessage().contains("Do not instantiate"));
		}
	}

	@Test
	public final void testMaskStartString() {
		assertNull(MaskUtil.maskStart(null));
		assertEquals("", MaskUtil.maskStart(""));
		assertEquals("*", MaskUtil.maskStart("t"));
		assertEquals("*est", MaskUtil.maskStart("test"));
		assertEquals("***ting", MaskUtil.maskStart("testing"));
	}

	@Test
	public final void testMaskStartStringCharIntInt() {
		assertNull(MaskUtil.maskStart(null, 'a', 0, 0));
		assertEquals("", MaskUtil.maskStart("", 'a', 0, 0));
		assertEquals("test", MaskUtil.maskStart("test", 'X', -1, 4));
		assertEquals("XXXX", MaskUtil.maskStart("test", 'X', 0, -1));
		assertEquals("test", MaskUtil.maskStart("test", 'X', 0, 4));
		assertEquals("Xest", MaskUtil.maskStart("test", 'X', 0, 3));
		assertEquals("XXst", MaskUtil.maskStart("test", 'X', 0, 2));
		assertEquals("XXXt", MaskUtil.maskStart("test", 'X', 0, 1));
		assertEquals("XXXX", MaskUtil.maskStart("test", 'X', 0, 0));
		assertEquals("Xest", MaskUtil.maskStart("test", 'X', 1, 4));
		assertEquals("XXst", MaskUtil.maskStart("test", 'X', 2, 4));
		assertEquals("XXXt", MaskUtil.maskStart("test", 'X', 3, 4));
		assertEquals("XXXX", MaskUtil.maskStart("test", 'X', 4, 4));

		assertEquals("Xest", MaskUtil.maskStart("test", 'X', 1, Integer.MAX_VALUE));
		assertEquals("XXXX", MaskUtil.maskStart("test", 'X', 1, Integer.MIN_VALUE));
	}

	@Test
	public final void testMaskEndString() {
		assertNull(MaskUtil.maskEnd(null));
		assertEquals("", MaskUtil.maskEnd(""));
		assertEquals("*", MaskUtil.maskEnd("t"));
		assertEquals("tes*", MaskUtil.maskEnd("test"));
		assertEquals("test***", MaskUtil.maskEnd("testing"));
	}

	@Test
	public final void testMaskEndStringCharIntInt() {
		assertNull(MaskUtil.maskEnd(null, 'a', 0, 0));
		assertEquals("", MaskUtil.maskEnd("", 'a', 0, 0));
		assertEquals("test", MaskUtil.maskEnd("test", 'X', -1, 4));
		assertEquals("XXXX", MaskUtil.maskEnd("test", 'X', 0, -1));
		assertEquals("test", MaskUtil.maskEnd("test", 'X', 0, 4));
		assertEquals("tesX", MaskUtil.maskEnd("test", 'X', 0, 3));
		assertEquals("teXX", MaskUtil.maskEnd("test", 'X', 0, 2));
		assertEquals("tXXX", MaskUtil.maskEnd("test", 'X', 0, 1));
		assertEquals("XXXX", MaskUtil.maskEnd("test", 'X', 0, 0));
		assertEquals("tesX", MaskUtil.maskEnd("test", 'X', 1, 4));
		assertEquals("teXX", MaskUtil.maskEnd("test", 'X', 2, 4));
		assertEquals("tXXX", MaskUtil.maskEnd("test", 'X', 3, 4));
		assertEquals("XXXX", MaskUtil.maskEnd("test", 'X', 4, 4));

		assertEquals("tesX", MaskUtil.maskEnd("test", 'X', 1, Integer.MAX_VALUE));
		assertEquals("XXXX", MaskUtil.maskEnd("test", 'X', 1, Integer.MIN_VALUE));
	}

	@Test
	public void testMaskString() {
		assertNull(MaskUtil.mask(null));
		assertEquals("", MaskUtil.mask(""));
		assertEquals("*", MaskUtil.mask("t"));
		assertEquals("****", MaskUtil.mask("test"));
		assertEquals("*******", MaskUtil.mask("testing"));
	}

	@Test
	public void testMask() {
		assertNull(MaskUtil.mask(null, '*', 4, 4, 4));
		assertEquals("", MaskUtil.mask("", '*', 4, 4, 4));
		assertEquals("test", MaskUtil.mask("test", 'X', -1, 2, 2));
		assertEquals("XXXX", MaskUtil.mask("test", 'X', 0, -1, -1));
		assertEquals("test", MaskUtil.mask("test", 'X', 0, 2, 2));
		assertEquals("tesX", MaskUtil.mask("test", 'X', 0, 3, 0));
		assertEquals("teXX", MaskUtil.mask("test", 'X', 0, 2, 0));
		assertEquals("tXXX", MaskUtil.mask("test", 'X', 0, 1, 0));
		assertEquals("XXXX", MaskUtil.mask("test", 'X', 0, 0, 0));

		assertEquals("Xest", MaskUtil.mask("test", 'X', 0, 0, 3));
		assertEquals("XXst", MaskUtil.mask("test", 'X', 0, 0, 2));
		assertEquals("XXXt", MaskUtil.mask("test", 'X', 0, 0, 1));

		assertEquals("tXst", MaskUtil.mask("test", 'X', 1, 4, 4));
		assertEquals("tXXt", MaskUtil.mask("test", 'X', 2, 4, 4));
		assertEquals("XXXt", MaskUtil.mask("test", 'X', 3, 4, 4));
		assertEquals("XXXX", MaskUtil.mask("test", 'X', 4, 4, 4));

		assertEquals("tXst", MaskUtil.mask("test", 'X', 1, Integer.MAX_VALUE, Integer.MAX_VALUE));
		assertEquals("teXst", MaskUtil.mask("teest", 'X', 1, Integer.MAX_VALUE, Integer.MAX_VALUE));
		assertEquals("tesX", MaskUtil.mask("test", 'X', 1, Integer.MAX_VALUE, 0));
		assertEquals("Xest", MaskUtil.mask("test", 'X', 1, 0, Integer.MAX_VALUE));
		assertEquals("Xest", MaskUtil.mask("test", 'X', 1, Integer.MAX_VALUE - 5, Integer.MAX_VALUE));
		assertEquals("tesX", MaskUtil.mask("test", 'X', 1, Integer.MAX_VALUE, Integer.MAX_VALUE - 5));
		assertEquals("tXst", MaskUtil.mask("test", 'X', 1, Integer.MAX_VALUE - 1, Integer.MAX_VALUE));
		assertEquals("teXt", MaskUtil.mask("test", 'X', 1, Integer.MAX_VALUE, Integer.MAX_VALUE - 1));
		assertEquals("tXXst", MaskUtil.mask("teest", 'X', 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE));
		assertEquals("teXXt", MaskUtil.mask("teest", 'X', 2, Integer.MAX_VALUE, Integer.MAX_VALUE - 1));
	}
}
