package xyz.malkki.microservicetest.domain

import com.github.dockerjava.api.model.Volume
import org.testcontainers.containers.BindMode
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
                                 val volumes: List<Volume> = emptyList(),
                                 val dependencies: List<String> = emptyList()
) {
    fun createContainer(): GenericContainer<*> {
        var container = GenericContainer(container)

        if (startupTimeout != null) {
            container = container.withStartupTimeout(startupTimeout)
        }

        if (waitStrategy != null) {
            container.setWaitStrategy(when (waitStrategy.type) {
                WaitStrategy.Type.PORT -> Wait.forListeningPort()
                WaitStrategy.Type.HEALTHCHECK -> Wait.forHealthcheck()
                WaitStrategy.Type.LOG -> Wait.forLogMessage(waitStrategy.logMessage!!, 1)
            })
        }

        volumes.forEach { volume ->
            container = when (volume.type) {
                Volume.Type.FILESYSTEM -> container.withFileSystemBind(volume.hostPath, volume.containerPath, BindMode.READ_WRITE)
                Volume.Type.RESOURCE -> container.withClasspathResourceMapping(volume.hostPath, volume.containerPath, BindMode.READ_ONLY)
            }
        }

        container.networkAliases = listOf(id)
        container.exposedPorts = ports
        cmd?.let { container.setCommand(it) }

        container.env = environment.map { (key, value) -> "$key=$value" }

        return container
    }

    data class WaitStrategy(val type: Type, val logMessage: String?) {
        enum class Type {
            PORT, LOG, HEALTHCHECK
        }
    }

    data class Volume(val type: Type, val hostPath: String, val containerPath: String) {
        enum class Type {
            FILESYSTEM, RESOURCE
        }
    }
}
