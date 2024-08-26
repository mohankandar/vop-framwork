package com.wynd.vop.framework.exception;

import com.wynd.vop.framework.validation.Validator;
import com.wynd.vop.framework.messages.MessageKey;
import com.wynd.vop.framework.messages.MessageSeverity;
import org.springframework.http.HttpStatus;

/**
 * The root VOP class for validation or defense <b>runtime</b> exceptions.
 * <p>
 * To support the consistency in validation errors, all VOP validation
 * exceptions should be this class, or a sub-class of this class.
 *
 * @see VopRuntimeException
 *

 */
public class VopValidationRuntimeException extends VopRuntimeException {
	private static final long serialVersionUID = -3876995562701933677L;

	/**
	 * Constructs a new <b>runtime</b> Exception indicating a validation or defense error.
	 *
	 * @see VopRuntimeException#BipRuntimeException(String, String, MessageSeverity, HttpStatus)
	 *
	 * @param key - the consumer-facing key that can uniquely identify the nature of the exception
	 * @param severity - the severity of the event: FATAL (500 series), ERROR (400 series), WARN (200 series), or INFO/DEBUG/TRACE
	 * @param status - the HTTP Status code that applies best to the encountered problem, see
	 *            <a href="https://tools.ietf.org/html/rfc7231">https://tools.ietf.org/html/rfc7231</a>
	 * @param params - arguments to fill in any params in the MessageKey message (e.g. value for {0})
	 */
	public VopValidationRuntimeException(final MessageKey key, final MessageSeverity severity, final HttpStatus status, final String... params) {
		super(key, severity, status, params);
	}

	/**
	 * Constructs a new <b>runtime</b> Exception indicating a {@link Validator}
	 * or defense error.
	 *
	 * @see VopRuntimeException#BipRuntimeException(String, String, MessageSeverity, HttpStatus, Throwable)
	 *
	 * @param key - the consumer-facing key that can uniquely identify the nature of the exception
	 * @param severity - the severity of the event: FATAL (500 series), ERROR (400 series), WARN (200 series), or INFO/DEBUG/TRACE
	 * @param status - the HTTP Status code that applies best to the encountered problem, see
	 *            <a href="https://tools.ietf.org/html/rfc7231">https://tools.ietf.org/html/rfc7231</a>
	 * @param cause - the throwable that caused this throwable
	 * @param params - arguments to fill in any params in the MessageKey message (e.g. value for {0})
	 */
	public VopValidationRuntimeException(final MessageKey key, final MessageSeverity severity, final HttpStatus status,
			final Throwable cause, final String... params) {
		super(key, severity, status, cause, params);
	}
}
