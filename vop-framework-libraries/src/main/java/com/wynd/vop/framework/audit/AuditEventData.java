package com.wynd.vop.framework.audit;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * Used as a data transfer object from the Auditable annotation for writing to the audit log.
 *
 */
public final class AuditEventData {

	/** Replacement for {@code null} parameters to the constructor that cannot be null or empty */
	private static final String UNKNOWN = "Unknown";
	/** Replacement for {@code null} parameters to the constructor that cannot be null */
	private static final String EMPTY = "";

	/**
	 * The event type.
	 */
	private final AuditEvents event;

	/**
	 * The activity (or method name).
	 */
	private final String activity;

	/**
	 * The class being audited.
	 */
	private final String auditClass;

	/**
	 * The specific date being audited.
	 */
	private String auditDate = StringUtils.EMPTY;

	/**
	 * The user from person traits.
	 */
	private String user = StringUtils.EMPTY;

	/**
	 * The tokenId from person traits.
	 */
	private String tokenId = StringUtils.EMPTY;

	/**
	 * Constructs a new AuditEventData object.
	 *
	 * @param event the event type.
	 * @param activity the activity or method name.
	 * @param auditClass the class name for class under audit.
	 */
	public AuditEventData(final AuditEvents event, final String activity, final String auditClass) {
		this.event = event == null ? AuditEvents.UNKNOWN : event;
		this.activity = StringUtils.isBlank(activity) ? UNKNOWN : activity;
		this.auditClass = StringUtils.isBlank(auditClass) ? UNKNOWN : auditClass;
	}

	/**
	 * Constructs a new AuditEventData object.
	 *
	 * @param event the event type.
	 * @param activity the activity or method name.
	 * @param auditClass the class name for class under audit.
	 */
	public AuditEventData(final AuditEvents event, final String activity, final String auditClass, final String auditDate) {
		this(event, activity, auditClass);
		this.auditDate = StringUtils.isBlank(auditDate) ? "" : auditDate;
	}

	/**
	 * Gets the event.
	 *
	 * @return the event.
	 */
	public AuditEvents getEvent() {
		return event;
	}

	/**
	 * Gets the activity.
	 *
	 * @return the activity
	 */
	public String getActivity() {
		return activity;
	}

	/**
	 * Gets the user.
	 *
	 * @return the user.
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Gets the tokenId.
	 *
	 * @return the tokenId.
	 */
	public String getTokenId() {
		return tokenId;
	}

	/**
	 * Gets the audited class name.
	 *
	 * @return the audited class name.
	 */
	public String getAuditClass() {
		return auditClass;
	}

	/**
	 * Gets the specific date being audited.
	 *
	 * @return the audited specific date.
	 */
	public String getAuditDate() {
		return auditDate;
	}

	@Override
	public String toString() {
		return "AuditEventData{" +
				"event=" + event +
				", activity='" + activity + '\'' +
				", auditClass='" + auditClass + '\'' +
				", auditDate='" + auditDate + '\'' +
				", tokenId=" + tokenId +
				", user=" + user +
				'}';
	}
}