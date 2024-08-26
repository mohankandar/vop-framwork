package com.wynd.vop.framework.swagger;

import org.junit.Assert;
import org.junit.Test;

public class SwaggerResponseMessagesTest {

	private static final String RESPONSE_200_MESSAGE = "A Response which indicates a successful Request.  "
			+ "Response may contain \"messages\" that could describe warnings or further information.";

	private static final String RESPONSE_400_MESSAGE = "There was an error encountered processing the Request.  "
			+ "Response will contain \"messages\" element with additional information on the error.  "
			+ "This request shouldn't be retried until corrected.";

	private static final String RESPONSE_401_MESSAGE = "The request is not authorized.  "
			+ "Please verify credentials in the request. "
			+ "Response will contain \"messages\" element with additional information on the error.";

	private static final String RESPONSE_403_MESSAGE = "Access to the resource is Forbidden.  "
			+ "Please verify if you have permission to access this resource. "
			+ "Response will contain \"messages\" element with additional information on the error.";

	private static final String RESPONSE_500_MESSAGE = "There was an error encountered processing the Request. "
			+ "Response will contain \"messages\" element with additional information on the error. Please retry. "
			+ "If problem persists, please contact support with a copy of the Response.";

	public static String getResponse200Message() {
		return RESPONSE_200_MESSAGE;
	}

	public static String getResponse400Message() {
		return RESPONSE_400_MESSAGE;
	}

	public static String getResponse401Message() {
		return RESPONSE_401_MESSAGE;
	}

	public static String getResponse403Message() {
		return RESPONSE_403_MESSAGE;
	}

	public static String getResponse500Message() {
		return RESPONSE_500_MESSAGE;
	}

	@Test
	public void response200MessageTextTest() throws Exception {
		Assert.assertEquals(SwaggerResponseMessages.MESSAGE_200, getResponse200Message());
	}

	@Test
	public void response401MessageTextTest() throws Exception {
		Assert.assertEquals(SwaggerResponseMessages.MESSAGE_401, getResponse401Message());
	}

	@Test
	public void response403MessageTextTest() throws Exception {
		Assert.assertEquals(SwaggerResponseMessages.MESSAGE_403, getResponse403Message());
	}

	@Test
	public void response400MessageTextTest() throws Exception {
		Assert.assertEquals(SwaggerResponseMessages.MESSAGE_400, getResponse400Message());
	}

	@Test
	public void response500MessageTextTest() throws Exception {
		Assert.assertEquals(SwaggerResponseMessages.MESSAGE_500, getResponse500Message());
	}

}
