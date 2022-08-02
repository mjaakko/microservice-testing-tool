package xyz.malkki.microservicetest.testdefinition

import xyz.malkki.microservicetest.domain.TestSuite

@Suppress("UNCHECKED_CAST")
internal class TestSuiteParser : ConfigParser<List<TestSuite>>() {
    private fun parseTestSuite(testSuite: Map<String, Any>): TestSuite {
        val dependencies = testSuite["dependencies"]?.let { it as List<String> }.orEmpty()
        val steps = testSuite["steps"]?.let { it as List<String> }.orEmpty()

        return TestSuite(testSuite["id"].toString(), testSuite["name"].toString(), dependencies, steps)
    }

    override fun validateConfig(config: Map<String, Any>): Pair<Boolean, String?> {
        if (!config.containsKey("test-suites")) {
            return false to "Missing key 'test-suites'"
        }
        val testSuites: List<Map<String, Any>> = config["test-suites"] as List<Map<String, Any>>
        testSuites.forEach {
            val (valid, message) = validateTestSuite(it)
            if (!valid) {
                return valid to message
            }
        }
        return true to null
    }

    private fun validateTestSuite(testSuiteConfig: Map<String, Any>): Pair<Boolean, String?> {
        if (!testSuiteConfig.containsKey("id")) {
            return false to "Missing key 'id' from test suite"
        }
        if (!testSuiteConfig.containsKey("name")) {
            return false to "Missing key 'name' from test suite"
        }
        if (!testSuiteConfig.containsKey("dependencies")) {
            return false to "Missing key 'dependencies' from test suite"
        }
        if (testSuiteConfig["dependencies"] !is List<*>) {
            return false to "Value for 'dependencies' must be a list"
        }
        if (!testSuiteConfig.containsKey("steps")) {
            return false to "Missing key 'steps' from test suite"
        }
        if (testSuiteConfig["steps"] !is List<*>) {
            return false to "Value for 'steps' must be a list"
        }
        return true to null
    }

    override fun parseConfig(config: Map<String, Any>): List<TestSuite> {
        val testSuites = config["test-suites"] as List<Map<String, Any>>

        return testSuites.map(::parseTestSuite).toList()
    }
}