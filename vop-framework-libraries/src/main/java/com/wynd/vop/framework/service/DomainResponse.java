package com.wynd.vop.framework.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wynd.vop.framework.messages.ConstraintParam;
import com.wynd.vop.framework.messages.MessageKey;
import com.wynd.vop.framework.messages.MessageSeverity;
import com.wynd.vop.framework.messages.ServiceMessage;
import com.wynd.vop.framework.transfer.AbstractResponseObject;
import com.wynd.vop.framework.transfer.DomainTransferObjectMarker;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * A base Response object capable of representing the payload of a service response.
 */
public class DomainResponse extends AbstractResponseObject implements DomainTransferObjectMarker, Serializable {
	private static final long serialVersionUID = -3937937807439785385L;

	/** The serviceMessages. */
	private List<ServiceMessage> serviceMessages;

	/** Whether the response should be cached or not */
	@JsonIgnore // don't serialize into the response entity
	private boolean doNotCacheResponse = false;

	/**
	 * Instantiates a new service response.
	 */
	public DomainResponse() {
		super();
	}

	/**
	 * Adds a {@link ServiceMessage} to the serviceMessages list on the response.
	 * <p>
	 * Messages made with this constructor CANNOT be used in a JSR303 context
	 * because there is no way to communicate constraint message parameters to a
	 * JSR303 implementation.
	 *
	 * @param severity - the severity of the message
	 * @param httpStatus - the http status associated with the message
	 * @param key - the key "code" for support calls
	 * @param params - arguments to fill in any params in the MessageKey message (e.g. value for {0})
	 */
	public final void addMessage(final MessageSeverity severity, final HttpStatus httpStatus, final MessageKey key,
			final String... params) {
		if (serviceMessages == null) {
			serviceMessages = new LinkedList<>();
		}
		serviceMessages.add(new ServiceMessage(severity, httpStatus, key, params));
	}

	/**
	 * Adds a {@link ServiceMessage} to the serviceMessages list on the response.
	 * <p>
	 * Messages made with this constructor CAN be used in a JSR303 context
	 * due to the inclusion of the {@link ConstraintParam} array. The array
	 * is the means of communicating constraint message parameters to the
	 * JSR303 implementation.
	 *
	 * @param severity the severity of the message
	 * @param httpStatus the http status associated with the message
	 * @param constraintParams - an array of constraint parameters
	 * @param key the key "code" for support calls
	 * @param params - arguments to fill in any params in the MessageKey message (e.g. value for {0})
	 */
	public final void addMessage(final MessageSeverity severity, final HttpStatus httpStatus, final ConstraintParam[] constraintParams,
			final MessageKey key, final String... params) {
		if (serviceMessages == null) {
			serviceMessages = new LinkedList<>();
		}
		serviceMessages.add(new ServiceMessage(severity, httpStatus, constraintParams, key, params));
	}

	/**
	 * Adds all serviceMessages.
	 *
	 * @param newMessages the newMessages
	 */
	public final void addMessages(final List<ServiceMessage> newMessages) {
		if (serviceMessages == null) {
			serviceMessages = new LinkedList<>();
		}
		serviceMessages.addAll(newMessages);
	}

	/**
	 * Gets the list of ServiceMessages.
	 *
	 * @return the serviceMessages
	 */
	public final List<ServiceMessage> getMessages() {
		if (serviceMessages == null) {
			serviceMessages = new LinkedList<>();
		}
		return this.serviceMessages;
	}

	/**
	 * Checks for serviceMessages of severity type.
	 *
	 * @param severity the severity
	 * @return true, if successful
	 */
	@Override
	protected boolean hasMessagesOfType(final MessageSeverity severity) {
		for (final ServiceMessage serviceMessage : getMessages()) {
			if (severity.equals(serviceMessage.getSeverity())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if is do not cache response.
	 *
	 * @return true, if is do not cache response
	 */
	public boolean isDoNotCacheResponse() {
		return doNotCacheResponse;
	}

	/**
	 * Sets the do not cache response.
	 *
	 * @param doNotCacheResponse the new do not cache response
	 */
	public void setDoNotCacheResponse(final boolean doNotCacheResponse) {
		this.doNotCacheResponse = doNotCacheResponse;
	}

}