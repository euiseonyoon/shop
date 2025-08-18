package com.example.shop.products.models

import com.example.shop.products.CATEGORY_PATH_DELIMITER
import com.example.shop.products.domain.Category

fun CreateCategoryRequest.toCategoryEntity(parent: Category?): Category {
    val categoryEntity = Category()

    categoryEntity.parent = parent
    categoryEntity.name = this.name
    categoryEntity.isEnabled = this.isEnabled ?: true
    categoryEntity.isLast = this.children.isNullOrEmpty()
    categoryEntity.fullPath = if (parent == null) {
        this.name
    } else {
        parent.fullPath!! + CATEGORY_PATH_DELIMITER + this.name
    }
    return categoryEntity
}
