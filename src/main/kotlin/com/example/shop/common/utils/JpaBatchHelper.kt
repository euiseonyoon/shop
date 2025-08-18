package com.example.shop.common.utils

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class JpaBatchHelper (
    private val entityManager: EntityManager,
    @Value("\${spring.jpa.properties.hibernate.jdbc.batch_size}")
    val batchSize: Int,
) {
    fun <T> batchInsert(entities: List<T>): List<T> {
        return batchExecute(entities) {
            entityManager.persist(it)
            it
        }
    }

    fun <T> batchUpdate(entities: List<T>) : List<T> {
        return batchExecute(entities) { entityManager.merge(it) }
    }

    fun <T> batchExecute(entities: List<T>, operation: (T) -> T): List<T> {
        val persistedEntities = mutableListOf<T>()

        entities.forEachIndexed { index, entity ->
            val result = operation(entity)
            persistedEntities.add(result)

            if ((index + 1) % batchSize == 0) {
                entityManager.flush()
                entityManager.clear()
            }
        }

        entityManager.flush()
        entityManager.clear()

        return persistedEntities
    }
}
