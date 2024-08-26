package com.wynd.vop.framework.service;

import com.wynd.vop.framework.messages.ConstraintParam;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import com.wynd.vop.framework.messages.ServiceMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DomainResponseTest {

	private DomainResponse mockServiceResponse;
	private List<ServiceMessage> testMessages = new ArrayList<ServiceMessage>();
	ServiceMessage infoMessage;
	ServiceMessage warnMessage;
	ServiceMessage errorMessage;
	ServiceMessage fatalMessage;

	@Before
	public void setUp() throws Exception {
		mockServiceResponse = new DomainResponse();
		infoMessage = new ServiceMessage(MessageSeverity.INFO, HttpStatus.BAD_REQUEST, MessageKeys.NO_KEY, "ServiceMessage text");
		warnMessage = new ServiceMessage(MessageSeverity.WARN, HttpStatus.BAD_REQUEST, MessageKeys.NO_KEY, "ServiceMessage text");
		errorMessage = new ServiceMessage(MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, MessageKeys.NO_KEY, "ServiceMessage text");
		fatalMessage = new ServiceMessage(MessageSeverity.FATAL, HttpStatus.BAD_REQUEST, MessageKeys.NO_KEY, "ServiceMessage text");
		addTestMessages();
	}

	private void addTestMessages() {
		testMessages.add(infoMessage);
		testMessages.add(warnMessage);
		testMessages.add(errorMessage);
		testMessages.add(fatalMessage);
	}

	@Test
	public void testAddMessageWithNullMessages() {

		mockServiceResponse.addMessage(MessageSeverity.ERROR, HttpStatus.BAD_REQUEST,
				new ConstraintParam[] { new ConstraintParam("pName", "pValue") }, MessageKeys.NO_KEY, new String[] {});

		assertNotNull(mockServiceResponse.getMessages());
		assertEquals(1, mockServiceResponse.getMessages().size());

	}

	@Test
	public void testAddMessageWithParams() {
		mockServiceResponse.addMessage(MessageSeverity.ERROR, HttpStatus.BAD_REQUEST,
				new ConstraintParam[] { new ConstraintParam("pName", "pValue") }, MessageKeys.NO_KEY, new String[] {});
		assertNotNull(mockServiceResponse.getMessages());
		assertEquals(1, mockServiceResponse.getMessages().size());

	}

	@After
	public void tearDown() throws Exception {
		testMessages.clear();
	}

	@Test
	public void testAddMessage() {
		mockServiceResponse.addMessage(MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, MessageKeys.NO_KEY, new String[] {});
		assertNotNull(mockServiceResponse.getMessages());
		assertEquals(1, mockServiceResponse.getMessages().size());

	}

	@Test
	public void testAddMessages() {
		mockServiceResponse.addMessages(testMessages);
		assertNotNull(mockServiceResponse.getMessages());
		assertEquals(4, mockServiceResponse.getMessages().size());
	}

	@Test
	public void testSetMessages() {
		mockServiceResponse.addMessages(testMessages);
		DomainResponse serviceResponseForEqualsTest = new DomainResponse();
		assertFalse(mockServiceResponse.getMessages().equals(serviceResponseForEqualsTest.getMessages()));
		assertNotNull(mockServiceResponse.getMessages().equals(serviceResponseForEqualsTest.getMessages()));
		assertNotNull(mockServiceResponse.getMessages());
		assertEquals(4, mockServiceResponse.getMessages().size());
	}

	@Test
	public void testHasFatals() {
		mockServiceResponse.addMessages(testMessages);
		assertTrue(mockServiceResponse.hasFatals());
	}

	@Test
	public void testHasErrors() {
		mockServiceResponse.addMessages(testMessages);
		assertTrue(mockServiceResponse.hasErrors());
	}

	@Test
	public void testHasWarnings() {
		mockServiceResponse.addMessages(testMessages);
		assertTrue(mockServiceResponse.hasWarnings());
	}

	@Test
	public void testHasInfos() {
		mockServiceResponse.addMessages(testMessages);
		assertTrue(mockServiceResponse.hasInfos());
	}

}
