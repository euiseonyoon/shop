package com.example.shop.products.respositories

import com.example.shop.products.domain.Category
import com.example.shop.products.respositories.extensions.CategoryRepositoryExtension
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long>, CategoryRepositoryExtension {
    fun findAllByParent(parent: Category): List<Category>

    fun existsByParent(parent: Category): Boolean
}
