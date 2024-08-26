package com.wynd.vop.framework.audit.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The purpose of this class is to collect the audit data for a request before serializing it to the logs.
 */
@JsonInclude(Include.NON_NULL)
public class HttpRequestAuditData extends RequestAuditData {

	private static final long serialVersionUID = -6346123934909781965L;

	/* A map of the http headers on the request. */
	private Map<String, String> headers = Collections.emptyMap();

	/* The uri of the request. */
	private String uri;

	/* The http method. */
	private String method;

	private List<String> attachmentTextList;

	/**
	 * Gets the http headers.
	 *
	 * @return the headers
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}

	/**
	 * Sets the http headers.
	 *
	 * @param headers
	 */
	public void setHeaders(final Map<String, String> headers) {
		this.headers = headers;
	}

	/**
	 * Gets the request uri.
	 *
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the uri.
	 *
	 * @param uri
	 */
	public void setUri(final String uri) {
		this.uri = uri;
	}

	/**
	 * Gets the http method.
	 *
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Sets the http method.
	 *
	 * @param method
	 */
	public void setMethod(final String method) {
		this.method = method;
	}

	/**
	 * gets the attachmentTextList.
	 *
	 * @return the attachment text list
	 */
	public List<String> getAttachmentTextList() {
		return attachmentTextList;
	}

	/**
	 * sets the attachmentTextList.
	 *
	 * @param attachmentTextList
	 */
	public void setAttachmentTextList(final List<String> attachmentTextList) {
		this.attachmentTextList = attachmentTextList;
	}

	/**
	 * Manually formatted JSON-like string of key/value pairs.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return ("HttpRequestAuditData{" + "headers=" + (headers == null ? "" : ReflectionToStringBuilder.toString(headers)) + ", uri='"
				+ uri + "\'" + ", method='" + method + "', request='" + (getRequest() == null ? "[]" : getRequest().toString())
				+ "', attachmentTextList='" + attachmentTextList + "'}");
	}
}
