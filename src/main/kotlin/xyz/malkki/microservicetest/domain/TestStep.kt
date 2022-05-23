package xyz.malkki.microservicetest.domain

/**
 * @property timeout Timeout in seconds. Null if there is no timeout
 */
data class TestStep(val id: String, val className: String, val dependencies: Set<String>, val timeout: Int?)
