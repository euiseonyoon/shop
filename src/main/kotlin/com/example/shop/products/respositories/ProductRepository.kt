package com.example.shop.products.respositories

import com.example.shop.products.domain.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long> {
    fun findAllByCategoryIdIn(categoryIds: List<Long>, pageable: Pageable): Page<Product>
}
