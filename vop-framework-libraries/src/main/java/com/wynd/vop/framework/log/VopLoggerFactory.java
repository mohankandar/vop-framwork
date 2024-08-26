package com.wynd.vop.framework.log;

import com.wynd.vop.framework.constants.VopConstants;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

/**
 * This class wraps the SLF4J logger to add logging enhancements for the platform.
 * <p>
 * If a future upgrade of SLF4J changes the Logger interface, changes will be required in the BipLogger class.
 *

 */
public final class VopLoggerFactory {

	/**
	 * Do not instantiate.
	 */
	private VopLoggerFactory() {
		throw new IllegalStateException(VopLoggerFactory.class.getSimpleName() + VopConstants.ILLEGALSTATE_STATICS);
	}

	/**
	 * Gets a SLF4J-compliant logger, enhanced for applications, for the specified class.
	 *
	 * @param clazz the Class for which logging is desired
	 * @return BipLogger
	 * @see org.slf4j.LoggerFactory#getLogger(Class)
	 */
	public static final VopLogger getLogger(Class<?> clazz) {
		return VopLogger.getLogger(LoggerFactory.getLogger(clazz));
	}

	/**
	 * Gets a SLF4J-compliant logger, enhanced for applications, for the specified name.
	 *
	 * @param name the name under which logging is desired
	 * @return BipLogger
	 * @see org.slf4j.LoggerFactory#getLogger(String)
	 */
	public static final VopLogger getLogger(String name) {
		return VopLogger.getLogger(LoggerFactory.getLogger(name));
	}

	/**
	 * Get the implementation of the logger factory that is bound to SLF4J, that serves as the basis for BipLoggerFactory.
	 *
	 * @return ILoggerFactory an instance of the bound factory implementation
	 * @see org.slf4j.LoggerFactory#getILoggerFactory()
	 */
	public static final ILoggerFactory getBoundFactory() {
		return LoggerFactory.getILoggerFactory();
	}

}
