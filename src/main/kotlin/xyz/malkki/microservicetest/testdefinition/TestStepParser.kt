package xyz.malkki.microservicetest.testdefinition

import checkKey
import org.yaml.snakeyaml.Yaml
import xyz.malkki.microservicetest.domain.TestStep
import java.io.InputStream

internal class TestStepParser {
    private val yaml = Yaml()

    fun getTestSteps(inputStream: InputStream): List<TestStep> {
        val steps = yaml.load<Map<String, Any>>(inputStream)["steps"] as List<Map<String, Any>>

        return steps.map(::parseStep).toList()
    }

    private fun parseStep(step: Map<String, Any>): TestStep {
        step.checkKey("id")
        step.checkKey("class")

        val dependencies = step["dependencies"]?.let { it as List<String> }.orEmpty().toSet()

        return TestStep(step["id"]!!.toString(), step["class"]!!.toString(), dependencies, step["timeout"]?.toString()?.toIntOrNull())
    }
}