package xyz.malkki.microservicetest.utils

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DependencyGraphTest {
    @Test
    fun `Test dependency graph gives correct sorted list`() {
        val dependencyGraph = DependencyGraph.Builder()
            .addDependencies("a")
            .addDependencies("b", "a")
            .addDependencies("c", "a", "b")
            .addDependencies("d")
            .addDependencies("e", "a", "c")
            .build()

        val sortedList = dependencyGraph.asSortedList()

        assertTrue(sortedList.indexOf("a") < sortedList.indexOf("b"), "a is before b in the sorted list")
        assertTrue(sortedList.indexOf("b") < sortedList.indexOf("c"), "b is before c in the sorted list")
        assertTrue(sortedList.indexOf("c") > sortedList.indexOf("a"), "c is after a in the sorted list")
        assertTrue(sortedList.indexOf("c") > sortedList.indexOf("b"), "c is after b in the sorted list")
    }

    @Test
    fun `Test graph with cycles`() {
        assertThrows(DependencyGraph.CyclicDependenciesException::class.java) {
            val dependencyGraph = DependencyGraph.Builder()
                .addDependencies("a", "b")
                .addDependencies("b", "c")
                .addDependencies("c", "a")
                .build()

            dependencyGraph.asSortedList()
        }
    }
}