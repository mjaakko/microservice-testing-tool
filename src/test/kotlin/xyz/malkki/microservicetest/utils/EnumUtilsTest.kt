package xyz.malkki.microservicetest.utils

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import xyz.malkki.microservicetest.domain.Microservice

class EnumUtilsTest {
    @Test
    fun `Test existing enum value`() {
        assertDoesNotThrow {
            val value = enumValueOfSafe<Microservice.Volume.Type>("FILESYSTEM")
            assertNotNull(value)
        }
    }

    @Test
    fun `Test non-existing enum value`() {
        assertDoesNotThrow {
            val value = enumValueOfSafe<Microservice.Volume.Type>("TEST")
            assertNull(value)
        }
    }
}