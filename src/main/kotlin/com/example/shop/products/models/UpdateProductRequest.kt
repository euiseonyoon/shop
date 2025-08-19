package com.example.shop.products.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProductRequest(
    val id: Long,
    val name: String? = null,
    val count: Int? = null,
    val price: Int? = null,
    val categoryId: Long? = null,
)
