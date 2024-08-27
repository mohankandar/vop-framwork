package com.wynd.vop.framework.rest.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wynd.vop.framework.AbstractBaseLogTester;
import com.wynd.vop.framework.exception.VopException;
import com.wynd.vop.framework.exception.VopExceptionData;
import com.wynd.vop.framework.exception.VopPartnerException;
import com.wynd.vop.framework.exception.VopPartnerRuntimeException;
import com.wynd.vop.framework.exception.VopRuntimeException;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.messages.MessageKey;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import com.wynd.vop.framework.rest.provider.ProviderResponse;
import com.wynd.vop.framework.sanitize.SanitizerException;
import io.micrometer.core.instrument.MeterRegistry;
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.*;
import javax.validation.constraints.NotBlank;
import javax.validation.groups.Default;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration
public class VopRestGlobalExceptionHandlerTest extends AbstractBaseLogTester {

	VopRestGlobalExceptionHandler vopRestGlobalExceptionHandler = new VopRestGlobalExceptionHandler();

	DummyObjectToBeValidated dummyObjectToBeValidated;

	private static final String[] params = new String[] {};
	private static final MessageKey TEST_KEY = MessageKeys.NO_KEY;
	private static final String TEST_KEY_TEXT = "NO_KEY";
	private static final String TEST_MESSAGE = "Test message";

	@SuppressWarnings("rawtypes")
	@Mock
	private ch.qos.logback.core.Appender mockAppender;

	@Mock
	private MeterRegistry meterRegistry;

	// Captor is genericised with ch.qos.logback.classic.spi.LoggingEvent
	@Captor
	private ArgumentCaptor<ch.qos.logback.classic.spi.LoggingEvent> captorLoggingEvent;

	// added the mockAppender to the root logger
	@Override
	@SuppressWarnings("unchecked")
	// It's not quite necessary but it also shows you how it can be done
	@Before
	public void setup() {
		VopLoggerFactory.getLogger(VopLogger.ROOT_LOGGER_NAME).getLoggerBoundImpl().addAppender(mockAppender);
	}

	// Always have this teardown otherwise we can stuff up our expectations.
	// Besides, it's
	// good coding practice
	@SuppressWarnings("unchecked")
	@After
	public void teardown() {
		VopLoggerFactory.getLogger(VopLogger.ROOT_LOGGER_NAME).getLoggerBoundImpl().detachAppender(mockAppender);
		SecurityContextHolder.clearContext();
	}

	@Test
	public void handleIllegalArgumentExceptionTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		IllegalArgumentException ex = new IllegalArgumentException("test illegal argument exception message");
		ResponseEntity<Object> response = vopRestGlobalExceptionHandler.handleIllegalArgumentException(req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
	}

