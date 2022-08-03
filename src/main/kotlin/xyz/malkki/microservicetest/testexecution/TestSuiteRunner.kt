package xyz.malkki.microservicetest.testexecution

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.testcontainers.containers.ContainerLaunchException
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import xyz.malkki.microservicetest.domain.Microservice
import xyz.malkki.microservicetest.domain.TestStep
import xyz.malkki.microservicetest.domain.TestSuite
import xyz.malkki.microservicetest.testdefinition.*
import xyz.malkki.microservicetest.utils.DependencyGraph
import xyz.malkki.microservicetest.utils.stopSafely
import java.io.BufferedInputStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

private val logger = KotlinLogging.logger { }
object TestSuiteRunner {
    private const val MICROSERVICES_CONFIG_DIR = "microservices"
    private const val STEPS_CONFIG_DIR = "steps"
    private const val TESTSUITES_CONFIG_DIR = "testsuites"

    private val microservices: Map<String, Microservice>
    private val testSteps: Map<String, TestStep>
    private val testSuites: Map<String, TestSuite>

    init {
        microservices = readConfig(MICROSERVICES_CONFIG_DIR, MicroserviceConfigParser()).associateBy { it.id }

        testSteps = readConfig(STEPS_CONFIG_DIR, TestStepParser()).associateBy { it.id }

        testSuites = readConfig(TESTSUITES_CONFIG_DIR, TestSuiteParser()).associateBy { it.id }
    }

    private fun <T> readConfig(resourceDirectoryName: String, configParser: ConfigParser<List<T>>): List<T> {
        val configFiles = getFilesFromResourcesDir(resourceDirectoryName)
        logger.info { "${configFiles.size} configuration files found from $resourceDirectoryName/" }
        return configFiles.flatMap { path ->
            logger.debug { "Reading configuration from $path" }
            BufferedInputStream(Files.newInputStream(path)).use {
                try {
                    configParser.validateAndParse(it)
                } catch (ice: InvalidConfigurationException) {
                    logger.warn(ice) { "Configuration in $path was invalid, ignoring..." }
                    emptyList()
                }
            }
        }
    }

    fun getTestSuites(): Collection<String> = testSuites.keys

    fun generateDynamicTests(): Collection<DynamicTest> {
        return testSuites.entries.map { (_, testSuite) -> DynamicTest.dynamicTest(testSuite.name) { runTestSuite(testSuite.id) } }
    }

    /**
     * Runs a test suite with the specified id
     */
    fun runTestSuite(testSuiteId: String) {
        if (!testSuites.containsKey(testSuiteId)) {
            throw IllegalArgumentException("No test suite found with ID: $testSuiteId")
        }

        val testSuite = testSuites[testSuiteId]!!

        logger.info { "Executing test suite ${testSuite.id} (${testSuite.name})" }

        val network = Network.newNetwork()

        val containers = mutableMapOf<String, GenericContainer<*>>()

        try {
            val microservicesOrdered = getServicesInStartupOrder(testSuite)

            logger.info { "Microservices needed for test suite ${testSuite.id}: ${microservicesOrdered.joinToString(", ")}}" }

            for (microservice in microservicesOrdered) {
                if (!microservices.containsKey(microservice)) {
                    throw IllegalArgumentException("No microservice found with ID: $microservice")
                }

                val container = microservices[microservice]!!.createContainer()
                container.network = network
                try {
                    logger.info { "Starting container $microservice (${container.dockerImageName})" }

                    container.start()
                } catch (cle: ContainerLaunchException) {
                    logger.error(cle) {
                        "Failed to start container $microservice (${container.dockerImageName}), latest logs from container:\n${container.logs}"
                    }
                    throw cle
                }
                containers[microservice] = container
            }

            val steps = testSuite.steps.map { if (testSteps.containsKey(it)) { testSteps[it]!! } else { throw IllegalArgumentException("No test step found with ID: $it") } }

            val testStepExecutor = TestStepExecutor(containers)
            testStepExecutor.executeSteps(steps)
        } catch (e: Exception) {
            logger.error { "Failed to execute test suite ${testSuite.id}: ${e.message}" }
            throw e
        } finally {
            for (container in containers.values) {
                container.stopSafely()
            }

            network.close()
        }
    }

    /**
     * Sorts the services with topological sort based on their dependencies so that each service is started after their dependencies
     * @return Ordered list of service IDs
     */
    private fun getServicesInStartupOrder(testSuite: TestSuite): List<String> {
        val microservicesFiltered = microservices.filter { (key, _) -> key in testSuite.services }

        for (service in testSuite.services) {
            assertTrue(service in microservicesFiltered.keys, "Service $service was not found from test configuration")
        }

        val microserviceDependencyGraphBuilder = DependencyGraph.Builder()

        microservicesFiltered.values.forEach { microservice -> microserviceDependencyGraphBuilder.addDependencies(microservice.id, microservice.dependencies) }

        return microserviceDependencyGraphBuilder.build().asSortedList()
    }

    private fun getFilesFromResourcesDir(dirName: String): List<Path> {
        val uri = TestSuiteRunner::class.java.classLoader.getResource(dirName)?.toURI()

        if (uri == null) {
            logger.warn { "Resource directory \"$dirName\" does not exist" }
            return emptyList()
        }

        //TODO: test if this works inside a JAR file
        val dirPath = if (uri.scheme == "jar") {
            val filesystem = FileSystems.newFileSystem(uri, emptyMap<String, Any>())
            filesystem.use { it.getPath(dirName) }
        } else {
            Paths.get(uri)
        }

        return Files.walk(dirPath, 1).toList().filterNot { it == dirPath }
    }

}