package com.wynd.vop.framework.client.ws;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.bind.ValidationEvent;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
@RunWith(MockitoJUnitRunner.class)
public class JaxbLogAndEatValidationEventHandlerTest {

	@Mock
	ValidationEvent mockEvent;
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testJaxbLogAndEatValidationEventHandler() {
		JaxbLogAndEatValidationEventHandler test = new JaxbLogAndEatValidationEventHandler(true);
		assertNotNull(test);
	}

	@Test
	public void testHandleEvent() {
		JaxbLogAndEatValidationEventHandler test = new JaxbLogAndEatValidationEventHandler(true);
		assertTrue(test.handleEvent(mockEvent));
	}

	@Test
	public void testHandleEventFalse() {
		JaxbLogAndEatValidationEventHandler test = new JaxbLogAndEatValidationEventHandler(false);
		assertTrue(test.handleEvent(mockEvent));
	}
}
