package com.wynd.vop.framework.autoconfigure.cache;

import com.wynd.vop.framework.autoconfigure.cache.VopRedisCacheProperties.RedisExpires;
import com.wynd.vop.framework.cache.interceptor.CacheInterceptor;
import com.wynd.vop.framework.log.VopBanner;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.validation.Defense;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Imported by {@link VopCacheAutoConfiguration} so it can participate in the autoconfiguration bootstrap.
 * <p>
 * Configures the TTLs and expiration times for individual caches, as declared in the application YAML
 * under {@code vop.framework.cache}.
 *

 */
@Component
public class VopCachesConfig extends CachingConfigurerSupport {
	/** Class logger */
	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(VopCachesConfig.class);

	private static final String CACHE_MANAGER_BEAN_NAME = "cacheManager";

	/** Reference to the Spring Context. Need this in order to get direct access bean refs. */
	@Autowired
	private ApplicationContext applicationContext;

	/** Cache properties derived from application YAML */
	@Autowired
	private VopRedisCacheProperties vopRedisCacheProperties;

	/**
	 * Post construction validations.
	 */
	@PostConstruct
	public void postConstruct() {
		Defense.notNull(vopRedisCacheProperties, VopRedisCacheProperties.class.getSimpleName() + " cannot be null.");
	}

	/**
	 * On the RefreshScope refresh event, destroy the JedisConnectionFactory to force it
	 * to rebuild from {@link #redisConnectionFactory()} with the current setting from the application YAML.
	 * <p>
	 * This event listener <b>must</b> run <b>after</b> any connection factory related event listeners.
	 *
	 * @param event the refresh event
	 */
	@EventListener
	public void onApplicationEvent(final RefreshScopeRefreshedEvent event) {
		LOGGER.debug("Event activated to reconfigure " + CACHE_MANAGER_BEAN_NAME + ": event.getName() {}",
				event.getName() + "; event.getSource() {}",
				event.getSource());

		if (!applicationContext.containsBean(CACHE_MANAGER_BEAN_NAME)) {
			LOGGER.debug(CACHE_MANAGER_BEAN_NAME + " does not yet exist.");
		} else {
			RedisCacheManager rcm = (RedisCacheManager) applicationContext.getBean(CACHE_MANAGER_BEAN_NAME);
			rcm.initializeCaches();
			LOGGER.debug(CACHE_MANAGER_BEAN_NAME + " re-initialized.");
		}
	}

