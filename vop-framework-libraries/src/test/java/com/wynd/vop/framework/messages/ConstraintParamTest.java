package com.wynd.vop.framework.messages;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ConstraintParamTest {

	private static final String TEST_VALUE = "value";
	private static final String TEST_NAME = "name";
	private static final String TEST_NEW_NAME = "new name";
	private static final String TEST_NEW_VALUE = "new value";

	@Test
	public final void testGettersAndSetters() {
		ConstraintParam params = new ConstraintParam(TEST_NAME, TEST_VALUE);
		assertTrue(params.getName().equals(TEST_NAME));
		assertTrue(params.getValue().equals(TEST_VALUE));
		params.setName(TEST_NEW_NAME);
		params.setValue(TEST_NEW_VALUE);
		assertTrue(params.getName().equals(TEST_NEW_NAME));
		assertTrue(params.getValue().equals(TEST_NEW_VALUE));
	}

}
