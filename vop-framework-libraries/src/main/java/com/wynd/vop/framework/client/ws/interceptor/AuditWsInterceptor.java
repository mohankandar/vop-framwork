package com.wynd.vop.framework.client.ws.interceptor;

import com.wynd.vop.framework.exception.VopPartnerRuntimeException;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import com.wynd.vop.framework.validation.Defense;
import com.wynd.vop.framework.audit.AuditEventData;
import com.wynd.vop.framework.audit.AuditEvents;
import com.wynd.vop.framework.audit.AuditLogSerializer;
import com.wynd.vop.framework.audit.AuditLogger;
import com.wynd.vop.framework.client.ws.interceptor.transport.ByteArrayTransportOutputStream;
import org.springframework.http.HttpStatus;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * This interceptor performs Audit logging of the request and response XML from the {@link WebserviceTemplate}.
 * Also, any SOAP Faults on the WebServiceTemplate operation will be audited.
 *

 */
public class AuditWsInterceptor implements ClientInterceptor {
	/** Class logger */
	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(AuditWsInterceptor.class);

	/** Asynchronous audit logger */
	private static final AuditLogSerializer asyncLogger = new AuditLogSerializer();

	/** Ensure logging only occurs once per instantiation */
	private boolean alreadyLogged = false;

	/** The text of the title of the audit log */
	private AuditWsInterceptorConfig config;

	/**
	 * Instantiate the interceptor to use the given configuration.
	 *
	 * @param config the config
	 */
	public AuditWsInterceptor(AuditWsInterceptorConfig config) {
		Defense.notNull(config);
		LOGGER.debug("Instantiating " + this.getClass().getSimpleName() + " with config: " + config.name());
		this.config = config;
	}

	@Override
	public boolean handleRequest(final MessageContext messageContext) {
		return true;
	}

	@Override
	public boolean handleResponse(final MessageContext messageContext) {
		return true;
	}

	@Override
	public boolean handleFault(final MessageContext messageContext) {
		LOGGER.debug("Executing handleFault(..) with config " + config.name());
		doAudit(config.faultMetadata(), messageContext.getResponse());
		return true;
	}

	@Override
	public void afterCompletion(final MessageContext messageContext, final Exception ex) {
		if (!alreadyLogged) {
			LOGGER.debug("Executing afterCompletion(..) with config " + config.name());
			// log request
			doAudit(config.requestMetadata(), messageContext.getRequest());

			LOGGER.debug("Partner call returned response: " + messageContext.getResponse());
			// log response, even if it is null
			doAudit(config.responseMetadata(), messageContext.getResponse());

			// remember that this interceptor has already done its job
			alreadyLogged = true;
		}
	}

	/**
	 * Asynchronously writes the audit information to the audit log.
	 *
	 * @param event the AuditEvent
	 * @param activityName the activity name
	 * @param title the title prepended to the message
	 * @param webServiceMessage the WebServiceMessage that contains the SOAP XML
	 * @throws IOException some problem reading the WebServiceMessage
	 */
	private void doAudit(AuditWsInterceptorConfig.AuditWsMetadata metadata,
			WebServiceMessage webServiceMessage) {
		LOGGER.debug("Writing audit log with metadata: " + metadata.getClass().getName());
		try {
			asyncLogger.asyncAuditMessageData(metadata.eventData(),
					metadata.messagePrefix() + getXml(webServiceMessage),
					MessageSeverity.INFO, null);
		} catch (Exception e) {
			handleInternalError(metadata.event(), metadata.activity(), e);
		}
	}

	/**
	 * Gets the XML (SOAP) representation of the {@link WebServiceMessage}.
	 *
	 * @param webServiceMessage
	 * @return String - the SOAP XML
	 * @throws IOException
	 */
	private String getXml(WebServiceMessage webServiceMessage) throws IOException {
		if (webServiceMessage == null) {
			return null;
		}

		ByteArrayTransportOutputStream byteArrayTransportOutputStream = new ByteArrayTransportOutputStream();
		webServiceMessage.writeTo(byteArrayTransportOutputStream);

		return new String(byteArrayTransportOutputStream.toByteArray(), StandardCharsets.ISO_8859_1);
	}

	/**
	 * Handles any exception thrown internally.
	 * <ul>
	 * <li>The error is written to the audit log
	 * <li>The following exception types are re-thrown untouched: BipPartnerRuntimeException, WebServiceClientException.
	 * <li>All other exception types are converted to BipPartnerRuntimeException which is then thrown.
	 * </ul>
	 *
	 * @param event
	 * @param activity
	 * @param e
	 * @throws RuntimeException
	 */
	protected void handleInternalError(final AuditEvents event, final String activity, final Exception e) {
		RuntimeException rethrowMe = null;
		String adviceName = this.getClass().getSimpleName() + ".afterCompletion";
		this.writeAuditError(adviceName, e, new AuditEventData(event, activity, config.auditedName()));
		if (VopPartnerRuntimeException.class.isAssignableFrom(e.getClass())) {
			rethrowMe = (VopPartnerRuntimeException) e;
		} else if (WebServiceClientException.class.isAssignableFrom(e.getClass())) {
			rethrowMe = (WebServiceClientException) e;
		} else {
			rethrowMe =
					new VopPartnerRuntimeException(MessageKeys.VOP_REST_CONFIG_WEBSERVICE_TEMPLATE_FAIL,
							MessageSeverity.FATAL, HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
		throw rethrowMe;
	}

	/**
	 * Write into Audit when exceptions occur while attempting to log audit records.
	 *
	 * @param adviceName the advice name
	 * @param e the exception
	 * @param auditEventData the audit event data
	 */
	protected void writeAuditError(final String adviceName, final Exception e, final AuditEventData auditEventData) {
		LOGGER.error(adviceName + " encountered uncaught exception.", e);
		AuditLogger.error(auditEventData,
				adviceName + " encountered uncaught exception: " + e.getLocalizedMessage(), e);
	}
}
