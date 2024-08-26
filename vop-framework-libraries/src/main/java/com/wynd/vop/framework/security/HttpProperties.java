package com.wynd.vop.framework.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Class used as http properties in projects.
 * The values assigned to members in this class are defaults,
 * and are typically overridden in yml and spring configuration.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "vop.framework.security.http")
public class HttpProperties {
	private String contentSecurityPolicy= "script-src 'self'";
	
    @NestedConfigurationProperty
	private Cors cors;
    
    /**
     * Inner class to hold the CORS related metadata
     * <p>
     * Any properties under {@code vop.framework.security.http.cors}.
     *
     */
    @Getter
    @Setter
    public static class Cors {
        private boolean enabled = false;
    }
    
}