	/**
	 * Create the default cache configuration, with TTL set as declared by {@code reference:cache:defaultExpires} in the
	 * <i>[project]/src/main/resources/[app].yml</i>. Used by {@link RedisCacheManager}.
	 *
	 * @return RedisCacheConfiguration
	 */
	@Bean
	public RedisCacheConfiguration redisCacheConfiguration() {
		return RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(vopRedisCacheProperties.getDefaultExpires()));
	}

	/**
	 * Produce the Map of {@link RedisCacheConfiguration} objects derived from the list declared by
	 * {@code reference:cache:expires:*}
	 * in the <i>[app].yml</i>. Used by {@link RedisCacheManager}.
	 *
	 * @return Map&lt;String, org.springframework.data.redis.cache.RedisCacheConfiguration&gt;
	 */
	@Bean
	@RefreshScope
	public Map<String, org.springframework.data.redis.cache.RedisCacheConfiguration> redisCacheConfigurations() {
		LOGGER.debug("redisCacheConfigurations invoked here");
		return getRedisCacheConfigs();
	}

	private Map<String, org.springframework.data.redis.cache.RedisCacheConfiguration> getRedisCacheConfigs() {
		Map<String, org.springframework.data.redis.cache.RedisCacheConfiguration> cacheConfigs = new HashMap<>();

		if (!CollectionUtils.isEmpty(vopRedisCacheProperties.getExpires())) {
			// key = name, value - TTL
			final Map<String, Long> resultExpires = vopRedisCacheProperties.getExpires().stream().filter(o -> o.getName() != null)
					.filter(o -> o.getTtl() != null).collect(Collectors.toMap(RedisExpires::getName, RedisExpires::getTtl));
			for (Entry<String, Long> entry : resultExpires.entrySet()) {
				org.springframework.data.redis.cache.RedisCacheConfiguration rcc =
						org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
						.entryTtl(Duration.ofSeconds(entry.getValue()));
				cacheConfigs.put(entry.getKey(), rcc);
			}
		}
		return cacheConfigs;
	}

	/**
	 * Create the cacheManager bean, configured by the redisCacheConfiguration bean.
	 *
	 * @param redisConnectionFactory
	 * @return CacheManager
	 */
	@Bean
	@RefreshScope
	public RedisCacheManager cacheManager(final RedisConnectionFactory redisConnectionFactory) {
		if (LOGGER.isDebugEnabled()) {
			StringBuilder initialCacheProperties = new StringBuilder("null");
			if (vopRedisCacheProperties.getExpires() != null) {
				initialCacheProperties = new StringBuilder("");
				for (VopRedisCacheProperties.RedisExpires expires : vopRedisCacheProperties.getExpires()) {
					initialCacheProperties.append("[name=" + expires.getName() + ";TTL=" + expires.getTtl() + "]") ;
				}
			}
			LOGGER.debug(this.getClass() + ".cacheManager build with ["
					+ "RedisCacheWriter=previously configured JedisConnectionFactory"
					+ "; Default RedisCacheConfiguration[TTL=" + vopRedisCacheProperties.getDefaultExpires()
					+ ";all others as defined by RedisCacheConfiguration.defaultCacheConfig()]"
					+ "; InitialCacheConfigurations[" + initialCacheProperties.toString() + "]");
		}

		return RedisCacheManager
				.builder(redisConnectionFactory)
				.cacheDefaults(this.redisCacheConfiguration())
				.withInitialCacheConfigurations(this.redisCacheConfigurations())
				.transactionAware()
				.build();
	}

	/**
	 * Interface to get cache operation attribute sources. Required by {@link #cacheInterceptor()}.
	 *
	 * @return CacheOperationSource - the cache operation attribute source
	 */
	@Bean
	public CacheOperationSource cacheOperationSource() {
		return new AnnotationCacheOperationSource();
	}

	/**
	 * Custom {@link CacheInterceptor} to audit {@code cache.get(Object, Object)} operations.
	 *
	 * @return CacheInterceptor - the interceptor
	 */
	@Bean
	public org.springframework.cache.interceptor.CacheInterceptor cacheInterceptor() {
		LOGGER.debug("cacheInterceptor invoked here");
		org.springframework.cache.interceptor.CacheInterceptor interceptor = new CacheInterceptor(vopRedisCacheProperties.isAllowNullReturn());
		interceptor.setCacheOperationSources(cacheOperationSource());
		return interceptor;
	}

	/**
	 * Reference cache keys follow a specific naming convention, as enforced by this bean.
	 * <p>
	 * {@inheritDoc}
	 */
	@Bean
	@Override
	public KeyGenerator keyGenerator() {
		LOGGER.info("keyGenerator invoked here");
		return new KeyGenerator() { // NOSONAR lambda expressions do not accept optional params
			@Override
			public Object generate(final Object o, final Method method, final Object... objects) {
				LOGGER.debug("Generating cacheKey");
				final StringBuilder sb = new StringBuilder();
				sb.append(o.getClass().getName()).append(ClassUtils.PACKAGE_SEPARATOR_CHAR);
				sb.append(method.getName());
				for (final Object obj : objects) {
					sb.append(ClassUtils.PACKAGE_SEPARATOR_CHAR).append(obj.toString());
				}
				LOGGER.debug("Generated cacheKey: {}", sb.toString());
				return sb.toString();
			}
		};
	}

	/**
	 * Logging for cache operation errors using the {@link CacheErrorHandler} strategy.
	 *
	 * <p>
	 * {@inheritDoc}
	 */
	@Bean
	@Override
	public CacheErrorHandler errorHandler() {
		return new RedisCacheErrorHandler();
	}

	/**
	 * The {@link CacheErrorHandler} strategy for Redis implementations.
	 */
	public static class RedisCacheErrorHandler implements CacheErrorHandler {

		/*
		 * (non-Javadoc)
		 *
		 * @see org.springframework.cache.interceptor.CacheErrorHandler#handleCacheGetError(java.lang.RuntimeException,
		 * org.springframework.cache.Cache, java.lang.Object)
		 */
		@Override
		public void handleCacheGetError(final RuntimeException exception, final Cache cache, final Object key) {
			LOGGER.error(VopBanner.newBanner("Unable to get from cache " + cache.getName(), Level.ERROR), exception.getMessage());
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.springframework.cache.interceptor.CacheErrorHandler#handleCachePutError(java.lang.RuntimeException,
		 * org.springframework.cache.Cache, java.lang.Object, java.lang.Object)
		 */
		@Override
		public void handleCachePutError(final RuntimeException exception, final Cache cache, final Object key,
				final Object value) {
			LOGGER.error(VopBanner.newBanner("Unable to put into cache " + cache.getName(), Level.ERROR), exception.getMessage());
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.springframework.cache.interceptor.CacheErrorHandler#handleCacheEvictError(java.lang.RuntimeException,
		 * org.springframework.cache.Cache, java.lang.Object)
		 */
		@Override
		public void handleCacheEvictError(final RuntimeException exception, final Cache cache, final Object key) {
			LOGGER.error(VopBanner.newBanner("Unable to evict from cache " + cache.getName(), Level.ERROR),
					exception.getMessage());
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.springframework.cache.interceptor.CacheErrorHandler#handleCacheClearError(java.lang.RuntimeException,
		 * org.springframework.cache.Cache)
		 */
		@Override
		public void handleCacheClearError(final RuntimeException exception, final Cache cache) {
			LOGGER.error(VopBanner.newBanner("Unable to clean cache " + cache.getName(), Level.ERROR), exception.getMessage());
		}
	}
}
