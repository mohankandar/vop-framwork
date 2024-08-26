package com.wynd.vop.framework.autoconfigure.cache;

import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Properties used to configure the Redis Cache.
 * <p>
 * For each cache used by the application, the Application YAML
 * (e.g. <tt>vop-<i>your-app-name</i>.yml</tt>) must declare
 * properties under {@code vop.framework:cache}:
 * <p>
 * <table border="1px">
 * <tr><th colspan="3">Properties under: {@code vop.framework:cache}</th></tr>
 * <tr><th>Property Name</th><th>Default Value</th><th>Type</th></tr>
 * <tr><td>defaultExpires</td><td>86400</td><td>Long</td></tr>
 * <tr><td>expires</td><td>null</td><td>List&lt;RedisExpires&gt;</td></tr>
 * </table>
 * <p>
 * The {@link RedisExpires} list is populated from list entries in the application yaml
 * under {@code vop.framework:cache:expires}.
 *
 */
@Component
public class VopRedisCacheProperties {
	/** Class logger */
	static final VopLogger LOGGER = VopLoggerFactory.getLogger(VopRedisCacheProperties.class);

	/** List of inner class {@link RedisExpires} configuration objects */
	private List<RedisExpires> expires;

	/** The default expiration time */
	private Long defaultExpires = 86400L;

	/** Allows Cached methods to return null values */
	private boolean allowNullReturn = true;

	/**
	 * The inner class {@link RedisExpires} configuration object.
	 *
	 * @param expires
	 */
	public void setExpires(final List<RedisExpires> expires) {
		this.expires = expires;
	}

	/**
	 * Default expiration time if cache does not appear in the expires list.
	 *
	 * @param defaultExpires the default expiration time
	 */
	public void setDefaultExpires(final Long defaultExpires) {
		this.defaultExpires = defaultExpires;
	}

	/**
	 * List of inner class {@link RedisExpires} configuration objects.
	 *
	 * @return List of RedisExpires objects
	 */
	public List<RedisExpires> getExpires() {
		return this.expires;
	}

	/**
	 * Default expiration time if cache does not appear in the expires list.
	 *
	 * @return Long the default expiration time
	 */
	public Long getDefaultExpires() {
		return this.defaultExpires;
	}

	/**
	 * Defines whether a cached method should return null values. Default is true,
	 * meaning any cached methods that return null will not be modified. If false, the interceptor
	 * will instead return {@code new Object()}
	 *
	 * @return If cached methods are allowed to return null
	 */
	public boolean isAllowNullReturn() {
		return allowNullReturn;
	}

	/**
	 * Defines whether a cached method should return null values. Default is true,
	 * meaning any cached methods that return null will not be modified. If false, the interceptor
	 * will instead return {@code new Object()}
	 *
	 * @param allowNullReturn the boolean if should cached methods return null
	 */
	public void setAllowNullReturn(boolean allowNullReturn) {
		this.allowNullReturn = allowNullReturn;
	}

	/**
	 * Inner class to hold the time to live (ttl) for a given cache name.
	 * <p>
	 * A list of RedisExpires objects is populated from list entries in the application yaml
	 * under {@code vop.framework:redis:cache:expires}.
	 *
	 */
	public static class RedisExpires {

		/** The cache name */
		private String name;

		/** The time-to-live for items cached under the cache name */
		private Long ttl;

		/**
		 * Redis cache name for which to set the time-to-live.
		 *
		 * @return String
		 */
		public String getName() {
			return name;
		}

		/**
		 * Redis cache name for which to set the time-to-live.
		 *
		 * @param name
		 */
		public void setName(final String name) {
			this.name = name;
		}

		/**
		 * Time-to-live for items cached under the cache name.
		 *
		 * @return Long
		 */
		public Long getTtl() {
			return ttl;
		}

		/**
		 * Time-to-live for items cached under the cache name.
		 *
		 * @param ttl
		 */
		public void setTtl(final Long ttl) {
			this.ttl = ttl;
		}
	}
}
