package com.wynd.vop.framework.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Utility class for HTTP Headers in VOP framework
 */
public final class HttpHeadersUtil {

	/**
	 * Constructor to prevent instantiation.
	 */
	private HttpHeadersUtil() {
		throw new IllegalAccessError("HttpHeadersUtil is a static class. Do not instantiate it.");
	}

	/**
	 * Builds an http header specifically for returning errors,
	 * by setting the content-type to application/problem+json.
	 *
	 * @return the http headers
	 */
	public static HttpHeaders buildHttpHeadersForError() {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
		return responseHeaders;
	}
}
