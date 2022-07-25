package xyz.malkki.microservicetest.testexecution

import org.testcontainers.containers.GenericContainer

interface TestStepCode {
    /**
     * Executes the test step
     * @param containers Map of containers that have been defined as a dependency for the test step
     * @param state Mutable map of test state
     */
    @Throws(Exception::class)
    fun execute(containers: Map<String, GenericContainer<*>>, updateState: (key: String, updater: (Any?) -> Any) -> Unit, getState: (key: String) -> Any?)
}