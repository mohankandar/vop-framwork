package com.wynd.vop.framework.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;

/**
 * ServiceMessage is a generic abstraction of a "message" or "notification" which is layer agnostic and can be used to communicate
 * status or
 * other sorts of information during method calls between components/layers. This is serializable and can be used in SOAP or REST
 * calls.
 *

 */
public class ServiceMessage extends AbstractMessage {

	/** The Constant serialVersionUID */
	private static final long serialVersionUID = -1711431368372127555L;

	/**
	 * The text is excluded from equals and hash as the key+severity are to jointly indicate a unique message. The text is supplemental
	 * information
	 */
	private static final String[] EQUALS_HASH_EXCLUDE_FIELDS = new String[] { "text" };

	/** The key */
	@XmlElement(required = true)
	@NotNull
	private String key = MessageKeys.NO_KEY.getKey();

	/** The message, with values already replaced for any replaceable parameters */
	private String text;
	/** String representation of the {@link HttpStatus#value()} */
	private String status;

	/** The message key enum */
	@JsonIgnore
	private MessageKey messageKey;

	/** The replaceable parameters for the message key */
	@JsonIgnore
	private String[] messageParams;

	/** The Http status enum */
	@JsonIgnore
	private HttpStatus httpStatus;

	/** The message severity enum */
	@XmlElement(required = true)
	@NotNull
	private MessageSeverity severity;

	/**
	 * Instantiates a new message.
	 *
	 * @param severity - the severity for the cause of the message
	 * @param key - the key representing the "error code" for the message
	 * @param httpStatus - the http status associated with the cause of the message
	 * @param params - arguments to fill in any params in the MessageKey message (e.g. value for {0})
	 */
	public ServiceMessage(final MessageSeverity severity, final HttpStatus httpStatus, final MessageKey key, final String... params) {
		this(severity, httpStatus, null, key, params);
	}

	/**
	 * Instantiates a new message, populating all available fields.
	 *
	 * @param severity - the severity for the cause of the message
	 * @param httpStatus - the http status associated with the cause of the message
	 * @param constraintParams - an array of constraint parameters
	 * @param key - the key representing the "error code" for the message
	 * @param params - arguments to fill in any params in the MessageKey message (e.g. value for {0})
	 */
	public ServiceMessage(final MessageSeverity severity, final HttpStatus httpStatus,
			final ConstraintParam[] constraintParams, final MessageKey key, final String... params) {
		super(constraintParams); // always call super() to set the timestamp
		this.severity = severity;
		this.httpStatus = httpStatus;
		this.messageKey = key;
		this.messageParams = params;

		if (key != null) {
			this.key = key.getKey();
		}
		this.text = key == null ? null : key.getMessage(params);
		this.status = httpStatus == null ? null : Integer.toString(httpStatus.value());
	}

	/**
	 * The property key as a String.
	 *
	 * @return the key
	 */
	public final String getKey() {
		return this.key;
	}

	/**
	 * The HttpStatus code as a String.
	 *
	 * @return String - the http status code
	 */
	@JsonProperty("status")
	@JsonCreator
	public String getStatus() {
		// Since this method is used by introspection based serialisation, it would need to return the status code number instead of
		// the default (enum name), which is why the toString() method is used
		status = httpStatus == null ? null : String.valueOf(httpStatus.value());
		return status;
	}

	/**
	 * The HttpStatus enum for the message.
	 *
	 * @return HttpStatus
	 */
	@JsonIgnore
	@JsonProperty(value = "httpStatus")
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	/**
	 * Gets the text of the message.
	 *
	 * @return the text
	 */
	public final String getText() {
		return this.text;
	}

	/**
	 * Gets the message severity.
	 *
	 * @return the message severity
	 */
	public final MessageSeverity getSeverity() {
		return this.severity;
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

}
