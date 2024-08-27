package com.wynd.vop.framework.validation;

import com.wynd.vop.framework.exception.VopRuntimeException;
import com.wynd.vop.framework.exception.VopValidationRuntimeException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DefenseTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPrivateConstructor() {
		Constructor<Defense> constructor = null;
		try {
			constructor = Defense.class.getDeclaredConstructor((Class<?>[]) null);
		} catch (NoSuchMethodException | SecurityException e1) {
			fail("Should NOT have thrown exception");
		}
		constructor.setAccessible(true);
		try {
			constructor.newInstance();
			fail("Should have thrown exception");
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			assertNotNull(e);
			assertTrue(InvocationTargetException.class.isAssignableFrom(e.getClass()));
			assertTrue(IllegalAccessError.class.isAssignableFrom(e.getCause().getClass()));
		}
	}

	@Test
	public void testIsInstanceOfHappyPath() {
		try {
			Defense defenseObj = Defense.class.newInstance();
			Defense.isInstanceOf(Defense.class, defenseObj);
		} catch (Exception e) {

		}
	}

	@Test
	public void testStateBooleanString() {
		Defense.state(true, "Boolean Condition is not satisfied");
	}

	@Test
	public void testStateBooleanStringForException() {
		try {
			Defense.state(false, "Boolean Condition is not satisfied");
		} catch (VopRuntimeException e) {
			assertTrue(e.getMessage().equals("Boolean Condition is not satisfied"));
		}
	}

	@Test(expected = VopValidationRuntimeException.class)
	public void testIsInstanceOfSadPath() {
		Defense.isInstanceOf(null, "test string object");
	}

	@Test
	public void testStateBoolean() {
		Defense.state(true);
	}

	@Test(expected = VopValidationRuntimeException.class)
	public void testStateBooleanSadPath() {
		Defense.state(false);
	}

	@Test
	public void testIsNullObject() {
		Defense.isNull(null);
	}

	@Test(expected = VopValidationRuntimeException.class)
	public void testIsNullObjectSadPath() {
		Defense.isNull(new Object());
	}

	@Test
	public void testIsNullObjectString() {
		Defense.isNull(null, "Object should be null");
	}

	@Test(expected = VopValidationRuntimeException.class)
	public void testIsNullObjectStringSadPath() {
		Defense.isNull(new Object(), "Object should be null");
	}

	@Test
	public void testNotNullObject() {
		Defense.notNull(this);
	}

	@Test(expected = VopValidationRuntimeException.class)
	public void testNotNullObjectSadPath() {
		Defense.notNull(null);
	}

	@Test
	public void testNotNullObjectString() {
		Defense.notNull(this, "Object cannot be null");
	}

	@Test
	public void testNotNullObjectStringForException() {
		try {
			Defense.notNull(null, "Object cannot be null");
		} catch (VopRuntimeException e) {
			assertTrue(e.getMessage().equals("Object cannot be null"));
		}
	}

	@Test
	public void testHasTextString() {
		Defense.hasText("Test Message");
	}

	@Test(expected = VopValidationRuntimeException.class)
	public void testHasTextStringForException() {
		Defense.hasText(null);
	}

	@Test
	public void testHasTextStringString() {
		Defense.hasText("Test", "Missing Text");
	}

	@Test
	public void testHasTextStringStringForException() {
		try {
			Defense.hasText("", "Text cannot be blank");
		} catch (VopValidationRuntimeException e) {
			assertTrue(e.getMessage().equals("Text cannot be blank"));
		}

	}

	@Test
	public void testNotEmptyCollectionOfQ() {
		List<String> dummyList = new ArrayList<String>();
		dummyList.add("value1");
		Defense.notEmpty(dummyList);
	}

	@Test
	public void testNotEmptyCollectionOfQString() {
		List<String> dummyList = new ArrayList<String>();
		dummyList.add("value1");
		Defense.notEmpty(dummyList, "Dummy List cannot be empty");
	}

	@Test
	public void testNotEmptyCollectionOfQStringForException() {
		try {
			List<String> dummyList = new ArrayList<String>();
			Defense.notEmpty(dummyList, "Dummy List cannot be empty");
		} catch (VopValidationRuntimeException e) {
			assertTrue(e.getMessage().equals("Dummy List cannot be empty"));
		}
	}

	@Test
	public void testNotEmptyStringArrayString() {
		String[] strArr = { "value1,value2" };
		Defense.notEmpty(strArr, "Array cannot be empty");
	}

	@Test
	public void testNotEmptyStringArrayStringForException() {
		try {
			String[] strArr = null;
			Defense.notEmpty(strArr, "Array cannot be empty");
		} catch (VopRuntimeException e) {
			assertTrue(e.getMessage().equals("Array cannot be empty"));
		}
	}

	@Test
	public void testIsTrueBoolean() {
		Defense.isTrue(true);
	}

	@Test
	public void testIsTrueBooleanString() {
		try {
			Defense.isTrue(false, "Boolean condition not met");

		} catch (VopValidationRuntimeException e) {
			assertTrue(e.getMessage().equals("Boolean condition not met"));
		}
	}

}
