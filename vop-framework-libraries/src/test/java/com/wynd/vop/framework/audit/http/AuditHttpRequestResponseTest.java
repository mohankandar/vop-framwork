package com.wynd.vop.framework.audit.http;

import com.wynd.vop.framework.audit.model.HttpRequestAuditData;
import com.wynd.vop.framework.audit.model.HttpResponseAuditData;
import com.wynd.vop.framework.exception.VopRuntimeException;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuditHttpRequestResponseTest {

	@Test
	public void getHttpRequestAuditDataTest() {
		AuditHttpRequestResponse auditHttpRequestResponse = new AuditHttpRequestResponse();
		HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
		HttpRequestAuditData requestAuditData = mock(HttpRequestAuditData.class);
		String[] stringArray = new String[] { "string1" };
		Set<String> set = new HashSet<>();
		set.addAll(Arrays.asList(stringArray));
		Enumeration<String> enumeration = new Vector<String>(set).elements();
		when(httpServletRequest.getHeaderNames()).thenReturn(enumeration);
		when(httpServletRequest.getContentType()).thenReturn(MediaType.MULTIPART_FORM_DATA_VALUE);
		ReflectionTestUtils.invokeMethod(auditHttpRequestResponse.new AuditHttpServletRequest(),
				"getHttpRequestAuditData", httpServletRequest, requestAuditData, null);
		verify(requestAuditData, times(1)).setAttachmentTextList(any());
		verify(requestAuditData, atLeastOnce()).setRequest(any());
	}

	@Test
	public void getHttpRequestAuditDataTestWithOctetStream() {
		AuditHttpRequestResponse auditHttpRequestResponse = new AuditHttpRequestResponse();
		HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
		HttpRequestAuditData requestAuditData = mock(HttpRequestAuditData.class);
		String[] stringArray = new String[] { "string1" };
		Set<String> set = new HashSet<>();
		set.addAll(Arrays.asList(stringArray));
		Enumeration<String> enumeration = new Vector<String>(set).elements();
		when(httpServletRequest.getHeaderNames()).thenReturn(enumeration);
		when(httpServletRequest.getContentType()).thenReturn(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		List<Object> requests =  new LinkedList<>();
		Resource mockResource = mock(Resource.class);
		InputStream inputStream = new ByteArrayInputStream("test string2".getBytes());
		try {
			when(mockResource.getInputStream()).thenReturn(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unable to mock Resource");
		}
		requests.add(mockResource);
		ReflectionTestUtils.invokeMethod(auditHttpRequestResponse.new AuditHttpServletRequest(), "getHttpRequestAuditData",
				httpServletRequest, requestAuditData, requests);
		verify(requestAuditData, times(1)).setAttachmentTextList(any());
		verify(requestAuditData, atLeastOnce()).setRequest(any());
	}

	@Test
	public void getHttpResponseAuditDataTest() {
		AuditHttpRequestResponse auditHttpRequestResponse = new AuditHttpRequestResponse();
		HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
		HttpResponseAuditData responseAuditData = mock(HttpResponseAuditData.class);
		Collection<String> enumeration = new ArrayList<String>();
		enumeration.add(HttpHeaders.CONTENT_TYPE);
		when(httpServletResponse.getHeaderNames()).thenReturn(enumeration);
		when(httpServletResponse.getContentType()).thenReturn(MediaType.TEXT_HTML_VALUE);
		ReflectionTestUtils.invokeMethod(auditHttpRequestResponse.new AuditHttpServletResponse(),
				"getHttpResponseAuditData", httpServletResponse, responseAuditData);
		verify(responseAuditData, times(1)).setHeaders(any());
	}

	@Test
	public void getHttpResponseAuditDataTestWithOctetStream() {
		AuditHttpRequestResponse auditHttpRequestResponse = new AuditHttpRequestResponse();
		HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
		HttpResponseAuditData responseAuditData = mock(HttpResponseAuditData.class);
		Collection<String> enumeration = new ArrayList<String>();
		enumeration.add(HttpHeaders.CONTENT_TYPE);
		when(httpServletResponse.getHeaderNames()).thenReturn(enumeration);
		when(httpServletResponse.getContentType()).thenReturn(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		ReflectionTestUtils.invokeMethod(auditHttpRequestResponse.new AuditHttpServletResponse(), "getHttpResponseAuditData",
				httpServletResponse, responseAuditData);
		verify(responseAuditData, times(1)).setHeaders(any());
	}

	@Test
	public void addStringOfSetSizeFromResource_Exception() {
		AuditHttpRequestResponse auditHttpRequestResponse = new AuditHttpRequestResponse();
		Resource mockResource = mock(Resource.class);
		try {
			when(mockResource.getInputStream()).thenThrow(new IOException());
		} catch (IOException e) {
			fail("Exception could not be triggered to test exception code");
		}
		ReflectionTestUtils.invokeMethod(auditHttpRequestResponse.new AuditHttpServletRequest(),
				"addStringOfSetSizeFromResource", new LinkedList<String>(), mockResource);
	}

	@Test(expected = VopRuntimeException.class)
	public void forwardDataInBodyToResponseTest_Exception() {
		AuditHttpRequestResponse auditHttpRequestResponse = new AuditHttpRequestResponse();
		ContentCachingResponseWrapper mockResponseWrapper = mock(ContentCachingResponseWrapper.class);
		try {
			doThrow(new IOException()).when(mockResponseWrapper).copyBodyToResponse();
		} catch (IOException e) {
			fail("Exception could not be triggered to test exception code");
		}
		ReflectionTestUtils.invokeMethod(auditHttpRequestResponse.new AuditHttpServletResponse(), "forwardDataInBodyToResponse",
				mockResponseWrapper);
	}

}
