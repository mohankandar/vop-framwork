package com.wynd.vop.framework.audit.model;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class MessageAuditDataTest {

	@Test
	public void toStringWithNullMessageTest() {
		MessageAuditData messageAuditData = new MessageAuditData();
		messageAuditData.setMessage(null);
		assertNull(messageAuditData.getMessage());
		assertTrue(messageAuditData.toString().equals("MessageAuditData{" + '}'));
	}
	
	@Test
	public void toStringWithNotNullMessageTest() {
		MessageAuditData messageAuditData = new MessageAuditData();
		messageAuditData.setMessage(Arrays.asList("Test Message"));
		assertNotNull(messageAuditData.toString());
	}

}
