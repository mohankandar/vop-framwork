package com.wynd.vop.framework.audit.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class HttpRequestAuditDataTest {
	@Test
	public void toStringTest() {
		HttpRequestAuditData httpRequestAuditData = new HttpRequestAuditData();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("testKey", "testValue");
		httpRequestAuditData.setHeaders(headers);
		String request = "test request";
		List<Object> requestList = new LinkedList<>();
		requestList.add(request);
		httpRequestAuditData.setRequest(requestList);
		assertTrue(httpRequestAuditData.toString()
				.equals("HttpRequestAuditData{headers=" + ReflectionToStringBuilder.toString(headers) +
						", uri='" + httpRequestAuditData.getUri() + "\'" + ", method='" + httpRequestAuditData.getMethod()
						+ "', request='" + requestList + "', attachmentTextList='" + httpRequestAuditData.getAttachmentTextList() + "'}"));
	}

	@Test
	public void toStringWithNullHeadersAndRequestTest() {
		HttpRequestAuditData httpRequestAuditData = new HttpRequestAuditData();
		httpRequestAuditData.setHeaders(null);
		httpRequestAuditData.setRequest(null);
		assertTrue(httpRequestAuditData.toString()
				.equals("HttpRequestAuditData{headers=, uri='null', method='null', request='[]', attachmentTextList='null'}"));
	}
}
