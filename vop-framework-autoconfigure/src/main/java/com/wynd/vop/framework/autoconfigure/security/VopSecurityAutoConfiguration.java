package com.wynd.vop.framework.autoconfigure.security;

import com.wynd.vop.framework.client.rest.template.RestClientTemplate;
import com.wynd.vop.framework.log.VopLogger;
import com.wynd.vop.framework.log.VopLoggerFactory;
import com.wynd.vop.framework.rest.exception.BasicErrorController;
import com.wynd.vop.framework.security.HttpProperties;
import javax.servlet.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * AutoConfiguration for various authentication types on the Platform (basic authentication, JWT)
 */
@Configuration
@Order(SecurityProperties.DEFAULT_FILTER_ORDER)
@EnableConfigurationProperties({WebEndpointProperties.class, HttpProperties.class,
    VopRestClientProperties.class})
public class VopSecurityAutoConfiguration {

  private static final String NOOP_PREFIX = "{noop}";

  /**
   * The Constant LOGGER.
   */
  private static final VopLogger LOGGER = VopLoggerFactory.getLogger(
      VopSecurityAutoConfiguration.class);

  /**
   * Adapter for JWT
   */

  @Value("${vop.framework.security.actuator.username:admin}")
  String actuatorUserName;

  @Value("${vop.framework.security.actuator.password:default}")
  String actuatorPassword;

  @Value("${vop.framework.security.actuator.role:ACTUATOR}")
  String actuatorRole;

  @Autowired
  private WebEndpointProperties webEndpointProperties;


  public VopSecurityAutoConfiguration(WebEndpointProperties webEndpointProperties,
      HttpProperties httpProperties) {
    this.webEndpointProperties = webEndpointProperties;
    this.httpProperties = httpProperties;
  }

  @Autowired
  private HttpProperties httpProperties;

  @Autowired
  @Lazy
  private RestClientTemplate restClientTemplate;

  @Bean
  @Order(1)
  public SecurityFilterChain basicFilterChain(HttpSecurity http) throws Exception {

    http.antMatcher(webEndpointProperties.getBasePath() + "/**")
        .authorizeRequests().antMatchers(webEndpointProperties.getBasePath() + "/info").permitAll()
        .antMatchers(webEndpointProperties.getBasePath() + "/health").permitAll()
        .antMatchers(webEndpointProperties.getBasePath() + "/**")
        .hasRole(actuatorRole).and().httpBasic().and().csrf()
        .disable()
        .addFilterAfter(headerFilter(), UsernamePasswordAuthenticationFilter.class);
    http.headers().contentSecurityPolicy(httpProperties.getContentSecurityPolicy());
    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    http.authenticationManager(authenticationManager());
    return http.build();
  }

  @Bean
  public InMemoryUserDetailsManager userDetailsService() {
    UserDetails user = User.withUsername(actuatorUserName)
        .password(NOOP_PREFIX.concat(actuatorPassword))
        .roles(actuatorRole)
        .build();
    return new InMemoryUserDetailsManager(user);
  }

  @Bean
  public Filter headerFilter() {
    return new RemoveHeaderFilter();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setPasswordEncoder(passwordEncoder());
    provider.setUserDetailsService(userDetailsService());
    return new ProviderManager(provider);
  }

  /**
   * The Rest Client Template
   *
   * @return RestClientTemplate the rest client template
   */
  @Bean
  @ConditionalOnMissingBean
  protected RestClientTemplate restClientTemplate() {
    return new RestClientTemplate();
  }


  @Bean
  @ConditionalOnMissingBean
  @Primary
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public BasicErrorController basicErrorController() {
    return new BasicErrorController();
  }

}

