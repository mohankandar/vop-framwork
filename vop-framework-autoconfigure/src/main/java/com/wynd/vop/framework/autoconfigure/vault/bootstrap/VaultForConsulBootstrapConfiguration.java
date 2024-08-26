package com.wynd.vop.framework.autoconfigure.vault.bootstrap;

import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.vault.config.consul.VaultConsulProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.env.VaultPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * This class bootstraps the Vault PropertySource as the first source loaded. This is important so that we can use the Vault generated Consul ACL token to authenticate with Consul
 * for both Service Discovery and a K/V configuration source.
 * 
 * This is a workaround for this not being supported by the spring-cloud-vault library. https://github.com/spring-cloud/spring-cloud-vault/issues/58
 * 
 * @author Jason Luck
 */
@Configuration
@AutoConfigureOrder(1)
@ConditionalOnProperty(prefix = "spring.cloud.vault.consul", name = "enabled", matchIfMissing = false)
public class VaultForConsulBootstrapConfiguration implements ApplicationContextAware,
		InitializingBean {

	
	/** Logger object */
	private static final VopLogger LOGGER = VopLoggerFactory.getLogger(VaultForConsulBootstrapConfiguration.class);
	
	/** Reference to the Spring Context. Need this in order to get access to the Environment object. */
	private ApplicationContext applicationContext;

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {

		ConfigurableEnvironment ce = (ConfigurableEnvironment) applicationContext
				.getEnvironment();

		if (ce.getPropertySources().contains("consul-token")) {
			LOGGER.debug("Consul ACL Token already present in properties, no need to generate a new one.");
			return;
		}

		LOGGER.info("Initializing Vault Property Source...");
		VaultOperations vaultOperations = applicationContext
				.getBean(VaultOperations.class);
		VaultConsulProperties consulProperties = applicationContext
				.getBean(VaultConsulProperties.class);

		VaultPropertySource vaultPropertySource = new VaultPropertySource(
				vaultOperations, String.format("%s/creds/%s",
						consulProperties.getBackend(), consulProperties.getRole()));
		
		//Store the generator Consul ACL token in properties for both service discovery and consul configuration.
		Map<String, Object> props = new HashMap<>();
		props.put("spring.cloud.consul.token", vaultPropertySource.getProperty("token")); //Consul Config
		props.put("spring.cloud.consul.discovery.acl-token", vaultPropertySource.getProperty("token")); //Service Discovery
		MapPropertySource mps = new MapPropertySource("consul-token", props);

		ce.getPropertySources().addFirst(mps);
		
	}
}
