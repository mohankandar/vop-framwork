package com.wynd.vop.framework.exception;

import com.wynd.vop.framework.messages.MessageKey;
import com.wynd.vop.framework.messages.MessageSeverity;
import org.springframework.http.HttpStatus;

/**
 * The root VOP class for managing <b>checked</b> exceptions to be thrown from
 * <i>Partner Clients</i>. A Partner Client could be a separate project/jar that is
 * included in the micro-service, or a packaged layer in the service project.
 * <p>
 * To support the consistency in partner responses, all VOP partner jars/packages
 * that throw checked exceptions should throw this class, or a sub-class of this class.
 *
 * @see VopException
 *

 */
public class VopPartnerException extends VopException {
	private static final long serialVersionUID = -1657198082980424519L;

	/**
	 * Constructs a new <b>checked</b> Exception indicating a problem occurred in the external partner
	 * that requires the application to make a decision.
	 * Examples could include scenarios like "requested data not found", "input data malformed",
	 * etc.
	 *
	 * @see VopException#VopException(String, String, MessageSeverity, HttpStatus)
	 *
	 * @param key - the consumer-facing key that can uniquely identify the nature of the exception
	 * @param severity - the severity of the event: FATAL (500 series), ERROR (400 series), WARN (200 series), or INFO/DEBUG/TRACE
	 * @param status - the HTTP Status code that applies best to the encountered problem, see
	 *            <a href="https://tools.ietf.org/html/rfc7231">https://tools.ietf.org/html/rfc7231</a>
	 * @param params - arguments to fill in any params in the MessageKey message (e.g. value for {0})
	 */
	public VopPartnerException(final MessageKey key, final MessageSeverity severity, final HttpStatus status, final String... params) {
		super(key, severity, status, params);
	}

	/**
	 * Constructs a new <b>checked</b> Exception indicating a problem occurred in the external partner
	 * that requires the application to make a decision.
	 * Examples could include scenarios like "requested data not found", "input data malformed",
	 * etc.
	 *
	 * @see VopException#VopException(String, String, MessageSeverity, HttpStatus, Throwable)
	 *
	 * @param key - the consumer-facing key that can uniquely identify the nature of the exception
	 * @param severity - the severity of the event: FATAL (500 series), ERROR (400 series), WARN (200 series), or INFO/DEBUG/TRACE
	 * @param status - the HTTP Status code that applies best to the encountered problem, see
	 *            <a href="https://tools.ietf.org/html/rfc7231">https://tools.ietf.org/html/rfc7231</a>
	 * @param cause - the throwable that caused this throwable
	 * @param params - arguments to fill in any params in the MessageKey message (e.g. value for {0})
	 */
	public VopPartnerException(final MessageKey key, final MessageSeverity severity, final HttpStatus status, final Throwable cause, final String... params) {
		super(key, severity, status, cause, params);
	}
}
