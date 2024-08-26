package com.wynd.vop.framework.audit;

import com.wynd.vop.framework.audit.model.ResponseAuditData;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class ResponseAuditDataTest {

	@Test
	public void toStringTest() {
		ResponseAuditData responseAuditData = new ResponseAuditData();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("testKey", "testValue");
		String response = "test response";
		responseAuditData.setResponse(response);
		assertTrue(responseAuditData.toString()
				.equals("ResponseAuditData{response=" + ReflectionToStringBuilder.toString(response) + "}"));
	}

	@Test
	public void toStringWithNullHeadersTest() {
		ResponseAuditData responseAuditData = new ResponseAuditData();
		assertTrue(responseAuditData.toString().equals("ResponseAuditData{response=}"));
	}
}
