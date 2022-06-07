package xyz.malkki.microservicetest.utils

/**
 * @property dependencyMap Map of dependencies. Key is the resource name and the value is its dependencies
 */
internal class DependencyGraph(private val dependencyMap: Map<String, List<String>>) {
    private val dependenciesReversed = dependencyMap.entries.flatMap { (resource, dependencies) -> dependencies.map { it to resource } }.groupBy({ it.first }, { it.second })

    class Builder {
        private val dependencyMap: MutableMap<String, MutableList<String>> = mutableMapOf()

        fun addDependencies(resource: String, vararg dependencies: String): Builder {
            addDependencies(resource, listOf(*dependencies))

            return this
        }

        fun addDependencies(resource: String, dependencies: List<String>): Builder {
            dependencyMap.compute(resource) { _: String, previous: MutableList<String>? ->
                val new = dependencies.toMutableList()
                if (previous != null) {
                    new.addAll(previous)
                }
                return@compute new
            }

            return this
        }

        fun build(): DependencyGraph = DependencyGraph(dependencyMap)
    }

    /**
     * @return Dependencies in topological sort
     * @throws CyclicDependenciesException when cycles are detected in dependencies
     */
    fun asSortedList(): List<String> {
        val sorted = mutableListOf<String>()

        val noDependencies = dependencyMap.filter { (_, dependencies) -> dependencies.isEmpty() }.keys.toMutableList()

        while (noDependencies.isNotEmpty()) {
            val node = noDependencies.removeFirst()
            sorted.add(node)

            for (neighbour in dependenciesReversed[node] ?: emptyList()) {
                if (dependencyMap[neighbour]!!.all { it in sorted }) {
                    noDependencies.add(neighbour)
                }
            }
        }

        if (sorted.size != dependencyMap.keys.size) {
            checkCycles()
        }

        return sorted
    }

    private fun checkCycles() {
        fun findCycles(origin: String, path: List<String>, cycles: MutableList<List<String>>) {
            val current = path.last()
            val dependencies = dependencyMap[current] ?: emptyList()
            for (dependency in dependencies) {
                if (dependency == origin) {
                    cycles.add(path + origin)
                } else {
                    findCycles(origin, path + dependency, cycles)
                }
            }
        }

        val cycles = dependencyMap.keys.flatMap { resource ->
            val cycles = mutableListOf<List<String>>()
            findCycles(resource, listOf(resource), cycles)
            return@flatMap cycles.toList()
        }.toSet()
        if (cycles.isNotEmpty()) {
            throw CyclicDependenciesException(cycles)
        }
    }

    class CyclicDependenciesException(val cycles: Collection<List<String>>) : Exception("Cyclic dependencies in graph: ${cycles.joinToString(", ") { cycle -> cycle.joinToString(" -> ") }}")
}