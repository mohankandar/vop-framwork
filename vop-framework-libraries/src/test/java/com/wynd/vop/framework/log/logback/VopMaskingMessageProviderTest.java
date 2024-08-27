package com.wynd.vop.framework.log.logback;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.system.OutputCaptureRule;

import static org.assertj.core.api.Assertions.assertThat;

public class VopMaskingMessageProviderTest {

	@Rule
	public OutputCaptureRule capture = new OutputCaptureRule();

	private static final VopLogger logger = VopLoggerFactory.getLogger(VopMaskingMessageProviderTest.class);

	@Test
	public void shouldMask() throws Exception {
		logger.info("This is a test with credit card number {}", "4111111111111111");
		assertThat(capture.toString()).contains("************1111").doesNotContain("4111111111111111");
	}

	@Test
	public void shouldContainStackTrace() throws Exception {
		logger.error("This is an error", new RuntimeException("Error!!"));
		DocumentContext out = JsonPath.parse(capture.toString());
		assertThat(out.read("$.severity", String.class)).isEqualTo("ERROR");
		assertThat(out.read("$.message", String.class)).contains("This is an error");
		assertThat(out.read("$.message", String.class)).contains("java.lang.RuntimeException: Error!!");
	}

}
