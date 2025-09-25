package com.example.shop.products.models

import com.example.shop.products.CATEGORY_PATH_DELIMITER
import com.example.shop.products.domain.Category
import com.example.shop.products.domain.Product

fun CreateCategoryRequest.toCategoryEntity(parent: Category?): Category {
    return Category(
        parent = parent,
        name = this.name,
        isEnabled = this.isEnabled ?: true,
        isLast = this.children.isNullOrEmpty(),
        fullPath = parent?.let { parent.fullPath + CATEGORY_PATH_DELIMITER + this.name } ?: this.name
    )
}

fun CreateProductRequest.toEntity(category: Category): Product {
    return Product(name, stock, price, category, isEnabled)
}
