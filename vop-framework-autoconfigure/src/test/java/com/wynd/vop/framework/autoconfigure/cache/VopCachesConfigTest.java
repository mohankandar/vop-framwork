package com.wynd.vop.framework.autoconfigure.cache;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.Assert.assertTrue;

public class VopCachesConfigTest {

	@Test
	public void getRedisCacheConfigsWithNullExpiresTest() {
		VopCachesConfig config = new VopCachesConfig();
		VopRedisCacheProperties vopRedisCacheProperties = new VopRedisCacheProperties();
		vopRedisCacheProperties.setExpires(null);
		ReflectionTestUtils.setField(config, "vopRedisCacheProperties", vopRedisCacheProperties);
		Map<String, org.springframework.data.redis.cache.RedisCacheConfiguration> map =
				ReflectionTestUtils.invokeMethod(config, "getRedisCacheConfigs", (Object[]) null);
		assertTrue(map.isEmpty());
	}
}
