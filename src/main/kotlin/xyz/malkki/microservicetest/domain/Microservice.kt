package xyz.malkki.microservicetest.domain

import org.testcontainers.containers.GenericContainer

data class Microservice(val id: String, val container: String, val ports: List<Int>, val cmd: String?) {
    fun createContainer(): GenericContainer<*> {
        val container = GenericContainer(container)
        container.exposedPorts = ports
        cmd?.let { container.setCommand(it) }

        return container
    }
}
