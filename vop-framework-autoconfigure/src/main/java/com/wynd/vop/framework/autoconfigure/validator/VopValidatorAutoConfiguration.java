package com.wynd.vop.framework.autoconfigure.validator;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Configuration for JSR303 validation (and other validation) message sources.
 */
@Configuration
@AutoConfigureBefore(MessageSourceAutoConfiguration.class)
public class VopValidatorAutoConfiguration {

	/**
	 * Validator.
	 *
	 * @param messageSource the message source
	 * @return the local validator factory bean
	 */
	@Bean
	@ConditionalOnMissingBean
	public LocalValidatorFactoryBean validator(MessageSource messageSource) {
		LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
		validatorFactoryBean.setValidationMessageSource(messageSource);
		return validatorFactoryBean;
	}
}
