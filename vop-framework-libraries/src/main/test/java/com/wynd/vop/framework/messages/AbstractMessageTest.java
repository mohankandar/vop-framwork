package com.wynd.vop.framework.messages;

import org.junit.Test;

import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.assertTrue;

public class AbstractMessageTest {

	private static final java.util.Date TIME = Date.from(Instant.now());
	private static final String TEST_VALUE = "value";
	private static final String TEST_NAME = "name";

	@Test
	public final void testGettersAndSetters() {
		ConstraintParam[] constraintParams = new ConstraintParam[] { new ConstraintParam(TEST_NAME, TEST_VALUE) };
		AbstractMessage message = new AbstractMessage(constraintParams) {
			private static final long serialVersionUID = 1L;
		};

		assertTrue(message.getConstraintParams().equals(constraintParams));
		assertTrue(message.getParamCount().equals(1));
		message.setTimestamp(TIME);
		assertTrue(message.getTimestamp().equals(TIME));
	}

	@Test
	public final void testGetParamCountWithNullConstraintParameters() {
		AbstractMessage message = new AbstractMessage(null) {
			private static final long serialVersionUID = 1L;
		};

		assertTrue(message.getParamCount().equals(0));
	}
}
