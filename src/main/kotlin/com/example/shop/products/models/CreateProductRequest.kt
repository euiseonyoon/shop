package com.example.shop.products.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateProductRequest(
    val name: String,
    val stock: Int,
    val price: Int,
    val categoryId: Long,
    val isEnabled: Boolean,
)