	@Test
	public void handleIllegalStateExceptionTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		IllegalStateException ex = new IllegalStateException("test illegal state exception message");
		ResponseEntity<Object> response = vopRestGlobalExceptionHandler.handleIllegalStateException(req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void logSeverityNullTest() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Method logMethod = vopRestGlobalExceptionHandler.getClass().getDeclaredMethod("log", Exception.class,
				MessageKey.class, MessageSeverity.class, HttpStatus.class, String[].class);
		logMethod.setAccessible(true);

		/* debug, null severity, status */
		logMethod.invoke(vopRestGlobalExceptionHandler, new Exception(TEST_MESSAGE), TEST_KEY, (MessageSeverity) null,
				HttpStatus.ACCEPTED, params);
		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		ch.qos.logback.classic.spi.LoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertTrue("INFO".equals(loggingEvent.getLevel().toString()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void logInfoTest() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Method logMethod = vopRestGlobalExceptionHandler.getClass().getDeclaredMethod("log", Exception.class,
				MessageKey.class, MessageSeverity.class, HttpStatus.class, String[].class);
		logMethod.setAccessible(true);

		/* info, severity, status */
		logMethod.invoke(vopRestGlobalExceptionHandler, new Exception(TEST_MESSAGE), TEST_KEY, MessageSeverity.INFO,
				HttpStatus.ACCEPTED, params);
		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		ch.qos.logback.classic.spi.LoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertTrue("INFO".equals(loggingEvent.getLevel().toString()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void logDebugTest() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Method logMethod = vopRestGlobalExceptionHandler.getClass().getDeclaredMethod("log", Exception.class,
				MessageKey.class, MessageSeverity.class, HttpStatus.class, String[].class);
		logMethod.setAccessible(true);

		/* info, severity, status */
		logMethod.invoke(vopRestGlobalExceptionHandler, new Exception(TEST_MESSAGE), TEST_KEY, MessageSeverity.DEBUG,
				HttpStatus.ACCEPTED, params);
		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		ch.qos.logback.classic.spi.LoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertTrue("DEBUG".equals(loggingEvent.getLevel().toString()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void logWarnTest() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Method logMethod = vopRestGlobalExceptionHandler.getClass().getDeclaredMethod("log", Exception.class,
				MessageKey.class, MessageSeverity.class, HttpStatus.class, String[].class);
		logMethod.setAccessible(true);

		/* warn, severity, status */
		logMethod.invoke(vopRestGlobalExceptionHandler, new Exception(TEST_MESSAGE), TEST_KEY, MessageSeverity.WARN,
				HttpStatus.ACCEPTED, params);
		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		ch.qos.logback.classic.spi.LoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertTrue("WARN".equals(loggingEvent.getLevel().toString()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void logErrorNoStatusTest() throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Method logMethod = vopRestGlobalExceptionHandler.getClass().getDeclaredMethod("log", Exception.class,
				MessageKey.class, MessageSeverity.class, HttpStatus.class, String[].class);
		logMethod.setAccessible(true);

		/* error, severity, no status */
		logMethod.invoke(vopRestGlobalExceptionHandler, new Exception(TEST_MESSAGE), TEST_KEY, MessageSeverity.ERROR,
				HttpStatus.ACCEPTED, params);
		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		ch.qos.logback.classic.spi.LoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertTrue("ERROR".equals(loggingEvent.getLevel().toString()));
	}

	@Test
	public void deriveMessageTests() {
		// null exception
		String returnValue = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler, "deriveMessage",
				(Exception) null);
		assertTrue(returnValue.contains(VopRestGlobalExceptionHandler.NO_EXCEPTION_MESSAGE));

		// exception without cause
		returnValue = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler, "deriveMessage",
				new VopRuntimeException(TEST_KEY, MessageSeverity.DEBUG, HttpStatus.BAD_REQUEST));
		assertTrue(returnValue.equals(TEST_KEY_TEXT));

		// exception without cause or message
		returnValue = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler, "deriveMessage", new Exception());
		assertTrue(returnValue.contains(VopRestGlobalExceptionHandler.NO_EXCEPTION_MESSAGE));

		// exception with message; cause that has a message
		Exception cause = new IllegalStateException(TEST_MESSAGE);
		returnValue = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler, "deriveMessage",
				new Exception(TEST_MESSAGE, cause));
		assertTrue(returnValue.contains(TEST_MESSAGE));

		// exception with blank space message; cause that has a message
		cause = new IllegalStateException(TEST_MESSAGE);
		returnValue = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler, "deriveMessage",
				new Exception("  ", cause));
		assertTrue(returnValue.contains(VopRestGlobalExceptionHandler.NO_EXCEPTION_MESSAGE));

		// exception without message; cause that has a message
		cause = new IllegalStateException(TEST_MESSAGE);
		returnValue = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler, "deriveMessage",
				new Exception(cause));
		assertTrue(returnValue.contains(TEST_MESSAGE));

		// exception without message; cause that does not have a message
		cause = new IllegalStateException("");
		returnValue = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler, "deriveMessage",
				new Exception(cause));
		assertTrue(returnValue.contains(VopRestGlobalExceptionHandler.NO_EXCEPTION_MESSAGE));
	}

	@Test
	public void handleMethodArgumentNotValidExceptionTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		MethodParameter parameter = null;

		try {
			parameter = new MethodParameter(VopRestGlobalExceptionHandlerTest.this.getClass()
					.getMethod("methodForExtractingMethodObject", new Class[] { String.class }), 0);
		} catch (NoSuchMethodException e) {
			fail("Error mocking the parameter");
		} catch (SecurityException e) {
			fail("Error mocking the parameter");
		}

		BindingResult bindingResult = mock(BindingResult.class);
		MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

		List<FieldError> fieldErrors = new LinkedList<>();
		FieldError fe = new FieldError("test object name", "test field", "test rejected value", true,
				new String[] { "test code" }, new Object[] { "test argument" }, "test default message");
		fieldErrors.add(fe);

		when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

		List<ObjectError> objectErrors = new LinkedList<>();
		ObjectError oe = new ObjectError("test ObjectError objectName", "test ObjectError DefaultMessage");
		ReflectionTestUtils.setField(oe, "codes", new String[] { "code1", "code2" });
		objectErrors.add(oe);

		when(bindingResult.getGlobalErrors()).thenReturn(objectErrors);
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(
				TestConfigurationForAuditHttpServletResponseBean.class);
		VopRestGlobalExceptionHandler brgeh = annotationConfigApplicationContext
				.getBean(VopRestGlobalExceptionHandler.class);

		MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpServletRequest));

		/* Test with request and exception */
		ResponseEntity<Object> response = brgeh.handleMethodArgumentNotValidException(req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
		annotationConfigApplicationContext.close();

		/* Test with request and no exception */
		response = brgeh.handleMethodArgumentNotValidException(req, (MethodArgumentNotValidException) null);
		assertTrue(response.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR));
		annotationConfigApplicationContext.close();
	}

	public void methodForExtractingMethodObject(final String parameter) {

	}

	@Test
	public void handleHttpClientErrorExceptionTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "test status text", headers,
				"test body".getBytes(), Charset.defaultCharset());

		/* With request and exception */
		ResponseEntity<Object> response = vopRestGlobalExceptionHandler.handleHttpClientErrorException(req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));

		/* With request and no exception */
		response = vopRestGlobalExceptionHandler.handleHttpClientErrorException(req, (HttpClientErrorException) null);
		assertTrue(response.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR));
	}

	@Test
	public void handleMethodArgumentTypeMismatchTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		MethodParameter parameter = null;
		try {
			parameter = new MethodParameter(VopRestGlobalExceptionHandlerTest.this.getClass()
					.getMethod("methodForExtractingMethodObject", new Class[] { String.class }), 0);
		} catch (NoSuchMethodException e) {
			fail("Error mocking the parameter");
		} catch (SecurityException e) {
			fail("Error mocking the parameter");
		}
		MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException("test value", String.class,
				"test name", parameter, new Exception("test wrapped message"));
		ResponseEntity<Object> response = vopRestGlobalExceptionHandler.handleMethodArgumentTypeMismatch(req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
	}

	@Test
	public void handleConstraintViolationTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		VopRestGlobalExceptionHandlerTest.DummyObjectToBeValidated dummyObject = new VopRestGlobalExceptionHandlerTest.DummyObjectToBeValidated();
		dummyObject.dummyField = "";

		Set<? extends ConstraintViolation<?>> constaintViolations = validator.validate(dummyObject, Default.class);

		ConstraintViolationException ex = new ConstraintViolationException("test message", constaintViolations);
		ResponseEntity<Object> response = vopRestGlobalExceptionHandler.handleConstraintViolation(req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
	}

	@Test
	public void handleConstraintViolationWithNullArgumentForExceptionTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		VopRestGlobalExceptionHandlerTest.DummyObjectToBeValidated dummyObject = new VopRestGlobalExceptionHandlerTest.DummyObjectToBeValidated();
		dummyObject.dummyField = "";

		Set<? extends ConstraintViolation<?>> constaintViolations = validator.validate(dummyObject, Default.class);

		new ConstraintViolationException("test message", constaintViolations);
		ResponseEntity<Object> response = vopRestGlobalExceptionHandler.handleConstraintViolation(req, null);
		assertTrue(response.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR));
	}




	@Test
	public void handleHttpMessageNotWritableExceptionTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);

		HttpMessageNotWritableException ex = new HttpMessageNotWritableException("test msg",
				new Exception("wrapped message"));

		ResponseEntity<Object> response = vopRestGlobalExceptionHandler.handleHttpMessageConversionException(req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
	}

	@Test
	public void handleHttpMessageNotReadableJsonParseExceptionTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);

		HttpMessageNotReadableException ex = new HttpMessageNotReadableException("test msg",
				new JsonParseException(null, "wrapped json parse exception message"), new HttpInputMessage() {

					@Override
					public HttpHeaders getHeaders() {
						HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.TEXT_PLAIN);
						return headers;
					}

					@Override
					public InputStream getBody() throws IOException {
						return new ByteArrayInputStream("test body".getBytes());
					}
				});

		ResponseEntity<Object> response = vopRestGlobalExceptionHandler.handleHttpMessageConversionException(req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
		assertTrue(response.getBody() instanceof ProviderResponse);
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String responseBody = objectMapper.writeValueAsString(response.getBody());
			assertTrue(responseBody.contains("Invalid data type in request."));
		} catch (JsonProcessingException e) {
			fail("Error processing the response body");
		}
	}

	@Test
	public void handleHttpMessageNotReadableJsonPayloadMissingTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);

		HttpMessageNotReadableException ex = new HttpMessageNotReadableException("test msg",
				new Exception("required request body is missing"), new HttpInputMessage() {

			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.TEXT_PLAIN);
				return headers;
			}

			@Override
			public InputStream getBody() throws IOException {
				return null;
			}
		});

		ResponseEntity<Object> response = vopRestGlobalExceptionHandler.handleHttpMessageConversionException(req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
		assertTrue(response.getBody() instanceof ProviderResponse);
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String responseBody = objectMapper.writeValueAsString(response.getBody());
			assertTrue(responseBody.toLowerCase().contains("payload body missing"));
		} catch (Exception e) {
			fail("Error processing the response body");
		}
	}


	@Test
	public void handleHttpMessageNotReadableJsonMappingExceptionTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);

		HttpMessageNotReadableException ex = new HttpMessageNotReadableException("test msg",
				new JsonMappingException(null, "wrapped json mapping exception message"), new HttpInputMessage() {

					@Override
					public HttpHeaders getHeaders() {
						HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.TEXT_PLAIN);
						return headers;
					}

					@Override
					public InputStream getBody() throws IOException {
						return new ByteArrayInputStream("test body".getBytes());
					}
				});

		ResponseEntity<Object> response = vopRestGlobalExceptionHandler.handleHttpMessageConversionException(req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
		assertTrue(response.getBody() instanceof ProviderResponse);
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String responseBody = objectMapper.writeValueAsString(response.getBody());
			assertTrue(responseBody.contains("Unable to map request."));
		} catch (JsonProcessingException e) {
			fail("Error processing the response body");
		}
	}

	@Test
	public void handleBipRuntimeExceptionJsonMappingExceptionTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);

		VopRuntimeException vopRuntimeException = new VopRuntimeException(MessageKeys.NO_KEY, MessageSeverity.ERROR,
				HttpStatus.BAD_REQUEST, StringUtils.EMPTY);

		HttpMessageNotReadableException ex = new HttpMessageNotReadableException("test msg", JsonMappingException
				.from(mock(JsonParser.class), "wrapped json mapping exception message", vopRuntimeException),
				new HttpInputMessage() {

					@Override
					public HttpHeaders getHeaders() {
						HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.TEXT_PLAIN);
						return headers;
					}

					@Override
					public InputStream getBody() throws IOException {
						return new ByteArrayInputStream("test body".getBytes());
					}
				});

		ResponseEntity<Object> response = vopRestGlobalExceptionHandler.handleHttpMessageConversionException(req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
		assertTrue(response.getBody() instanceof ProviderResponse);
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String responseBody = objectMapper.writeValueAsString(response.getBody());
			assertTrue(responseBody.contains(MessageKeys.NO_KEY.getMessage(StringUtils.EMPTY)));
		} catch (JsonProcessingException e) {
			fail("Error processing the response body");
		}
	}
	
	@Test
	public void handleBipExceptionJsonMappingExceptionTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);

		VopException vopException = new VopException(MessageKeys.NO_KEY, MessageSeverity.ERROR,
				HttpStatus.BAD_REQUEST, StringUtils.EMPTY);

		HttpMessageNotReadableException ex = new HttpMessageNotReadableException("test msg", JsonMappingException
				.from(mock(JsonParser.class), "wrapped json mapping exception message", vopException),
				new HttpInputMessage() {

					@Override
					public HttpHeaders getHeaders() {
						HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.TEXT_PLAIN);
						return headers;
					}

					@Override
					public InputStream getBody() throws IOException {
						return new ByteArrayInputStream("test body".getBytes());
					}
				});

		ResponseEntity<Object> response = vopRestGlobalExceptionHandler.handleHttpMessageConversionException(req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
		assertTrue(response.getBody() instanceof ProviderResponse);
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String responseBody = objectMapper.writeValueAsString(response.getBody());
			assertTrue(responseBody.contains(MessageKeys.NO_KEY.getMessage(StringUtils.EMPTY)));
		} catch (JsonProcessingException e) {
			fail("Error processing the response body");
		}
	}

	@Test
	public void handleMissingServletRequestPartExceptionTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);

		MissingServletRequestPartException ex = new MissingServletRequestPartException("file");

		ResponseEntity<Object> response = vopRestGlobalExceptionHandler.handleMissingServletRequestPartException(req,
				ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
	}

	@Test
	public void handleNoHandlerFoundExceptionTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		NoHandlerFoundException ex = new NoHandlerFoundException("test msg", "wrapped message", headers);

		ResponseEntity<Object> response = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler,
				"handleNoHandlerFoundException", req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.NOT_FOUND));
	}

	@Test
	public void handleHttpRequestMethodNotSupportedTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		List<String> suppotedMethods = Arrays.asList(new String[] { "GET" });
		HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("test method name",
				suppotedMethods);

		ResponseEntity<Object> response = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler,
				"handleHttpRequestMethodNotSupported", req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.METHOD_NOT_ALLOWED));
	}

	@Test
	public void handleHttpMediaTypeNotSupportedTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		Arrays.asList(new String[] { "supported method1" });
		List<MediaType> supportedMediatypes = new LinkedList<>();
		supportedMediatypes.add(MediaType.TEXT_HTML);
		UnsupportedMediaTypeStatusException ex = new UnsupportedMediaTypeStatusException(MediaType.TEXT_PLAIN,
				supportedMediatypes);

		ResponseEntity<Object> response = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler,
				"handleHttpMediaTypeNotSupported", req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.UNSUPPORTED_MEDIA_TYPE));
	}

	@Test
	public void handleSanitizerExceptionTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		SanitizerException ex = new SanitizerException("test", new Exception("test exception"));

		ResponseEntity<Object> response = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler,
				"handleSanitizerException", req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
	}

	@Test
	public void handleBipRuntimeExceptionTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		VopRuntimeException ex = new VopRuntimeException(TEST_KEY, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST);

		ResponseEntity<Object> response = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler,
				"handleBipRuntimeException", req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
	}

	@Test
	public void handleAllTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		Exception ex = new Exception("test message");

		ResponseEntity<Object> response = vopRestGlobalExceptionHandler.handleAll(req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR));
	}

	@Test
	public void failSafeHandlerTest() {
		ResponseEntity<Object> response = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler,
				"failSafeHandler");
		assertTrue(response.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR));
	}

	@Test
	public void standardHandlerWithNullExceptionTest() {
		ResponseEntity<Object> response = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler,
				"standardHandler", null, HttpStatus.BAD_REQUEST);
		assertTrue(response.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR));
	}

	@Test
	public void standardHandlerWithNullMessagekeyTest()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		VopRuntimeException ex = new VopRuntimeException(MessageKeys.NO_KEY, MessageSeverity.DEBUG,
				HttpStatus.BAD_REQUEST);
		Field exceptionData = ex.getClass().getDeclaredField("exceptionData");
		exceptionData.setAccessible(true);
		exceptionData.set(ex,
				new VopExceptionData((MessageKey) null, ((VopExceptionData) exceptionData.get(ex)).getSeverity(),
						((VopExceptionData) exceptionData.get(ex)).getStatus(),
						((VopExceptionData) exceptionData.get(ex)).getParams()));

		ResponseEntity<Object> response = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler,
				"standardHandler", ex, HttpStatus.BAD_REQUEST);
		assertTrue(response.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR));
	}

	@Test
	public void handleBipPartnerRuntimeExceptionTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		VopPartnerRuntimeException ex = new VopPartnerRuntimeException(TEST_KEY, MessageSeverity.ERROR,
				HttpStatus.BAD_REQUEST);
		ResponseEntity<Object> response = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler,
				"handleBipPartnerRuntimeException", req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
	}

	@Test
	public void handleBipPartnerCheckedExceptionTest() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		VopPartnerException ex = new VopPartnerException(TEST_KEY, MessageSeverity.ERROR, HttpStatus.BAD_REQUEST);
		ResponseEntity<Object> response = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler,
				"handleBipPartnerCheckedException", req, ex);
		assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
	}

	@Test
	public void standardHandlerWithWarnTest() {
		ResponseEntity<Object> response = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler,
				"standardHandler", new Exception(), TEST_KEY, MessageSeverity.WARN, HttpStatus.OK, null);
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));
	}

	@Test
	public void standardHandlerWithNullException2Test() {
		ResponseEntity<Object> response = ReflectionTestUtils.invokeMethod(vopRestGlobalExceptionHandler,
				"standardHandler", null, TEST_KEY, MessageSeverity.WARN, HttpStatus.OK, null);
		assertTrue(response.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR));
	}

	static class DummyObjectToBeValidated {

		@NotBlank
		String dummyField;
	}

	@Configuration
	@ComponentScan(basePackages = { "com.wynd.vop.framework" })
	static class TestConfigurationForAuditHttpServletResponseBean {

	}

}
