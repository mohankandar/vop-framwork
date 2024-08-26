# What is this repository for?

To run spring boot application and spring cloud enabled services on the VOP Platform, it must adhere to various service patterns. This repository contains a suite of framework libraries, auto configurations, test libraries and parent POM that must be included as dependencies to enable the patterns.

For information regarding recommended development patterns for developing service applications, and the purpose and usage of capabilities that are provided by the VOP Framework, see the [vop-person README.md].

For information on framework release notes, -- TODO

# Prequisites
The vop-framework requires JDK 8 or 11 and Maven 3.6 or higher.

# Project Breakdown & Links

1. [vop-framework-reactor](link): This is the root reactor project (you are in that repo now). This project forms the aggregate of modules that make up the complete framework, and manages the Fortify scans.

2. [vop-framework-parentpom](vop-framework-parentpom/README.md): Parent POM for spring boot application and partial cloud enabled services. It provides a common Maven configuration and dependencies for the suite of projects, and dependency management for capabilities (e.g. database management).
	- Makes VOP Framework the parent for your projects.

3. [vop-framework-autoconfigure](vop-framework-autoconfigure/README.md): Shared auto-configuration for the services to enable the patterns for audit, cache, feign, rest,  swagger, service, etc.

4. [vop-framework-libraries](vop-framework-libraries/README.md): Common VOP capabilities for the services to implement consistent behavior.


5. [vop-framework-test-lib](vop-framework-test-lib/README.md): Test library framework to support functional testing for service applications.

# How to include VOP Framework libraries in your project

See the [vop-person README]