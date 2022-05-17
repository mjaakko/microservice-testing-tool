package xyz.malkki.microservicetest.domain

data class TestSuite(val id: String, val name: String, val services: List<String>, val steps: List<String>)
