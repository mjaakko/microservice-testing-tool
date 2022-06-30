package xyz.malkki.microservicetest.testdefinition

import checkKey
import org.yaml.snakeyaml.Yaml
import xyz.malkki.microservicetest.domain.Microservice
import xyz.malkki.microservicetest.utils.enumValueOfSafe
import java.io.InputStream
import java.time.Duration

internal class MicroserviceConfigParser {
    private val yaml = Yaml()

    fun getServices(inputStream: InputStream): List<Microservice> {
        val services: List<Map<String, Any>> = yaml.load<Map<String, Any>>(inputStream)["services"] as List<Map<String, Any>>

        return services.map(::parseService).toList()
    }

    private fun parseService(serviceConfig: Map<String, Any>): Microservice {
        serviceConfig.checkKey("id")
        serviceConfig.checkKey("container")

        val exposedPorts = serviceConfig["ports"]?.let { (it as List<Any>).mapNotNull { port -> port.toString().toIntOrNull() } }.orEmpty()

        val startupTimeout = serviceConfig["startupTimeout"]?.let { it as Int }?.let { Duration.ofSeconds(it.toLong()) }

        val waitStrategyConfig = serviceConfig["waitStrategy"]?.let { it as Map<String, Any> }
        waitStrategyConfig?.checkKey("type")

        val waitStrategyType = enumValueOfSafe<Microservice.WaitStrategy.Type>(waitStrategyConfig?.get("type").toString())
        val waitStrategy = if (waitStrategyType == Microservice.WaitStrategy.Type.LOG) {
            waitStrategyConfig!!.checkKey("logMessage")

            Microservice.WaitStrategy(waitStrategyType, waitStrategyConfig["logMessage"]!!.toString())
        } else if (waitStrategyType != null) {
            Microservice.WaitStrategy(waitStrategyType, null)
        } else {
            null
        }

        val environment: Map<String, Any> = serviceConfig["environment"]?.let { it as Map<String, Any> } ?: emptyMap()

        val volumes: List<Microservice.Volume> = serviceConfig["volumes"]?.let { it as List<Map<String, Any>> }?.map(::parseVolume) ?: emptyList()

        val dependencies = serviceConfig["dependencies"]?.let { it as List<String> }.orEmpty()

        return Microservice(serviceConfig["id"]!!.toString(), serviceConfig["container"]!!.toString(), exposedPorts, serviceConfig["cmd"]?.toString(), startupTimeout, waitStrategy, environment, volumes, dependencies)
    }

    private fun parseVolume(volumeConfig: Map<String, Any>): Microservice.Volume {
        volumeConfig.checkKey("type")
        volumeConfig.checkKey("hostPath")
        volumeConfig.checkKey("containerPath")

        val volumeType = enumValueOfSafe<Microservice.Volume.Type>(volumeConfig["type"] as String)
        if (volumeType == null) {
            throw InvalidConfigurationException("Unknown volume type: ${volumeConfig["type"]} (allowed values: ${Microservice.Volume.Type.values().joinToString(", ")})")
        }

        return Microservice.Volume(volumeType!!,
            volumeConfig["hostPath"].toString(),
            volumeConfig["containerPath"].toString()
        )
    }
}