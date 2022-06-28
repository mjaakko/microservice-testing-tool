import xyz.malkki.microservicetest.testdefinition.InvalidConfigurationException

/**
 * Checks if the key exists in the map and throws an error with more helpful message if not
 */
internal fun Map<String, Any>.checkKey(key: String) {
    if (!containsKey(key)) {
        throw InvalidConfigurationException("Missing '$key' from configuration")
    }
}