package com.wynd.vop.framework.autoconfigure.feign;

import feign.Request;
import feign.Response;
import feign.Response.Body;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class FeignCustomErrorDecoderTest {

	private static final String TEST_STREAM = "{\n" + "  \"messages\": [\n" + "    {\n"
			+ "      \"timestamp\": \"2019-03-08T20:11:37.334\",\n" + "      \"key\": \"BAD_REQUEST\",\n"
			+ "      \"severity\": \"ERROR\",\n" + "      \"status\": \"404\",\n"
			+ "      \"text\": \"participantID: PersonInfoRequest.participantID cannot be zero\"\n" + "    }\n"
			+ "  ]\n" + "}";

	private static final String TEST_URL = "test_url";

	private static final String TEST_CONTENT = "test_content";

	Map<String, Collection<String>> headers = new HashMap<String, Collection<String>>();

	@Mock
	Body body;

	Body nullBody = null;

	Reader reader;

	@Before
	public void setup() {
		headers.put("test_header_key", Arrays.asList(new String[] { "test_header_value" }));
		reader = new StringReader(TEST_STREAM);
	}

	@Test
	public void decodeTestForNullBodyWithJSONException() {

		FeignCustomErrorDecoder feignCustomErrorDecoder = new FeignCustomErrorDecoder();

		Response responseForNullBody = Response.builder().status(401).body(nullBody).headers(headers).request(Request
				.create(Request.HttpMethod.GET, TEST_URL, headers, TEST_CONTENT.getBytes(), Charset.defaultCharset()))
				.build();

		final Exception feignException = feignCustomErrorDecoder.decode("test.key", responseForNullBody);
		assertTrue(feignException instanceof Exception);
	}

	@Test
	public void decodeTestFor404Error() {

		FeignCustomErrorDecoder feignCustomErrorDecoder = new FeignCustomErrorDecoder();
		try {
			when(body.asReader(StandardCharsets.UTF_8)).thenReturn(reader);
		} catch (IOException e) {
			fail("unable to stub asReader() method in Body object");
		}
		Response response = Response.builder().status(404).body(body).headers(headers).request(Request
				.create(Request.HttpMethod.GET, TEST_URL, headers, TEST_CONTENT.getBytes(), Charset.defaultCharset()))
				.build();
		try {
			feignCustomErrorDecoder.decode("test.key", response);
		} catch (Exception e) {
			fail("Exception should not be thrown while calling decode() method in FeignCustomErrorDecoder object");
		}
	}

	@Test
	public void decodeTestFor404ErrorWithIOException() {

		FeignCustomErrorDecoder feignCustomErrorDecoder = new FeignCustomErrorDecoder();
		try {
			when(body.asReader()).thenThrow(new IOException());
		} catch (IOException e) {
			fail("unable to stub asReader() method in Body object");
		}
		Response response = Response.builder().status(404).body(body).headers(headers).request(Request
				.create(Request.HttpMethod.GET, TEST_URL, headers, TEST_CONTENT.getBytes(), Charset.defaultCharset()))
				.build();
		try {
			assertTrue(feignCustomErrorDecoder.decode("test.key", response) instanceof Exception);
			fail("Exception should be thrown while calling decode() method in FeignCustomErrorDecoder object");
		} catch (Exception e) {
			assertNotNull(e); // exception should be thrown
		}
	}

	@Test
	public void decodeTestFor404ErrorWithJSONException() {

		FeignCustomErrorDecoder feignCustomErrorDecoder = new FeignCustomErrorDecoder();
		try {
			reader = new StringReader("Invalid JSON String");
			when(body.asReader()).thenReturn(reader);
		} catch (IOException e) {
			fail("unable to stub asReader() method in Body object");
		}
		Response response = Response.builder().status(404).body(body).headers(headers).request(Request
				.create(Request.HttpMethod.GET, TEST_URL, headers, TEST_CONTENT.getBytes(), Charset.defaultCharset()))
				.build();
		try {
			feignCustomErrorDecoder.decode("test.key", response);
			fail("Exception should be thrown while calling decode() method in FeignCustomErrorDecoder object");
		} catch (Exception e) {
			assertNotNull(e); // exception should be thrown
		}
	}

	@Test
	public void decodeTestFor500Error() {
		FeignCustomErrorDecoder feignCustomErrorDecoder = new FeignCustomErrorDecoder();
		try {
			when(body.asInputStream()).thenReturn(new ByteArrayInputStream(TEST_STREAM.getBytes()));
		} catch (IOException e) {
			fail("unable to stub asReader() method in Body object");
		}
		Response response = Response.builder().status(500).body(body).headers(headers).request(Request
				.create(Request.HttpMethod.GET, TEST_URL, headers, TEST_CONTENT.getBytes(), Charset.defaultCharset()))
				.build();

		try {
			assertTrue(feignCustomErrorDecoder.decode("test.key", response) instanceof Exception);
		} catch (Exception e) {
			fail("Exception should not be thrown while calling decode() method in FeignCustomErrorDecoder object");
		}
	}
}
