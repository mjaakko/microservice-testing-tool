package xyz.malkki.microservicetest.testexecution

import org.junit.jupiter.api.assertTimeout
import org.testcontainers.containers.GenericContainer
import xyz.malkki.microservicetest.domain.TestStep
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.createInstance

internal class TestStepExecutor(private val containers: Map<String, GenericContainer<*>>) {
    fun executeSteps(steps: List<TestStep>) {
        val state = ConcurrentHashMap<String, Any>()

        fun updateState(key: String, updater: (Any?) -> Any) {
            state.compute(key) { _, value -> updater(value) }
        }

        steps.forEach { step ->
            //TODO: check if class exists
            val clazz = Class.forName(step.className).kotlin

            val testStep = clazz.createInstance()
            if (testStep is TestStepCode) {
                val dependencies = containers.filterKeys { step.dependencies.contains(it) }

                if (step.timeout == null) {
                    testStep.execute(dependencies, ::updateState, state::get)
                } else {
                    assertTimeout(Duration.ofSeconds(step.timeout.toLong()), "Test step ${step.id} was not executed in ${step.timeout} seconds") {
                        testStep.execute(dependencies, ::updateState, state::get)
                    }
                }
            } else {
                //TODO: use different type of exception
                throw IllegalArgumentException("$clazz does not implement TestStep")
            }
        }
    }
}