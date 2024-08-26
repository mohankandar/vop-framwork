package com.wynd.vop.framework.autoconfigure.cache;

import com.wynd.vop.framework.autoconfigure.audit.AuditAutoConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Duration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ImportAutoConfiguration(RefreshAutoConfiguration.class)
@ContextConfiguration(classes = { VopRedisCacheProperties.class, RedisProperties.class })
public class VopJedisConnectionConfigTest {

	private AnnotationConfigApplicationContext context;

	@InjectMocks
	VopJedisConnectionConfig vopJedisConnectionConfig;

	/** The output capture. */
	@Rule
	public OutputCaptureRule outputCapture = new OutputCaptureRule();

	// Captor is genericised with ch.qos.logback.classic.spi.LoggingEvent
	@Captor
	private ArgumentCaptor<ch.qos.logback.classic.spi.LoggingEvent> captorLoggingEvent;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		if (context != null) {
			context.close();
		}
	}

	@Test
	public final void testRedisConnectionFactory()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		context = new AnnotationConfigApplicationContext();
		context.register(RedisAutoConfiguration.class,
				VopRedisCacheProperties.class,
				VopJedisConnectionConfig.class, VopCacheAutoConfiguration.class,
				AuditAutoConfiguration.class, TestConfigurationForAuditBeans.class, BuildProperties.class,
				TestConfigurationClassForBuildProperties.class);
		context.getBeanFactory().registerScope("refresh", new SimpleThreadScope());
		context.refresh();

		vopJedisConnectionConfig = context.getBean(VopJedisConnectionConfig.class);
		assertNotNull(vopJedisConnectionConfig);

		vopJedisConnectionConfig.redisConnectionFactory();
		assertTrue(outputCapture.toString().contains("poolConfig:"));

		RedisProperties props = vopJedisConnectionConfig.redisProperties;
		props.getJedis().getPool().setMaxActive(1000);
		props.getJedis().getPool().setMaxIdle(1000);
		props.getJedis().getPool().setMaxWait(Duration.ofMillis(1000L));
		props.getJedis().getPool().setMinIdle(1000);

		vopJedisConnectionConfig.redisConnectionFactory();
		assertTrue(outputCapture.toString().contains("MaxIdle=1000"));

		props = vopJedisConnectionConfig.redisProperties;
		props.setSsl(true);
		props.setTimeout(Duration.ofSeconds(0L));
		props.getJedis().getPool().setMaxActive(0);
		props.getJedis().getPool().setMaxIdle(0);
		props.getJedis().getPool().setMaxWait(null);
		props.getJedis().getPool().setMinIdle(0);

		vopJedisConnectionConfig.redisConnectionFactory();
		assertTrue(outputCapture.toString().contains("MinIdle=0"));
	}

}
