package com.example.shop.products.respositories

import com.example.shop.products.domain.Product
import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints

interface ProductRepository : JpaRepository<Product, Long> {
    fun findAllByCategoryIdIn(categoryIds: List<Long>, pageable: Pageable): Page<Product>

    fun findAllByIdIn(productIds: List<Long>): List<Product>

    fun existsByIdAndIsEnabledTrue(productId: Long): Boolean
}
