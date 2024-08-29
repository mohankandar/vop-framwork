package com.wynd.vop.framework.messages;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

/**
 * A message @PropertySource for Service Vop*Exception and *Message list.
 * <p>
 * This class derives its values from the framework-messages.properties file.
 * that is added to the spring context as an {@code @PropertySource}.
 * Primarily used by
 * <p>
 * <u>Usage and Maintenance</u><br/>
 * Any change in framework-messages.properties must be reflected in this class.<br/>
 * Any change in this class must be reflected in framework-messages.properties.
 *

 */
public enum MessageKeys implements MessageKey {

	//
	// Default messages defined for enumerations SHOULD NOT have params ... no {} brackets.
	//

	/** No key provided or specified; no args */
	NO_KEY("NO_KEY", "Unknown, no key provided."),
	/** Key for warning messages; {0} = warning message */
	WARN_KEY("WARN", ""),
	/** Key for propagating exceptions as VopExceptionExtender; {0} = exception message */
	PROPAGATE("PROPAGATE", ""),

	/** Problem with reflection; {0} = class simple name */
	VOP_DEV_ILLEGAL_INSTANTIATION("vop.framework.dev.illegal.instantiation",
			"Do not instantiate static classes or do not try to instantiate when an appropriate constructor does not exist."),
	/** Problem with reflection; {0} = class simple name */
	VOP_DEV_ILLEGAL_ACCESS("vop.framework.dev.illegal.access", "Could not access appropriate constructor."),
	/**
	 * Problem with reflection; {0} = class being instantiated; {1} = action being taken; {2} = type being acted against; {3}
	 * super-interface
	 */
	VOP_DEV_ILLEGAL_INVOCATION("vop.framework.dev.illegal.invocation", "Could not find or instantiate class."),

	/** Malformed JMX ObjectName; {0} is the name of the class being registered as a bean; {1} is the ObjectName being registered */
	VOP_JMX_CACHE_NAMING_MALFORMED("vop.jmx.cache.naming.malformed",
			"Could not register class on the MBeanServer because its ObjectName is malformed."),
	/** Some pre-processing issue; {0} is the class name or JMX ObjectName being registered / deregistered */
	VOP_JMX_REGISTRATION_PRE("vop.jmx.registration.pre", "Problem with pre-registration or pre-deregistration of JMX MBean."),
	/** Non-compliant JMX bean; {0} is the class name or JMX ObjectName */
	VOP_JMX_BEAN_NONCOMPLIANT("vop.jmx.bean.noncompliant", "Proposed class is not a JMX compliant MBean."),

	/** Last resort, unexpected exception; {0} = exception class simple name; {1} = exception message */
	VOP_GLOBAL_GENERAL_EXCEPTION("vop.framework.global.general.exception", "Unexpected exception."),
	/** Exception handler cast failed; {0} = class name */
	VOP_EXCEPTION_HANDLER_ERROR_VALUES("vop.framework.exception.handler.error.values.",
			"Could not instantiate VopRuntimeException."),
	/** Exception handler cast failed; {0} = class name */
	VOP_EXCEPTION_HANDLER_ERROR_CAST("vop.framework.exception.handler.error.cast",
			"Could not cast throwable to VopRuntimeException."),
	/** MethodArgumentNotValidException; {0} = "field" or "object"; {1} = codes; {2} = default message */
	VOP_GLOBAL_VALIDATOR_METHOD_ARGUMENT_NOT_VALID("vop.framework.global.validator.method.argument.not.valid", "Argument not valid."),
	/** HttpClientErrorException; {0} = http status code; {1} = exception message */
	VOP_GLOBAL_HTTP_CLIENT_ERROR("vop.framework.global.http.client.error", "Client Error."),
	/** MethodArgumentTypeMismatchException; {0} = argument name; {1} = expected class name */
	VOP_GLOBAL_REST_API_TYPE_MISMATCH("vop.framework.global.rest.api.type.mismatch", "API argument type could not be resolved."),
	/** ConstraintViolationException; {0} = bean class name; {1} = property name; {2} = violation message */
	VOP_GLOBAL_VALIDATOR_CONSTRAINT_VIOLATION("vop.framework.global.validator.constraint.violation",
			"Validation constraint was violated."),

