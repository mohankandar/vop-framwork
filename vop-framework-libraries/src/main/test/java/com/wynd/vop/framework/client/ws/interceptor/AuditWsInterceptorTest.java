package com.wynd.vop.framework.client.ws.interceptor;

import com.wynd.vop.framework.audit.AuditEventData;
import com.wynd.vop.framework.audit.AuditEvents;
import com.wynd.vop.framework.client.ws.interceptor.transport.ByteArrayTransportOutputStream;
import com.wynd.vop.framework.exception.VopPartnerRuntimeException;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class AuditWsInterceptorTest {

	AuditWsInterceptor interceptor = new AuditWsInterceptor(AuditWsInterceptorConfig.AFTER);
	MessageContext messageContext = mock(MessageContext.class);
	WebServiceMessage webServiceMessage = new WebServiceMessage() {

		@Override
		public void writeTo(final OutputStream outputStream) throws IOException {
			((ByteArrayTransportOutputStream) outputStream).write("test xml message".getBytes());
		}

		@Override
		public Source getPayloadSource() {
			return null;
		}

		@Override
		public Result getPayloadResult() {
			return null;
		}
	};
	AuditWsInterceptorConfig.AuditWsMetadata auditWsMetaData = mock(AuditWsInterceptorConfig.AuditWsMetadata.class);
	AuditEventData auditServiceEventData = new AuditEventData(AuditEvents.SERVICE_AUDIT, "MethodName", "ClassName");

	@Test
	public void handleRequestTest() {
		assertTrue(interceptor.handleRequest(messageContext));
	}

	@Test
	public void handleResponseTest() {
		assertTrue(interceptor.handleResponse(messageContext));
	}

	@Test
	public void handleFaultTest() {
		assertTrue(interceptor.handleFault(messageContext));
	}

	@Test
	public void afterCompletionTest() {
		interceptor.afterCompletion(messageContext, new Exception());
		verify(messageContext, times(1)).getRequest();
		verify(messageContext, times(2)).getResponse();
	}

	@Test
	public void afterCompletionAlreadyLoggedTest() {
		ReflectionTestUtils.setField(interceptor, "alreadyLogged", true);
		interceptor.afterCompletion(messageContext, new Exception());
		verify(messageContext, times(0)).getRequest();
		verify(messageContext, times(0)).getResponse();
	}

	@Test
	public void doAuditTest() {
		when(auditWsMetaData.eventData()).thenReturn(auditServiceEventData);
		when(auditWsMetaData.messagePrefix()).thenReturn("test prefix value");
		ReflectionTestUtils.invokeMethod(interceptor, "doAudit", auditWsMetaData, webServiceMessage);
	}

	@Test
	public void getXmlTest() {
		assertTrue(ReflectionTestUtils.invokeMethod(interceptor, "getXml", webServiceMessage).equals("test xml message"));
	}

	@Test(expected = VopPartnerRuntimeException.class)
	public void handleInternalErrorTest() {
		ReflectionTestUtils.invokeMethod(interceptor, "handleInternalError", AuditEvents.PARTNER_SOAP_REQUEST, "test audit Activity",
				new Exception());
	}

	@Test
	public void writeAuditErrorTest() {
		ReflectionTestUtils.invokeMethod(interceptor, "writeAuditError", "test advice name", new Exception(),
				new AuditEventData(AuditEvents.PARTNER_SOAP_REQUEST, "test activity", "test audited name"));
	}

}
