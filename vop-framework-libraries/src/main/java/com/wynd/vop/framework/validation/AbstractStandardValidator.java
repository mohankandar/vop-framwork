package com.wynd.vop.framework.validation;

import com.wynd.vop.framework.exception.VopRuntimeException;
import com.wynd.vop.framework.exception.interceptor.ExceptionHandlingUtils;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import com.wynd.vop.framework.messages.ServiceMessage;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract implementation of the {@link Validator} interface.
 * <p>
 * Provides standardized pre-validation capabilities, and stores objects for use in the validator implementation.
 * See the {@link #initValidate(Object, List, Object...)} method for more information.
 *
 * @see Validator
 *

 *
 * @param <T> type-cast the object being validated
 */
public abstract class AbstractStandardValidator<T> implements Validator<T> {

	/** Class logger */
	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(AbstractStandardValidator.class);

	/** The class of the object that will be validated */
	private Class<T> toValidateClass;

	/** The method that caused the validator to be executed */
	private Method callingMethod;

	/** Full class.method name of the calling method */
	private String callingMethodName;

	/** Supplemental objects for processing the validation */
	private Object[] supplemental;

	/**
	 * As a convenience to the developer, performs standardized pre-validation steps before calling the implementations
	 * {@link #validate(Object, List)} method.<br/>
	 * This method:
	 * <ul>
	 * <li>Can be strongly typed, per the class-level Type Parameter.
	 * <li>Does Exception handling for the entire validation process.
	 * <li>Stashes any supplemental objects for retrieval by {@link #getSupplemental()} and {@link #getSupplemental(Class)}.<br/>
	 * Examples of supplemental objects: while validating a response object, the request object is added as a supplemental in case it
	 * is needed.
	 * <li>Stashes the calling {@link Method} (if provided) for retrieval by {@link #getCallingMethod()} and
	 * {@link #getCallingMethodName()}.
	 * <li>Null checks the {@code toValidate} parameter. If the null check fails, returns with message ({@link #validate(Object, List)}
	 * method is never called).
	 * <li>Verifies that the class of the toValidate parameter is correct. If it fails, returns with message
	 * ({@link #validate(Object, List)} method is never called).
	 * <li>Null checks the messages parameter initializes it if necessary.
	 * </ul>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initValidate(final Object toValidate, final List<ServiceMessage> messages, final Object... supplemental) {
		try {
			this.supplemental = supplemental;

			List<ServiceMessage> messagesToAdd = messages != null ? messages : new ArrayList<>();

			this.callingMethodName = callingMethod == null ? ""
					: callingMethod.getDeclaringClass().getSimpleName()
							+ "." + callingMethod.getName() + ": ";

			LOGGER.debug("Validating " + (toValidate == null ? "null" : toValidate.getClass().getSimpleName())
					+ " for " + callingMethodName);

			// request-level null check
			if (toValidate == null) {
				LOGGER.debug("Request is null");
				messagesToAdd.add(new ServiceMessage(MessageSeverity.ERROR, HttpStatus.BAD_REQUEST,
						MessageKeys.VOP_VALIDATOR_NOT_NULL, callingMethodName));
				return;
			}

			setToValidateClass(toValidate);
			// check class is correct (if correct it will not be null)
			if (this.toValidateClass == null) { // NOSONAR : will always be false
				handleInvalidClass(toValidate, messagesToAdd);
				return;
			}

			// unchecked type-cast (but pre-verified above) to invoke implementation-specific validation
			validate((T) toValidate, messagesToAdd);

		} catch (Throwable t) { // NOSONAR intentionally broad catch
			final VopRuntimeException runtime =
					ExceptionHandlingUtils.resolveRuntimeException(MessageKeys.VOP_VALIDATOR_INITIALIZE_ERROR_UNEXPECTED, t);

			if (runtime != null) {
				throw runtime;
			} else {
				throw t;
			}
		}
	}

	/**
	 * Get the class of the object to be validated.
	 * <p>
	 * If the toValidate object is not an instantiation of the class param {@code T}
	 * (directly or as subclass), a message is logged and {@code null} is returned.
	 *
	 * @param toValidate - the object to be validated
	 */
	@SuppressWarnings("unchecked")
	void setToValidateClass(final Object toValidate) {
		this.toValidateClass = null;
		try {
			this.toValidateClass = (Class<T>) toValidate.getClass();
		} catch (Exception e) {
			LOGGER.debug("Unable to set the type to be validated", e);
		}
	}

	private void handleInvalidClass(final Object toValidate, final List<ServiceMessage> messages) {
		MessageKeys key = MessageKeys.VOP_VALIDATOR_TYPE_MISMATCH;
		String[] params =
				new String[] { (toValidate == null ? "'null Object'" : toValidate.getClass().getName()),
						(getValidatedType() == null ? "'no object to validate'" : getValidatedType().getName()) };
		LOGGER.debug(key.getMessage(params));
		messages.add(new ServiceMessage(MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, key, params));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.wynd.vop.framework.validation.Validator#getValidatedType()
	 */
	@Override
	public Class<T> getValidatedType() {
		return toValidateClass;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.wynd.vop.framework.validation.Validator#setCallingMethod(java.lang.reflect.Method)
	 */
	@Override
	public void setCallingMethod(final Method callingMethod) {
		this.callingMethod = callingMethod;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.wynd.vop.framework.validation.Validator#getCallingMethod()
	 */
	@Override
	public Method getCallingMethod() {
		return this.callingMethod;
	}

	/**
	 * The calling method name, derived from {@link #getCallingMethod()}.
	 *
	 * @return the callingMethodName
	 */
	protected String getCallingMethodName() {
		return callingMethodName;
	}

	/**
	 * Returns {@code true} if any supplemental objects have been added to the validator.
	 *
	 * @return boolean - true if the supplmental list has anything in it
	 */
	protected boolean hasSupplemental() {
		return (this.supplemental != null) && (this.supplemental.length > 0);
	}

	/**
	 * Returns {@code true} if any supplemental objects of type {@code clazz}
	 * have been added to the validator.
	 * <p>
	 * Note that {@code clazz} must be the exact type. Subclasses will not be identified.
	 *
	 * @param clazz - the exact type to look for
	 * @return boolean - {@code true} if an object of type {@code clazz} was found
	 */
	protected boolean hasSupplemental(final Class<?> clazz) {
		if ((clazz != null) && hasSupplemental()) {
			for (Object obj : supplemental) {
				if (clazz.equals(obj.getClass())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get all supplemental objects, for use in the {@link #validate(Object, List)}
	 * method.
	 *
	 * @return Object[] - the array of supplemental objects
	 */
	protected Object[] getSupplemental() {
		return this.supplemental;
	}

	/**
	 * Get any supplemental objects of type {@code clazz}, for use in the
	 * {@link #validate(Object, List)} method.
	 * <p>
	 * The first found object of type {@code clazz} is returned.
	 * If multiple objects of the same type exist, try getting the list with
	 * {@link #getSupplemental()}.
	 *
	 * @param clazz - the type of object to get
	 * @return Object - the supplemental object
	 */
	protected Object getSupplemental(final Class<?> clazz) {
		if (hasSupplemental(clazz)) {
			for (Object obj : supplemental) {
				if (clazz.equals(obj.getClass())) {
					return obj;
				}
			}
		}
		return null;
	}
}
