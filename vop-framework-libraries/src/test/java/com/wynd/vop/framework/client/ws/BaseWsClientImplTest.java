package com.wynd.vop.framework.client.ws;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BaseWsClientImplTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		new TestBaseWsClient();
	}

}
class TestBaseWsClient extends BaseWsClientImpl {
	
	TestBaseWsClient() {
		super();
	}
}