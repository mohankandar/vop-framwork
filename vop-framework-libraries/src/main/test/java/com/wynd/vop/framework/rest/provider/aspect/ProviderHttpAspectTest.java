package com.wynd.vop.framework.rest.provider.aspect;

import com.wynd.vop.framework.AbstractBaseLogTester;
import com.wynd.vop.framework.aspect.AuditableAnnotationAspect;
import com.wynd.vop.framework.audit.AuditEventData;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import com.wynd.vop.framework.messages.ServiceMessage;
import com.wynd.vop.framework.rest.provider.ProviderResponse;
import com.wynd.vop.framework.service.DomainResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPart;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ProviderHttpAspectTest extends AbstractBaseLogTester {

	private final VopLogger providerHttpLog = super.getLogger(ProviderHttpAspect.class);

	private ProviderHttpAspect providerHttpAspect;

	@Mock
	private JoinPoint joinPoint;

	@Mock
	private ResponseEntity<DomainResponse> responseEntity;

	@Mock
	private DomainResponse domainResponse;

	@Mock
	private MethodSignature mockSignature;

	@InjectMocks
	private final AuditableAnnotationAspect logAnnotatedAspect = new AuditableAnnotationAspect();

	private final Object[] mockArray = { new Object() };

	private final List<ServiceMessage> detailedMsg = new ArrayList<ServiceMessage>();

	@Before
	public void setUp() throws Exception {
		super.getAppender().clear();

		final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
		final MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();

		httpServletRequest.setContentType("multipart/form-data");
		final MockPart userData = new MockPart("userData", "userData", "{\"name\":\"test aida\"}".getBytes());
		httpServletRequest.addPart(userData);

		httpServletRequest.addHeader("TestHeader", "TestValue");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpServletRequest, httpServletResponse));

		super.getAppender().clear();
		providerHttpLog.setLevel(Level.DEBUG);
		try {
			Mockito.lenient().when(joinPoint.getArgs()).thenReturn(mockArray);
			Mockito.lenient().when(joinPoint.getSignature()).thenReturn(mockSignature);
			Mockito.lenient().when(mockSignature.getMethod()).thenReturn(myMethod());

			final ServiceMessage msg =
					new ServiceMessage(MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, MessageKeys.NO_KEY, "ServiceMessage text");
			detailedMsg.add(msg);
			Mockito.lenient().when(responseEntity.getBody()).thenReturn(domainResponse);
			Mockito.lenient().when(domainResponse.getMessages()).thenReturn(detailedMsg);
		} catch (final Throwable e) {

		}

	}

	@Test
	public void testbeforeAuditAdvice() {
		super.getAppender().clear();

		providerHttpAspect = new ProviderHttpAspect();
		try {
			Mockito.lenient().when(joinPoint.getSignature()).thenReturn(mockSignature);
			Mockito.lenient().when(mockSignature.getMethod()).thenReturn(myMethod());
			Mockito.lenient().when(joinPoint.getTarget()).thenReturn(new TestClass());

			providerHttpAspect.beforeAuditAdvice(joinPoint);
		} catch (final Throwable throwable) {
			fail("Exception is not exceptecd");
		}
	}

	@Test
	public void testbeforeAuditAdviceWithNoArgsForJoinPoint() {
		super.getAppender().clear();

		providerHttpAspect = new ProviderHttpAspect();
		try {
			Mockito.lenient().when(joinPoint.getSignature()).thenReturn(mockSignature);
			Mockito.lenient().when(joinPoint.getArgs()).thenReturn(new Object[] {});
			Mockito.lenient().when(mockSignature.getMethod()).thenReturn(myMethod());
			Mockito.lenient().when(joinPoint.getTarget()).thenReturn(new TestClass());
			VopLoggerFactory.getLogger(ProviderHttpAspect.class).setLevel(Level.INFO);

			providerHttpAspect.beforeAuditAdvice(joinPoint);
		} catch (final Throwable throwable) {
			fail("Exception is not exceptecd");
		}
	}

	// @Test
	// public void testbeforeAuditAdviceCatchBlock() {
	// super.getAppender().clear();
	//
	// providerHttpAspect = spy(ProviderHttpAspect.class);
	// try {
	// when(joinPoint.getSignature()).thenReturn(mockSignature);
	// when(joinPoint.getArgs()).thenReturn(new Object[] {});
	// when(mockSignature.getMethod()).thenReturn(myMethod());
	// when(mockSignature.getDeclaringType()).thenReturn(this.getClass());
	// when(mockSignature.getName()).thenReturn("myMethod");
	//
	// Mockito.lenient().when(joinPoint.getTarget()).thenReturn(new TestClass());
	// doThrow().when(providerHttpAspect).writeRequestInfoAudit(Arrays.asList(new Object[] {}),
	// new AuditEventData(AuditEvents.API_REST_REQUEST, mockSignature.getName(), mockSignature.getDeclaringType().getName()));
	//
	// providerHttpAspect.beforeAuditAdvice(joinPoint);
	// } catch (final Throwable throwable) {
	// fail("Exception is not expectecd");
	// }
	// }

	@Test
	public void testAfterreturningAuditAdvice() {
		super.getAppender().clear();

		providerHttpAspect = new ProviderHttpAspect();
		try {
			Mockito.lenient().when(joinPoint.getSignature()).thenReturn(mockSignature);
			Mockito.lenient().when(mockSignature.getMethod()).thenReturn(myMethod());
			Mockito.lenient().when(joinPoint.getTarget()).thenReturn(new TestClass());
			providerHttpAspect.afterreturningAuditAdvice(joinPoint, new ResponseEntity<ProviderResponse>(HttpStatus.OK));
		} catch (final Throwable throwable) {
			fail("Exception is not exceptecd");
		}
	}

	@Test
	public void testAfterreturningAuditAdvice_withProviderResponseBody() {
		super.getAppender().clear();

		providerHttpAspect = new ProviderHttpAspect();
		try {
			Mockito.lenient().when(joinPoint.getSignature()).thenReturn(mockSignature);
			Mockito.lenient().when(mockSignature.getMethod()).thenReturn(myMethod());
			Mockito.lenient().when(joinPoint.getTarget()).thenReturn(new TestClass());
			providerHttpAspect.afterreturningAuditAdvice(joinPoint,
					new ResponseEntity<ProviderResponse>(new ProviderResponse(), HttpStatus.OK));
		} catch (final Throwable throwable) {
			fail("Exception is not exceptecd");
		}
	}

	@Test
	public void testAfterreturningAuditAdvice_withProviderResponseReturnType() {
		super.getAppender().clear();

		providerHttpAspect = new ProviderHttpAspect();
		try {
			Mockito.lenient().when(joinPoint.getSignature()).thenReturn(mockSignature);
			Mockito.lenient().when(mockSignature.getMethod()).thenReturn(myMethod());
			Mockito.lenient().when(joinPoint.getTarget()).thenReturn(new TestClass());
			providerHttpAspect.afterreturningAuditAdvice(joinPoint, new ProviderResponse());
		} catch (final Throwable throwable) {
			fail("Exception is not exceptecd");
		}
	}

	@Test
	public void testAfterreturningAuditAdvice_withNullReturned() {
		super.getAppender().clear();

		providerHttpAspect = new ProviderHttpAspect();
		try {
			Mockito.lenient().when(joinPoint.getSignature()).thenReturn(mockSignature);
			Mockito.lenient().when(mockSignature.getMethod()).thenReturn(myMethod());
			Mockito.lenient().when(joinPoint.getTarget()).thenReturn(new TestClass());
			providerHttpAspect.afterreturningAuditAdvice(joinPoint, null);
		} catch (final Throwable throwable) {
			fail("Exception is not exceptecd");
		}
	}

	@Test
	public void testAfterThrowingAdvice() {
		super.getAppender().clear();
		providerHttpAspect = new ProviderHttpAspect();
		try {
			providerHttpAspect.afterThrowingAdvice(joinPoint, new Exception("test exception"));
		} catch (Throwable e) {
			fail("Exception should not be thrown");
		}
	}

	@Test
	public void testAfterThrowingAdviceWithNullThrowableArgument() {
		super.getAppender().clear();
		providerHttpAspect = new ProviderHttpAspect();
		try {
			providerHttpAspect.afterThrowingAdvice(joinPoint, null);
		} catch (Throwable e) {
			fail("Exception should not be thrown");
		}
	}

	@Test
	public void testHandleInternalException() {
		providerHttpAspect = mock(ProviderHttpAspect.class);
		try {
			ReflectionTestUtils.invokeMethod(providerHttpAspect, "handleInternalException", "test adviceName",
					"attemptingToAction test value", new AuditEventData(null, null, null), new Throwable("test Message"));
		} catch (Throwable e) {
			fail("Exception should not be thrown");
		}
	}

	@Test
	public void testHandleAnyRethrownExceptions() {
		providerHttpAspect = mock(ProviderHttpAspect.class);
		ResponseEntity<ProviderResponse> entity = ReflectionTestUtils.invokeMethod(providerHttpAspect, "handleAnyRethrownExceptions",
				"test adviceName", new Throwable("test Message"), new Throwable());
		assertNotNull(entity);
	}

	public Method myMethod() throws NoSuchMethodException {
		return getClass().getDeclaredMethod("someMethod");
	}

	public void someMethod() {
		// do nothing
	}

	class TestClass {

	}

}
