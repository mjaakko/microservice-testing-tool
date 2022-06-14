package xyz.malkki.microservicetest.steps

import org.testcontainers.containers.GenericContainer
import redis.clients.jedis.Jedis
import xyz.malkki.microservicetest.testexecution.TestStepCode

class SetRedisValueStepCode : TestStepCode {
    override fun execute(containers: Map<String, GenericContainer<*>>, updateState: (key: String, updater: (Any?) -> Any) -> Unit, getState: (key: String) -> Any?) {
        val redis = containers["redis"]!!

        val jedis = Jedis(redis.host, redis.firstMappedPort)
        jedis.use {
            jedis.connect()

            jedis.set("test", "test")
        }
    }
}