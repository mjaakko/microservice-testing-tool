package xyz.malkki.microservicetest.testdefinition

import checkKey
import org.yaml.snakeyaml.Yaml
import xyz.malkki.microservicetest.domain.Microservice
import java.io.InputStream

class MicroserviceConfigParser {
    private val yaml = Yaml()

    fun getServices(inputStream: InputStream): List<Microservice> {
        val services: List<Map<String, Any>> = yaml.load<Map<String, Any>>(inputStream)["services"] as List<Map<String, Any>>

        return services.map(::parseService).toList()
    }

    private fun parseService(serviceConfig: Map<String, Any>): Microservice {
        serviceConfig.checkKey("id")
        serviceConfig.checkKey("container")

        val exposedPorts = serviceConfig["ports"]?.let { (it as List<Any>).mapNotNull { port -> port.toString().toIntOrNull() } }.orEmpty()

        val environment: Map<String, Any> = serviceConfig["environment"]?.let { it as Map<String, Any> } ?: emptyMap()

        val dependencies = serviceConfig["dependencies"]?.let { it as List<String> }.orEmpty()

        return Microservice(serviceConfig["id"]!!.toString(), serviceConfig["container"]!!.toString(), exposedPorts, serviceConfig["cmd"]?.toString(), environment, dependencies)
    }
}