package com.wynd.vop.framework.autoconfigure.swagger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.util.UrlPathHelper;

@RunWith(SpringRunner.class)
public class VopSwaggerAutoConfigurationTest {

	@InjectMocks
  VopSwaggerAutoConfiguration vopSwaggerAutoConfiguration;
	
	@Mock
	private ResourceHandlerRegistry registry;
	
	@Mock
	private ResourceHandlerRegistration registration;
	
	@Mock
	private MockHttpServletResponse response;
	
	@Mock
	private MockHttpServletRequest request;
	
	@Mock
	private ViewControllerRegistry viewControllerRegistry;
	
	@Before
	public void setUp() {
		GenericWebApplicationContext appContext = new GenericWebApplicationContext();
		appContext.refresh();

		this.registry = new ResourceHandlerRegistry(appContext, new MockServletContext(),
				new ContentNegotiationManager(), new UrlPathHelper());

		this.registration = this.registry.addResourceHandler("/**");
		this.registration.addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");
		
		this.viewControllerRegistry = new ViewControllerRegistry(new StaticApplicationContext());
		this.request = new MockHttpServletRequest("GET", "/");
		this.response = new MockHttpServletResponse();
	}

	@Test
	public void swaggerAutoConfigurationTest() throws Exception {
		vopSwaggerAutoConfiguration.addResourceHandlers(registry);
		vopSwaggerAutoConfiguration.addViewControllers(viewControllerRegistry);
	}
}
