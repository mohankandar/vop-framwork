package com.wynd.vop.framework.autoconfigure.swagger;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * BipSwaggerAutoConfiguration that adds Spring MVC configuration to enable
 * Swagger UI access
 *
 */
@Configuration
@EnableWebMvc
@ConditionalOnProperty(prefix = "vop.framework.swagger", name = "enabled", matchIfMissing = true)
public class VopSwaggerAutoConfiguration implements WebMvcConfigurer {

	/**
	 * Add handlers to serve static Swagger UI resources.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
		registry.addResourceHandler("/**").addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");
	}

	/**
	 * This method was introduced just for convenient Swagger UI access. Without it
	 * Swagger UI can be accessed with /index.html GET call.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/swagger-ui.html").setViewName("forward:/index.html");
	}
}
