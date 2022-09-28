# microservice-testing-tool [![Build and publish](https://github.com/mjaakko/microservice-testing-tool/actions/workflows/build-and-publish.yml/badge.svg)](https://github.com/mjaakko/microservice-testing-tool/actions/workflows/build-and-publish.yml) [![Latest version](https://img.shields.io/github/v/tag/mjaakko/microservice-testing-tool)](https://github.com/mjaakko/microservice-testing-tool/tags)

Microservice testing tool for doing system-level testing with applications using microservice architecture. Created for M.Sc. thesis at University of Helsinki.

## Requirements

The tool uses *Testcontainers* library to run the containers containing the microservices. Testcontainers uses *Docker* as its container engine, which means that Docker must be available on the environment that runs the tests.

## Usage

This microservice testing tool can be used either as a library to add tests to an existing repository or it can be used to build standalone JAR files that can run the tests in CI/CD pipelines, for example.

In both cases, the tool must be included as a dependency in the project which contains the test code. The tool is published with [GitHub packages](https://github.com/mjaakko/microservice-testing-tool/packages/1431155). Using GitHub Packages requires authentication with a token that needs to be configured to be used with [Maven](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-to-github-packages) or [Gradle](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#authenticating-to-github-packages).

### As a library

1. Include the package as a test dependency, for example with Gradle: `testImplementation 'xyz.malkki:microservice-testing-tool:1.0.0'`
2. Create a test class that includes the following function (example in Kotlin):
```kotlin
@TestFactory
fun `Test microservices`(): Collection<DynamicTest> {
    return TestSuiteRunner.generateDynamicTests()
}
```
3. Write test configuration and test steps

### Standalone

1. Include the package as a dependency, for example with Gradle:  `implementation 'xyz.malkki:microservice-testing-tool:1.0.0'`
2. Create a main function that includes the following:
```kotlin
MicroserviceTestExecutor.runMicroserviceTests()
```
3. Write test configuration and test steps
4. (optional) Create a runnable JAR file so that the tests can be run in CI/CD pipelines

### Writing test configuration

For complete example, see [transitdata-tests](https://github.com/HSLdevcom/transitdata-tests) repository.

1. Create `.yml` file to `microservices/` resource directory which lists available services
  * Only `id` and `container` fields are required in the configuration
```yaml
services:
  - id: mosquitto
    container: eclipse-mosquitto:1.6.3 #Docker image
    ports: #List of open ports
      - 1883
    volumes: #List of volumes used
      - type: RESOURCE #Either RESOURCE or FILESYSTEM
        hostPath: mosquitto.conf #Refers to either a resource or a file in the filesystem depending on the value of type
        containerPath: /mosquitto/config/mosquitto.conf #Path where the volume will be mounted on the container
    waitStrategy:
      type: LOG #Either LOG, PORT or HEALTHCHECK
      logMessage: ".*Opening ipv4 listen socket on port 1883.*" #Log message to wait for if LOG wait strategy is used
  - id: postgres
    container: postgres:14-alpine
    ports:
      - 5432
    environment: #Environment variables in the container
      POSTGRES_PASSWORD: test
  - id: pulsar
    container: apachepulsar/pulsar
    startupTimeout: 90 #Timeout in seconds to wait for the service to start
    waitStrategy:
      type: PORT #Waits until first of the ports accepts connections
    cmd: bin/pulsar standalone #Command that is run in the container
    ports:
      - 6650
-   dependencies: #List of services that must be started before this service
      - redis
```
2. Create `.yml` file to `steps/` resource directory which lists available test steps
  * `id` and `class` are required fields. It is also highly recommended to specify timeout with `timeout` to avoid tests getting stuck infinitely
```yaml
  - id: start-mqtt
    class: xyz.malkki.microservicetest.steps.StartMqttStepCode
    timeout: 15 #Timeout in seconds
  - id: stop-mqtt
    class: xyz.malkki.microservicetest.steps.StopMqttStepCode #Full class name. The class must implement xyz.malkki.microservicetest.testexecution.TestStepCode
    timeout: 15
  - id: create-database-table
    class: xyz.malkki.microservicetest.teststeps.ExecuteSqlTestStep
    timeout: 30
    parameters: #Parameters for the test if using ParametrizedTestStepCode
      connectionString: jdbc:postgresql://postgres:5432/?user=postgres&password=test
      sqlResource: create_db.sql
```
3. Write code for test steps. Test steps must implement `xyz.malkki.microservicetest.testexecution.TestStepCode` interface
4. Create `.yml` file to `testsuites/` resource directory which lists available tests
```yaml
test-suites:
  - id: mosquitto
    name: Test Mosquitto
    dependencies: #List IDs of the services that are needed for the test
      - mosquitto
    steps: #Steps that the test uses. Steps are executed in the order that they are listed
      - start-mqtt
      - stop-mqtt
```

#### Parametrized test steps

Parametrized test steps help reusing code for commong operations such as executing SQL files. Parametrized test steps can be created by implementing `xyz.malkki.microservicetest.testexecution.ParametrizedTestStepCode` interface. Parameters can be set in the test step YAML-file with the `parameters` object.

There are currently two parametrized test steps included with the tool: 
* `xyz.malkki.microservicetest.teststeps.ExecuteSqlTestStep`
  * Executes SQL code from resource
  * Parameters:
    * `connectionString`: connection string to the database. Hostname and port are automatically changed to use the hostname and the port of the container.
    * `sqlResource`: name of the resource, which contains the SQL code
* `xyz.malkki.microservicetest.teststeps.WaitTestStep`
  * Waits the specified amount of time
  * Parameters:
    * `millis`: Amount of milliseconds to wait