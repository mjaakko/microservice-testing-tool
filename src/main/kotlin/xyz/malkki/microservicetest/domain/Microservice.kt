package xyz.malkki.microservicetest.domain

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.time.Duration

internal data class Microservice(val id: String,
                        val container: String,
                        val ports: List<Int>,
                        val cmd: String?,
                        val startupTimeout: Duration?,
                        val waitStrategy: WaitStrategy?,
                        val environment: Map<String, Any> = emptyMap(),
                        val dependencies: List<String> = emptyList()
) {
    fun createContainer(): GenericContainer<*> {
        val container = if (startupTimeout != null) {
            GenericContainer(container).withStartupTimeout(startupTimeout)
        } else {
            GenericContainer(container)
        }

        if (waitStrategy != null) {
            container.setWaitStrategy(when (waitStrategy.type) {
                WaitStrategy.Type.PORT -> Wait.forListeningPort()
                WaitStrategy.Type.HEALTHCHECK -> Wait.forHealthcheck()
                WaitStrategy.Type.LOG -> Wait.forLogMessage(waitStrategy.logMessage!!, 1)
            })
        }

        container.networkAliases = listOf(id)
        container.exposedPorts = ports
        cmd?.let { container.setCommand(it) }

        container.env = environment.map { (key, value) -> "$key=$value" }

        return container
    }

    data class WaitStrategy(val type: Type, val logMessage: String?) {
        enum class Type {
            PORT, LOG, HEALTHCHECK;

            companion object {
                fun valueOfSafe(value: String): Type? {
                    return try {
                        Type.valueOf(value)
                    } catch (iae: IllegalArgumentException) {
                        null
                    }
                }
            }
        }
    }
}
