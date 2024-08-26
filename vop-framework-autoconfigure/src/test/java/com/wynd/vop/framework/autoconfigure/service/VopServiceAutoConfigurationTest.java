package com.wynd.vop.framework.autoconfigure.service;

import com.wynd.vop.framework.autoconfigure.audit.AuditAutoConfiguration;
import com.wynd.vop.framework.autoconfigure.cache.TestConfigurationForAuditBeans;
import org.junit.After;
import org.junit.Test;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import static org.junit.Assert.assertNotNull;

/**
 * Created by rthota on 8/24/17.
 */
public class VopServiceAutoConfigurationTest {

	private AnnotationConfigWebApplicationContext context;

	@After
	public void close() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void testWebConfiguration() throws Exception {
		context = new AnnotationConfigWebApplicationContext();
		context.register(AuditAutoConfiguration.class, VopServiceAutoConfiguration.class, TestConfigurationForAuditBeans.class);
		context.refresh();
		assertNotNull(context);
		assertNotNull(this.context.getBean(AuditAutoConfiguration.class));
		assertNotNull(this.context.getBean(VopServiceAutoConfiguration.class));

	}
}
