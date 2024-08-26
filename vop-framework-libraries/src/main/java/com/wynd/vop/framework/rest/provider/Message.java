package com.wynd.vop.framework.rest.provider;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wynd.vop.framework.messages.MessageSeverity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * Message model object used to return messages (errors, warnings, etc) to the service consumer.
 */
public class Message implements Serializable {
	private static final long serialVersionUID = -8835969328009728923L;

	/**
	 * The text is excluded from equals and hash as the key+severity are to jointly indicate a unique message. The text is supplemental
	 * information.
	 */
	private static final String[] EQUALS_HASH_EXCLUDE_FIELDS = new String[] { "text" };

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private Date timestamp; // NOSONAR cannot be final

	/** The key. */
	@NotNull
	private String key;

	/** The message severity. */
	@NotNull
	private String severity;

	/** The Http status. */
	private Integer status;

	/** The message. */
	private String text;

	/**
	 * Instantiates a new message.
	 */
	public Message() { // NOSONAR @NotNull is a validation annotation, not a usage annotation
		this.timestamp = new Date();
	} // NOSONAR @NotNull is a validation annotation, not a usage annotation

	/**
	 * Instantiates a new message.
	 * <p>
	 * Severity <b>must</b> be a case-sensitive match for a member of the {@link MessageSeverity} enum.
	 * <br/>
	 * Key <b>must</b> match a key from [TBD] properties, as declared in {@link} swagger constants class.
	 * <br/>
	 * HttpStatus <b>must</b> match an int value contained in the {@link HttpStatus} enum.
	 *
	 * @param severity the severity for the cause of the message
	 * @param key the key representing the "error code" for the message
	 * @param text the text of the message
	 * @param httpStatus the http status associated with the cause of the message
	 */
	public Message(final String severity, final String key, final String text, final Integer httpStatus) {
		this();
		this.severity = severity;
		this.key = key;
		this.text = text;
		this.status = httpStatus;
	}

	/**
	 * Gets the timestamp to be part of message payload.
	 *
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp for the message payload.
	 *
	 * @param timestamp the new timestamp
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	public final String getKey() {
		return this.key;
	}

	/**
	 * Sets the key.
	 *
	 * @param key the new key
	 */
	public final void setKey(final String key) {
		this.key = key;
	}

	/**
	 * Gets the Http status code.
	 *
	 * @return the Http status code
	 */
	public String getStatus() {
		// Since this method is used by introspection based serialisation, it would need to return the status code number instead of
		// the default (enum name), which is why the toString() method is used
		return status == null ? null : status.toString();
	}

	/**
	 * Sets the HttpStatus.
	 *
	 * @param status the new HttpStatus
	 */
	public void setStatus(final Integer status) {
		this.status = status;
	}

	/**
	 * Gets the text.
	 *
	 * @return the text
	 */
	public final String getText() {
		return this.text;
	}

	/**
	 * Sets the text.
	 *
	 * @param text the new text
	 */
	public final void setText(final String text) {
		this.text = text;
	}

	/**
	 * Gets the message severity.
	 *
	 * @return the message severity
	 */
	public final String getSeverity() {
		return this.severity;
	}

	/**
	 * Sets the message severity.
	 *
	 * @param severity the new message severity
	 */
	public final void setSeverity(final String severity) {
		this.severity = severity;
	}

	/**
	 * Get the {@link HttpStatus} enumeration of this message
	 * If the status is null, then this method returns
	 * 201 {@link HttpStatus#CREATED}.
	 *
	 * @return the HttpStatus
	 */
	public final HttpStatus getHttpStatus() {
		if (status == null) {
			return null;
		}
		return HttpStatus.resolve(status);
	}

	/**
	 * Get the {@link HttpStatus} enumeration for the status Integer.
	 * If the status integer is null, then this method returns
	 * 201 {@link HttpStatus#CREATED}.
	 *
	 * @param status the integer status
	 * @return the HttpStatus
	 */
	public final HttpStatus getHttpStatus(Integer status) {
		if (status == null) {
			return HttpStatus.CREATED;
		}
		return HttpStatus.resolve(status);
	}

	/*
	 * (non-Javadoc)
	 *
	 */
	@Override
	public final boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, EQUALS_HASH_EXCLUDE_FIELDS);
	}

	/*
	 * (non-Javadoc)
	 *
	 */
	@Override
	public final int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, EQUALS_HASH_EXCLUDE_FIELDS);
	}

	/**
	 * Returns a String that shows the full content of the Message.
	 */
	@Override
	public final String toString() {
		return severity + " " + key + "(" + status + "): " + text;
	}

}
