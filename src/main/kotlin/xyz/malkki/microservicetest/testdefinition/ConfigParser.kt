package xyz.malkki.microservicetest.testdefinition

import org.yaml.snakeyaml.Yaml
import java.io.InputStream

internal abstract class ConfigParser<T> {
    companion object {
        private val yaml = Yaml()
    }

    private fun readRawConfig(inputStream: InputStream): Map<String, Any> {
        return yaml.load(inputStream)
    }

    fun validateAndParse(inputStream: InputStream): T {
        val rawConfig = readRawConfig(inputStream)

        val (valid, message) = validateConfig(rawConfig)
        if (!valid) {
            throw InvalidConfigurationException(message)
        }

        return parseConfig(rawConfig)
    }

    abstract fun validateConfig(config: Map<String, Any>): Pair<Boolean, String?>

    abstract fun parseConfig(config: Map<String, Any>): T
}