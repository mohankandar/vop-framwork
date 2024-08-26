package com.wynd.vop.framework.service.aspect;

import com.wynd.vop.framework.exception.VopRuntimeException;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import com.wynd.vop.framework.messages.ServiceMessage;
import com.wynd.vop.framework.service.DomainResponse;
import com.wynd.vop.framework.validation.AbstractStandardValidator;
import com.wynd.vop.framework.validation.Validator;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This aspect invokes business validations on eligible service API methods.
 *
 * Eligible service operations are any those which ...
 * <ol>
 * <li>have public scope
 * <li>have a spring @Service annotation
 * <li>have a companion validator named with the form <tt><i>ClassName</i>Validator</tt> that is in the "validators" package below
 * where the model object is found,
 * e.g. {@code com.wynd.vop.reference.api.model.v1.validators.PersonInfoValidator.java}.
 * </ol>
 * <p>
 * Validators called by this aspect <b>should</b> extend {@link AbstractStandardValidator} or
 * similar implementation.
 *
 * Developers note: this class cannot be converted to {@code @Before} and {@code @After}
 * advice. JoinPoint.proceed() is called conditionally on success/failure of input validation.
 * Before and After advice does not provide that opportunity.
 *
 * @see Validator
 * @see AbstractStandardValidator
 *

 */
@Aspect
@Order(-9998)
public class ServiceValidationAspect extends BaseServiceAspect {

	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(ServiceValidationAspect.class);

	/** Text added to end of class name to determine its validator name */
	private static final String POSTFIX = "Validator";

	/** keep track of previous resolveValidatorClass() calls to reduce (noticeable in prod) overhead  - ConcurrentHashMap can't store null values, so manually synchronize instead */
	private static final HashMap<String,Class> resolvedClasses = new HashMap<String,Class>();

	/**
	 * Around advice for{@link BaseServiceAspect#serviceImpl()} pointcut.
	 * <p>
	 * This method will execute validations on any parameter objects in the method signature.<br/>
	 * Any failed validations is added to the method's response object, and is audit logged.
	 * <p>
	 * Validators called by this aspect <b>should</b> extend {@link AbstractStandardValidator} or
	 * similar implementation.
	 * 
	 * Developers note: this class cannot be converted to {@code @Before} and {@code @After}
	 * advice. JoinPoint.proceed() is called conditionally on success/failure of input validation.
	 * Before and After advice does not provide that opportunity.
	 *
	 * @param joinPoint
	 * @return Object
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	@Around("publicStandardServiceMethod() && serviceImpl()")
	public Object aroundAdvice(final ProceedingJoinPoint joinPoint) throws Throwable {

		LOGGER.debug(this.getClass().getSimpleName() + " executing around method:" + joinPoint.toLongString());
		DomainResponse domainResponse = null;

		try {
			LOGGER.debug("Validating service interface request inputs.");

			// get the request and the calling method from the JoinPoint
			List<Object> methodParams = Arrays.asList(joinPoint.getArgs());
			Method method = null;
			if (joinPoint.getArgs().length > 0) {
				Class<?>[] methodParamTypes = new Class<?>[methodParams.size()];
				for (int i = 0; i < methodParams.size(); i++) {
					Object param = methodParams.get(i);
					methodParamTypes[i] = param == null ? null : param.getClass();
				}
				method = joinPoint.getSignature().getDeclaringType().getDeclaredMethod(joinPoint.getSignature().getName(),
						methodParamTypes);
			}

			// attempt to validate all inputs to the method
			domainResponse = validateInputsToTheMethod(methodParams, method);

			// if there were no errors from validation, proceed with the actual method
			if (!didValidationPass(domainResponse)) { // NOSONAR didValidationPass is not always true, unlike what sonar believes
				LOGGER.debug("Service interface request validation failed. >>> Skipping execution of "
						+ joinPoint.getSignature().toShortString() + " and returning immediately.");
			} else {
				LOGGER.debug("Service interface request validation succeeded. Executing " + joinPoint.getSignature().toShortString());

				domainResponse = (DomainResponse) joinPoint.proceed();

				// only call post-proceed() validation if there are no errors on the response
				callPostValidationBasedOnDomainResponse(joinPoint, domainResponse, method);
			}
		} finally {
			LOGGER.debug(this.getClass().getSimpleName() + " after method was called.");
		}

		return domainResponse;

	}

	/**
	 * Call post validation based on domain response.
	 *
	 * @param joinPoint the join point
	 * @param domainResponse the domain response
	 * @param method the method
	 */
	private void callPostValidationBasedOnDomainResponse(final ProceedingJoinPoint joinPoint, DomainResponse domainResponse,
			Method method) {
		if ((domainResponse != null) && !(domainResponse.hasErrors() || domainResponse.hasFatals())) {
			LOGGER.debug("Validating service interface response outputs.");
			validateResponse(domainResponse, domainResponse.getMessages(), method, joinPoint.getArgs());
		}
	}

