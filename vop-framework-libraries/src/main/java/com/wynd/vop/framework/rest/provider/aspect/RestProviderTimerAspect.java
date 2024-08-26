package com.wynd.vop.framework.rest.provider.aspect;

import com.wynd.vop.framework.aspect.PerformanceLoggingAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

@Aspect
@Order(-9999)
public class RestProviderTimerAspect extends BaseHttpProviderPointcuts {

	@Around("publicServiceResponseRestMethod()")
	public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
		// thrown exceptions are handled in the PerformanceLoggingAspect
		return PerformanceLoggingAspect.aroundAdvice(joinPoint);
	}

}
