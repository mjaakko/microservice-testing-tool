package xyz.malkki.microservicetest.utils

import java.util.*

internal fun <T> Optional<T>.getNullable(): T? = if (isPresent) { get() } else { null }