	/** JAXB Marshaller configuration failed; no args */
	VOP_REST_CONFIG_JAXB_MARSHALLER_FAIL("vop.framework.rest.config.jaxb.marshaller.failed", "Error configuring JAXB marshaller."),
	/** WebserviceTemplate configuration failed; no args */
	VOP_REST_CONFIG_WEBSERVICE_TEMPLATE_FAIL("vop.framework.rest.config.webservice.template.failed",
			"Unexpected exception thrown by WebServiceTemplate."),
	/** Propogate message from other service; {0} = message key; {1} = message text */
	VOP_FEIGN_MESSAGE_RECEIVED("vop.framework.feign.message.received", "External service returned error message."),

	/** JWT token is invalid; no args */
	VOP_TOKEN_PAYLOAD_MISSING("vop.framework.security.token.payload.invalid", "Payload Body Missing"),

	VOP_SECURITY_TOKEN_INVALID("vop.framework.security.token.invalid", "Invalid Token."),
	VOP_SECURITY_TOKEN_INVALID_REQ_PARAM_MISSING("vop.framework.security.token.invalid.req.param.missing",
			"Invalid Token. Parameter(s) Missing. Required JWT parameters configured {0}"),
	/** JWT token cannot be blank; no args */
	VOP_SECURITY_TOKEN_BLANK("vop.framework.security.token.blank", "No JWT Token in Header."),
	/**
	 * JWT token cannot be blank; #{0} = the problem; {1} = the token; {2} = the simple class name of the exception {3} = message from
	 * the exception
	 */
	VOP_SECURITY_TOKEN_BROKEN("vop.framework.security.token.broken", "JWT Token is not valid."),
	/** Correlation IDs passed in the JWT token cannot be blank; no args */
	VOP_SECURITY_TRAITS_CORRELATIONID_BLANK("vop.framework.security.traits.correlationid.blank",
			"Cannot process blank correlation id."),
	/** Correlation IDs passed in the JWT token; {0} = ELEMENT_SS_COUNT, {1} = ELEMENT_MAX_COUNT */
	VOP_SECURITY_TRAITS_CORRELATIONID_INVALID("vop.framework.security.traits.correlationid.invalid",
			"Invalid number of elements in correlation id."),
	/** IdType specified does not exist; {0} = IdTypes.[value] */
	VOP_SECURITY_TRAITS_IDTYPE_INVALID("vop.framework.security.traits.idtype.invalid",
			"Specified IdType does not exist."),
	/** Issuer specified does not exist; {0} = Issuers.[value] */
	VOP_SECURITY_TRAITS_ISSUER_INVALID("vop.framework.security.traits.issuer.invalid",
			"Specified Issuer does not exist."),
	/** Source specified does not exist; {0} = Sources.[value] */
	VOP_SECURITY_TRAITS_SOURCE_INVALID("vop.framework.security.traits.source.invalid",
			"Specified Source does not exist."),
	/** Source specified does not exist; {0} = Sources.[value] */
	VOP_SECURITY_TRAITS_JWTSOURCE_INVALID("vop.framework.security.traits.jwtsource.invalid",
			"Specified JWT Source does not exist."),
	/** UserStatus specified does not exist; {0} = UserStatus.[value] */
	VOP_SECURITY_TRAITS_USERSTATUS_INVALID("vop.framework.security.traits.userstatus.invalid",
			"Specified UserStatus does not exist."),
	/** Encryption failed for some reason; {0} = kind of object that was being encrypted */
	VOP_SECURITY_ENCRYPT_FAIL("vop.framework.security.encrypt.failed", "Encryption failed."),
	/** Signing failed for some reason; {0} = kind of object that was being signed */
	VOP_SECURITY_SIGN_FAIL("vop.framework.security.sign.failed", "Could not sign."),
	/** Encryption failed for some reason; {0} = action being taken on the attribute; {1} attribute name */
	VOP_SECURITY_ATTRIBUTE_FAIL("vop.framework.security.attribute.failed", "Could not modify attribute."),
	/** SAML insertion failed; no args */
	VOP_SECURITY_SAML_INSERT_FAIL("vop.framework.security.saml.insert.failed", "SAML insertion failed."),
	/** SSL initialization failed {0} = exception simple class name; {1} = exception message */
	VOP_SECURITY_SSL_CONTEXT_FAIL("vop.framework.security.ssl.context.failed", "Could not establish SSL context."),

