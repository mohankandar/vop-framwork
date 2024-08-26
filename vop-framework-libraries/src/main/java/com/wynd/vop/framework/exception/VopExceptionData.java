package com.wynd.vop.framework.exception;

import com.wynd.vop.framework.messages.MessageKey;
import com.wynd.vop.framework.messages.MessageSeverity;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * A Class to hold relevant information about an exception in the VOP framework.
 * 
 * Implied private properties are:
 * 
 * <ul>
 * <li>{@link MessageKey} key: the consumer-facing key that can uniquely identify the nature of the exception
 * <li>String[] params: parameters required to fill into the message format
 * <li>MessageSeverity severity: the severity of the event: FATAL (500 series), ERROR (400 series), WARN (200 series), or
 * INFO/DEBUG/TRACE
 * <li>HttpStatus status: the HTTP Status code that applies best to the encountered problem, see
 * <a href="https://tools.ietf.org/html/rfc7231">https://tools.ietf.org/html/rfc7231</a>
 * </ul>
 */
public class VopExceptionData implements Serializable {

	/**
	 * SerialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/** The consumer facing identity key */
	private final MessageKey key;
	/** Any values needed to fill in params (e.g. value for {0}) in the MessageKey message */
	private final String[] params;
	/** The severity of the event: FATAL (500 series), ERROR (400 series), WARN (200 series), or INFO/DEBUG/TRACE */
	private final MessageSeverity severity;
	/** The best-fit HTTP Status, see <a href="https://tools.ietf.org/html/rfc7231">https://tools.ietf.org/html/rfc7231</a> */
	private final HttpStatus status;

	public VopExceptionData(final MessageKey key, final MessageSeverity severity, final HttpStatus status, final String... params) {
		this.key = key;
		this.params = params;
		this.severity = severity;
		this.status = status;
	}

	public MessageKey getMessageKey() {
		return this.key;
	}

	/**
	 * The consumer-facing key that can uniquely identify the nature of the exception
	 *
	 * @return the key
	 */
	public String getKey() {
		return key.getKey();
	}

	/**
	 * Any objects that might be needed to fill in params in the message, e.g. the value for {0}.
	 *
	 * @return Object array
	 */
	public String[] getParams() {
		return this.params;
	}

	/**
	 * The HTTP Status code that applies best to the encountered problem, see
	 * <a href="https://tools.ietf.org/html/rfc7231">https://tools.ietf.org/html/rfc7231</a>
	 *
	 * @return the status
	 */
	public HttpStatus getStatus() {
		return status;
	}

	/**
	 * The severity of the event: FATAL (500 series), ERROR (400 series), WARN (200 series), or INFO/DEBUG/TRACE
	 *
	 * @return the severity
	 */
	public MessageSeverity getSeverity() {
		return severity;
	}

	/**
	 * The server name that the exception occurred on.
	 * <p>
	 * Implementations may simply return the "server.name" system property.
	 *
	 * @return String the server name
	 */
	public String getServerName() {
		return System.getProperty("server.name");
	}

}
