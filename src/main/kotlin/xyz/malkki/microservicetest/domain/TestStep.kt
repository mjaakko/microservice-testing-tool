package xyz.malkki.microservicetest.domain

data class TestStep(val id: String, val className: String, val dependencies: Set<String>)
