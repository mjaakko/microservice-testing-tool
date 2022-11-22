package xyz.malkki.microservicetest.teststeps

import mu.KotlinLogging
import org.testcontainers.containers.GenericContainer
import xyz.malkki.microservicetest.testexecution.ParametrizedTestStepCode

private val log = KotlinLogging.logger {}

class WaitTestStep : ParametrizedTestStepCode {
    override fun execute(
        containers: Map<String, GenericContainer<*>>,
        parameters: Map<String, Any>,
        updateState: (key: String, updater: (Any?) -> Any) -> Unit,
        getState: (key: String) -> Any?
    ) {
        val millis = parameters["millis"]?.let { if (it is Number) { it.toLong() } else { it.toString().toLongOrNull() } }

        if (millis == null) {
            log.warn { "Invalid value for 'millis'" }
        } else {
            Thread.sleep(millis)
        }
    }
}