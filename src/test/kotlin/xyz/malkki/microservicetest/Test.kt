package xyz.malkki.microservicetest

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertTimeout
import java.time.Duration

class Test {
    @org.junit.jupiter.api.Test
    fun `Test`() {
        assertTimeout(Duration.ofSeconds(5)) {
            assertTrue(true)
            Thread.sleep(3 * 1000)
            assertTrue(true)
        }
    }
}