package com.wynd.vop.framework.validation;

import com.wynd.vop.framework.messages.ServiceMessage;

import java.lang.reflect.Method;
import java.util.List;

/**
 * An interface for business validation classes in the service (domain) layers.
 * <p>
 * This interface is not coupled to any other validating mechanism,
 * and supports the encapsulation of validation logic as a first-class citizen.
 * <p>
 * Classes that implement this interface <b>must</b> provide a no-arg constructor.
 * <p>
 * This interface shamelessly steals from {@link org.springframework.validation.Validator},
 * adding generic &lt;T&gt; to type-cast the object being validated.
 *

 *
 * @param <T> type-cast the object being validated
 */
public interface Validator<T> {

	/**
	 * Provides the opportunity to perform pre-validation and/or post-validation steps.
	 * <p>
	 * The implementation of this method may be used to call the {@link #validate(Object, List)} method.
	 * Check the specific implementation to confirm behavior.
	 *
	 * @param toValidate the object that is to be validated
	 * @param messages to be returned to the service method caller
	 * @param supplemental any additional data / information objects for use in the validation process
	 */
	void initValidate(Object toValidate, List<ServiceMessage> messages, Object... supplemental);

	/**
	 * Call the validate method on the validator for model object T to validate the supplied {@code toValidate} object.
	 * 
	 * <p>
	 * The supplied {@code List<ServiceMessage> messages} instance can be used to report any resulting validation errors.
	 * 
	 * <p>
	 * This implementation pre-validates the following conditions of the {@code toValidate} parameter for you:
	 * <ul>
	 * <li>Stash any supplemental objects for retrieval by {@link #getSupplemental()} and {@link #getSupplemental(Class)}.<br/>
	 * Examples of supplemental objects: while validating a response object, the request object is added as a supplemental in case it
	 * is needed.
	 * <li>Stash the calling {@link Method} (if provided) for retrieval by {@link #getCallingMethod()} and
	 * {@link #getCallingMethodName()}
	 * <li>Null check the {@code toValidate} parameter. If the null check fails, returns with message ({@link #validate(Object, List)}
	 * method is never called)
	 * <li>Class of the toValidate parameter verified to be correct. If it fails, returns with message ({@link #validate(Object, List)}
	 * method is never called)
	 * <li>messages parameter null checked and list initialized if necessary
	 * </ul>
	 *
	 * @param toValidate the object that is to be validated
	 * @param messages to be returned to the service method caller
	 *
	 * @see Validator
	 */
	void validate(T toValidate, List<ServiceMessage> messages);

	/**
	 * The type being validated.
	 * <p>
	 * Implementations would typically {@code return T.class} (whatever class T is).
	 *
	 * @return Class of type T
	 */
	Class<T> getValidatedType();

	/**
	 * Optional. Store the method that caused the Validator to be invoked.
	 * This is particularly useful when an interceptor or aspect is used to invoke validation.
	 * <p>
	 * Implementations would typically do {@code this.callingMethod = callingMethod;}
	 * <p>
	 * The callingMethod can be used - among many other things - to determine the class and method from which the validation was
	 * invoked, e.g. {@code "class that caused validation: " + callingMethod.getDeclaringClass().getName()}
	 *
	 * @param callingMethod the method call that caused the Validator to be invoked
	 */
	void setCallingMethod(Method callingMethod);

	/**
	 * Optional. Store the method that caused the Validator to be invoked.
	 * This is particularly useful when an interceptor or aspect is used to invoke validation.
	 * <p>
	 * Implementations would typically do {@code this.callingMethod = callingMethod;}
	 * <p>
	 * The callingMethod can be used - among many other things - to determine the class and method from which the validation was
	 * invoked, e.g. {@code "class that caused validation: " + callingMethod.getDeclaringClass().getName()}
	 *
	 * @return Method null, or the method call that caused the Validator to be invoked
	 */
	Method getCallingMethod();
}