	/** Sanitizing filename failed; {0} = operation */
	VOP_SECURITY_SANITIZE_FAIL("vop.framework.security.sanitize.failed", "Unexpected error: {0}."),
    
    /** Generate JWT Unsupported Algorithm; */
    VOP_SECURITY_GENERATE_JWT_UNSUPPORTED_ALGORITHM("vop.framework.security.jwt.unsupported.algorithm", "No Supported JWT Algorithm or corresponding secret found."),
    
    /** Create signing key failed; {0} = operation */
	VOP_SECURITY_JWT_CREATE_SIGNING_KEY_FAIL("vop.framework.security.jwt.create.signing.key.failed", "Unexpected JWT Siging Key error: {0}."),

	/** Kong JWT token is invalid; no args */
	VOP_SECURITY_KONG_TOKEN_INVALID("vop.framework.security.token.invalid", "Invalid Kong Token."),

	/** Auditing error during cache operations; {0} = advice name, {1} = operation attempted */
	VOP_SECURITY_KONG_ERROR_UNEXPECTED("vop.framework.security.token.kong.error.unexpected",
			"An unexpected error occurred while retrieving Kong data."),

	/** Auditing error during cache operations; {0} = advice name, {1} = operation attempted */
	VOP_AUDIT_CACHE_ERROR_UNEXPECTED("vop.framework.audit.cache.error.unexpected",
			"An unexpected error occurred while auditing cache retrieval."),
	/** Auditing error produced by the aspect/interceptor itself; {0} = advice name, {1} = operation attempted */
	VOP_AUDIT_ASPECT_ERROR_UNEXPECTED("vop.framework.audit.aspect.error.unexpected",
			"An unexpected error occurred while auditing from aspect/interceptor."),
	/** Auditing appears to be broken; {0} = advice name, {1} = originating throwable that called writeAuditError */
	VOP_AUDIT_ASPECT_ERROR_CANNOT_AUDIT("vop.framework.audit.aspect.error.cannot.audit",
			"Cannot write audit error for throwables."),

	/** Validator initialization; no args */
	VOP_VALIDATOR_INITIALIZE_ERROR_UNEXPECTED("vop.framework.validator.initialize.error.unexpected",
			"Could not initialize standard validator."),
	VOP_VALIDATOR_ASSERTION("vop.framework.validator.assertion", "Assertion failed."),
	/** Object cannot be null; {0} the object that cannot be null */
	VOP_VALIDATOR_NOT_NULL("vop.framework.validator.not.null", "Object cannot be null."),
	/** {0} = validated object class name; {1} = expected class name */
	VOP_VALIDATOR_TYPE_MISMATCH("vop.framework.validator.type.mismatch", "Validated object is not of excpected type."),

	/** Simulator could not find mock response file; {0} = XML file name; {1} = key used to construct file name */
	VOP_REMOTE_MOCK_NOT_FOUND("vop.framework.remote.mock.not.found",
			"Could not read mock XML file. Please make sure the correct response file exists in the main/resources directory."),
	/**
	 * RemoteServiceCallMock is not set up to process a type; {0} = the RemoteServiceCallMock class; {1} = the class used in the
	 * request
	 */
	VOP_REMOTE_MOCK_UNKNOWN("vop.framework.remote.mock.unknown.type",
			"RemoteServiceCallMock getKeyForMockResponse(..) does not have a file naming block for request type."),

