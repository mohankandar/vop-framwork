package com.wynd.vop.framework.autoconfigure.cache;

import com.wynd.vop.framework.audit.AuditLogSerializer;
import com.wynd.vop.framework.audit.BaseAsyncAudit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class TestConfigurationForAuditBeans {

	@Bean
	@ConditionalOnMissingBean
	public AuditLogSerializer auditLogSerializer() {
		return new AuditLogSerializer();
	}

	@Bean
	@ConditionalOnMissingBean
	public BaseAsyncAudit baseAsyncAudit() {
		return new BaseAsyncAudit();
	}

}