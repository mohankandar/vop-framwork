# VOP Application Framework

## Overview

This is the parent POM for the VOP Application Framework, designed to facilitate the development of Spring Boot and Spring Cloud-based microservices. The framework provides a standard set of dependencies, plugins, and configurations to streamline the development process and ensure consistency across projects.

## Features

- **Spring Boot Integration**: Leverages the Spring Boot framework for rapid application development.
- **Spring Cloud**: Supports cloud-native development with Spring Cloud components.
- **Testing and Quality**:
    - Integrated with JUnit and Mockito for unit testing.
    - Supports code coverage with JaCoCo.
    - Configured for static code analysis with SonarQube.
- **Logging**: Configured with Logback and Logstash for structured logging.
- **OpenAPI/Swagger**: Supports API documentation generation with OpenAPI Generator and Swagger UI.
- **Database Support**:
    - Integrated with H2 for in-memory databases.
    - Supports PostgreSQL and Oracle databases with appropriate JDBC drivers.
    - Database migrations managed with Liquibase.
- **Metrics and Monitoring**: Integrated with Micrometer for application metrics and supports Prometheus for metrics export.
- **Security**: Includes dependencies for Spring Security and JWT-based authentication.
- **Dependency Management**: Centralized management of commonly used dependencies to ensure version consistency.

## Usage

### Building the Project

To build the project, run the following Maven command:

```bash
mvn clean install
