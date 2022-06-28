package xyz.malkki.microservicetest.steps

import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.junit.jupiter.api.Assertions.assertTrue
import org.testcontainers.containers.GenericContainer
import xyz.malkki.microservicetest.testexecution.TestStepCode

class StopMqttStepCode : TestStepCode {
    override fun execute(
        containers: Map<String, GenericContainer<*>>,
        updateState: (key: String, updater: (Any?) -> Any) -> Unit,
        getState: (key: String) -> Any?
    ) {
        Thread.sleep(10000)

        val mqttClient = (getState("mqtt-client") as MqttAsyncClient)
        mqttClient.disconnectForcibly(1000, 1000)
        mqttClient.close(true)

        assertTrue((getState("mqtt-messages") as List<*>).isNotEmpty())
    }
}