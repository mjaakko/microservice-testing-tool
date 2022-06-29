package xyz.malkki.microservicetest.utils

import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import java.io.PrintStream
import java.time.Duration
import kotlin.properties.Delegates

class BetterSummaryGeneratingListener : TestExecutionListener {
    private var start by Delegates.notNull<Long>()
    private var finish by Delegates.notNull<Long>()

    private val failed = mutableMapOf<TestIdentifier, TestExecutionResult>()
    private val aborted = mutableMapOf<TestIdentifier, TestExecutionResult>()
    private val successful = mutableMapOf<TestIdentifier, TestExecutionResult>()
    private val skipped = mutableMapOf<TestIdentifier, String>()

    val allSuccessful: Boolean
        get() = failed.isEmpty()

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        start = System.nanoTime()
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        finish = System.nanoTime()
    }

    override fun executionSkipped(testIdentifier: TestIdentifier, reason: String) {
        if (testIdentifier.isTest) {
            skipped[testIdentifier] = reason
        }
    }

    override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
        if (testIdentifier.isTest) {
            when (testExecutionResult.status!!) {
                TestExecutionResult.Status.FAILED -> {
                    failed[testIdentifier] = testExecutionResult
                }
                TestExecutionResult.Status.ABORTED -> {
                    aborted[testIdentifier] = testExecutionResult
                }
                TestExecutionResult.Status.SUCCESSFUL -> {
                    successful[testIdentifier] = testExecutionResult
                }
            }
        }
    }

    fun printSummary(printStream: PrintStream) {
        val duration = Duration.ofNanos(finish - start)
        printStream.println("Tests executed in ${duration.prettyFormat()}")
        printLines(printStream)
        printTests(printStream, "skipped", skipped.keys)
        printTests(printStream, "aborted", aborted.keys)
        printTests(printStream, "successful", successful.keys)
        printTests(printStream, "failed", failed.keys)
        printLines(printStream)
        failed.forEach { (testIdentifier, testResult) ->
            printStream.println("${testIdentifier.displayName} failed: ${testResult.throwable.getNullable()?.message}")
            testResult.throwable.getNullable()?.printStackTrace(printStream)
            printStream.println()
        }
    }

    private fun printTests(printStream: PrintStream, state: String, tests: Collection<TestIdentifier>) {
        printStream.println("Tests $state (${tests.size}): ${tests.joinToString(", ") { it.displayName }}")
    }

    private fun printLines(printStream: PrintStream) = printStream.println("----------------------")

    private fun Duration.prettyFormat(): String {
        val minutes = toMinutes()
        val seconds = toSeconds() - minutes * 60
        val milliseconds = toMillis() - minutes * 60 * 1000 - seconds * 1000

        val parts = listOfNotNull(
            if (minutes > 0) { "${minutes}min" } else { null },
            if (seconds > 0) { "${seconds}s" } else { null },
            if (milliseconds > 0) { "${milliseconds}ms" } else { null }
        )

        return parts.joinToString("\u00A0")
    }
}