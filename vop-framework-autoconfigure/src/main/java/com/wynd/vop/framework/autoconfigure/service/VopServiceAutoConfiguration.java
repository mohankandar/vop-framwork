package com.wynd.vop.framework.autoconfigure.service;

import com.wynd.vop.framework.aspect.AuditableAnnotationAspect;
import com.wynd.vop.framework.service.aspect.ServiceTimerAspect;
import com.wynd.vop.framework.service.aspect.ServiceValidationAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by rthota on 8/24/17.
 */

@Configuration
public class VopServiceAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public AuditableAnnotationAspect auditableAnnotationAspect() {
		return new AuditableAnnotationAspect();
	}

	@Bean
	@ConditionalOnMissingBean
	public ServiceTimerAspect serviceTimerAspect() {
		return new ServiceTimerAspect();
	}

	@Bean
	@ConditionalOnMissingBean
	public ServiceValidationAspect serviceValidationAspect() {
		return new ServiceValidationAspect();
	}

}
