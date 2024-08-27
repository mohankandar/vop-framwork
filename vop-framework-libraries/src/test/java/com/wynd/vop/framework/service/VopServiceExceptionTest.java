package com.wynd.vop.framework.service;

import com.wynd.vop.framework.messages.MessageKey;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class VopServiceExceptionTest {

	private static final String TEST_KEY_MESSAGE = "NO_KEY";
	private static final MessageKey TEST_KEY = MessageKeys.NO_KEY;

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEmptyConstructor() {
		VopServiceException vopServiceException = new VopServiceException(TEST_KEY, null, null, null, new String[] { null });
		assertTrue(vopServiceException.getMessage().equals(TEST_KEY_MESSAGE));
		assertNull(vopServiceException.getExceptionData().getSeverity());
		assertNull(vopServiceException.getExceptionData().getStatus());
		assertNull(vopServiceException.getCause());
	}

	@Test
	public void testPopulatedConstructor() {
		VopServiceException vopServiceException =
				new VopServiceException(TEST_KEY, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST,
						new Throwable("test"));
		assertNotNull(vopServiceException.getExceptionData().getKey());
		assertNotNull(vopServiceException.getMessage());
		assertNotNull(vopServiceException.getExceptionData().getSeverity());
		assertNotNull(vopServiceException.getExceptionData().getStatus());
		assertNotNull(vopServiceException.getCause());
	}

	@Test
	public void testPopulatedNoCauseConstructor() {
		VopServiceException vopServiceException =
				new VopServiceException(TEST_KEY, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST);
		assertNotNull(vopServiceException.getExceptionData().getKey());
		assertNotNull(vopServiceException.getMessage());
		assertNotNull(vopServiceException.getExceptionData().getSeverity());
		assertNotNull(vopServiceException.getExceptionData().getStatus());
		assertNull(vopServiceException.getCause());
	}

}
