package xyz.malkki.microservicetest

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import xyz.malkki.microservicetest.testexecution.TestSuiteRunner
import kotlin.time.ExperimentalTime

@ExperimentalTime
class MicroserviceTest {
    @TestFactory
    fun `Test microservices`(): Collection<DynamicTest> {
        return TestSuiteRunner.generateDynamicTests()
    }
}