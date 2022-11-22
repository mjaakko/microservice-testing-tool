package xyz.malkki.microservicetest.testexecution

import org.testcontainers.containers.GenericContainer

interface TestStepCode : ParametrizedTestStepCode {
    /**
     * Executes the test step
     * @param containers Map of containers that have been defined as a dependency for the test step
     * @param updateState Function for updating objects in the test state
     * @param getState Function for getting objects from the test state
     */
    @Throws(Exception::class)
    fun execute(containers: Map<String, GenericContainer<*>>, updateState: (key: String, updater: (Any?) -> Any) -> Unit, getState: (key: String) -> Any?)

    override fun execute(
        containers: Map<String, GenericContainer<*>>,
        parameters: Map<String, Any>,
        updateState: (key: String, updater: (Any?) -> Any) -> Unit,
        getState: (key: String) -> Any?
    ) {
        execute(containers, updateState, getState)
    }
}