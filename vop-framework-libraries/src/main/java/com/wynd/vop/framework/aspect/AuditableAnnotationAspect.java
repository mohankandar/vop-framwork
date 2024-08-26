package com.wynd.vop.framework.aspect;

import com.wynd.vop.framework.audit.annotation.Auditable;
import com.wynd.vop.framework.audit.model.ResponseAuditData;
import com.wynd.vop.framework.exception.VopRuntimeException;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import com.wynd.vop.framework.audit.AuditEventData;
import com.wynd.vop.framework.audit.model.RequestAuditData;
import com.wynd.vop.framework.rest.provider.aspect.BaseHttpProviderPointcuts;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Before and After audit logging for the {@link Auditable} annotation.
 * <p>
 * Note that this aspect does NOT process AfterThrowing advice. Because
 * the {@code @Auditable} annotation could be applied to any method,
 * it has been decided to allow method exceptions to flow through the aspect
 * as-is.
 *

 */
@Aspect
public class AuditableAnnotationAspect extends BaseHttpProviderPointcuts {
	/** The Constant LOGGER. */
	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(AuditableAnnotationAspect.class);

	/** The Constant AUDIT_DEBUG_PREFIX_METHOD. */
	private static final String AUDIT_DEBUG_PREFIX_METHOD = "Audit Annotated Method: {}";

	/** The Constant AUDIT_DEBUG_PREFIX_CLASS. */
	private static final String AUDIT_DEBUG_PREFIX_CLASS = "Audit Annotated Class: {}";

	/** The Constant AUDIT_DEBUG_PREFIX_ANNOTATION. */
	private static final String AUDIT_DEBUG_PREFIX_ANNOTATION = "Auditable Annotation: {}";

	/** The Constant AUDIT_DEBUG_PREFIX_EVENT. */
	private static final String AUDIT_DEBUG_PREFIX_EVENT = "AuditEventData: {}";

	/** The Constant AUDIT_ERROR_PREFIX_EXCEPTION. */
	private static final String AUDIT_ERROR_PREFIX_EXCEPTION = "Could not audit event due to unexpected exception.";

	/**
	 * Instantiate the aspect.
	 */
	public AuditableAnnotationAspect() {
		super();
	}

	/**
	 * Advice for auditing before the call to a method annotated with {@link Auditable}.
	 * <p>
	 * Note that this aspect does NOT process AfterThrowing advice. Because
	 * the {@code @Auditable} annotation could be applied to any method,
	 * it has been decided to allow method exceptions to flow through the aspect
	 * as-is.
	 *
	 * @param joinPoint
	 */
	@Before("auditableExecution()")
	public void auditAnnotationBefore(final JoinPoint joinPoint) {
		List<Object> request = null;
		Auditable auditableAnnotation = null;
		AuditEventData auditEventData = null;

		try {
			if (joinPoint.getArgs().length > 0) {
				request = Arrays.asList(joinPoint.getArgs());
			}

			final Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
			LOGGER.debug(AUDIT_DEBUG_PREFIX_METHOD, method);
			final String className = method.getDeclaringClass().getName();
			LOGGER.debug(AUDIT_DEBUG_PREFIX_CLASS, className);
			auditableAnnotation = method.getAnnotation(Auditable.class);
			LOGGER.debug(AUDIT_DEBUG_PREFIX_ANNOTATION, auditableAnnotation);
			if (auditableAnnotation != null) {
				String auditDate = processAuditDate(auditableAnnotation);
				auditEventData =
						new AuditEventData(auditableAnnotation.event(), auditableAnnotation.activity(),
								StringUtils.isBlank(auditableAnnotation.auditClass()) ? className : auditableAnnotation.auditClass(),
										auditDate);
				LOGGER.debug(AUDIT_DEBUG_PREFIX_EVENT, auditEventData.toString());

				final RequestAuditData requestAuditData = new RequestAuditData();
				requestAuditData.setRequest(request);

				baseAsyncAudit.writeRequestAuditLog(requestAuditData, auditEventData, MessageSeverity.INFO, null,
						RequestAuditData.class);
			}
		} catch (Exception e) { // NOSONAR intentionally broad catch
			baseAsyncAudit.handleInternalExceptionAndRethrowApplicationExceptions("auditAnnotationBefore",
					"AuditingUsingAuditableAnnotation", auditEventData,
					MessageKeys.VOP_AUDIT_ASPECT_ERROR_CANNOT_AUDIT, e);
		}

	}

