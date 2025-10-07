package com.example.shop.common

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
open class TestPostgresqlContainer {
    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:15")
            .apply {
                this.withDatabaseName("testDb")
                this.withPassword("sa")
                this.withPassword("sa")
            }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("spring.datasource.hikari.maximum-pool-size") { 30 }

        }
    }
}
