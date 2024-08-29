package com.wynd.vop.framework.autoconfigure.feign;

import com.wynd.vop.framework.autoconfigure.cache.TestConfigurationForAuditBeans;
import feign.Logger.Level;
import feign.Request.Options;
import feign.Target;
import com.wynd.vop.framework.autoconfigure.audit.AuditAutoConfiguration;
import com.wynd.vop.framework.rest.provider.aspect.ProviderHttpAspect;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import static org.junit.Assert.assertNotNull;

/**
 * Created by rthota on 8/24/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class VopFeignAutoConfigurationTest {

	private static String CONNECTION_TIMEOUT = "20000";

	private VopFeignAutoConfiguration vopFeignAutoConfiguration;

	private AnnotationConfigWebApplicationContext context;

	@Before
	public void setup() {
		context = new AnnotationConfigWebApplicationContext();
		TestPropertyValues.of("vop.framework.client.rest.connectionTimeout=" + CONNECTION_TIMEOUT).applyTo(context);
		context.register(JacksonAutoConfiguration.class, SecurityAutoConfiguration.class,
				EmbeddedWebServerFactoryCustomizerAutoConfiguration.class,
				AuditAutoConfiguration.class, VopFeignAutoConfiguration.class,
				ProviderHttpAspect.class, TestConfigurationForAuditBeans.class);

		context.refresh();
		assertNotNull(context);

		vopFeignAutoConfiguration = context.getBean(VopFeignAutoConfiguration.class);
		assertNotNull(vopFeignAutoConfiguration);
	}

	@After
	public void close() {
		if (this.context != null) {
			this.context.close();
		}
	}

	/**
	 * Test of feignCustomErrorDecoder method, of class VopFeignAutoConfiguration.
	 */
	@Test
	public void testFeignErrorDecoder() {
		final FeignCustomErrorDecoder result = vopFeignAutoConfiguration.feignCustomErrorDecoder();
		assertNotNull(result);

	}
	
	/**
	 * Test of feignLoggerLevel method, of class VopFeignAutoConfiguration.
	 */
	@Test
	public void testFeignLoggerLevel() {
		final Level result = vopFeignAutoConfiguration.feignLoggerLevel();
		assertNotNull(result);

	}
	
	/**
	 * Test of requestOptions method, of class VopFeignAutoConfiguration.
	 */
	@Test
	public void testFeignRequestOptions() {
		ConfigurableEnvironment environment = new MockEnvironment();
		final Options result = vopFeignAutoConfiguration.requestOptions(environment);
		assertNotNull(result);

	}
	
	@SuppressWarnings("rawtypes")
	class TestTarget extends Target.HardCodedTarget {

		@SuppressWarnings("unchecked")
		public TestTarget(final Class type, final String url) {
			super(type, url);
		}

	}
}
