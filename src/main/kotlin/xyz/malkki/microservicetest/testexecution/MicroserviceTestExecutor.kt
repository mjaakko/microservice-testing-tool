package xyz.malkki.microservicetest.testexecution

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import xyz.malkki.microservicetest.utils.BetterSummaryGeneratingListener
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime

@ExperimentalTime
class MicroserviceTestExecutor {
    companion object {
        fun runMicroserviceTests(exitAfterDone: Boolean = true) {
            val listener = BetterSummaryGeneratingListener()

            val request = LauncherDiscoveryRequestBuilder.request().selectors(selectClass(MicroserviceTestExecutor::class.java)).build()
            val launcher = LauncherFactory.create()
            val testPlan = launcher.discover(request)
            launcher.registerTestExecutionListeners(listener)
            launcher.execute(testPlan)

            listener.printSummary(System.out)

            if (exitAfterDone) {
                exitProcess(if (listener.allSuccessful) { 0 } else { 1 })
            }
        }
    }

    @TestFactory
    fun `Test microservices with microservice-testing-tool`(): Collection<DynamicTest> = TestSuiteRunner.generateDynamicTests()
}