package com.wynd.vop.framework.autoconfigure.cache.server;

import com.wynd.vop.framework.autoconfigure.cache.VopRedisCacheProperties;
import com.wynd.vop.framework.autoconfigure.cache.server.VopEmbeddedRedisServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestEmbeddedRedisServerAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public VopEmbeddedRedisServer vopEmbeddedRedisServer() {
		return new VopEmbeddedRedisServer();
	}

	@Bean
	@ConditionalOnMissingBean
	public RedisProperties redisProperties() {
		RedisProperties redisProperties = new RedisProperties();
		redisProperties.setPort(0);
		return redisProperties;
	}

	@Bean
	@ConditionalOnMissingBean
	public VopRedisCacheProperties vopRedisCacheProperties() {
		return new VopRedisCacheProperties();
	}
}