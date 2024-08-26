package com.wynd.vop.framework.service.aspect;

import com.wynd.vop.framework.aspect.PerformanceLoggingAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

@Aspect
@Order(-9999)
public class ServiceTimerAspect extends BaseServiceAspect {

	@Around("publicStandardServiceMethod() && !restController()")
	public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
		// thrown exceptions are handled in the PerformanceLoggingAspect
		return PerformanceLoggingAspect.aroundAdvice(joinPoint);
	}

}
