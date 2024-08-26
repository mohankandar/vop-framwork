package com.wynd.vop.framework.audit;

import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.slf4j.event.Level;

/**
 * The Class AuditLogger.
 */
public class AuditLogger {

	static final VopLogger LOGGER = VopLoggerFactory.getLogger(AuditLogger.class);

	/**
	 * Replacement for {@code null} parameters to the MDC entries that cannot be
	 * null or empty
	 */
	private static final String UNKNOWN = "Unknown";
	/**
	 * Replacement for {@code null} parameters to the MDC entries that cannot be
	 * null
	 */
	private static final String EMPTY = "";

	static {
		String auditOverride = System.getProperty("com.wynd.vop.framework.audit.enableAuditLoggingLevelOverride");
		if(auditOverride != null && auditOverride.equalsIgnoreCase("true")){
			LOGGER.setLevel(Level.valueOf(System.getProperty("com.wynd.vop.framework.audit.auditLogger.auditLevel")));
		} else {
			LOGGER.setLevel(Level.DEBUG);
		}
	}

	/*
	 * private constructor
	 */
	private AuditLogger() {

	}

	/**
	 * Debug.
	 *
	 * @param auditable
	 *            the auditable
	 * @param activityDetail
	 *            the activity detail
	 */
	public static void debug(AuditEventData auditable, String activityDetail) {
		addMdcSecurityEntries(auditable);
		LOGGER.debug(activityDetail);
		MDC.clear();
	}

	/**
	 * Info.
	 *
	 * @param auditable
	 *            the auditable
	 * @param activityDetail
	 *            the activity detail
	 */
	public static void info(AuditEventData auditable, String activityDetail) {
		addMdcSecurityEntries(auditable);
		LOGGER.info(activityDetail);
		MDC.clear();

	}

	/**
	 * Warn.
	 *
	 * @param auditable
	 *            the auditable
	 * @param activityDetail
	 *            the activity detail
	 */
	public static void warn(AuditEventData auditable, String activityDetail) {
		addMdcSecurityEntries(auditable);
		LOGGER.warn(activityDetail);
		MDC.clear();

	}

	/**
	 * Error.
	 *
	 * @param auditable
	 *            the auditable
	 * @param activityDetail
	 *            the activity detail
	 */
	public static void error(final AuditEventData auditable, final String activityDetail, final Throwable t) {
		addMdcSecurityEntries(auditable);
		LOGGER.error(activityDetail, t);
		MDC.clear();

	}

	/**
	 * Adds the MDC security entries.
	 *
	 * @param auditable
	 *            the auditable
	 */
	private static void addMdcSecurityEntries(AuditEventData auditable) {
		if (auditable == null) {
			auditable = new AuditEventData(AuditEvents.UNKNOWN, UNKNOWN, UNKNOWN); // NOSONAR
		}
		MDC.put("logType", "auditlogs");
		MDC.put("activity", StringUtils.isBlank(auditable.getActivity()) ? UNKNOWN : auditable.getActivity());
		MDC.put("event", auditable.getEvent() == null ? AuditEvents.UNKNOWN.name() : auditable.getEvent().name());
		MDC.put("audit_class", StringUtils.isBlank(auditable.getAuditClass()) ? UNKNOWN : auditable.getAuditClass());
		MDC.put("user", StringUtils.isBlank(auditable.getUser()) ? UNKNOWN : auditable.getUser());
		MDC.put("tokenId", StringUtils.isBlank(auditable.getTokenId()) ? EMPTY : auditable.getTokenId());
		if(StringUtils.isNotBlank(auditable.getAuditDate())) {
			MDC.put("audit_date", auditable.getAuditDate());
		}
	}
}
