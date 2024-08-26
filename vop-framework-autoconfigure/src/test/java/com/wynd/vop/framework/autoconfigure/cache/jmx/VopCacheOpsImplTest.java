package com.wynd.vop.framework.autoconfigure.cache.jmx;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import redis.clients.jedis.JedisPoolConfig;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class VopCacheOpsImplTest {

	/** Prefix for log statements */
	private static final String PREFIX = ":::: ";

	/** The output capture. */
	@Rule
	public OutputCaptureRule outputCapture = new OutputCaptureRule();

	@SuppressWarnings("rawtypes")
	@Mock
	private ch.qos.logback.core.Appender mockAppender;

	@Mock
	private RedisCacheManager cacheManager;

	VopVopCacheOpsImpl vopCacheOpsImpl;

	@Before
	public void setup() {
		vopCacheOpsImpl = new VopVopCacheOpsImpl();
		assertNotNull(vopCacheOpsImpl);
	}

	@Test
	public void testClearAllCaches() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field cm = vopCacheOpsImpl.getClass().getDeclaredField("cacheManager");
		cm.setAccessible(true);
		cm.set(vopCacheOpsImpl, cacheManager);

		vopCacheOpsImpl.clearAllCaches();
	}

	@Test
	public void testClearAllCachesNoCache()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field cm = vopCacheOpsImpl.getClass().getDeclaredField("cacheManager");
		cm.setAccessible(true);
		cm.set(vopCacheOpsImpl, null);

		vopCacheOpsImpl.clearAllCaches();
	}

	@Test
	public void testlogCacheConfigProperties() {
		RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
		ReflectionTestUtils.setField(redisCacheConfiguration, "ttl", Duration.ofSeconds(1500L));
		ReflectionTestUtils.setField(vopCacheOpsImpl, "redisCacheConfiguration", redisCacheConfiguration);
		Properties properties = new Properties();
		properties.setProperty("testKey", "testValue");
		BuildProperties buildProperties = new BuildProperties(properties);
		Map<String, RedisCacheConfiguration> redisCacheConfigurations = new HashMap<String, RedisCacheConfiguration>();
		redisCacheConfigurations.put("testKeyForConfig", redisCacheConfiguration);
		ReflectionTestUtils.setField(vopCacheOpsImpl, "redisCacheConfigurations", redisCacheConfigurations);
		RedisCacheManager mockCacheManager = mock(RedisCacheManager.class);
		when(mockCacheManager.getCacheNames()).thenReturn(Arrays.asList(new String[] { "cacheName1" }));
		ReflectionTestUtils.setField(vopCacheOpsImpl, "cacheManager", mockCacheManager);

		ReflectionTestUtils.setField(vopCacheOpsImpl, "buildProperties", buildProperties);
		vopCacheOpsImpl.logCacheConfigProperties();
		String outString = outputCapture.toString();

		assertTrue(outString.contains("Cache Configs in '" + buildProperties.getName() + "'"));
		assertTrue(outString.contains("Config for Default: [TTL=" + redisCacheConfiguration.getTtl().toMillis()));
		assertTrue(outString.contains(redisCacheConfiguration.getConversionService().getClass().getName()));
		assertTrue(outString.contains(PREFIX + "Config for [key="));
		assertTrue(outString.contains(PREFIX + "Cache names in CacheManager: [" + "cacheName1" + "]"));
	}

	@Test
	public void testlogCacheConfigPropertiesWithNullValues() {
		try {
			VopVopCacheOpsImpl vopCacheOpsImpl = new VopVopCacheOpsImpl();
			RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
			ReflectionTestUtils.setField(vopCacheOpsImpl, "redisCacheConfiguration", redisCacheConfiguration);
			ReflectionTestUtils.setField(vopCacheOpsImpl, "redisCacheConfigurations", null);
			RedisCacheManager mockCacheManager = mock(RedisCacheManager.class);
			when(mockCacheManager.getCacheNames()).thenReturn(Arrays.asList(new String[] {}));
			ReflectionTestUtils.setField(vopCacheOpsImpl, "cacheManager", mockCacheManager);
			vopCacheOpsImpl.logCacheConfigProperties();

			Map<String, RedisCacheConfiguration> redisCacheConfigurations = new HashMap<String, RedisCacheConfiguration>();
			ReflectionTestUtils.setField(vopCacheOpsImpl, "redisCacheConfigurations", redisCacheConfigurations);
			vopCacheOpsImpl.logCacheConfigProperties();
		} catch (Exception e) {
			fail("Exception should not be thrown");
		}
	}

	@Test
	public void testlogCurrentJedisConnectionFactoryFields() {
		JedisConnectionFactory mockRedisConnectionFactory = mock(JedisConnectionFactory.class);

		RedisStandaloneConfiguration mockRsc = mock(RedisStandaloneConfiguration.class);
		when(mockRedisConnectionFactory.getStandaloneConfiguration()).thenReturn(mockRsc);

		JedisClientConfiguration MockJcc = mock(JedisClientConfiguration.class);
		when(MockJcc.getConnectTimeout()).thenReturn(Duration.ofSeconds(1500L));
		when(MockJcc.getReadTimeout()).thenReturn(Duration.ofSeconds(1500L));
		when(mockRedisConnectionFactory.getClientConfiguration()).thenReturn(MockJcc);

		GenericObjectPoolConfig<JedisPoolConfig> gopc = new GenericObjectPoolConfig<>();
		@SuppressWarnings("unchecked")
		GenericObjectPoolConfig<redis.clients.jedis.Jedis> mockGopc = mock(gopc.getClass());
		when(mockRedisConnectionFactory.getPoolConfig()).thenReturn(mockGopc);

		ReflectionTestUtils.setField(vopCacheOpsImpl, "redisConnectionFactory", mockRedisConnectionFactory);
		vopCacheOpsImpl.logCurrentJedisConnectionFactoryFields();
		String outString = outputCapture.toString();

		assertTrue(outString.contains(PREFIX + "    clientName = "));
		verify(mockRsc, times(1)).getDatabase();
		verify(mockRsc, times(1)).getHostName();
		verify(mockRsc, times(1)).getPort();

		verify(MockJcc, times(1)).getClientName();
		verify(MockJcc, times(1)).getConnectTimeout();
		verify(MockJcc, times(1)).getHostnameVerifier();
		verify(MockJcc, times(1)).getPoolConfig();
		verify(MockJcc, times(1)).getReadTimeout();

		verify(mockGopc, times(1)).getEvictionPolicyClassName();
		verify(mockGopc, times(1)).getEvictorShutdownTimeoutMillis();
		verify(mockGopc, times(1)).getMaxIdle();
		verify(mockGopc, times(1)).getMaxTotal();
		verify(mockGopc, times(1)).getMaxWaitMillis();
		verify(mockGopc, times(1)).getMinEvictableIdleTimeMillis();
		verify(mockGopc, times(1)).getMinIdle();
		verify(mockGopc, times(1)).getNumTestsPerEvictionRun();
		verify(mockGopc, times(1)).getSoftMinEvictableIdleTimeMillis();
		verify(mockGopc, times(1)).getTimeBetweenEvictionRunsMillis();
		verify(mockGopc, times(1)).getBlockWhenExhausted();
		verify(mockGopc, times(1)).getFairness();
		verify(mockGopc, times(1)).getLifo();
		verify(mockGopc, times(1)).getTestOnBorrow();
		verify(mockGopc, times(1)).getTestOnCreate();
		verify(mockGopc, times(1)).getTestOnReturn();
		verify(mockGopc, times(1)).getTestWhileIdle();
	}

	@Test
	public void testlogCurrentJedisConnectionFactoryFieldsWithNullValues() {
		try {
			ReflectionTestUtils.setField(vopCacheOpsImpl, "redisConnectionFactory", null);
			vopCacheOpsImpl.logCurrentJedisConnectionFactoryFields();
		} catch (Exception e) {
			fail("Exception should not be thrown");
		}
	}

	@Test
	public void testlogCurrentCacheManagerFields() {
		RedisCacheManager mockCacheManager = mock(RedisCacheManager.class);
		String cacheName = "cacheName1";
		when(mockCacheManager.getCacheNames()).thenReturn(Arrays.asList(new String[] { cacheName }));
		RedisCache mockCache = mock(RedisCache.class);
		when(mockCache.getCacheConfiguration()).thenReturn(RedisCacheConfiguration.defaultCacheConfig());
		when(mockCacheManager.isTransactionAware()).thenReturn(false);
		when(mockCacheManager.getCache(cacheName)).thenReturn(mockCache);

		ReflectionTestUtils.setField(vopCacheOpsImpl, "cacheManager", mockCacheManager);
		ReflectionTestUtils.setField(vopCacheOpsImpl, "redisCacheConfiguration", RedisCacheConfiguration.defaultCacheConfig());

		vopCacheOpsImpl.logCurrentCacheManagerFields();
		String outString = outputCapture.toString();

		assertTrue(outString.contains(PREFIX + "RedisCacheManager = "));
		assertTrue(outString.contains(PREFIX + "RedisCacheConfiguration (immutable) = "));
		assertTrue(outString.contains(PREFIX + "Caches = "));

	}

}
