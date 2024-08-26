package com.wynd.vop.framework.audit.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class HttpResponseAuditDataTest {

	@Test
	public void toStringTest() {
		HttpResponseAuditData responseAuditData = new HttpResponseAuditData();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("testKey", "testValue");
		responseAuditData.setHeaders(headers);
		String response = "test response";
		responseAuditData.setResponse(response);
		String testAttachment = "test attachment";
		List<String> attachmentTextList = new LinkedList<>();
		attachmentTextList.add(testAttachment);
		responseAuditData.setAttachmentTextList(attachmentTextList);
		assertTrue(responseAuditData.toString().equals("HttpResponseAuditData{headers=" + ReflectionToStringBuilder.toString(headers)
		+ ", uri='" + "', response='" + response + "', attachmentTextList='" + responseAuditData.getAttachmentTextList()
		+ "'}"));
	}

	@Test
	public void toStringWithNullHeadersTest() {
		HttpResponseAuditData responseAuditData = new HttpResponseAuditData();
		responseAuditData.setHeaders(null);
		responseAuditData.setResponse(null);
		responseAuditData.setAttachmentTextList(null);
		assertTrue(responseAuditData.toString()
				.equals("HttpResponseAuditData{headers=, uri='', response='', attachmentTextList='null'}"));
	}
}
