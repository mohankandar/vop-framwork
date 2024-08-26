package com.wynd.vop.framework.audit.http;

import com.wynd.vop.framework.audit.AuditEventData;
import com.wynd.vop.framework.audit.AuditLogger;
import com.wynd.vop.framework.audit.BaseAsyncAudit;
import com.wynd.vop.framework.audit.model.HttpRequestAuditData;
import com.wynd.vop.framework.audit.model.HttpResponseAuditData;
import com.wynd.vop.framework.constants.VopConstants;
import com.wynd.vop.framework.exception.VopRuntimeException;
import com.wynd.vop.framework.log.VopBanner;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import com.wynd.vop.framework.sanitize.Sanitizer;
import com.wynd.vop.framework.util.HttpHeadersUtil;
import com.wynd.vop.framework.validation.Defense;
import com.wynd.vop.framework.rest.provider.ProviderResponse;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Performs audit logging specifically for HttpServlet request/response objects.
 *

 */
@Component
public class AuditHttpRequestResponse {
	/** Class logger */
	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(AuditHttpRequestResponse.class);

	@Autowired
	protected BaseAsyncAudit baseAsyncAudit;

	/**
	 * Protected constructor.
	 */
	public AuditHttpRequestResponse() {
		super();
	}
	
	/**
	 * Make sure the class was initialized properly
	 */
	@PostConstruct
	public void postConstruct() {
		Defense.notNull(baseAsyncAudit);
	}


	/**
	 * Provides access to audit operations related to logging the servlet Request in a fluent way.
	 *
	 * @return AuditServletRequest - the container for request audit operations
	 */
	public AuditHttpServletRequest auditServletRequest() {
		return new AuditHttpServletRequest();
	}

	/**
	 * Container class for audit operations related to logging the servlet Request.
	 *
	
	 */
	public class AuditHttpServletRequest {

		/**
		 * Write audit log for HTTP request.
		 *
		 * @param requests - the request object
		 * @param auditEventData - the audit meta-data for the event
		 */
		public void writeHttpRequestAuditLog(final List<Object> requests, final AuditEventData auditEventData) {

			LOGGER.debug("RequestContextHolder.getRequestAttributes() {}", RequestContextHolder.getRequestAttributes());

			final HttpRequestAuditData requestAuditData = new HttpRequestAuditData();

			final HttpServletRequest httpServletRequest =
					((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

			getHttpRequestAuditData(httpServletRequest, requestAuditData, requests);

			baseAsyncAudit.writeRequestAuditLog(requestAuditData, auditEventData, MessageSeverity.INFO, null,
					HttpRequestAuditData.class);
		}

		/**
		 * Add request header information, and any multipart/form or multipart/mixed data, to the audit data.
		 *
		 * @param httpServletRequest the servlet request
		 * @param requestAuditData the audit data object
		 */
		private void getHttpRequestAuditData(final HttpServletRequest httpServletRequest,
				final HttpRequestAuditData requestAuditData, final List<Object> requests) {

			ArrayList<String> listOfHeaderNames = Collections.list(httpServletRequest.getHeaderNames());
			final Map<String, String> headers = populateRequestHeadersMap(httpServletRequest, listOfHeaderNames);

			requestAuditData.setHeaders(headers);
			requestAuditData.setUri(httpServletRequest.getRequestURI());
			requestAuditData.setMethod(httpServletRequest.getMethod());
			requestAuditData.setRequest(requests);
			
			final String contentType = httpServletRequest.getContentType();

			LOGGER.debug("Content Type: {}", Sanitizer.stripXss(contentType));

			if ((contentType != null) && (contentType.toLowerCase(Locale.ENGLISH).startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)
					|| contentType.toLowerCase(Locale.ENGLISH).startsWith(VopConstants.MIME_MULTIPART_MIXED))) {

				final List<String> attachmentTextList = getMultipartHeaders(httpServletRequest);
				requestAuditData.setAttachmentTextList(attachmentTextList);
				requestAuditData.setRequest(null);
			} else if ((contentType != null)
					&& (contentType.toLowerCase(Locale.ENGLISH).startsWith(MediaType.APPLICATION_OCTET_STREAM_VALUE))) {
				LinkedList<String> linkedList = new LinkedList<>();
				for (Object eachRequest : requests) {
					if (eachRequest instanceof Resource) {
						Resource resource = (Resource) eachRequest;
						addStringOfSetSizeFromResource(linkedList, resource);
					}
				}
				requestAuditData.setAttachmentTextList(linkedList);
				requestAuditData.setRequest(null);
			}
		}

		private void addStringOfSetSizeFromResource(final LinkedList<String> linkedList, final Resource resource) {
			InputStream in = null;
			try {
				in = resource.getInputStream();
				linkedList.add(BaseAsyncAudit.convertBytesOfSetSizeToString(in));
			} catch (IOException e) {
				LOGGER.error("Could not read Http Request", e);
			} finally {
				BaseAsyncAudit.closeInputStreamIfRequired(in);
			}
		}

		/**
		 * Add request multipart/form or multipart/mixed header information to the audit data.
		 *
		 * @param httpServletRequest the servlet request
		 * @return List of the headers in key/value string format
		 */
		private List<String> getMultipartHeaders(final HttpServletRequest httpServletRequest) {
			final List<String> multipartHeaders = new LinkedList<>();
			InputStream inputstream = null;
			try {
				for (final Part part : httpServletRequest.getParts()) {
					final Map<String, String> partHeaders = new HashMap<>();
					for (final String headerName : part.getHeaderNames()) {
						String value;
						value = part.getHeader(headerName);
						partHeaders.put(headerName, value);
					}

					try {
						inputstream = part.getInputStream();
						multipartHeaders
						.add(partHeaders.toString() + ", " + BaseAsyncAudit.convertBytesOfSetSizeToString(inputstream));
					} finally {
						BaseAsyncAudit.closeInputStreamIfRequired(inputstream);
					}
				}
			} catch (final Exception ex) {
				LOGGER.error(VopBanner.newBanner(VopConstants.INTERCEPTOR_EXCEPTION, Level.ERROR),
						"Error occurred while reading the upload file. {}", ex);
			}
			return multipartHeaders;
		}

		/**
		 * Copies headers in the servlet request into a Map.
		 *
		 * @param httpServletRequest
		 * @param headersToBePopulated
		 * @param listOfHeaderNames
		 */
		private Map<String, String> populateRequestHeadersMap(final HttpServletRequest httpServletRequest,
				final Collection<String> listOfHeaderNames) {

			final Map<String, String> headersToBePopulated = new HashMap<>();
			for (final String headerName : listOfHeaderNames) {
				String value;
				value = httpServletRequest.getHeader(headerName);
				headersToBePopulated.put(headerName, value);
			}
			return headersToBePopulated;
		}

	}

