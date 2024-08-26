package com.wynd.vop.framework.client.ws.interceptor;

import com.wynd.vop.framework.audit.AuditEventData;
import com.wynd.vop.framework.audit.AuditEvents;
import org.springframework.ws.client.core.WebServiceTemplate;

/**
 * Configurations of audit metadata that are available for use with the {@link AuditWsInterceptor}.
 *

 */
public enum AuditWsInterceptorConfig {

	/** Configuration intended for auditing objects <b><i>before</i></b> other ClientInterceptors run */
	BEFORE("Raw XML"),
	/** Configuration intended for auditing objects <b><i>after</i></b> other ClientInterceptors run */
	AFTER("Wire Log");

	/** The title string used in the audit message prefix */
	private String title;

	/** The class reported as being under audit */
	private static final Class<WebServiceTemplate> AUDITED = WebServiceTemplate.class;

	/** System new-line character */
	private static final String NEW_LINE = System.getProperty("line.separator");

	/** Arrow used in message prefixes */
	private static final String ARROW = " -> ";

	/**
	 * Instantiate the webservice audit interceptor.
	 *
	 * @param title - the String to be used in the audit message prefix
	 */
	AuditWsInterceptorConfig(String title) {
		this.title = title;
	}

	/**
	 * What class is deemed to be under audit.
	 *
	 * @return Class - the class deemed to be under audit
	 */
	Class<?> audited() {
		return AUDITED;
	}

	/**
	 * The simple name of the class that is deemed to be under audit.
	 *
	 * @return String - the simple name of the class that is deemed to be under audit
	 */
	String auditedName() {
		return AUDITED.getSimpleName();
	}

	/**
	 * Inner abstract class to declare scoped (non-public) methods for audit logging metadata.
	 *
	
	 */
	abstract class AuditWsMetadata { // NOSONAR The methods need to be private, hence an interface cannot be used
		/** Get a new AuditEventData instance for the data object being audited */
		abstract AuditEventData eventData();

		/** Get the AuditEvents enum for the audit event */
		abstract AuditEvents event();

		/** Get the activity identifier string for the audit event */
		abstract String activity();

		/** Get the message prefix string for the data object being audited */
		abstract String messagePrefix();
	}

	/**
	 * Class that declares the auditing metadata for webservice <b><i>request</i></b> objects.
	 *
	 * @see AuditWsMetadata
	
	 */
	class RequestMetadata extends AuditWsMetadata {
		/** the activity identifier string for the audit event - named as activiti to fix fortify violation*/
		private final String activiti = AuditEvents.PARTNER_SOAP_REQUEST.getDefaultActivity();

		@Override
		AuditEventData eventData() {
			return new AuditEventData(AuditEvents.PARTNER_SOAP_REQUEST, activiti, AUDITED.getName());
		}

		@Override
		AuditEvents event() {
			return AuditEvents.PARTNER_SOAP_REQUEST;
		}

		@Override
		String activity() {
			return activiti;
		}

		@Override
		String messagePrefix() {
			return activiti + ARROW + title + " : " + NEW_LINE;
		}
	}

	/**
	 * The auditing metadata for webservice <b><i>request</i></b> objects.
	 *
	 * @return Request - the metadata for auditing webservice request objects
	 */
	RequestMetadata requestMetadata() {
		return new RequestMetadata();
	}

	/**
	 * Class that declares the auditing metadata for webservice <b><i>response</i></b> objects.
	 *
	 * @see AuditWsMetadata
	
	 */
	class ResponseMetadata extends AuditWsMetadata {
		/** the activity identifier string for the audit event - named as activiti to fix fortify violation*/
		private final String activiti;

		/** Instantiate default metadata */
		public ResponseMetadata() {
			this.activiti = AuditEvents.PARTNER_SOAP_RESPONSE.getDefaultActivity();
		}

		/** Intended for subclasses to instantiate default metadata */
		protected ResponseMetadata(AuditEvents auditEvent) {
			this.activiti = auditEvent.getDefaultActivity();
		}

		@Override
		AuditEventData eventData() {
			return new AuditEventData(AuditEvents.PARTNER_SOAP_RESPONSE, activiti, AUDITED.getName());
		}

		@Override
		AuditEvents event() {
			return AuditEvents.PARTNER_SOAP_RESPONSE;
		}

		@Override
		String activity() {
			return activiti;
		}

		@Override
		String messagePrefix() {
			return activiti + ARROW + title + " : " + NEW_LINE;
		}
	}

	/**
	 * The auditing metadata for webservice <b><i>response</i></b> objects.
	 *
	 * @return Response - the metadata for auditing webservice response objects
	 */
	ResponseMetadata responseMetadata() {
		return new ResponseMetadata();
	}

	/**
	 * Class that declares the auditing metadata for webservice <b><i>fault</i></b> objects.
	 *
	 * @see AuditWsMetadata
	
	 */
	class FaultMetadata extends ResponseMetadata {
		/** Instantiate with the activity identifier string for the audit event */
		public FaultMetadata() {
			super(AuditEvents.PARTNER_SOAP_FAULT);
		}
	}

	/**
	 * The auditing metadata for webservice <b><i>response</i></b> objects.
	 *
	 * @return Response - the metadata for auditing webservice response objects
	 */
	FaultMetadata faultMetadata() {
		return new FaultMetadata();
	}
}
