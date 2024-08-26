package com.wynd.vop.framework.autoconfigure.cache;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class VopRedisCachePropertiesTest {

	@Test
	public void testGetters() {
		VopRedisCacheProperties vopRedisCacheProperties = new VopRedisCacheProperties();
		assertNull(vopRedisCacheProperties.getExpires());
		assertEquals(new Long(86400L), vopRedisCacheProperties.getDefaultExpires());
		assertTrue(vopRedisCacheProperties.isAllowNullReturn());
	}

	@Test
	public void testSetters() {
		VopRedisCacheProperties vopRedisCacheProperties = new VopRedisCacheProperties();
		List<VopRedisCacheProperties.RedisExpires> listRedisExpires = new ArrayList<>();
		VopRedisCacheProperties.RedisExpires redisExpires = new VopRedisCacheProperties.RedisExpires();
		redisExpires.setName("methodcachename_projectname_projectversion");
		redisExpires.setTtl(86400L);
		listRedisExpires.add(0, redisExpires);
		vopRedisCacheProperties.setExpires(listRedisExpires);
		vopRedisCacheProperties.setDefaultExpires(500L);
		vopRedisCacheProperties.setAllowNullReturn(false);
		assertTrue(!vopRedisCacheProperties.getExpires().isEmpty());
		assertTrue(Long.valueOf(86400L).equals(vopRedisCacheProperties.getExpires().get(0).getTtl()));
		assertEquals(new Long(500L), vopRedisCacheProperties.getDefaultExpires());
		assertFalse(vopRedisCacheProperties.isAllowNullReturn());
	}
}