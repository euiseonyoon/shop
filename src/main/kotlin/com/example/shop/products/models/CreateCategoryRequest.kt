package com.example.shop.products.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val isEnabled: Boolean? = null,
    val children: List<CreateCategoryRequest>? = null,
    val parentId: Long? = null,
)
