package com.wynd.vop.framework.exception;

import com.wynd.vop.framework.messages.MessageKey;
import com.wynd.vop.framework.messages.MessageKeys;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class VopRuntimeExceptionTest {

	private static final String TEST_KEY_MESSAGE = "NO_KEY";
	private static final MessageKey TEST_KEY = MessageKeys.NO_KEY;
	private static final String SERVER_NAME_PROPERTY = "server.name";

	@Before
	public void setUp() {
		System.setProperty(SERVER_NAME_PROPERTY, "Test Server");
	}

	@BeforeClass
	public static void setUpClass() {
		System.setProperty(SERVER_NAME_PROPERTY, "Test Server");
	}

	@Test
	public void getMessageTestServerNameNull() throws Exception {
		VopRuntimeException vopRuntimeException =
				new VopRuntimeException(TEST_KEY, null, null, (String[]) null);
		Assert.assertTrue(vopRuntimeException.getExceptionData().getServerName().equals(System.getProperty("server.name")));
	}

	@Test
	public void getMessageTestCategoryNull() throws Exception {
		VopRuntimeException vopRuntimeException = new VopRuntimeException(TEST_KEY, null, null, (String[]) null);
		Assert.assertTrue(vopRuntimeException.getMessage().equals(TEST_KEY_MESSAGE));
	}

	@Test
	public void getMessageCauseAndMessageTest() throws Exception {
		Throwable cause = new Throwable("test");
		VopRuntimeException vopRuntimeException = new VopRuntimeException(TEST_KEY, null, null, cause);
		Assert.assertTrue(vopRuntimeException.getMessage().equals(TEST_KEY_MESSAGE));
	}

	@Test
	public void initializationWithNullKeyTest() {
		assertNotNull(new VopRuntimeException(null, null, null, (String[]) null));
	}
}
