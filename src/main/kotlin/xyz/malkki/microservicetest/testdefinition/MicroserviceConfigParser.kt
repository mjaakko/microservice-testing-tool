package xyz.malkki.microservicetest.testdefinition

import xyz.malkki.microservicetest.domain.Microservice
import xyz.malkki.microservicetest.utils.enumValueOfSafe
import java.time.Duration

@Suppress("UNCHECKED_CAST")
internal class MicroserviceConfigParser : ConfigParser<List<Microservice>>() {

    private fun parseService(serviceConfig: Map<String, Any>): Microservice {
        val exposedPorts = serviceConfig["ports"]?.let { (it as List<Any>).mapNotNull { port -> port.toString().toIntOrNull() } }.orEmpty()

        val startupTimeout = serviceConfig["startupTimeout"]?.let { it as Int }?.let { Duration.ofSeconds(it.toLong()) }

        val waitStrategyConfig = serviceConfig["waitStrategy"]?.let { it as Map<String, Any> }

        val waitStrategyType = enumValueOfSafe<Microservice.WaitStrategy.Type>(waitStrategyConfig?.get("type").toString())
        val waitStrategy = if (waitStrategyType == Microservice.WaitStrategy.Type.LOG) {
            Microservice.WaitStrategy(waitStrategyType, waitStrategyConfig!!["logMessage"]!!.toString())
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
        return Microservice.Volume(
            enumValueOfSafe<Microservice.Volume.Type>(volumeConfig["type"].toString())!!,
            volumeConfig["hostPath"].toString(),
            volumeConfig["containerPath"].toString()
        )
    }

    override fun validateConfig(config: Map<String, Any>): Pair<Boolean, String?> {
        if (!config.containsKey("services")) {
            return false to "Missing key 'services'"
        }
        if (config["services"] !is List<*>) {
            return false to "Value for 'services' must be a list"
        }

        val services: List<Map<String, Any>> = config["services"] as List<Map<String, Any>>
        services.forEach {
            val (valid, message) = validateService(it)

            if (!valid) {
                return valid to message
            }
        }

        return true to null
    }

    private fun validateService(serviceConfig: Map<String, Any>): Pair<Boolean, String?> {
        if (!serviceConfig.containsKey("id")) {
            return false to "Missing key 'id' from service"
        }
        if (!serviceConfig.containsKey("container")) {
            return false to "Missing key 'id' from service"
        }
        if (serviceConfig.containsKey("ports")) {
            if (serviceConfig["ports"] !is List<*>) {
                return false to "Value for 'ports' must be a list"
            }
            val ports = serviceConfig["ports"] as List<Any>
            ports.forEach {
                if (it !is Int) {
                    return false to "Ports must be integers (was: ${it})"
                }
            }
        }
        if (serviceConfig.containsKey("startupTimeout") && serviceConfig["startupTimeout"] !is Int) {
            return false to "Value for 'startupTimeout' must be an integer (was: ${serviceConfig["startupTimeout"]})"
        }
        if (serviceConfig.containsKey("waitStrategy")) {
            if (serviceConfig["waitStrategy"] !is Map<*, *>) {
                return false to "Value for 'waitStrategy' must be an object"
            }

            val waitStrategy = serviceConfig["waitStrategy"] as Map<String, Any>
            if (!waitStrategy.containsKey("type")) {
                return false to "Missing key 'type' from wait strategy"
            }
            if (waitStrategy["type"] == "LOG" && !waitStrategy.containsKey("logMessage")) {
                return false to "Missing key 'logMessage' from wait strategy"
            }
        }
        if (serviceConfig.containsKey("environment") && serviceConfig["environment"] !is Map<*, *>) {
            return false to "Value for 'environment' must be an object"
        }
        if (serviceConfig.containsKey("volumes")) {
            if (serviceConfig["volumes"] !is List<*>) {
                return false to "Value for 'volumes' must be a list"
            }
            val volumes = serviceConfig["volumes"] as List<Map<String, Any>>
            volumes.forEach {
                val (valid, message) = validateVolume(it)
                if (!valid) {
                    return valid to message
                }
            }
        }
        if (serviceConfig.containsKey("dependencies") && serviceConfig["dependencies"] !is List<*>) {
            return false to "Value for 'dependencies' must be a list"
        }
        return true to null
    }

    private fun validateVolume(volumeConfig: Map<String, Any>): Pair<Boolean, String?> {
        if (!volumeConfig.containsKey("type")) {
            return false to "Missing key 'type' from volume"
        }
        if (!volumeConfig.containsKey("hostPath")) {
            return false to "Missing key 'hostPath' from volume"
        }
        if (!volumeConfig.containsKey("containerPath")) {
            return false to "Missing key 'containerPath' from volume"
        }
        if (enumValueOfSafe<Microservice.Volume.Type>(volumeConfig["type"].toString()) == null) {
            return false to "Value for 'type' must be one of: ${Microservice.Volume.Type.values().joinToString(", ")}"
        }
        return true to null
    }

    override fun parseConfig(config: Map<String, Any>): List<Microservice> {
        return (config["services"] as List<Map<String, Any>>).map(::parseService)
    }
}