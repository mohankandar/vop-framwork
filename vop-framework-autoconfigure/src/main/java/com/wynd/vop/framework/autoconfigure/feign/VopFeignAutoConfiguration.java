package com.wynd.vop.framework.autoconfigure.feign;

import feign.Logger;
import feign.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Auto configuration for feign enabled REST clients (e.g.
 * {@code EnableFeignClients}).
 *
 * Created by rthota on 9/05/17.
 */
@Configuration
public class VopFeignAutoConfiguration {

	@Value("${vop.framework.client.rest.connectionTimeout:20000}")
	private String connectionTimeout;

	/**
	 * Custom Feign Error Decoder.
	 *
	 * @return the feign custom error decoder
	 */
	@Bean
	public FeignCustomErrorDecoder feignCustomErrorDecoder() {
		return new FeignCustomErrorDecoder();
	}

	/**
	 * Request options.
	 *
	 * @param env the env
	 * @return the request. options
	 */
	@Bean
	Request.Options requestOptions(ConfigurableEnvironment env) {
		int ribbonReadTimeout = env.getProperty("ribbon.ReadTimeout", int.class, 6000);
		int ribbonConnectionTimeout = env.getProperty("ribbon.ConnectTimeout", int.class, 3000);

		return new Request.Options(ribbonConnectionTimeout, ribbonReadTimeout);
	}

	/**
	 * Feign logger level.
	 *
	 * @return the logger. level
	 */
	@Bean
	Logger.Level feignLoggerLevel() {
		return Logger.Level.FULL;
	}
}
