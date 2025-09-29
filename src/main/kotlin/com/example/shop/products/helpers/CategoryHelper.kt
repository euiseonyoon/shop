package com.example.shop.products.helpers

import com.example.shop.products.domain.Category
import com.example.shop.products.models.CreateCategoryRequest
import com.example.shop.products.models.UpdateCategoryRequest

interface CategoryHelper {
    fun makeCategoriesToInsert(
        requests: List<CreateCategoryRequest>,
        parent: Category?,
    ): List<Category>

    fun modifyCategoriesToUpdate(
        targetCategories: List<Category>,
        requestMap: Map<Long, UpdateCategoryRequest>,
    ): List<Category>
}
