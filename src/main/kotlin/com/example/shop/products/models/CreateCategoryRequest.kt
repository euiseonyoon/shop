package com.example.shop.products.models

data class CreateCategoryRequest(
    val name: String,
    val isEnabled: Boolean? = null,
    val children: List<CreateCategoryRequest>? = null,
    val parentId: Long? = null,
)
