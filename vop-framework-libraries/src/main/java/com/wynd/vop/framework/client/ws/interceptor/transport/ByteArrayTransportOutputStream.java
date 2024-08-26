package com.wynd.vop.framework.client.ws.interceptor.transport;

import org.springframework.ws.transport.TransportOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream for writing spring WebServiceMessages to XML.
 * 

 */
public class ByteArrayTransportOutputStream extends TransportOutputStream {

	/** System new-line character */
	private static final String NEW_LINE = System.getProperty("line.separator");

	/** Local storage of the outputstream */
	private ByteArrayOutputStream byteArrayOutputStream;

	/**
	 * Instantiate the ByteArrayTransportOutputStream class.
	 */
	public ByteArrayTransportOutputStream() {
		super();
	}

	@Override
	public void addHeader(final String name, final String value) throws IOException {
		createOutputStream();
		String header = name + ": " + value + NEW_LINE;
		byteArrayOutputStream.write(header.getBytes());
	}

	@Override
	protected OutputStream createOutputStream() throws IOException {
		if (byteArrayOutputStream == null) {
			byteArrayOutputStream = new ByteArrayOutputStream();
		}
		return byteArrayOutputStream;
	}

	/**
	 * Returns the underlying ByteArrayOutputStream as a byte array.
	 * 
	 * @return byte[]
	 */
	public byte[] toByteArray() {
		return byteArrayOutputStream.toByteArray();
	}
}
