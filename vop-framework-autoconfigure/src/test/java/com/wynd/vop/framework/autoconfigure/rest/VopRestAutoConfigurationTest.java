package com.wynd.vop.framework.autoconfigure.rest;

import com.wynd.vop.framework.autoconfigure.cache.TestConfigurationForAuditBeans;
import com.wynd.vop.framework.autoconfigure.audit.AuditAutoConfiguration;
import com.wynd.vop.framework.rest.provider.aspect.ProviderHttpAspect;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.wynd.vop.framework.util.HttpClientUtils;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * Created by rthota on 8/24/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class VopRestAutoConfigurationTest {

	private static final String CONNECTION_TIMEOUT = "20000";

	private VopRestAutoConfiguration vopRestAutoConfiguration;

	private AnnotationConfigWebApplicationContext context;

	@Before
	public void setup() {
		context = new AnnotationConfigWebApplicationContext();
		TestPropertyValues.of("feign.hystrix.enabled=true").applyTo(context);
		TestPropertyValues.of("vop.framework.client.rest.connectionTimeout=" + CONNECTION_TIMEOUT).applyTo(context);
		context.register(JacksonAutoConfiguration.class, SecurityAutoConfiguration.class,
				EmbeddedWebServerFactoryCustomizerAutoConfiguration.class,
				AuditAutoConfiguration.class, VopRestAutoConfiguration.class,
				ProviderHttpAspect.class, TestConfigurationForAuditBeans.class);

		context.refresh();
		assertNotNull(context);

		// test configuration and give vopRestAutoConfiguration a value for other tests
		vopRestAutoConfiguration = context.getBean(VopRestAutoConfiguration.class);
		assertNotNull(vopRestAutoConfiguration);
	}

	@After
	public void close() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void testConfiguration_Broken() {
		TestPropertyValues.of("vop.framework.client.rest.connectionTimeout=BLAHBLAH").applyTo(context);

		try {
			context.refresh();
			vopRestAutoConfiguration.restClientTemplate();
			fail("BipRestAutoConfiguration should have thrown IllegalStateException or BeansException");
		} catch (Exception e) {
			assertTrue(BeansException.class.isAssignableFrom(e.getClass()));
		} finally {
			TestPropertyValues.of("vop.framework.client.rest.connectionTimeout=" + CONNECTION_TIMEOUT).applyTo(context);
			context.refresh();
			vopRestAutoConfiguration = context.getBean(VopRestAutoConfiguration.class);
			assertNotNull(vopRestAutoConfiguration);
		}
	}

	@Test
	public void testWebConfiguration() throws Exception {
		assertNotNull(vopRestAutoConfiguration.providerHttpAspect());
		assertNotNull(vopRestAutoConfiguration.restProviderTimerAspect());
		assertNotNull(vopRestAutoConfiguration.restClientTemplate());
	}

	@Test
	public void testSetRetryHandlerToClientBuilder() throws Exception {
		VopRestAutoConfiguration config = new VopRestAutoConfiguration();
		HttpClientBuilder clientBuilder = HttpClients.custom();
        HttpClientUtils.setRetryHandlerToClientBuilder(clientBuilder, 3);
		assertTrue(((HttpRequestRetryHandler) ReflectionTestUtils.getField(clientBuilder, "retryHandler"))
				.retryRequest(new NoHttpResponseException(""), 0, new HttpClientContext()));
	}

}
