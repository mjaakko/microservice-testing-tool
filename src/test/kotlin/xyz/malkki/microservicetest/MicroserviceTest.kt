package xyz.malkki.microservicetest

import xyz.malkki.microservicetest.testexecution.TestSuiteRunner
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class MicroserviceTest {
    @TestFactory
    fun `Test microservices`(): Collection<DynamicTest> {
        return TestSuiteRunner.generateDynamicTests()
    }
}