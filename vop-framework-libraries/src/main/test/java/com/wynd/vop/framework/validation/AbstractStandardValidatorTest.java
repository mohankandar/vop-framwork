package com.wynd.vop.framework.validation;

import com.wynd.vop.framework.exception.VopRuntimeException;
import com.wynd.vop.framework.messages.ServiceMessage;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class AbstractStandardValidatorTest {

	@Test
	public void initializeAbstractStandardValidatorTest() {
		AbstractStandardValidator<String> abstractStandardValidator = new AbstractStandardValidator<String>() {

			@Override
			public void validate(final String toValidate, final List<ServiceMessage> messages) {
				// do nothing
			}
		};

		try {
			abstractStandardValidator.setCallingMethod(AbstractStandardValidatorTest.class.getMethod("testMethod", String.class));
		} catch (NoSuchMethodException e) {
			fail("unable to find testMethod");
		} catch (SecurityException e) {
			fail("unable to fetch testMethod");
		}
		List<ServiceMessage> messages = null;

		abstractStandardValidator.initValidate(null, messages, new Object());
		assertTrue(abstractStandardValidator.getValidatedType() == null);

		abstractStandardValidator.initValidate("test string object", messages, new Object());
		assertTrue(abstractStandardValidator.getValidatedType().equals(String.class));

		// sad path
		abstractStandardValidator.initValidate("test string object", messages, new Object());
		assertTrue(abstractStandardValidator.getValidatedType().equals(String.class));

		Method method = null;
		try {
			method = AbstractStandardValidatorTest.class.getMethod("testMethod", String.class);
			assertTrue(abstractStandardValidator.getCallingMethod().equals(method));
		} catch (NoSuchMethodException e) {
			fail("unable to find testMethod");
		} catch (SecurityException e) {
			fail("unable to fetch testMethod");
		}
		assertTrue(abstractStandardValidator.getCallingMethodName()
				.equals(method.getDeclaringClass().getSimpleName() + "." + method.getName() + ": "));
		assertTrue(abstractStandardValidator.hasSupplemental());
		assertTrue(abstractStandardValidator.hasSupplemental(Object.class));
		assertTrue(abstractStandardValidator.getSupplemental(Object.class) instanceof Object);
	}

	@Test
	public void setToValidateClassTest() {
		AbstractStandardValidator<String> abstractStandardValidator = new AbstractStandardValidator<String>() {

			@Override
			public void validate(final String toValidate, final List<ServiceMessage> messages) {
				// do nothing
			}
		};

		ReflectionTestUtils.invokeMethod(abstractStandardValidator, "setToValidateClass", "test Object");
		assertNotNull(abstractStandardValidator.getValidatedType());
		assertTrue(abstractStandardValidator.getValidatedType().equals(String.class));

		// sad path
		ReflectionTestUtils.invokeMethod(abstractStandardValidator, "setToValidateClass", new Object[] { null });
		assertNull(abstractStandardValidator.getValidatedType());
	}

	@Test
	public void handleInvalidClassTest() {
		AbstractStandardValidator<String> abstractStandardValidator = new AbstractStandardValidator<String>() {

			@Override
			public void validate(final String toValidate, final List<ServiceMessage> messages) {
				// do nothing
			}
		};
		List<ServiceMessage> messages = new LinkedList<>();
		try {
			ReflectionTestUtils.setField(abstractStandardValidator, "toValidateClass", Integer.class);
			ReflectionTestUtils.invokeMethod(abstractStandardValidator, "handleInvalidClass", "test Object", messages);
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception not expected");
		}
	}

	@Test
	public void nullInvalidClassTest() {
		AbstractStandardValidator<String> abstractStandardValidator = new AbstractStandardValidator<String>() {

			@Override
			public void validate(final String toValidate, final List<ServiceMessage> messages) {
				// do nothing
			}
		};
		AbstractStandardValidator<String> spiedValidator = Mockito.spy(abstractStandardValidator);
		Mockito.doNothing().when(spiedValidator).setToValidateClass(Mockito.any(String.class));

		List<ServiceMessage> messages = new ArrayList<>();
		spiedValidator.initValidate("testString", messages);

		assertTrue("The messsages list should not be empty", !messages.isEmpty());
		assertTrue("Incorrect message", messages.get(0).getText().contains("no object"));
	}

	@Test
	public void initThrowsException() {
		AbstractStandardValidator<String> abstractStandardValidator = new AbstractStandardValidator<String>() {

			@Override
			public void validate(final String toValidate, final List<ServiceMessage> messages) {
				// do nothing
			}
		};
		AbstractStandardValidator<String> spiedValidator = Mockito.spy(abstractStandardValidator);
		Mockito.doThrow(new RuntimeException("Throw exception")).when(spiedValidator).setToValidateClass(Mockito.any(String.class));

		List<ServiceMessage> messages = new ArrayList<>();
		try {
			spiedValidator.initValidate("testString", messages);
			fail("spiedValidator.initValidate() should have thrown exception.");
		} catch (Throwable e) {
			assertTrue(VopRuntimeException.class.isAssignableFrom(e.getClass()));
			assertNotNull(e.getCause());
			assertTrue(e.getCause().getMessage().contains("Throw exception"));
		}

	}

	@Test
	public void getSupplementalTest() {
		AbstractStandardValidator<String> abstractStandardValidator = new AbstractStandardValidator<String>() {

			@Override
			public void validate(final String toValidate, final List<ServiceMessage> messages) {
				// do nothing
			}
		};
		ReflectionTestUtils.setField(abstractStandardValidator, "supplemental", new Object[] { "test object 1", "test object2" });
		Object[] returnValue = ReflectionTestUtils.invokeMethod(abstractStandardValidator, "getSupplemental");
		assertEquals(2, returnValue.length);
	}

	@Test
	public void getSupplementalForGivenClassTest() {
		AbstractStandardValidator<String> abstractStandardValidator = new AbstractStandardValidator<String>() {

			@Override
			public void validate(final String toValidate, final List<ServiceMessage> messages) {
				// do nothing
			}
		};
		ReflectionTestUtils.setField(abstractStandardValidator, "supplemental", new Object[] { "test object 1", "test object2" });
		Object returnValue = ReflectionTestUtils.invokeMethod(abstractStandardValidator, "getSupplemental", Integer.class);
		assertEquals(null, returnValue);
	}

	@Test
	public void hasSupplementalForGivenClassTest() {
		AbstractStandardValidator<String> abstractStandardValidator = new AbstractStandardValidator<String>() {

			@Override
			public void validate(final String toValidate, final List<ServiceMessage> messages) {
				// do nothing
			}
		};
		ReflectionTestUtils.setField(abstractStandardValidator, "supplemental", new Object[] { "test object 1", "test object2" });
		Boolean returnValue = ReflectionTestUtils.invokeMethod(abstractStandardValidator, "hasSupplemental", Integer.class);
		assertFalse(returnValue);
	}

	public void testMethod(final String testParam) {
		System.out.println(testParam);
	}

}
