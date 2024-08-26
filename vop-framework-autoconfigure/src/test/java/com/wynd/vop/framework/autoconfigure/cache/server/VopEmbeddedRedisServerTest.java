package com.wynd.vop.framework.autoconfigure.cache.server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.junit.Assert.*;

/**
 *
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestEmbeddedRedisServerAutoConfiguration.class)
public class VopEmbeddedRedisServerTest {

	@Autowired
  VopEmbeddedRedisServer referenceEmbeddedServer;

	@Before
	public void setUp() {
		try {
			if (referenceEmbeddedServer.getRedisServer().isActive()) {
				referenceEmbeddedServer.stopRedis();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test(timeout = 5000L)
	public void testSimpleRun() throws Exception {
		try {
			referenceEmbeddedServer.startRedis();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			referenceEmbeddedServer.stopRedis();
		}
	}

	@Test
	public void shouldAllowSubsequentRuns() throws Exception {
		try {
			referenceEmbeddedServer.startRedis();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			referenceEmbeddedServer.stopRedis();
		}

		try {
			referenceEmbeddedServer.startRedis();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			referenceEmbeddedServer.stopRedis();
		}

		try {
			referenceEmbeddedServer.startRedis();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			referenceEmbeddedServer.stopRedis();
		}
	}

	@Test
	public void testSimpleOperationsAfterRun() throws Exception {
		JedisPool pool = null;
		Jedis jedis = null;
		try {
			referenceEmbeddedServer.startRedis();

			pool = new JedisPool("localhost", referenceEmbeddedServer.getRedisServer().ports().get(0));
			jedis = pool.getResource();
			jedis.mset("abc", "1", "def", "2");

			assertEquals("1", jedis.mget("abc").get(0));
			assertEquals("2", jedis.mget("def").get(0));
			assertEquals(null, jedis.mget("xyz").get(0));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				pool.close();
			}
			referenceEmbeddedServer.stopRedis();
		}
	}

	@Test
	public void shouldIndicateInactiveBeforeStart() throws Exception {
		referenceEmbeddedServer.stopRedis();
		assertFalse(referenceEmbeddedServer.getRedisServer().isActive());
	}

	@Test
	public void shouldIndicateActiveAfterStart() throws Exception {
		try {
			referenceEmbeddedServer.startRedis();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			assertTrue(referenceEmbeddedServer.getRedisServer().isActive());
			referenceEmbeddedServer.stopRedis();
		}
	}

	@Test
	public void shouldIndicateInactiveAfterStop() throws Exception {
		try {
			referenceEmbeddedServer.startRedis();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			referenceEmbeddedServer.stopRedis();
			assertFalse(referenceEmbeddedServer.getRedisServer().isActive());
		}
	}

	@After
	public void teardown() {
		try {
			if (referenceEmbeddedServer.getRedisServer().isActive()) {
				referenceEmbeddedServer.stopRedis();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}