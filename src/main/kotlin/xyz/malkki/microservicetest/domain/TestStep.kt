package xyz.malkki.microservicetest.domain

/**
 * @property timeout Timeout in seconds. Null if there is no timeout
 */
internal data class TestStep(val id: String, val className: String, val parameters: Map<String, Any>, val timeout: Int?)
