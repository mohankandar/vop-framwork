package com.wynd.vop.framework.exception.interceptor;

import com.wynd.vop.framework.constants.VopConstants;
import com.wynd.vop.framework.exception.VopExceptionExtender;
import com.wynd.vop.framework.exception.VopRuntimeException;
import com.wynd.vop.framework.log.VopBanner;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.messages.MessageKey;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Contains utility ops for logging and handling exceptions consistently. Primarily for usage in interceptors which
 * implement ThrowsAdvice and handle exceptions to ensure these all log then consistently.
 *
 */
public final class ExceptionHandlingUtils {
	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(ExceptionHandlingUtils.class);

	/** The Constant LOC_EXCEPTION_PREFIX. */
	private static final String LOC_EXCEPTION_PREFIX =
			" caught exception, handling it as configured.  Here are details [";

	/** The Constant LOG_EXCEPTION_MID. */
	private static final String LOG_EXCEPTION_MID = "] args [";

	/** The Constant LOG_EXCEPTION_POSTFIX. */
	private static final String LOG_EXCEPTION_POSTFIX = "].";

	/** The Constant LOG_EXCEPTION_UNDERSCORE. */
	private static final String LOG_EXCEPTION_UNDERSCORE = "_";

	/** The Constant LOG_EXCEPTION_DOT. */
	private static final String LOG_EXCEPTION_DOT = ".";

	/**
	 * private constructor for utility class
	 */
	private ExceptionHandlingUtils() {
	}

	/**
	 * Resolve the throwable to an {@link VopRuntimeException} (or subclass of BipRuntimeException).
	 *
	 * @param messageKey the message key to use for this type of exception
	 * @param throwable the throwable
	 * @return the runtime exception
	 * @throws InstantiationException the instantiation exception
	 * @throws IllegalAccessException the illegal access exception
	 */
	public static VopRuntimeException resolveRuntimeException(final MessageKey messageKey, final Throwable throwable) {
		// custom exception type to represent the error
		VopRuntimeException resolvedRuntimeException = null;

		if (VopRuntimeException.class.isAssignableFrom(throwable.getClass())) {
			// have to cast so the "Throwable throwable" variable can be returned as-is
			resolvedRuntimeException = castToBipRuntimeException(throwable);

		} else if (VopExceptionExtender.class.isAssignableFrom(throwable.getClass())) {
			resolvedRuntimeException = convertFromBipExceptionExtender(throwable);

		} else {
			// make a new BipRuntimeException from the non-VOP throwable
			resolvedRuntimeException =
					new VopRuntimeException(messageKey, MessageSeverity.FATAL, HttpStatus.INTERNAL_SERVER_ERROR, throwable);
		}

		return resolvedRuntimeException;
	}

	static VopRuntimeException convertFromBipExceptionExtender(final Throwable throwable) {
		VopRuntimeException resolvedRuntimeException = null;
		try {
			// cast "Throwable throwable" variable to the VOP exception interface
			VopExceptionExtender vop = (VopExceptionExtender) throwable;
			String [] stringArray = new String [] {};
			// instantiate the Runtime version of the interface
			resolvedRuntimeException = (VopRuntimeException) throwable.getClass()
					.getConstructor(MessageKey.class, MessageSeverity.class, HttpStatus.class, Throwable.class, stringArray.getClass())
					.newInstance(vop.getExceptionData().getMessageKey(), vop.getExceptionData().getSeverity(),
							vop.getExceptionData().getStatus(), throwable, stringArray);
		} catch (ClassCastException | IllegalAccessException | IllegalArgumentException | InstantiationException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			MessageKeys key = MessageKeys.VOP_EXCEPTION_HANDLER_ERROR_VALUES;
			LOGGER.error(new VopBanner(VopConstants.RESOLVE_EXCEPTION, Level.ERROR),
					key.getMessage(e.getClass().getName()), e);
			throw new VopRuntimeException(key, MessageSeverity.FATAL, HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
		return resolvedRuntimeException;
	}

	static VopRuntimeException castToBipRuntimeException(final Throwable throwable) { // method added for testability
		VopRuntimeException resolvedRuntimeException = null;
		try {
			resolvedRuntimeException = (VopRuntimeException) throwable;
		} catch (ClassCastException e) {
			MessageKeys key = MessageKeys.VOP_EXCEPTION_HANDLER_ERROR_CAST;
			LOGGER.error(new VopBanner(VopConstants.RESOLVE_EXCEPTION, Level.ERROR),
					key.getMessage(throwable.getClass().getName()), e);
			throw new VopRuntimeException(key, MessageSeverity.FATAL, HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
		return resolvedRuntimeException;
	}

	/**
	 * Log exception.
	 *
	 * @param catcher the catcher - some descriptive name for whomever caught this exception and wants it logged
	 * @param method the method
	 * @param args the args
	 * @param throwable the throwable
	 */
	public static void logException(final String catcher, final Method method, final Object[] args,
			final Throwable throwable) {
		final VopLogger errorLogger =
				VopLoggerFactory.getLogger(method.getDeclaringClass().getName() + LOG_EXCEPTION_DOT + method.getName()
						+ LOG_EXCEPTION_UNDERSCORE + throwable.getClass().getName());
		final String errorMessage =
				throwable.getClass().getName() + " thrown by " + method.getDeclaringClass().getName()
						+ LOG_EXCEPTION_DOT + method.getName();
		if (errorLogger.isWarnEnabled()) {
			errorLogger.warn(catcher + LOC_EXCEPTION_PREFIX + errorMessage + LOG_EXCEPTION_MID + Arrays.toString(args)
					+ LOG_EXCEPTION_POSTFIX, throwable);
		} else {
			// if we disable warn logging (all the details and including stack trace) we only show minimal
			// evidence of the error in the logs
			errorLogger.error(catcher + LOC_EXCEPTION_PREFIX + errorMessage + LOG_EXCEPTION_MID + Arrays.toString(args)
					+ LOG_EXCEPTION_POSTFIX);
		}
	}

}
