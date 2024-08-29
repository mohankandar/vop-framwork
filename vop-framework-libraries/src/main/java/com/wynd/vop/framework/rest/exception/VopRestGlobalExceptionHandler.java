package com.wynd.vop.framework.rest.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wynd.vop.framework.audit.AuditEventData;
import com.wynd.vop.framework.audit.AuditEvents;
import com.wynd.vop.framework.exception.VopException;
import com.wynd.vop.framework.exception.VopExceptionExtender;
import com.wynd.vop.framework.exception.VopPartnerException;
import com.wynd.vop.framework.exception.VopPartnerRuntimeException;
import com.wynd.vop.framework.exception.VopRuntimeException;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.messages.MessageKey;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import com.wynd.vop.framework.rest.provider.Message;
import com.wynd.vop.framework.rest.provider.ProviderResponse;
import com.wynd.vop.framework.rest.provider.aspect.BaseHttpProviderPointcuts;
import com.wynd.vop.framework.sanitize.SanitizerException;
import com.wynd.vop.framework.util.HttpHeadersUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Objects;

/**
 * A global exception handler as the last line of defense before sending response to the service consumer.
 * This class converts exceptions to appropriate {@link Message} objects and puts them on the
 * response.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class VopRestGlobalExceptionHandler extends BaseHttpProviderPointcuts {

	/** The Constant LOGGER. */
	private static final VopLogger logger = VopLoggerFactory.getLogger(VopRestGlobalExceptionHandler.class);

	@Autowired
	private MessageSource messageSource;

	/**
	 * Return value if no exception exists to provide a message.
	 * To get default message text, use {@link #deriveMessage(Exception)}.
	 */
	protected static final String NO_EXCEPTION_MESSAGE = "Source exception has no message.";
	protected static final String PARSE_EXCEPTION_MESSAGE = "Invalid data type in request.";
	protected static final String MAPPING_EXCEPTION_MESSAGE = "Unable to map request.";
	protected static final String RUNTIME_EXCEPTION_MESSAGE = "A runtime exception occurred when attempting the request";
	protected static final String VOP_EXCEPTION_MESSAGE = "An exception occurred when attempting the request";
	protected static final String GENERIC_JSON_EXCEPTION_MESSAGE = "Invalid JSON payload";




	/**
	 * For java.lang.Exception and all subclasses.
	 * If exception message is empty, gets message of the cause if it exists.
	 *
	 * @param ex the Exception
	 * @return String the message
	 */
	private String deriveMessage(final Exception ex) {
		if (ex == null) {
			return NO_EXCEPTION_MESSAGE;
		}

		/*
		 * If exception message is empty, and if cause is not null,
		 * then cause class name will be in the exception message.
		 * So get the cause classname so we can scrub it out of the message.
		 */
		String causeClassname = (ex.getCause() == null
				? null
				: ex.getCause().getClass().getName() + ":");

		/* Scrub any occurrances of cause classname from exception message */
		String msg = (causeClassname != null && StringUtils.isNotBlank(ex.getMessage())
				? ex.getMessage().replaceAll(causeClassname, "")
				: ex.getMessage());

		/* Final check for empty */
		if (StringUtils.isBlank(msg)) {
			msg = NO_EXCEPTION_MESSAGE;
		}

		return msg;
	}


	/**
	 * For java.lang.Exception and all subclasses.
	 * If exception message is empty, gets message of the cause if it exists.
	 *
	 * @param key the message key
	 * @return String the message
	 */
	private String deriveMessageFromKey(final MessageKey key) {

		String msg = "";

		msg = key.getMessage();

		/* Final check for empty */
		if (StringUtils.isBlank(msg)) {
			msg = NO_EXCEPTION_MESSAGE;
		}

		return msg;
	}


	/**
	 * If key is null, returns the "NO-KEY" key.
	 *
	 * @param key - the initial string intended to represent the key
	 * @return MessageKey - the key, or NO_KEY
	 */
	private MessageKey deriveKey(final MessageKey key) {
		return ObjectUtils.defaultIfNull(key, MessageKeys.NO_KEY);
	}

	/**
	 * INFO logs the exception and its details.
	 *
	 * @param ex - the exception
	 * @param key - the key to use for reporting to support/maintenance
	 * @param severity - the MessageSeverity to report for the exception
	 * @param status - the status to report for the exception
	 * @param params - arguments to fill in any params in the MessageKey message (e.g. value for {0})
	 */
	private void log(final Exception ex, final MessageKey key, final MessageSeverity severity, final HttpStatus status,
			final String... params) {
		Level slf4jLevel = Level.INFO;
		if (severity != null) {
			slf4jLevel = severity.getLevel();
		}
		log(slf4jLevel, ex, key, severity, status, params);
	}

	/**
	 * Logs the exception and its details.
	 *
	 * @param level - the Log Level to log at
	 * @param ex - the exception
	 * @param key - the key to use for reporting to support/maintenance
	 * @param severity - the MessageSeverity to report for the exception
	 * @param status - the status to report for the exception
	 * @param params - arguments to fill in any params in the MessageKey message (e.g. value for {0})
	 */
	private void log(final Level level, final Exception ex, final MessageKey key, final MessageSeverity severity,
			final HttpStatus status, final String... params) {
		MessageKey derivedKey = deriveKey(key);
		String msg = status + "-" + severity + " "
				+ (ex == null ? "null" : ex.getClass().getName()) + " "
				+ derivedKey + ":" + derivedKey.getMessage(params);
		if (Level.ERROR.equals(level)) {
			logger.error(msg, ex);
		} else if (Level.WARN.equals(level)) {
			logger.warn(msg, ex);
		} else if (Level.INFO.equals(level)) {
			logger.info(msg, ex);
		} else {
			logger.debug(msg, ex);
		}
	}

	/**
	 * Write an audit log for the response object(s).
	 *
	 * @param response the response
	 * @param auditEventData the auditable annotation
	 */
	protected void audit(final Object response, final AuditEventData auditEventData, Throwable throwable) {
		super.auditServletResponse().writeHttpResponseAuditLog(response, auditEventData, MessageSeverity.ERROR, throwable);
	}

	/**
	 * A last resort to return a (somewhat) meaningful response to the consumer when there is no source exception.
	 *
	 * @return ResponseEntity the HTTP Response Entity
	 */
	protected ResponseEntity<Object> failSafeHandler() {
		log(Level.ERROR, null, MessageKeys.NO_KEY, MessageSeverity.FATAL, HttpStatus.INTERNAL_SERVER_ERROR);
		ProviderResponse apiError = new ProviderResponse();
		apiError.addMessage(MessageSeverity.FATAL, MessageKeys.NO_KEY.getKey(), MessageKeys.NO_KEY.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR);
		return new ResponseEntity<>(apiError, HttpHeadersUtil.buildHttpHeadersForError(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Standard exception handler for any exception that implements {@link VopExceptionExtender}.
	 *
	 * @param ex - the exception that implements VopExceptionExtender
	 * @param httpResponseStatus - the status to put on the HTTP Response Entity.
	 * @return ResponseEntity - the HTTP Response Entity
	 */
	protected ResponseEntity<Object> standardHandler(final VopExceptionExtender ex, final HttpStatus httpResponseStatus) {
		if ((ex == null) || (ex.getExceptionData().getMessageKey() == null)) {
			return failSafeHandler();
		}
		return standardHandler((Exception) ex, ex.getExceptionData().getMessageKey(), ex.getExceptionData().getSeverity(),
				httpResponseStatus);
	}

	/**
	 * Standard exception handler for any Exception.
	 *
	 * @param ex - the Exception
	 * @param key - the key to use for reporting to support/maintenance
	 * @param severity - the MessageSeverity to report for the exception
	 * @param httpResponseStatus - the status to put on the HTTP Response Entity.
	 * @param params - arguments to fill in any params in the MessageKey message (e.g. value for {0})
	 * @return ResponseEntity the HTTP Response Entity
	 */
	protected ResponseEntity<Object> standardHandler(final Exception ex, final MessageKey key, final MessageSeverity severity,
			final HttpStatus httpResponseStatus, final String... params) {
		if (ex == null) {
			return failSafeHandler();
		}
		ProviderResponse apiError = new ProviderResponse();

		MessageKey derivedKey = deriveKey(key);
		log(ex, derivedKey, severity, httpResponseStatus, params);
		apiError.addMessage(severity, derivedKey.getKey(), deriveMessage(ex), httpResponseStatus);

		return new ResponseEntity<>(apiError, HttpHeadersUtil.buildHttpHeadersForError(), httpResponseStatus);
	}

	/**
	 * Standard exception handler for any Exception.
	 *
	 * @param ex - the Exception
	 * @param key - the key to use for reporting to support/maintenance
	 * @param severity - the MessageSeverity to report for the exception
	 * @param httpResponseStatus - the status to put on the HTTP Response Entity.
	 * @param params - arguments to fill in any params in the MessageKey message (e.g. value for {0})
	 * @return ResponseEntity the HTTP Response Entity
	 */
	protected ResponseEntity<Object> standardHandler(final Exception ex, final MessageKey key, final MessageSeverity severity,
													 final HttpStatus httpResponseStatus, final Boolean hideExceptionMessage, final String... params) {
		if (ex == null) {
			return failSafeHandler();
		}
		ProviderResponse apiError = new ProviderResponse();

		MessageKey derivedKey = deriveKey(key);
		log(ex, derivedKey, severity, httpResponseStatus, params);

		if (hideExceptionMessage){
			apiError.addMessage(severity, derivedKey.getKey(), deriveMessageFromKey(key), httpResponseStatus);
		}else{
			apiError.addMessage(severity, derivedKey.getKey(), deriveMessage(ex), httpResponseStatus);
		}

		return new ResponseEntity<>(apiError, HttpHeadersUtil.buildHttpHeadersForError(), httpResponseStatus);
	}

	// 400

	/**
	 * Handle VopPartnerRuntimeException.
	 *
	 * @param req the req
	 * @param ex the ex
	 * @return the response entity
	 */
	@ExceptionHandler(value = VopPartnerRuntimeException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public final ResponseEntity<Object> handleVopPartnerRuntimeException(final HttpServletRequest req,
			final VopPartnerRuntimeException ex) {
		return standardHandler(ex, HttpStatus.BAD_REQUEST);
	}


	/**
	 * Handle VopPartnerException.
	 *
	 * @param req the req
	 * @param ex the ex
	 * @return the response entity
	 */
	@ExceptionHandler(value = VopPartnerException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public final ResponseEntity<Object> handleVopPartnerCheckedException(final HttpServletRequest req, final VopPartnerException ex) {
		return standardHandler(ex, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle illegal argument exception.
	 *
	 * @param req the req
	 * @param ex the ex
	 * @return the response entity
	 */
	@ExceptionHandler(value = IllegalArgumentException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public final ResponseEntity<Object> handleIllegalArgumentException(final HttpServletRequest req,
			final IllegalArgumentException ex) {
		return standardHandler(ex, MessageKeys.NO_KEY, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle illegal state exceptions - developer attempting to instantiate a class that is for statics.
	 *
	 * @param req the req
	 * @param ex the ex
	 * @return the response entity
	 */
	@ExceptionHandler(value = IllegalStateException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public final ResponseEntity<Object> handleIllegalStateException(final HttpServletRequest req, final IllegalStateException ex) {
		return standardHandler(ex, MessageKeys.VOP_DEV_ILLEGAL_INSTANTIATION, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle {@code @Valid} failures when method argument is not valid.
	 *
	 * @param req the req
	 * @param ex the ex
	 * @return the response entity
	 */
	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public final ResponseEntity<Object> handleMethodArgumentNotValidException(final HttpServletRequest req,
			final MethodArgumentNotValidException ex) {

		final ProviderResponse apiError = new ProviderResponse();
		if (ex == null) {
			return failSafeHandler();
		} else {
			MessageKey key = MessageKeys.VOP_GLOBAL_VALIDATOR_METHOD_ARGUMENT_NOT_VALID;
			for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
				String errorCodes = String.join(", ", error.getCodes());
				String message = messageSource.getMessage(error, Locale.US);
				if (StringUtils.isEmpty(message)) {
					message = error.getDefaultMessage();
				}
				String[] params = new String[] { "field", errorCodes, message };
				log(ex, key, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, params);
				apiError.addMessage(MessageSeverity.ERROR, errorCodes,
						message, HttpStatus.BAD_REQUEST);
			}
			for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
				String errorCodes = String.join(", ", error.getCodes());
				String message = messageSource.getMessage(error, Locale.US);
				if (StringUtils.isEmpty(message)) {
					message = error.getDefaultMessage();
				}
				String[] params = new String[] { "object", errorCodes, message };
				log(ex, key, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, params);
				apiError.addMessage(MessageSeverity.ERROR, errorCodes,
						message, HttpStatus.BAD_REQUEST);
			}
		}

		audit(apiError, new AuditEventData(AuditEvents.API_REST_REQUEST, "jsr303Validation", req.getPathInfo()), ex);

		return new ResponseEntity<>(apiError, HttpHeadersUtil.buildHttpHeadersForError(), HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle HTTP 4xx response in http client.
	 *
	 * @param req the req
	 * @param httpClientErrorException the http client error exception
	 * @return the response entity
	 */
	@ExceptionHandler(value = HttpClientErrorException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public final ResponseEntity<Object> handleHttpClientErrorException(final HttpServletRequest req,
			final HttpClientErrorException httpClientErrorException) {

		ProviderResponse apiError = new ProviderResponse();
		if (httpClientErrorException == null) {
			return failSafeHandler();
		} else {
			MessageKey key = MessageKeys.VOP_GLOBAL_HTTP_CLIENT_ERROR;
			HttpStatus status = httpClientErrorException.getStatusCode();
			String statusReason = httpClientErrorException.getStatusCode().getReasonPhrase();
			String[] params = new String[] { statusReason, httpClientErrorException.getMessage() };

			log(httpClientErrorException, key, MessageSeverity.ERROR, status, params);

			byte[] responseBody = httpClientErrorException.getResponseBodyAsByteArray();

			try {
				apiError = new ObjectMapper().readValue(responseBody, ProviderResponse.class);
			} catch (IOException e) {
				log(e, MessageKeys.VOP_GLOBAL_GENERAL_EXCEPTION, MessageSeverity.ERROR, status,
						params);
				apiError.addMessage(MessageSeverity.ERROR, key.getKey(),
						new String(responseBody, Charset.defaultCharset()),
						httpClientErrorException.getStatusCode());
			}
		}

		return new ResponseEntity<>(apiError, HttpHeadersUtil.buildHttpHeadersForError(), HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle method argument type mismatch.
	 *
	 * @param req the req
	 * @param ex the ex
	 * @return the response entity
	 */
	@ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public final ResponseEntity<Object> handleMethodArgumentTypeMismatch(final HttpServletRequest req,
			final MethodArgumentTypeMismatchException ex) {

		MessageKey key = MessageKeys.VOP_GLOBAL_REST_API_TYPE_MISMATCH;
		String[] params = new String[] { ex.getName(), ex.getRequiredType().getName() };

		log(Level.ERROR, ex, key, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, params);

		final ProviderResponse apiError = new ProviderResponse();
		apiError.addMessage(MessageSeverity.ERROR, key.getKey(),
				key.getMessage(params), HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(apiError, HttpHeadersUtil.buildHttpHeadersForError(), HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle constraint violation.
	 *
	 * @param req the req
	 * @param ex the ex
	 * @return the response entity
	 */
	@ExceptionHandler(value = ConstraintViolationException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public final ResponseEntity<Object> handleConstraintViolation(final HttpServletRequest req,
			final ConstraintViolationException ex) {

		final ProviderResponse apiError = new ProviderResponse();
		if ((ex == null) || (ex.getConstraintViolations() == null)) {
			return failSafeHandler();
		} else {
			MessageKey key = MessageKeys.VOP_GLOBAL_VALIDATOR_CONSTRAINT_VIOLATION;
			for (final ConstraintViolation<?> violation : ex.getConstraintViolations()) {
				String[] params =
						new String[] { violation.getRootBeanClass().getName(), violation.getPropertyPath().toString(),
								violation.getMessage() };
				log(ex, key, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST, params);
				apiError.addMessage(MessageSeverity.ERROR, key.getKey(),
						key.getMessage(params), HttpStatus.BAD_REQUEST);
			}
		}
		return new ResponseEntity<>(apiError, HttpHeadersUtil.buildHttpHeadersForError(), HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handle HTTP message not readable exception.
	 *
	 * @param req
	 *            the req
	 * @param httpHttpMessageConversionException
	 *            the http message not readable or writable exception
	 * @return the response entity
	 */
	@ExceptionHandler(value = HttpMessageConversionException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public final ResponseEntity<Object> handleHttpMessageConversionException(final HttpServletRequest req,
			final HttpMessageConversionException httpHttpMessageConversionException) {
		return jsonExceptionHandler(httpHttpMessageConversionException);
	}
	
	/**
	 * Handle MissingServletRequestPartException.
	 *
	 * @param req the req
	 * @param ex the ex
	 * @return the response entity
	 */
	@ExceptionHandler(value = MissingServletRequestPartException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public final ResponseEntity<Object> handleMissingServletRequestPartException(final HttpServletRequest req,
			final MissingServletRequestPartException ex) {
		return standardHandler(ex, MessageKeys.NO_KEY, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST);
	}







	/**
	 * JSON exception handler to support processing
	 * HttpMessageNotReadableException.
	 *
	 * @param httpHttpMessageConversionException
	 *            the HTTP message not readable exception
	 * @return the response entity
	 */
	private ResponseEntity<Object> jsonExceptionHandler(
			final HttpMessageConversionException httpHttpMessageConversionException) {
		String jsonOriginalMessage = StringUtils.EMPTY;
		String messageKey = MessageKeys.NO_KEY.getKey();
		String clientResponseMessage = StringUtils.EMPTY;
		final Throwable mostSpecificCause = httpHttpMessageConversionException.getMostSpecificCause();
		if (mostSpecificCause instanceof JsonParseException) {
			JsonParseException jpe = (JsonParseException) mostSpecificCause;
			jsonOriginalMessage = jpe.getOriginalMessage();
			logger.error(jsonOriginalMessage);
			clientResponseMessage = PARSE_EXCEPTION_MESSAGE;
		} else if (mostSpecificCause instanceof JsonMappingException) {
			JsonMappingException jme = (JsonMappingException) mostSpecificCause;
			jsonOriginalMessage = jme.getOriginalMessage();
			logger.error(jsonOriginalMessage);
			clientResponseMessage = MAPPING_EXCEPTION_MESSAGE;
		} else if (mostSpecificCause instanceof VopRuntimeException) {
			VopRuntimeException vopRuntimeException = (VopRuntimeException) mostSpecificCause;
			messageKey = vopRuntimeException.getExceptionData().getKey();
			jsonOriginalMessage = vopRuntimeException.getMessage();
			logger.error(jsonOriginalMessage);
			clientResponseMessage = RUNTIME_EXCEPTION_MESSAGE;
		} else if (mostSpecificCause instanceof VopException) {
			VopException vopException = (VopException) mostSpecificCause;
			messageKey = vopException.getExceptionData().getKey();
			jsonOriginalMessage = vopException.getMessage();
			logger.error(jsonOriginalMessage);
			clientResponseMessage = VOP_EXCEPTION_MESSAGE;
		}

		final ProviderResponse apiError = new ProviderResponse();
		if (!StringUtils.isEmpty(clientResponseMessage)) {
			apiError.addMessage(MessageSeverity.ERROR, messageKey,
					clientResponseMessage, HttpStatus.BAD_REQUEST);
			return new ResponseEntity<>(apiError, HttpHeadersUtil.buildHttpHeadersForError(), HttpStatus.BAD_REQUEST);

		} else {


			// handle required payload empty
			if (Objects.requireNonNull(httpHttpMessageConversionException.getMessage()).toLowerCase().contains("required request body is missing")){
				return standardHandler(httpHttpMessageConversionException, MessageKeys.VOP_TOKEN_PAYLOAD_MISSING, MessageSeverity.ERROR,
						HttpStatus.BAD_REQUEST, true);
			}

			logger.error(httpHttpMessageConversionException.getMessage());
			apiError.addMessage(MessageSeverity.ERROR, messageKey,
					GENERIC_JSON_EXCEPTION_MESSAGE, HttpStatus.BAD_REQUEST);
			return new ResponseEntity<>(apiError,
					HttpHeadersUtil.buildHttpHeadersForError(), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Handle no handler found exception.
	 *
	 * @param req the req
	 * @param ex the ex
	 * @return the response entity
	 */
	@ExceptionHandler(value = NoHandlerFoundException.class)
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public final ResponseEntity<Object> handleNoHandlerFoundException(final HttpServletRequest req, final NoHandlerFoundException ex) {
		return standardHandler(ex, MessageKeys.NO_KEY, MessageSeverity.ERROR, HttpStatus.NOT_FOUND);
	}

	// 405

	/**
	 * Handle http request method not supported.
	 *
	 * @param req the req
	 * @param ex the ex
	 * @return the response entity
	 */
	@ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
	public final ResponseEntity<Object> handleHttpRequestMethodNotSupported(final HttpServletRequest req,
			final HttpRequestMethodNotSupportedException ex) {
		return standardHandler(ex, MessageKeys.NO_KEY, MessageSeverity.ERROR, HttpStatus.METHOD_NOT_ALLOWED);
	}

	// 415

	/**
	 * Handle http media type not supported.
	 *
	 * @param req the req
	 * @param ex the ex
	 * @return the response entity
	 */
	@ExceptionHandler(value = UnsupportedMediaTypeStatusException.class)
	@ResponseStatus(value = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
	public final ResponseEntity<Object> handleHttpMediaTypeNotSupported(final HttpServletRequest req,
			final UnsupportedMediaTypeStatusException ex) {
		return standardHandler(ex, MessageKeys.NO_KEY, MessageSeverity.ERROR, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
	}

	// Handle all

	/**
	 * Handle vop runtime exception.
	 *
	 * @param req the req
	 * @param ex the ex
	 * @return the response entity
	 */
	@ExceptionHandler(value = VopRuntimeException.class)
	public final ResponseEntity<Object> handleVopRuntimeException(final HttpServletRequest req, final VopRuntimeException ex) {
		return standardHandler(ex, ex.getExceptionData().getStatus());
	}

	/**
	 * Handle sanitizer exception.
	 *
	 * @param req the req
	 * @param ex the ex
	 * @return the response entity
	 */
	@ExceptionHandler(value = SanitizerException.class)
	public final ResponseEntity<Object> handleSanitizerException(final HttpServletRequest req, final SanitizerException ex) {
		return standardHandler(ex, MessageKeys.VOP_SECURITY_SANITIZE_FAIL, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST,
				ex.getMessage());
	}

	/**
	 * Handle all.
	 *
	 * @param req the req
	 * @param ex the ex
	 * @return the response entity
	 */
	@ExceptionHandler(value = Exception.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public final ResponseEntity<Object> handleAll(final HttpServletRequest req, final Exception ex) {
		return standardHandler(ex, MessageKeys.NO_KEY, MessageSeverity.FATAL, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
