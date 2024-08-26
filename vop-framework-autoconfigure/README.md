# What is this project for?

VOP Framework Autoconfigure Project is a suite of POM files that provides application services with starter dependencies for the VOP platform.

# Overview of the packages

The [`vop-framework-logback-starter.xml`] defines the Logback configuration _include_ for the VOP asynchronous appender `VOP_FRAMEWORK_ASYNC_CONSOLE_APPENDER` and the json formatting `VOP_FRAMEWORK_CONSOLE_LOG_ENCODER`. 

The framework-supplied appender can be referenced in service [`logback-spring.xml`] by including the resource:

```xml
<include resource="gov/va/vop/framework/starter/logger/vop-framework-logback-starter.xml" />
<appender-ref ref="VOP_FRAMEWORK_ASYNC_CONSOLE_APPENDER" />
```

## com.wynd.vop.framework.audit.autoconfigure:

Audit auto-configuration that provides the serializer bean and enables async execution.

```java
@Configuration
@EnableAsync
public class VopAuditAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AuditLogSerializer auditLogSerializer() {
        return new AuditLogSerializer();
    }
}
```

## com.wynd.vop.framework.cache.autoconfigure:

Redis cache auto-configuration that provides property-driven beans to set up the Redis connection, start the Redis Embedded Server (if in a spring profile that requires it), and expose a JMX bean for developers to clear the cache with.

Caches are configured for a specific naming scheme of: `cacheName_ProjectName_MavenVersion`; the actual properties entry might look something like _`appName`_`Service\_@project.name@\_@project.version@`

Redis attributes are configured in the Service Application's application YAML file under `spring.redis` properties.

Cache-specific attributes for the application are configured in the application YAML under the `vop.framework.cache.**` property. See properties and comments under the `spring:redis:**` and the `vop.framework:cache:**` sections in [vop-person.yml]

Any properties that do not appear in the appropriate hierarchy will be silently ignored, so default values, or nulls will be substituted for properties that were believed to be configured.

Auto-configuration is declared as below. Beans created in this class refer to the other classes found in the package.

```java
@Configuration
@AutoConfigureAfter(CacheAutoConfiguration.class)
@EnableCaching
/* @Import to participate in the auto configure bootstrap process */
@Import({ VopCachesConfig.class, VopJedisConnectionConfig.class })
@ConditionalOnProperty(name = CacheAutoConfiguration.CONDITIONAL_SPRING_REDIS,
    havingValue = VopCacheAutoConfiguration.CACHE_SERVER_TYPE)
@EnableMBeanExport(defaultDomain = "com.wynd.vop", registration = RegistrationPolicy.FAIL_ON_EXISTING)
public class VopCacheAutoConfiguration extends CachingConfigurerSupport {
    /** Domain under which JMX beans are exposed */
    public static final String JMX_DOMAIN = "com.wynd.vop";
    /** ConditionalOnProperty property name */
    public static final String CONDITIONAL_SPRING_REDIS = "spring.cache.type";
    /** The cache server type */
    public static final String CACHE_SERVER_TYPE = "redis";
    
    /** Refresh order for JedisConnectionFactory must be lower than for CacheManager */
    static final int REFRESH_ORDER_CONNECTION_FACTORY = 1;
    /** Refresh order for CacheManager must be higher than for JedisConnectionFactory */
    static final int REFRESH_ORDER_CACHES = 10;
    ...
}
```


## com.wynd.vop.framework.feign.autoconfigure:

Feign client auto-configuration creates some beans to support RESTful client calls through the feign library:

- `FeignCustomErrorDecoder` has been created to interrogate and modify the Exception being propagated.

```java
@Configuration
public class VopFeignAutoConfiguration {
    ...
}
```

## com.wynd.vop.framework.hystrix.autoconfigure:

Hystrix auto-configuration sets up Hystrix with the THREAD strategy. The configuration copies RequestAttributes from ThreadLocal to Hystrix threads in the `RequestAttributeAwareCallableWrapper` bean. This is done to make sure the necessary request information is available on the Hystrix thread.

```java
@Configuration
@ConditionalOnProperty(value = "hystrix.wrappers.enabled", matchIfMissing = true)
public class HystrixContextAutoConfiguration {
    ...
}
```

## com.wynd.vop.framework.rest.autoconfigure:

REST auto-configuration creates beans to enable a number of capabilities related to RESTful clients and providers.

- `restClientTemplate` is a customized bean that acts as an alternative to using the Feign client.
- `VopRestGlobalExceptionHandler` is configured to handle exceptions from server to client and modify them (if needed) for appropriate communication to the consumer.
- `ProviderHttpAspect` audits requests and responses passing throught the provider.
- `RestProviderTimerAspect` logs performance data using `PerformanceLoggingAspect`.

```java
@Configuration
public class VopRestAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ProviderHttpAspect providerHttpAspect() {
        return new ProviderHttpAspect();
    }
    @Bean
    @ConditionalOnMissingBean
    public VopRestGlobalExceptionHandler vopRestGlobalExceptionHandler() {
        return new VopRestGlobalExceptionHandler();
    }
    @Bean
    @ConditionalOnMissingBean
    public RestProviderTimerAspect restProviderTimerAspect() {
        return new RestProviderTimerAspect();
    }
    @Bean
    public HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory() {
        ...
    }
    @Bean
    @ConditionalOnMissingBean
    public RestClientTemplate restClientTemplate() {
        ...
    }
    @Bean
    @ConditionalOnMissingBean
    public TokenClientHttpRequestInterceptor tokenClientHttpRequestInterceptor() {
        return new TokenClientHttpRequestInterceptor();
    }
}
```


## com.wynd.vop.framework.swagger.autoconfigure:

Swagger starter and autoconfiguration to generate and configure swagger documentation:

```java
@Configuration
@EnableWebMvc
@ConditionalOnProperty(prefix = "vop.framework.swagger", name = "enabled", matchIfMissing = true)
public class VopSwaggerAutoConfiguration implements WebMvcConfigurer {
    ...
}
```

## com.wynd.vop.framework.validator.autoconfigure:

Validator auto-configuration enables the standard JSR 303 validator (useful for model validation in REST controllers, for example). `LocalValidatorFactoryBean` is created to allow further customization of the validator's behaviour.

```java
@Configuration
@AutoConfigureBefore(MessageSourceAutoConfiguration.class)
public class VopValidatorAutoConfiguration {
    ...
}
```


# How to add dependencies in your maven pom.xml?

Standard maven dependency configuration.

```xml
<dependency>
    <groupId>com.wynd.vop.framework</groupId>
    <artifactId>vop-framework-autoconfigure</artifactId>
    <version><latest version></version>
</dependency>
```