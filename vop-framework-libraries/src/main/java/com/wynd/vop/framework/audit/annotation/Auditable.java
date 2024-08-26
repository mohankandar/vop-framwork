package com.wynd.vop.framework.audit.annotation;

import com.wynd.vop.framework.aspect.AuditableAnnotationAspect;
import com.wynd.vop.framework.audit.AuditEvents;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Auditable annotation that asynchronously logs audit data. Can be applied to
 * methods or classes.
 * <p>
 * This annotation is intended for use in the service layers, and should never
 * be applied to a provider class or method.
 * <p>
 * Required attributes:
 * <ul>
 * <li>event - an {@link AuditEvents} audit event enumeration
 * <li>activity - specific String description of the event
 * </ul>
 * Optional attributes:
 * <ul>
 * <li>auditClass - name of the java Class under audit
 * <li>auditDate - date of the audit event if specifically needed to be set. the
 * system generated timestamp is stored by the framework against timestamp field
 * of the audit logs
 * </ul>
 * Implementation is {@link AuditableAnnotationAspect}
 *
 * Created by vgadda on 8/17/17.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Auditable {
	/** An {@link AuditEvents} audit event enumeration */
	AuditEvents event();

	/** Specific String description of the event */
	String activity();

	/** Fully Qualified Name of the Java Class under audit */
	String auditClass() default "";

	/**
	 * The actual value expression: for example
	 * {@code T(java.time.LocalDateTime).now().toString()} style expressions <br/>
	 * <br/>
	 * Optional DateTimeStamp to be explicitly set. If the field isn't set, then
	 * the field "audit_date" won't be added to MDC audit log (logType="auditlogs")
	 */
	String auditDate() default "";
}
