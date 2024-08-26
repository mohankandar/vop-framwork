package com.wynd.vop.framework.aspect;

import com.wynd.vop.framework.AbstractBaseLogTester;
import com.wynd.vop.framework.log.VopLogger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.JoinPoint.StaticPart;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.event.Level;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PerformanceLoggingAspectTest extends AbstractBaseLogTester {

	/** Underlying logger implementation of BipLogger */
	private VopLogger AspectLoggingLOG = super.getLogger(PerformanceLoggingAspect.class);
	/** Underlying logger implementation of BipLogger */
	private VopLogger AspectLoggingTestLOG = super.getLogger(PerformanceLoggingAspectTest.class);

	@Mock
	private ProceedingJoinPoint proceedingJoinPoint;

	@Mock
	private MethodSignature signature;

	@Mock
	private JoinPoint.StaticPart staticPart;

	@Override
	@Before
	public void setup() throws Throwable {
		when(proceedingJoinPoint.toLongString()).thenReturn("ProceedingJoinPointLongString");
		when(proceedingJoinPoint.getStaticPart()).thenReturn(staticPart);
		when(staticPart.getSignature()).thenReturn(signature);
		when(signature.getMethod()).thenReturn(myMethod());
	}

	@Override
	@After
	public void tearDown() {
	}

	@Test
	public void testConstructor() {
		try {
			Constructor<?> constructor = PerformanceLoggingAspect.class.getDeclaredConstructors()[0];
			constructor.setAccessible(true);
			constructor.newInstance((Object[]) null);
			fail("Should have thrown InvocationTargetException.");
		} catch (IllegalAccessError | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			assertTrue(InvocationTargetException.class.equals(e.getClass()));
			assertTrue(IllegalAccessError.class.equals(e.getCause().getClass()));
		}
	}

	@Test
	public void testAroundAdviceDebugOn() throws Throwable {
		super.getAppender().clear();

		PerformanceLoggingAspect.aroundAdvice(proceedingJoinPoint);

		assertEquals("PerformanceLoggingAspect executing around method:ProceedingJoinPointLongString",
				super.getAppender().get(0).getMessage());
		assertEquals("enter [PerformanceLoggingAspectTest.someMethod]", super.getAppender().get(1).getMessage());
		assertEquals("PerformanceLoggingAspect after method was called.", super.getAppender().get(2).getMessage());
		assertTrue(
				super.getAppender().get(3).getMessage().contains("exit [PerformanceLoggingAspectTest.someMethod] in elapsed time ["));
		assertEquals(ch.qos.logback.classic.Level.INFO, super.getAppender().get(3).getLevel());

	}

	@Test(expected = NullPointerException.class)
	public void testAroundAdviceWithNullMethod() throws Throwable {
		when(signature.getMethod()).thenReturn(null);
		PerformanceLoggingAspect.aroundAdvice(proceedingJoinPoint);
	}

	@Test
	public void testAroundAdviceDebugOff() throws Throwable {
		super.getAppender().clear();
		AspectLoggingLOG.setLevel(Level.INFO);
		AspectLoggingTestLOG.setLevel(Level.INFO);

		PerformanceLoggingAspect.aroundAdvice(proceedingJoinPoint);

		assertTrue(
				super.getAppender().get(0).getMessage().contains("exit [PerformanceLoggingAspectTest.someMethod] in elapsed time ["));
		assertEquals(ch.qos.logback.classic.Level.INFO, super.getAppender().get(0).getLevel());

	}

	// TODO turned off until exception handling is decided
	@Test(expected = NullPointerException.class)
	public void testAroundAdviceThrowError() throws Throwable {
		super.getAppender().clear();
		AspectLoggingLOG.setLevel(Level.ERROR);
		AspectLoggingTestLOG.setLevel(Level.ERROR);
		MethodSignature mockSignature = mock(MethodSignature.class);
		StaticPart mockStaticPart = mock(StaticPart.class);
		when(mockSignature.getMethod()).thenReturn(null);
		when(mockStaticPart.getSignature()).thenReturn(mockSignature);
		when(proceedingJoinPoint.getStaticPart()).thenReturn(mockStaticPart);
		PerformanceLoggingAspect.aroundAdvice(proceedingJoinPoint);
	}

	public Method myMethod() throws NoSuchMethodException {
		return getClass().getDeclaredMethod("someMethod");
	}

	public void someMethod() {
		// do nothing
	}
}
