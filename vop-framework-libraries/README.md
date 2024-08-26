This page documents the purpose and capabilities of _VOP Framework Libraries_ for the services.

# What is this library project for?

This project contains interfaces, annotations and classes consumed by the application services for various functionality:

- Marker interfaces for transfer objects to support common identification and behavior
- Rest Provider Message classes, RestTemplate
- Audit and Performance Logging aspects
- Utility ops for logging and handling exceptions
- Root classes for checked and runtime exceptions
- WebService client config
- API Gateway Kong Configuration for JWT Consumer 
- Service Domain Message classes, timer and validation aspects

# VOP Framework principles

VOP Framework aims to:

- free developers from many of the complexities of dealing with the underlying platform,
- enable centralized application configuration,
- enable developers to focus more on business requirements and less on boilerplate code,
- encourage developers to use good coding practices and patterns that are effective and efficient,
- encourage developers to write code that presents a common "look and feel" across projects,
- enable developers to produce reliable code that takes less time to develop and test.

# How to add the Framework dependency

Add the dependency in the application project's POM file.

```xml
<dependency>
    <groupId>com.wynd.vop.framework</groupId>
    <artifactId>vop-framework-libraries</artifactId>
    <version><!-- add the appropriate version --></version>
</dependency>
```


# Log Masking

Logback is configured in [`vop-framework-logback-starter.xml`] As the app starts up, logback's `ContextInitializer.configureByResource(..)` method reads the configured `VopMaskingMessageProvider` encoder provider. Logback invokes this custom provider by convention: the tag names and values within the `<provider>` xml declaration are used to infer java class names and properties used by the provider.

The framework uses masking rules to provide default masking for the `VOP_FRAMEWORK_ASYNC_CONSOLE_APPENDER`. See the [_Logger_](#logger) sequence diagram below.

Additional log masking definitions can be declared within services with the `VopMaskingFilter` class. This class can be referenced to declare masking in a logback filter.

If declarative masking in logback config is not sufficient for specific data, developers can manually mask data with methods from `MaskUtils`.
