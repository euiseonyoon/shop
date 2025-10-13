package com.example.shop.configurations

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class CommonConfig {
    @Bean
    fun json(): Json = Json { ignoreUnknownKeys = true }

    @Bean
    fun jpaQueryFactory(entityManager: EntityManager): JPAQueryFactory = JPAQueryFactory(entityManager)
}