	/**
	 * Returns {@code true} if DomainResponse is not {@code null} and its messages list is {@code null} or empty.
	 *
	 * @param domainResponse the domain response
	 * @return true, if successful
	 */
	private boolean didValidationPass(final DomainResponse domainResponse) {
		return (domainResponse == null) || ((domainResponse.getMessages() == null) || domainResponse.getMessages().isEmpty());
	}

	/**
	 * Validates all input args to a method.
	 *
	 * @param methodParams - the method args
	 * @param method - the method being executed
	 * @return 
	 */
	private DomainResponse validateInputsToTheMethod(final List<Object> methodParams, final Method method) {
		DomainResponse response = null;
		if (methodParams != null) {
			List<ServiceMessage> messages = new ArrayList<>();

			for (final Object arg : methodParams) {
				validateRequest(arg, messages, method);
			}
			// add any validation error messages
			if (!messages.isEmpty()) {
				response = addValidationErrorMessages(method, messages);
			}
		}

		return response;
	}

	/**
	 * Adds the validation error messages.
	 *
	 * @param method the method
	 * @param messages the messages
	 * @return the domain response
	 */
	private DomainResponse addValidationErrorMessages(final Method method, final List<ServiceMessage> messages) {
		DomainResponse response = null;
		try {
			response = (DomainResponse) method.getReturnType().newInstance();
		}catch (InstantiationException e) {
			LOGGER.error("Could not return input validation errors because the class " + method.getReturnType() + " could not be instantiated", e);
			throw new VopRuntimeException(MessageKeys.VOP_DEV_ILLEGAL_INSTANTIATION, MessageSeverity.ERROR,
					HttpStatus.INTERNAL_SERVER_ERROR, method.getReturnType().getSimpleName());
		}catch (IllegalAccessException e) {
			LOGGER.error("Could not return input validation errors because the class " + method.getReturnType() + " could not be accessed", e);
			throw new VopRuntimeException(MessageKeys.VOP_DEV_ILLEGAL_ACCESS, MessageSeverity.ERROR,
					HttpStatus.INTERNAL_SERVER_ERROR, method.getReturnType().getSimpleName());
		}
		response.addMessages(messages);
		return response;
	}

	/**
	 * Use ONLY for exceptions raised due to:
	 * <ul>
	 * <li>issues with acquiring the validator class for the originating service impl
	 * <li>issues instantiating the validator class
	 * </ul>
	 *
	 * @param validatorClass
	 * @param e
	 * @param object
	 * @throws VopRuntimeException
	 */
	private void handleValidatorInstantiationExceptions(final Class<?> validatorClass, final Exception e, final Object object) {
		// Validator programming issue - throw exception
		MessageKeys key = MessageKeys.VOP_DEV_ILLEGAL_INVOCATION;
		String[] params = new String[] { (validatorClass != null ? validatorClass.getName() : "null"), "validate",
				object.getClass().getName(), Validator.class.getName() };
		LOGGER.error(key.getMessage(params), e);
		throw new VopRuntimeException(key, MessageSeverity.FATAL, HttpStatus.INTERNAL_SERVER_ERROR, e, params);
	}

