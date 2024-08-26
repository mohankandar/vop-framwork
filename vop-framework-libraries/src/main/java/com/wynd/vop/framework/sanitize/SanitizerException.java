package com.wynd.vop.framework.sanitize;

/**
 * Custom exception class for errors encountered in sanitization processes.
 * Extends {@link RuntimeException} to provide unchecked exceptions for sanitization failures.
 */
public class SanitizerException extends RuntimeException {

	private static final long serialVersionUID = -3712716005598510941L;

	/**
	 * Constructs a new {@code SanitizerException} with no detail message.
	 */
	public SanitizerException() {
		super();
	}

	/**
	 * Constructs a new {@code SanitizerException} with the specified detail message.
	 *
	 * @param message the detail message explaining the reason for the exception.
	 */
	public SanitizerException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@code SanitizerException} with the specified cause.
	 * The detail message is set to the result of {@code (cause==null ? null : cause.toString())}.
	 *
	 * @param cause the cause of the exception, which can be retrieved later using {@link Throwable#getCause()}.
	 */
	public SanitizerException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@code SanitizerException} with both a detail message and a cause.
	 *
	 * @param message the detail message explaining the reason for the exception.
	 * @param cause the cause of the exception, which can be retrieved later using {@link Throwable#getCause()}.
	 */
	public SanitizerException(String message, Throwable cause) {
		super(message, cause);
	}
}
