package com.wynd.vop.framework.exception;

import com.wynd.vop.framework.messages.MessageKey;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class VopFeignRuntimeExceptionTest {

	private static final MessageKey TEST_KEY = MessageKeys.NO_KEY;
	private static final String TEST_TEXT = "NO_KEY";
	private static final HttpStatus TEST_HTTP_STATUS = HttpStatus.BAD_REQUEST;
	private static final MessageSeverity TEST_SEVERITY = MessageSeverity.ERROR;

	@Test
	public void instantiateBipFeignRuntimeExceptionTest() throws Exception {
		VopFeignRuntimeException vopFeignRuntimeException =
				new VopFeignRuntimeException(TEST_KEY, TEST_SEVERITY, TEST_HTTP_STATUS);

		Assert.assertTrue(vopFeignRuntimeException.getExceptionData().getKey().equals(TEST_KEY.getKey()));
		Assert.assertTrue(vopFeignRuntimeException.getMessage().equals(TEST_TEXT));
		Assert.assertTrue(vopFeignRuntimeException.getExceptionData().getStatus().equals(TEST_HTTP_STATUS));
		Assert.assertTrue(vopFeignRuntimeException.getExceptionData().getSeverity().equals(TEST_SEVERITY));
	}

	@Test
	public void instantiateUsingOtherConstructorBipFeignRuntimeExceptionTest() throws Exception {
		VopFeignRuntimeException vopFeignRuntimeException =
				new VopFeignRuntimeException(TEST_KEY, TEST_SEVERITY,
						TEST_HTTP_STATUS, new Exception("test wrapped error"));

		Assert.assertTrue(vopFeignRuntimeException.getExceptionData().getKey().equals(TEST_KEY.getKey()));
		Assert.assertTrue(vopFeignRuntimeException.getMessage().equals(TEST_TEXT));
		Assert.assertTrue(vopFeignRuntimeException.getExceptionData().getStatus().equals(TEST_HTTP_STATUS));
		Assert.assertTrue(vopFeignRuntimeException.getExceptionData().getSeverity().equals(TEST_SEVERITY));
	}

}
