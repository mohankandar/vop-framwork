package com.wynd.vop.framework.audit.model;

import com.wynd.vop.framework.audit.AuditableData;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * An {@link AuditableData} transfer object for sending any message list to the audit logger.
 *
 */
public class MessageAuditData implements Serializable, AuditableData {
	private static final long serialVersionUID = -6463691536690649662L;

	/* The request. */
	private transient List<String> message = Collections.emptyList();

	/**
	 * Gets the request that is being logged in the audit logs.
	 *
	 * @return the request
	 */
	public List<String> getMessage() {
		return message;
	}

	/**
	 * Set the message object to be logged in the audit logs.
	 *
	 * @param message
	 */
	public void setMessage(final List<String> message) {
		this.message = message;
	}

	/**
	 * Manually formatted JSON-like string of key/value pairs.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return "MessageAuditData{" + (message == null ? "" : ReflectionToStringBuilder.toString(message)) + '}';
	}
}
