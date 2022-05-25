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

        return sorted
    }
}