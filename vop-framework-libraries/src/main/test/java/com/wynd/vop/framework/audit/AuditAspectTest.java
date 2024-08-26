package com.wynd.vop.framework.audit;

import com.wynd.vop.framework.audit.annotation.Auditable;
import com.wynd.vop.framework.messages.MessageKeys;
import com.wynd.vop.framework.messages.MessageSeverity;
import com.wynd.vop.framework.service.DomainRequest;
import com.wynd.vop.framework.service.DomainResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Created by vgadda on 8/17/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Config.class, JacksonAutoConfiguration.class })
public class AuditAspectTest {

	@Autowired
	AuditableService auditableService;

	@Test
	public void test() {
		MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpServletRequest));
		AuditServiceRequest request = new AuditServiceRequest();
		request.setText("AuditServiceRequest");
		auditableService.annotatedMethod(request);
	}
}

@Component
class TestAuditableService implements AuditableService {

	@Override
	@Auditable(event = AuditEvents.API_REST_REQUEST, activity = "testActivity")
	public DomainResponse annotatedMethod(DomainRequest request) {
		DomainResponse response = new DomainResponse();
		response.addMessage(MessageSeverity.INFO, null, MessageKeys.NO_KEY);
		return response;
	}
}

interface AuditableService {
	DomainResponse annotatedMethod(DomainRequest request);
}

class AuditServiceRequest extends DomainRequest {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String text;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}

@Configuration
@ComponentScan({ "com.wynd.vop.framework.audit",
		"com.wynd.vop.framework.audit.autoconfigure" })
@EnableAspectJAutoProxy(proxyTargetClass = true)
class Config {

}