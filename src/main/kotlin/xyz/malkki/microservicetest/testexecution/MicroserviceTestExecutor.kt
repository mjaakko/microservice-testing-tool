package xyz.malkki.microservicetest.testexecution

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import java.io.PrintWriter

class MicroserviceTestExecutor {
    companion object {
        fun runMicroserviceTests() {
            val listener = SummaryGeneratingListener()

            val request = LauncherDiscoveryRequestBuilder.request().selectors(selectClass(MicroserviceTestExecutor::class.java)).build()
            val launcher = LauncherFactory.create()
            val testPlan = launcher.discover(request)
            launcher.registerTestExecutionListeners(listener)
            launcher.execute(testPlan)

            listener.summary.printFailuresTo(PrintWriter(System.out))
            listener.summary.printTo(PrintWriter(System.out))
        }
    }

    @TestFactory
    fun `Test microservices with microservice-testing-tool`(): Collection<DynamicTest> = TestSuiteRunner.generateDynamicTests()
}