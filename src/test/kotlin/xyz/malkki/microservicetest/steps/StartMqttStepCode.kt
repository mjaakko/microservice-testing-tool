package xyz.malkki.microservicetest.steps

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.testcontainers.containers.GenericContainer
import xyz.malkki.microservicetest.testexecution.TestStepCode
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.concurrent.thread

class StartMqttStepCode : TestStepCode {
    override fun execute(
        containers: Map<String, GenericContainer<*>>,
        updateState: (key: String, updater: (Any?) -> Any) -> Unit,
        getState: (key: String) -> Any?
    ) {
        updateState("mqtt-messages") { Collections.synchronizedList(mutableListOf<Pair<String, MqttMessage>>()) }

        val mosquitto = containers["mosquitto"]!!

        val url = "tcp://${mosquitto.host}:${mosquitto.firstMappedPort}"

        val mqttClient = MqttAsyncClient(url, "mqtt-async-client")
        mqttClient.setCallback(object : MqttCallbackExtended {
            override fun connectionLost(cause: Throwable) {
                cause.printStackTrace()
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                updateState("mqtt-messages") {
                    (it as MutableList<Pair<String, MqttMessage>>).add(topic to message)
                    return@updateState it
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
            }

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                mqttClient.subscribe("#", 0)
            }
        })
        try {
            mqttClient.connect().waitForCompletion()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        updateState("mqtt-client") { mqttClient }

        thread(isDaemon = true) {
            Thread.sleep(5000)

            mqttClient.publish("test", MqttMessage("test".toByteArray(StandardCharsets.UTF_8)))
            mqttClient.publish("test", MqttMessage("test".toByteArray(StandardCharsets.UTF_8)))
            mqttClient.publish("test", MqttMessage("test".toByteArray(StandardCharsets.UTF_8)))
            mqttClient.publish("test", MqttMessage("test".toByteArray(StandardCharsets.UTF_8)))
        }
    }
}