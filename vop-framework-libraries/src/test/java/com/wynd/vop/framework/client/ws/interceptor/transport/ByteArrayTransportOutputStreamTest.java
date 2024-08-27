package com.wynd.vop.framework.client.ws.interceptor.transport;

import org.apache.commons.codec.binary.StringUtils;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ByteArrayTransportOutputStreamTest {

	/** System new-line character */
	private static final String NEW_LINE = System.getProperty("line.separator");

	@Test
	public void addHeaderTest() {
		ByteArrayTransportOutputStream stream = new ByteArrayTransportOutputStream();
		try {
			stream.createOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unable to create output stream");
		}
		try {
			stream.addHeader("test_header_name", "test_header_value");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unable to add header to output stream");
		}
		assertTrue(StringUtils.newStringUtf8(stream.toByteArray()).equals("test_header_name: test_header_value" + NEW_LINE));
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unable to close output stream");

		}
	}
}
