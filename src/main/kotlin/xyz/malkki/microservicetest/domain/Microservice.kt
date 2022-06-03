package xyz.malkki.microservicetest.domain

import org.testcontainers.containers.GenericContainer

data class Microservice(val id: String,
                        val container: String,
                        val ports: List<Int>,
                        val cmd: String?,
                        val environment: Map<String, Any> = emptyMap(),
                        val dependencies: List<String> = emptyList()
) {
    fun createContainer(): GenericContainer<*> {
        val container = GenericContainer(container)
        container.networkAliases = listOf(id)
        container.exposedPorts = ports
        cmd?.let { container.setCommand(it) }

        container.env = environment.map { (key, value) -> "$key=$value" }

        return container
    }
}
