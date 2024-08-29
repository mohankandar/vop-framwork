package com.wynd.vop.framework.cache.interceptor;

import com.wynd.vop.framework.audit.AuditLogSerializer;
import com.wynd.vop.framework.audit.BaseAsyncAudit;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.service.DomainResponse;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.event.Level;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@RunWith(MockitoJUnitRunner.class)
public class VopCacheInterceptorTest {

	@Mock
	AuditLogSerializer asyncAuditLogSerializer = new AuditLogSerializer();

	CacheInterceptor vopCacheInterceptor = new CacheInterceptor(true);

	class TestObject {
		public String testMethod(final String msg) {
			return "Hello";
		}
	}

	class TestInvocation implements MethodInvocation {
		Method method;

		public TestInvocation() {
			try {
				method = TestObject.class.getMethod("testMethod", String.class);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException("Could not set method");
			}
		}

		@Override
		public Object[] getArguments() {
			return new Object[] { "Hello" };
		}

		@Override
		public Object proceed() throws Throwable {
			return new DomainResponse();
		}

		@Override
		public Object getThis() {
			return this;
		}

		@Override
		public AccessibleObject getStaticPart() {
			return method;
		}

		@Override
		public Method getMethod() {
			return method;
		}
	}

	class BrokenTestInvocation extends TestInvocation {
		Method method;

		public BrokenTestInvocation() {
			super();
		}

		@Override
		public Method getMethod() {
			throw new RuntimeException("Testing");
		}
	}

	class ReturnsNullTestInvocation extends TestInvocation {
		@Override
		public Object proceed() throws Throwable {
			return null;
		}
	};

	@Before
	public void setup() throws Throwable {
		doNothing().when(asyncAuditLogSerializer).asyncAuditRequestResponseData(any(), any(), any(), any(), any());
		BaseAsyncAudit baseAsyncAudit = new BaseAsyncAudit();
		ReflectionTestUtils.setField(baseAsyncAudit, "auditLogSerializer", asyncAuditLogSerializer);
		vopCacheInterceptor.baseAsyncAudit = baseAsyncAudit;
	}

	@Test
	public final void testVopCacheInterceptor() {
		assertNotNull(new CacheInterceptor(false));
	}

	@Test
	public final void testInvokeMethodInvocation() throws Throwable {
		TestInvocation testInvocation = new TestInvocation();

		Object ret = vopCacheInterceptor.invoke(testInvocation);
		assertNotNull(ret);
		assertTrue(DomainResponse.class.isAssignableFrom(ret.getClass()));
		assertTrue(((DomainResponse) ret).getMessages().isEmpty());
	}

	@Test
	public final void testInvokeMethodInvocation_nullResponse_createObject() throws Throwable {
		CacheInterceptor noNullsCacheInterceptor = new CacheInterceptor(false);
		BaseAsyncAudit baseAsyncAudit = new BaseAsyncAudit();
		ReflectionTestUtils.setField(baseAsyncAudit, "auditLogSerializer", asyncAuditLogSerializer);

		noNullsCacheInterceptor.baseAsyncAudit = baseAsyncAudit;

		TestInvocation testInvocation = new ReturnsNullTestInvocation();

		Object ret = noNullsCacheInterceptor.invoke(testInvocation);
		assertNotNull(ret);
		assertTrue(Object.class.isAssignableFrom(ret.getClass()));
	}

	@Test
	public final void testInvokeMethodInvocation_nullResponse_allowNulls() throws Throwable {

		TestInvocation testInvocation = new ReturnsNullTestInvocation();

		Object ret = (vopCacheInterceptor).invoke(testInvocation);
		assertNull(ret);
	}

	@Test(expected = Throwable.class)
	public final void testHandleInternalException() throws Throwable {
		BrokenTestInvocation testInvocation = new BrokenTestInvocation();

		vopCacheInterceptor.invoke(testInvocation);
	}

	@Test
	public final void testInvokeMethodInvocationWithDebugDisabled() throws Throwable {
		TestInvocation testInvocation = new TestInvocation();

		testInvocation = new TestInvocation();
		VopLogger logger = (VopLogger) ReflectionTestUtils.getField(vopCacheInterceptor, "LOGGER");
		logger.setLevel(Level.INFO);
		Object ret = vopCacheInterceptor.invoke(testInvocation);
		assertNotNull(ret);
		assertTrue(DomainResponse.class.isAssignableFrom(ret.getClass()));
		assertTrue(((DomainResponse) ret).getMessages().isEmpty());
		logger.setLevel(Level.DEBUG);
	}
}
