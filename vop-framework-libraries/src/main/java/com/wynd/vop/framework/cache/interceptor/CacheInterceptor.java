package com.wynd.vop.framework.cache.interceptor;

import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.audit.AuditEventData;
import com.wynd.vop.framework.audit.AuditEvents;
import com.wynd.vop.framework.audit.BaseAsyncAudit;
import com.wynd.vop.framework.audit.model.HttpResponseAuditData;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;

import java.util.Arrays;

/**
 * Audit cache GET operations.
 * <p>
 * This interceptor is equivalent to an Around aspect of the method that
 * has the Cache annotation(s) - e.g. @CachePut.
 * <p>
 * This interceptor does not distinguish cache operations, so all executions
 * of the application caching method will create audit records.
 * If this behavior is undesirable, it will be necessary to override enough
 * of the inherited code to have control of the {@link #doGet(Cache, Object)} method.
 *

 */
public class CacheInterceptor extends org.springframework.cache.interceptor.CacheInterceptor {

	private static final long serialVersionUID = -4368188406381849484L;

	/** Class logger */
	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(CacheInterceptor.class);
	/** The advice logging name for this interceptor */
	private static final String ADVICE_NAME = "invokeBipCacheInterceptor";
	/** The activity name for this interceptor */
	private static final String ACTIVITY = "cacheInvoke";


	private boolean allowNullReturn;

	/** Get the object for general auditing. */
	@Autowired
	transient BaseAsyncAudit baseAsyncAudit;

	/**
	 * Instantiate an BipCacheInterceptor to audit cache GET operations.
	 * @param allowNullReturn
	 */
	public CacheInterceptor(boolean allowNullReturn) {
		this.allowNullReturn = allowNullReturn;
		LOGGER.debug("Instantiating " + CacheInterceptor.class.getName());
	}

	/**
	 * Perform audit logging after the method has been called.
	 * <p>
	 * This interceptor is equivalent to an Around aspect of the method that
	 * has the Cache annotation(s) - e.g. @CachePut.
	 * <p>
	 * This interceptor does not distinguish cache operations, so all executions
	 * of the application caching method will create audit records.
	 * If this behavior is undesirable, it will be necessary to override enough
	 * of the inherited code to have control of the {@link #doGet(Cache, Object)} method.
	 */
	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable {

		Class<?> underAudit = invocation.getThis().getClass();
		AuditEventData auditEventData = new AuditEventData(AuditEvents.CACHED_SERVICE_RESPONSE, ACTIVITY, underAudit.getName());

		Object response = null;

		try {
			response = super.invoke(invocation);
			if (!allowNullReturn && response == null) {
				response = new Object();
			}

			if (LOGGER.isDebugEnabled()) {
				String prefix = this.getClass().getSimpleName() + ".invoke(..) :: ";
				LOGGER.debug(prefix + "Invocation class: " + invocation.getClass().toGenericString());
				LOGGER.debug(prefix + "Invoked from: " + invocation.getThis().getClass().getName());
				LOGGER.debug(prefix + "Invoking method: " + invocation.getMethod().toGenericString());
				LOGGER.debug(prefix + "  having annotations: " + Arrays.toString(invocation.getStaticPart().getAnnotations()));
				LOGGER.debug(prefix + "Returning: " + (response == null
						? "null"
						: ReflectionToStringBuilder.toString(response, null, false, false, Object.class)));
			}

			baseAsyncAudit.writeResponseAuditLog(response, new HttpResponseAuditData(), auditEventData, null, null);
			LOGGER.debug(ADVICE_NAME + " audit logging handed off to async.");

		} finally {
			LOGGER.debug(ADVICE_NAME + " finished.");
		}

		return response;
	}
}
