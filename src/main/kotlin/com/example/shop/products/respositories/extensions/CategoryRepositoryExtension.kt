package com.example.shop.products.respositories.extensions

import com.example.shop.products.domain.Category
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CategoryRepositoryExtension {
    fun searchWithIdsOrNames(names: List<String>?, ids: List<Long>?, pageable: Pageable): Page<Category>

    fun searchByIdIncludeChildren(id: Long, includeChildren: Boolean): List<Category>
}