	/**
	 * Advice for auditing after the call to a method annotated with {@link Auditable}.
	 * <p>
	 * Note that this aspect does NOT process AfterThrowing advice. Because
	 * the {@code @Auditable} annotation could be applied to any method,
	 * it has been decided to allow method exceptions to flow through the aspect
	 * as-is.
	 *
	 * @param joinPoint
	 * @param response
	 */
	@AfterReturning(pointcut = "auditableExecution()", returning = "response")
	public void auditAnnotationAfterReturning(final JoinPoint joinPoint, final Object response) {
		LOGGER.debug("Response: {}", response);

		Auditable auditableAnnotation = null;
		AuditEventData auditEventData = null;

		try {
			final Method afterReturningMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
			LOGGER.debug(AUDIT_DEBUG_PREFIX_METHOD, afterReturningMethod);
			final String declaringClassName = afterReturningMethod.getDeclaringClass().getName();
			LOGGER.debug(AUDIT_DEBUG_PREFIX_CLASS, declaringClassName);
			auditableAnnotation = afterReturningMethod.getAnnotation(Auditable.class);
			LOGGER.debug(AUDIT_DEBUG_PREFIX_ANNOTATION, auditableAnnotation);

			if (auditableAnnotation != null) {
				String auditDate = processAuditDate(auditableAnnotation);
				auditEventData =
						new AuditEventData(auditableAnnotation.event(), auditableAnnotation.activity(),
								StringUtils.isBlank(auditableAnnotation.auditClass()) ? declaringClassName : auditableAnnotation.auditClass(),
										auditDate);
				LOGGER.debug(AUDIT_DEBUG_PREFIX_EVENT, auditEventData.toString());

				baseAsyncAudit.writeResponseAuditLog(response, new ResponseAuditData(), auditEventData, null, null);
			}
		} catch (Exception e) { // NOSONAR intentionally broad catch
			LOGGER.error(AUDIT_ERROR_PREFIX_EXCEPTION, e);
			throw new VopRuntimeException(MessageKeys.VOP_AUDIT_ASPECT_ERROR_CANNOT_AUDIT, MessageSeverity.FATAL,
					HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
	}

	/**
	 * Advice for auditing after the call to a method annotated with {@link Auditable}.
	 * <p>
	 * Note that this aspect does NOT process AfterThrowing advice. Because
	 * the {@code @Auditable} annotation could be applied to any method,
	 * it has been decided to allow method exceptions to flow through the aspect
	 * as-is.
	 *
	 * @param joinPoint the join point
	 * @param throwable the throwable
	 * @throws Throwable the throwable
	 */
	@AfterThrowing(pointcut = "auditableExecution()", throwing = "throwable")
	public void auditAnnotationAfterThrowing(final JoinPoint joinPoint, final Throwable throwable) throws Throwable { // NOSONAR Have to define generic Throwable exception
		LOGGER.debug("afterThrowing throwable: {}" + throwable);

		Auditable auditableAnnotation = null;
		AuditEventData auditEventData = null;

		try {
			final Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
			final String className = method.getDeclaringClass().getName();
			LOGGER.debug(AUDIT_DEBUG_PREFIX_CLASS, className);
			auditableAnnotation = method.getAnnotation(Auditable.class);
			LOGGER.debug(AUDIT_DEBUG_PREFIX_ANNOTATION, auditableAnnotation);

			if (auditableAnnotation != null) {
				String auditDate = processAuditDate(auditableAnnotation);
				String auditedClass = StringUtils.isBlank(auditableAnnotation.auditClass())
						? className
								: auditableAnnotation.auditClass();
				auditEventData =
						new AuditEventData(auditableAnnotation.event(), auditableAnnotation.activity(), auditedClass,
								auditDate);
				LOGGER.debug(AUDIT_DEBUG_PREFIX_EVENT, auditEventData.toString());

				baseAsyncAudit.writeResponseAuditLog("An exception occurred in " + auditedClass + ".", new ResponseAuditData(),
						auditEventData,
						MessageSeverity.ERROR, throwable);
			}
		} catch (Exception e) { // NOSONAR intentionally broad catch
			LOGGER.error(AUDIT_ERROR_PREFIX_EXCEPTION, e);
			throw new VopRuntimeException(MessageKeys.VOP_AUDIT_ASPECT_ERROR_CANNOT_AUDIT, MessageSeverity.FATAL,
					HttpStatus.INTERNAL_SERVER_ERROR, e);
		}

		throw throwable;
	}

	/**
	 * Process audit date.
	 *
	 * @param auditableAnnotation the auditable annotation
	 * @return the string
	 */
	private String processAuditDate(final Auditable auditableAnnotation) {
		String processedAuditDate = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(auditableAnnotation.auditDate())) {
			try {
				processedAuditDate = auditableAnnotation.auditDate();
				final ExpressionParser expressionParser = new SpelExpressionParser();
				final Expression expression = expressionParser.parseExpression(auditableAnnotation.auditDate());
				Object parsedSpel = expression.getValue();
				if (parsedSpel instanceof String) {
					processedAuditDate = (String) parsedSpel;
				} else if (parsedSpel instanceof ArrayList) {
					processedAuditDate = (String) ((ArrayList<?>) parsedSpel).get(0);
				}
			}catch (ParseException e) {
				LOGGER.error("ParseException for @Auditable annotation, auditDate attribute {}", e);
			}
		}
		return processedAuditDate;
	}
}
