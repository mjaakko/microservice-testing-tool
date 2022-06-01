package xyz.malkki.microservicetest.utils

import org.testcontainers.containers.GenericContainer

/**
 * Tries to stop the container, but catches all exceptions
 */
internal fun GenericContainer<*>.stopSafely() {
    try {
        stop()
    } catch (e: Exception) {
        println("Failed to stop container $containerId (${dockerImageName}): ${e.message}")
    }
}