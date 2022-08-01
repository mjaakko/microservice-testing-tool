package xyz.malkki.microservicetest.testexecution

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.assertTimeout
import org.testcontainers.containers.GenericContainer
import xyz.malkki.microservicetest.domain.TestStep
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

private val logger = KotlinLogging.logger {}

internal class TestStepExecutor(private val containers: Map<String, GenericContainer<*>>) {
    fun executeSteps(steps: List<TestStep>) {
        val state = ConcurrentHashMap<String, Any>()

        fun updateState(key: String, updater: (Any?) -> Any) {
            state.compute(key) { _, value -> updater(value) }
        }

        steps.forEach { step ->
            //TODO: check if class exists
            val clazz = getClassByName(step.className)

            assertNotNull(clazz, "No class found with name ${step.className}")

            val testStep = clazz!!.createInstance()
            if (testStep is ParametrizedTestStepCode) {
                val dependencies = containers.filterKeys { step.dependencies.contains(it) }

                logger.info { "Executing test step ${step.id} with parameters ${step.parameters}" }

                if (step.timeout == null) {
                    testStep.execute(dependencies, step.parameters, ::updateState, state::get)
                } else {
                    assertTimeout(Duration.ofSeconds(step.timeout.toLong()), "Test step ${step.id} was not executed in ${step.timeout} seconds") {
                        testStep.execute(dependencies, step.parameters, ::updateState, state::get)
                    }
                }
            } else {
                //TODO: use different type of exception
                throw IllegalArgumentException("$clazz does not implement ParametrizedTestStepCode")
            }
        }
    }

    private fun getClassByName(name: String): KClass<out Any>? {
        return try {
            Class.forName(name).kotlin
        } catch (cnfe: ClassNotFoundException) {
            null
        }
    }
}