	/**
	 * Provides access to audit operations related to logging the servlet Response in a fluent way.
	 *
	 * @return AuditServletResponse - the container for response audit operations
	 */
	public AuditHttpServletResponse auditServletResponse() {
		return new AuditHttpServletResponse();
	}

	/**
	 * Provides access to audit operations related to logging the servlet Response in a fluent way.
	 *
	 * @return AuditServletResponse - the container for response audit operations
	
	 */
	public class AuditHttpServletResponse {

		/**
		 * Write audit log for HTTP response.
		 *
		 * @param response - the HTTP response
		 * @param auditEventData - the audit event meta-data
		 * @param severity - the Message Severity, if {@code null} then MessageSeverity.INFO is used
		 * @param t - a throwable, if relevant (may be {@code null})
		 */
		public void writeHttpResponseAuditLog(final Object response, final AuditEventData auditEventData,
				final MessageSeverity severity, final Throwable t) {

			final HttpServletResponse httpServletResponse =
					((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();

			final HttpResponseAuditData responseAuditData = new HttpResponseAuditData();

			if (httpServletResponse != null) {
				getHttpResponseAuditData(httpServletResponse, responseAuditData);
			}

			baseAsyncAudit.writeResponseAuditLog(response, responseAuditData, auditEventData, severity, t);
		}

		/**
		 * Add response header information to the audit data.
		 *
		 * @param httpServletResponse the servlet response
		 * @param responseAuditData the container to put the header info in
		 */
		private void getHttpResponseAuditData(final HttpServletResponse httpServletResponse,
				final HttpResponseAuditData responseAuditData) {
			final Map<String, String> headers = new HashMap<>();
			final Collection<String> headerNames = httpServletResponse.getHeaderNames();

			for (final String headerName : headerNames) {
				String value;
				value = httpServletResponse.getHeader(headerName);
				headers.put(headerName, value);
			}

			String contentType = httpServletResponse.getContentType();
			if ((contentType != null) && contentType.equalsIgnoreCase(MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
				ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpServletResponse);
				ByteArrayInputStream byteStream = new ByteArrayInputStream(responseWrapper.getContentAsByteArray());
				final LinkedList<String> linkedList = addStringOfSetSize(byteStream);
				forwardDataInBodyToResponse(responseWrapper);
				responseAuditData.setAttachmentTextList(linkedList);
			}
			responseAuditData.setHeaders(headers);
		}

		private LinkedList<String> addStringOfSetSize(final ByteArrayInputStream byteStream) {
			LinkedList<String> linkedList = new LinkedList<>();
			try {
				linkedList.add(BaseAsyncAudit.convertBytesOfSetSizeToString(byteStream));
			} finally {
				BaseAsyncAudit.closeInputStreamIfRequired(byteStream);
			}
			return linkedList;
		}

		private void forwardDataInBodyToResponse(final ContentCachingResponseWrapper responseWrapper) {
			try {
				responseWrapper.copyBodyToResponse();
			} catch (IOException ioe) {
				LOGGER.error("Could not continue copying the response", ioe);
				throw new VopRuntimeException(MessageKeys.VOP_AUDIT_ASPECT_ERROR_UNEXPECTED, MessageSeverity.ERROR,
						HttpStatus.INTERNAL_SERVER_ERROR, "");
			}
		}

	}

	/**
	 * Standard handling of exceptions that are thrown from within the advice
	 * (not exceptions thrown by application code).
	 *
	 * @param adviceName the name of the advice method in which the exception was thrown
	 * @param attemptingTo the attempted task that threw the exception
	 * @param auditEventData the audit event data object
	 * @param throwable the exception that was thrown
	 */
	protected ResponseEntity<ProviderResponse> handleInternalException(final String adviceName, final String attemptingTo,
			final AuditEventData auditEventData, final Throwable throwable) {
		try {
			MessageKeys key = MessageKeys.VOP_AUDIT_ASPECT_ERROR_UNEXPECTED;
			final VopRuntimeException vopRuntimeException = new VopRuntimeException(key,
					MessageSeverity.FATAL, HttpStatus.INTERNAL_SERVER_ERROR, throwable, adviceName, attemptingTo);

			String msg = "Error ServiceMessage: " + vopRuntimeException;
			AuditLogger.error(auditEventData, msg, vopRuntimeException);
			LOGGER.error(adviceName + " auditing uncaught exception.", vopRuntimeException);

			final ProviderResponse providerResponse = new ProviderResponse();
			providerResponse.addMessage(MessageSeverity.FATAL, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
					vopRuntimeException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

			return new ResponseEntity<>(providerResponse, HttpHeadersUtil.buildHttpHeadersForError(), HttpStatus.INTERNAL_SERVER_ERROR);

		} catch (Throwable e) { // NOSONAR intentionally catching throwable
			return handleAnyRethrownExceptions(adviceName, throwable, e);
		}
	}

	/**
	 * If audit is having troubles, this is the last resort for logging and returning meaningful
	 * exception information.
	 *
	 * @param adviceName - the advice from which the audit is being called
	 * @param originatingThrowable - the exception that started all of this
	 * @param e - the current exception
	 * @return ResponseEntity - the ProviderResponse with available information
	 */
	private ResponseEntity<ProviderResponse> handleAnyRethrownExceptions(final String adviceName, final Throwable originatingThrowable,
			final Throwable e) {
		ResponseEntity<ProviderResponse> entity;
		MessageKeys key = MessageKeys.VOP_AUDIT_ASPECT_ERROR_CANNOT_AUDIT;
		String msg = key.getMessage(adviceName, originatingThrowable.getClass().getSimpleName());
		LOGGER.error(VopBanner.newBanner(VopConstants.INTERCEPTOR_EXCEPTION, Level.ERROR),
				msg, e);

		ProviderResponse body = new ProviderResponse();
		body.addMessage(MessageSeverity.FATAL, HttpStatus.INTERNAL_SERVER_ERROR.name(),
				msg, HttpStatus.INTERNAL_SERVER_ERROR);
		entity = new ResponseEntity<>(body, HttpHeadersUtil.buildHttpHeadersForError(), HttpStatus.INTERNAL_SERVER_ERROR);
		return entity;
	}

}
