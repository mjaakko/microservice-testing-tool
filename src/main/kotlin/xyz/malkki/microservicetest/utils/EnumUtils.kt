package xyz.malkki.microservicetest.utils

internal inline fun <reified E : Enum<E>> enumValueOfSafe(value: String): E? {
    return try {
        enumValueOf<E>(value)
    } catch (iae: IllegalArgumentException) {
        null
    }
}