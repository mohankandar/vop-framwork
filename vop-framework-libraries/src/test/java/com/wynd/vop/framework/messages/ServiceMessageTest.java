package com.wynd.vop.framework.messages;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.*;

public class ServiceMessageTest {

	private static final MessageKeys TEST_KEY = MessageKeys.NO_KEY;
	private static final String TEST_KEY_MESSAGE = "NO_KEY";

	@Test
	public void testEmptyConstructor() throws Exception {
		ServiceMessage serviceMessage = new ServiceMessage(null, null, null, null, new String[] { null });
		assertEquals(TEST_KEY.getKey(), serviceMessage.getKey());
		assertNull(serviceMessage.getSeverity());
		assertNull(serviceMessage.getText());
	}

	@Test
	public void testSeverityKeyConstructor() throws Exception {
		ServiceMessage serviceMessage =
				new ServiceMessage(MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, TEST_KEY, "ServiceMessage text");
		assertEquals(MessageSeverity.ERROR, serviceMessage.getSeverity());
		assertEquals(TEST_KEY.getKey(), serviceMessage.getKey());
	}

	@Test
	public void testSeverityKeyTextConstructor() throws Exception {
		ServiceMessage serviceMessage =
				new ServiceMessage(MessageSeverity.WARN, HttpStatus.BAD_REQUEST, TEST_KEY, "ServiceMessage text");
		assertEquals(MessageSeverity.WARN, serviceMessage.getSeverity());
		assertEquals(TEST_KEY.getKey(), serviceMessage.getKey());
		assertEquals(TEST_KEY_MESSAGE, serviceMessage.getText());
	}

	@Test
	public void testParamsConstructor() throws Exception {
		ServiceMessage serviceMessage = new ServiceMessage(MessageSeverity.ERROR, HttpStatus.BAD_REQUEST,
				new ConstraintParam[] { new ConstraintParam("param1", "para1Value") }, TEST_KEY, new String[] {});
		assertEquals(new Integer(1), serviceMessage.getParamCount());
	}

	@Test
	public void testGetStatus() throws Exception {
		ServiceMessage message1 =
				new ServiceMessage(MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, TEST_KEY, "ServiceMessage text");
		assertTrue(message1.getHttpStatus() == HttpStatus.BAD_REQUEST);
		assertNotNull(message1.getStatus());
	}

	@Test
	public void testGetStatusWhenNull() throws Exception {
		ServiceMessage message1 =
				new ServiceMessage(MessageSeverity.ERROR, null, TEST_KEY, "ServiceMessage text");
		assertNull(message1.getStatus());
	}

	@Test
	public void testMessageSeverityValueOf() throws Exception {

		assertEquals(MessageSeverity.WARN, MessageSeverity.fromValue("WARN"));
		assertEquals("WARN", MessageSeverity.WARN.value());
	}

}