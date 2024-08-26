package com.wynd.vop.framework.autoconfigure.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * An implementation of {@link RequestInterceptor} that adds the JWT token from
 * the originating request, and adds it to the outgoing request. No changes are
 * made to the response.
 * <p>
 * Use this class when making feign assisted (e.g. {@code @EnableFeignClients})
 * inter-=service REST calls that require PersonTraits.
 */
public class TokenFeignRequestInterceptor implements RequestInterceptor {

	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(TokenFeignRequestInterceptor.class);

	/**
	 * Add token header from the originating request to the outgoing request. No
	 * changes made to the response.
	 */
	@Override
	public void apply(RequestTemplate template) {
		//TODO: Apply token map
	}
}