	/** SQS message generic send error message */
	VOP_SQS_MESSAGE_TRANSFER_ERROR("vop.framework.sqs.services.transfer.error","Could not send message to SQS"),
	/** SQS message send error message with exception message attached */
	VOP_SQS_MESSAGE_TRANSFER_ERROR_MESSAGE("vop.framework.sqs.services.transfer.message.error","Could not send message to SQS"),
	/** SQS message send message ID null */
	VOP_SQS_MESSAGE_TRANSFER_FAILED_MESSAGE_ID_NULL("vop.framework.sqs.services.transfer.message.id.null","Message sent to SQS but did not result in a message ID"),

	/** SQS create text message genericerror message */
	VOP_SQS_MESSAGE_CREATE_EXCEPTION("vop.framework.sqs.services.create.error","Could not create a text message."),
	/** SQS retrieve endpoint error message */
	VOP_SQS_ENDPOINT_RETRIEVE_EXCEPTION("vop.framework.sqs.services.retrieve.endpoint.error","Could not retrieve Sqs endpoint."),
	/** SQS create text message error message */
	VOP_SQS_MESSAGE_CREATE_EXCEPTION_MESSAGE("vop.framework.sqs.services.create.message.error","Could not create a text message."),
	/** SQS JMS message generic send error message */
	VOP_SQS_MESSAGE_CREATE_JMS_FAILED("vop.framework.sqs.services.create.jms.error","JMS had an issue."),
	/** SQS JMS message generic send error message */
	VOP_SQS_MESSAGE_CREATE_JMS_EXCEPTION_MESSAGE("vop.framework.sqs.services.create.jms.message.error","JMS had an issue."),
	/** SNS create topic error message */
	VOP_SNS_TOPIC_CREATE_EXCEPTION_MESSAGE("vop.framework.sns.services.create.topic.error","Could not create a SNS topic."),
	/** SNS retrieve topic arn error message */
	VOP_SNS_TOPICARN_RETRIEVE_EXCEPTION_MESSAGE("vop.framework.sns.services.retrieve.topicarn.error","Could not retrieve topic arn."),

	/** S3 upload file error message */
	VOP_S3_UPLOAD_FILE_EXCEPTION_MESSAGE("vop.framework.s3.services.upload.file.error","Could not upload file to an S3 bucket."),
	/** S3 delete object error message */
	VOP_S3_DELETE_OBJECT_EXCEPTION_MESSAGE("vop.framework.s3.services.delete.object.error","Could not delete an S3 object.")

	;

	/** The filename "name" part of the properties file to get from the classpath */
	private static final String PROPERTIES_FILE = "framework-messages";
	/** The spring message source */
	private static ReloadableResourceBundleMessageSource messageSource;
	/* Populate the message source from the properties file */
	static {
		messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:" + PROPERTIES_FILE);
		messageSource.setDefaultEncoding("UTF-8");
	}

	/** The key - must be identical to the key in framework-messages.properties */
	private String key;
	/** A default message, in case the key is not found in framework-messages.properties */
	private String defaultMessage;

	/**
	 * Construct keys with their property file counterpart key and a default message.
	 *
	 * @param key - the key as declared in the properties file
	 * @param defaultMessage - in case the key cannot be found
	 */
	private MessageKeys(final String key, final String defaultMessage) {
		this.key = key;
		this.defaultMessage = defaultMessage;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.wynd.vop.framework.messages.MessageKey#getKey()
	 */
	@Override
	public String getKey() {
		return this.key;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.wynd.vop.framework.messages.MessageKey#getMessage(java.lang.String[])
	 */
	@Override
	public String getMessage(final String... params) {
		return messageSource.getMessage(this.key, params, this.defaultMessage, Locale.getDefault());
	}
}
