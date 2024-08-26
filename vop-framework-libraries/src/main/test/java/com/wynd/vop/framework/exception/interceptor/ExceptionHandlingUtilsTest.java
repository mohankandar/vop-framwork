package com.wynd.vop.framework.exception.interceptor;

import com.wynd.vop.framework.exception.VopRuntimeException;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.messages.MessageKey;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.slf4j.event.Level;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { JacksonAutoConfiguration.class })
public class ExceptionHandlingUtilsTest {

	private static final String TEST_KEY_MESSAGE = "NO_KEY";

	private static final MessageKey TEST_KEY = MessageKeys.NO_KEY;

	@SuppressWarnings("rawtypes")
	@Mock
	private ch.qos.logback.core.Appender mockAppender;

	@Mock
	NullPointerException nullPointerException;

	// Captor is genericised with ch.qos.logback.classic.spi.LoggingEvent
	@Captor
	private ArgumentCaptor<ch.qos.logback.classic.spi.LoggingEvent> captorLoggingEvent;

	VopLogger logger = VopLoggerFactory.getLogger(ExceptionHandlingUtilsTest.class);

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		logger.setLevel(Level.DEBUG);
		logger.getLoggerBoundImpl().addAppender(mockAppender);
	}

	@After
	public void tearDown() {

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoggingUtils() throws Exception {
		// setup
		Object[] args = new Object[2];
		args[0] = "Arg One";
		args[1] = 42L;

		Level originalLevel = logger.getLevel();
		logger.setLevel(Level.WARN);

		ExceptionHandlingUtils.logException("Catcher", this.getClass().getMethod("testLoggingUtils"), args,
				new Throwable("test throw"));
		verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());
		final List<ch.qos.logback.classic.spi.LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
		Assert.assertTrue(loggingEvents.get(0).toString().contains(
				"[WARN] Catcher caught exception, handling it as configured.  Here are details [java.lang.Throwable thrown by com.wynd.vop.framework.exception.interceptor.ExceptionHandlingUtilsTest.testLoggingUtils] args [[Arg One, 42]]."));
		Assert.assertTrue(loggingEvents.get(0).getMessage().contains("java.lang.Throwable"));
		Assert.assertEquals(ch.qos.logback.classic.Level.WARN, loggingEvents.get(0).getLevel());

		logger.setLevel(originalLevel);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoggingWarnOff() throws Exception {
		final VopLogger logger = VopLoggerFactory.getLogger(ExceptionHandlingUtilsTest.class);
		Level originalLevel = logger.getLevel();
		logger.setLevel(Level.ERROR);

		ExceptionHandlingUtils.logException("Catcher", myMethod(), null, new Throwable("test throw"));
		verify(mockAppender, times(1)).doAppend(captorLoggingEvent.capture());
		final List<ch.qos.logback.classic.spi.LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();

		Assert.assertEquals(
				"[ERROR] Catcher caught exception, handling it as configured.  Here are details [java.lang.Throwable thrown by com.wynd.vop.framework.exception.interceptor.ExceptionHandlingUtilsTest.someMethod] args [null].",
				loggingEvents.get(0).toString());
		Assert.assertEquals(ch.qos.logback.classic.Level.ERROR, loggingEvents.get(0).getLevel());

		logger.setLevel(originalLevel);
	}

	@Test
	public void testResolveRuntimeExceptionWithClassCastException() {
		ExceptionHandlingUtils.resolveRuntimeException(MessageKeys.NO_KEY, nullPointerException);
	}

	@Test
	public void testConvertFromBipExceptionExtender() {
		VopRuntimeException resolvedRuntimeException = ExceptionHandlingUtils
				.convertFromBipExceptionExtender(new VopRuntimeException(TEST_KEY, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST));
		assertTrue(resolvedRuntimeException.getExceptionData().getKey().equals(TEST_KEY.getKey()));
		assertTrue(resolvedRuntimeException.getMessage().equals(TEST_KEY_MESSAGE));
		assertTrue(resolvedRuntimeException.getExceptionData().getSeverity().equals(MessageSeverity.ERROR));
		assertTrue(resolvedRuntimeException.getExceptionData().getStatus().equals(HttpStatus.BAD_REQUEST));
	}

	@Test
	public void testConvertFromBipExceptionExtenderWithClassCastException() {
		try {
			ExceptionHandlingUtils.convertFromBipExceptionExtender(new RuntimeException());
		} catch (VopRuntimeException e) {
			assertTrue(e.getExceptionData().getKey().equals(MessageKeys.VOP_EXCEPTION_HANDLER_ERROR_VALUES.getKey()));
			assertTrue(e.getMessage().equals(MessageKeys.VOP_EXCEPTION_HANDLER_ERROR_VALUES.getMessage((String[]) null)));
			assertTrue(e.getExceptionData().getSeverity().equals(MessageSeverity.FATAL));
			assertTrue(e.getExceptionData().getStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR));
		}
	}

	@Test
	public void testCastToBipRuntimeException() {
		try {
			ExceptionHandlingUtils.castToBipRuntimeException(
					new VopRuntimeException(TEST_KEY, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST));
		} catch (VopRuntimeException e) {
			assertTrue(e.getExceptionData().getKey().equals(""));
			String msg = "Could not instantiate BipRuntimeException using values from throwable java.lang.RuntimeException";
			assertTrue(e.getMessage().equals(msg));
			assertTrue(e.getExceptionData().getSeverity().equals(MessageSeverity.FATAL));
			assertTrue(e.getExceptionData().getStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR));
		}
	}

	@Test
	public void testCastToBipRuntimeExceptionCatchBlock() {
		try {
			ExceptionHandlingUtils.castToBipRuntimeException(new Exception());
		} catch (VopRuntimeException e) {
			assertTrue(e.getExceptionData().getMessageKey().equals(MessageKeys.VOP_EXCEPTION_HANDLER_ERROR_CAST));
			String msg = MessageKeys.VOP_EXCEPTION_HANDLER_ERROR_CAST.getMessage((String[]) null);
			assertTrue(e.getMessage().equals(msg));
			assertTrue(e.getExceptionData().getSeverity().equals(MessageSeverity.FATAL));
			assertTrue(e.getExceptionData().getStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR));
		}
	}

	@Test
	public void testCastToBipRuntimeExceptionExtenderWithClassCastException() {
		try {
			ExceptionHandlingUtils.convertFromBipExceptionExtender(new RuntimeException());
		} catch (VopRuntimeException e) {
			assertTrue(e.getExceptionData().getKey().equals(MessageKeys.VOP_EXCEPTION_HANDLER_ERROR_VALUES.getKey()));
			assertTrue(e.getMessage().equals(MessageKeys.VOP_EXCEPTION_HANDLER_ERROR_VALUES.getMessage((String[]) null)));
			assertTrue(e.getExceptionData().getSeverity().equals(MessageSeverity.FATAL));
			assertTrue(e.getExceptionData().getStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR));
		}
	}

	public Method myMethod() throws NoSuchMethodException {
		return getClass().getDeclaredMethod("someMethod");
	}

	public void someMethod() {
		// do nothing
	}

}
