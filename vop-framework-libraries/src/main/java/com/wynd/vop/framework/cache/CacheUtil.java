/**
 *
 */
package com.wynd.vop.framework.cache;

import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.service.DomainResponse;

import java.util.UUID;

/**
 * Utils for cache
 */
public final class CacheUtil {

	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(CacheUtil.class);
	protected static final String SEPARATOR = "_";

	/**
	 * hide constructor.
	 */
	private CacheUtil() {
	}

	/**
	 * Returns false if the {@code domainResponse} is null, has errors or fatals,
	 * or has its {@code doNotCacheResponse} switch set to {@code true}.
	 * 
	 * @param domainResponse
	 * @return boolean
	 */
	public static boolean checkResultConditions(DomainResponse domainResponse) {
		return domainResponse == null || domainResponse.hasErrors() || domainResponse.hasFatals()
				|| domainResponse.isDoNotCacheResponse();
	}

	/**
	 * Generate a unique user based complex cache key. This implementation uses the
	 * user's unique username and the beneficiary as a prefix for the rest of the
	 * complex key
	 *
	 * @param keyValues
	 *            the key values
	 * @return the user based key
	 */
	public static String getUserBasedKey(final Object... keyValues) {
		return getUserBasedKey() + SEPARATOR + createKey(keyValues);
	}


	/**
	 * Creates a unique cache key using the given key values.
	 *
	 * @param keyValues
	 *            the key values
	 * @return the string
	 */
	public static final String createKey(final Object... keyValues) {
		final StringBuilder cacheKey = new StringBuilder();
		for (Object key : keyValues) {
			if (key != null) {
				if (cacheKey.length() > 0) {
					cacheKey.append(SEPARATOR);
				}
				cacheKey.append(key.hashCode());
			}
		}
		return cacheKey.toString();
	}

}
