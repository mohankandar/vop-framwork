package com.wynd.vop.framework.rest.provider;

import com.wynd.vop.framework.messages.MessageSeverity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ProviderResponseTest {

	private ProviderResponse testResponse;
	private List<Message> testMessages = new ArrayList<Message>();
	Message infoMessage;
	Message warnMessage;
	Message errorMessage;
	Message fatalMessage;

	@Before
	public void setUp() throws Exception {
		testResponse = new ProviderResponse();
		infoMessage = new Message("INFO", "InfoKey", "Dummy info text", 200);
		warnMessage = new Message("WARN", "WarnKey", "Dummy warning text", 200);
		errorMessage = new Message("ERROR", "ErrorKey", "Dummy error text", 400);
		fatalMessage = new Message("FATAL", "FatalKey", "Dummy fatal text", 500);
		addTestMessages();
	}

	private void addTestMessages() {
		testMessages.add(infoMessage);
		testMessages.add(warnMessage);
		testMessages.add(errorMessage);
		testMessages.add(fatalMessage);
	}

	@After
	public void tearDown() throws Exception {
		testMessages.clear();
	}

	@Test
	public void testAddMessage() {
		testResponse.addMessage(MessageSeverity.INFO, "InfoKey", "Dummy info text", HttpStatus.ACCEPTED);

		assertNotNull(testResponse.getMessages());
		assertEquals(1, testResponse.getMessages().size());

	}

	@Test
	public void testAddMessages() {
		testResponse.addMessages(testMessages);
		assertNotNull(testResponse.getMessages());
		assertEquals(4, testResponse.getMessages().size());
	}

	@Test
	public void testGetMessages() {
		testResponse.addMessages(testMessages);
		assertNotNull(testResponse.getMessages());
		assertEquals(4, testResponse.getMessages().size());
	}

	@Test
	public void testGetMessagesWithNullArgument() {
		testResponse.setMessages(null);
		assertNotNull(testResponse.getMessages());
		assertEquals(0, testResponse.getMessages().size());
	}

	@Test
	public void testSetMessages() {
		testResponse.setMessages(testMessages);
		ProviderResponse serviceResponseForEqualsTest = new ProviderResponse();
		assertFalse(testResponse.equals(serviceResponseForEqualsTest));
		serviceResponseForEqualsTest.setMessages(testMessages);
		assertTrue(testResponse.getMessages().get(0).equals(serviceResponseForEqualsTest.getMessages().get(0)));
		assertNotNull(testResponse.getMessages());
		assertEquals(4, testResponse.getMessages().size());
	}

	@Test
	public void testHasFatals() {
		testResponse.setMessages(testMessages);
		assertTrue(testResponse.hasFatals());
	}

	@Test
	public void testHasErrors() {
		testResponse.setMessages(testMessages);
		assertTrue(testResponse.hasErrors());
	}

	@Test
	public void testHasWarnings() {
		testResponse.setMessages(testMessages);
		assertTrue(testResponse.hasWarnings());
	}

	@Test
	public void testHasInfos() {
		testResponse.setMessages(testMessages);
		assertTrue(testResponse.hasInfos());
	}

}