	/**
	 * Locate the {@link Validator} for the request object, and if it exists,
	 * invoke the {@link Validator#getValidatedType()} method. If the
	 * Validator does not exist, a warning is logged and validation
	 * will be skipped.
	 * <p>
	 * Validator implementations <b>must</b> exist in a validators package
	 * under the package in which {@code object} exists.
	 *
	 * @see Validator
	 * @see AbstractStandardValidator
	 *
	 * @param object the object to validate
	 * @param messages list on which to return validation messages
	 * @param callingMethod optional; the method that caused this validator to be called
	 */
	private void validateRequest(final Object object, final List<ServiceMessage> messages, final Method callingMethod) {

		Class<?> validatorClass = this.resolveValidatorClass(object);

		//validation is skipped if validatorClass is null
		if (validatorClass != null) {
			// invoke the validator - no supplemental objects
			try {
				invokeValidator(object, messages, callingMethod, validatorClass);

			} catch (InstantiationException | IllegalAccessException | NullPointerException e) {
				handleValidatorInstantiationExceptions(validatorClass, e, object);
			}
		}
	}

	/**
	 * Invoke validator.
	 *
	 * @param object the object
	 * @param messages the messages
	 * @param callingMethod the calling method
	 * @param validatorClass the validator class
	 * @param supplemental the supplemental
	 * @throws InstantiationException the instantiation exception
	 * @throws IllegalAccessException the illegal access exception
	 */
	private void invokeValidator(final Object object, final List<ServiceMessage> messages, final Method callingMethod,
			final Class<?> validatorClass, final Object... supplemental) throws InstantiationException, IllegalAccessException {
		Validator<?> validator = (Validator<?>) validatorClass.newInstance();
		validator.setCallingMethod(callingMethod);
		validator.initValidate(object, messages, supplemental);
	}

	/**
	 * Locate the {@link Validator} for the object, and if it exists,
	 * invoke the {@link Validator#getValidatedType()} method. If the
	 * Validator does not exist, a warning is logged and validation
	 * will be skipped.
	 * <p>
	 * Validator implementations <b>must</b> exist in a validators package
	 * under the package in which {@code object} exists.
	 *
	 * @see Validator
	 * @see AbstractStandardValidator
	 *
	 * @param object
	 * @param messages
	 * @param callingMethod
	 * @param requestObjects
	 */
	private void validateResponse(final DomainResponse object, final List<ServiceMessage> messages, final Method callingMethod,
			final Object... requestObjects) {

		Class<?> validatorClass = this.resolveValidatorClass(object);

		//validation is skipped if validatorClass is null
		if (validatorClass != null) {
			// invoke the validator, sned request objects as well
			try {
				invokeValidator(object, messages, callingMethod, validatorClass, requestObjects);

			} catch (InstantiationException | IllegalAccessException | NullPointerException e) {
				handleValidatorInstantiationExceptions(validatorClass, e, object);
			}
		}
	}

	/**
	 * Determine the Validator class for the model object that is to be validated.
	 * <p>
	 * The pattern for Validator classes is:<br/>
	 * <tt><i>model.objects.class.package</i>.validators.<i>ModelObjectClassSimpleName</i>Validator</tt>
	 *
	 * @param object
	 * @return
	 */
	private Class<?> resolveValidatorClass(final Object object) {
		// Deduce the validator class name based on the pattern
		String qualifiedValidatorName = object.getClass().getPackage() + ".validators." + object.getClass().getSimpleName() + POSTFIX;
		qualifiedValidatorName = qualifiedValidatorName.replaceAll("package\\s+", "");

		// check previously-resolved classes
		synchronized (resolvedClasses) {
			if (resolvedClasses.containsKey(qualifiedValidatorName)) {
				return resolvedClasses.get(qualifiedValidatorName);
			}
		}

		// find out if a validator exists for object
		Class<?> validatorClass = null;
		try {
			validatorClass = Class.forName(qualifiedValidatorName);
		} catch (ClassNotFoundException e) {
			// no validator, return without error
			LOGGER.warn("Could not find validator class " + qualifiedValidatorName
					+ " - skipping validation for object " + ReflectionToStringBuilder.toString(object));
		}

		// add resolved class to cache, null or otherwise, to prevent future classloader hits
		synchronized (resolvedClasses) {
			resolvedClasses.put(qualifiedValidatorName, validatorClass);
			if (resolvedClasses.size() == 1000) {
				LOGGER.warn("ServiceValidationAspect internal cache 'resolvedClasses' is over 1000 entries - this most likely indicates a dynamic class-name issue and will be a serious memory leak");
			}
		}
		return validatorClass;
	}
}
