package com.wynd.vop.framework.rest.provider.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;

@RunWith(MockitoJUnitRunner.class)
public class BaseHttpProviderPointcutsTest {

	@Test
	public void testRestController() {
		BaseHttpProviderPointcuts.restController();
	}

	@Test
	public void testPublicServiceResponseRestMethod() {
		BaseHttpProviderPointcuts.publicServiceResponseRestMethod();
	}

	@Mock
	private ProceedingJoinPoint proceedingJoinPoint;
	@Mock
	private MethodSignature signature;

	@Mock
	private JoinPoint.StaticPart staticPart;
	private Object[] value;

	@Before
	public void setUp() throws Exception {
		value = new Object[1];
		value[0] = "";
		Mockito.lenient().when(proceedingJoinPoint.getArgs()).thenReturn(value);
		Mockito.lenient().when(proceedingJoinPoint.getStaticPart()).thenReturn(staticPart);
		Mockito.lenient().when(proceedingJoinPoint.getSignature()).thenReturn(signature);
		Mockito.lenient().when(signature.getMethod()).thenReturn(myMethod());
	}

	/**
	 * Test of auditableAnnotation method, of class BaseHttpProviderPointcuts.
	 */
	@Test
	public void testAuditableAnnotation() {
		BaseHttpProviderPointcuts.auditableAnnotation();
	}

	/**
	 * Test of auditableExecution method, of class BaseHttpProviderPointcuts.
	 */
	@Test
	public void testAuditableExecution() {
		BaseHttpProviderPointcuts.auditableExecution();
	}

	/**
	 * Test of auditRestController method, of class BaseHttpProviderPointcuts.
	 */
	@Test
	public void testAuditRestController() {
		BaseHttpProviderPointcuts.restController();
	}

	/**
	 * Test of publicResourceDownloadRestMethod method, of class BaseHttpProviderPointcuts.
	 */
	@Test
	public void testPublicResourceDownloadRestMethod() {
		BaseHttpProviderPointcuts.publicResourceDownloadRestMethod();
	}


	public Method myMethod() throws NoSuchMethodException {
		return getClass().getDeclaredMethod("someMethod");
	}

	public void someMethod() {
		// do nothing
	}

}
