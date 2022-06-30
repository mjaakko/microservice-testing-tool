# microservice-testing-tool [![Build and publish](https://github.com/mjaakko/microservice-testing-tool/actions/workflows/build-and-publish.yml/badge.svg)](https://github.com/mjaakko/microservice-testing-tool/actions/workflows/build-and-publish.yml) [![Latest version](https://img.shields.io/github/v/tag/mjaakko/microservice-testing-tool)](https://github.com/mjaakko/microservice-testing-tool/tags)

Microservice testing tool (TODO: find a better name) for doing system-level testing with applications using microservice architecture. Created for M.Sc. thesis at University of Helsinki.

## Requirements

The tool uses Testcontainers library to run the containers containing the microservices. Testcontainers uses Docker as its container engine, which means that Docker must be available on the environment that runs the tests.

## Usage

This microservice testing tool can be used either as a library to add tests to an existing repository or it can be used to build standalone JAR files that can run the tests in CI/CD pipelines, for example.

In both cases, the tool must be included as a dependency in the project which contains the test code. The tool is published with [GitHub packages](https://github.com/mjaakko/microservice-testing-tool/packages/1431155). Using GitHub Packages requires authentication with a token that needs to be configured to be used with [Maven](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-to-github-packages) or [Gradle](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#authenticating-to-github-packages).

### As a library

1. Include the package as a test dependency, for example with Gradle: `testImplementation 'xyz.malkki:microservice-testing-tool:0.2.1'`
2. Create a test class that includes the following function (example in Kotlin):
```kotlin
@TestFactory
fun `Test microservices`(): Collection<DynamicTest> {
    return TestSuiteRunner.generateDynamicTests()
}
```
3. Write test configuration and test steps

### Standalone

1. Include the package as a dependency, for example with Gradle:  `implementation 'xyz.malkki:microservice-testing-tool:0.2.1'`
2. Create a main function that includes the following:
```kotlin
MicroserviceTestExecutor.runMicroserviceTests()
```
3. Write test configuration and test steps
4. (optional) Create a runnable JAR file so that the tests can be run in CI/CD pipelines

### Writing test configuration

TODO
