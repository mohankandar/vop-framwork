package com.wynd.vop.framework.rest.provider.aspect;

import com.wynd.vop.framework.audit.http.AuditHttpRequestResponse;
import org.aspectj.lang.annotation.Pointcut;

/**
 * This is the base class for REST provider aspects.
 * It provides the Point Cuts to be used by extending classes that declare types of @Aspect advice.
 *
 
 */
public class BaseHttpProviderPointcuts extends AuditHttpRequestResponse {

	/**
	 * Protected constructor.
	 */
	protected BaseHttpProviderPointcuts() {
		super();
	}

	/**
	 * This point cut selects any code within a REST controller class that...
	 * <ol>
	 * <li>is annotated with org.springframework.web.bind.annotation.RestController
	 * </ol>
	 */
	@Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
	protected static final void restController() {
		// Do nothing.
	}

	/**
	 * This point cut selects REST endpoint operations that are of interest to external consumers.
	 * Those are operations that...
	 * <ol>
	 * <li>are in a rest controller class (see the {@link #restController()} pointcut)
	 * <li>that are public in scope
	 * <li>where the method returns {@code com.wynd.vop.framework.transfer.ProviderTransferObjectMarker+} or
	 * {@code org.springframework.http.ResponseEntity<com.wynd.vop.framework.transfer.ProviderTransferObjectMarker+>}
	 * </ol>
	 */
	@Pointcut("restController() && ("
			+ "execution(public org.springframework.http.ResponseEntity<com.wynd.vop.framework.transfer.ProviderTransferObjectMarker+> *(..))"
			+ " || execution(public com.wynd.vop.framework.transfer.ProviderTransferObjectMarker+ *(..))"
			+ ")")
	protected static final void publicServiceResponseRestMethod() {
		// Do nothing.
	}

	/**
	 * This point cut selects REST endpoint operations that are of interest to external consumers. Those are operations that...
	 * <ol>
	 * <li>are in a rest controller class (see the {@link #restController()} pointcut)
	 * <li>that are public in scope
	 * <li>where the method returns {@code org.springframework.core.io.Resource+} or
	 * {@code org.springframework.http.ResponseEntity<org.springframework.core.io.Resource+>}
	 * </ol>
	 */
	@Pointcut("restController() && ("
			+ "execution(public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource+> *(..))"
			+ " || execution(public org.springframework.core.io.Resource+ *(..))" + ")")
	protected static final void publicResourceDownloadRestMethod() {
		// Do nothing.
	}

	/**
	 * This point cut selects code (e.g. methods) that ...
	 * <ol>
	 * <li>are annotated with com.wynd.vop.framework.audit.annotation.Auditable
	 * </ol>
	 */
	@Pointcut("@annotation(com.wynd.vop.framework.audit.annotation.Auditable)")
	protected static final void auditableAnnotation() {
		// Do nothing.
	}

	/**
	 * This point cut selects code (e.g. methods) that...
	 * <ol>
	 * <li>are annotated with com.wynd.vop.framework.audit.annotation.Auditable - see {@link #auditableAnnotation()}
	 * <li>and, only at the time when the code inside the annotated method is executed
	 * </ol>
	 */
	@Pointcut("auditableAnnotation() && execution(* *(..))")
	protected static final void auditableExecution() {
		// Do nothing.
	}
}
