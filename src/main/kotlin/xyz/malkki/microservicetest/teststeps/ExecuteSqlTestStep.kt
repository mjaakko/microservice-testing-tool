package xyz.malkki.microservicetest.teststeps

import mu.KotlinLogging
import org.testcontainers.containers.GenericContainer
import xyz.malkki.microservicetest.testexecution.ParametrizedTestStepCode
import java.nio.charset.StandardCharsets
import java.sql.DriverManager

private val log = KotlinLogging.logger {}

private val HOST_AND_PORT_REGEX = Regex("\\w+:\\d+")

class ExecuteSqlTestStep : ParametrizedTestStepCode {
    override fun execute(
        containers: Map<String, GenericContainer<*>>,
        parameters: Map<String, Any>,
        updateState: (key: String, updater: (Any?) -> Any) -> Unit,
        getState: (key: String) -> Any?
    ) {
        val connectionString = parameters["connectionString"]!!.toString()

        val hostAndPort = HOST_AND_PORT_REGEX.find(connectionString)?.value?.split(':', limit = 2)

        val connectionStringModified = if (hostAndPort == null || hostAndPort.size < 2 || hostAndPort[1].toIntOrNull() == null) {
            connectionString
        } else {
            val (host, port) = hostAndPort

            val container = containers[host]
            if (container == null) {
                connectionString
            } else {
                val mappedHost = container.host
                val mappedPort = container.getMappedPort(port.toInt())

                log.info { "Mapping host and port in connection string to container ($host:$port -> $mappedHost:$mappedPort)" }

                HOST_AND_PORT_REGEX.replace(connectionString, "$mappedHost:$mappedPort")
            }
        }

        log.info { "Using connection string: $connectionStringModified" }

        val sqlResource = parameters["sqlResource"].toString()
        val sql = ExecuteSqlTestStep::class.java.classLoader.getResourceAsStream(sqlResource).use { it.readAllBytes().toString(StandardCharsets.UTF_8) }

        DriverManager.getConnection(connectionStringModified).use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.execute()
            }
        }
    }
}