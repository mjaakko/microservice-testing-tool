package xyz.malkki.microservicetest.testdefinition

import xyz.malkki.microservicetest.domain.TestStep

@Suppress("UNCHECKED_CAST")
internal class TestStepParser : ConfigParser<List<TestStep>>() {
    private fun parseStep(step: Map<String, Any>): TestStep {
        val parameters = step["parameters"]?.let { it as Map<String, Any> } ?: emptyMap()

        return TestStep(step["id"]!!.toString(), step["class"]!!.toString(), parameters, step["timeout"]?.toString()?.toIntOrNull())
    }

    override fun validateConfig(config: Map<String, Any>): Pair<Boolean, String?> {
        if (!config.containsKey("steps")) {
            return false to "Missing key 'steps'"
        }
        if (config["steps"] !is List<*>) {
            return false to "Value for 'steps' must be a list"
        }
        val steps: List<Map<String, Any>> = config["steps"] as List<Map<String, Any>>
        steps.forEach {
            val (valid, message) = validateStep(it)
            if (!valid) {
                return valid to message
            }
        }
        return true to null
    }

    private fun validateStep(stepConfig: Map<String, Any>): Pair<Boolean, String?> {
        if (!stepConfig.containsKey("id")) {
            return false to "Missing key 'id' from step"
        }
        if (!stepConfig.containsKey("class")) {
            return false to "Missing key 'class' from step"
        }
        if (stepConfig.containsKey("parameters") && stepConfig["parameters"] !is Map<*, *>) {
            return false to "Value for 'parameters' must be an object"
        }
        if (stepConfig.containsKey("timeout") && stepConfig["timeout"] !is Int) {
            return false to "Value for 'timeout' must be an integer (was: ${stepConfig["timeout"]}"
        }
        return true to null
    }

    override fun parseConfig(config: Map<String, Any>): List<TestStep> {
        val steps = config["steps"] as List<Map<String, Any>>

        return steps.map(::parseStep).toList()
    }
}