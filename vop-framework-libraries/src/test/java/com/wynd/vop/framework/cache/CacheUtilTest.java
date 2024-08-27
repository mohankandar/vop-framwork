package com.wynd.vop.framework.cache;

import com.wynd.vop.framework.service.DomainResponse;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CacheUtilTest {

	@After
	public void teardown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void testCheckResultConditions() {
		DomainResponse domainResponse = new DomainResponse();
		domainResponse.setDoNotCacheResponse(true);
		boolean result = CacheUtil.checkResultConditions(domainResponse);
		assertTrue(result);
		domainResponse.setDoNotCacheResponse(false);
		result = CacheUtil.checkResultConditions(domainResponse);
		assertFalse(result);
	}

}
