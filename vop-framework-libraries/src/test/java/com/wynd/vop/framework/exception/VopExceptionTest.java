package com.wynd.vop.framework.exception;

import com.wynd.vop.framework.messages.MessageKey;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class VopExceptionTest {
	private static final String TEST_VALUE = "test value";
	private static final MessageKey TEST_KEY = MessageKeys.NO_KEY;

	@Test
	public void initializeVopExceptionTest() {
		assertNotNull(new VopException(TEST_KEY, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST));
		assertNotNull(new VopException(TEST_KEY, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, new Exception("wrapped message")));
		assertNotNull(new VopException(null, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, new Exception("wrapped message")));
	}

	@Test
	public void getterTest() {
		String[] params = new String[] { "param1", "param2" };
		VopException vopException =
				new VopException(TEST_KEY, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST,
						new Exception("wrapped message"), params);
		assertTrue(vopException.getExceptionData().getKey().equals(TEST_KEY.getKey()));
		assertTrue(vopException.getMessage().equals(TEST_KEY.getKey()));
		assertTrue(vopException.getExceptionData().getSeverity().equals(MessageSeverity.ERROR));
		assertTrue(vopException.getExceptionData().getParams().equals(params));
		assertTrue(vopException.getExceptionData().getStatus().equals(HttpStatus.BAD_REQUEST));
		System.setProperty("server.name", TEST_VALUE);
		assertTrue(vopException.getExceptionData().getServerName().equals(System.getProperty("server.name")));
	}

}
