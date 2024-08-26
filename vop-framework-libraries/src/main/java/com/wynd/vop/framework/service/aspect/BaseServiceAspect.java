package com.wynd.vop.framework.service.aspect;

import org.aspectj.lang.annotation.Pointcut;

/**
 * This is the base class containing the PointCuts for service aspects.
 *
 
 */
public class BaseServiceAspect {

	protected BaseServiceAspect() {
		super();
	}

	/**
	 * This aspect defines the pointcut of standard REST controller. Those are controllers that...
	 *
	 * (1) are annotated with org.springframework.web.bind.annotation.RestController
	 *
	 * Ensure you follow that pattern to make use of this standard pointcut.
	 */
	@Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
	protected static final void restController() {
		// Do nothing.
	}

	/**
	 * This pointcut reflects a public standard service method.
	 *
	 * These are methods which are
	 * (1) public
	 * (2) return a DomainResponse
	 *
	 * @See com.wynd.vop.framework.service.DomainResponse
	 */
	@Pointcut("execution(public com.wynd.vop.framework.service.DomainResponse+ *(..))")
	protected static final void publicStandardServiceMethod() {
		// Do Nothing
	}

	/**
	 * This aspect defines the pointcut of Service implementation. Those are services that...
	 *
	 * (1) are annotated with org.springframework.stereotype.Service
	 *
	 * Ensure you follow that pattern to make use of this standard pointcut.
	 */
	@Pointcut("within(@org.springframework.stereotype.Service *)")
	protected static final void serviceImpl() {
		// Do nothing.
	}

}
