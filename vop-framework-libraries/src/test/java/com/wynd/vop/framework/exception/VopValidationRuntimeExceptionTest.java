package com.wynd.vop.framework.exception;

import com.wynd.vop.framework.messages.MessageKey;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertNotNull;

public class VopValidationRuntimeExceptionTest {


	private static final MessageKey TEST_KEY = MessageKeys.NO_KEY;

	@Test
	public void initializeVopValidationRuntimeExceptionTest() {
		assertNotNull(new VopValidationRuntimeException(TEST_KEY, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST));
		assertNotNull(new VopValidationRuntimeException(TEST_KEY, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST,
				new Exception()));
	}
}
