package xyz.malkki.microservicetest.testdefinition

import checkKey
import org.yaml.snakeyaml.Yaml
import xyz.malkki.microservicetest.domain.TestSuite
import java.io.InputStream

class TestSuiteParser {
    private val yaml = Yaml()

    fun getTestSuites(inputStream: InputStream): List<TestSuite> {
        val testSuites = yaml.load<Map<String, Any>>(inputStream)["test-suites"] as List<Map<String, Any>>

        return testSuites.map(::parseTestSuite).toList()
    }

    private fun parseTestSuite(testSuite: Map<String, Any>): TestSuite {
        testSuite.checkKey("id")
        testSuite.checkKey("name")

        val dependencies = testSuite["dependencies"]?.let { it as List<String> }.orEmpty()
        val steps = testSuite["steps"]?.let { it as List<String> }.orEmpty()

        return TestSuite(testSuite["id"].toString(), testSuite["name"].toString(), dependencies, steps)
    }
}