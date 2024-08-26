package com.wynd.vop.framework.audit.model;

import com.wynd.vop.framework.audit.AuditableData;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;

/**
 * An {@link AuditableData} transfer response object for sending any Object to the audit logger.
 *

 */
public class ResponseAuditData implements Serializable, AuditableData {
	private static final long serialVersionUID = -5812100176075217636L;

	/* The response. */
	private transient Object response;

	/**
	 * Gets the response.
	 *
	 * @return the response
	 */
	public Object getResponse() {
		return response;
	}

	/**
	 * Sets the response.
	 *
	 * @param response
	 */
	public void setResponse(final Object response) {
		this.response = response;
	}

	/**
	 * Manually formatted JSON-like string of key/value pairs.
	 */
	@Override
	public String toString() {
		return "ResponseAuditData{response=" + (getResponse() == null ? "" : ReflectionToStringBuilder.toString(getResponse())) + '}';
	}
}
