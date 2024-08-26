package com.wynd.vop.framework.rest.provider;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.*;

public class MessageTest {

	private static final String TEST_TEXT = "test text";

	@Test
	public void testInitializationAndGetter() {
		Message message = new Message("ERROR", "test key", TEST_TEXT, 400);
		Date testTimeStamp = Date.from(Instant.now());
		message.setTimestamp(testTimeStamp);
		assertTrue(message.getTimestamp().equals(testTimeStamp));
		assertTrue(message.getStatus().equals("400"));
		assertTrue(message.getText().equals(TEST_TEXT));
		assertTrue(message.getHttpStatus().equals(HttpStatus.resolve(400)));
		assertTrue(message.getHttpStatus(400).equals(HttpStatus.resolve(400)));
	}

	@Test
	public void testGetStatusWhenNull() throws Exception {
		Message message1 = new Message("ERROR", "test key", TEST_TEXT, null);
		assertNull(message1.getStatus());
	}

	@Test
	public void testGetHttpStatus() throws Exception {
		Message message1 = new Message("ERROR", "test key", TEST_TEXT, 400);
		assertEquals(400, message1.getHttpStatus().value());
		assertEquals(202, message1.getHttpStatus(202).value());
	}

	@Test
	public void testGetHttpStatusWhenNull() throws Exception {
		Message message1 = new Message("ERROR", "test key", TEST_TEXT, null);
		assertNull(null, message1.getHttpStatus());
	}
}
