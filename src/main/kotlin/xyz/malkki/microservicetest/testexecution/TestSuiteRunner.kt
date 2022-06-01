package xyz.malkki.microservicetest.testexecution

import org.junit.jupiter.api.DynamicTest
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import xyz.malkki.microservicetest.domain.Microservice
import xyz.malkki.microservicetest.domain.TestStep
import xyz.malkki.microservicetest.domain.TestSuite
import xyz.malkki.microservicetest.testdefinition.MicroserviceConfigParser
import xyz.malkki.microservicetest.testdefinition.TestStepParser
import xyz.malkki.microservicetest.testdefinition.TestSuiteParser
import xyz.malkki.microservicetest.utils.DependencyGraph
import xyz.malkki.microservicetest.utils.stopSafely
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

object TestSuiteRunner {
    private val microservices: Map<String, Microservice>
    private val testSteps: Map<String, TestStep>
    private val testSuites: Map<String, TestSuite>

    init {
        val microserviceConfigParser = MicroserviceConfigParser()
        microservices = getFilesFromResourcesDir("microservices").flatMap {
            Files.newInputStream(it).use { microserviceConfigParser.getServices(it) }
        }.associateBy { it.id }

        val testStepParser = TestStepParser()
        testSteps = getFilesFromResourcesDir("steps").flatMap {
            Files.newInputStream(it).use { testStepParser.getTestSteps(it) }
        }.associateBy { it.id }

        val testSuiteParser = TestSuiteParser()
        testSuites = getFilesFromResourcesDir("testsuites").flatMap {
            Files.newInputStream(it).use { testSuiteParser.getTestSuites(it) }
        }.associateBy { it.id }
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

        val network = Network.newNetwork()

        val containers = mutableMapOf<String, GenericContainer<*>>()

        try {
            for (microservice in getServicesInStartupOrder(testSuite)) {
                if (!microservices.containsKey(microservice)) {
                    throw IllegalArgumentException("No microservice found with ID: $microservice")
                }

                val container = microservices[microservice]!!.createContainer()
                container.network = network
                container.start()
                containers[microservice] = container
            }

            val steps = testSuite.steps.map { if (testSteps.containsKey(it)) { testSteps[it]!! } else { throw IllegalArgumentException("No test step found with ID: $it") } }

            val testStepExecutor = TestStepExecutor(containers)
            testStepExecutor.executeSteps(steps)
        } catch (e: Exception) {
            println("Failed to execute test suite ${testSuite.id}: ${e.message}")
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
        val microserviceDependencyGraphBuilder = DependencyGraph.Builder()

        microservicesFiltered.values.forEach { microservice -> microserviceDependencyGraphBuilder.addDependencies(microservice.id, microservice.dependencies) }

        return microserviceDependencyGraphBuilder.build().asSortedList()
    }

    private fun getFilesFromResourcesDir(dirName: String): List<Path> {
        val uri = TestSuiteRunner::class.java.classLoader.getResource(dirName)?.toURI()

        if (uri == null) {
            println("Resource directory \"$dirName\" does not exist")
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