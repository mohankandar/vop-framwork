package com.wynd.vop.framework.audit;

import com.wynd.vop.framework.audit.model.MessageAuditData;
import com.wynd.vop.framework.audit.model.ResponseAuditData;
import com.wynd.vop.framework.constants.VopConstants;
import com.wynd.vop.framework.exception.VopRuntimeException;
import com.wynd.vop.framework.log.VopBanner;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import com.wynd.vop.framework.validation.Defense;
import com.wynd.vop.framework.audit.model.HttpResponseAuditData;
import com.wynd.vop.framework.audit.model.RequestAuditData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Performs simple audit logging on any type of request or response objects.
 *

 */
@Component
public class BaseAsyncAudit {
	/** Class logger */
	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(BaseAsyncAudit.class);

	private static final String INTERNAL_EXCEPTION_PREFIX = "Error ServiceMessage: ";

	/** How many bytes of an uploaded file will be read for inclusion in the audit record */
	public static final int NUMBER_OF_BYTES_TO_LIMIT_AUDIT_LOGGED_OBJECT = 1024;

	@Autowired
	AuditLogSerializer auditLogSerializer;

	/**
	 * Instantiate the class.
	 */
	public BaseAsyncAudit() {
		super();
	}

	/**
	 * Make sure the class was initialized properly
	 */
	@PostConstruct
	public void postConstruct() {
		Defense.notNull(auditLogSerializer);
	}

	/**
	 * Get the asynchronous logger to initiate logging audit data.
	 *
	 * @return AuditLogSerializer
	 */
	public AuditLogSerializer getAsyncLogger() {
		return auditLogSerializer;
	}
	
	/**
	 * Write any kind of message object list to the audit logs.
	 *
	 * @param messageAuditData - the {@link AuditableData} container to put the message in
	 * @param auditEventData - the audit meta-data for the event
	 * @param severity - the Message Severity, if {@code null} then MessageSeverity.INFO is used
	 * @param t - a throwable, if relevant (may be {@code null})
	 * @param auditDataclass the audit dataclass
	 */
	public void writeMessageAuditLog(final MessageAuditData messageAuditData,
			final AuditEventData auditEventData, final MessageSeverity severity, final Throwable t, final Class<?> auditDataclass) {

		getAsyncLogger().asyncAuditRequestResponseData(auditEventData, messageAuditData, auditDataclass,
				severity, t);
	}

	/**
	 * Write any kind of request object list to the audit logs.
	 *
	 * @param requestAuditData - the {@link AuditableData} container to put the request in
	 * @param auditEventData - the audit meta-data for the event
	 * @param severity - the Message Severity, if {@code null} then MessageSeverity.INFO is used
	 * @param t - a throwable, if relevant (may be {@code null})
	 * @param auditDataclass the audit dataclass
	 */
	public void writeRequestAuditLog(final RequestAuditData requestAuditData,
			final AuditEventData auditEventData, final MessageSeverity severity, final Throwable t, final Class<?> auditDataclass) {

		getAsyncLogger().asyncAuditRequestResponseData(auditEventData, requestAuditData, auditDataclass,
				severity, t);
	}

	/**
	 * Write any kind of response Object to the audit logs.
	 *
	 * @param response - the response object
	 * @param responseAuditData - the {@link AuditableData} container to put the response in
	 * @param auditEventData - the audit meta-data for the event
	 * @param severity - the Message Severity, if {@code null} then MessageSeverity.INFO is used
	 * @param t - a throwable, if relevant (may be {@code null})
	 */
	public void writeResponseAuditLog(final Object response, final ResponseAuditData responseAuditData,
			final AuditEventData auditEventData,
			final MessageSeverity severity, final Throwable t) {

		if (responseAuditData!=null) {
			responseAuditData.setResponse(response);
		}

		LOGGER.debug("Invoking AuditLogSerializer.asyncLogRequestResponseAspectAuditData()");
		getAsyncLogger().asyncAuditRequestResponseData(auditEventData, responseAuditData,
				HttpResponseAuditData.class, severity == null ? MessageSeverity.INFO : severity, t);
	}

	/**
	 * Read the first 1024 bytes and convert that into a string.
	 *
	 * @param in the input stream
	 * @return the string
	 */
	public static String convertBytesOfSetSizeToString(final InputStream in) {
		if (in == null) {
			return StringUtils.EMPTY;
		} else {
			int offset = 0;
			int bytesRead = 0;
			final byte[] data = new byte[NUMBER_OF_BYTES_TO_LIMIT_AUDIT_LOGGED_OBJECT];
			try {
				while ((bytesRead = in.read(data, offset, data.length - offset)) != -1) {
					offset += bytesRead;
					if (offset >= data.length) {
						break;
					}
				}
				return new String(data, 0, offset, StandardCharsets.UTF_8.name());
			} catch (Exception e) {
				LOGGER.warn("Problem reading byte from inputstream.", e);
				return StringUtils.EMPTY;
			} finally {
				BaseAsyncAudit.closeInputStreamIfRequired(in);
			}
		}
	}

	/**
	 * Attempt to close an input stream.
	 *
	 * @param inputstream the inputstream
	 */
	public static void closeInputStreamIfRequired(final InputStream inputstream) {
		if (inputstream != null) {
			try {
				inputstream.close();
			} catch (final Exception e) { // NOSONAR intentionally broad catch
				LOGGER.warn("Problem closing input stream.", e);
			}
		}
	}

	/**
	 * Standard handling of exceptions that are thrown from within the advice (not exceptions thrown by application code, such
	 * exceptions are rethrown).
	 *
	 * @param adviceName the name of the advice/method in which the exception was thrown
	 * @param attemptingTo the attempted task that threw the exception
	 * @param auditEventData the audit event data object
	 * @param throwable the exception that was thrown
	 */
	public void handleInternalExceptionAndRethrowApplicationExceptions(final String adviceName, final String attemptingTo,
			final AuditEventData auditEventData, final MessageKeys key, final Throwable throwable) {

		try {
			LOGGER.error(key.getMessage(adviceName, attemptingTo), throwable);
			final VopRuntimeException vopRuntimeException = new VopRuntimeException(
					key, MessageSeverity.FATAL, HttpStatus.INTERNAL_SERVER_ERROR, throwable,
					adviceName, attemptingTo);

			AuditLogger.error(auditEventData,
					INTERNAL_EXCEPTION_PREFIX + vopRuntimeException.getMessage(),
					vopRuntimeException);

			throw vopRuntimeException;

		} catch (Throwable e) { // NOSONAR intentionally catching throwable
			handleAnyRethrownExceptions(adviceName, e);
		}
	}

	/**
	 * If - after attempting to audit an internal error - another exception is thrown,
	 * then put the whole mess in an error log (non-audit), and throw the exception again
	 * as a Runtime exception.
	 *
	 * @param adviceName the name of the advice/method in which the exception was thrown
	 * @param e the unexpected exception
	 * @throws RuntimeException
	 */
	private void handleAnyRethrownExceptions(final String adviceName, final Throwable e) {

		String msg = adviceName + " - Throwable occured while attempting to writeAuditError for Throwable.";
		LOGGER.error(VopBanner.newBanner(VopConstants.INTERCEPTOR_EXCEPTION, Level.ERROR),
				msg, e);

		RuntimeException ise = null;
		if (!RuntimeException.class.isAssignableFrom(e.getClass())) {
			ise = new IllegalStateException(msg);
		} else {
			ise = (RuntimeException) e;
		}
		throw ise;
	}
}
